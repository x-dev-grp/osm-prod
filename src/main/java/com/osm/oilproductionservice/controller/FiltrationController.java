package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.service.FiltrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/filtration")
public class FiltrationController {

    private static final Logger logger = LoggerFactory.getLogger(FiltrationController.class);
    private final FiltrationService filtrationService;

    public FiltrationController(FiltrationService filtrationService) {
        this.filtrationService = filtrationService;
    }

    /**
     * Créer une nouvelle opération de filtration
     */
    @PostMapping
    public ResponseEntity<?> createFiltration(@Valid @RequestBody FiltrationRequestDto req) {
        try {

            FiltrationResultDto result = filtrationService.createFiltration(req);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur de validation: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }



    //endpoint de modification générale
    @PutMapping("/{operationId}")
    public ResponseEntity<?> updateFiltration(
            @PathVariable UUID operationId,
            @Valid @RequestBody FiltrationRequestDto req) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Modification de l'opération {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.updateFiltration(operationId, req);

            logger.info("Request ID: {} - Opération {} modifiée avec succès", requestId, operationId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Request ID: {} - Erreur de validation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }


    @PutMapping("/{operationId}/start")
    public ResponseEntity<?> startFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {

            FiltrationResultDto result = filtrationService.startFiltration(operationId);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    @PutMapping("/{operationId}/complete")
    public ResponseEntity<?> completeFiltration(
            @PathVariable UUID operationId,
            @Valid @RequestBody FiltrationCompletionDto completionData) { // [NOUVEAU] Paramètre ajouté
        String requestId = generateRequestId();

        try {
            // Appel du service avec les données de completion
            FiltrationResultDto result = filtrationService.completeFiltration(operationId, completionData);

            logger.info("Request ID: {} - Opération {} terminée avec succès", requestId, operationId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Erreur de validation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    @PutMapping("/{operationId}/status")
    public ResponseEntity<?> updateFiltrationStatus(
            @PathVariable UUID operationId,
            @Valid @RequestBody UpdateFiltrationStatusDto statusDto) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Mise à jour du statut de l'opération {} vers {}",
                    requestId, operationId, statusDto.getStatus());

            FiltrationResultDto result = filtrationService.updateFiltrationStatus(operationId, statusDto);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }


    @PutMapping("/{operationId}/note")
    public ResponseEntity<?> addNote(
            @PathVariable UUID operationId,
            @RequestBody String note) { // La note est envoyée dans le body
        String requestId = generateRequestId();

        try {

            // [NOUVEAU] Appel à la nouvelle méthode du service
            FiltrationResultDto result = filtrationService.addNote(operationId, note);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }
    @DeleteMapping("/{operationId}")
    public ResponseEntity<?> deleteFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();
        try {

            filtrationService.deleteFiltration(operationId);

            return ResponseEntity.noContent().build(); // 204 — matches Observable<void> on the frontend

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Opération non trouvée: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }
    /**
     *  Récupérer une opération spécifique
     */
    @GetMapping("/{operationId}")
    public ResponseEntity<?> getFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {

            FiltrationResultDto result = filtrationService.getFiltrationById(operationId);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Opération non trouvée: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    /**
     * Récupérer toutes les opérations
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllFiltrations() {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Récupération de toutes les opérations", requestId);

            List<FiltrationResultDto> results = filtrationService.getAllFiltrations();

            logger.info("Request ID: {} - {} opérations trouvées", requestId, results.size());
            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getFiltrationsByStatus(@PathVariable FiltrationStatus status) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Récupération des opérations avec statut: {}", requestId, status);

            // [NOUVEAU] Appel à la nouvelle méthode du service
            List<FiltrationResultDto> results = filtrationService.getFiltrationsByStatus(status);

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }




    }

    // retourner les détails de traçabilité d’une opération
    @GetMapping("/{operationId}/traceability")
    public ResponseEntity<?> getTraceability(@PathVariable UUID operationId) {
        FiltrationResultDto dto = filtrationService.getFiltrationById(operationId);
        // On peut aussi enrichir avec les livraisons associées au lot source.
        return ResponseEntity.ok(dto);
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static class ErrorResponse {
        private final String message;
        private final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}