package com.osm.oilproductionservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;



import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;



/**
 * The UnifiedDelivery entity combines common delivery fields, oil-specific properties, and olive-specific properties.
 * Depending on the deliveryType, only a subset of these fields may be populated.
 */
@Entity
@Table(name = "delivery")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnifiedDelivery extends BaseEntity implements Serializable {

    // --- Common Fields ---
    private String deliveryNumber;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;  // E.g., OIL, OLIVE, etc.

    private String lotNumber;
    private LocalDateTime deliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType region;

    // Example weight fields (if these are required)
    private float poidsBrute;
    private float poidsNet;

    private String matriculeCamion;
    private String etatCamion;

    @ManyToOne(fetch = FetchType.LAZY)
    private Supplier supplier;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QualityControlResult> qualityControlResults = new HashSet<>();

    @Column(name = "has_quality_control")
    private boolean hasQualityControl = false;

    public boolean isHasQualityControl() {
        return hasQualityControl;
    }

    public void setHasQualityControl(boolean hasQualityControl) {
        this.hasQualityControl = hasQualityControl;
    }

    // --- Oil Delivery Specific Fields ---
    // Note: globalLotNumber is common to both oil and olive deliveries.
    private String globalLotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilVariety; // The variety of oil
    private Float oilQuantity;   // In appropriate unit, e.g., liters

    private Float unitPrice;
    private Float price;
    private Float paidAmount;
    private Float unpaidAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    private StorageUnit storageUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilType;   // E.g., Extra Virgin, Virgin, etc.

    // --- Olive Delivery Specific Fields ---
    private LocalDateTime trtDate;   // Treatment date for olive delivery
    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType operationType;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oliveVariety;
    private int sackCount;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oliveType;

    @Enumerated(EnumType.STRING)
    private OliveLotStatus status;  // Status of the olive lot

    // Additional fields found in the UnifiedDelivery constructor, if needed
    private Float rendement; // Yield or performance measure
    @ManyToOne(fetch = FetchType.LAZY)
    private MillMachine millMachine;
    private Float oliveQuantity;
    private String parcel;



    /**
     * A full-argument constructor to initialize all common fields along with delivery-specific properties.
     * In real usage, you might create helper constructors or builders to handle the optional fields.
     */


    // --- Getters and Setters ---
    // Common getters and setters



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

    public BaseType getRegion() {
        return region;
    }

    public void setRegion(BaseType region) {
        this.region = region;
    }

    public float getPoidsBrute() {
        return poidsBrute;
    }

    public void setPoidsBrute(float poidsBrute) {
        this.poidsBrute = poidsBrute;
    }

    public float getPoidsNet() {
        return poidsNet;
    }

    public void setPoidsNet(float poidsNet) {
        this.poidsNet = poidsNet;
    }

    public String getMatriculeCamion() {
        return matriculeCamion;
    }

    public void setMatriculeCamion(String matricule_camion) {
        this.matriculeCamion = matricule_camion;
    }

    public String getEtatCamion() {
        return etatCamion;
    }

    public void setEtatCamion(String etat_camion) {
        this.etatCamion = etat_camion;
    }

    public Supplier getSupplierType() {
        return supplier;
    }

    public void setSupplierType(Supplier supplier) {
        this.supplier = supplier;
    }

    public Set<QualityControlResult> getQualityControlResults() {
        return qualityControlResults;
    }

    public void setQualityControlResults(Set<QualityControlResult> qualityControlResults) {
        this.qualityControlResults = qualityControlResults;
    }

    // Oil-specific getters and setters
    public String getGlobalLotNumber() {
        return globalLotNumber;
    }

    public void setGlobalLotNumber(String globalLotNumber) {
        this.globalLotNumber = globalLotNumber;
    }

    public BaseType getOilVariety() {
        return oilVariety;
    }

    public void setOilVariety(BaseType oilVariety) {
        this.oilVariety = oilVariety;
    }

    public Float getOilQuantity() {
        return oilQuantity;
    }

    public void setOilQuantity(Float oilQuantity) {
        this.oilQuantity = oilQuantity;
    }

    public Float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Float paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Float getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Float unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public StorageUnit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
    }

    public BaseType getOilType() {
        return oilType;
    }

    public void setOilType(BaseType oilType) {
        this.oilType = oilType;
    }

    // Olive-specific getters and setters
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    public LocalDateTime getTrtDate() {
        return trtDate;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    public void setTrtDate(LocalDateTime trtDate) {
        this.trtDate = trtDate;
    }

    public BaseType getOperationType() {
        return operationType;
    }

    public void setOperationType(BaseType operationType) {
        this.operationType = operationType;
    }

    public BaseType getOliveVariety() {
        return oliveVariety;
    }

    public void setOliveVariety(BaseType oliveVariety) {
        this.oliveVariety = oliveVariety;
    }

    public int getSackCount() {
        return sackCount;
    }

    public void setSackCount(int sackCount) {
        this.sackCount = sackCount;
    }

    public BaseType getOliveType() {
        return oliveType;
    }

    public void setOliveType(BaseType oliveType) {
        this.oliveType = oliveType;
    }

    public OliveLotStatus getStatus() {
        return status;
    }

    public void setStatus(OliveLotStatus status) {
        this.status = status;
    }

    public Float getRendement() {
        return rendement;
    }

    public void setRendement(Float rendement) {
        this.rendement = rendement;
    }

    public MillMachine getMillMachine() {
        return millMachine;
    }

    public void setMillMachine(MillMachine millMachine) {
        this.millMachine = millMachine;
    }

    public Float getOliveQuantity() {
        return oliveQuantity;
    }

    public void setOliveQuantity(Float oliveQuantity) {
        this.oliveQuantity = oliveQuantity;
    }

    public String getParcel() {
        return parcel;
    }

    public void setParcel(String parcel) {
        this.parcel = parcel;
    }


}
