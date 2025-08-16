package com.osm.oilproductionservice.model;

import com.xdev.communicator.models.shared.enums.PartnerCategory;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

/**
 * A SupplierInfo.
 */
@Entity
@Table(name = "supplier_info")
public class SupplierInfo extends BaseEntity {

    private String name;
    private String lastname;
    private String phone;
    private String email;
    private String address;
    private String rib;
    private String bankName;
    private String matriculeFiscal;
    /**
     * Customer category (PREMIUM, STANDARD, BASIC, etc.)
     */
    @Enumerated(EnumType.STRING)
    private PartnerCategory category;

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public PartnerCategory getCategory() {
        return category;
    }

    public void setCategory(PartnerCategory category) {
        this.category = category;
    }
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

    public Float getTotalOliveQuantity() {
        return totalOliveQuantity;
    }

    public void setTotalOliveQuantity(Float totalOliveQuantity) {
        this.totalOliveQuantity = totalOliveQuantity;
    }

    public Float getTotalOilQuantity() {
        return totalOilQuantity;
    }

    public Float getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public Float getTotalUnpaidAmount() {
        return totalUnpaidAmount;
    }

    public Float getTotalDebt() {
        return totalDebt;
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

    // Standard getters and setters for basic fields

    // Calculated totals based on child class values

    /**
     * Sums the olive quantity from all olive deliveries.
     */

    public void setTotalOilQuantity(Float totalOilQuantity) {
        this.totalOilQuantity = totalOilQuantity;
    }

    /**
     * Sums the paid amounts from all oil deliveries.
     * (Assuming olive deliveries do not carry financial fields.)
     */


    public void setTotalPaidAmount(Float totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    /**
     * Sums the unpaid amounts from all oil deliveries.
     */


    public void setTotalUnpaidAmount(Float totalUnpaidAmount) {
        this.totalUnpaidAmount = totalUnpaidAmount;
    }

    /**
     * Calculates total debt (for example, as the difference between unpaid and paid amounts).
     */


    public void setTotalDebt(Float totalDebt) {
        this.totalDebt = totalDebt;
    }
}
