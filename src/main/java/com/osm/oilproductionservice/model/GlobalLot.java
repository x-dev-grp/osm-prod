package com.osm.oilproductionservice.model;

import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A logical grouping of individual lots.
 * Caches totalKg for quick checks.
 */
@Entity
@Table(name = "global_lot")
public class GlobalLot extends BaseEntity {

    @Column(nullable = false, length = 50, unique = true)
    private String globalLotNumber;

    @Column(nullable = false)
    private Double totalKg = 0.0;

    @OneToMany(mappedBy = "globalLot", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private Set<Lot> lots = new HashSet<>();

    public GlobalLot() { }

    public GlobalLot(String globalLotNumber) {
        this.globalLotNumber = globalLotNumber;
    }

    public GlobalLot(String globalLotNumber, Double totalKg, Set<Lot> lots) {
        this.globalLotNumber = globalLotNumber;
        this.totalKg = totalKg;
        this.lots = lots;
    }

    // Helper method to add a Lot
    public void addLot(Lot lot) {
        if (lot == null) return;
        this.lots.add(lot);
        lot.setGlobalLot(this);
        updateTotalKg();
    }

    // Helper method to remove a Lot
    public void removeLot(Lot lot) {
        if (lot == null) return;
        this.lots.remove(lot);
        lot.setGlobalLot(null);
        updateTotalKg();
    }

    // Helper method to update totalKg
    private void updateTotalKg() {
        this.totalKg = this.lots.stream().mapToDouble(Lot::getOliveQuantity).sum();
    }

    public String getGlobalLotNumber() {
        return globalLotNumber;
    }

    public void setGlobalLotNumber(String globalLotNumber) {
        this.globalLotNumber = globalLotNumber;
    }

    public Double getTotalKg() {
        return totalKg;
    }

    public void setTotalKg(Double totalKg) {
        this.totalKg = totalKg;
    }

    public Set<Lot> getLots() {
        return lots;
    }

    public void setLots(Set<Lot> lots) {
        this.lots.clear();
        if (lots != null) {
            lots.forEach(this::addLot);
        }
    }
}