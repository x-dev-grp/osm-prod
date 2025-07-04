package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.*;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UnifiedDeliveryService extends BaseServiceImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private final GenericRepository genericRepository;
    private final DeliveryRepository deliveryRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierInfoTypeRepository supplierInfoTypeRepository;
    private final StorageUnitRepo storageUnitRepo;

    public UnifiedDeliveryService(BaseRepository<UnifiedDelivery> repository, ModelMapper modelMapper, GenericRepository genericRepository, DeliveryRepository deliveryRepository, SupplierRepository supplierRepository, SupplierInfoTypeRepository supplierInfoTypeRepository, StorageUnitRepo storageUnitRepo) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
        this.deliveryRepository = deliveryRepository;
        this.supplierRepository = supplierRepository;
        this.supplierInfoTypeRepository = supplierInfoTypeRepository;
        this.storageUnitRepo = storageUnitRepo;
    }

    @Override
    public UnifiedDeliveryDTO save(UnifiedDeliveryDTO dto) {
        // Map DTO to entity
        UnifiedDelivery delivery = modelMapper.map(dto, UnifiedDelivery.class);


        if (dto.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getSupplierInfo().getId()));
            delivery.setSupplierType(supplier);
        }

        // Save entity
        UnifiedDelivery savedDelivery = deliveryRepository.saveAndFlush(delivery);

        // Map back to DTO and return
        return modelMapper.map(savedDelivery, UnifiedDeliveryDTO.class);
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO update(UnifiedDeliveryDTO dto) {
        // 1. Load existing or fail
        UnifiedDelivery existing = deliveryRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("UnifiedDelivery not found with id: " + dto.getId()));


        BeanUtils.copyProperties(dto, existing, "id", "supplier", "storageUnit", "externalId");

        // 3. Resolve and set the Supplier relationship
        if (dto.getSupplier() != null && dto.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getId()));
            existing.setSupplierType(supplier);
        } else {
            existing.setSupplierType(null);
        }
        if (dto.getStorageUnit() != null && dto.getStorageUnit().getId() != null) {
            StorageUnit stu = storageUnitRepo.findById(dto.getStorageUnit().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getStorageUnit().getId()));
            existing.setStorageUnit(stu);
        } else {
            existing.setStorageUnit(null);
        }

        // 4. Persist changes
        UnifiedDelivery updated = deliveryRepository.saveAndFlush(existing);

        // 5. Map back to DTO and return
        return modelMapper.map(updated, UnifiedDeliveryDTO.class);
    }

    public List<UnifiedDeliveryDTO> getForPlanning() {
        return deliveryRepository.findOliveDeliveriesControlled().stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
    }

    public List<UnifiedDeliveryDTO> findByDeliveryTypeInAndQualityControlResultsIsNull(List<String> types) {
        return deliveryRepository.findByDeliveryTypeInAndQualityControlResultsIsNull(types).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
    }

    // Get deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getDeliveriesBySupplier(UUID supplierId) {
        return deliveryRepository.findBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
    }

    // Get paid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getPaidDeliveriesBySupplier(UUID supplierId) {
        return deliveryRepository.findPaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
    }

    // Get unpaid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getUnpaidDeliveriesBySupplier(UUID supplierId) {
        return deliveryRepository.findUnpaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
    }

    @Override
    public Set<Action> actionsMapping(UnifiedDelivery delivery) {
        if (delivery.getDeliveryType() == DeliveryType.OIL) return mapOilDeliveryActions(delivery);
        else return mapOliveDeliveryActions(delivery);
    }

    private Set<Action> mapOliveDeliveryActions(UnifiedDelivery delivery) {
        Set<Action> actions = new HashSet<>();
        switch (delivery.getStatus()) {
            case NEW -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE, Action.TO_PROD, Action.OLIVE_QUALITY));

            }
            case IN_PROGRESS -> {
                actions.add(Action.COMPLETE);
            }
            case OLIVE_CONTROLLED -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE, Action.TO_PROD, Action.UPDATE_OLIVE_QUALITY));
                switch (delivery.getOperationType()) {
                    case EXCHANGE -> {
                        actions.add(Action.OIL_OUT_TRANSACTION);
                    }

                }

            }
            case COMPLETED -> {
                actions.add(Action.UPDATE);
                switch (delivery.getOperationType()) {
                    case SIMPLE_RECEPTION -> {
                        actions.add(Action.OIL_PAYMENT);
                    }
                    case BASE, OLIVE_PURCHASE -> {
                        actions.add(Action.OIL_RECEPTION);
                    }
                    case EXCHANGE -> {
                        actions.addAll(Set.of(Action.OIL_OUT_TRANSACTION, Action.OIL_RECEPTION));
                    }

                }
            }

        }
        return actions;
    }

    private Set<Action> mapOilDeliveryActions(UnifiedDelivery delivery) {
        Set<Action> actions = new HashSet<>();
        switch (delivery.getStatus()) {
            case NEW -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE, Action.OIL_QUALITY));

            }
            case OIL_CONTROLLED -> {
                actions.addAll(Set.of(Action.UPDATE_OIL_QUALITY, Action.OIL_IN_TRANSACTION));

            }
        }
        return actions;
    }
}
