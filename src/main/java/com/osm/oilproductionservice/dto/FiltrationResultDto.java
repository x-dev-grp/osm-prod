package com.osm.oilproductionservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@Data
public class FiltrationResultDto {



    private String note;


    private Long operationId;
    private UUID sourceId;
    private UUID targetId;
    private double volumeFiltered;
    private String status;
    private LocalDateTime timestamp;






    


}
