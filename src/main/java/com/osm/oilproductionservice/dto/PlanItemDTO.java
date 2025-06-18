package com.osm.oilproductionservice.dto;

import lombok.Data;

@Data
public class PlanItemDTO {
    private String type; // "LOT" or "GLOBAL_LOT"
    private String id; // lotNumber for LOT, globalLotNumber for GLOBAL_LOT
    private UnifiedDeliveryDTO lot; // Used in getPlanning response
}