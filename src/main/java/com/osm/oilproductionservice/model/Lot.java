package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDate;

/**
 * A single reception lot that may (optionally) be grouped into a GlobalLot
 * and/or scheduled on a MillMachine.
 */
@Entity
@Table(name = "lot")
@Builder
public class Lot extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String lotNumber;

    @Column(nullable = false)
    private Double oliveQuantity = 0.0;          // kg

    private LocalDate deliveryDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mill_machine_id")
    private MillMachine millMachine;


    private Integer sequence;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_lot_id")
    private GlobalLot globalLot;
    @ManyToOne(fetch = FetchType.LAZY)          // NEW – back-reference
    @JoinColumn(name = "delivery_id")
    private UnifiedDelivery delivery;           // provenance

    @Column(length = 50)                        // NEW – fast search
    private String deliveryNumberCache;

    public Lot() {
    }

    public Lot(String lotNumber, Double oliveQuantity, LocalDate deliveryDate, MillMachine millMachine, Integer sequence, GlobalLot globalLot, UnifiedDelivery delivery, String deliveryNumberCache) {
        this.lotNumber = lotNumber;
        this.oliveQuantity = oliveQuantity;
        this.deliveryDate = deliveryDate;
        this.millMachine = millMachine;
        this.sequence = sequence;
        this.globalLot = globalLot;
        this.delivery = delivery;
        this.deliveryNumberCache = deliveryNumberCache;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public Double getOliveQuantity() {
        return oliveQuantity;
    }

    public void setOliveQuantity(Double oliveQuantity) {
        this.oliveQuantity = oliveQuantity;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public MillMachine getMillMachine() {
        return millMachine;
    }

    public void setMillMachine(MillMachine millMachine) {
        this.millMachine = millMachine;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public GlobalLot getGlobalLot() {
        return globalLot;
    }

    public void setGlobalLot(GlobalLot globalLot) {
        this.globalLot = globalLot;
    }

    public UnifiedDelivery getDelivery() {
        return delivery;
    }

    public void setDelivery(UnifiedDelivery delivery) {
        this.delivery = delivery;
    }

    public String getDeliveryNumberCache() {
        return deliveryNumberCache;
    }

    public void setDeliveryNumberCache(String deliveryNumberCache) {
        this.deliveryNumberCache = deliveryNumberCache;
    }

    public void assignToMill(MillMachine mill, int sequence) {
        this.millMachine = mill;
        this.sequence = sequence;
    }
}
