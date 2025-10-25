package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.OilContainerSale;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.communicator.models.enums.PaymentMethod;
import com.xdev.communicator.models.enums.QualityGrades;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the FE payload. We only *use* ids from nested objects.
 * - supplier?.id (optional)
 * - storageUnit.id (required)
 * - containerSales: [{ id, count }]
 * <p>
 * Totals are recalculated on the server (we ignore FE totalAmount/unpaidAmount).
 */
public class OilSaleCreateRequest {

    private String id; // ignored on create

    // supplier is optional; we only read supplier.id if present
    private String supplier;

    @NotNull
    private String storageUnit;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal unitPrice;

    @NotNull
    private Currency currency;        // e.g., "TND"

    @NotNull
    private PaymentMethod paymentMethod;   // e.g., "CASH", "BANK_TRANSFER"

    @NotNull
    private LocalDateTime saleDate;

    @NotNull
    private QualityGrades qualityGrade;    // e.g., "EXTRA_VIRGIN"

    private String invoiceNumber;
    private String description;

    // FE may send; server recomputes.
    private BigDecimal totalAmount;


    private Double paidAmount;
    private BigDecimal unpaidAmount;
    private String status;
    private String oilTransactionUUID;
    private List<OilContainerSale> containerSales;

    public List<OilContainerSale> getContainerSales() {
        return containerSales;
    }

    public void setContainerSales(List<OilContainerSale> containerSales) {
        this.containerSales = containerSales;
    }

    public String getOilTransactionUUID() {
        return oilTransactionUUID;
    }

    public void setOilTransactionUUID(String oilTransactionUUID) {
        this.oilTransactionUUID = oilTransactionUUID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public QualityGrades getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(QualityGrades qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(String storageUnit) {
        this.storageUnit = storageUnit;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
