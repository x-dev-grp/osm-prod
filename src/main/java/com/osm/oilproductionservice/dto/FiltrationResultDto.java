package com.osm.oilproductionservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Data
public class FiltrationResultDto {

    private UUID operationId;           // ID de l'opération
    private StorageUnitDto source;               // Unité source
    private StorageUnitDto target;               // Unité cible
    private double volumeFiltered;       // Volume initial à filtrer

    // [NOUVEAU] Champs ajoutés pour la complétion
    private Double volumeAfter;          // Volume après filtration
    private Double lossVolume;           // Volume perdu pendant la filtration
    private Double lossPercent;          // Pourcentage de perte

    private String status;                // Statut (CREATED, IN_PROGRESS, COMPLETED)
    private LocalDateTime timestamp;      // Date de l'opération
    private String note;                   // Note
}