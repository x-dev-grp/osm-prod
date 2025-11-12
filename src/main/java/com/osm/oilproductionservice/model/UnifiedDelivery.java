package com.osm.oilproductionservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xdev.communicator.models.enums.DeliveryType;
import com.xdev.communicator.models.enums.OliveLotStatus;
import com.xdev.communicator.models.enums.Olive_Oil_Type;
import com.xdev.communicator.models.enums.OperationType;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.math3.util.Precision.round;

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
    private String categoryOliveOil;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    // E.g., OIL, OLIVE, etc.
    private String lotNumber;

    private String description;

    private String lotOliveNumber;

    private LocalDateTime deliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType region;

    // Numeric (default to 0)
    private Double poidsBrute = 0d;
    private Double poidsNet = 0d;

    private String matriculeCamion;
    private String etatCamion;

    private boolean paid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Supplier supplier;

    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<QualityControlResult> qualityControlResults = new HashSet<>();

    @Column(name = "has_quality_control")
    private boolean hasQualityControl = false;

    // --- Oil Delivery Specific Fields ---
    private String globalLotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oilVariety; // The variety of oil

    private Double oilQuantity = 0d;   // e.g., liters
    private Double unitPrice = 0d;
    private Double price = 0d;
    private Double paidAmount = 0d;
    private Double unpaidAmount = 0d;

    @ManyToOne(fetch = FetchType.LAZY)
    private StorageUnit storageUnit;

    @Enumerated(EnumType.STRING)
    private Olive_Oil_Type oilType;

    // --- Olive Delivery Specific Fields ---
    private LocalDateTime trtDate;   // Treatment date

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType oliveVariety;

    private int sackCount;

    @Enumerated(EnumType.STRING)
    private Olive_Oil_Type oliveType;

    @Enumerated(EnumType.STRING)
    private OliveLotStatus status;  // Olive lot status

    private Double rendement = 0d; // Yield/performance

    @ManyToOne(fetch = FetchType.LAZY)
    private MillMachine millMachine;

    private Double oliveQuantity = 0d;

    private Double poidsCamionVide = 0d;

    @ManyToOne(fetch = FetchType.LAZY)
    private BaseType parcel;

    private Integer trtDuration;

    // ----- Getters / Setters -----

    public Olive_Oil_Type getOilType() {
        return oilType;
    }

    public void setOilType(Olive_Oil_Type oilType) {
        this.oilType = oilType;
    }

    public Double getPoidsCamionVide() {
        return poidsCamionVide;
    }

    public void setPoidsCamionVide(Double poidsCamionVide) {
        this.poidsCamionVide = poidsCamionVide == null ? 0d : round(poidsCamionVide, 3);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTrtDuration() {
        return trtDuration;
    }

    public void setTrtDuration(Integer trtDuration) {
        this.trtDuration = trtDuration;
    }

    public boolean getPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public boolean isHasQualityControl() {
        return hasQualityControl;
    }

    public void setHasQualityControl(boolean hasQualityControl) {
        this.hasQualityControl = hasQualityControl;
    }

    public String getLotOliveNumber() {
        return lotOliveNumber;
    }

    public void setLotOliveNumber(String lotOliveNumber) {
        this.lotOliveNumber = lotOliveNumber;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getCategoryOliveOil() {
        return categoryOliveOil;
    }

    public void setCategoryOliveOil(String categoryOlivOil) {
        this.categoryOliveOil = categoryOlivOil;
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

    public BaseType getRegion() {
        return region;
    }

    public void setRegion(BaseType region) {
        this.region = region;
    }

    public Double getPoidsBrute() {
        return poidsBrute;
    }

    public void setPoidsBrute(Double poidsBrute) {
        this.poidsBrute = poidsBrute == null ? 0d : round(poidsBrute, 3);
    }

    public Double getPoidsNet() {
        return poidsNet;
    }

    public void setPoidsNet(Double poidsNet) {
        this.poidsNet = poidsNet == null ? 0d : round(poidsNet, 3);
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

    public Double getOilQuantity() {
        return oilQuantity;
    }

    public void setOilQuantity(Double oilQuantity) {
        this.oilQuantity = oilQuantity == null ? 0d : round(oilQuantity, 3);
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice == null ? 0d : round(unitPrice, 3);
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price == null ? 0d : round(price, 3);
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount == null ? 0d : round(paidAmount, 3);
    }

    public Double getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(Double unpaidAmount) {
        this.unpaidAmount = unpaidAmount == null ? 0d : round(unpaidAmount, 3);
    }

    public StorageUnit getStorageUnit() {
        return storageUnit;
    }

    public void setStorageUnit(StorageUnit storageUnit) {
        this.storageUnit = storageUnit;
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

    public Olive_Oil_Type getOliveType() {
        return oliveType;
    }

    public void setOliveType(Olive_Oil_Type oliveType) {
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
        this.rendement = rendement == null ? 0d : round(rendement, 3);
    }

    public MillMachine getMillMachine() {
        return millMachine;
    }

    public void setMillMachine(MillMachine millMachine) {
        this.millMachine = millMachine;
    }

    public Double getOliveQuantity() {
        return oliveQuantity;
    }

    public void setOliveQuantity(Double oliveQuantity) {
        this.oliveQuantity = oliveQuantity == null ? 0d : round(oliveQuantity, 3);
    }

    public BaseType getParcel() {
        return parcel;
    }

    public void setParcel(BaseType parcel) {
        this.parcel = parcel;
    }


}
