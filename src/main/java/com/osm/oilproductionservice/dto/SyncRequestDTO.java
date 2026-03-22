package com.osm.oilproductionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Requête de synchronisation d'opération")
public class SyncRequestDTO {

    @Schema(description = "Code QR scanné", example = "QR123456789", required = true)
    @NotBlank(message = "Le code QR est obligatoire")
    @Size(max = 255, message = "Le code QR ne peut pas dépasser 255 caractères")
    private String qrCode;

    @Schema(description = "Identifiant de l'appareil mobile", example = "DEVICE_001", required = true)
    @NotBlank(message = "L'identifiant de l'appareil est obligatoire")
    @Size(max = 100, message = "L'identifiant de l'appareil ne peut pas dépasser 100 caractères")
    private String deviceId;

    @Schema(description = "Timestamp de l'opération (optionnel)", example = "2024-01-15T10:30:00")
    private String timestamp;

    @Schema(description = "Type d'opération (optionnel)", example = "SCAN")
    private String operationType;

    @Schema(description = "Données supplémentaires (optionnel)", example = "{\"location\": \"usineA\"}")
    private String additionalData;

    // Constructeurs
    public SyncRequestDTO() {}

    public SyncRequestDTO(String qrCode, String deviceId) {
        this.qrCode = qrCode;
        this.deviceId = deviceId;
    }

    public SyncRequestDTO(String qrCode, String deviceId, String timestamp) {
        this.qrCode = qrCode;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    // Getters et Setters
    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return "SyncRequestDTO{" +
                "qrCode='" + qrCode + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", operationType='" + operationType + '\'' +
                '}';
    }

    // Méthode pour valider les champs obligatoires
    public boolean isValid() {
        return qrCode != null && !qrCode.trim().isEmpty() &&
                deviceId != null && !deviceId.trim().isEmpty();
    }

    // Méthode pour obtenir une version nettoyée des données
    public SyncRequestDTO sanitize() {
        SyncRequestDTO sanitized = new SyncRequestDTO();
        sanitized.setQrCode(this.qrCode != null ? this.qrCode.trim() : null);
        sanitized.setDeviceId(this.deviceId != null ? this.deviceId.trim() : null);
        sanitized.setTimestamp(this.timestamp);
        sanitized.setOperationType(this.operationType);
        sanitized.setAdditionalData(this.additionalData);
        return sanitized;
    }

    // Méthode pour créer une copie
    public SyncRequestDTO copy() {
        SyncRequestDTO copy = new SyncRequestDTO();
        copy.setQrCode(this.qrCode);
        copy.setDeviceId(this.deviceId);
        copy.setTimestamp(this.timestamp);
        copy.setOperationType(this.operationType);
        copy.setAdditionalData(this.additionalData);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SyncRequestDTO that = (SyncRequestDTO) o;

        if (!qrCode.equals(that.qrCode)) return false;
        return deviceId.equals(that.deviceId);
    }

    @Override
    public int hashCode() {
        int result = qrCode.hashCode();
        result = 31 * result + deviceId.hashCode();
        return result;
    }

    // Méthode statique pour créer une requête
    public static SyncRequestDTO of(String qrCode, String deviceId) {
        return new SyncRequestDTO(qrCode, deviceId);
    }

    // Méthode pour vérifier si la requête est pour une opération en attente
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(operationType);
    }

    // Méthode pour vérifier si la requête est pour une opération synchronisée
    public boolean isSynced() {
        return "SYNCED".equalsIgnoreCase(operationType) ||
                "SYNC".equalsIgnoreCase(operationType);
    }

    // Méthode pour obtenir les métadonnées sous forme de Map
    public java.util.Map<String, String> getMetadata() {
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        if (timestamp != null) metadata.put("timestamp", timestamp);
        if (operationType != null) metadata.put("operationType", operationType);
        if (additionalData != null) metadata.put("additionalData", additionalData);
        return metadata;
    }
}