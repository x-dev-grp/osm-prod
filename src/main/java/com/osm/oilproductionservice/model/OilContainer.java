package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
public class OilContainer extends BaseEntity {

    private String name;
    private String description;

    // Defaults for safety
    private BigDecimal capacityInLiters = BigDecimal.ZERO;
    private Integer stockQuantity = 0;
    private BigDecimal buyPrice = BigDecimal.ZERO;
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    private Boolean active = Boolean.TRUE;

    // ---- getters/setters ----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // trim; allow null
        this.name = (name == null) ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCapacityInLiters() {
        return capacityInLiters;
    }

    public void setCapacityInLiters(BigDecimal capacityInLiters) {
        // keep non-null
        this.capacityInLiters = (capacityInLiters == null) ? BigDecimal.ZERO : capacityInLiters;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = (stockQuantity == null) ? 0 : stockQuantity;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        // 2 decimals for monetary values
        this.buyPrice = (buyPrice == null) ? BigDecimal.ZERO : buyPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        // 2 decimals for monetary values
        this.sellingPrice = (sellingPrice == null) ? BigDecimal.ZERO : sellingPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = (active == null) ? Boolean.TRUE : active;
    }
}
