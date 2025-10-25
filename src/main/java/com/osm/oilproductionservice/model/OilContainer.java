package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
public class OilContainer extends BaseEntity {

    private String name;
    private String description;
    private BigDecimal capacityInLiters;
    private Integer stockQuantity = 0;
    private BigDecimal buyPrice;
    private BigDecimal sellingPrice;
    private Boolean active = Boolean.TRUE;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ---- getters/setters ----
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public BigDecimal getCapacityInLiters() {
        return capacityInLiters;
    }

    public void setCapacityInLiters(BigDecimal capacityInLiters) {
        this.capacityInLiters = capacityInLiters;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
