package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.FiltrationStepDto;
import com.osm.oilproductionservice.dto.GenealogyDto;
import com.osm.oilproductionservice.dto.IntakeStepDto;
import com.osm.oilproductionservice.dto.RootSourceDto;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.model.TraceabilityLot;
import com.osm.oilproductionservice.model.TraceabilitySourceType;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.QualityControlResultRepository;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.communicator.models.enums.DeliveryType;
import com.xdev.communicator.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class GenealogyService {

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationRepo;
    private final DeliveryRepository deliveryRepo;
    private final TraceabilityLotService traceabilityLotService;
    private final QualityControlResultRepository qualityControlResultRepository;
    private final OilTransactionRepository oilTransactionRepository;

    public GenealogyService(StorageUnitRepo storageUnitRepo,
                            FiltrationOperationRepo filtrationRepo,
                            DeliveryRepository deliveryRepo,
                            TraceabilityLotService traceabilityLotService,
                            QualityControlResultRepository qualityControlResultRepository,
                            OilTransactionRepository oilTransactionRepository) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
        this.deliveryRepo = deliveryRepo;
        this.traceabilityLotService = traceabilityLotService;
        this.qualityControlResultRepository = qualityControlResultRepository;
        this.oilTransactionRepository = oilTransactionRepository;
    }

    @Transactional(readOnly = true)
    public GenealogyDto getFullGenealogy(UUID lotOrStorageId) {
        Optional<TraceabilityLot> traceabilityLotOpt = traceabilityLotService.resolveByLotOrStorage(lotOrStorageId);
        if (traceabilityLotOpt.isPresent()) {
            return buildFromTraceabilityLot(traceabilityLotOpt.get());
        }

        StorageUnit unit = storageUnitRepo.findById(lotOrStorageId)
                .orElseThrow(() -> new RuntimeException("Storage unit or traceability lot not found"));

        GenealogyDto dto = new GenealogyDto();
        dto.setStorageUnitId(unit.getId());
        dto.setLotNumber(unit.getLotNumber());
        dto.setStorageUnitName(unit.getName());

        buildLegacyFiltrationChain(unit.getLotNumber(), unit.getId(), dto);
        dto.setIntakeChain(buildIntakeChainForStorageUnit(unit.getId()));
        return dto;
    }

    private GenealogyDto buildFromTraceabilityLot(TraceabilityLot traceabilityLot) {
        GenealogyDto dto = new GenealogyDto();
        dto.setTraceabilityLotId(traceabilityLot.getId());
        dto.setTraceabilitySourceType(traceabilityLot.getSourceType() != null ? traceabilityLot.getSourceType().name() : null);
        dto.setRootReceptionId(traceabilityLot.getRootReceptionId());
        dto.setStorageUnitId(traceabilityLot.getStorageUnitId());
        dto.setLotNumber(traceabilityLot.getLotNumber());

        if (traceabilityLot.getStorageUnitId() != null) {
            storageUnitRepo.findById(traceabilityLot.getStorageUnitId())
                    .ifPresent(storageUnit -> dto.setStorageUnitName(storageUnit.getName()));
        }

        buildTraceabilityChain(traceabilityLot, dto);
        dto.setFilteredQualityControls(resolveQualityControls(traceabilityLot.getId(), traceabilityLot.getFiltrationOperationId()));
        appendRootSource(traceabilityLot, dto);
        dto.setIntakeChain(resolveIntakeChain(traceabilityLot, dto));
        return dto;
    }

    /**
     * Full intake on the final tank; upstream réception/stockage source is on {@link FiltrationStepDto#getSourceIntakeChain()}.
     */
    private List<IntakeStepDto> resolveIntakeChain(TraceabilityLot traceabilityLot, GenealogyDto dto) {
        if (traceabilityLot.getStorageUnitId() == null) {
            return List.of();
        }
        return buildIntakeChainForStorageUnit(traceabilityLot.getStorageUnitId());
    }

    private void buildTraceabilityChain(TraceabilityLot traceabilityLot, GenealogyDto dto) {
        TraceabilityLot current = traceabilityLot;
        while (current != null) {
            if (current.getSourceType() == TraceabilitySourceType.FILTRATION && current.getFiltrationOperationId() != null) {
                TraceabilityLot finalCurrent = current;
                filtrationRepo.findByIdAndIsDeletedFalse(current.getFiltrationOperationId())
                        .ifPresent(operation -> dto.getFiltrations().add(toFiltrationStep(operation, finalCurrent.getId())));
            }

            if (current.getParentLotId() == null) {
                break;
            }

            current = traceabilityLotService.findById(current.getParentLotId()).orElse(null);
        }
    }

    private void appendRootSource(TraceabilityLot traceabilityLot, GenealogyDto dto) {
        UUID rootSourceId = traceabilityLot.getRootReceptionId() != null
                ? traceabilityLot.getRootReceptionId()
                : traceabilityLot.getSourceEntityId();
        if (rootSourceId == null) {
            return;
        }

        deliveryRepo.findById(rootSourceId).ifPresent(delivery -> {
            if (delivery.getDeliveryType() == DeliveryType.OIL
                    && delivery.getLotOliveNumber() != null
                    && !delivery.getLotOliveNumber().isBlank()) {
                deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(
                                delivery.getLotOliveNumber(), DeliveryType.OLIVE)
                        .stream()
                        .findFirst()
                        .ifPresent(olive -> dto.getRootSources().add(toRootSource(olive, null)));
            }
            dto.getRootSources().add(toRootSource(delivery, traceabilityLot.getSourceType()));
        });
    }

    private void buildLegacyFiltrationChain(String lotNumber, UUID storageUnitId, GenealogyDto dto) {
        if (lotNumber == null && storageUnitId == null) {
            return;
        }

        if (lotNumber != null) {
            Optional<FiltrationOperation> opOpt = filtrationRepo.findByTargetLotNumberAndIsDeletedFalse(lotNumber);
            if (opOpt.isPresent()) {
                FiltrationOperation op = opOpt.get();
                dto.getFiltrations().add(toFiltrationStep(op, null));
                UUID sourceUnitId = op.getSourceStorageUnit() != null ? op.getSourceStorageUnit().getId() : null;
                buildLegacyFiltrationChain(op.getSourceLotNumber(), sourceUnitId, dto);
                return;
            }
        }

        findRootSources(lotNumber, storageUnitId, dto);
    }

    private FiltrationStepDto toFiltrationStep(FiltrationOperation op, UUID traceabilityLotId) {
        FiltrationStepDto step = new FiltrationStepDto();
        step.setOperationId(op.getId());
        step.setSourceLotNumber(op.getSourceLotNumber());
        step.setTargetLotNumber(op.getTargetLotNumber());
        step.setVolumeFiltered(op.getVolumeAfter());
        step.setTimestamp(op.getOperationDate() != null ? op.getOperationDate().toString() : null);
        step.setQualityControls(resolveQualityControls(traceabilityLotId, op.getId()));

        if (op.getSourceStorageUnit() != null) {
            step.setSourceStorageUnitId(op.getSourceStorageUnit().getId());
            step.setSourceStorageUnitName(op.getSourceStorageUnit().getName());
            step.setSourceIntakeChain(buildIntakeChainForStorageUnit(op.getSourceStorageUnit().getId()));
        }

        return step;
    }

    private Map<String, String> resolveQualityControls(UUID traceabilityLotId, UUID filtrationOperationId) {
        List<QualityControlResult> results = traceabilityLotId != null
                ? qualityControlResultRepository.findByTraceabilityLotIdAndIsDeletedFalse(traceabilityLotId)
                : List.of();

        if (results.isEmpty() && filtrationOperationId != null) {
            results = qualityControlResultRepository.findByFiltrationOperationIdAndIsDeletedFalse(filtrationOperationId);
        }

        if (results.isEmpty()) {
            return Map.of();
        }

        Map<String, String> qcs = new HashMap<>();
        for (QualityControlResult result : results) {
            if (result.getRule() != null) {
                String key = result.getRule().getRuleName() != null
                        ? result.getRule().getRuleName()
                        : result.getRule().getRuleKey();
                if (key != null) {
                    qcs.put(key, result.getMeasuredValue());
                }
            }
        }
        return qcs;
    }

    private void findRootSources(String lotNumber, UUID storageUnitId, GenealogyDto dto) {
        List<UnifiedDelivery> deliveries = new java.util.ArrayList<>();
        
        if (storageUnitId != null) {
            deliveries.addAll(deliveryRepo.findAllByStorageUnitIdAndDeliveryTypeAndIsDeletedFalse(storageUnitId, DeliveryType.OIL));
        }
        
        if (deliveries.isEmpty() && lotNumber != null) {
            deliveries.addAll(deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, DeliveryType.OIL));
        }

        for (UnifiedDelivery delivery : deliveries) {
            dto.getRootSources().add(toRootSource(delivery, null));
        }
    }

    /**
     * Rebuilds olive reception → oil reception → RECEPTION_IN transaction for a storage tank.
     */
    private List<IntakeStepDto> buildIntakeChainForStorageUnit(UUID storageUnitId) {
        if (storageUnitId == null) {
            return List.of();
        }

        Optional<OilTransaction> receptionTxOpt = oilTransactionRepository
                .findFirstByStorageUnitDestinationIdAndTransactionTypeOrderByCreatedDateAsc(
                        storageUnitId, TransactionType.RECEPTION_IN);
        if (receptionTxOpt.isEmpty()) {
            return List.of();
        }

        OilTransaction receptionTx = receptionTxOpt.get();
        StorageUnit storageUnit = storageUnitRepo.findById(storageUnitId).orElse(null);

        UnifiedDelivery oilDelivery = resolveOilDelivery(receptionTx);
        List<IntakeStepDto> chain = new ArrayList<>();

        if (oilDelivery != null && oilDelivery.getLotOliveNumber() != null && !oilDelivery.getLotOliveNumber().isBlank()) {
            deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(
                            oilDelivery.getLotOliveNumber(), DeliveryType.OLIVE)
                    .stream()
                    .findFirst()
                    .ifPresent(olive -> chain.add(toIntakeStepFromDelivery(olive, "OLIVE_RECEPTION")));
        }

        if (oilDelivery != null) {
            chain.add(toIntakeStepFromDelivery(oilDelivery, "OIL_RECEPTION"));
        }

        chain.add(toIntakeStepFromTransaction(receptionTx, storageUnit, oilDelivery));
        return chain;
    }

    private UnifiedDelivery resolveOilDelivery(OilTransaction receptionTx) {
        if (receptionTx.getReception() == null) {
            return null;
        }
        UUID receptionId = receptionTx.getReception().getId();
        if (receptionId == null) {
            return receptionTx.getReception();
        }
        return deliveryRepo.findById(receptionId).orElse(receptionTx.getReception());
    }

    private IntakeStepDto toIntakeStepFromDelivery(UnifiedDelivery delivery, String type) {
        IntakeStepDto step = new IntakeStepDto();
        step.setType(type);
        step.setDeliveryId(delivery.getId());
        step.setDeliveryNumber(delivery.getDeliveryNumber());
        step.setLotNumber(delivery.getLotNumber());
        step.setLotOliveNumber(delivery.getLotOliveNumber());
        step.setDeliveryType(delivery.getDeliveryType() != null ? delivery.getDeliveryType().name() : null);
        step.setSupplierName(resolveSupplierName(delivery));
        step.setDeliveryDate(delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().toString() : null);
        step.setQuantityKg(resolveDeliveryQuantity(delivery));

        Map<String, Object> extra = new LinkedHashMap<>();
        if (delivery.getRendement() != null) {
            extra.put("rendement", delivery.getRendement());
        }
        if (delivery.getOilVariety() != null) {
            extra.put("variety", delivery.getOilVariety().getName());
        } else if (delivery.getOliveVariety() != null) {
            extra.put("variety", delivery.getOliveVariety().getName());
        }
        if (delivery.getCategoryOliveOil() != null) {
            extra.put("qualityGrade", delivery.getCategoryOliveOil());
        }
        if (delivery.getGlobalLotNumber() != null) {
            extra.put("globalLotNumber", delivery.getGlobalLotNumber());
        }
        if (delivery.getOperationType() != null) {
            extra.put("operationType", delivery.getOperationType().name());
        }
        step.setExtra(extra);

        if (delivery.getQualityControlResults() != null && !delivery.getQualityControlResults().isEmpty()) {
            Map<String, String> qcs = new HashMap<>();
            for (QualityControlResult result : delivery.getQualityControlResults()) {
                if (result.getRule() != null && result.getRule().getRuleName() != null) {
                    qcs.put(result.getRule().getRuleName(), result.getMeasuredValue());
                }
            }
            step.setQualityControls(qcs);
        }
        return step;
    }

    private IntakeStepDto toIntakeStepFromTransaction(OilTransaction tx, StorageUnit storageUnit, UnifiedDelivery oilDelivery) {
        IntakeStepDto step = new IntakeStepDto();
        step.setType("STORAGE_INTAKE");
        step.setTransactionId(tx.getId());
        step.setQuantityKg(tx.getQuantityKg());
        step.setDeliveryDate(tx.getCreatedDate() != null ? tx.getCreatedDate().toString() : null);
        if (storageUnit != null) {
            step.setStorageUnitId(storageUnit.getId());
            step.setStorageUnitName(storageUnit.getName());
        }
        if (oilDelivery != null) {
            step.setDeliveryId(oilDelivery.getId());
            step.setLotNumber(oilDelivery.getLotNumber());
            step.setDeliveryNumber(oilDelivery.getDeliveryNumber());
        }
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("transactionType", TransactionType.RECEPTION_IN.name());
        if (tx.getQualityGrade() != null) {
            extra.put("qualityGrade", tx.getQualityGrade());
        }
        step.setExtra(extra);
        return step;
    }

    private String resolveSupplierName(UnifiedDelivery delivery) {
        Supplier supplier = delivery.getSupplier();
        if (supplier == null) {
            return null;
        }
        return supplier.getName();
    }

    private Double resolveDeliveryQuantity(UnifiedDelivery delivery) {
        if (delivery.getDeliveryType() == DeliveryType.OLIVE) {
            return delivery.getOliveQuantity() != null ? delivery.getOliveQuantity() : delivery.getPoidsNet();
        }
        return delivery.getOilQuantity() != null ? delivery.getOilQuantity() : delivery.getPoidsNet();
    }

    private RootSourceDto toRootSource(UnifiedDelivery delivery, TraceabilitySourceType sourceType) {
        RootSourceDto root = new RootSourceDto();

        if (delivery.getDeliveryType() == DeliveryType.OLIVE) {
            root.setType("OLIVE_RECEPTION");
            root.setSourceId(delivery.getId());
            root.setSupplierName(resolveSupplierName(delivery));
        } else if (sourceType == TraceabilitySourceType.TRITURATION || delivery.getMillMachine() != null) {
            root.setType("TRITURATION");
            root.setSourceId(delivery.getId());
            root.setSupplierName(delivery.getMillMachine() != null
                    ? "Production Interne (Moulin: " + delivery.getMillMachine().getName() + ")"
                    : "Production Interne");
        } else {
            root.setType("RECEPTION");
            root.setSourceId(delivery.getId());
            root.setSupplierName(delivery.getSupplierType() != null ? delivery.getSupplierType().getName() : "Inconnu");
        }

        root.setLotNumber(delivery.getLotNumber());
        root.setDate(delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().toString() : "");

        Map<String, Object> extra = new HashMap<>();
        if (delivery.getRendement() != null) {
            extra.put("rendement", delivery.getRendement());
        }
        if (delivery.getOilVariety() != null) {
            extra.put("variety", delivery.getOilVariety().getName());
        }
        if (delivery.getStorageUnit() != null) {
            extra.put("storageUnitId", delivery.getStorageUnit().getId());
            extra.put("storageUnitName", delivery.getStorageUnit().getName());
        }
        if (delivery.getLotOliveNumber() != null) {
            extra.put("lotOliveNumber", delivery.getLotOliveNumber());
        }
        if (delivery.getDeliveryNumber() != null) {
            extra.put("deliveryNumber", delivery.getDeliveryNumber());
        }
        if (delivery.getCategoryOliveOil() != null) {
            extra.put("qualityGrade", delivery.getCategoryOliveOil());
        }
        root.setExtra(extra);

        if (delivery.getQualityControlResults() != null && !delivery.getQualityControlResults().isEmpty()) {
            Map<String, String> qcs = new HashMap<>();
            for (QualityControlResult res : delivery.getQualityControlResults()) {
                if (res.getRule() != null) {
                    qcs.put(res.getRule().getRuleName(), res.getMeasuredValue());
                }
            }
            root.setQualityControls(qcs);
        }

        return root;
    }
}
