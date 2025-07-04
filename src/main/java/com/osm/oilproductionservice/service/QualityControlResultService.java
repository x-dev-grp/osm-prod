package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.QualityControlResultDto;
import com.osm.oilproductionservice.enums.*;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.QualityControlResultRepository;
import com.osm.oilproductionservice.repository.QualityControlRuleRepository;
import com.xdev.xdevbase.models.Action;
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
    private final QualityControlResultRepository qualityControlResultRepository;
    private final OilTransactionRepository oilTransactionRepository;
    private final OilTransactionService oilTransactionService;

    public QualityControlResultService(BaseRepository<QualityControlResult> repository, ModelMapper modelMapper, QualityControlResultRepository repository1, QualityControlRuleRepository ruleRepository, DeliveryRepository deliveryRepo, ModelMapper modelMapper1, UnifiedDeliveryService unifiedDeliveryService, QualityControlResultRepository qualityControlResultRepository, OilTransactionRepository oilTransactionRepository, OilTransactionService oilTransactionService) {
        super(repository, modelMapper);
        this.repository = repository1;
        this.ruleRepository = ruleRepository;
        this.deliveryRepo = deliveryRepo;
        this.modelMapper = modelMapper1;
        this.unifiedDeliveryService = unifiedDeliveryService;
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
        Set<UUID> ruleIds = dtos.stream().map(dto -> {
            if (dto.getRule() == null || dto.getRule().getId() == null) {
                throw new IllegalArgumentException("Rule is required for QualityControlResult");
            }
            return dto.getRule().getId();
        }).collect(Collectors.toSet());

        Map<UUID, QualityControlRule> ruleMap = ruleRepository.findAllById(ruleIds).stream().collect(Collectors.toMap(QualityControlRule::getId, rule -> rule));

        if (ruleMap.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more Rules not found for provided IDs");
        }

        // 2. Preload all Delivery IDs
        Set<UUID> deliveryUuids = dtos.stream().map(QualityControlResultDto::getDeliveryId).collect(Collectors.toSet());

        Map<UUID, UnifiedDelivery> deliveryMap = this.deliveryRepo.findAllById(deliveryUuids).stream().collect(Collectors.toMap(UnifiedDelivery::getId, delivery -> delivery));

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
            delivery.setHasQualityControl(true);
             if(delivery.getDeliveryType() == DeliveryType.OIL)// since we just added new results
               delivery.setStatus(OliveLotStatus.OIL_CONTROLLED);
             else
                 delivery.setStatus(OliveLotStatus.OLIVE_CONTROLLED);

        }
        deliveryRepo.saveAll(deliveryMap.values());

        // 6. Map to DTO
        return savedEntities.stream().map(entity -> modelMapper.map(entity, QualityControlResultDto.class)).collect(Collectors.toList());
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
                    List<String> allowedValues = Arrays.stream(allowedText.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
                    if (!allowedValues.contains(measuredValue)) {
                        throw new IllegalArgumentException("Invalid string value for rule ID: " + rule.getId() + ". Allowed values are: " + allowedValues);
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

        return results.stream().map(entity -> modelMapper.map(entity, QualityControlResultDto.class)).collect(Collectors.toList());
    }

    @Override
    public Set<Action> actionsMapping(QualityControlResult result) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE,Action.DELETE));

        return actions;
    }

    public List<QualityControlResultDto> savebatch(List<QualityControlResultDto> dtos) {
        UnifiedDelivery sod = deliveryRepo.findById(dtos.getFirst().getDeliveryId()).orElse(null);
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
            oilTransaction.setOilType(sod.getOilType());
            oilTransactionService.save(modelMapper.map(oilTransaction, OilTransactionDTO.class));
        }

        List<QualityControlResult> list = dtos.stream().map((element) -> modelMapper.map(element, QualityControlResult.class)).toList();

        sod.setHasQualityControl(true);
        // since we just added new results
        if(sod.getDeliveryType() == DeliveryType.OIL)// since we just added new results
            sod.setStatus(OliveLotStatus.OIL_CONTROLLED);
        else
            sod.setStatus(OliveLotStatus.OLIVE_CONTROLLED);
        deliveryRepo.save(sod);

        List<QualityControlResult> savedDtos = qualityControlResultRepository.saveAll(list);
        return savedDtos.stream().map((element) -> modelMapper.map(element, QualityControlResultDto.class)).toList();
    }
}