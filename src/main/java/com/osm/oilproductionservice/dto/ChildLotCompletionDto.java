package com.osm.oilproductionservice.dto;

public class ChildLotCompletionDto {
    private String lotNumber;
    private Double oilQuantity;
    private Double rendement;
    private Double unpaidPrice;

    public ChildLotCompletionDto() {}
    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
    public Double getOilQuantity() { return oilQuantity; }
    public void setOilQuantity(Double oilQuantity) { this.oilQuantity = oilQuantity; }
    public Double getRendement() { return rendement; }
    public void setRendement(Double rendement) { this.rendement = rendement; }
    public Double getUnpaidPrice() { return unpaidPrice; }
    public void setUnpaidPrice(Double unpaidPrice) { this.unpaidPrice = unpaidPrice; }
}
