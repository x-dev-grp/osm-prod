package com.osm.oilproductionservice.repository;

import com.osm.oilproductionservice.enums.DeliveryType;
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

    UnifiedDelivery findByLotNumberAndDeliveryType(String lotNumber, DeliveryType deliveryType);

    List<UnifiedDelivery> findByGlobalLotNumber(String globalLotNumber);

    List<UnifiedDelivery> findByMillMachineId(UUID mill);

    List<UnifiedDelivery> findByMillMachineIsNotNull();

    /**
     * Find all olive deliveries that have already been quality-controlled,
     * or simple receptions etc. We ensure the common predicates (deliveryType + isDeleted)
     * apply to all OR branches by grouping them.
     */
    @Query("""
        SELECT d
          FROM UnifiedDelivery d
         WHERE d.deliveryType = 'OLIVE'
           AND d.isDeleted = false
           AND (
                 (d.operationType = 'BASE'             AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
              OR (d.operationType = 'OLIVE_PURCHASE'  AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
              OR (d.operationType = 'PAYMENT'         AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
              OR (d.operationType = 'EXCHANGE'        AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY')
                      AND COALESCE(d.unitPrice, 0) <> 0)
              OR (d.operationType = 'SIMPLE_RECEPTION'AND d.status IN ('OLIVE_CONTROLLED','IN_PROGRESS','PROD_READY'))
           )
        """)
    List<UnifiedDelivery> findOliveDeliveriesControlled();

    // If qualityControlResults is a collection, IS EMPTY is correct (not IS NULL).
    @Query("""
        SELECT u
          FROM UnifiedDelivery u
         WHERE u.deliveryType IN :types
           AND u.qualityControlResults IS EMPTY
           AND u.isDeleted = false
        """)
    List<UnifiedDelivery> findByDeliveryTypeInAndQualityControlResultsIsNull(@Param("types") List<String> types);

    // Find deliveries by supplier ID
    @Query("""
        SELECT d
          FROM UnifiedDelivery d
         WHERE d.supplier.id = :supplierId
           AND d.isDeleted = false
        """)
    List<UnifiedDelivery> findBySupplierId(@Param("supplierId") UUID supplierId);

    // Fully paid = both price and paidAmount are non-null, and paidAmount ≥ price
    @Query("""
        SELECT d
          FROM UnifiedDelivery d
         WHERE d.supplier.id    = :supplierId
           AND d.price          IS NOT NULL
           AND d.paidAmount     IS NOT NULL
           AND d.paidAmount    >= d.price
           AND d.deliveryType   = 'OLIVE'
           AND d.operationType  = 'SIMPLE_RECEPTION'
           AND d.isDeleted      = false
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
              OR d.paidAmount <  d.price
           )
           AND d.deliveryType = 'OLIVE'
           AND d.isDeleted    = false
        """)
    List<UnifiedDelivery> findUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Count fully paid (note: JPQL must use property name isDeleted, not column is_deleted)
    @Query("""
        SELECT COUNT(d)
          FROM UnifiedDelivery d
         WHERE d.supplier.id   = :supplierId
           AND d.price         IS NOT NULL
           AND d.paidAmount    IS NOT NULL
           AND d.paidAmount   >= d.price
           AND d.deliveryType  = 'OLIVE'
           AND d.isDeleted     = false
        """)
    long countFullyPaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Count unpaid
    @Query("""
        SELECT COUNT(d)
          FROM UnifiedDelivery d
         WHERE d.supplier.id = :supplierId
           AND (
                 d.paidAmount IS NULL
              OR d.price      IS NULL
              OR d.paidAmount <  d.price
           )
           AND d.deliveryType = 'OLIVE'
           AND d.isDeleted    = false
        """)
    long countUnpaidDeliveriesBySupplierId(@Param("supplierId") UUID supplierId);

    // Native query: keep column name is_deleted (snake_case) since it's SQL, not JPQL
    @Query(value = "SELECT * FROM delivery d WHERE d.mill_machine_id = :mill AND d.status = :status AND d.is_deleted = false",
            nativeQuery = true)
    List<UnifiedDelivery> findByMillMachineIdAndStatus(@Param("mill") UUID mill, @Param("status") String status);

    Optional<UnifiedDelivery> findTopByOrderByCreatedDateDesc();
}
