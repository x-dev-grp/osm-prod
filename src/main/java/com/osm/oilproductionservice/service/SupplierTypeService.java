package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.SupplierDto;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.GenericRepository;
import com.osm.oilproductionservice.repository.SupplierInfoTypeRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class SupplierTypeService extends BaseServiceImpl<Supplier, SupplierDto, SupplierDto> {

    // Repository to reattach BaseType entities (for region and supplier type)
    private final GenericRepository baseTypeRepository;
    private final SupplierInfoTypeRepository supplierInfoRepository;
    private final DeliveryRepository deliveryRepository;

    // Constructor injection: in addition to Supplier repository and ModelMapper,
    // inject the BaseType repository.
    public SupplierTypeService(BaseRepository<Supplier> repository,
                               ModelMapper modelMapper,
                               GenericRepository baseTypeRepository,
                               SupplierInfoTypeRepository supplierInfoRepository,
                               DeliveryRepository deliveryRepository) {
        super(repository, modelMapper);
        this.baseTypeRepository = baseTypeRepository;
        this.supplierInfoRepository = supplierInfoRepository;
        this.deliveryRepository = deliveryRepository;
    }

    // Get count of paid payments for a supplier
    public long getPaidPaymentsCount(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPaidPaymentsCount", supplierId);

        try {
            long count = deliveryRepository.countFullyPaidDeliveriesBySupplierId(supplierId);

            OSMLogger.logDataAccess(this.getClass(), "PAID_PAYMENTS_COUNT", "Supplier");
            OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_PAID_PAYMENTS_QUERIED",
                    "Found " + count + " paid payments for supplier: " + supplierId);
            OSMLogger.logMethodExit(this.getClass(), "getPaidPaymentsCount", count);
            OSMLogger.logPerformance(this.getClass(), "getPaidPaymentsCount", startTime, System.currentTimeMillis());

            return count;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting paid payments count for supplier: " + supplierId, e);
            throw e;
        }
    }

    // Get count of unpaid payments for a supplier
    public long getUnpaidPaymentsCount(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getUnpaidPaymentsCount", supplierId);

        try {
            long count = deliveryRepository.countUnpaidDeliveriesBySupplierId(supplierId);

            OSMLogger.logDataAccess(this.getClass(), "UNPAID_PAYMENTS_COUNT", "Supplier");
            OSMLogger.logBusinessEvent(this.getClass(), "SUPPLIER_UNPAID_PAYMENTS_QUERIED",
                    "Found " + count + " unpaid payments for supplier: " + supplierId);
            OSMLogger.logMethodExit(this.getClass(), "getUnpaidPaymentsCount", count);
            OSMLogger.logPerformance(this.getClass(), "getUnpaidPaymentsCount", startTime, System.currentTimeMillis());

            return count;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getting unpaid payments count for supplier: " + supplierId, e);
            throw e;
        }
    }

    @Override
    public Set<Action> actionsMapping(Supplier supplier) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", supplier);

        try {
            Set<Action> actions = new HashSet<>();
            actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));

            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", "Actions: " + actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());

            return actions;

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error mapping actions for supplier: " + supplier.getId(), e);
            throw e;
        }
    }
}
