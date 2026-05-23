package com.osm.oilproductionservice.dto;

import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class FiltrationStepDto {
    private UUID operationId;
    private String sourceLotNumber;
    private String targetLotNumber;
    private Double volumeFiltered;
    private String timestamp;
    
    private UUID sourceStorageUnitId;
    private String sourceStorageUnitName;
    private Map<String, String> qualityControls;
    /** Intake chain for the source tank used in this filtration step. */
    private java.util.List<IntakeStepDto> sourceIntakeChain = new java.util.ArrayList<>();
}
