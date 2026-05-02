package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.xdev.communicator.models.enums.DeliveryType;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenealogyService {

    private final StorageUnitRepo storageUnitRepo;
    private final FiltrationOperationRepo filtrationRepo;
    private final DeliveryRepository deliveryRepo;

    public GenealogyService(StorageUnitRepo storageUnitRepo, 
                            FiltrationOperationRepo filtrationRepo, 
                            DeliveryRepository deliveryRepo) {
        this.storageUnitRepo = storageUnitRepo;
        this.filtrationRepo = filtrationRepo;
        this.deliveryRepo = deliveryRepo;
    }

    public GenealogyDto getFullGenealogy(UUID storageUnitId) {
        StorageUnit unit = storageUnitRepo.findById(storageUnitId)
                .orElseThrow(() -> new RuntimeException("Storage unit not found"));

        GenealogyDto dto = new GenealogyDto();
        dto.setStorageUnitId(unit.getId());
        dto.setLotNumber(unit.getLotNumber());
        dto.setStorageUnitName(unit.getName());

        String currentLot = unit.getLotNumber();
        buildFiltrationChain(currentLot, dto);

        return dto;
    }

    private void buildFiltrationChain(String lotNumber, GenealogyDto dto) {
        if (lotNumber == null) return;

        Optional<FiltrationOperation> opOpt = filtrationRepo.findByTargetLotNumberAndIsDeletedFalse(lotNumber);
        
        if (opOpt.isPresent()) {
            FiltrationOperation op = opOpt.get();
            FiltrationStepDto step = new FiltrationStepDto();
            step.setOperationId(op.getId());
            step.setSourceLotNumber(op.getSourceLotNumber());
            step.setTargetLotNumber(op.getTargetLotNumber());
            step.setVolumeFiltered(op.getVolumeAfter());
            step.setTimestamp(op.getOperationDate().toString());
            
            if (op.getSourceStorageUnit() != null) {
                step.setSourceStorageUnitId(op.getSourceStorageUnit().getId());
                step.setSourceStorageUnitName(op.getSourceStorageUnit().getName());
            }
            
            dto.getFiltrations().add(step);
            
            // Recurse to source
            buildFiltrationChain(op.getSourceLotNumber(), dto);
        } else {
            // No more filtrations, find root sources (Reception or Trituration)
            findRootSources(lotNumber, dto);
        }
    }

    private void findRootSources(String lotNumber, GenealogyDto dto) {
        List<UnifiedDelivery> deliveries = deliveryRepo.findAllByLotNumberAndDeliveryTypeAndIsDeletedFalse(lotNumber, DeliveryType.OIL);
        
        for (UnifiedDelivery delivery : deliveries) {
            RootSourceDto root = new RootSourceDto();
            
            if (delivery.getMillMachine() != null) {
                root.setType("TRITURATION");
                root.setSourceId(delivery.getMillMachine().getId());
                root.setSupplierName("Production Interne (Moulin: " + delivery.getMillMachine().getName() + ")");
            } else {
                root.setType("RECEPTION");
                root.setSourceId(delivery.getId());
                root.setSupplierName(delivery.getSupplierType() != null ? delivery.getSupplierType().getName() : "Inconnu");
            }
            
            root.setLotNumber(delivery.getLotNumber());
            root.setDate(delivery.getDeliveryDate() != null ? delivery.getDeliveryDate().toString() : "");
            
            Map<String, Object> extra = new HashMap<>();
            if (delivery.getRendement() != null) extra.put("rendement", delivery.getRendement());
            if (delivery.getOilVariety() != null) extra.put("variety", delivery.getOilVariety().getName());
            if (delivery.getStorageUnit() != null) {
                extra.put("storageUnitId", delivery.getStorageUnit().getId());
                extra.put("storageUnitName", delivery.getStorageUnit().getName());
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

            dto.getRootSources().add(root);
        }
    }
}
