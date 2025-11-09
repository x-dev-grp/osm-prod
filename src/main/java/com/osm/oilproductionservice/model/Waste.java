package com.osm.oilproductionservice.model;

import com.xdev.communicator.models.enums.Currency;
import com.xdev.communicator.models.enums.PaymentMethod;
import com.xdev.communicator.models.enums.WasteType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

import static org.apache.commons.math3.util.Precision.round;

@Entity
@Table(name = "waste")
public class Waste extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private WasteType type;

    // Defaults & safety
    private Double quantityInKg = 0d;
    private BigDecimal unitPrice = BigDecimal.ZERO;

    // computed & stored for reporting:
    private BigDecimal totalPrice = BigDecimal.ZERO;

    private Instant saleDate;
    private String invoiceNumber;
    private Boolean paid = Boolean.FALSE;
    private Instant paymentDate;

    private Double paidAmount = 0d;
    private Double unpaidAmount = 0d;

    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private String storageLocationCode;
    private UUID customer;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String notes;

    // ==================== Getters / Setters ====================

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
        this.quantityInKg = (quantityInKg == null) ? 0d : round(quantityInKg, 3);
        recalcTotalPrice();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = (unitPrice == null) ? BigDecimal.ZERO : unitPrice;
        recalcTotalPrice();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /**
     * Allows manual override but keeps non-null, scaled value.
     */
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = (totalPrice == null) ? BigDecimal.ZERO : totalPrice.setScale(2, RoundingMode.HALF_UP);
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
        this.paid = (paymentReceived == null) ? Boolean.FALSE : paymentReceived;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = (paidAmount == null) ? 0d : round(paidAmount, 3);
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = (unpaidAmount == null) ? 0d : round(unpaidAmount, 3);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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

    // ==================== Helpers ====================

    private void recalcTotalPrice() {
        // totalPrice = unitPrice * quantityInKg, scaled 2
        BigDecimal qty = BigDecimal.valueOf(this.quantityInKg == null ? 0d : this.quantityInKg);
        this.totalPrice = (this.unitPrice == null ? BigDecimal.ZERO : this.unitPrice).multiply(qty).setScale(2, RoundingMode.HALF_UP);
    }
}
