package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.dto.FiltrationStatus;
import com.osm.oilproductionservice.model.FiltrationOperation;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FiltrationOperationRepo extends BaseRepository<FiltrationOperation> {

    /**
     * [NOUVEAU] Trouver les opérations par statut
     *
     * Cette méthode est automatiquement implémentée par Spring Data JPA
     * Grâce à la convention de nommage "findBy" + "NomDuChamp"
     *
     * @param status Le statut recherché
     * @return Liste des opérations avec ce statut
     */
    List<FiltrationOperation> findByStatus(FiltrationStatus status);

    /**
     * [NOUVEAU] Trouver les opérations par unité source
     *
     * @param sourceId ID de l'unité source
     * @return Liste des opérations où cette unité est la source
     */
    List<FiltrationOperation> findBySourceStorageUnitId(UUID sourceId);

    /**
     * [NOUVEAU] Trouver les opérations par unité cible
     *
     * @param targetId ID de l'unité cible
     * @return Liste des opérations où cette unité est la cible
     */
    List<FiltrationOperation> findByTargetStorageUnitId(UUID targetId);

    /**
     * [NOUVEAU] Trouver toutes les opérations triées par date (du plus récent au plus ancien)
     *
     * @return Liste des opérations triées
     */
    List<FiltrationOperation> findAllByOrderByOperationDateDesc();
}