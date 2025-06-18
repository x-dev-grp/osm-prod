package com.osm.oilproductionservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlanningSaveRequest {
    private List<MillPlanDTO> mills;
    private List<GlobalLotDto> globalLots;

    public PlanningSaveRequest(List<MillPlanDTO> mills, List<GlobalLotDto> globalLots) {
        this.mills = mills;
        this.globalLots = globalLots;
    }

    public List<MillPlanDTO> getMills() {
        return mills;
    }

    public void setMills(List<MillPlanDTO> mills) {
        this.mills = mills;
    }

    public List<GlobalLotDto> getGlobalLots() {
        return globalLots;
    }

    public void setGlobalLots(List<GlobalLotDto> globalLots) {
        this.globalLots = globalLots;
    }
}