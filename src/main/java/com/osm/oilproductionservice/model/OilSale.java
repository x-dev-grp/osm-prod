package com.osm.oilproductionservice.model;


import com.osm.oilproductionservice.enums.QualityGrades;
import com.osm.oilproductionservice.enums.SaleStatus;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Oil Sale entity for managing oil sales transactions
 */
@Entity
@Table(name = "oil_sales")
public class OilSale extends BaseEntity implements Serializable {
    private Double paiedAmount;
    private Double unpaiedAmount;

    private boolean paid = false;
    // ==================== CORE SALE FIELDS ====================

    /**
     * Unique invoice number for the sale
     */
    @Column(unique = true,  length = 50)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    private QualityGrades qualityGrade;

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    /**
     * Sale status (PENDING, CONFIRMED, DELIVERED, CANCELLED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status = SaleStatus.PENDING;

    /**
     * Sale date
     */
    @Column(nullable = false)
    private LocalDateTime saleDate;

    // ==================== CUSTOMER INFORMATION ====================

    /**
     * Customer who made the purchase
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // ==================== SUPPLIER INFORMATION ====================

    /**
     * Supplier who is selling the oil
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // ==================== STORAGE UNIT INFORMATION ====================

    /**
     * Storage unit from which oil is sold
     */
      private UUID storageUnit;

    /**
     * Oil type being sold
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oil_type_id")
    private BaseType oilType;

    // ==================== QUANTITY & PRICING ====================

    /**
     * Quantity sold in liters
     */
    @Column( precision = 10, scale = 2)
    private BigDecimal quantity;

    /**
     * Unit price per liter
     */
    @Column( precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Total amount for the sale
     */
    @Column( precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Currency used for the transaction
     */
    @Column(length = 3)
    private String currency= "TND";

    // ==================== PAYMENT INFORMATION ====================

    /**
     * Payment method used
     */
    @Column(length = 50)
    private String paymentMethod;

    /**
     * Bank account used (if applicable)
     */
    @Column(length = 100)
    private String bankAccount;

    /**
     * Check number (if payment method is CHECK)
     */
    @Column(length = 50)
    private String checkNumber;

    /**
     * External transaction ID (for bank transfers, etc.)
     */
    @Column(length = 100)
    private String externalTransactionId;

    // ==================== ADDITIONAL INFORMATION ====================

    /**
     * Description or notes about the sale
     */
    @Column(length = 1000)
    private String description;

    /**
     * Delivery date (when oil was/will be delivered)
     */
    private LocalDateTime deliveryDate;

    /**
     * Delivery address
     */
    @Column(length = 500)
    private String deliveryAddress;

    /**
     * Delivery notes
     */
    @Column(length = 1000)
    private String deliveryNotes;

    // ==================== CONSTRUCTORS ====================

    public Double getPaiedAmount() {
        return paiedAmount;
    }

    public void setPaiedAmount(Double paiedAmount) {
        this.paiedAmount = paiedAmount;
    }

    public Double getUnpaiedAmount() {
        return unpaiedAmount;
    }

    public void setUnpaiedAmount(Double unpaiedAmount) {
        this.unpaiedAmount = unpaiedAmount;
    }

    public OilSale() {
        super();
    }

    // ==================== GETTERS AND SETTERS ====================

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public SaleStatus getStatus() {
        return status;
    }

    public void setStatus(SaleStatus status) {
        this.status = status;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public UUID getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(UUID storageUnit) {
        this.storageUnit = storageUnit;
    }

    public BaseType getOilType() {
        return oilType;
    }

    public void setOilType(BaseType oilType) {
        this.oilType = oilType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        calculateTotalAmount();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotalAmount();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public void setDeliveryNotes(String deliveryNotes) {
        this.deliveryNotes = deliveryNotes;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Calculate the total amount based on quantity and unit price
     */
    private void calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            this.totalAmount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    /**
     * Check if the sale is confirmed
     */
    public boolean isConfirmed() {
        return SaleStatus.CONFIRMED.equals(status);
    }

    /**
     * Check if the sale is delivered
     */
    public boolean isDelivered() {
        return SaleStatus.DELIVERED.equals(status);
    }

    /**
     * Check if the sale is cancelled
     */
    public boolean isCancelled() {
        return SaleStatus.CANCELLED.equals(status);
    }

    /**
     * Check if the sale is pending
     */
    public boolean isPending() {
        return SaleStatus.PENDING.equals(status);
    }

    @Override
    public String toString() {
        return "OilSale{" +
                "id=" + getId() +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", status=" + status +
                ", saleDate=" + saleDate +
                ", customer=" + (customer != null ? customer.getCustomerName() : "null") +
                 ", oilType=" + (oilType != null ? oilType.getName() : "null") +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", currency='" + currency + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
} 