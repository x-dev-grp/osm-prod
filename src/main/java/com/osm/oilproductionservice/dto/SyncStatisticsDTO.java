package com.osm.oilproductionservice.dto;


import java.util.Map;

public class SyncStatisticsDTO {

    private Long pendingCount;
    private Long syncedCount;
    private Long failedCount;
    private Long totalCount;
    private Long todayCount;
    private Map<String, Long> distribution;
    private Double successRate;

    // Constructeurs
    public SyncStatisticsDTO() {}

    public SyncStatisticsDTO(Long pendingCount, Long syncedCount, Long failedCount) {
        this.pendingCount = pendingCount;
        this.syncedCount = syncedCount;
        this.failedCount = failedCount;
        this.totalCount = (pendingCount != null ? pendingCount : 0) +
                (syncedCount != null ? syncedCount : 0) +
                (failedCount != null ? failedCount : 0);
        calculateSuccessRate();
    }

    // Getters et Setters
    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
        updateTotalCount();
        calculateSuccessRate();
    }

    public Long getSyncedCount() {
        return syncedCount;
    }

    public void setSyncedCount(Long syncedCount) {
        this.syncedCount = syncedCount;
        updateTotalCount();
        calculateSuccessRate();
    }

    public Long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Long failedCount) {
        this.failedCount = failedCount;
        updateTotalCount();
        calculateSuccessRate();
    }

    public Long getTotalCount() {
        if (totalCount == null) {
            updateTotalCount();
        }
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(Long todayCount) {
        this.todayCount = todayCount;
    }

    public Map<String, Long> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<String, Long> distribution) {
        this.distribution = distribution;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    // Méthodes utilitaires privées
    private void updateTotalCount() {
        this.totalCount = (pendingCount != null ? pendingCount : 0) +
                (syncedCount != null ? syncedCount : 0) +
                (failedCount != null ? failedCount : 0);
    }

    private void calculateSuccessRate() {
        Long total = getTotalCount();
        if (total != null && total > 0 && syncedCount != null) {
            this.successRate = Math.round((syncedCount * 100.0) / total * 100.0) / 100.0;
        } else {
            this.successRate = 0.0;
        }
    }

    // Méthode pour obtenir un résumé textuel
    public String getSummary() {
        return String.format(
                "Statistiques - Total: %d, En attente: %d, Synchronisées: %d, Échouées: %d, Aujourd'hui: %d, Taux de succès: %.2f%%",
                getTotalCount(),
                pendingCount != null ? pendingCount : 0,
                syncedCount != null ? syncedCount : 0,
                failedCount != null ? failedCount : 0,
                todayCount != null ? todayCount : 0,
                successRate != null ? successRate : 0.0
        );
    }

    // Méthode pour vérifier s'il y a des opérations en attente
    public boolean hasPendingOperations() {
        return pendingCount != null && pendingCount > 0;
    }

    // Méthode pour vérifier s'il y a des opérations échouées
    public boolean hasFailedOperations() {
        return failedCount != null && failedCount > 0;
    }

    // Méthode pour obtenir le pourcentage d'opérations synchronisées
    public Double getSyncedPercentage() {
        Long total = getTotalCount();
        if (total != null && total > 0 && syncedCount != null) {
            return Math.round((syncedCount * 100.0) / total * 100.0) / 100.0;
        }
        return 0.0;
    }

    // Méthode pour obtenir le pourcentage d'opérations en attente
    public Double getPendingPercentage() {
        Long total = getTotalCount();
        if (total != null && total > 0 && pendingCount != null) {
            return Math.round((pendingCount * 100.0) / total * 100.0) / 100.0;
        }
        return 0.0;
    }

    // Méthode pour obtenir le pourcentage d'opérations échouées
    public Double getFailedPercentage() {
        Long total = getTotalCount();
        if (total != null && total > 0 && failedCount != null) {
            return Math.round((failedCount * 100.0) / total * 100.0) / 100.0;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "SyncStatisticsDTO{" +
                "pendingCount=" + pendingCount +
                ", syncedCount=" + syncedCount +
                ", failedCount=" + failedCount +
                ", totalCount=" + totalCount +
                ", todayCount=" + todayCount +
                ", distribution=" + distribution +
                ", successRate=" + successRate +
                '}';
    }

    // Méthode statique pour créer un DTO vide
    public static SyncStatisticsDTO empty() {
        return new SyncStatisticsDTO(0L, 0L, 0L);
    }

    // Méthode pour fusionner avec un autre DTO (utile pour les agrégations)
    public SyncStatisticsDTO merge(SyncStatisticsDTO other) {
        if (other == null) {
            return this;
        }

        this.pendingCount = (this.pendingCount != null ? this.pendingCount : 0) +
                (other.pendingCount != null ? other.pendingCount : 0);
        this.syncedCount = (this.syncedCount != null ? this.syncedCount : 0) +
                (other.syncedCount != null ? other.syncedCount : 0);
        this.failedCount = (this.failedCount != null ? this.failedCount : 0) +
                (other.failedCount != null ? other.failedCount : 0);
        this.todayCount = (this.todayCount != null ? this.todayCount : 0) +
                (other.todayCount != null ? other.todayCount : 0);

        updateTotalCount();
        calculateSuccessRate();

        return this;
    }


}