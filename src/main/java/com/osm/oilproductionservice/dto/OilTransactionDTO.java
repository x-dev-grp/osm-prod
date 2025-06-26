package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.enums.TransactionType;
import com.osm.oilproductionservice.model.OilTransaction;
import com.xdev.xdevbase.dtos.BaseDto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OilTransactionDTO extends BaseDto<OilTransaction> {
    private Boolean isDeleted = false;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private StorageUnitDto storageUnitDestination;
    private StorageUnitDto storageUnitSource;
    private String qualityGrade;
    private Double quantityKg;
    private Double unitPrice;
    private Double totalPrice;
    private UUID receptionId;
    private TransactionType transactionType;

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }


    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }


    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public StorageUnitDto getStorageUnitDestination() {
        return storageUnitDestination;
    }

    public void setStorageUnitDestination(StorageUnitDto storageUnitDestination) {
        this.storageUnitDestination = storageUnitDestination;
    }

    public StorageUnitDto getStorageUnitSource() {
        return storageUnitSource;
    }

    public void setStorageUnitSource(StorageUnitDto storageUnitSource) {
        this.storageUnitSource = storageUnitSource;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public UUID getReceptionId() {
        return receptionId;
    }

    public void setReceptionId(UUID receptionId) {
        this.receptionId = receptionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
}
