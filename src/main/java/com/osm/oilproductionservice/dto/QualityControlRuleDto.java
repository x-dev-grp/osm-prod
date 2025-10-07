package com.osm.oilproductionservice.dto;

import com.xdev.communicator.models.enums.RuleType;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.xdev.xdevbase.dtos.BaseDto;

public class QualityControlRuleDto extends BaseDto<QualityControlRule> {

    private String ruleKey; // Technical key for the rule (e.g., "infestation_percentage")
    private Boolean oilQc; // Indicates if the rule applies to oil quality control
    private String ruleName; // User-friendly name for the rule (e.g., "Infestation Percentage")
    private String description; // Description of the rule's purpose
    private Float minValue; // Minimum acceptable value for the rule
    private Float maxValue; // Maximum acceptable value for the rule
    private RuleType ruleType;
    private Boolean booleanValue;
    private String ruleTextValue; // New field for text values
    private String rawStringValue;

    public String getRuleKey() {
        return ruleKey;
    }

    public void setRuleKey(String ruleKey) {
        this.ruleKey = ruleKey;
    }

    public Boolean getOilQc() {
        return oilQc;
    }

    public void setOilQc(Boolean oilQc) {
        this.oilQc = oilQc;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getMinValue() {
        return minValue;
    }

    public void setMinValue(Float minValue) {
        this.minValue = minValue;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Float maxValue) {
        this.maxValue = maxValue;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getRuleTextValue() {
        return ruleTextValue;
    }

    public void setRuleTextValue(String ruleTextValue) {
        this.ruleTextValue = ruleTextValue;
    }

    public String getRawStringValue() {
        return rawStringValue;
    }

    public void setRawStringValue(String rawStringValue) {
        this.rawStringValue = rawStringValue;
    }
}