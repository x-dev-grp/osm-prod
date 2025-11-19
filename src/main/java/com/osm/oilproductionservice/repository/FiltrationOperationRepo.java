package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.FiltrationOperation;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FiltrationOperationRepo extends BaseRepository<FiltrationOperation> {
    List<FiltrationOperation> findByStorageUnit_IdOrderByOperationDateDesc(UUID storageUnitId);
}
