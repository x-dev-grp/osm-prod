package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.Transporter;
import com.xdev.xdevbase.dtos.BaseDto;

public class TransporterDTO extends BaseDto<Transporter> {

    private String name;
    private String contactNumber;
    private String email;
    private String licenseNumber;
    private String address;

    public TransporterDTO(String name, String contactNumber, String email, String licenseNumber, String address) {
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
}
