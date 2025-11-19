package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.StorageUnit;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StorageUnitRepo extends BaseRepository<StorageUnit> {
    UUID id(UUID id);
}
