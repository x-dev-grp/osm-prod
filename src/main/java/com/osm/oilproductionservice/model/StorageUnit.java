package com.osm.oilproductionservice.model;

import com.xdev.communicator.models.enums.QualityGrades;
import com.xdev.communicator.models.enums.StorageStatus;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.apache.commons.math3.util.Precision.round;

@Entity
@Table(name = "storage_unit")
public class StorageUnit extends BaseEntity {

    private String name;

    private String lotNumber;
    @Enumerated(EnumType.STRING)
    private QualityGrades qualityGrade;

    private String location;
    private String description;

    private Double maxCapacity = 0.0;
    private Double currentVolume = 0.0;

    private LocalDateTime nextMaintenanceDate;
    private LocalDateTime lastInspectionDate;

    private Double avgCost = 0.0;
    private Double totalCost = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilVariety; // OIL_VARIETY
    @Enumerated(EnumType.STRING)
    private StorageStatus status;


    private LocalDateTime lastFillDate;
    private LocalDateTime lastEmptyDate;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", unique = true)
    private Supplier supplier;
    private Boolean paidStorage  ;
    private Double monthlyRentalPrice  ;

    public double getMonthlyRentalPrice() {
        return monthlyRentalPrice;
    }

    public void setMonthlyRentalPrice(double monthlyRentalPrice) {
        this.monthlyRentalPrice = monthlyRentalPrice;
    }

    public boolean isPaidStorage() {
        return paidStorage;
    }

    public void setPaidStorage(boolean paidStorage) {
        this.paidStorage = paidStorage;
    }


    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public StorageUnit() {
    }

    // Constructors, Getters, Setters — all can stay


    // Utility: Get % of fill used (can help for alerts/warnings)
    public double getFillPercentage() {
        return maxCapacity != null && maxCapacity > 0
                ? (currentVolume / maxCapacity) * 100.0
                : 0.0;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Boolean getPaidStorage() {
        return paidStorage;
    }

    public void setPaidStorage(Boolean paidStorage) {
        this.paidStorage = paidStorage;
    }

    public void setMonthlyRentalPrice(Double monthlyRentalPrice) {
        this.monthlyRentalPrice = monthlyRentalPrice == null ? null :  round(monthlyRentalPrice, 3);
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
        this.maxCapacity = maxCapacity == null ? null : round(maxCapacity, 3);
    }

    public Double getCurrentVolume() {
        return currentVolume;
    }

    public void setCurrentVolume(Double currentVolume) {
        this.currentVolume = currentVolume == null ? null : round(currentVolume, 3);
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

            if (this.currentVolume == 0) {
                this.avgCost = 0.0;
            } else {
                this.avgCost = rd.apply(this.totalCost / this.currentVolume);
            }
        }
    }
    public void updateDeletedCurrentVolume(Double volume, int direction, Double unitPrice) {
        java.util.function.Function<Double, Double> rd = v -> BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (volume != null) {
            if (direction == 0) {
                this.currentVolume = rd.apply(this.currentVolume + volume);
                this.totalCost = rd.apply(this.totalCost + (volume * unitPrice));
            } else {
                this.currentVolume = rd.apply(this.currentVolume - volume);
                this.totalCost = rd.apply(this.totalCost - (volume * this.avgCost));
            }

            if (this.currentVolume == 0) {
                this.avgCost = 0.0;
            } else {
                this.avgCost = rd.apply(this.totalCost / this.currentVolume);
            }
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

    public BaseType getOilVariety() {
        return oilVariety;
    }

    public void setOilVariety(BaseType oilVariety) {
        this.oilVariety = oilVariety;
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
        this.avgCost = avgCost == null ? null : round(avgCost, 3);
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost == null ? null : round(totalCost, 3);
    }
}
