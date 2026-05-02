package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.dto.FiltrationStatus;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FiltrationService {

    private static final Logger logger = LoggerFactory.getLogger(FiltrationService.class);

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationRepo;
    private final org.modelmapper.ModelMapper modelMapper;

    public FiltrationService(StorageUnitRepo storageUnitRepo, FiltrationOperationRepo filtrationRepo, org.modelmapper.ModelMapper modelMapper) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
        this.modelMapper = modelMapper;
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
        String operationId = generateOperationId();

        try {
            logger.info("Opération {} - Début de création", operationId);

            validateRequest(req);

            StorageUnit sourceUnit = findStorageUnitById(req.getSource(), "Source");
            StorageUnit targetUnit = findStorageUnitById(req.getTarget(), "Target");

            logger.info("Opération {} - Unités chargées: Source vol={}, Target vol={}/{}", operationId, sourceUnit.getCurrentVolume(), targetUnit.getCurrentVolume(), targetUnit.getMaxCapacity());

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
                throw new IllegalArgumentException(String.format("Impossible de démarrer: statut actuel = %s, attendu = CREATED", operation.getStatus()));
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
    public FiltrationResultDto completeFiltration(UUID operationId) {

        try {

            // Récupération de l'opération
            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Vérification du statut
            if (operation.getStatus() != FiltrationStatus.IN_PROGRESS) {
                throw new IllegalArgumentException(String.format("Impossible de terminer: statut actuel = %s, attendu = IN_PROGRESS", operation.getStatus()));
            }

            // [NOUVEAU] Validation du volume après filtration
            if (operation.getVolumeAfter() == null || operation.getVolumeAfter() < 0) {
                throw new IllegalArgumentException("Le volume après filtration doit être positif");
            }
            if (operation.getVolumeAfter() > operation.getVolumeToFilter()) {
                throw new IllegalArgumentException("Le volume après filtration ne peut pas dépasser le volume initial");
            }

            // Calcul des pertes
            double volumeInitial = operation.getVolumeToFilter();
            double volumeAfter = operation.getVolumeAfter();
            double lossVolume = volumeInitial - volumeAfter;
            double lossPercent = (lossVolume / volumeInitial) * 100;

            // Récupération des unités de stockage
            StorageUnit sourceUnit = operation.getSourceStorageUnit();
            StorageUnit targetUnit = operation.getTargetStorageUnit();


            // 1. Récupérer le lot source stocké lors de la création
            String sourceLotNumber = operation.getSourceLotNumber();

            // 2. Générer un nouveau lot pour la cible
            String targetLotNumber;
            if (sourceLotNumber != null && !sourceLotNumber.isBlank()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                targetLotNumber = "FILT-" + sourceLotNumber + "-" + timestamp;
            } else {
                // Fallback si la source n'a pas de lot (cas improbable, mais sécurité)
                targetLotNumber = "FILT-" + UUID.randomUUID().toString().substring(0, 8);
            }

            // 3. Mettre à jour la cuve cible avec le nouveau lot
            targetUnit.setLotNumber(targetLotNumber);
            targetUnit.setFilteredOil(true);
            targetUnit.setLastFiltrationDate(LocalDateTime.now());

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

            // Mise à jour de l'opération avec les données de completion
            operation.setStatus(FiltrationStatus.COMPLETED);
            operation.setVolumeAfter(volumeAfter);
            operation.setLossVolume(lossVolume);
            operation.setLossPercent(lossPercent);
            operation.setTargetLotNumber(targetLotNumber);   // ← Stocker le lot cible dans l'opération

            // Ajouter la note de completion si fournie
            if (operation.getNote() != null && !operation.getNote().isEmpty()) {
                String updatedNote = operation.getNote() == null ? "" : operation.getNote() + " | ";
                operation.setNote(updatedNote + "Completion: " + operation.getNote());
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
                        "Changement de statut interdit: seule une opération CREATED peut changer de statut"
                );
            }

            if (newStatus != FiltrationStatus.IN_PROGRESS) {
                throw new IllegalStateException(
                        "Transition non autorisée: seule la transition CREATED -> IN_PROGRESS est permise"
                );
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
                throw new IllegalStateException("Modification de note non autorisée pour le statut: " + operation.getStatus());
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

    public FiltrationResultDto getFiltrationById(UUID operationId) { // [MODIFIÉ] Nom de méthode
        String traceId = generateOperationId();

        try {

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            return mapToDto(operation);

        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
    public List<FiltrationResultDto> getAllFiltrations() {
        String traceId = generateOperationId();

        try {

            // [MODIFIÉ] Utilisation de la nouvelle méthode avec tri
            List<FiltrationOperation> operations = filtrationRepo.findAllByIsDeletedFalseOrderByOperationDateDesc();

            return operations.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }
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
        return storageUnitRepo.findById(id).orElseThrow(() ->
                new IllegalArgumentException(String.format("%s non trouvée avec l'ID: %s", type, id)));
    }

     //Valide les règles métier
    private void validateBusinessRules(StorageUnit source, StorageUnit target, double volume) {
        if (volume > source.getCurrentVolume()) {
            throw new IllegalArgumentException(String.format("Volume insuffisant dans la source: disponible=%.2fL, requis=%.2fL", source.getCurrentVolume(), volume));
        }

        double newTargetVolume = target.getCurrentVolume() + volume;
        if (newTargetVolume > target.getMaxCapacity()) {
            throw new IllegalArgumentException(String.format("Capacité insuffisante dans la cible: disponible=%.2fL, max=%.2fL, nouveau volume=%.2fL", target.getMaxCapacity() - target.getCurrentVolume(), target.getMaxCapacity(), newTargetVolume));
        }
    }

    // Recherche une opération

    private FiltrationOperation findFiltrationOperationById(UUID id) {
        FiltrationOperation op = filtrationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Opération non trouvée avec l'ID: %s", id)));
        if (Boolean.TRUE.equals(op.getDeleted())) {                         // ← added guard
            throw new IllegalArgumentException(
                    String.format("Opération supprimée, ID: %s", id));
        }
        return op;
    }
    //Convertit une entité en DTO
    private FiltrationResultDto mapToDto(FiltrationOperation operation) {
        FiltrationResultDto dto = new FiltrationResultDto();
        dto.setOperationId(operation.getId());
        dto.setSource(modelMapper.map(operation.getSourceStorageUnit(), StorageUnitDto.class));
        dto.setTarget(modelMapper.map(operation.getTargetStorageUnit(), StorageUnitDto.class));
        dto.setVolumeFiltered(operation.getVolumeToFilter());
        dto.setVolumeAfter(operation.getVolumeAfter());
        dto.setLossVolume(operation.getLossVolume());
        dto.setLossPercent(operation.getLossPercent());
        dto.setStatus(operation.getStatus() != null ? operation.getStatus().toString() : null);
        dto.setTimestamp(operation.getOperationDate());
        dto.setNote(operation.getNote());

        // Ajout des numéros de lot pour la traçabilité
        dto.setSourceLotNumber(operation.getSourceLotNumber());
        dto.setTargetLotNumber(operation.getTargetLotNumber());

        return dto;
    }

    private String generateOperationId() {
        return "OP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
}