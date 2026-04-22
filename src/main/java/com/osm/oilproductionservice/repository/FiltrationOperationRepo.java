package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.FiltrationOperation;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FiltrationOperationRepo extends BaseRepository<FiltrationOperation> {

    // Override the base findById to exclude soft-deleted records
    @Query(value = "SELECT * FROM filtration_operation WHERE id = :id AND is_deleted = false", nativeQuery = true)
    Optional<FiltrationOperation> findById(@Param("id") UUID id);

    // Exclude deleted from all-results query, ordered by date desc
    @Query(value = "SELECT * FROM filtration_operation WHERE is_deleted = false ORDER BY operation_date DESC", nativeQuery = true)
    List<FiltrationOperation> findAllByIsDeletedFalseOrderByOperationDateDesc();

    // Filter by status, excluding deleted
    @Query(value = "SELECT * FROM filtration_operation WHERE status = :status AND is_deleted = false", nativeQuery = true)
    List<FiltrationOperation> findByStatusAndIsDeletedFalse(@Param("status") String status);

    // Filter by source unit, excluding deleted
    @Query(value = "SELECT * FROM filtration_operation WHERE source_storage_unit_id = :sourceId AND is_deleted = false", nativeQuery = true)
    List<FiltrationOperation> findBySourceStorageUnitIdAndIsDeletedFalse(@Param("sourceId") UUID sourceId);

    // Filter by target unit, excluding deleted
    @Query(value = "SELECT * FROM filtration_operation WHERE target_storage_unit_id = :targetId AND is_deleted = false", nativeQuery = true)
    List<FiltrationOperation> findByTargetStorageUnitIdAndIsDeletedFalse(@Param("targetId") UUID targetId);
}