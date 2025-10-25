package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.OilTransaction;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OilTransactionRepository extends BaseRepository<OilTransaction> {
    List<OilTransaction> findByStorageUnitDestinationId(UUID storageUnitId);
    // Add this method for payment validation
    List<OilTransaction> findByOilSaleId(UUID oilSaleId);
    Optional<OilTransaction> findByOilSaleIdAndIsDeletedFalse(UUID oilSaleId);
    Optional<OilTransaction> findFirstByOilSaleIdOrderByCreatedDateDesc(UUID oilSaleId);
}
