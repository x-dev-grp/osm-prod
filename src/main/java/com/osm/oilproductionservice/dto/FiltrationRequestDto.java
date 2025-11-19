package com.osm.oilproductionservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Data
public class FiltrationRequestDto {

    private UUID source;
    private UUID target;
    private Double volumeToFilter;
    private String note;




}

