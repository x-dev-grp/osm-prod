package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.enums.TransactionType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
public class OilTransaction extends BaseEntity implements Serializable {

    private TransactionType transactionType;

    /**
     * Tank / cistern affected by the transaction
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private StorageUnit storageUnitDestination;
    @ManyToOne(fetch = FetchType.LAZY)
    private StorageUnit storageUnitSource;

    private String qualityGrade;
    /**
     * Net quantity moved, in kilograms (positive for IN, negative for OUT)
     */
    private Double quantityKg;
    private Double unitPrice;
    private double totalPrice;
    private UUID receptionId;


    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public StorageUnit getStorageUnitDestination() {
        return storageUnitDestination;
    }

    public void setStorageUnitDestination(StorageUnit storageUnitDestination) {
        this.storageUnitDestination = storageUnitDestination;
    }

    public StorageUnit getStorageUnitSource() {
        return storageUnitSource;
    }

    public void setStorageUnitSource(StorageUnit storageUnitSource) {
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

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice() {
        this.totalPrice = unitPrice * quantityKg;
    }

    public UUID getReceptionId() {
        return receptionId;
    }

    public void setReceptionId(UUID receptionId) {
        this.receptionId = receptionId;
    }

}
