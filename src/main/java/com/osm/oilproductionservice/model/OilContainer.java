package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "oil_container")
public class OilContainer extends BaseEntity {

    private String name;
    private String lotNumber;
    private String description;
    /**
     * Liters capacity, e.g. 1.00, 5.00
     */
    private BigDecimal capacityInLiters;
    /**
     * How many of these are currently in stock
     */
    private Integer stockQuantity;
    /**
     * e.g. "Glass", "Tin", "Plastic"
     */
    private String material;
    /**
     * Cost to acquire each empty container
     */
    private BigDecimal buyPrice;
    /**
     * Price charged per filled container
     */
    private BigDecimal sellingPrice;
    /**
     * When stock ≤ this, trigger a reorder
     */
    private Integer reorderThreshold;
    /**
     * Quantity to order when threshold is hit
     */
    private Integer reorderQuantity;
    /**
     * e.g. "WH1-A3-B2"
     */
    private String storageLocationCode;
    /**
     * URL to a photo of the container
     */
    private String imageUrl;
    /**
     * Enable/disable without deleting
     */
    private Boolean active = true;
    /**
     * e.g. "FDA food-grade"
     */
    private String certification;

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

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

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }
}
