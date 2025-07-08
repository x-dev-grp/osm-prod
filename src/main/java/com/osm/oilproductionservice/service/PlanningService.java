package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.MillMachineRepository;
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
@RequiredArgsConstructor
public class PlanningService {

    public static final String MILL_NOT_FOUND = "Mill not found: ";
    public static final String DELIVERY_NOT_FOUND = "Delivery not found: ";
    public static final String NO_DELIVERIES_FOUND_FOR_GLOBAL_LOT = "No deliveries found for global lot: ";
    private static final Logger log = LoggerFactory.getLogger(PlanningService.class);
    private final MillMachineRepository millRepo;
    private final DeliveryRepository deliveryRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public void savePlanning(PlanningSaveRequest req) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "savePlanning", req);
        try {
            log.info("Saving planning at {}", new Date());

            validateRequest(req);

            // 1. Get all deliveries that are currently assigned to mills
            List<UnifiedDelivery> currentlyAssignedDeliveries = deliveryRepo.findByMillMachineIsNotNull();

            // 2. Load all deliveries referenced in the request
            Map<String, UnifiedDelivery> deliveryMap = loadDeliveries(req);

            // 3. Clear assignments for deliveries that are in the request
            clearAssignments(deliveryMap.values());

            // 4. If no lots are assigned in the request, clear all mill assignments
            if (req.getMills().stream().allMatch(m -> m.getItems().isEmpty())) {
                log.info("No lots assigned in request, clearing all mill assignments");
                currentlyAssignedDeliveries.forEach(d -> {
                    d.setMillMachine(null);
                    d.setGlobalLotNumber(null);
                });
                deliveryRepo.saveAll(currentlyAssignedDeliveries);
                return;
            }

            // 5. Process assignments for each mill
            Set<String> processedLotNumbers = new HashSet<>();
            req.getMills().forEach(millPlan -> {
                if (millPlan.getItems() != null && !millPlan.getItems().isEmpty()) {
                    // Process regular lots
                    millPlan.getItems().stream()
                            .filter(item -> item.getType().equals(PlanItemType.LOT.toString()))
                            .forEach(item -> {
                                UnifiedDelivery delivery = deliveryMap.get(item.getId());
                                if (delivery != null && delivery.getStatus() != OliveLotStatus.COMPLETED) {
                                    MillMachine mill = millRepo.findById(millPlan.getMillMachineId())
                                            .orElseThrow(() -> new IllegalArgumentException(MILL_NOT_FOUND + millPlan.getMillMachineId()));
                                    delivery.setMillMachine(mill);
                                    processedLotNumbers.add(delivery.getLotNumber());
                                }
                            });

                    // Process global lots
                    millPlan.getItems().stream()
                            .filter(item -> item.getType().equals(PlanItemType.GLOBAL_LOT.toString()))
                            .forEach(item -> req.getGlobalLots().stream()
                                    .filter(gl -> gl.getGlobalLotNumber().equals(item.getId()))
                                    .findFirst()
                                    .ifPresent(globalLot -> {
                                        MillMachine mill = millRepo.findById(millPlan.getMillMachineId())
                                                .orElseThrow(() -> new IllegalArgumentException(MILL_NOT_FOUND + millPlan.getMillMachineId()));

                                        globalLot.getLots().forEach(lotDto -> {
                                            UnifiedDelivery delivery = deliveryMap.get(lotDto.getLotNumber());
                                            if (delivery != null && delivery.getStatus() != OliveLotStatus.COMPLETED) {
                                                delivery.setMillMachine(mill);
                                                delivery.setGlobalLotNumber(globalLot.getGlobalLotNumber());
                                                processedLotNumbers.add(delivery.getLotNumber());
                                            }
                                        });
                                    }));
                }
            });

            // 6. Clear assignments for lots that were previously assigned but not in the current request
            currentlyAssignedDeliveries.stream()
                    .filter(d -> !processedLotNumbers.contains(d.getLotNumber()))
                    .forEach(d -> {
                        d.setMillMachine(null);
                        d.setGlobalLotNumber(null);
                    });

            // 7. Save all changes
            List<UnifiedDelivery> allDeliveriesToSave = new ArrayList<>();
            allDeliveriesToSave.addAll(deliveryMap.values());
            allDeliveriesToSave.addAll(currentlyAssignedDeliveries.stream()
                    .filter(d -> !processedLotNumbers.contains(d.getLotNumber()))
                    .toList());

            deliveryRepo.saveAll(allDeliveriesToSave);
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
            // Get all delivery IDs (lotNumbers) from mill assignments
            Set<String> deliveryIds = req.getMills().stream()
                    .flatMap(m -> m.getItems().stream())
                    .filter(item -> item.getType().equals("LOT"))
                    .map(PlanItemDTO::getId)
                    .collect(Collectors.toSet());

            // Get all delivery IDs (lotNumbers) from global lots
            deliveryIds.addAll(req.getGlobalLots().stream()
                    .flatMap(g -> g.getLots().stream())
                    .map(UnifiedDeliveryDTO::getLotNumber)
                    .collect(Collectors.toSet()));

            // Load deliveries by lotNumber, filtering out completed ones
            List<UnifiedDelivery> deliveries = deliveryRepo.findByLotNumberIn(deliveryIds).stream()
                    .filter(d -> d.getStatus() != OliveLotStatus.COMPLETED)
                    .toList();

            return deliveries.stream()
                    .collect(Collectors.toMap(UnifiedDelivery::getLotNumber, d -> d));
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
                List<UnifiedDelivery> deliveries = deliveryRepo.findByMillMachineIdAndStatus(mill.getId(), OliveLotStatus.OLIVE_CONTROLLED.name());
                if (!deliveries.isEmpty()) {
                    MillPlanDTO millPlan = millPlans.stream()
                            .filter(mp -> mp.getMillMachineId().equals(mill.getId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Mill plan not found"));
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
            List<UnifiedDelivery> allDeliveries = deliveryRepo.findOliveDeliveriesControlled()
                    .stream()
                    .filter(d -> d.getStatus() == OliveLotStatus.OLIVE_CONTROLLED).toList();
            List<UnifiedDeliveryDTO> allDeliveryDtos = allDeliveries.stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).toList();
            Map<String, List<UnifiedDeliveryDTO>> globalLotGroups = allDeliveryDtos.stream()
                    .filter(d -> d.getGlobalLotNumber() != null)
                    .collect(Collectors.groupingBy(UnifiedDeliveryDTO::getGlobalLotNumber));

            List<GlobalLotDto> globalLots = globalLotGroups.entrySet().stream()
                    .map(entry -> {
                        String globalLotNumber = entry.getKey();
                        List<UnifiedDeliveryDTO> deliveries = entry.getValue();
                        double totalWeight = deliveries.stream()
                                .mapToDouble(UnifiedDeliveryDTO::getPoidsNet)
                                .sum();
                        List<UnifiedDeliveryDTO> deliveryDtos = deliveries.stream()
                                .map(d -> {
                                    UnifiedDeliveryDTO dto = new UnifiedDeliveryDTO();
                                    dto.setId(d.getId());
                                    dto.setLotNumber(d.getLotNumber());
                                    dto.setOliveQuantity(d.getPoidsNet());
                                    dto.setDeliveryDate(d.getTrtDate() != null ? LocalDateTime.parse(d.getTrtDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null);
                                    dto.setMillMachine(d.getMillMachine() != null ? d.getMillMachine() : null);
                                    dto.setGlobalLotNumber(globalLotNumber);
                                    return dto;
                                })
                                .collect(Collectors.toList());

                        // Add GLOBAL_LOT to mill if assigned
                        deliveries.stream()
                                .filter(d -> d.getMillMachine() != null)
                                .findFirst()
                                .ifPresent(d -> {
                                    MillPlanDTO millPlan = millPlans.stream()
                                            .filter(mp -> mp.getMillMachineId().equals(d.getMillMachine().getId()))
                                            .findFirst()
                                            .orElseThrow(() -> new IllegalStateException("Mill plan not found"));
                                    PlanItemDTO item = new PlanItemDTO();
                                    item.setType("GLOBAL_LOT");
                                    item.setId(globalLotNumber);
                                    millPlan.getItems().add(item);
                                });

                        return new GlobalLotDto(globalLotNumber, totalWeight, deliveryDtos);
                    })
                    .collect(Collectors.toList());

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
    public void markLotCompleted(String lotNumber, Double oilQuantity, Double rendement) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "markLotCompleted", lotNumber, oilQuantity, rendement);
        try {
            List<UnifiedDelivery> delivery = deliveryRepo.findByLotNumberIn(Set.of(lotNumber));
            if (delivery.isEmpty()) {
                throw new EntityNotFoundException("Lot not found: " + lotNumber);
            }
            UnifiedDelivery lot = delivery.get(0);
            lot.setStatus(OliveLotStatus.COMPLETED);
            lot.setOilQuantity(oilQuantity);
            lot.setRendement(rendement);
            deliveryRepo.save(lot);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "markLotCompleted", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "markLotCompleted", null);
            OSMLogger.logPerformance(this.getClass(), "markLotCompleted", startTime, System.currentTimeMillis());
        }
    }

    @Transactional
    public void markGlobalLotCompleted(String globalLotNumber, Double oilQuantity, Double rendement) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "markGlobalLotCompleted", globalLotNumber, oilQuantity, rendement);
        try {
            List<UnifiedDelivery> list = deliveryRepo.findByGlobalLotNumber(globalLotNumber);
            if (list.isEmpty()) {
                throw new EntityNotFoundException("Global lot not found: " + globalLotNumber);
            }
            for (UnifiedDelivery d : list) {
                d.setStatus(OliveLotStatus.COMPLETED);
                d.setOilQuantity(oilQuantity); // or distribute as needed
                d.setRendement(rendement);     // or distribute as needed
            }
            deliveryRepo.saveAll(list);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "markGlobalLotCompleted", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "markGlobalLotCompleted", null);
            OSMLogger.logPerformance(this.getClass(), "markGlobalLotCompleted", startTime, System.currentTimeMillis());
        }
    }
}