package com.osm.oilproductionservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * [NOUVEAU] DTO pour la complétion d'une opération de filtration
 * Ce DTO a été créé pour:
 * - Recevoir les données nécessaires quand on termine une opération
 * - Permettre à l'utilisateur de saisir le volume après filtration
 * - Ajouter une note de completion
 * Il est utilisé dans l'endpoint PUT /{operationId}/complete
 */
@Data
public class FiltrationCompletionDto {

    @NotNull(message = "Le volume après filtration est requis")
    @DecimalMin(value = "0.0", message = "Le volume après filtration doit être positif")
    private Double volumeAfter;      // Volume après filtration (saisi par l'utilisateur)

    private String note;              // Note de completion (optionnelle)
}