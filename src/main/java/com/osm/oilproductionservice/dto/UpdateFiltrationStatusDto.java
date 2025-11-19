package com.osm.oilproductionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UpdateFiltrationStatusDto {

    @NotNull(message = "Status cannot be null")
    private FiltrationStatu status;

    private Double volumeAfter;

    private Double lossVolume;

    private Double lossPercent;

    private String note;
}