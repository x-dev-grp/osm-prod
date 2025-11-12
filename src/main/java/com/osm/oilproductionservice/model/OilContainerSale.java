package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class OilContainerSale extends BaseEntity implements Serializable {

    // ✅ Owning side with the join column; the field name is "oilSale"
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oil_sale_id", nullable = false)
    private OilSale oilSale;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private OilContainer container;

    // Defaults
    private Integer count = 0;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal lineTotal = BigDecimal.ZERO;

    // ==================== Getters / Setters ====================

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
        this.count = (count == null) ? 0 : count;
        recalcLineTotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = (unitPrice == null) ? BigDecimal.ZERO : unitPrice;
        recalcLineTotal();
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        // allow manual override but keep non-null for safety
        this.lineTotal = (lineTotal == null) ? BigDecimal.ZERO : lineTotal.setScale(2, RoundingMode.HALF_UP);
    }

    // ==================== Helpers ====================

    private void recalcLineTotal() {
        // lineTotal = unitPrice * count, 2 decimals
        BigDecimal qty = BigDecimal.valueOf(this.count == null ? 0 : this.count);
        this.lineTotal = (this.unitPrice == null ? BigDecimal.ZERO : this.unitPrice)
                .multiply(qty)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
