package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.FiltrationRequestDto;
import com.osm.oilproductionservice.dto.FiltrationResultDto;
import com.osm.oilproductionservice.dto.FiltrationStatu;
import com.osm.oilproductionservice.dto.UpdateFiltrationStatusDto;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.math3.util.Precision.round;

@Service
public class FiltrationService {
    private static final Logger logger = LoggerFactory.getLogger(FiltrationService.class);

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationRepo;

    public FiltrationService(StorageUnitRepo storageUnitRepo, FiltrationOperationRepo filtrationRepo) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
    }
    @Transactional
    public FiltrationResultDto filter(FiltrationRequestDto req) {
        String operationId = generateOperationId();

        try {
            logger.info("Operation ID: {} - Starting filtration process", operationId);
            logger.debug("Operation ID: {} - Request details: source={}, target={}, volume={}",
                    operationId, req.getSource(), req.getTarget(), req.getVolumeToFilter());

            // Validate input
            validateRequest(req);

            var sourceId = req.getSource();
            var targetId = req.getTarget();
            double volumeToFilter = req.getVolumeToFilter();

            // 1) Charger les cuves
            StorageUnit sourceUnit = findStorageUnitById(sourceId, "Source");
            StorageUnit targetUnit = findStorageUnitById(targetId, "Target");

            logger.info("Operation ID: {} - Storage units loaded - Source: {} (current volume: {}), Target: {} (current volume: {})",
                    operationId, sourceId, sourceUnit.getCurrentVolume(), targetId, targetUnit.getCurrentVolume());

            // 2) Validations métier
            validateBusinessRules(sourceUnit, targetUnit, volumeToFilter, operationId);

            // 3) Create and save filtration operation
            FiltrationOperation operation = createFiltrationOperation(sourceUnit, targetUnit, volumeToFilter, req.getNote());
            filtrationRepo.save(operation);

            logger.info("Operation ID: {} - Filtration operation created successfully with ID: {}",
                    operationId, operation.getId());

            // 4) Create result DTO
            return createFiltrationResult(operation, operationId);

        } catch (IllegalArgumentException e) {
            logger.error("Operation ID: {} - Validation error: {}", operationId, e.getMessage());
            throw e; // Let the controller handle validation errors

        } catch (Exception e) {
            logger.error("Operation ID: {} - Unexpected error during filtration: {}", operationId, e.getMessage(), e);
            throw new RuntimeException("Failed to process filtration operation: " + e.getMessage(), e);
        }
    }

    private void validateRequest(FiltrationRequestDto req) {
        if (req == null) {
            throw new IllegalArgumentException("Filtration request cannot be null");
        }
        if (req.getSource() == null) {
            throw new IllegalArgumentException("Source storage unit ID cannot be null");
        }
        if (req.getTarget() == null) {
            throw new IllegalArgumentException("Target storage unit ID cannot be null");
        }
        if (req.getVolumeToFilter() <= 0) {
            throw new IllegalArgumentException("Volume to filter must be positive");
        }
    }

    private StorageUnit findStorageUnitById(UUID id, String unitType) {
        return storageUnitRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("%s storage unit not found with ID: %d", unitType, id)));
    }

    private void validateBusinessRules(StorageUnit source, StorageUnit target, double volumeToFilter, String operationId) {
        // Check if source has enough volume
        if (volumeToFilter > source.getCurrentVolume()) {
            logger.warn("Operation ID: {} - Insufficient volume in source. Available: {}, Requested: {}",
                    operationId, source.getCurrentVolume(), volumeToFilter);
            throw new IllegalArgumentException(
                    String.format("Insufficient volume in source storage unit. Available: %.2f, Requested: %.2f",
                            source.getCurrentVolume(), volumeToFilter));
        }

        // Check if target has enough capacity
        double targetNewVolume = target.getCurrentVolume() + volumeToFilter;
        if (targetNewVolume > target.getMaxCapacity()) {
            logger.warn("Operation ID: {} - Insufficient capacity in target. Current: {}, Max: {}, Would become: {}",
                    operationId, target.getCurrentVolume(), target.getMaxCapacity(), targetNewVolume);
            throw new IllegalArgumentException(
                    String.format("Insufficient capacity in target storage unit. Current: %.2f, Max capacity: %.2f, Would become: %.2f",
                            target.getCurrentVolume(), target.getMaxCapacity(), targetNewVolume));
        }

        // Optional: Check if source and target are different
        if (source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Source and target storage units must be different");
        }

        logger.info("Operation ID: {} - Business rules validation passed", operationId);
    }

    private FiltrationOperation createFiltrationOperation(StorageUnit source, StorageUnit target,
                                                          double volumeToFilter, String note) {
        FiltrationOperation op = new FiltrationOperation();
        op.setSourceStorageUnit(source);
        op.setTargetStorageUnit(target);
        op.setVolumeToFilter(volumeToFilter);
        op.setStatus(FiltrationStatu.CREATED);
        op.setNote(note);
        return op;
    }

    private FiltrationResultDto createFiltrationResult(FiltrationOperation operation, String operationId) {
        FiltrationResultDto result = new FiltrationResultDto();

        result.setSourceId(operation.getSourceStorageUnit().getId());
        result.setTargetId(operation.getTargetStorageUnit().getId());
        result.setVolumeFiltered(operation.getVolumeToFilter());
        result.setStatus(operation.getStatus().toString());
        result.setTimestamp(LocalDateTime.now()); // Assuming you have createdAt field
        result.setNote(operation.getNote());

        logger.info("Operation ID: {} - Filtration result prepared successfully", operationId);
        return result;
    }

    private String generateOperationId() {
        return "FILTER-" + System.currentTimeMillis() + "-" +
                java.util.UUID.randomUUID().toString().substring(0, 4);
    }
    // Ajouter ces méthodes dans FiltrationService.java

    @Transactional
    public FiltrationResultDto startFiltration(UUID operationId) {
        String operationId_log = generateOperationId();

        try {
            logger.info("Operation ID: {} - Starting filtration operation with ID: {}", operationId_log, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Vérifier que l'opération est dans un état valide pour démarrer
            if (operation.getStatus() != FiltrationStatu.CREATED) {
                throw new IllegalArgumentException(
                        String.format("Cannot start filtration operation with status: %s. Expected status: CREATED",
                                operation.getStatus()));
            }

            // Mettre à jour le statut
            operation.setStatus(FiltrationStatu.IN_PROGRESS);
            filtrationRepo.save(operation);

            logger.info("Operation ID: {} - Filtration operation {} started successfully", operationId_log, operationId);

            return createFiltrationResult(operation, operationId_log);

        } catch (IllegalArgumentException e) {
            logger.error("Operation ID: {} - Validation error: {}", operationId_log, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Operation ID: {} - Unexpected error starting filtration: {}", operationId_log, e.getMessage(), e);
            throw new RuntimeException("Failed to start filtration operation: " + e.getMessage(), e);
        }
    }

    @Transactional
    public FiltrationResultDto completeFiltration(UUID operationId) {
        String operationId_log = generateOperationId();

        try {
            logger.info("Operation ID: {} - Completing filtration operation with ID: {}", operationId_log, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Vérifier que l'opération est en cours
            if (operation.getStatus() != FiltrationStatu.IN_PROGRESS) {
                throw new IllegalArgumentException(
                        String.format("Cannot complete filtration operation with status: %s. Expected status: IN_PROGRESS",
                                operation.getStatus()));
            }

            // Mettre à jour le statut
            operation.setStatus(FiltrationStatu.COMPLETED);

            // Mettre à jour les volumes après filtration (si nécessaire)
            // operation.setVolumeAfter(calculateVolumeAfter(operation));
            // operation.setLossVolume(calculateLoss(operation));
            // operation.setLossPercent(calculateLossPercent(operation));

            filtrationRepo.save(operation);

            logger.info("Operation ID: {} - Filtration operation {} completed successfully", operationId_log, operationId);

            return createFiltrationResult(operation, operationId_log);

        } catch (IllegalArgumentException e) {
            logger.error("Operation ID: {} - Validation error: {}", operationId_log, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Operation ID: {} - Unexpected error completing filtration: {}", operationId_log, e.getMessage(), e);
            throw new RuntimeException("Failed to complete filtration operation: " + e.getMessage(), e);
        }
    }

    @Transactional
    public FiltrationResultDto updateFiltrationStatus(UUID operationId, UpdateFiltrationStatusDto statusDto) {
        String operationId_log = generateOperationId();

        try {
            logger.info("Operation ID: {} - Updating filtration operation {} status to: {}",
                    operationId_log, operationId, statusDto.getStatus());

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Valider la transition d'état
            validateStatusTransition(operation.getStatus(), statusDto.getStatus());

            // Mettre à jour le statut
            operation.setStatus(statusDto.getStatus());

            // Mettre à jour les volumes si fournis
            if (statusDto.getVolumeAfter() != null) {
                operation.setVolumeAfter(statusDto.getVolumeAfter());
            }

            if (statusDto.getLossVolume() != null) {
                operation.setLossVolume(statusDto.getLossVolume());
            }

            if (statusDto.getLossPercent() != null) {
                operation.setLossPercent(statusDto.getLossPercent());
            }

            if (statusDto.getNote() != null) {
                operation.setNote(statusDto.getNote());
            }

            filtrationRepo.save(operation);

            logger.info("Operation ID: {} - Filtration operation {} status updated successfully to {}",
                    operationId_log, operationId, operation.getStatus());

            return createFiltrationResult(operation, operationId_log);

        } catch (IllegalArgumentException e) {
            logger.error("Operation ID: {} - Validation error: {}", operationId_log, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Operation ID: {} - Unexpected error updating filtration status: {}", operationId_log, e.getMessage(), e);
            throw new RuntimeException("Failed to update filtration status: " + e.getMessage(), e);
        }
    }

    public FiltrationResultDto getFiltrationStatus(UUID operationId) {
        String operationId_log = generateOperationId();

        try {
            logger.info("Operation ID: {} - Getting filtration operation status for ID: {}", operationId_log, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            logger.info("Operation ID: {} - Filtration operation {} status retrieved: {}",
                    operationId_log, operationId, operation.getStatus());

            return createFiltrationResult(operation, operationId_log);

        } catch (IllegalArgumentException e) {
            logger.error("Operation ID: {} - Operation not found: {}", operationId_log, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Operation ID: {} - Unexpected error getting filtration status: {}", operationId_log, e.getMessage(), e);
            throw new RuntimeException("Failed to get filtration status: " + e.getMessage(), e);
        }
    }

    private FiltrationOperation findFiltrationOperationById(UUID operationId) {
        return filtrationRepo.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Filtration operation not found with ID: %d", operationId)));
    }

    private void validateStatusTransition(FiltrationStatu currentStatus, FiltrationStatu newStatus) {
        // Définir les transitions valides
        boolean isValidTransition = false;

        switch (currentStatus) {
            case CREATED:
                isValidTransition = (newStatus == FiltrationStatu.IN_PROGRESS);
                break;
            case IN_PROGRESS:
                isValidTransition = (newStatus == FiltrationStatu.COMPLETED);
                break;
            case COMPLETED:
                isValidTransition = false; // Une opération terminée ne peut pas changer de statut
                break;
            default:
                isValidTransition = false;
        }

        if (!isValidTransition) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

}


