package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.SupplierInfo;
import com.xdev.communicator.models.shared.enums.PartnerCategory;
import com.xdev.xdevbase.dtos.BaseDto;

/**
 * DTO for {@link SupplierInfo}
 */

public class SupplierInfoDto extends BaseDto<SupplierInfo> {
    private String name;
    private String lastname;
    private String phone;
    private String email;
    private String address;
    private BaseTypeDto region;
    private String rib;
    private String bankName;

    private String matriculeFiscal;
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


    public BaseTypeDto getRegion() {
        return region;
    }

    public void setRegion(BaseTypeDto region) {
        this.region = region;
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
}