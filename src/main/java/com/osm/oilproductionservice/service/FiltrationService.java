package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.communicator.models.enums.TransactionState;
import com.xdev.communicator.models.enums.TransactionType;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.utils.BusinessCodeGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FiltrationService {

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationRepo;
    private final ModelMapper modelMapper;
    private final OilTransactionService oilTransactionService;
    private final BusinessCodeGenerator businessCodeGenerator;

    public FiltrationService(StorageUnitRepo storageUnitRepo, FiltrationOperationRepo filtrationRepo,
            org.modelmapper.ModelMapper modelMapper, OilTransactionService oilTransactionService,
            BusinessCodeGenerator businessCodeGenerator) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
        this.modelMapper = modelMapper;
        this.oilTransactionService = oilTransactionService;
        this.businessCodeGenerator = businessCodeGenerator;
    }

    @Transactional
    public void deleteFiltration(UUID operationId) {
        String traceId = generateOperationId();
        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            operation.setDeleted(true);
            filtrationRepo.save(operation);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression", e);
        }
    }

    @Transactional
    public FiltrationResultDto createFiltration(FiltrationRequestDto req) {

        try {

            StorageUnit sourceUnit = findStorageUnitById(req.getSource(), "Source");
            StorageUnit targetUnit = findStorageUnitById(req.getTarget(), "Target");

            validateBusinessRules(sourceUnit, targetUnit, req.getVolumeToFilter());

            // Récupération du lot source (peut être null)
            String sourceLotNumber = sourceUnit.getLotNumber();

            FiltrationOperation operation = new FiltrationOperation();
            operation.setSourceStorageUnit(sourceUnit);
            operation.setTargetStorageUnit(targetUnit);
            operation.setVolumeToFilter(req.getVolumeToFilter());
            operation.setStatus(FiltrationStatus.CREATED);
            operation.setNote(req.getNote());
            operation.setOperationDate(LocalDateTime.now());
            operation.setSourceLotNumber(sourceLotNumber); // ← Ajout du lot source

            FiltrationOperation saved = filtrationRepo.save(operation);

            return mapToDto(saved);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de l'opération", e);
        }
    }

    @Transactional
    public FiltrationResultDto startFiltration(UUID operationId) {

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            if (operation.getStatus() != FiltrationStatus.CREATED) {
                throw new IllegalArgumentException(String.format(
                        "Impossible de démarrer: statut actuel = %s, attendu = CREATED", operation.getStatus()));

            }

            operation.setStatus(FiltrationStatus.IN_PROGRESS);

            FiltrationOperation updated = filtrationRepo.save(operation);

            return mapToDto(updated);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {

            throw new RuntimeException("Erreur lors du démarrage", e);
        }
    }

    @Transactional
    public FiltrationResultDto completeFiltration(UUID operationId, FiltrationCompletionDto completionData) {

        try {

            // Récupération de l'opération
            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Vérification du statut
            if (operation.getStatus() != FiltrationStatus.IN_PROGRESS) {
                throw new IllegalArgumentException(String.format(
                        "Impossible de terminer: statut actuel = %s, attendu = IN_PROGRESS", operation.getStatus()));
            }

            // Validation du volume après filtration
            if (completionData.getVolumeAfter() == null || completionData.getVolumeAfter() < 0) {
                throw new IllegalArgumentException("Le volume après filtration doit être positif");
            }
            if (completionData.getVolumeAfter() > operation.getVolumeToFilter()) {
                throw new IllegalArgumentException("Le volume après filtration ne peut pas dépasser le volume initial");
            }

            // Calcul des pertes
            double volumeInitial = operation.getVolumeToFilter();
            double volumeAfter = completionData.getVolumeAfter();
            double lossVolume = volumeInitial - volumeAfter;
            double lossPercent = (lossVolume / volumeInitial) * 100;

            // Récupération des unités de stockage
            StorageUnit sourceUnit = operation.getSourceStorageUnit();
            StorageUnit targetUnit = operation.getTargetStorageUnit();

            // Generate the target lot number with the shared business-code format.
            String targetLotNumber = businessCodeGenerator.generate(FiltrationOperation.class, "targetLotNumber", "FI");

            targetUnit.setLotNumber(targetLotNumber);
            targetUnit.setFilteredOil(true);
            targetUnit.setLastFiltrationDate(LocalDateTime.now());
            targetUnit.setQualityGrade(sourceUnit.getQualityGrade()); // Set quality grade from source
            targetUnit.setOilVariety(sourceUnit.getOilVariety()); // Preserve oil variety for label generation

            // 4. (Optionnel) Si la cuve source devient vide, effacer son numéro de lot
            double newSourceVolume = sourceUnit.getCurrentVolume() - volumeInitial;
            if (Math.abs(newSourceVolume) < 0.001) {
                sourceUnit.setLotNumber(null);
            }

            // Mise à jour des volumes et coûts
            Double sourceAvgCost = sourceUnit.getAvgCost() != null ? sourceUnit.getAvgCost() : 0.0;

            // 5. Retirer le volume de la source
            sourceUnit.updateCurrentVolume(volumeInitial, 0, sourceAvgCost);

            // 6. Ajouter le volume filtré à la cible
            targetUnit.updateCurrentVolume(volumeAfter, 1, sourceAvgCost);

            // Sauvegarde des unités
            storageUnitRepo.save(sourceUnit);
            storageUnitRepo.save(targetUnit);

            // Create oil transaction for the filtration
            OilTransactionDTO transactionDto = new OilTransactionDTO();
            transactionDto.setTransactionType(TransactionType.FILTRATION);
            transactionDto.setStorageUnitSource(modelMapper.map(sourceUnit, StorageUnitDto.class));
            transactionDto.setStorageUnitDestination(modelMapper.map(targetUnit, StorageUnitDto.class));
            transactionDto.setQuantityKg(volumeAfter);
            transactionDto
                    .setQualityGrade(sourceUnit.getQualityGrade() != null ? sourceUnit.getQualityGrade().name() : null);
            transactionDto.setTransactionState(TransactionState.COMPLETED);

            // Preserve the original reception delivery for downstream label generation and
            // quality propagation
            var originalReception = oilTransactionService.findByStorageUnitId(sourceUnit.getId()).stream()
                    .filter(tx -> tx.getTransactionType() == TransactionType.RECEPTION_IN)
                    .map(OilTransaction::getReception)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .map(reception -> modelMapper.map(reception, UnifiedDeliveryDTO.class));

            originalReception.ifPresent(reception -> {
                transactionDto.setReception(reception);
                if (reception.getCategoryOliveOil() != null && !reception.getCategoryOliveOil().isBlank()) {
                    transactionDto.setQualityGrade(reception.getCategoryOliveOil());
                    try {
                        targetUnit.setQualityGrade(com.xdev.communicator.models.enums.QualityGrades
                                .valueOf(reception.getCategoryOliveOil()));
                    } catch (IllegalArgumentException ignored) {
                        // keep existing target quality if the delivery category does not match enum
                        // values
                    }
                }
            });

            oilTransactionService.save(transactionDto);

            // Mise à jour de l'opération avec les données de completion
            operation.setStatus(FiltrationStatus.COMPLETED);
            operation.setVolumeAfter(volumeAfter);
            operation.setLossVolume(lossVolume);
            operation.setLossPercent(lossPercent);
            operation.setTargetLotNumber(targetLotNumber); // ← Stocker le lot cible dans l'opération

            // Ajouter la note de completion si fournie
            if (completionData.getNote() != null && !completionData.getNote().isEmpty()) {
                String updatedNote = operation.getNote() == null ? "" : operation.getNote() + " | ";
                operation.setNote(updatedNote + "Completion: " + completionData.getNote());
            }

            FiltrationOperation updated = filtrationRepo.save(operation);

            return mapToDto(updated);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la terminaison", e);
        }
    }

    @Transactional
    public FiltrationResultDto updateFiltration(UUID operationId, FiltrationRequestDto req) {
        String traceId = generateOperationId();

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);
            FiltrationStatus currentStatus = operation.getStatus();

            if (currentStatus == FiltrationStatus.IN_PROGRESS) {
                throw new IllegalStateException("Modification impossible: l'opération est en cours");
            }

            if (currentStatus == FiltrationStatus.CANCELLED) {
                throw new IllegalStateException("Modification impossible: l'opération est annulée");
            }

            if (currentStatus == FiltrationStatus.COMPLETED) {
                operation.setNote(req.getNote());
                FiltrationOperation updated = filtrationRepo.save(operation);

                return mapToDto(updated);
            }

            if (currentStatus != FiltrationStatus.CREATED) {
                throw new IllegalStateException("Modification non autorisée pour le statut: " + currentStatus);
            }

            if (req == null) {
                throw new IllegalArgumentException("La requête ne peut pas être nulle");
            }
            if (req.getSource() == null) {
                throw new IllegalArgumentException("L'ID de l'unité source est requis");
            }
            if (req.getTarget() == null) {
                throw new IllegalArgumentException("L'ID de l'unité cible est requis");
            }
            if (req.getVolumeToFilter() == null || req.getVolumeToFilter() <= 0) {
                throw new IllegalArgumentException("Le volume à filtrer doit être positif");
            }
            if (req.getSource().equals(req.getTarget())) {
                throw new IllegalArgumentException("La source et la cible doivent être différentes");
            }

            StorageUnit sourceUnit = findStorageUnitById(req.getSource(), "Source");
            StorageUnit targetUnit = findStorageUnitById(req.getTarget(), "Target");

            validateBusinessRules(sourceUnit, targetUnit, req.getVolumeToFilter());

            operation.setSourceStorageUnit(sourceUnit);
            operation.setTargetStorageUnit(targetUnit);
            operation.setVolumeToFilter(req.getVolumeToFilter());
            operation.setNote(req.getNote());
            operation.setSourceLotNumber(sourceUnit.getLotNumber());

            FiltrationOperation updated = filtrationRepo.save(operation);

            return mapToDto(updated);

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la modification de l'opération", e);
        }
    }

    @Transactional
    public FiltrationResultDto updateFiltrationStatus(UUID operationId, UpdateFiltrationStatusDto statusDto) {

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            FiltrationStatus currentStatus = operation.getStatus();
            FiltrationStatus newStatus = statusDto.getStatus();

            if (currentStatus != FiltrationStatus.CREATED) {
                throw new IllegalStateException(
                        "Changement de statut interdit: seule une opération CREATED peut changer de statut");
            }

            if (newStatus != FiltrationStatus.IN_PROGRESS) {
                throw new IllegalStateException(
                        "Transition non autorisée: seule la transition CREATED -> IN_PROGRESS est permise");
            }

            operation.setStatus(FiltrationStatus.IN_PROGRESS);

            String statusNote = "STATUS -> IN_PROGRESS";
            if (statusDto.getNote() != null && !statusDto.getNote().isBlank()) {
                statusNote = statusNote + " : " + statusDto.getNote().trim();
            }

            String currentNote = operation.getNote();
            operation.setNote(currentNote == null || currentNote.isBlank()
                    ? statusNote
                    : currentNote + " | " + statusNote);

            FiltrationOperation updated = filtrationRepo.save(operation);

            return mapToDto(updated);

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour", e);
        }
    }

    @Transactional
    public FiltrationResultDto addNote(UUID operationId, String note) {

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            if (operation.getStatus() == FiltrationStatus.IN_PROGRESS) {
                throw new IllegalStateException("Modification impossible: l'opération est en cours");
            }

            if (operation.getStatus() != FiltrationStatus.CREATED
                    && operation.getStatus() != FiltrationStatus.COMPLETED) {
                throw new IllegalStateException(
                        "Modification de note non autorisée pour le statut: " + operation.getStatus());
            }

            operation.setNote(note);

            FiltrationOperation updated = filtrationRepo.save(operation);

            return mapToDto(updated);

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la modification de la note", e);
        }
    }

    @Transactional(readOnly = true)
    public FiltrationResultDto getFiltrationById(UUID operationId) { // [MODIFIÉ] Nom de méthode
        String traceId = generateOperationId();

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            return mapToDto(operation);

        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<FiltrationResultDto> getAllFiltrations() {
        String traceId = generateOperationId();
        UUID tenantId = TenantContext.getCurrentTenant();

        try {

            // [MODIFIÉ] Utilisation de la nouvelle méthode avec tri
            List<FiltrationOperation> operations = filtrationRepo.findAllByTenantIdAndIsDeletedFalse(tenantId);

            return operations.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FiltrationResultDto> getFiltrationsByStatus(FiltrationStatus status) {
        String traceId = generateOperationId();

        try {

            // [NOUVEAU] Appel à la nouvelle méthode du repository
            List<FiltrationOperation> operations = filtrationRepo.findByStatusAndIsDeletedFalse(status.toString());

            return operations.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }

    private void validateRequest(FiltrationRequestDto req) {
        if (req == null) {
            throw new IllegalArgumentException("La requête ne peut pas être nulle");
        }
        if (req.getSource() == null) {
            throw new IllegalArgumentException("L'ID de l'unité source est requis");
        }
        if (req.getTarget() == null) {
            throw new IllegalArgumentException("L'ID de l'unité cible est requis");
        }
        if (req.getVolumeToFilter() == null || req.getVolumeToFilter() <= 0) {
            throw new IllegalArgumentException("Le volume à filtrer doit être positif");
        }
        // [NOUVEAU] Vérification que source et cible sont différentes
        if (req.getSource().equals(req.getTarget())) {
            throw new IllegalArgumentException("La source et la cible doivent être différentes");
        }
    }

    // Recherche une unité
    private StorageUnit findStorageUnitById(UUID id, String type) {
        return storageUnitRepo.findById(id).orElseThrow(
                () -> new IllegalArgumentException(String.format("%s non trouvée avec l'ID: %s", type, id)));
    }

    // Valide les règles métier
    private void validateBusinessRules(StorageUnit source, StorageUnit target, double volume) {
        if (volume > source.getCurrentVolume()) {
            throw new IllegalArgumentException(
                    String.format("Volume insuffisant dans la source: disponible=%.2fL, requis=%.2fL",
                            source.getCurrentVolume(), volume));
        }

        double newTargetVolume = target.getCurrentVolume() + volume;
        if (newTargetVolume > target.getMaxCapacity()) {
            throw new IllegalArgumentException(String.format(
                    "Capacité insuffisante dans la cible: disponible=%.2fL, max=%.2fL, nouveau volume=%.2fL",
                    target.getMaxCapacity() - target.getCurrentVolume(), target.getMaxCapacity(), newTargetVolume));
        }
    }

    // Recherche une opération

    private FiltrationOperation findFiltrationOperationById(UUID id) {
        FiltrationOperation op = filtrationRepo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Opération non trouvée avec l'ID: %s", id)));

        return op;
    }

    private String generateOperationId() {
        return "OP-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private FiltrationResultDto mapToDto(FiltrationOperation operation) {
        if (operation == null) {
            return null;
        }

        FiltrationResultDto dto = new FiltrationResultDto();

        dto.setOperationId(operation.getId());

        StorageUnit sourceStorageUnit = operation.getSourceStorageUnit();
        StorageUnit targetStorageUnit = operation.getTargetStorageUnit();

        dto.setSource(mapStorageUnitToDto(sourceStorageUnit));
        dto.setTarget(mapStorageUnitToDto(targetStorageUnit));

        dto.setVolumeFiltered(operation.getVolumeToFilter());
        dto.setVolumeAfter(operation.getVolumeAfter());
        dto.setLossVolume(operation.getLossVolume());
        dto.setLossPercent(operation.getLossPercent());

        dto.setStatus(operation.getStatus() != null ? operation.getStatus().name() : null);
        dto.setTimestamp(operation.getOperationDate());
        dto.setNote(operation.getNote());

        dto.setSourceLotNumber(sourceStorageUnit != null ? sourceStorageUnit.getLotNumber() : null);
        dto.setTargetLotNumber(targetStorageUnit != null ? targetStorageUnit.getLotNumber() : null);

        return dto;
    }

    private StorageUnitDto mapStorageUnitToDto(StorageUnit storageUnit) {
        if (storageUnit == null) {
            return null;
        }

        StorageUnitDto dto = new StorageUnitDto();

        dto.setId(storageUnit.getId());
        dto.setLotNumber(storageUnit.getLotNumber());

        /*
         * Keep only safe scalar/simple fields here.
         * Do not map deep relations with ModelMapper.
         */

        try {
            dto.setName(storageUnit.getName());
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        try {
            dto.setQrHex(storageUnit.getQrHex());
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        try {
            dto.setMaxCapacity(storageUnit.getMaxCapacity());
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        try {
            dto.setCurrentVolume(storageUnit.getCurrentVolume());
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        try {
            dto.setQualityGrade(storageUnit.getQualityGrade());
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        try {
            dto.setOilVariety(modelMapper.map(storageUnit.getOilVariety(), BaseTypeDto.class));
        } catch (Exception ignored) {
            // Field/method not available in some versions.
        }

        return dto;
    }
}
