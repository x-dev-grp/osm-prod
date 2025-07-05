package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.Lot;
import com.osm.oilproductionservice.model.MillMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Spring-Data JPA repository for {@link Lot}.
 * Uses <code>String</code> IDs (UUID strings) to stay consistent with the
 * rest of the domain model.
 */
public interface LotRepository extends JpaRepository<Lot, UUID> {

    /*───────────────────────────────────────────────────────────────*
     *  Light-weight scalar query – used in capacity checks          *
     *───────────────────────────────────────────────────────────────*/
    @Query("select coalesce(l.oliveQuantity, 0) from Lot l where l.id = :id")
    Double weightOfLot(@Param("id") UUID lotId);

    /*───────────────────────────────────────────────────────────────*
     *  Bulk clear: detach all given lots from any mill column       *
     *───────────────────────────────────────────────────────────────*/
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Lot l set l.millMachine = null, l.sequence = null " +
            "where l.id in :ids")
    void clearMillAssignments(@Param("ids") Set<UUID> lotIds);


    @Query("select l from Lot l where l.delivery.id = :deliveryId")
    Optional<Lot> lookupByDelivery(@Param("deliveryId") UUID deliveryId);

    /*───────────────────────────────────────────────────────────────*
     *  Assign a single lot to a mill and set its position           *
     *───────────────────────────────────────────────────────────────*/
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Lot l set l.millMachine = :mill, l.sequence = :seq " +
            "where l.id = :id")
    void assignToMill(@Param("id") UUID lotId,
                      @Param("mill") MillMachine mill,
                      @Param("seq") Integer sequence);

    /*───────────────────────────────────────────────────────────────*
     *  Convenience finder used by validation or imports             *
     *───────────────────────────────────────────────────────────────*/
    Optional<Lot> findByLotNumber(String lotNumber);

    Optional<Lot> findByDeliveryId(UUID id);

    List<Lot> findByMillMachineId(UUID millMachineId);
}
