package com.osm.oilproductionservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Olive_Oil_Type {
    OC("oc"),
    OB("ob");

    private String name;

    Olive_Oil_Type(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Olive -> Oil (OC→HC, OB→HB) */

    /** Tolerant JSON factory: accepts "OC"/"OB" and also "HC"/"HB" by mapping them */
    @JsonCreator
    public static Olive_Oil_Type from(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toUpperCase();

        // canonical olive codes
        if ("OC".equalsIgnoreCase(v)) return OC;
        if ("OB".equalsIgnoreCase(v)) return OB;

        // tolerate oil codes by mapping them
        if ("HC".equalsIgnoreCase(v)) return OC; // HC oil -> OC olive
        if ("HB".equalsIgnoreCase(v)) return OB; // HB oil -> OB olive



        throw new IllegalArgumentException("Unknown Olive_Oil_Type: " + raw);
    }

    /** Always serialize as the enum code (OC/OB) */
    @JsonValue
    public String toJson() {
        return name();
    }
}
