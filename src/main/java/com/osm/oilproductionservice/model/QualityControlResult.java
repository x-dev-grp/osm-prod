package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "quality_control_result")
public class QualityControlResult extends BaseEntity implements Serializable {

    // The identifier of the quality rule (e.g., could be a foreign key from a rule table or a code)
    @ManyToOne(fetch = FetchType.LAZY)
    private QualityControlRule rule;

    // The value measured by the quality controller for this rule
    @Column(name = "measured_value", nullable = false)
    private String measuredValue;

    // Many-to-One relationship: each result is associated with one UnifiedDelivery.
    @ManyToOne(fetch = FetchType.LAZY)
    private UnifiedDelivery delivery;

    @Column(name = "filtration_operation_id")
    private UUID filtrationOperationId;

    @Column(name = "traceability_lot_id")
    private UUID traceabilityLotId;

    // Constructors
    public QualityControlResult() {
    }

    public QualityControlResult(QualityControlRule rule, String measuredValue, UnifiedDelivery delivery) {
        this.rule = rule;
        this.measuredValue = measuredValue;
        this.delivery = delivery;
    }


    public QualityControlRule getRule() {
        return rule;
    }

    public void setRule(QualityControlRule ruleId) {
        this.rule = ruleId;
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(String measuredValue) {
        this.measuredValue = measuredValue;
    }

    public UnifiedDelivery getDelivery() {
        return delivery;
    }

    public void setDelivery(UnifiedDelivery delivery) {
        this.delivery = delivery;
    }

    public UUID getFiltrationOperationId() {
        return filtrationOperationId;
    }

    public void setFiltrationOperationId(UUID filtrationOperationId) {
        this.filtrationOperationId = filtrationOperationId;
    }

    public UUID getTraceabilityLotId() {
        return traceabilityLotId;
    }

    public void setTraceabilityLotId(UUID traceabilityLotId) {
        this.traceabilityLotId = traceabilityLotId;
    }
}
