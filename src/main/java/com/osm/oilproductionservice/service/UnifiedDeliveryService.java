package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.ExchangePricingDto;
import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.*;
import com.xdev.communicator.models.production.enums.OperationType;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedDeliveryService extends BaseServiceImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private final GenericRepository genericRepository;
    private final DeliveryRepository deliveryRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierInfoTypeRepository supplierInfoTypeRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final OilTransactionService oilTransactionService;
    public UnifiedDeliveryService(BaseRepository<UnifiedDelivery> repository, ModelMapper modelMapper, GenericRepository genericRepository, DeliveryRepository deliveryRepository, SupplierRepository supplierRepository, SupplierInfoTypeRepository supplierInfoTypeRepository, StorageUnitRepo storageUnitRepo, OilTransactionService oilTransactionService) {
        super(repository, modelMapper);
        this.genericRepository = genericRepository;
        this.deliveryRepository = deliveryRepository;
        this.supplierRepository = supplierRepository;
        this.supplierInfoTypeRepository = supplierInfoTypeRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.oilTransactionService = oilTransactionService;
    }

    @Override
    public UnifiedDeliveryDTO save(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", dto);
        // Map DTO to entity
        UnifiedDelivery delivery = modelMapper.map(dto, UnifiedDelivery.class);


        if (dto.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getSupplierInfo().getId()));
            delivery.setSupplierType(supplier);
        }
        delivery.setStatus(OliveLotStatus.NEW);


        // Save entity
        UnifiedDelivery savedDelivery = deliveryRepository.saveAndFlush(delivery);

        // Map back to DTO and return
        OSMLogger.logMethodExit(this.getClass(), "save", savedDelivery);
        OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
        return modelMapper.map(savedDelivery, UnifiedDeliveryDTO.class);
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO update(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "update", dto);
        // 1. Load existing or fail
        UnifiedDelivery existing = deliveryRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("UnifiedDelivery not found with id: " + dto.getId()));

        dto.setStatus(existing.getStatus());
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
//todo check poid net at this step
        // 4. Persist changes
        UnifiedDelivery updated = deliveryRepository.saveAndFlush(existing);
        if(isValidForTransaction(updated)){
            oilTransactionService.createSingleOilTransactionIn(updated);
        }
        // 5. Map back to DTO and return
        OSMLogger.logMethodExit(this.getClass(), "update", updated);
        OSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
        return modelMapper.map(updated, UnifiedDeliveryDTO.class);
    }

    public List<UnifiedDeliveryDTO> getForPlanning() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getForPlanning", null);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findOliveDeliveriesControlled().stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getForPlanning", result);
        OSMLogger.logPerformance(this.getClass(), "getForPlanning", startTime, System.currentTimeMillis());
        return result;
    }

    public List<UnifiedDeliveryDTO> findByDeliveryTypeInAndQualityControlResultsIsNull(List<String> types) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", types);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findByDeliveryTypeInAndQualityControlResultsIsNull(types).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", result);
        OSMLogger.logPerformance(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", startTime, System.currentTimeMillis());
        return result;
    }

    // Get deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get paid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getPaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findFullyPaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getPaidDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getPaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get unpaid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getUnpaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getUnpaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findUnpaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getUnpaidDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getUnpaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    @Override
    public Set<Action> actionsMapping(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", delivery);
        if (delivery.getDeliveryType() == DeliveryType.OIL) {
            Set<Action> actions = mapOilDeliveryActions(delivery);
            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        } else {
            Set<Action> actions = mapOliveDeliveryActions(delivery);
            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        }
    }

    private Set<Action> mapOliveDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "mapOliveDeliveryActions", delivery);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        actions.add(Action.GEN_PDF);
        switch (delivery.getStatus()) {
            case NEW -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE , Action.OLIVE_QUALITY));

            }
            case IN_PROGRESS -> {
                actions.add(Action.COMPLETE);
            }
            case OLIVE_CONTROLLED , PROD_READY -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE,   Action.UPDATE_OLIVE_QUALITY));
                switch (delivery.getOperationType()) {
                    case EXCHANGE -> {
                        actions.add(Action.SET_PRICE);
                        if (delivery.getStatus() == OliveLotStatus.PROD_READY) {
                            actions.add(Action.OIL_OUT_TRANSACTION);
                        }
                    }
                    case OLIVE_PURCHASE -> {
                        actions.add(Action.SET_PRICE);

                    }

                }

            }
            case COMPLETED -> {
                switch (delivery.getOperationType()) {
                    case SIMPLE_RECEPTION -> {
                        actions.add(Action.OIL_QUALITY);
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
        OSMLogger.logMethodExit(this.getClass(), "mapOliveDeliveryActions", actions);
        OSMLogger.logPerformance(this.getClass(), "mapOliveDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }

    private Set<Action> mapOilDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "mapOilDeliveryActions", delivery);
        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);
        switch (delivery.getStatus()) {
            case NEW -> {
                actions.addAll(Set.of(Action.CANCEL, Action.DELETE, Action.UPDATE, Action.OIL_QUALITY));

            }
            case OIL_CONTROLLED -> {
                actions.addAll(Set.of(Action.UPDATE_OIL_QUALITY, Action.SET_PRICE));

            }
            case PROD_READY -> {
                actions.add(Action.OIL_IN_TRANSACTION);
            }
        }
        OSMLogger.logMethodExit(this.getClass(), "mapOilDeliveryActions", actions);
        OSMLogger.logPerformance(this.getClass(), "mapOilDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }

    public UUID createOilRecFromOliveRecImpl(UUID uuid) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createOilRecFromOliveRecImpl", uuid);
        UnifiedDelivery delivery = repository.findById(uuid).orElse(null);
        if (delivery != null) {
            UnifiedDelivery newDelivery = new UnifiedDelivery();
            newDelivery.setDeliveryType(DeliveryType.OIL);
            newDelivery.setStatus(OliveLotStatus.WAITING_FOR_PRICING);
            Map<String, Object> deliveryMap = generateDeliveryNumber(delivery);
            newDelivery.setLotNumber(deliveryMap.get("lotNumber").toString());
            newDelivery.setDeliveryNumber(deliveryMap.get("deliveryNumber").toString());
            newDelivery.setDeliveryDate(LocalDateTime.now());
            newDelivery.setOilQuantity(0.0);
            newDelivery.setUnitPrice(0.0);
            newDelivery.setOilType(delivery.getOliveType());
            newDelivery.setRegion(delivery.getRegion());
            newDelivery.setSupplier(delivery.getSupplier());
            newDelivery.setLotOliveNumber(delivery.getLotNumber());
            newDelivery.setOperationType(OperationType.PAYMENT);

            newDelivery.setOilVariety(delivery.getOliveVariety());
            delivery.setStatus(OliveLotStatus.WAITING_FOR_PRICING);
            repository.save(delivery);
            return repository.save(newDelivery).getId();

        }
        OSMLogger.logMethodExit(this.getClass(), "createOilRecFromOliveRecImpl", null);
        OSMLogger.logPerformance(this.getClass(), "createOilRecFromOliveRecImpl", startTime, System.currentTimeMillis());
        return null;
    }

    public UUID createOilTransactionFromExchange(UUID uuid,UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();

        OSMLogger.logMethodExit(this.getClass(), "createOilTransactionFromExchange", null);
        OSMLogger.logPerformance(this.getClass(), "createOilTransactionFromExchange", startTime, System.currentTimeMillis());
        return null;
    }

    private Map<String, Object> generateDeliveryNumber(UnifiedDelivery del) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateDeliveryNumber", del);
        // 1) Find last delivery to calculate next sequence
        UnifiedDelivery last = deliveryRepository
                .findTopByOrderByCreatedDateDesc()
                .orElse(null);

        int nextSeq = 1;
        if (last != null) {
            try {
                nextSeq = Integer.parseInt(last.getDeliveryNumber()) + 1;
            } catch (NumberFormatException e) {
                // log.warn("Invalid deliveryNumber on last record, resetting to 1", e);
                nextSeq = 1;
            }
        }

        // 2) Build each piece
        String sequencePart = String.format("%04d", nextSeq);          // zero-padded to 4 digits
        String oliveTypeCode = del.getOliveType().getName();           // e.g. "OB"
        int year = del.getDeliveryDate().getYear();                    // e.g. 2025
        String yearPart = String.format("%02d", year % 100);           // last two digits: "25"

        // 3) Concatenate into final lot number
        String lotNumber = sequencePart + oliveTypeCode + yearPart;    // "0005OB25"

        // 4) Return both if you still need the raw sequence
        Map<String, Object> map = new HashMap<>();
        map.put("deliveryNumber", nextSeq);
        map.put("lotNumber", lotNumber);
        OSMLogger.logMethodExit(this.getClass(), "generateDeliveryNumber", map);
        OSMLogger.logPerformance(this.getClass(), "generateDeliveryNumber", startTime, System.currentTimeMillis());
        return map;
    }

    boolean isValidForTransaction(UnifiedDelivery delivery) {
        return (delivery.getDeliveryType() == DeliveryType.OIL && (delivery.getOilQuantity()!=null && delivery.getOilQuantity() > 0)&& (delivery.getPrice() != null && delivery.getPrice() > 0) && delivery.getStorageUnit() != null && (delivery.getUnitPrice() != null && delivery.getUnitPrice() > 0));
    }

    @Transactional
    public void updateStatus(UUID id, OliveLotStatus status) {
        try {
            repository.findById(id).ifPresent(delivery -> {
                delivery.setStatus(status);
                deliveryRepository.save(delivery);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void updateprice(UUID id, Double unitPrice) {
        try {
            repository.findById(id).ifPresent(delivery -> {
                delivery.setUnitPrice(unitPrice);
                switch (delivery.getDeliveryType()) {
                    case OIL -> {
                        delivery.setPrice(unitPrice * delivery.getOilQuantity());
                        delivery.setStatus(OliveLotStatus.STOCK_READY);

                    }
                    case OLIVE -> {
                        delivery.setPrice(unitPrice * delivery.getPoidsNet());
                        delivery.setStatus(OliveLotStatus.PROD_READY);
                    }
                    case null, default -> {}
                }

                deliveryRepository.save(delivery);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } }

    @Transactional
    public void updateExchangePricingAndCreateOilTransactionOut(ExchangePricingDto dto) {
        UnifiedDelivery delivery = deliveryRepository.findById(dto.getDeliveryId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Delivery not found: " + dto.getDeliveryId())
                );

        // 1) update pricing on the Delivery
        delivery.setUnitPrice(dto.getUnitPrice());
        delivery.setPrice(dto.getPrice());
        delivery.setStatus(OliveLotStatus.PROD_READY);
//        delivery.setQualityGrade(dto.getQualityGrade());
        deliveryRepository.save(delivery);
        oilTransactionService.createSingleOilTransactionOut(delivery, dto);

    }
}
