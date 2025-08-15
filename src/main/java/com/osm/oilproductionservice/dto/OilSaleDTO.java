package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.enums.QualityGrades;
import com.osm.oilproductionservice.enums.SaleStatus;
import com.osm.oilproductionservice.model.Customer;
import com.osm.oilproductionservice.model.OilSale;
import com.xdev.communicator.models.shared.dto.CustomerDto;
import com.xdev.communicator.models.shared.enums.TypeCategory;
import com.xdev.xdevbase.dtos.BaseDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO for OilSale entity
 */
public class OilSaleDTO extends BaseDto<OilSale> {

    // ==================== CORE SALE FIELDS ====================

    private String invoiceNumber;
    private SaleStatus status;
    private LocalDateTime saleDate;

    // ==================== CUSTOMER INFORMATION ====================

    private CustomerDto customer;

    // ==================== SUPPLIER INFORMATION ====================

    private SupplierDto supplier;

    // ==================== STORAGE UNIT INFORMATION ====================

    private UUID storageUnit;
    private BaseTypeDto oilType;

    // ==================== QUANTITY & PRICING ====================

    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String currency;

    // ==================== PAYMENT INFORMATION ====================

    private String paymentMethod;
    private String bankAccount;
    private String checkNumber;
    private String externalTransactionId;

    // ==================== ADDITIONAL INFORMATION ====================

    private String description;
    private LocalDateTime deliveryDate;
    private String deliveryAddress;
    private String deliveryNotes;
    private boolean paid = false;
    private QualityGrades qualityGrade;
    private Double paidAmount;
    private Double unpaidAmount;


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

    public CustomerDto getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDto customer) {
        this.customer = customer;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public UUID getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(UUID storageUnit) {
        this.storageUnit = storageUnit;
    }

    public BaseTypeDto getOilType() {
        return oilType;
    }

    public void setOilType(BaseTypeDto oilType) {
        this.oilType = oilType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Double getPaiedAmount() {
        return paidAmount;
    }

    public void setPaiedAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }
}