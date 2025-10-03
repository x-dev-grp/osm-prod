package com.osm.oilproductionservice.dto;


import com.osm.oilproductionservice.enums.OperationType;
import com.osm.oilproductionservice.enums.TransactionType;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.communicator.models.enums.PaymentMethod;
import com.xdev.communicator.models.enums.TransactionDirection;
import com.xdev.communicator.models.shared.BankAccountDto;
import com.xdev.communicator.models.shared.ExpenseDto;
import com.xdev.communicator.models.shared.SupplierDto;
import com.xdev.xdevbase.dtos.BaseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class FinancialTransactionDto extends BaseDto {
    private TransactionType transactionType;
    private TransactionDirection direction;
    private BigDecimal amount;
    private Currency currency;
    private PaymentMethod paymentMethod;
    private BankAccountDto bankAccount;
    private String checkNumber;
    private String lotNumber;
    private SupplierDto supplier;
     private ExpenseDto expense;
    private String description;
    private String invoiceReference;
    private String receiptReference;
    private LocalDateTime transactionDate;
    private Boolean approved;
    private LocalDateTime approvalDate;
    private String approvedBy;
    private String externalTransactionId;
    private Double paidAmount;
    private Double unpaidAmount;
    private UUID tenantId;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private OperationType operationType;

    @Override
    public String toString() {
        return "FinancialTransactionDto{" +
                "transactionType=" + transactionType +
                ", direction=" + direction +
                ", amount=" + amount +
                ", currency=" + currency +
                ", paymentMethod=" + paymentMethod +
                ", bankAccount=" + bankAccount +
                ", checkNumber='" + checkNumber + '\'' +
                ", lotNumber='" + lotNumber + '\'' +
                ", supplier=" + supplier +
                 ", expense=" + expense +
                ", description='" + description + '\'' +
                ", invoiceReference='" + invoiceReference + '\'' +
                ", receiptReference='" + receiptReference + '\'' +
                ", transactionDate=" + transactionDate +
                ", approved=" + approved +
                ", approvalDate=" + approvalDate +
                ", approvedBy='" + approvedBy + '\'' +
                '}';
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionDirection getDirection() {
        return direction;
    }

    public void setDirection(TransactionDirection direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public  SupplierDto getsupplier() {
        return supplier;
    }

    public void setsupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }



    public ExpenseDto getExpense() {
        return expense;
    }

    public void setExpense(ExpenseDto expense) {
        this.expense = expense;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    public String getReceiptReference() {
        return receiptReference;
    }

    public void setReceiptReference(String receiptReference) {
        this.receiptReference = receiptReference;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
}
