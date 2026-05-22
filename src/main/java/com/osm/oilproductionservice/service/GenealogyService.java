package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.FiltrationStepDto;
import com.osm.oilproductionservice.dto.GenealogyDto;
import com.osm.oilproductionservice.dto.RootSourceDto;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.TraceabilityLot;
import com.osm.oilproductionservice.model.TraceabilitySourceType;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.communicator.models.enums.DeliveryType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    public GenealogyService(StorageUnitRepo storageUnitRepo,
                            FiltrationOperationRepo filtrationRepo,
                            DeliveryRepository deliveryRepo,
                            TraceabilityLotService traceabilityLotService) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
        this.deliveryRepo = deliveryRepo;
        this.traceabilityLotService = traceabilityLotService;
    }

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

        buildLegacyFiltrationChain(unit.getLotNumber(), dto);
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
        appendRootSource(traceabilityLot, dto);
        return dto;
    }

    private void buildTraceabilityChain(TraceabilityLot traceabilityLot, GenealogyDto dto) {
        TraceabilityLot current = traceabilityLot;
        while (current != null) {
            if (current.getSourceType() == TraceabilitySourceType.FILTRATION && current.getFiltrationOperationId() != null) {
                filtrationRepo.findByIdAndIsDeletedFalse(current.getFiltrationOperationId())
                        .ifPresent(operation -> dto.getFiltrations().add(toFiltrationStep(operation)));
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

        deliveryRepo.findById(rootSourceId).ifPresent(delivery ->
                dto.getRootSources().add(toRootSource(delivery, traceabilityLot.getSourceType())));
    }

    private void buildLegacyFiltrationChain(String lotNumber, GenealogyDto dto) {
        if (lotNumber == null) {
            return;
        }

        Optional<FiltrationOperation> opOpt = filtrationRepo.findByTargetLotNumberAndIsDeletedFalse(lotNumber);
        if (opOpt.isPresent()) {
            FiltrationOperation op = opOpt.get();
            dto.getFiltrations().add(toFiltrationStep(op));
            buildLegacyFiltrationChain(op.getSourceLotNumber(), dto);
            return;
        }

        findRootSources(lotNumber, dto);
    }

    private FiltrationStepDto toFiltrationStep(FiltrationOperation op) {
        FiltrationStepDto step = new FiltrationStepDto();
        step.setOperationId(op.getId());
        step.setSourceLotNumber(op.getSourceLotNumber());
        step.setTargetLotNumber(op.getTargetLotNumber());
        step.setVolumeFiltered(op.getVolumeAfter());
        step.setTimestamp(op.getOperationDate() != null ? op.getOperationDate().toString() : null);

        if (op.getSourceStorageUnit() != null) {
            step.setSourceStorageUnitId(op.getSourceStorageUnit().getId());
            step.setSourceStorageUnitName(op.getSourceStorageUnit().getName());
        }

        return step;
    }

    private void findRootSources(String lotNumber, GenealogyDto dto) {
        List<UnifiedDelivery> deliveries = deliveryRepo
                .findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, DeliveryType.OIL);
        for (UnifiedDelivery delivery : deliveries) {
            dto.getRootSources().add(toRootSource(delivery, null));
        }
    }

    private RootSourceDto toRootSource(UnifiedDelivery delivery, TraceabilitySourceType sourceType) {
        RootSourceDto root = new RootSourceDto();

        if (sourceType == TraceabilitySourceType.TRITURATION || delivery.getMillMachine() != null) {
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
