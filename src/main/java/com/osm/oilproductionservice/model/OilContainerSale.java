package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.math.BigDecimal;
@Entity
public class OilContainerSale extends BaseEntity implements Serializable {
    // ✅ Owning side with the join column; the field name is "oilSale"
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oil_sale_id", nullable = false)
    private OilSale oilSale;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private OilContainer container;

    private Integer count;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    // getters/setters
    public OilSale getOilSale() {
        return oilSale;
    }

    public void setOilSale(OilSale oilSale) {
        this.oilSale = oilSale;
    }

    public OilContainer getContainer() {
        return container;
    }

    public void setContainer(OilContainer container) {
        this.container = container;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}