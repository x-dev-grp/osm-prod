package com.osm.oilproductionservice.dto;


import com.osm.oilproductionservice.model.OilContainer;
import com.xdev.xdevbase.dtos.BaseDto;

import java.math.BigDecimal;

public class OilContainerDTO extends BaseDto<OilContainer> {

    private final Boolean active = true;
    private String name;
    private String description;
    private BigDecimal capacityInLiters;
    private Integer stockQuantity;
    private String material;
    private BigDecimal buyPrice;
    private BigDecimal sellingPrice;
    private Integer reorderThreshold;
    private Integer reorderQuantity;
    private String storageLocationCode;
    private String imageUrl;
    private String certification;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        this.capacityInLiters = capacityInLiters;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public String getStorageLocationCode() {
        return storageLocationCode;
    }

    public void setStorageLocationCode(String storageLocationCode) {
        this.storageLocationCode = storageLocationCode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }
}