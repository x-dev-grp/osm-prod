package com.osm.oilproductionservice.dto;

import com.osm.oilproductionservice.model.Supplier;
import com.xdev.xdevbase.dtos.BaseDto;

/**
 * DTO for {@link Supplier}
 */

public class SupplierDto extends BaseDto<Supplier> {
    private SupplierInfoDto supplierInfo;
    private BaseTypeDto genericSupplierType;


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