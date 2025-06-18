package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.QualityControlResult;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QualityControlResultRepository extends BaseRepository<QualityControlResult> {
    List<QualityControlResult> findByDeliveryId(UUID deliveryId);
}