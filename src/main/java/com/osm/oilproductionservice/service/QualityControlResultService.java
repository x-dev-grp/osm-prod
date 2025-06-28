package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.QualityControlResultDto;
import com.osm.oilproductionservice.enums.*;
import com.osm.oilproductionservice.model.*;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.QualityControlResultRepository;
import com.osm.oilproductionservice.repository.QualityControlRuleRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QualityControlResultService extends BaseServiceImpl<QualityControlResult, QualityControlResultDto, QualityControlResultDto> {

    private static final Logger log = LoggerFactory.getLogger(QualityControlResultService.class);

    private final QualityControlResultRepository repository;

    private final QualityControlRuleRepository ruleRepository;
    private final DeliveryRepository deliveryRepo;

    private final ModelMapper modelMapper;

    private final UnifiedDeliveryService unifiedDeliveryService;
    private final UnifiedDeliveryRepository unifiedDeliveryRepository;
    private final QualityControlResultRepository qualityControlResultRepository;
    private final OilTransactionRepository oilTransactionRepository;
    private final OilTransactionService oilTransactionService;

    public QualityControlResultService(BaseRepository<QualityControlResult> repository, ModelMapper modelMapper, QualityControlResultRepository repository1, QualityControlRuleRepository ruleRepository, DeliveryRepository deliveryRepo, ModelMapper modelMapper1, UnifiedDeliveryService unifiedDeliveryService,
                                       UnifiedDeliveryRepository unifiedDeliveryRepository, QualityControlResultRepository qualityControlResultRepository, OilTransactionRepository oilTransactionRepository, OilTransactionService oilTransactionService) {
        super(repository, modelMapper);
        this.repository = repository1;
        this.ruleRepository = ruleRepository;
        this.deliveryRepo = deliveryRepo;
        this.modelMapper = modelMapper1;
        this.unifiedDeliveryService = unifiedDeliveryService;
        this.unifiedDeliveryRepository = unifiedDeliveryRepository;
        this.qualityControlResultRepository = qualityControlResultRepository;
        this.oilTransactionRepository = oilTransactionRepository;
        this.oilTransactionService = oilTransactionService;
    }

    @Transactional
    public List<QualityControlResultDto> saveAll(List<QualityControlResultDto> dtos) {
        log.debug("Processing saveAll for {} DTOs", dtos.size());

        if (dtos.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Preload all Rule IDs and validate in batch
        Set<UUID> ruleIds = dtos.stream()
                .map(dto -> {
                    if (dto.getRule() == null || dto.getRule().getId() == null) {
                        throw new IllegalArgumentException("Rule is required for QualityControlResult");
                    }
                    return dto.getRule().getId();
                })
                .collect(Collectors.toSet());

        Map<UUID, QualityControlRule> ruleMap = ruleRepository.findAllById(ruleIds).stream()
                .collect(Collectors.toMap(QualityControlRule::getId, rule -> rule));

        if (ruleMap.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more Rules not found for provided IDs");
        }

        // 2. Preload all Delivery IDs
        Set<UUID> deliveryUuids = dtos.stream()
                .map(QualityControlResultDto::getDeliveryId)
                .collect(Collectors.toSet());

        Map<UUID, UnifiedDelivery> deliveryMap = this.deliveryRepo.findAllById(deliveryUuids).stream()
                .collect(Collectors.toMap(UnifiedDelivery::getId, delivery -> delivery));

        if (deliveryMap.size() != deliveryUuids.size()) {
            throw new IllegalArgumentException("One or more Deliveries not found for provided IDs");
        }

        // 3. Process DTOs
        List<QualityControlResult> entities = new ArrayList<>();
        for (QualityControlResultDto dto : dtos) {
            QualityControlResult entity = new QualityControlResult();

            // Get Rule
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());

            // Validate measured value
            validateMeasuredValue(dto.getMeasuredValue(), rule);

            entity.setRule(rule);
            entity.setMeasuredValue(dto.getMeasuredValue());

            // Get Delivery
            UUID deliveryId = dto.getDeliveryId();
            UnifiedDelivery delivery = deliveryMap.get(deliveryId);
            entity.setDelivery(delivery);

            entities.add(entity);
        }

        // 4. Save all entities in one go
        log.debug("Saving {} QualityControlResult entities", entities.size());
        List<QualityControlResult> savedEntities = repository.saveAll(entities);

        // 5. Update hasQualityControl flag
        for (UnifiedDelivery delivery : deliveryMap.values()) {
            delivery.setHasQualityControl(true); // since we just added new results
            delivery.setStatus(OliveLotStatus.CONTROLLED);
        }
        deliveryRepo.saveAll(deliveryMap.values());

        // 6. Map to DTO
        return savedEntities.stream()
                .map(entity -> modelMapper.map(entity, QualityControlResultDto.class))
                .collect(Collectors.toList());
    }

    // ✅ extracted validation
    private void validateMeasuredValue(String measuredValue, QualityControlRule rule) {
        RuleType ruleType = rule.getRuleType();

        switch (ruleType) {
            case NUMERIC:
                try {
                    Double value = Double.parseDouble(measuredValue);
                    if (rule.getMinValue() != null && value < rule.getMinValue()) {
                        throw new IllegalArgumentException("Measured value below minValue for rule ID: " + rule.getId());
                    }
                    if (rule.getMaxValue() != null && value > rule.getMaxValue()) {
                        throw new IllegalArgumentException("Measured value above maxValue for rule ID: " + rule.getId());
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric value for rule ID: " + rule.getId());
                }
                break;

            case BOOLEAN:
                if (!"true".equalsIgnoreCase(measuredValue) && !"false".equalsIgnoreCase(measuredValue)) {
                    throw new IllegalArgumentException("Invalid boolean value for rule ID: " + rule.getId());
                }
                break;

            case STRING:
                String allowedText = rule.getRuleTextValue();
                if (allowedText != null && !allowedText.trim().isEmpty()) {
                    List<String> allowedValues = Arrays.stream(allowedText.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    if (!allowedValues.contains(measuredValue)) {
                        throw new IllegalArgumentException("Invalid string value for rule ID: " + rule.getId() +
                                ". Allowed values are: " + allowedValues);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown rule type: " + ruleType);
        }
    }


    @Override
    public List<QualityControlResultDto> findAll() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Transactional(readOnly = true)
    public List<QualityControlResultDto> findByDeliveryId(UUID deliveryId) {
        log.debug("Fetching quality control results for deliveryId: {}", deliveryId);
        if (deliveryId == null) {
            log.error("Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID is required");
        }

        List<QualityControlResult> results = repository.findByDeliveryId(deliveryId);
        log.debug("Found {} quality control results for deliveryId: {}", results.size(), deliveryId);

        return results.stream()
                .map(entity -> modelMapper.map(entity, QualityControlResultDto.class))
                .collect(Collectors.toList());
    }

//    @Transactional
//    public List<QualityControlResultDto> updateAll(List<QualityControlResultDto> dtos) {
//        log.debug("Processing updateAll for {} DTOs", dtos.size());
//        List<QualityControlResult> entities = dtos.stream().map(dto -> {
//            log.debug("Mapping DTO for update: {}", dto);
//
//            // Ensure ID is present for updates
//            if (dto.getId() == null) {
//                log.error("ID is required for updating QualityControlResult");
//                throw new IllegalArgumentException("ID is required for updating QualityControlResult");
//            }
//
//            // Fetch existing entity
//            QualityControlResult entity = repository.findById(dto.getId())
//                    .orElseThrow(() -> {
//                        log.error("Quality control result not found for ID: {}", dto.getId());
//                        return new IllegalArgumentException("Quality control result not found for ID: " + dto.getId());
//                    });
//
//            // Set measured value
//            if (RuleType.NUMERIC.equals(dto.getRule().getRuleType())) {
//                try {
//                    Double.parseDouble(dto.getMeasuredValue());
//                } catch (NumberFormatException e) {
//                    throw new IllegalArgumentException("Invalid numeric value for rule: " + dto.getRule().getId());
//                }
//            } else if (RuleType.BOOLEAN.equals(dto.getRule().getRuleType())) {
//                if (!"true".equalsIgnoreCase(dto.getMeasuredValue()) && !"false".equalsIgnoreCase(dto.getMeasuredValue())) {
//                    throw new IllegalArgumentException("Invalid boolean value for rule: " + dto.getRule().getId());
//                }
//            } else {
//                throw new IllegalArgumentException("Invalid rule type: " + dto.getRule().getRuleType());
//            }
//            entity.setMeasuredValue(dto.getMeasuredValue());
//
//            // Handle rule
//            QualityControlRuleDto ruleDto = dto.getRule();
//            if (ruleDto != null) {
//                QualityControlRule rule = ruleRepository.findById(ruleDto.getId())
//                        .orElseThrow(() -> {
//                            log.error("Rule not found for ID: {}", ruleDto.getId());
//                            return new IllegalArgumentException("Rule not found for ID: " + ruleDto.getId());
//                        });
//                entity.setRule(rule);
//            } else {
//                log.error("Rule is required for QualityControlResult");
//                throw new IllegalArgumentException("Rule is required for QualityControlResult");
//            }
//
//            // Handle delivery
//            UnifiedDeliveryDTO deliveryDto = dto.getDelivery();
//            if (deliveryDto != null) {
//                UnifiedDeliveryDTO existingDelivery = unifiedDeliveryService.findById(deliveryDto.getId());
//                if (existingDelivery == null) {
//                    log.error("Delivery not found for ID: {}", deliveryDto.getId());
//                    throw new IllegalArgumentException("Delivery not found for ID: " + deliveryDto.getId());
//                }
//                UnifiedDelivery delivery = modelMapper.map(existingDelivery, UnifiedDelivery.class);
//                entity.setDelivery(delivery);
//            } else {
//                log.error("Delivery is required for QualityControlResult");
//                throw new IllegalArgumentException("Delivery is required for QualityControlResult");
//            }
//
//            return entity;
//        }).collect(Collectors.toList());
//
//        log.debug("Updating {} entities", entities.size());
//        List<QualityControlResult> updatedEntities = repository.saveAll(entities);
//        log.debug("Mapping {} updated entities to DTOs", updatedEntities.size());
//        return updatedEntities.stream()
//                .map(entity -> modelMapper.map(entity, QualityControlResultDto.class))
//                .collect(Collectors.toList());
//    }

    @Override
    public Set<String> actionsMapping(QualityControlResult result) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }

    public List<QualityControlResultDto> savebatch(List<QualityControlResultDto> dtos) {
        UnifiedDelivery sod = unifiedDeliveryRepository.findById(dtos.getFirst().getDeliveryId()).orElse(null);
        if (Objects.nonNull(sod) && sod.getDeliveryType() == DeliveryType.OIL) {
            OilTransaction oilTransaction = new OilTransaction();
            oilTransaction.setStorageUnitDestination(sod.getStorageUnit());
            oilTransaction.setStorageUnitSource(null);
            oilTransaction.setTransactionType(TransactionType.RECEPTION_IN);
            oilTransaction.setQualityGrade(null);
            oilTransaction.setTransactionState(TransactionState.COMPLETED);
            oilTransaction.setQuantityKg(sod.getOilQuantity());
            oilTransaction.setUnitPrice(sod.getUnitPrice());
            oilTransaction.setReception(sod);
            oilTransactionService.save(modelMapper.map(oilTransaction, OilTransactionDTO.class));
        }

        List<QualityControlResult> list = dtos.stream().map((element) -> modelMapper.map(element, QualityControlResult.class)).toList();

        sod.setHasQualityControl(true); // since we just added new results
        sod.setStatus(OliveLotStatus.CONTROLLED);
        deliveryRepo.saveAndFlush(sod);

        List<QualityControlResult> savedDtos = qualityControlResultRepository.saveAll(list);
        return savedDtos.stream().map((element) -> modelMapper.map(element, QualityControlResultDto.class)).toList();
    }
}