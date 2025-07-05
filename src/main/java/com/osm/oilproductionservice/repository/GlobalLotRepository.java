package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.GlobalLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CRUD interface for {@link GlobalLot}.
 * <p>
 * Extends {@code JpaRepository} so you automatically get:
 * <ul>
 *   <li>save / saveAll</li>
 *   <li>findById / findAll / findAllById</li>
 *   <li>delete / deleteById / deleteAll</li>
 *   <li>paging &amp; sorting helpers</li>
 * </ul>
 * plus any custom queries you declare below.
 */
public interface GlobalLotRepository extends JpaRepository<GlobalLot, UUID> {

    /**
     * Handy finder when the UI searches by “G-number”
     */
    Optional<GlobalLot> findGlobalLotByGlobalLotNumber(String label);
    // BEFORE (does not exist at runtime)

    // AFTER
    Optional<GlobalLot> findByGlobalLotNumber(String globalLotNumber);

    List<GlobalLot> findByLotsMillMachineId(UUID id);
}
