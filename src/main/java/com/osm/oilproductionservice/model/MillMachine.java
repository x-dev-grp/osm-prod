package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "mill_machine")
public class MillMachine extends BaseEntity implements Serializable {

    // A descriptive name for the machine (e.g., "Hydraulic Press")
    private String name;

    // Specific type of machine used in the mill (e.g., "Crusher", "Press", "Separator")
    private String machineType;

    // Manufacturer details
    private String manufacturer;
    private String model;
    private String serialNumber;

    // Capacity of the machine (e.g., in kg/hour or liters/hour)
    private Double capacity;

    // Current operational status (e.g., "OPERATIONAL", "MAINTENANCE", "OUT_OF_SERVICE")
    private String operatingStatus;

    // Total operating hours (for scheduling maintenance)
    private Long hoursOperated;
    // Date/time when the machine was last maintained
    private LocalDateTime lastMaintenanceDate;
    // Date/time when the next maintenance is scheduled
    private LocalDateTime nextMaintenanceDate;
    // Additional details or notes about the machine
    @Column(length = 1000)
    private String description;

    public MillMachine() {
    }

    public MillMachine(String name, String machineType, String manufacturer, String model, String serialNumber, Double capacity, String operatingStatus, Long hoursOperated, LocalDateTime lastMaintenanceDate, LocalDateTime nextMaintenanceDate, String description) {
        this.name = name;
        this.machineType = machineType;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.capacity = capacity;
        this.operatingStatus = operatingStatus;
        this.hoursOperated = hoursOperated;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.nextMaintenanceDate = nextMaintenanceDate;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMachineType() {
        return machineType;
    }

    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public String getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public Long getHoursOperated() {
        return hoursOperated;
    }

    public void setHoursOperated(Long hoursOperated) {
        this.hoursOperated = hoursOperated;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
