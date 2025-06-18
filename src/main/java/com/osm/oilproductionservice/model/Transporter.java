package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * A Transporter represents a company or individual responsible for the transportation of goods.
 */
@Entity
@Table(name = "transporter")
public class Transporter extends BaseEntity implements


        Serializable {

    private String name;
    private String contactNumber;
    private String email;
    private String licenseNumber;
    private String address;
    @ManyToOne(optional = false)
    private UnifiedDelivery deliveries;

    public Transporter() {
    }

    // Getters and setters

    public Transporter(String name, String contactNumber, String email, String licenseNumber, String address) {
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UnifiedDelivery getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(UnifiedDelivery deliveries) {
        this.deliveries = deliveries;
    }
}
