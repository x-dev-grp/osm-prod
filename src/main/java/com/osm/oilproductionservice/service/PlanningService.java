package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.enums.TransactionType;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.MillMachineRepository;
import com.xdev.communicator.models.production.enums.OperationType;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    public static final String MILL_NOT_FOUND = "Mill not found: ";
    public static final String DELIVERY_NOT_FOUND = "Delivery not found: ";
    public static final String NO_DELIVERIES_FOUND_FOR_GLOBAL_LOT = "No deliveries found for global lot: ";
    private static final Logger log = LoggerFactory.getLogger(PlanningService.class);
    private final MillMachineRepository millRepo;
    private final DeliveryRepository deliveryRepo;
    private final ModelMapper modelMapper;
    private final UnifiedDeliveryService unifiedDeliveryService;
    private final OilTransactionService oilTransactionService;
    public PlanningService(MillMachineRepository millRepo, DeliveryRepository deliveryRepo, ModelMapper modelMapper, UnifiedDeliveryService unifiedDeliveryService, OilTransactionService oilTransactionService) {
        this.millRepo = millRepo;
        this.deliveryRepo = deliveryRepo;
        this.modelMapper = modelMapper;
        this.unifiedDeliveryService = unifiedDeliveryService;
        this.oilTransactionService = oilTransactionService;
    }

    @Transactional
    public void savePlanning(PlanningSaveRequest req) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "savePlanning", req);
        try {
            log.info("Saving planning at {}", new Date());

            validateRequest(req);

            // 1. Load all deliveries (both in request and currently assigned)
            Map<String, UnifiedDelivery> deliveryMap = loadDeliveries(req);

            // 2. Store original statuses for unassigned lots
            Map<String, OliveLotStatus> originalStatuses = new HashMap<>();
            deliveryMap.values().forEach(d -> {
                originalStatuses.put(d.getLotNumber(), d.getStatus());
            });

            // 3. Clear assignments for all deliveries first
            clearAssignments(deliveryMap.values());

            // 4. If no lots are assigned in the request, clear all mill assignments
            if (req.getMills().stream().allMatch(m -> m.getItems().isEmpty())) {
                log.info("No lots assigned in request, clearing all mill assignments");
                deliveryMap.values().forEach(d -> {
                    d.setMillMachine(null);
                    if (d.getOperationType() == OperationType.BASE || d.getOperationType() == OperationType.EXCHANGE) {
                        d.setStatus(OliveLotStatus.PROD_READY);
                    } else {
                        d.setStatus(!d.getQualityControlResults().isEmpty() ? OliveLotStatus.OLIVE_CONTROLLED : OliveLotStatus.NEW);
                    }
                    d.setGlobalLotNumber(null);
                });
                deliveryRepo.saveAll(deliveryMap.values());
                return; // Exit early since no assignments
            }

            // 5. Process assignments for each mill
            Set<String> processedLotNumbers = new HashSet<>();
            req.getMills().forEach(millPlan -> {
                if (millPlan.getItems() != null && !millPlan.getItems().isEmpty()) {
                    // Process regular lots
                    millPlan.getItems().stream().filter(item -> item.getType().equals("LOT")).forEach(item -> {
                        UnifiedDelivery delivery = deliveryMap.get(item.getId());
                        if (delivery != null && delivery.getStatus() != OliveLotStatus.COMPLETED) {
                            MillMachine mill = millRepo.findById(millPlan.getMillMachineId()).orElseThrow(() -> new IllegalArgumentException(MILL_NOT_FOUND + millPlan.getMillMachineId()));
                            delivery.setMillMachine(mill);
                            delivery.setStatus(OliveLotStatus.IN_PROGRESS); // Set assigned lots to IN_PROGRESS
                            processedLotNumbers.add(delivery.getLotNumber());
                        }
                    });

                    // Process global lots
                    millPlan.getItems().stream().filter(item -> item.getType().equals("GLOBAL_LOT")).forEach(item -> req.getGlobalLots().stream().filter(gl -> gl.getGlobalLotNumber().equals(item.getId())).findFirst().ifPresent(globalLot -> {
                        MillMachine mill = millRepo.findById(millPlan.getMillMachineId()).orElseThrow(() -> new IllegalArgumentException(MILL_NOT_FOUND + millPlan.getMillMachineId()));

                        globalLot.getLots().forEach(lotDto -> {
                            UnifiedDelivery delivery = deliveryMap.get(lotDto.getLotNumber());
                            if (delivery != null && delivery.getStatus() != OliveLotStatus.COMPLETED) {
                                delivery.setMillMachine(mill);
                                delivery.setGlobalLotNumber(globalLot.getGlobalLotNumber());
                                delivery.setStatus(OliveLotStatus.IN_PROGRESS); // Set assigned lots to IN_PROGRESS
                                processedLotNumbers.add(delivery.getLotNumber());
                            }
                        });
                    }));
                }
            });

            // 6. Handle unassigned lots - revert to their previous status
            deliveryMap.values().stream().filter(d -> !processedLotNumbers.contains(d.getLotNumber())).forEach(d -> {
                d.setMillMachine(null);
                d.setGlobalLotNumber(null);

                // Revert to previous status based on operation type and quality control
                if (d.getOperationType() == OperationType.BASE) {
                    d.setStatus(OliveLotStatus.PROD_READY);
                } else {
                    d.setStatus(!d.getQualityControlResults().isEmpty() ? OliveLotStatus.OLIVE_CONTROLLED : OliveLotStatus.NEW);
                }
            });

            // 7. Save all changes
            deliveryRepo.saveAll(deliveryMap.values());

            log.info("Planning saved successfully. {} lots assigned to mills, {} lots unassigned", processedLotNumbers.size(), deliveryMap.size() - processedLotNumbers.size());
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "savePlanning", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "savePlanning", null);
            OSMLogger.logPerformance(this.getClass(), "savePlanning", startTime, System.currentTimeMillis());
        }
    }

    private void validateRequest(PlanningSaveRequest req) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "validateRequest", req);
        try {
            if (req.getMills() == null || req.getMills().isEmpty()) {
                throw new IllegalArgumentException("No mills provided");
            }
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "validateRequest", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "validateRequest", null);
            OSMLogger.logPerformance(this.getClass(), "validateRequest", startTime, System.currentTimeMillis());
        }
    }

    private Map<String, UnifiedDelivery> loadDeliveries(PlanningSaveRequest req) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "loadDeliveries", req);
        try {
            // Load all available deliveries for planning (those that can be assigned to mills)
            List<UnifiedDelivery> allAvailableDeliveries = deliveryRepo.findOliveDeliveriesControlled();

            // Filter out completed deliveries
            List<UnifiedDelivery> deliveries = allAvailableDeliveries.stream().filter(d -> d.getStatus() != OliveLotStatus.COMPLETED).toList();

            return deliveries.stream().collect(Collectors.toMap(UnifiedDelivery::getLotNumber, d -> d));
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "loadDeliveries", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "loadDeliveries", null);
            OSMLogger.logPerformance(this.getClass(), "loadDeliveries", startTime, System.currentTimeMillis());
        }
    }

    private void clearAssignments(Collection<UnifiedDelivery> deliveries) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "clearAssignments", deliveries);
        try {
            deliveries.forEach(d -> {
                d.setMillMachine(null);
                d.setGlobalLotNumber(null);

                // Set appropriate status based on operation type and quality control
                if (d.getOperationType() == OperationType.BASE) {
                    d.setStatus(OliveLotStatus.PROD_READY);
                } else {
                    d.setStatus(!d.getQualityControlResults().isEmpty() ? OliveLotStatus.OLIVE_CONTROLLED : OliveLotStatus.NEW);
                }
            });
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "clearAssignments", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "clearAssignments", null);
            OSMLogger.logPerformance(this.getClass(), "clearAssignments", startTime, System.currentTimeMillis());
        }
    }

    @Transactional
    public PlanningSaveRequest getPlanning() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPlanning", null);
        try {
            log.info("Fetching current planning state at {}", new Date());

            // Get all mills
            List<MillMachine> mills = millRepo.findAll();
            List<MillMachineDto> millMachineDtos = mills.stream().map((element) -> modelMapper.map(element, MillMachineDto.class)).toList();
            List<MillPlanDTO> millPlans = new ArrayList<>();
            Set<String> assignedLotNumbers = new HashSet<>();

            // Initialize MillPlanDTO for all mills
            for (MillMachineDto mill : millMachineDtos) {
                millPlans.add(new MillPlanDTO(mill.getId(), new ArrayList<>()));
            }

            // Assign deliveries to mills
            for (MillMachineDto mill : millMachineDtos) {
                List<UnifiedDelivery> deliveries = deliveryRepo.findByMillMachineIdAndStatus(mill.getId(), OliveLotStatus.IN_PROGRESS.name());
                if (!deliveries.isEmpty()) {
                    MillPlanDTO millPlan = millPlans.stream().filter(mp -> mp.getMillMachineId().equals(mill.getId())).findFirst().orElseThrow(() -> new IllegalStateException("Mill plan not found"));
                    deliveries.forEach(d -> {
                        PlanItemDTO item = new PlanItemDTO();
                        item.setType("LOT");
                        item.setId(d.getLotNumber());
                        UnifiedDeliveryDTO lot = new UnifiedDeliveryDTO();
                        lot.setId(d.getId());
                        lot.setLotNumber(d.getLotNumber());
                        lot.setOliveQuantity(d.getPoidsNet());
                        lot.setDeliveryDate(d.getTrtDate() != null ? LocalDateTime.parse(d.getTrtDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null);
                        lot.setMillMachine(mill);
                        lot.setGlobalLotNumber(d.getGlobalLotNumber());
                        item.setLot(lot);
                        millPlan.getItems().add(item);
                        assignedLotNumbers.add(d.getLotNumber());
                    });
                }
            }

            // Group deliveries by global lot
            List<UnifiedDelivery> allDeliveries = deliveryRepo.findOliveDeliveriesControlled();
            List<UnifiedDeliveryDTO> allDeliveryDtos = allDeliveries.stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).toList();
            Map<String, List<UnifiedDeliveryDTO>> globalLotGroups = allDeliveryDtos.stream().filter(d -> d.getGlobalLotNumber() != null).collect(Collectors.groupingBy(UnifiedDeliveryDTO::getGlobalLotNumber));

            List<GlobalLotDto> globalLots = globalLotGroups.entrySet().stream().map(entry -> {
                String globalLotNumber = entry.getKey();
                List<UnifiedDeliveryDTO> deliveries = entry.getValue();
                double totalWeight = deliveries.stream().mapToDouble(UnifiedDeliveryDTO::getPoidsNet).sum();
                List<UnifiedDeliveryDTO> deliveryDtos = deliveries.stream().map(d -> {
                    UnifiedDeliveryDTO dto = new UnifiedDeliveryDTO();
                    dto.setId(d.getId());
                    dto.setLotNumber(d.getLotNumber());
                    dto.setOliveQuantity(d.getPoidsNet());
                    dto.setDeliveryDate(d.getTrtDate() != null ? LocalDateTime.parse(d.getTrtDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null);
                    dto.setMillMachine(d.getMillMachine() != null ? d.getMillMachine() : null);
                    dto.setGlobalLotNumber(globalLotNumber);
                    return dto;
                }).collect(Collectors.toList());

                // Add GLOBAL_LOT to mill if assigned
                deliveries.stream().filter(d -> d.getMillMachine() != null).findFirst().ifPresent(d -> {
                    MillPlanDTO millPlan = millPlans.stream().filter(mp -> mp.getMillMachineId().equals(d.getMillMachine().getId())).findFirst().orElseThrow(() -> new IllegalStateException("Mill plan not found"));
                    PlanItemDTO item = new PlanItemDTO();
                    item.setType("GLOBAL_LOT");
                    item.setId(globalLotNumber);
                    millPlan.getItems().add(item);
                });

                return new GlobalLotDto(globalLotNumber, totalWeight, deliveryDtos);
            }).collect(Collectors.toList());

            return new PlanningSaveRequest(millPlans, globalLots);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "getPlanning", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getPlanning", null);
            OSMLogger.logPerformance(this.getClass(), "getPlanning", startTime, System.currentTimeMillis());
        }
    }

    @Transactional
    public void markLotCompleted(String lotNumber, Double oilQuantity, Double rendement, Double unpaidPrice,boolean autoSetStorage ) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "markLotCompleted", lotNumber, oilQuantity, rendement);
        try {
            // Validate input parameters
            if (lotNumber == null || lotNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Lot number cannot be null or empty");
            }

            if (oilQuantity != null && oilQuantity < 0) {
                throw new IllegalArgumentException("Oil quantity cannot be negative");
            }

            if (rendement != null && (rendement < 0 || rendement > 100)) {
                throw new IllegalArgumentException("Rendement must be between 0 and 100");
            }

            if (unpaidPrice != null && unpaidPrice < 0) {
                throw new IllegalArgumentException("Unpaid price cannot be negative");
            }

            List<UnifiedDelivery> delivery = deliveryRepo.findByLotNumberIn(Set.of(lotNumber));
            if (delivery.isEmpty()) {
                throw new EntityNotFoundException("Lot not found: " + lotNumber);
            }
            UnifiedDelivery lot = delivery.getFirst();

            lot.setOilQuantity(oilQuantity);
            lot.setRendement(rendement);
            lot.setPaid(false);
            lot.setOilType(lot.getOliveType());
            lot.setOilVariety(lot.getOliveVariety());
            lot.setStatus(OliveLotStatus.COMPLETED);

            deliveryRepo.save(lot);
            if (lot.getOperationType() == OperationType.EXCHANGE ||
                    lot.getOperationType() == OperationType.BASE ||
                    lot.getOperationType() == OperationType.OLIVE_PURCHASE) {

                unifiedDeliveryService.createOilRecFromOliveRecImpl(lot.getId(),false);

            }
            if(lot.getOperationType()==OperationType.SIMPLE_RECEPTION){
                lot.setUnpaidAmount(unpaidPrice);

            }
            if(autoSetStorage ) {
                if(lot.getSupplier() != null && lot.getSupplier().getStorageUnit() != null) {
                    OilTransactionDTO oilTransaction = new OilTransactionDTO();
                    oilTransaction.setTransactionState(TransactionState.COMPLETED);
                    oilTransaction.setOilType(modelMapper.map(lot.getOliveType(),BaseTypeDto.class));
                    oilTransaction.setTransactionType(TransactionType.TRANSFER_IN);
                    oilTransaction.setReception(modelMapper.map(lot,UnifiedDeliveryDTO.class));
                    oilTransaction.setQuantityKg(lot.getOilQuantity());
                    oilTransaction.setUnitPrice(0.0);
                    oilTransaction.setTotalPrice(0.0);
                    oilTransaction.setStorageUnitDestination(modelMapper.map(lot.getSupplier().getStorageUnit(),StorageUnitDto.class));
                    oilTransactionService.save(oilTransaction);
                }

            }


            log.info("Lot {} marked as completed with oilQuantity: {}, rendement: {}, unpaidPrice: {}", lotNumber, oilQuantity, rendement, unpaidPrice);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "markLotCompleted", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "markLotCompleted", null);
            OSMLogger.logPerformance(this.getClass(), "markLotCompleted", startTime, System.currentTimeMillis());
        }
    }

    @Transactional

    public void markGlobalLotCompleted(String globalLotNumber, List<ChildLotCompletionDto> childLots) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "markGlobalLotCompleted", globalLotNumber, childLots);

        try {
            // Validate input parameters
            if (globalLotNumber == null || globalLotNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Global lot number cannot be null or empty");
            }

            if (childLots == null || childLots.isEmpty()) {
                throw new IllegalArgumentException("Child lots list cannot be null or empty");
            }

            // verify global lot exists
            List<UnifiedDelivery> existing = deliveryRepo.findByGlobalLotNumber(globalLotNumber);
            if (existing.isEmpty()) {
                throw new EntityNotFoundException("Global lot not found: " + globalLotNumber);
            }

            // delegate each child
            childLots.forEach(dto -> markLotCompleted(dto.getLotNumber(), dto.getOilQuantity(), dto.getRendement(), dto.getUnpaidPrice(),false));

            log.info("Global lot {} completed with {} child lots", globalLotNumber, childLots.size());
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "markGlobalLotCompleted", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "markGlobalLotCompleted", null);
            OSMLogger.logPerformance(this.getClass(), "markGlobalLotCompleted", startTime, System.currentTimeMillis());
        }
    }
}