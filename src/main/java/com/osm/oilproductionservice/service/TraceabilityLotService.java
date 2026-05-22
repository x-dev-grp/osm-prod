package com.osm.oilproductionservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.TraceabilityLot;
import com.osm.oilproductionservice.model.TraceabilitySourceType;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.TraceabilityLotRepository;
import com.xdev.communicator.models.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TraceabilityLotService {

    private final TraceabilityLotRepository traceabilityLotRepository;
    private final OilTransactionService oilTransactionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public TraceabilityLot ensureRootLotForStorageUnit(StorageUnit storageUnit) {
        return traceabilityLotRepository
                .findFirstByStorageUnitIdAndActiveTrueAndIsDeletedFalseOrderByCapturedAtDesc(storageUnit.getId())
                .orElseGet(() -> createRootLot(storageUnit));
    }

    @Transactional
    public TraceabilityLot createFilteredLot(StorageUnit sourceUnit, StorageUnit targetUnit, FiltrationOperation operation,
            Double quantity) {
        TraceabilityLot parentLot = ensureRootLotForStorageUnit(sourceUnit);

        TraceabilityLot filteredLot = new TraceabilityLot();
        filteredLot.setLotNumber(operation.getTargetLotNumber());
        filteredLot.setSourceType(TraceabilitySourceType.FILTRATION);
        filteredLot.setSourceEntityId(operation.getId());
        filteredLot.setRootReceptionId(parentLot.getRootReceptionId());
        filteredLot.setParentLotId(parentLot.getId());
        filteredLot.setStorageUnitId(targetUnit.getId());
        filteredLot.setFiltrationOperationId(operation.getId());
        filteredLot.setQualityGrade(targetUnit.getQualityGrade() != null ? targetUnit.getQualityGrade().name() : null);
        filteredLot.setOilVariety(targetUnit.getOilVariety() != null ? targetUnit.getOilVariety().getName() : null);
        filteredLot.setQuantity(quantity);
        filteredLot.setCapturedAt(LocalDateTime.now());
        filteredLot.setActive(true);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("operationId", operation.getId());
        snapshot.put("sourceLotNumber", operation.getSourceLotNumber());
        snapshot.put("targetLotNumber", operation.getTargetLotNumber());
        snapshot.put("sourceStorageUnitId", sourceUnit.getId());
        snapshot.put("targetStorageUnitId", targetUnit.getId());
        snapshot.put("quantity", quantity);
        filteredLot.setSourceSnapshotJson(toJson(snapshot));

        deactivateActiveLotsForStorage(targetUnit.getId());
        return traceabilityLotRepository.save(filteredLot);
    }

    @Transactional(readOnly = true)
    public Optional<TraceabilityLot> resolveByLotOrStorage(UUID lotOrStorageId) {
        Optional<TraceabilityLot> directLot = traceabilityLotRepository.findByIdAndIsDeletedFalse(lotOrStorageId);
        if (directLot.isPresent()) {
            return directLot;
        }

        return traceabilityLotRepository.findFirstByStorageUnitIdAndActiveTrueAndIsDeletedFalseOrderByCapturedAtDesc(lotOrStorageId);
    }

    @Transactional(readOnly = true)
    public Optional<TraceabilityLot> findById(UUID lotId) {
        return traceabilityLotRepository.findByIdAndIsDeletedFalse(lotId);
    }

    private TraceabilityLot createRootLot(StorageUnit storageUnit) {
        UnifiedDelivery reception = oilTransactionService.findByStorageUnitId(storageUnit.getId()).stream()
                .filter(tx -> tx.getTransactionType() == TransactionType.RECEPTION_IN)
                .map(OilTransaction::getReception)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);

        TraceabilityLot rootLot = new TraceabilityLot();
        rootLot.setLotNumber(resolveLotNumber(storageUnit));
        rootLot.setSourceType(resolveRootSourceType(reception));
        rootLot.setSourceEntityId(reception != null ? reception.getId() : storageUnit.getId());
        rootLot.setRootReceptionId(reception != null ? reception.getId() : null);
        rootLot.setParentLotId(null);
        rootLot.setStorageUnitId(storageUnit.getId());
        rootLot.setFiltrationOperationId(null);
        rootLot.setQualityGrade(storageUnit.getQualityGrade() != null ? storageUnit.getQualityGrade().name() : null);
        rootLot.setOilVariety(storageUnit.getOilVariety() != null ? storageUnit.getOilVariety().getName() : null);
        rootLot.setQuantity(storageUnit.getCurrentVolume());
        rootLot.setCapturedAt(LocalDateTime.now());
        rootLot.setActive(true);
        rootLot.setSourceSnapshotJson(buildRootSnapshot(storageUnit, reception));

        deactivateActiveLotsForStorage(storageUnit.getId());
        return traceabilityLotRepository.save(rootLot);
    }

    private void deactivateActiveLotsForStorage(UUID storageUnitId) {
        traceabilityLotRepository.findFirstByStorageUnitIdAndActiveTrueAndIsDeletedFalseOrderByCapturedAtDesc(storageUnitId)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    traceabilityLotRepository.save(existing);
                });
    }

    private TraceabilitySourceType resolveRootSourceType(UnifiedDelivery reception) {
        if (reception != null && reception.getMillMachine() != null) {
            return TraceabilitySourceType.TRITURATION;
        }
        return TraceabilitySourceType.RECEPTION;
    }

    private String buildRootSnapshot(StorageUnit storageUnit, UnifiedDelivery reception) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("storageUnitId", storageUnit.getId());
        snapshot.put("storageUnitName", storageUnit.getName());
        snapshot.put("lotNumber", resolveLotNumber(storageUnit));
        snapshot.put("qualityGrade", storageUnit.getQualityGrade() != null ? storageUnit.getQualityGrade().name() : null);
        snapshot.put("oilVariety", storageUnit.getOilVariety() != null ? storageUnit.getOilVariety().getName() : null);
        snapshot.put("currentVolume", storageUnit.getCurrentVolume());
        if (reception != null) {
            snapshot.put("receptionId", reception.getId());
            snapshot.put("deliveryNumber", reception.getDeliveryNumber());
            snapshot.put("deliveryLotNumber", reception.getLotNumber());
            snapshot.put("deliveryDate", reception.getDeliveryDate());
        }
        return toJson(snapshot);
    }

    private String resolveLotNumber(StorageUnit storageUnit) {
        if (storageUnit.getLotNumber() != null && !storageUnit.getLotNumber().isBlank()) {
            return storageUnit.getLotNumber();
        }
        return "STORAGE-" + storageUnit.getId();
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de serialiser la traceabilite du lot", e);
        }
    }
}
