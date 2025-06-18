package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.QualityControlRuleDto;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class QualityControlRuleService extends BaseServiceImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {
    public QualityControlRuleService(BaseRepository<QualityControlRule> repository, ModelMapper modelMapper) {
        super(repository, modelMapper);
    }

//    private final QualityControlRuleRepository qualityControlRuleRepository;
//    private final ModelMapper modelMapper;
//
//    @Autowired
//    public QualityControlRuleService(QualityControlRuleRepository qualityControlRuleRepository, ModelMapper modelMapper) {
//        this.qualityControlRuleRepository = qualityControlRuleRepository;
//        this.modelMapper = modelMapper;
//    }
//
//    /**
//     * Create a new quality control rule.
//     * Checks for duplicates based on ruleKey or ruleName.
//     */
//    public QualityControlRuleDto createRule(QualityControlRuleDto ruleDto) {
//        // Map DTO to entity
//        QualityControlRule rule = modelMapper.map(ruleDto, QualityControlRule.class);
//
//        // Check if a rule with the same ruleKey exists
//        Optional<QualityControlRule> ruleByKey = qualityControlRuleRepository.findByRuleKey(rule.getRuleKey());
//        if (ruleByKey.isPresent()) {
//            throw new RuntimeException("A rule with the ruleKey '" + rule.getRuleKey() + "' already exists.");
//        }
//
//        // Check if a rule with the same ruleName exists
//        Optional<QualityControlRule> ruleByName = qualityControlRuleRepository.findByRuleName(rule.getRuleName());
//        if (ruleByName.isPresent()) {
//            throw new RuntimeException("A rule with the ruleName '" + rule.getRuleName() + "' already exists.");
//        }
//
//        // Save the rule and map the saved entity back to DTO
//        QualityControlRule savedRule = qualityControlRuleRepository.save(rule);
//        return modelMapper.map(savedRule, QualityControlRuleDto.class);
//    }
//
//    /**
//     * Retrieve all quality control rules.
//     */
//    public List<QualityControlRuleDto> getAllRules() {
//        List<QualityControlRule> rules = qualityControlRuleRepository.findAll();
//        return rules.stream()
//                .map(rule -> modelMapper.map(rule, QualityControlRuleDto.class))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Retrieve a quality control rule by its ID.
//     */
//    public QualityControlRuleDto getRuleById(Long id) {
//        Optional<QualityControlRule> optionalRule = qualityControlRuleRepository.findById(id);
//        return optionalRule.map(rule -> modelMapper.map(rule, QualityControlRuleDto.class)).orElse(null);
//    }
//
//    /**
//     * Update an existing quality control rule.
//     * Checks for duplicates if ruleKey or ruleName is changed.
//     */
//    public QualityControlRuleDto updateRule(Long id, QualityControlRuleDto ruleDetailsDto) {
//        Optional<QualityControlRule> optionalRule = qualityControlRuleRepository.findById(id);
//        if (optionalRule.isPresent()) {
//            QualityControlRule rule = optionalRule.get();
//
//            // Map DTO to entity for rule details
//            QualityControlRule ruleDetails = modelMapper.map(ruleDetailsDto, QualityControlRule.class);
//
//            // Check for duplicate ruleKey if changed
//            if (!rule.getRuleKey().equals(ruleDetails.getRuleKey())) {
//                Optional<QualityControlRule> ruleByKey = qualityControlRuleRepository.findByRuleKey(ruleDetails.getRuleKey());
//                if (ruleByKey.isPresent()) {
//                    throw new RuntimeException("A rule with the ruleKey '" + ruleDetails.getRuleKey() + "' already exists.");
//                }
//            }
//
//            // Check for duplicate ruleName if changed
//            if (!rule.getRuleName().equals(ruleDetails.getRuleName())) {
//                Optional<QualityControlRule> ruleByName = qualityControlRuleRepository.findByRuleName(ruleDetails.getRuleName());
//                if (ruleByName.isPresent()) {
//                    throw new RuntimeException("A rule with the ruleName '" + ruleDetails.getRuleName() + "' already exists.");
//                }
//            }
//
//            // Update the rule fields
//            rule.setRuleKey(ruleDetails.getRuleKey());
//            rule.setRuleName(ruleDetails.getRuleName());
//            rule.setDescription(ruleDetails.getDescription());
//            rule.setMinValue(ruleDetails.getMinValue());
//            rule.setMaxValue(ruleDetails.getMaxValue());
//
//            // Save the updated rule and map it back to DTO
//            QualityControlRule updatedRule = qualityControlRuleRepository.save(rule);
//            return modelMapper.map(updatedRule, QualityControlRuleDto.class);
//        }
//        return null;
//    }
//
//    /**
//     * Delete a quality control rule by its ID.
//     */
//    public boolean deleteRule(Long id) {
//        if (qualityControlRuleRepository.existsById(id)) {
//            qualityControlRuleRepository.deleteById(id);
//            return true;
//        }
//        return false;
//    }

    @Override
    public Set<String> actionsMapping(QualityControlRule rule) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}