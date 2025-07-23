package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

/**
 * A Supplier.
 */
@Entity
@Table(name = "supplier")
public class Supplier extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "supplier_info_id", nullable = false)
    private SupplierInfo supplierInfo;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "generic_suppliertype_id", nullable = false)
    private BaseType genericSupplierType;
    private Boolean hasStorage=false;
    @OneToOne(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private StorageUnit storageUnit;

    public Boolean getHasStorage() {
        return hasStorage;
    }

    public void setHasStorage(Boolean hasStorage) {
        this.hasStorage = hasStorage;
    }

    public StorageUnit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    public SupplierInfo getSupplierInfo() {
        return supplierInfo;
    }

    public void setSupplierInfo(SupplierInfo supplierInfo) {
        this.supplierInfo = supplierInfo;
    }

    public BaseType getGenericSupplierType() {
        return genericSupplierType;
    }

    public void setGenericSupplierType(BaseType genericSupplierType) {
        this.genericSupplierType = genericSupplierType;
    }


}
