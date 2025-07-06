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

        // 1. Collect and validate Rule IDs
        Set<UUID> ruleIds = dtos.stream()
                .peek(dto -> {
                    if (dto.getRule() == null || dto.getRule().getId() == null) {
                        throw new IllegalArgumentException("Rule is required for QualityControlResult");
                    }
                })
                .map(dto -> dto.getRule().getId())
                .collect(Collectors.toSet());

        Map<UUID, QualityControlRule> ruleMap = ruleRepository
                .findAllById(ruleIds)
                .stream()
                .collect(Collectors.toMap(QualityControlRule::getId, rule -> rule));

        if (ruleMap.size() != ruleIds.size()) {
            throw new IllegalArgumentException("One or more Rules not found for provided IDs");
        }

        // 2. Collect and validate Delivery IDs
        Set<UUID> deliveryIds = dtos.stream()
                .map(QualityControlResultDto::getDeliveryId)
                .collect(Collectors.toSet());

        Map<UUID, UnifiedDelivery> deliveryMap = deliveryRepo
                .findAllById(deliveryIds)
                .stream()
                .collect(Collectors.toMap(UnifiedDelivery::getId, d -> d));

        if (deliveryMap.size() != deliveryIds.size()) {
            throw new IllegalArgumentException("One or more Deliveries not found for provided IDs");
        }

        // 3. If it's an oil reception, create one OilTransaction per delivery
        //    (you could also choose to do this per-DTO, but typically there's one per delivery)
        deliveryMap.values().stream()
                .filter(d -> d.getDeliveryType() == DeliveryType.OIL)
                .forEach(oilDelivery -> {
                    OilTransaction tx = new OilTransaction();
                    tx.setStorageUnitDestination(oilDelivery.getStorageUnit());
                    tx.setStorageUnitSource(null);
                    tx.setTransactionType(TransactionType.RECEPTION_IN);
                    tx.setTransactionState(TransactionState.COMPLETED);
                    tx.setQuantityKg(oilDelivery.getOilQuantity());
                    tx.setUnitPrice(oilDelivery.getUnitPrice());
                    tx.setReception(oilDelivery);
                    tx.setOilType(oilDelivery.getOilType());
                    oilTransactionService.save(
                            modelMapper.map(tx, OilTransactionDTO.class)
                    );
                });

        // 4. Map DTO → entity, validate measured value, collect
        List<QualityControlResult> entities = dtos.stream()
                .map(dto -> {
                    QualityControlRule rule = ruleMap.get(dto.getRule().getId());
                    validateMeasuredValue(dto.getMeasuredValue(), rule);

                    QualityControlResult e = new QualityControlResult();
                    e.setRule(rule);
                    e.setMeasuredValue(dto.getMeasuredValue());
                    e.setDelivery(deliveryMap.get(dto.getDeliveryId()));
                    return e;
                })
                .toList();

        log.debug("Saving {} QualityControlResult entities", entities.size());
        List<QualityControlResult> saved = repository.saveAll(entities);

        // 5. Update each delivery’s QC flag and status, then persist
        deliveryMap.values().forEach(d -> {
            d.setHasQualityControl(true);
            if (d.getDeliveryType() == DeliveryType.OIL) {
                d.setStatus(OliveLotStatus.OIL_CONTROLLED);
            } else {
                d.setStatus(OliveLotStatus.OLIVE_CONTROLLED);
            }
        });
        deliveryRepo.saveAll(deliveryMap.values());

        // 6. Map back to DTOs
        return saved.stream()
                .map(e -> modelMapper.map(e, QualityControlResultDto.class))
                .toList();
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
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

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
        if (sod.getDeliveryType() == DeliveryType.OIL)// since we just added new results
            sod.setStatus(OliveLotStatus.OIL_CONTROLLED);
        else
            sod.setStatus(OliveLotStatus.OLIVE_CONTROLLED);
        deliveryRepo.save(sod);

        List<QualityControlResult> savedDtos = qualityControlResultRepository.saveAll(list);
        return savedDtos.stream().map((element) -> modelMapper.map(element, QualityControlResultDto.class)).toList();
    }
}