package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.StorageUnit;
import com.xdev.communicator.models.enums.QualityGrades;
import com.xdev.communicator.models.enums.StorageStatus;
import com.xdev.communicator.models.shared.SupplierDto;
import com.xdev.xdevbase.dtos.BaseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class StorageUnitDto extends BaseDto<StorageUnit> {

    private String name;
    private String location;
    private String description;

    private Double maxCapacity = 0.0;
    private Double currentVolume = 0.0;

    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime lastInspectionDate;

    private Double avgCost = 0.0;
    private Double totalCost = 0.0;

    private BaseTypeDto oilVariety; // OIL_VARIETY
    private StorageStatus status = StorageStatus.AVAILABLE;
    private Boolean paidStorage;
    private Boolean filteredOil;
    private Double monthlyRentalPrice;
    private QualityGrades qualityGrade;
    private String lotNumber;
    private LocalDateTime lastFillDate;
    private LocalDateTime lastEmptyDate;
    private SupplierDto supplier;

    public StorageUnitDto() {
    }

    // Optional helper for client-side rendering
    public double getFillPercentage() {
        return maxCapacity != null && maxCapacity > 0 ? (currentVolume / maxCapacity) * 100.0 : 0.0;
    }


}
