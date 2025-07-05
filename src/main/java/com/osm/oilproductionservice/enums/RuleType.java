package com.osm.oilproductionservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RuleType {
    NUMERIC,
    BOOLEAN,
    STRING;

    @JsonCreator
    public static RuleType fromString(String key) {
        return key == null ? null : RuleType.valueOf(key.trim().toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
