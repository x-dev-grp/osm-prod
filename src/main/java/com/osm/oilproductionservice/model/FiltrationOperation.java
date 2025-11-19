package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.dto.FiltrationStatu;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import static org.apache.commons.math3.util.Precision.round;

@Entity
@Table(
        name = "filtration_operation",
        indexes = @Index(name = "ix_filtration_storage_unit", columnList = "storage_unit_id")

)
public class FiltrationOperation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_unit_id", nullable = false)
    private StorageUnit sourceStorageUnit;
    private FiltrationStatu status;

    public FiltrationStatu getStatus() {
        return status;
    }

    public void setStatus(FiltrationStatu status) {
        this.status = status;
    }

    @Column(nullable = false)
    private LocalDateTime operationDate = LocalDateTime.now();

    private Double volumeToFilter = 0.0;
    private Double volumeAfter = 0.0;


    private Double lossVolume = 0.0;
    private Double lossPercent = 0.0;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_unit_id", nullable = false)
    private StorageUnit targetStorageUnit;

    public StorageUnit getTargetStorageUnit() {
        return targetStorageUnit;
    }

    public void setTargetStorageUnit(StorageUnit targetStorageUnit) {
        this.targetStorageUnit = targetStorageUnit;
    }

    public StorageUnit getSourceStorageUnit() { return sourceStorageUnit; }
    public void setSourceStorageUnit(StorageUnit storageUnit) { this.sourceStorageUnit = storageUnit; }

    public LocalDateTime getOperationDate() { return operationDate; }
    public void setOperationDate(LocalDateTime operationDate) { this.operationDate = operationDate; }

    public Double getVolumeToFilter() { return volumeToFilter; }
    public void setVolumeToFilter(Double volumeBefore) { this.volumeToFilter = volumeBefore == null ? null : round(volumeBefore, 3); }

    public Double getVolumeAfter() { return volumeAfter; }
    public void setVolumeAfter(Double volumeAfter) { this.volumeAfter = volumeAfter == null ? null : round(volumeAfter, 3); }

    public Double getLossVolume() { return lossVolume; }
    public void setLossVolume(Double lossVolume) { this.lossVolume = lossVolume == null ? null : round(lossVolume, 3); }

    public Double getLossPercent() { return lossPercent; }
    public void setLossPercent(Double lossPercent) { this.lossPercent = lossPercent == null ? null : round(lossPercent, 3); }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
