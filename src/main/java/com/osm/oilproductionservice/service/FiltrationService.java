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

    /**
     * [DÉJÀ EXISTANT] Créer une nouvelle opération
     */
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

            FiltrationOperation operation = new FiltrationOperation();
            operation.setSourceStorageUnit(sourceUnit);
            operation.setTargetStorageUnit(targetUnit);
            operation.setVolumeToFilter(req.getVolumeToFilter());
            operation.setStatus(FiltrationStatus.CREATED);
            operation.setNote(req.getNote());
            operation.setOperationDate(LocalDateTime.now());

            FiltrationOperation saved = filtrationRepo.save(operation);
            logger.info("Opération {} - Sauvegardée avec ID: {}", operationId, saved.getId());

            return mapToDto(saved);

        } catch (IllegalArgumentException e) {
            logger.error("Opération {} - Erreur validation: {}", operationId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Opération {} - Erreur technique: {}", operationId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création de l'opération", e);
        }
    }

    /**
     * [DÉJÀ EXISTANT] Démarrer une opération
     */
    @Transactional
    public FiltrationResultDto startFiltration(UUID operationId) {
        String operationId_log = generateOperationId();
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Démarrage opération ID: {}", traceId, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            if (operation.getStatus() != FiltrationStatus.CREATED) {
                throw new IllegalArgumentException(String.format("Impossible de démarrer: statut actuel = %s, attendu = CREATED", operation.getStatus()));
            }

            operation.setStatus(FiltrationStatus.IN_PROGRESS);

            FiltrationOperation updated = filtrationRepo.save(operation);
            logger.info("Trace {} - Opération {} démarrée", traceId, operationId);

            return mapToDto(updated);

        } catch (IllegalArgumentException e) {
            logger.error("Trace {} - Erreur validation: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Trace {} - Erreur technique: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors du démarrage", e);
        }
    }

    /**
     * [MODIFIÉ] Terminer une opération et mettre à jour les unités de stockage
     * <p>
     * MODIFICATIONS IMPORTANTES:
     * 1. Ajout du paramètre FiltrationCompletionDto pour recevoir le volume après filtration
     * 2. Calcul automatique des pertes (volumeInitial - volumeAfter)
     * 3. Mise à jour des volumes des unités de stockage
     * 4. Marquage de l'huile comme filtrée dans la cuve cible
     * 5. Enregistrement de la date de dernière filtration
     */
    @Transactional
    public FiltrationResultDto completeFiltration(UUID operationId,FiltrationCompletionDto completionData) {
        String operationId_log = generateOperationId();
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Terminaison opération ID: {}", traceId, operationId);

            // Récupération de l'opération
            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // Vérification du statut
            if (operation.getStatus() != FiltrationStatus.IN_PROGRESS) {
                throw new IllegalArgumentException(String.format("Impossible de terminer: statut actuel = %s, attendu = IN_PROGRESS", operation.getStatus()));
            }

            // [NOUVEAU] Validation du volume après filtration
            if (completionData.getVolumeAfter() == null || completionData.getVolumeAfter() < 0) {
                throw new IllegalArgumentException("Le volume après filtration doit être positif");
            }
            if (completionData.getVolumeAfter() > operation.getVolumeToFilter()) {
                throw new IllegalArgumentException("Le volume après filtration ne peut pas dépasser le volume initial");
            }

            // [NOUVEAU] Calcul des pertes
            double volumeInitial = operation.getVolumeToFilter();
            double volumeAfter = completionData.getVolumeAfter();
            double lossVolume = volumeInitial - volumeAfter;
            double lossPercent = (lossVolume / volumeInitial) * 100;

            // [NOUVEAU] Récupération des unités de stockage
            StorageUnit sourceUnit = operation.getSourceStorageUnit();
            StorageUnit targetUnit = operation.getTargetStorageUnit();

            // [NOUVEAU] MISE À JOUR DES UNITÉS DE STOCKAGE
            Double sourceAvgCost = sourceUnit.getAvgCost() != null ? sourceUnit.getAvgCost() : 0.0;

            // 1. Retirer le volume de la source
            sourceUnit.updateCurrentVolume(volumeInitial, 0, null);

            // 2. Ajouter le volume filtré à la cible
            targetUnit.updateCurrentVolume(volumeAfter, 1, sourceAvgCost);

            // 3. Marquer l'huile comme filtrée dans la cuve cible
            targetUnit.setFilteredOil(true);                 // [NOUVEAU] Champ utilisé
            targetUnit.setLastFiltrationDate(LocalDateTime.now()); // [NOUVEAU] Champ utilisé

            // 4. Sauvegarder les unités mises à jour
            storageUnitRepo.save(sourceUnit);
            storageUnitRepo.save(targetUnit);

            // [NOUVEAU] Mise à jour de l'opération avec les données de completion
            operation.setStatus(FiltrationStatus.COMPLETED);
            operation.setVolumeAfter(volumeAfter);
            operation.setLossVolume(lossVolume);
            operation.setLossPercent(lossPercent);

            // Ajouter la note de completion si fournie
            if (completionData.getNote() != null && !completionData.getNote().isEmpty()) {
                String updatedNote = operation.getNote() == null ? "" : operation.getNote() + " | ";
                operation.setNote(updatedNote + "Completion: " + completionData.getNote());
            }

            FiltrationOperation updated = filtrationRepo.save(operation);

            // [NOUVEAU] Logs détaillés des modifications
            logger.info("Trace {} - Opération terminée: perte={}L ({}%)", traceId, String.format("%.3f", lossVolume), String.format("%.2f", lossPercent));
            logger.info("Trace {} - Unités mises à jour: Source {} -> {}L, Target {} -> {}L", traceId, sourceUnit.getId(), sourceUnit.getCurrentVolume(), targetUnit.getId(), targetUnit.getCurrentVolume());

            return mapToDto(updated);

        } catch (IllegalArgumentException e) {
            logger.error("Trace {} - Erreur validation: {}", traceId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Trace {} - Erreur technique: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la terminaison", e);
        }
    }

    /**
     * [DÉJÀ EXISTANT] Mettre à jour le statut
     */
    @Transactional
    public FiltrationResultDto updateFiltrationStatus(UUID operationId, UpdateFiltrationStatusDto statusDto) {
        String operationId_log = generateOperationId();
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Mise à jour statut opération {} vers {}",
                    traceId, operationId, statusDto.getStatus());

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            FiltrationStatus currentStatus = operation.getStatus();
            FiltrationStatus newStatus = statusDto.getStatus();

            // Cancellation allowed only if IN_PROGRESS
            if (FiltrationStatus.CANCELLED.equals(newStatus)
                    && !FiltrationStatus.IN_PROGRESS.equals(currentStatus)) {

                logger.error("Trace {} - Annulation refusée. Statut actuel: {}",
                        traceId, currentStatus);

                throw new IllegalStateException(
                        "L’opération ne peut être annulée que si elle est en cours (IN_PROGRESS)"
                );
            }

            operation.setStatus(newStatus);

            // Note: always append a status trace + optional user note
            String statusNote = "STATUS -> " + newStatus.name();
            if (statusDto.getNote() != null && !statusDto.getNote().isBlank()) {
                statusNote = statusNote + " : " + statusDto.getNote().trim();
            }

            String currentNote = operation.getNote();
            operation.setNote(currentNote == null || currentNote.isBlank()
                    ? statusNote
                    : currentNote + " | " + statusNote);

            FiltrationOperation updated = filtrationRepo.save(operation);

            logger.info("Trace {} - Statut mis à jour vers {}", traceId, newStatus);

            return mapToDto(updated);

        } catch (IllegalArgumentException e) {
            logger.error("Trace {} - Erreur validation: {}", traceId, e.getMessage());
            throw e;

        } catch (Exception e) {
            logger.error("Trace {} - Erreur technique: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour", e);
        }
    }

    /**
     * [NOUVEAU] Ajouter une note à une opération
     * <p>
     * Cette méthode permet d'ajouter une note sans modifier le statut
     * Les notes sont concaténées avec un séparateur " | " pour garder l'historique
     */
    @Transactional
    public FiltrationResultDto addNote(UUID operationId, String note) {
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Ajout de note à l'opération {}", traceId, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            // [NOUVEAU] Ajout de la note (concaténation avec l'existante)
            String currentNote = operation.getNote();
            String newNote = note;
            operation.setNote(currentNote == null ? newNote : currentNote + " | " + newNote);

            FiltrationOperation updated = filtrationRepo.save(operation);

            logger.info("Trace {} - Note ajoutée avec succès", traceId);

            return mapToDto(updated);

        } catch (Exception e) {
            logger.error("Trace {} - Erreur: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'ajout de la note", e);
        }
    }

    /**
     * [MODIFIÉ] Récupérer une opération par son ID (renommée pour plus de clarté)
     * Ancien nom: getFiltrationStatus
     * Nouveau nom: getFiltrationById
     */
    public FiltrationResultDto getFiltrationById(UUID operationId) { // [MODIFIÉ] Nom de méthode
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Consultation opération ID: {}", traceId, operationId);

            FiltrationOperation operation = findFiltrationOperationById(operationId);

            return mapToDto(operation);

        } catch (IllegalArgumentException e) {
            logger.error("Trace {} - Opération non trouvée: {}", traceId, e.getMessage());
            throw e;
        }
    }

    /**
     * [MODIFIÉ] Récupérer toutes les opérations avec tri par date
     * Modification: Ajout du tri par date décroissante
     */
    public List<FiltrationResultDto> getAllFiltrations() {
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Récupération de toutes les opérations", traceId);

            // [MODIFIÉ] Utilisation de la nouvelle méthode avec tri
            List<FiltrationOperation> operations = filtrationRepo.findAllByOrderByOperationDateDesc();

            return operations.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Trace {} - Erreur: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }

    /**
     * [NOUVEAU] Récupérer les opérations par statut
     * <p>
     * Utile pour filtrer l'affichage dans le frontend
     * Exemple: voir seulement les opérations en cours
     */
    public List<FiltrationResultDto> getFiltrationsByStatus(FiltrationStatus status) {
        String traceId = generateOperationId();

        try {
            logger.info("Trace {} - Récupération des opérations avec statut: {}", traceId, status);

            // [NOUVEAU] Appel à la nouvelle méthode du repository
            List<FiltrationOperation> operations = filtrationRepo.findByStatus(status);

            return operations.stream().map(this::mapToDto).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Trace {} - Erreur: {}", traceId, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la récupération", e);
        }
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * [DÉJÀ EXISTANT] Valide la requête
     */
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

    /**
     * [DÉJÀ EXISTANT] Recherche une unité
     */
    private StorageUnit findStorageUnitById(UUID id, String type) {
        return storageUnitRepo.findById(id).orElseThrow(() -> new IllegalArgumentException(String.format("%s non trouvée avec l'ID: %s", type, id)));
    }

    /**
     * [DÉJÀ EXISTANT] Valide les règles métier
     */
    private void validateBusinessRules(StorageUnit source, StorageUnit target, double volume) {
        if (volume > source.getCurrentVolume()) {
            throw new IllegalArgumentException(String.format("Volume insuffisant dans la source: disponible=%.2fL, requis=%.2fL", source.getCurrentVolume(), volume));
        }

        double newTargetVolume = target.getCurrentVolume() + volume;
        if (newTargetVolume > target.getMaxCapacity()) {
            throw new IllegalArgumentException(String.format("Capacité insuffisante dans la cible: disponible=%.2fL, max=%.2fL, nouveau volume=%.2fL", target.getMaxCapacity() - target.getCurrentVolume(), target.getMaxCapacity(), newTargetVolume));
        }
    }

    /**
     * [DÉJÀ EXISTANT] Recherche une opération
     */
    private FiltrationOperation findFiltrationOperationById(UUID id) {
        return filtrationRepo.findById(id).orElseThrow(() -> new IllegalArgumentException(String.format("Opération non trouvée avec l'ID: %s", id)));
    }



    /**
     * [MODIFIÉ] Convertit une entité en DTO avec les nouveaux champs
     * Ajout des champs: volumeAfter, lossVolume, lossPercent
     */
    private FiltrationResultDto mapToDto(FiltrationOperation operation) {
        FiltrationResultDto dto = new FiltrationResultDto();
        dto.setOperationId(operation.getId());
        dto.setSource(modelMapper.map(operation.getSourceStorageUnit(), StorageUnitDto.class));
        dto.setTarget(modelMapper.map(operation.getTargetStorageUnit(), StorageUnitDto.class));
        dto.setVolumeFiltered(operation.getVolumeToFilter());
        // [NOUVEAU] Nouveaux champs ajoutés au DTO
        dto.setVolumeAfter(operation.getVolumeAfter());
        dto.setLossVolume(operation.getLossVolume());
        dto.setLossPercent(operation.getLossPercent());
        dto.setStatus(operation.getStatus() != null ? operation.getStatus().toString() : null);
        dto.setTimestamp(operation.getOperationDate());
        dto.setNote(operation.getNote());
        return dto;
    }

    /**
     * [DÉJÀ EXISTANT] Génère un ID de trace
     */
    private String generateOperationId() {
        return "OP-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
    }
}