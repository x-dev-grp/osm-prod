package com.osm.oilproductionservice.dto;

import lombok.Data;
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
}
