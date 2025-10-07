package com.osm.oilproductionservice.model;

import com.xdev.communicator.models.enums.Olive_Oil_Type;
import com.xdev.communicator.models.enums.TransactionState;
import com.xdev.communicator.models.enums.TransactionType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.apache.commons.math3.util.Precision.round;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "oil_type")
    private Olive_Oil_Type oilType;
    private TransactionState transactionState = TransactionState.COMPLETED;
    /**
     * Net quantity moved, in kilograms (positive for IN, negative for OUT)
     */
    private Double quantityKg;
    private Double unitPrice;
    private double totalPrice;

    public Olive_Oil_Type getOilType() {
        return oilType;
    }

    public void setOilType(Olive_Oil_Type oilType) {
        this.oilType = oilType;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private UnifiedDelivery reception;

    private UUID oilSaleId;

    public UUID getOilSaleId() {
        return oilSaleId;
    }

    public void setOilSaleId(UUID oilSaleId) {
        this.oilSaleId = oilSaleId;
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
        this.quantityKg = quantityKg == null ? null :  round(quantityKg, 3);
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice == null ? null : round(unitPrice, 3);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalPrice() {

        if (unitPrice == null || quantityKg == null) {
            this.totalPrice = 0.0;
            return;
        }
        this.totalPrice = (BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantityKg)).setScale(2, RoundingMode.HALF_UP)).doubleValue();
        }

    public UnifiedDelivery getReception() {
        return reception;
    }

    public void setReception(UnifiedDelivery receptionId) {
        this.reception = receptionId;
    }

}
