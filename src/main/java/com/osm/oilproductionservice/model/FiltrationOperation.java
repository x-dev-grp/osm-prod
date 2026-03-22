package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.dto.FiltrationStatus;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static org.apache.commons.math3.util.Precision.round;

@Getter
@Entity
@Table(
        name = "filtration_operation",
        indexes = {
                @Index(name = "ix_filtration_source_unit", columnList = "source_storage_unit_id"),
                @Index(name = "ix_filtration_target_unit", columnList = "target_storage_unit_id")
        }
)
public class FiltrationOperation extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_storage_unit_id", nullable = false)
    private StorageUnit sourceStorageUnit;

    // Getters and setters...
    @Setter
    @Enumerated(EnumType.STRING)  // Added this if FiltrationStatus is an enum
    private FiltrationStatus status;

    @Setter
    @Column(nullable = false)
    private LocalDateTime operationDate = LocalDateTime.now();

    private Double volumeToFilter = 0.0;
    private Double volumeAfter = 0.0;

    private Double lossVolume = 0.0;
    private Double lossPercent = 0.0;

    @Setter
    private String note;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_storage_unit_id", nullable = false)
    private StorageUnit targetStorageUnit;

    public void setVolumeToFilter(Double volumeBefore) { this.volumeToFilter = volumeBefore == null ? null : round(volumeBefore, 3); }

    public void setVolumeAfter(Double volumeAfter) { this.volumeAfter = volumeAfter == null ? null : round(volumeAfter, 3); }

    public void setLossVolume(Double lossVolume) { this.lossVolume = lossVolume == null ? null : round(lossVolume, 3); }

    public void setLossPercent(Double lossPercent) { this.lossPercent = lossPercent == null ? null : round(lossPercent, 3); }

}
