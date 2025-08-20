package com.osm.oilproductionservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OliveType {
    OC("oc"),
    OB("ob");

    private String name;

    OliveType(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Olive -> Oil (OC→HC, OB→HB) */
    public OilType toOilType() {
        return this == OC ? OilType.HC : OilType.HB;
    }

    /** Tolerant JSON factory: accepts "OC"/"OB" and also "HC"/"HB" by mapping them */
    @JsonCreator
    public static OliveType from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toUpperCase();

        // canonical olive codes
        if ("OC".equalsIgnoreCase(v)) return OC;
        if ("OB".equalsIgnoreCase(v)) return OB;

        // tolerate oil codes by mapping them
        if ("HC".equalsIgnoreCase(v)) return OC; // HC oil -> OC olive
        if ("HB".equalsIgnoreCase(v)) return OB; // HB oil -> OB olive



        throw new IllegalArgumentException("Unknown OliveType: " + raw);
    }

    /** Always serialize as the enum code (OC/OB) */
    @JsonValue
    public String toJson() {
        return name();
    }
}
