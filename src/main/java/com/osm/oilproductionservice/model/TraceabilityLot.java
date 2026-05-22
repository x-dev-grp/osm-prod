package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "traceability_lot")
@Getter
@Setter
@Audited
public class TraceabilityLot extends BaseEntity {

    @Column(name = "lot_number", nullable = false, length = 120)
    private String lotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private TraceabilitySourceType sourceType;

    @Column(name = "source_entity_id")
    private UUID sourceEntityId;

    @Column(name = "root_reception_id")
    private UUID rootReceptionId;

    @Column(name = "parent_lot_id")
    private UUID parentLotId;

    @Column(name = "storage_unit_id")
    private UUID storageUnitId;

    @Column(name = "filtration_operation_id")
    private UUID filtrationOperationId;

    @Column(name = "quality_grade")
    private String qualityGrade;

    @Column(name = "oil_variety")
    private String oilVariety;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Lob
    @Column(name = "source_snapshot_json", columnDefinition = "TEXT")
    private String sourceSnapshotJson;
}
