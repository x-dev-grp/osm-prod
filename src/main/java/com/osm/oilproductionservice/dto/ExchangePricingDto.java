package com.osm.oilproductionservice.dto;

import java.util.UUID;

public class ExchangePricingDto {
    /** Delivery ID */
    private UUID deliveryId;

    /** Standard pricing fields */
    private Double unitPrice;
    private Double price;

    /** Exchange-specific fields */
    private String qualityGrade;
    private Double oilUnitPrice;
    private Double oilQuantity;
    private Double oilTotalValue;

    public ExchangePricingDto(UUID deliveryId, Double unitPrice, Double price, String qualityGrade, Double oilUnitPrice, Double oilQuantity, Double oilTotalValue) {
        this.deliveryId = deliveryId;
        this.unitPrice = unitPrice;
        this.price = price;
        this.qualityGrade = qualityGrade;
        this.oilUnitPrice = oilUnitPrice;
        this.oilQuantity = oilQuantity;
        this.oilTotalValue = oilTotalValue;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getQualityGrade() {
        return qualityGrade;
    }

    public void setQualityGrade(String qualityGrade) {
        this.qualityGrade = qualityGrade;
    }

    public Double getOilUnitPrice() {
        return oilUnitPrice;
    }

    public void setOilUnitPrice(Double oilUnitPrice) {
        this.oilUnitPrice = oilUnitPrice;
    }

    public Double getOilQuantity() {
        return oilQuantity;
    }

    public void setOilQuantity(Double oilQuantity) {
        this.oilQuantity = oilQuantity;
    }

    public Double getOilTotalValue() {
        return oilTotalValue;
    }

    public void setOilTotalValue(Double oilTotalValue) {
        this.oilTotalValue = oilTotalValue;
    }
}
