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
import com.xdev.communicator.models.production.enums.OperationType;
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
            throw new IllegalArgumentException(
                    "All QualityControlResultDto must reference the same delivery"
            );
        }

        // 2) Load that delivery
        UnifiedDelivery delivery = deliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Delivery not found for ID " + deliveryId
                ));

        // 3) Load & validate rules
        Map<UUID, QualityControlRule> ruleMap = fetchAndValidateRules(dtos);

        // 4) If it’s an OIL delivery AND NOT still waiting for pricing, create exactly one transaction now.
        if (unifiedDeliveryService.isValidForTransaction(delivery)) {
            oilTransactionService.createSingleOilTransaction(delivery);
        }

        // 5) Map each DTO → entity
        List<QualityControlResult> entities = dtos.stream()
                .map(dto -> {
                    QualityControlRule rule = ruleMap.get(dto.getRule().getId());
                    validateMeasuredValue(dto.getMeasuredValue(), rule);

                    QualityControlResult e = new QualityControlResult();
                    e.setRule(rule);
                    e.setMeasuredValue(dto.getMeasuredValue());
                    e.setDelivery(delivery);
                    return e;
                })
                .toList();

        // 6) Persist QC results
        List<QualityControlResult> saved = repository.saveAll(entities);

        // 7) Mark delivery as quality-checked
        delivery.setHasQualityControl(true);

        if (delivery.getOperationType() == OperationType.BASE) {
            delivery.setStatus(OliveLotStatus.PROD_READY);
        } else {
            delivery.setStatus(delivery.getDeliveryType() == DeliveryType.OIL ? OliveLotStatus.OIL_CONTROLLED : OliveLotStatus.OLIVE_CONTROLLED);
        }
        deliveryRepo.save(delivery);

        // 8) Map back to DTOs
        List<QualityControlResultDto> resultDtos = saved.stream()
                .map(e -> modelMapper.map(e, QualityControlResultDto.class))
                .toList();
        OSMLogger.logMethodExit(this.getClass(), "saveAll", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "saveAll", startTime, System.currentTimeMillis());
        return resultDtos;
    }

    // ——————————————————————————————————

    // Helper: fetch & validate rule IDs
    private Map<UUID, QualityControlRule> fetchAndValidateRules(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "fetchAndValidateRules", dtos);
        Set<UUID> ruleIds = dtos.stream()
                .peek(dto -> {
                    if (dto.getRule() == null || dto.getRule().getId() == null) {
                        throw new IllegalArgumentException("Each DTO must reference a valid Rule ID");
                    }
                })
                .map(dto -> dto.getRule().getId())
                .collect(Collectors.toSet());

        List<QualityControlRule> rules = ruleRepository.findAllById(ruleIds);
        if (rules.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more provided Rule IDs were not found");
        }
        Map<UUID, QualityControlRule> ruleMap = rules.stream()
                .collect(Collectors.toMap(QualityControlRule::getId, Function.identity()));
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

    public List<QualityControlResultDto> savebatch(List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "savebatch", dtos);
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
        if (sod.getDeliveryType() == DeliveryType.OIL)// since we just added new results
            sod.setStatus(OliveLotStatus.OIL_CONTROLLED);
        else
            sod.setStatus(OliveLotStatus.OLIVE_CONTROLLED);
        deliveryRepo.save(sod);

        List<QualityControlResult> savedDtos = qualityControlResultRepository.saveAll(list);
        List<QualityControlResultDto> resultDtos = savedDtos.stream().map((element) -> modelMapper.map(element, QualityControlResultDto.class)).toList();
        OSMLogger.logMethodExit(this.getClass(), "savebatch", resultDtos);
        OSMLogger.logPerformance(this.getClass(), "savebatch", startTime, System.currentTimeMillis());
        return resultDtos;
    }
}