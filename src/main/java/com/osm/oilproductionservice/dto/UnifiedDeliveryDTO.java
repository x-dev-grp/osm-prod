package com.osm.oilproductionservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.xdev.communicator.models.production.enums.OperationType;
import com.xdev.xdevbase.dtos.BaseDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * DTO for UnifiedDelivery.
 */
public class UnifiedDeliveryDTO extends BaseDto<UnifiedDelivery> {

    // --- Common Fields ---
    private String deliveryNumber;

    @NotNull(message = "Delivery type is required")
    private DeliveryType deliveryType;
    private OperationType operationType;

    private String lotNumber;
    private String lotOliveNumber;

    @NotNull(message = "Delivery date is required")
    private LocalDateTime deliveryDate;

    // For the DTO, you may choose to represent complex objects (e.g. BaseType region) as Strings (e.g., region name)
    @NotNull(message = "Region is required")
    private BaseTypeDto region;

    private Double poidsBrute;
    private Double poidsNet;
    private String matriculeCamion;
    private String etatCamion;
    // Represent supplierType as a string for simplification (or use a nested DTO if needed)

    private SupplierDto supplier;
    // Quality control results can be represented as IDs, summaries, or a list of DTOs

    // --- Oil Delivery Specific Fields ---
    // This field is common to both oil and olive deliveries if needed
    private String globalLotNumber;
    // Represent oil-specific associations (could be names or IDs)

    private BaseTypeDto oilVariety;

    private Double oilQuantity;

    private Double unitPrice;

    private Double price;

    private Double paidAmount;

    private Double unpaidAmount;

    private BaseTypeDto oilType;

    // --- Olive Delivery Specific Fields ---

    private LocalDateTime trtDate;


    private BaseTypeDto oliveVariety;

    private int sackCount;

    private BaseTypeDto oliveType;

    private OliveLotStatus status;

    private Double rendement;

    private Double oliveQuantity;

    private String parcel;

    private Set<QualityControlResultDto> qualityControlResults = new HashSet<>();

    private StorageUnitDto storageUnit;
    private MillMachineDto millMachine;

    public String getLotOliveNumber() {
        return lotOliveNumber;
    }

    public void setLotOliveNumber(String lotOliveNumber) {
        this.lotOliveNumber = lotOliveNumber;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }


    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Set<QualityControlResultDto> getQualityControlResults() {
        return qualityControlResults;
    }

    public void setQualityControlResults(Set<QualityControlResultDto> qualityControlResults) {
        this.qualityControlResults = qualityControlResults;
    }

    public StorageUnitDto getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnitDto storageUnit) {
        this.storageUnit = storageUnit;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public MillMachineDto getMillMachine() {
        return millMachine;
    }

    public void setMillMachine(MillMachineDto millMachine) {
        this.millMachine = millMachine;
    }

    public BaseTypeDto getRegion() {
        return region;
    }

    public void setRegion(BaseTypeDto region) {
        this.region = region;
    }


    public Double getPoidsBrute() {
        return poidsBrute;
    }

    public void setPoidsBrute(Double poidsBrute) {
        this.poidsBrute = poidsBrute;
    }

    public Double getPoidsNet() {
        return poidsNet;
    }

    public void setPoidsNet(Double poidsNet) {
        this.poidsNet = poidsNet;
    }

    public String getMatriculeCamion() {
        return matriculeCamion;
    }

    public void setMatriculeCamion(String matriculeCamion) {
        this.matriculeCamion = matriculeCamion;
    }

    public String getEtatCamion() {
        return etatCamion;
    }

    public void setEtatCamion(String etatCamion) {
        this.etatCamion = etatCamion;
    }

    public SupplierDto getSupplier() {
        return supplier;
    }

    public void setSupplier(SupplierDto supplier) {
        this.supplier = supplier;
    }

    public String getGlobalLotNumber() {
        return globalLotNumber;
    }

    public void setGlobalLotNumber(String globalLotNumber) {
        this.globalLotNumber = globalLotNumber;
    }

    public BaseTypeDto getOilVariety() {
        return oilVariety;
    }

    public void setOilVariety(BaseTypeDto oilVariety) {
        this.oilVariety = oilVariety;
    }

    public Double getOilQuantity() {
        return oilQuantity;
    }

    public void setOilQuantity(Double oilQuantity) {
        this.oilQuantity = oilQuantity;
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

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }


    public BaseTypeDto getOilType() {
        return oilType;
    }

    public void setOilType(BaseTypeDto oilType) {
        this.oilType = oilType;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime getTrtDate() {
        return trtDate;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public void setTrtDate(LocalDateTime trtDate) {
        this.trtDate = trtDate;
    }


    public BaseTypeDto getOliveVariety() {
        return oliveVariety;
    }

    public void setOliveVariety(BaseTypeDto oliveVariety) {
        this.oliveVariety = oliveVariety;
    }

    public int getSackCount() {
        return sackCount;
    }

    public void setSackCount(int sackCount) {
        this.sackCount = sackCount;
    }

    public BaseTypeDto getOliveType() {
        return oliveType;
    }

    public void setOliveType(BaseTypeDto oliveType) {
        this.oliveType = oliveType;
    }

    public OliveLotStatus getStatus() {
        return status;
    }

    public void setStatus(OliveLotStatus status) {
        this.status = status;
    }

    public Double getRendement() {
        return rendement;
    }

    public void setRendement(Double rendement) {
        this.rendement = rendement;
    }


    public Double getOliveQuantity() {
        return oliveQuantity;
    }

    public void setOliveQuantity(Double oliveQuantity) {
        this.oliveQuantity = oliveQuantity;
    }

    public String getParcel() {
        return parcel;
    }

    public void setParcel(String parcel) {
        this.parcel = parcel;
    }
}
