package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.FiltrationAnalyticsDto;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.osm.oilproductionservice.repository.FiltrationOperationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service d'analytique pour osm-prod.
 * Expose les données de filtrage pour le module Rapports & Analyses.
 */
@Service
@RequiredArgsConstructor
public class ProdAnalyticsService {

    private final FiltrationOperationRepo filtrationRepo;

    public List<FiltrationAnalyticsDto> getFiltrationReport(LocalDateTime start, LocalDateTime end) {
        return filtrationRepo.findAllByIsDeletedFalseOrderByOperationDateDesc().stream()
                .filter(f -> start == null || (f.getOperationDate() != null && !f.getOperationDate().isBefore(start)))
                .filter(f -> end == null || (f.getOperationDate() != null && !f.getOperationDate().isAfter(end)))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private FiltrationAnalyticsDto mapToDto(FiltrationOperation f) {
        FiltrationAnalyticsDto dto = new FiltrationAnalyticsDto();
        dto.setOperationId(f.getId() != null ? f.getId().toString() : "N/A");
        dto.setOperationDate(f.getOperationDate());
        dto.setInputVolume(f.getVolumeToFilter() != null
                ? BigDecimal.valueOf(f.getVolumeToFilter()).setScale(3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        dto.setOutputVolume(f.getVolumeAfter() != null
                ? BigDecimal.valueOf(f.getVolumeAfter()).setScale(3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        dto.setLossVolume(f.getLossVolume() != null
                ? BigDecimal.valueOf(f.getLossVolume()).setScale(3, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        // Efficiency = 100 - lossPercent
        double efficiency = (f.getLossPercent() != null) ? (100.0 - f.getLossPercent()) : 100.0;
        dto.setEfficiencyRate(BigDecimal.valueOf(efficiency).setScale(2, RoundingMode.HALF_UP));
        return dto;
    }
}
