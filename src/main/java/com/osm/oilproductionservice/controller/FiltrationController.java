package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.FiltrationRequestDto;
import com.osm.oilproductionservice.dto.FiltrationResultDto;
import com.osm.oilproductionservice.dto.UpdateFiltrationStatusDto;
import com.osm.oilproductionservice.service.FiltrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.UID;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/filtration")
public class FiltrationController {

    private static final Logger logger = LoggerFactory.getLogger(FiltrationController.class);
    private final FiltrationService filtrationService;

    public FiltrationController(FiltrationService filtrationService) {
        this.filtrationService = filtrationService;
    }

    @PostMapping
    public ResponseEntity<?> createOpFiltrage(@Valid @RequestBody FiltrationRequestDto req) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Starting filtration process with request: {}", requestId, req);

            FiltrationResultDto result = filtrationService.filter(req);

            logger.info("Request ID: {} - Filtration created successfully with status: {}", requestId, result.getStatus());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Invalid input parameters: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Invalid input: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Unexpected error during filtration: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred during filtration"));
        }
    }

    @PutMapping("/{operationId}/start")
    public ResponseEntity<?> startFiltration(@PathVariable Long operationId) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Starting filtration operation with ID: {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.startFiltration(operationId);

            logger.info("Request ID: {} - Filtration operation {} started successfully", requestId, operationId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Invalid operation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Invalid operation: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Unexpected error starting filtration: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred while starting the filtration"));
        }
    }

    @PutMapping("/{operationId}/complete")
    public ResponseEntity<?> completeFiltration(@PathVariable Long operationId) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Completing filtration operation with ID: {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.completeFiltration(operationId);

            logger.info("Request ID: {} - Filtration operation {} completed successfully", requestId, operationId);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Invalid operation: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Invalid operation: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Unexpected error completing filtration: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred while completing the filtration"));
        }
    }

    @PutMapping("/{operationId}/status")
    public ResponseEntity<?> updateFiltrationStatus(
            @PathVariable Long operationId,
            @Valid @RequestBody UpdateFiltrationStatusDto statusDto) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Updating filtration operation {} status to: {}",
                    requestId, operationId, statusDto.getStatus());

            FiltrationResultDto result = filtrationService.updateFiltrationStatus(operationId, statusDto);

            logger.info("Request ID: {} - Filtration operation {} status updated successfully to {}",
                    requestId, operationId, result.getStatus());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Invalid operation or status: {}", requestId, e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Invalid operation or status: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Unexpected error updating filtration status: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred while updating the filtration status"));
        }
    }

    @GetMapping("/{operationId}")
    public ResponseEntity<?> getFiltrationStatus(@PathVariable UUID operationId) {
        String requestId = generateRequestId();

        try {
            logger.info("Request ID: {} - Getting filtration operation status for ID: {}", requestId, operationId);

            FiltrationResultDto result = filtrationService.getFiltrationStatus(operationId);

            logger.info("Request ID: {} - Filtration operation {} status retrieved: {}",
                    requestId, operationId, result.getStatus());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            logger.error("Request ID: {} - Operation not found: {}", requestId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Operation not found: " + e.getMessage()));

        } catch (Exception e) {
            logger.error("Request ID: {} - Unexpected error getting filtration status: {}", requestId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred while getting the filtration status"));
        }
    }

    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    // Simple inner class for error responses
    private static class ErrorResponse {
        private final String message;
        private final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}