package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.*;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.MillMachineRepository;
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

    private static final Logger log = LoggerFactory.getLogger(PlanningService.class);
    public static final String MILL_NOT_FOUND = "Mill not found: ";
    public static final String DELIVERY_NOT_FOUND = "Delivery not found: ";
    public static final String NO_DELIVERIES_FOUND_FOR_GLOBAL_LOT = "No deliveries found for global lot: ";

    private final MillMachineRepository millRepo;
    private final DeliveryRepository deliveryRepo;
    private final ModelMapper modelMapper;

    @Transactional
    public void savePlanning(PlanningSaveRequest req) {
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
                    .filter(item ->  item.getType().equals(PlanItemType.GLOBAL_LOT.toString()))
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
    }

    private void validateRequest(PlanningSaveRequest req) {
        if (req.getMills() == null || req.getMills().isEmpty()) {
            throw new IllegalArgumentException("No mills provided");
        }
    }

    private Map<String, UnifiedDelivery> loadDeliveries(PlanningSaveRequest req) {
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
    }

    private void clearAssignments(Collection<UnifiedDelivery> deliveries) {
        deliveries.forEach(d -> {
            d.setMillMachine(null);
            d.setGlobalLotNumber(null);
        });
    }

    @Transactional
    public PlanningSaveRequest getPlanning() {
        log.info("Fetching current planning state at {}", new Date());

        // Get all mills
       List<MillMachine> mills = millRepo.findAll();
        List<MillMachineDto> millMachineDtos= mills.stream().map((element) -> modelMapper.map(element, MillMachineDto.class)).toList();
        List<MillPlanDTO> millPlans = new ArrayList<>();
        Set<String> assignedLotNumbers = new HashSet<>();

        // Initialize MillPlanDTO for all mills
        for (MillMachineDto mill : millMachineDtos) {
            millPlans.add(new MillPlanDTO(mill.getId(), new ArrayList<>()));
        }

        // Assign deliveries to mills
        for (MillMachineDto mill : millMachineDtos) {
            List<UnifiedDelivery> deliveries = deliveryRepo.findByMillMachineIdAndStatus(mill.getId(), OliveLotStatus.CONTROLLED.name());
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
            .filter(d -> d.getStatus() == OliveLotStatus.CONTROLLED).toList();        List<UnifiedDeliveryDTO> allDeliveryDtos = allDeliveries.stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).toList();
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
    }

    @Transactional
    public void markLotCompleted(String lotNumber) {
        List<UnifiedDelivery> delivery = deliveryRepo.findByLotNumberIn((Set.of(lotNumber)));

        delivery.getFirst().setStatus(OliveLotStatus.COMPLETED);   // enum field
        deliveryRepo.save(delivery.getFirst());
    }

    @Transactional
    public void markGlobalLotCompleted(String globalLotNumber) {
        List<UnifiedDelivery> list = deliveryRepo.findByGlobalLotNumber(globalLotNumber);
        if (list.isEmpty()) {
            throw new EntityNotFoundException("Global lot not found: " + globalLotNumber);
        }
        list.forEach(d -> d.setStatus(OliveLotStatus.COMPLETED));
        deliveryRepo.saveAll(list);
    }
}