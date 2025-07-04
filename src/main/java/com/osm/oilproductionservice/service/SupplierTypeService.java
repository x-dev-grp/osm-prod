package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.SupplierDto;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.repository.DeliveryRepository;
import com.osm.oilproductionservice.repository.GenericRepository;
import com.osm.oilproductionservice.repository.SupplierInfoTypeRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
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
        return deliveryRepository.countPaidDeliveriesBySupplierId(supplierId);
    }

    // Get count of unpaid payments for a supplier
    public long getUnpaidPaymentsCount(UUID supplierId) {
        return deliveryRepository.countUnpaidDeliveriesBySupplierId(supplierId);
    }

    @Override
    public Set<Action> actionsMapping(Supplier supplier) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE,Action.DELETE));

        return actions;
    }

}
