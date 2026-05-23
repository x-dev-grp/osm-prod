package com.osm.oilproductionservice.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GenealogyDto {
    private UUID traceabilityLotId;
    private String traceabilitySourceType;
    private UUID rootReceptionId;
    private UUID storageUnitId;
    private String lotNumber;
    private String storageUnitName;
    private Map<String, String> filteredQualityControls;

    private List<FiltrationStepDto> filtrations = new ArrayList<>();
    private List<RootSourceDto> rootSources = new ArrayList<>();
    /** Olive → oil reception → stock entry on the anchor storage unit (runtime). */
    private List<IntakeStepDto> intakeChain = new ArrayList<>();
}
