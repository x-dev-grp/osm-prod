package com.osm.oilproductionservice.dto;


import com.osm.oilproductionservice.model.OilContainer;
import com.xdev.xdevbase.dtos.BaseDto;

import java.math.BigDecimal;

public class OilContainerDTO extends BaseDto<OilContainer> {

    private String name;

    private BigDecimal capacityInLiters;

    private Integer stockQuantity;

    private BigDecimal buyPrice;

    private BigDecimal sellingPrice;

    private Boolean active;
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
