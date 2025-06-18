package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.enums.DeliveryType;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRepository extends BaseRepository<UnifiedDelivery> {


    /* ── OPTIONAL HELPERS (if you still use them elsewhere) ───── */
    @Query("select coalesce(d.oliveQuantity, 0) from UnifiedDelivery d where d.id = :id")
    double weightOfLot(@Param("id") String id);

    List<UnifiedDelivery> findByIdIn(Set<UUID> ids);
    List<UnifiedDelivery> findByLotNumberIn(Set<String> lotNumbers);
     List<UnifiedDelivery> findByGlobalLotNumber(String globalLotNumber);
    List<UnifiedDelivery> findByMillMachineId(UUID mill);

    List<UnifiedDelivery> findByMillMachineIsNotNull();

    @Query(value = """
            SELECT d.*
            FROM   delivery d
            WHERE  d.delivery_type = 'OLIVE'      -- DeliveryType.OLIVE
              AND  d.status        = 'CONTROLLED' -- OliveLotStatus.CONTROLLED
            """,
            nativeQuery = true)
    List<UnifiedDelivery> findOliveDeliveriesControlled();

    @Query("SELECT u FROM UnifiedDelivery u WHERE u.deliveryType IN :types AND u.qualityControlResults IS EMPTY")
    List<UnifiedDelivery> findByDeliveryTypeInAndQualityControlResultsIsNull(@Param("types") List<String> types);
}