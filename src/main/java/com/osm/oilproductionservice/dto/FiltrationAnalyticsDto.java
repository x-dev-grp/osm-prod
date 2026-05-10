package com.osm.oilproductionservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO retourné par l'endpoint analytics/filtration de osm-prod.
 * Structure identique à FiltrationReportDto dans osm-cond pour compatibilité Feign.
 */
@Data
public class FiltrationAnalyticsDto {
    private String operationId;
    private LocalDateTime operationDate;
    private BigDecimal inputVolume;
    private BigDecimal outputVolume;
    private BigDecimal lossVolume;
    private BigDecimal efficiencyRate;
}
