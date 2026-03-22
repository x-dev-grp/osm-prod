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
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Création d'une opération de filtration", requestId);
            logger.debug("Request ID: {} - Détails: source={}, target={}, volume={}",
                    requestId, req.getSource(), req.getTarget(), req.getVolumeToFilter());

            FiltrationResultDto result = filtrationService.createFiltration(req);

            logger.info("Request ID: {} - Opération créée avec succès, ID: {}", requestId, result.getOperationId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Erreur de validation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Erreur de validation: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    /**
     * [DÉJÀ EXISTANT] Démarrer une opération (CREATED -> IN_PROGRESS)
     */
    @PutMapping("/{operationId}/start")
    public ResponseEntity<?> startFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Démarrage de l'opération ID: {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.startFiltration(operationId);

            logger.info("Request ID: {} - Opération {} démarrée avec succès", requestId, operationId);
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

    /**
     * [MODIFIÉ] Terminer une opération avec mise à jour des unités de stockage
     *
     * MODIFICATIONS APPORTÉES:
     * 1. Ajout du paramètre @RequestBody FiltrationCompletionDto pour recevoir le volume après filtration
     * 2. La méthode appelle maintenant completeFiltration avec les données de completion
     * 3. Le service mettra à jour les unités de stockage automatiquement
     */
    @PutMapping("/{operationId}/complete")
    public ResponseEntity<?> completeFiltration(
            @PathVariable UUID operationId,
            @Valid @RequestBody FiltrationCompletionDto completionData) { // [NOUVEAU] Paramètre ajouté
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Terminaison de l'opération ID: {}", requestId, operationId);
            // [NOUVEAU] Log du volume après filtration
            logger.info("Request ID: {} - Volume après filtration: {} L", requestId, completionData.getVolumeAfter());

            // [MODIFIÉ] Appel du service avec les données de completion
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

    /**
     * [DÉJÀ EXISTANT] Mettre à jour le statut d'une opération
     */
    @PutMapping("/{operationId}/status")
    public ResponseEntity<?> updateFiltrationStatus(
            @PathVariable UUID operationId,
            @Valid @RequestBody UpdateFiltrationStatusDto statusDto) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Mise à jour du statut de l'opération {} vers {}",
                    requestId, operationId, statusDto.getStatus());

            FiltrationResultDto result = filtrationService.updateFiltrationStatus(operationId, statusDto);

            logger.info("Request ID: {} - Statut mis à jour avec succès", requestId);
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

    /**
     * [NOUVEAU] Ajouter une note à une opération
     *
     * Ce nouvel endpoint permet d'ajouter une note sans changer le statut
     * Utile pour les commentaires ou observations pendant l'opération
     */
    @PutMapping("/{operationId}/note")
    public ResponseEntity<?> addNote(
            @PathVariable UUID operationId,
            @RequestBody String note) { // La note est envoyée dans le body
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Ajout d'une note à l'opération {}", requestId, operationId);

            // [NOUVEAU] Appel à la nouvelle méthode du service
            FiltrationResultDto result = filtrationService.addNote(operationId, note);

            logger.info("Request ID: {} - Note ajoutée avec succès", requestId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Erreur: {}", requestId, e.getMessage());
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

    /**
     * [DÉJÀ EXISTANT] Récupérer une opération spécifique
     */
    @GetMapping("/{operationId}")
    public ResponseEntity<?> getFiltration(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Consultation de l'opération ID: {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.getFiltrationById(operationId);

            logger.info("Request ID: {} - Opération trouvée", requestId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Opération non trouvée: {}", requestId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Opération non trouvée: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Erreur inattendue: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Une erreur technique est survenue"));
        }
    }

    /**
     * [DÉJÀ EXISTANT] Récupérer toutes les opérations
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

    /**
     * [NOUVEAU] Récupérer les opérations filtrées par statut
     *
     * Exemple: /api/production/filtration/status/CREATED
     * Retourne toutes les opérations avec le statut CREATED
     */
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