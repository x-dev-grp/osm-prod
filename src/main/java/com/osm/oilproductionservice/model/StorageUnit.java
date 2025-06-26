package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.enums.StorageStatus;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_unit")
public class StorageUnit extends BaseEntity {

    private String name;
    private String location;
    private String description;

    private Double maxCapacity = 0.0;
    private Double currentVolume = 0.0;

    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime lastInspectionDate;

    private Double avgCost = 0.0;
    private Double totalCost = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilType; // OIL_VARIETY

    @Enumerated(EnumType.STRING)
    private StorageStatus status;

    private LocalDateTime lastFillDate;
    private LocalDateTime lastEmptyDate;

    public StorageUnit() {
    }

    // Constructors, Getters, Setters — all can stay

    public StorageUnit(String name, String location, String description, Double maxCapacity, Double currentVolume, LocalDateTime nextMaintenanceDate, LocalDateTime lastInspectionDate, BaseType oilType, StorageStatus status, LocalDateTime lastFillDate, LocalDateTime lastEmptyDate) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.maxCapacity = maxCapacity;
        this.currentVolume = currentVolume;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.lastInspectionDate = lastInspectionDate;
        this.oilType = oilType;
        this.status = status;
        this.lastFillDate = lastFillDate;
        this.lastEmptyDate = lastEmptyDate;
    }

    // Utility: Get % of fill used (can help for alerts/warnings)
    public double getFillPercentage() {
        return maxCapacity != null && maxCapacity > 0
                ? (currentVolume / maxCapacity) * 100.0
                : 0.0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Double maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public Double getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(Double currentVolume) {
        this.currentVolume = currentVolume;
    }

    public void updateCurrentVolume(Double volume, int direction, Double unitPrice) {
        java.util.function.Function<Double, Double> rd = v -> BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (volume != null) {
            if (direction == 0) {
                this.currentVolume = rd.apply(this.currentVolume - volume);
                this.totalCost = rd.apply(this.totalCost - (volume * this.avgCost));
            } else {
                this.currentVolume = rd.apply(this.currentVolume + volume);
                this.totalCost = rd.apply(this.totalCost + (volume * unitPrice));
            }

            this.avgCost = rd.apply(this.totalCost / this.currentVolume);
        }
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }

    public void setLastInspectionDate(LocalDateTime lastInspectionDate) {
        this.lastInspectionDate = lastInspectionDate;
    }

    public BaseType getOilType() {
        return oilType;
    }

    public void setOilType(BaseType oilType) {
        this.oilType = oilType;
    }

    public StorageStatus getStatus() {
        return status;
    }

    public void setStatus(StorageStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastFillDate() {
        return lastFillDate;
    }

    public void setLastFillDate(LocalDateTime lastFillDate) {
        this.lastFillDate = lastFillDate;
    }

    public LocalDateTime getLastEmptyDate() {
        return lastEmptyDate;
    }

    public void setLastEmptyDate(LocalDateTime lastEmptyDate) {
        this.lastEmptyDate = lastEmptyDate;
    }

    public Double getAvgCost() {
        return avgCost;
    }

    public void setAvgCost(Double avgCost) {
        this.avgCost = avgCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }
}
