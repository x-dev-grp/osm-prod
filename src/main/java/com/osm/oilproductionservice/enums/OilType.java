package com.osm.oilproductionservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OilType {
    HB("hb"),
    HC("hc");

    private String name;

    OilType(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Oil -> Olive (HB→OB, HC→OC) */
    public OliveType toOliveType() {
        return this == HC ? OliveType.OC : OliveType.OB;
    }

    /** Tolerant JSON factory: accepts "HB"/"HC" and also "OB"/"OC" by mapping them */
    @JsonCreator
    public static OilType from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toUpperCase();

        // canonical oil codes
        if ("HB".equalsIgnoreCase(v)) return HB;
        if ("HC".equalsIgnoreCase(v)) return HC;

        // tolerate olive codes by mapping them
        if ("OB".equalsIgnoreCase(v)) return HB; // OB olive -> HB oil
        if ("OC".equalsIgnoreCase(v)) return HC; // OC olive -> HC oil


        throw new IllegalArgumentException("Unknown OilType: " + raw);
    }

    /** Always serialize as the enum code (HB/HC) */
    @JsonValue
    public String toJson() {
        return name();
    }
}
