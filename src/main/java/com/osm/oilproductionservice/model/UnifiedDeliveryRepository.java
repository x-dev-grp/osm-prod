package com.osm.oilproductionservice.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnifiedDeliveryRepository extends JpaRepository<UnifiedDelivery, UUID> {
}