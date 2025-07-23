package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.xdev.xdevbase.repos.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DeliveryRepository extends BaseRepository<UnifiedDelivery> {


    /* ── OPTIONAL HELPERS (if you still use them elsewhere) ───── */
    @Query("select coalesce(d.oliveQuantity, 0) from UnifiedDelivery d where d.id = :id")
    double weightOfLot(@Param("id") String id);

    @Query(value = """
        SELECT *
          FROM delivery d
         WHERE d.lot_olive_number = (
                   SELECT d2.lot_number
                     FROM delivery d2
                    WHERE d2.id = :deliveryId
               )
        """,
            nativeQuery = true)
    UnifiedDelivery findByLotOliveNumber(@Param("deliveryId") UUID deliveryId);



    List<UnifiedDelivery> findByLotNumberIn(Set<String> lotNumbers);
    UnifiedDelivery findByLotNumber(String lotNumbers);

    List<UnifiedDelivery> findByGlobalLotNumber(String globalLotNumber);

    List<UnifiedDelivery> findByMillMachineId(UUID mill);

    List<UnifiedDelivery> findByMillMachineIsNotNull();

    /**
     * Find all olive deliveries that have already been quality-controlled,
     * or simple receptions that are either NEW or OLIVE_CONTROLLED.
     */
    @Query("SELECT d " +
            "FROM UnifiedDelivery d " +
            "WHERE d.deliveryType = 'OLIVE'" +
            " and (d.operationType = 'BASE' and d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))" +
            " OR  (d.operationType  = 'OLIVE_PURCHASE' and d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))" +
            " OR  (d.operationType  = 'PAYMENT' and d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))" +
            "  OR (d.operationType  = 'EXCHANGE' and d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY') and d.unitPrice <>0 )" +
            " OR  (d.operationType  = 'SIMPLE_RECEPTION' and d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY','NEW'))"
    )
    List<UnifiedDelivery> findOliveDeliveriesControlled();

    @Query("SELECT u FROM UnifiedDelivery u WHERE u.deliveryType IN :types AND u.qualityControlResults IS EMPTY")
    List<UnifiedDelivery> findByDeliveryTypeInAndQualityControlResultsIsNull(@Param("types") List<String> types);

    // Find deliveries by supplier ID
    @Query("SELECT d FROM UnifiedDelivery d WHERE d.supplier.id = :supplierId")
    List<UnifiedDelivery> findBySupplierId(@Param("supplierId") UUID supplierId);

    // Fully paid = both price and paidAmount are non-null, and paidAmount ≥ price
    @Query("""
              SELECT d
                FROM UnifiedDelivery d
               WHERE d.supplier.id    = :supplierId
                 AND d.price          IS NOT NULL
                 AND d.paidAmount     IS NOT NULL
                 AND d.paidAmount    >= d.price
                 AND d.deliveryType  = 'OLIVE'
                 AND d.operationType = 'SIMPLE_RECEPTION'
            
            """)
    List<UnifiedDelivery> findFullyPaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Not fully paid = either no payment or payment < price
    @Query("""
              SELECT d
                FROM UnifiedDelivery d
               WHERE d.supplier.id = :supplierId
                 AND (
                       d.paidAmount IS NULL
                    OR d.price      IS NULL
                    OR d.paidAmount < d.price
                 )
                 AND d.deliveryType  = 'OLIVE'
            """)
    List<UnifiedDelivery> findUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Count only those deliveries where both price and paidAmount are non‐null
// and paidAmount is at least the price (i.e. fully paid)
    @Query("""
              SELECT COUNT(d)
                FROM UnifiedDelivery d
               WHERE d.supplier.id    = :supplierId
                 AND d.price IS NOT NULL
                 AND d.paidAmount IS NOT NULL
                 AND d.paidAmount >= d.price
                 AND d.deliveryType  = 'OLIVE'
            
            """)
    long countFullyPaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Count deliveries that are either never paid or paid less than the price
    @Query("""
              SELECT COUNT(d)
                FROM UnifiedDelivery d
               WHERE d.supplier.id = :supplierId
                 AND (
                       d.paidAmount IS NULL
                    OR d.price IS    NULL
                    OR d.paidAmount < d.price
                 )
                  AND d.deliveryType  = 'OLIVE'
            
            """)
    long countUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    @Query(value = "SELECT * FROM delivery d WHERE d.mill_machine_id = :mill AND d.status = :status", nativeQuery = true)
    List<UnifiedDelivery> findByMillMachineIdAndStatus(@Param("mill") UUID mill, @Param("status") String status);

    Optional<UnifiedDelivery> findTopByOrderByCreatedDateDesc();


}