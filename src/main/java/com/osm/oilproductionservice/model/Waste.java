package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.enums.WasteType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "waste")
public class Waste extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private WasteType type;

    private Double quantityInKg;
    private BigDecimal unitPrice;

    // computed & stored for reporting:
    private BigDecimal totalPrice;

    private Instant saleDate;
    private String invoiceNumber;
    private Boolean paid;
    private Instant paymentDate;


    private String storageLocationCode;
    private UUID customer;
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    private String notes;

    public WasteType getType() {
        return type;
    }

    public void setType(WasteType type) {
        this.type = type;
    }

    public Double getQuantityInKg() {
        return quantityInKg;
    }

    public void setQuantityInKg(Double quantityInKg) {
        this.quantityInKg = quantityInKg;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Instant getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Instant saleDate) {
        this.saleDate = saleDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paymentReceived) {
        this.paid = paymentReceived;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStorageLocationCode() {
        return storageLocationCode;
    }

    public void setStorageLocationCode(String storageLocationCode) {
        this.storageLocationCode = storageLocationCode;
    }

    public UUID getCustomer() {
        return customer;
    }

    public void setCustomer(UUID customer) {
        this.customer = customer;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


}
