package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.xdev.xdevbase.dtos.BaseDto;
import jakarta.persistence.OneToOne;

/**
 * DTO for {@link Supplier}
 */

public class SupplierDto extends BaseDto<Supplier> {
    private SupplierInfoDto supplierInfo;
    private BaseTypeDto genericSupplierType;
    private Boolean hasStorage;

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }



    public SupplierInfoDto getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(SupplierInfoDto supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public BaseTypeDto getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseTypeDto genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }
}