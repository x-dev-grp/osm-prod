package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

/**
 * A Supplier.
 */
@Entity
@Table(name = "supplier")
public class Supplier extends BaseEntity {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "generic_suppliertype_id", nullable = false)
    private BaseType genericSupplierType;
    private Boolean hasStorage = false;
    private String name;
    private String lastname;
    private String phone;
    private String email;
    private String address;
    private String rib;
    private String bankName;
    private String matriculeFiscal;

    public StorageUnit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    @OneToOne(mappedBy = "supplier",fetch = FetchType.LAZY)
    private StorageUnit storageUnit;
    @ManyToOne(fetch = FetchType.EAGER)
    private BaseType region;
    // Calculated fields (not persisted)
    @Transient
    private Float totalOliveQuantity;
    @Transient
    private Float totalOilQuantity;
    @Transient
    private Float totalPaidAmount;
    @Transient
    private Float totalUnpaidAmount;
    @Transient
    private Float totalDebt;

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public Float getTotalOliveQuantity() {
        return totalOliveQuantity;
    }

    public void setTotalOliveQuantity(Float totalOliveQuantity) {
        this.totalOliveQuantity = totalOliveQuantity;
    }

    public Float getTotalOilQuantity() {
        return totalOilQuantity;
    }

    /**
     * Sums the olive quantity from all olive deliveries.
     */

    public void setTotalOilQuantity(Float totalOilQuantity) {
        this.totalOilQuantity = totalOilQuantity;
    }

    public Float getTotalPaidAmount() {
        return totalPaidAmount;
    }

    /**
     * Sums the paid amounts from all oil deliveries.
     * (Assuming olive deliveries do not carry financial fields.)
     */


    public void setTotalPaidAmount(Float totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public Float getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    /**
     * Sums the unpaid amounts from all oil deliveries.
     */


    public void setTotalUnpaidAmount(Float totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    public Float getTotalDebt() {
        return totalDebt;
    }

    /**
     * Calculates total debt (for example, as the difference between unpaid and paid amounts).
     */


    public void setTotalDebt(Float totalDebt) {
        this.totalDebt = totalDebt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    // Standard getters and setters for basic fields

    // Calculated totals based on child class values

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public BaseType getRegion() {
        return region;
    }

    public void setRegion(BaseType region) {
        this.region = region;
    }

    public BaseType getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseType genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }


}
