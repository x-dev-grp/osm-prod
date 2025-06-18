package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "machine_plan")
public class MachinePlan extends BaseEntity implements Serializable {


    // Link to the mill machine that will process the delivery
    @ManyToOne(fetch = FetchType.LAZY)
    private MillMachine machine;

    // Link to the delivery assigned to this plan
    @ManyToOne(fetch = FetchType.LAZY)
    private UnifiedDelivery delivery;

    // Planned start and end times for processing
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;

    // Status of the plan (e.g., SCHEDULED, IN_PROGRESS, COMPLETED)
    private String status;

    public MachinePlan() {
    }

    public MachinePlan(MillMachine machine, UnifiedDelivery delivery, LocalDateTime plannedStartTime,
                       LocalDateTime plannedEndTime, String status) {
        this.machine = machine;
        this.delivery = delivery;
        this.plannedStartTime = plannedStartTime;
        this.plannedEndTime = plannedEndTime;
        this.status = status;
    }

    // Getters and setters


    public MillMachine getMachine() {
        return machine;
    }

    public void setMachine(MillMachine machine) {
        this.machine = machine;
    }

    public UnifiedDelivery getDelivery() {
        return delivery;
    }

    public void setDelivery(UnifiedDelivery delivery) {
        this.delivery = delivery;
    }

    public LocalDateTime getPlannedStartTime() {
        return plannedStartTime;
    }

    public void setPlannedStartTime(LocalDateTime plannedStartTime) {
        this.plannedStartTime = plannedStartTime;
    }

    public LocalDateTime getPlannedEndTime() {
        return plannedEndTime;
    }

    public void setPlannedEndTime(LocalDateTime plannedEndTime) {
        this.plannedEndTime = plannedEndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}