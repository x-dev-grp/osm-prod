package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.TraceabilityLot;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraceabilityLotRepository extends BaseRepository<TraceabilityLot> {
    Optional<TraceabilityLot> findByIdAndIsDeletedFalse(UUID id);

    Optional<TraceabilityLot> findFirstByStorageUnitIdAndActiveTrueAndIsDeletedFalseOrderByCapturedAtDesc(UUID storageUnitId);

    Optional<TraceabilityLot> findFirstByLotNumberAndIsDeletedFalseOrderByCapturedAtDesc(String lotNumber);

    List<TraceabilityLot> findAllByRootReceptionIdAndIsDeletedFalse(UUID rootReceptionId);
}
