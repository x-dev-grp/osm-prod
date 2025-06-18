package com.osm.oilproductionservice.dto;


import com.osm.oilproductionservice.model.MillMachine;
import com.xdev.xdevbase.dtos.BaseDto;

import java.time.LocalDateTime;

public class MillMachineDto extends BaseDto<MillMachine> {

    private String name;
    private String machineType;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private Double capacity;
    private String operatingStatus;
    private Long hoursOperated;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    private String description;

    // Default constructor
    public MillMachineDto() {
    }

    // Parameterized constructor
    public MillMachineDto(String name, String machineType, String manufacturer, String model,
                          String serialNumber, Double capacity, String operatingStatus, Long hoursOperated,
                          LocalDateTime lastMaintenanceDate, LocalDateTime nextMaintenanceDate, String description) {
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

    // Getters and Setters


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