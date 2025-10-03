package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.QualityControlResultDto;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.enums.RuleType;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.QualityControlResultRepository;
import com.osm.oilproductionservice.repository.QualityControlRuleRepository;
import com.xdev.communicator.models.enums.OperationType;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QualityControlResultService extends BaseServiceImpl<QualityControlResult, QualityControlResultDto, QualityControlResultDto> {

    private static final Logger log = LoggerFactory.getLogger(QualityControlResultService.class);
    private final DeliveryRepository deliveryRepository;
    private final QualityControlResultRepository repository;
    private final QualityControlRuleRepository ruleRepository;
    private final DeliveryRepository deliveryRepo;
    private final ModelMapper modelMapper;
    private final UnifiedDeliveryService unifiedDeliveryService;
    private final QualityControlResultRepository qualityControlResultRepository;
    private final OilTransactionRepository oilTransactionRepository;
    private final OilTransactionService oilTransactionService;
    Set<String> allowedSet = new HashSet<>(Arrays.asList("Extra Vierge", "Vierge", "Lampante"));

    public QualityControlResultService(BaseRepository<QualityControlResult> repository, ModelMapper modelMapper, QualityControlResultRepository repository1, QualityControlRuleRepository ruleRepository, DeliveryRepository deliveryRepo, ModelMapper modelMapper1, UnifiedDeliveryService unifiedDeliveryService, QualityControlResultRepository qualityControlResultRepository, OilTransactionRepository oilTransactionRepository, OilTransactionService oilTransactionService, DeliveryRepository deliveryRepository) {
        super(repository, modelMapper);
        this.repository = repository1;
        this.ruleRepository = ruleRepository;
        this.deliveryRepo = deliveryRepo;
        this.modelMapper = modelMapper1;
        this.unifiedDeliveryService = unifiedDeliveryService;
        this.qualityControlResultRepository = qualityControlResultRepository;
        this.oilTransactionRepository = oilTransactionRepository;
        this.oilTransactionService = oilTransactionService;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public List<QualityControlResultDto> findAll() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findAll", null);
        try {
            throw new UnsupportedOperationException("Not implemented yet");
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "findAll", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "findAll", null);
            OSMLogger.logPerformance(this.getClass(), "findAll", startTime, System.currentTimeMillis());
        }
    }

    @Override
    public Set<Action> actionsMapping(QualityControlResult result) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

        return actions;
    }

    @Transactional
    public List<QualityControlResultDto> saveAll(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveAll", dtos);
        if (dtos.isEmpty()) {
            OSMLogger.logMethodExit(this.getClass(), "saveAll", Collections.emptyList());
            OSMLogger.logPerformance(this.getClass(), "saveAll", startTime, System.currentTimeMillis());
            return Collections.emptyList();
        }

        // 1) Ensure all DTOs point to the same delivery
        UUID deliveryId = dtos.getFirst().getDeliveryId();
        if (dtos.stream().anyMatch(dto -> !deliveryId.equals(dto.getDeliveryId()))) {
            throw new IllegalArgumentException("All QualityControlResultDto must reference the same delivery");
        }

        // 2) Load that delivery
        UnifiedDelivery delivery = deliveryRepo.findById(deliveryId).orElseThrow(() -> new IllegalArgumentException("Delivery not found for ID " + deliveryId));

        // 3) Load & validate rules
        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);


        // 5) Map each DTO → entity
        List<QualityControlResult> entities = dtos.stream().map(dto -> {
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());
            validateMeasuredValue(dto.getMeasuredValue(), rule);
            QualityControlResult e = new QualityControlResult();
            e.setRule(rule);
            e.setMeasuredValue(dto.getMeasuredValue());
            e.setDelivery(delivery);
            return e;
        }).toList();

        Optional<QualityControlResult> match = entities.stream()
                // 2) match on measuredValue ∈ allowed
                .filter(qcr -> allowedSet.contains(qcr.getMeasuredValue()))
                // 3) grab the first match (or use findAny())
                .findFirst();

        if (match.isPresent()) {
            QualityControlResult result = match.get();
            delivery.setCategoryOliveOil(result.getMeasuredValue());
        }

        // 6) Persist QC results
        List<QualityControlResult> saved = repository.saveAll(entities);

        // 7) Mark delivery as quality-checked
        delivery.setHasQualityControl(true);

        if (delivery.getOperationType() == OperationType.BASE && delivery.getDeliveryType() == DeliveryType.OLIVE) {
            delivery.setStatus(OliveLotStatus.PROD_READY);
        } else {
            delivery.setStatus(delivery.getDeliveryType() == DeliveryType.OIL ? OliveLotStatus.OIL_CONTROLLED : OliveLotStatus.OLIVE_CONTROLLED);
        }
        deliveryRepo.save(delivery);

        // 8) Map back to DTOs
        List<QualityControlResultDto> resultDtos = saved.stream().map(e -> modelMapper.map(e, QualityControlResultDto.class)).toList();
        OSMLogger.logMethodExit(this.getClass(), "saveAll", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "saveAll", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    @Transactional
    public List<QualityControlResultDto> saveOilQcForOliveRec(UUID idx, List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveAllForIdx", idx, dtos);
        if (dtos.isEmpty()) {
            OSMLogger.logMethodExit(this.getClass(), "saveAllForIdx", Collections.emptyList());
            OSMLogger.logPerformance(this.getClass(), "saveAllForIdx", startTime, System.currentTimeMillis());
            return Collections.emptyList();
        }
        // You can add custom logic for idx here (e.g., link to a batch, etc.)
        UnifiedDelivery newOIlRec = unifiedDeliveryService.createOilRecFromOliveRecImpl(idx, true);
        log.info("Saving QC results for idx: {} ({} results)", idx, dtos.size());
        // Validate rules
        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);
        // Map each DTO → entity (no delivery linkage)
        List<QualityControlResult> entities = dtos.stream().map(dto -> {
            QualityControlRule rule = ruleMap.get(dto.getRule().getId());
            validateMeasuredValue(dto.getMeasuredValue(), rule);
            QualityControlResult e = new QualityControlResult();
            e.setRule(rule);
            e.setMeasuredValue(dto.getMeasuredValue());
            e.setDelivery(newOIlRec); // if you add an idx field to the entity
            return e;
        }).toList();
        // Persist QC results
        List<QualityControlResult> saved = repository.saveAll(entities);
        // Map back to DTOs
        List<QualityControlResultDto> resultDtos = saved.stream().map(e -> modelMapper.map(e, QualityControlResultDto.class)).toList();
        OSMLogger.logMethodExit(this.getClass(), "saveAllForIdx", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "saveAllForIdx", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    // ——————————————————————————————————

    // Helper: fetch & validate rule IDs
    private Map<UUID, QualityControlRule> fetchAndValidateRules(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "fetchAndValidateRules", dtos);
        Set<UUID> ruleIds = dtos.stream().peek(dto -> {
            if (dto.getRule() == null || dto.getRule().getId() == null) {
                throw new IllegalArgumentException("Each DTO must reference a valid Rule ID");
            }
        }).map(dto -> dto.getRule().getId()).collect(Collectors.toSet());

        List<QualityControlRule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more provided Rule IDs were not found");
        }
        Map<UUID, QualityControlRule> ruleMap = rules.stream().collect(Collectors.toMap(QualityControlRule::getId, Function.identity()));
        OSMLogger.logMethodExit(this.getClass(), "fetchAndValidateRules", ruleMap);
        OSMLogger.logPerformance(this.getClass(), "fetchAndValidateRules", startTime, System.currentTimeMillis());
        return ruleMap;
    }

    // ✅ extracted validation
    private void validateMeasuredValue(String measuredValue, QualityControlRule rule) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "validateMeasuredValue", measuredValue, rule);
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
        OSMLogger.logMethodExit(this.getClass(), "validateMeasuredValue", null);
        OSMLogger.logPerformance(this.getClass(), "validateMeasuredValue", startTime, System.currentTimeMillis());
    }


    @Transactional(readOnly = true)
    public List<QualityControlResultDto> findByDeliveryId(UUID deliveryId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByDeliveryId", deliveryId);
        log.debug("Fetching quality control results for deliveryId: {}", deliveryId);
        if (deliveryId == null) {
            log.error("Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID is required");
        }

        List<QualityControlResult> results = repository.findByDeliveryId(deliveryId);
        log.debug("Found {} quality control results for deliveryId: {}", results.size(), deliveryId);

        List<QualityControlResultDto> resultDtos = results.stream().map(entity -> modelMapper.map(entity, QualityControlResultDto.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "findByDeliveryId", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "findByDeliveryId", startTime, System.currentTimeMillis());
        return resultDtos;
    }

//    @Transactional(readOnly = true)
//    public List<QualityControlResultDto> findOilResultsByOliveDeliveryFromOliveLotNumber(String oliveLotNUmber) {
//        long startTime = System.currentTimeMillis();
//        OSMLogger.logMethodEntry(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", oliveLotNUmber);
////UUID oilRecFromOliveRec_Lotnumber = deliveryRepository.findByLotOliveNumber(oliveLotNUmber).getFirst().getQualityControlResults()
////        List<QualityControlResult> results = repository.findByDeliveryIdAndRule_OilQcTrue(deliveryId);
//         List<QualityControlResult> results = (List<QualityControlResult>) deliveryRepository.findByLotOliveNumber(oliveLotNUmber).getFirst().getQualityControlResults();
//        List<QualityControlResultDto> resultDtos = results.stream()
//                .map(e -> modelMapper.map(e, QualityControlResultDto.class))
//                .toList();
//        OSMLogger.logMethodExit(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", resultDtos);
//        OSMLogger.logPerformance(this.getClass(), "findOilResultsByOliveDeliveryFromOliveLotNumber", startTime, System.currentTimeMillis());
//        return resultDtos;
//    }
}