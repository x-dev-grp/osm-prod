package com.osm.oilproductionservice.model;

 import com.xdev.communicator.models.shared.enums.CustomerCategory;
 import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

/**
 * Customer entity for managing customer information and financial relationships
 */
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity implements Serializable {

    // ==================== CORE CUSTOMER FIELDS ====================

    /**
     * Customer code/identifier
     */
    @Column(unique = true,  length = 50)
    private String matriculeFiscal;
    @Column(unique = true,  length = 8)
    private String numCIN;

    /**
     * Customer name
     */
    @Column( length = 200)
    private String customerName;
    @Column( length = 200)
    private String customerLastName;


    // ==================== CONTACT INFORMATION ====================

    /**
     * Primary contact person
     */
    @Column(length = 100)
    private String contactPerson;

    /**
     * Email address
     */
    @Column(length = 100)
    private String email;

    /**
     * Phone number
     */
    @Column(length = 20)
    private String phone;

    /**
     * Mobile number
     */
    @Column(length = 20)
    private String mobile;

    /**
     * Fax number
     */
    @Column(length = 20)
    private String fax;

    // ==================== ADDRESS INFORMATION ====================

    /**
     * Address line 1
     */
    @Column(length = 200)
    private String address;


    /**
     * Postal code
     */
    @Column(length = 20)
    private String postalCode;

    /**
     * Country
     */
    @Column(length = 100)
    private String country;

    // ==================== BUSINESS INFORMATION ====================


    /**
     * Customer category (PREMIUM, STANDARD, BASIC, etc.)
     */
    @Enumerated(EnumType.STRING)
    private CustomerCategory category;


    // ==================== METADATA FIELDS ====================

    /**
     * Customer notes
     */
    @Column(length = 1000)
    private String notes;


    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public String getNumCIN() {
        return numCIN;
    }

    public void setNumCIN(String numCIN) {
        this.numCIN = numCIN;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }


    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public CustomerCategory getCategory() {
        return category;
    }

    public void setCategory(CustomerCategory category) {
        this.category = category;
    }



    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
