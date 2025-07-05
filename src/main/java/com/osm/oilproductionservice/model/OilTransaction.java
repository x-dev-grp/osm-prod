package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.enums.TransactionType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;

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

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilType;
    private TransactionState transactionState = TransactionState.COMPLETED;
    /**
     * Net quantity moved, in kilograms (positive for IN, negative for OUT)
     */
    private Double quantityKg;
    private Double unitPrice;
    private double totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    private UnifiedDelivery reception;

    public BaseType getOilType() {
        return oilType;
    }

    public void setOilType(BaseType oilType) {
        this.oilType = oilType;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

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

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalPrice() {
        this.totalPrice = unitPrice * quantityKg;
    }

    public UnifiedDelivery getReception() {
        return reception;
    }

    public void setReception(UnifiedDelivery receptionId) {
        this.reception = receptionId;
    }

}
