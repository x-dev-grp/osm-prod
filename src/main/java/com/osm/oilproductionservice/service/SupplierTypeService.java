package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.SupplierDto;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.repository.GenericRepository;
import com.osm.oilproductionservice.repository.SupplierInfoTypeRepository;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class SupplierTypeService extends BaseServiceImpl<Supplier, SupplierDto, SupplierDto> {

    // Repository to reattach BaseType entities (for region and supplier type)
    private final GenericRepository baseTypeRepository;
    private final SupplierInfoTypeRepository supplierInfoRepository;

    // Constructor injection: in addition to Supplier repository and ModelMapper,
    // inject the BaseType repository.
    public SupplierTypeService(BaseRepository<Supplier> repository,
                               ModelMapper modelMapper,
                               GenericRepository baseTypeRepository, SupplierInfoTypeRepository supplierInfoRepository) {
        super(repository, modelMapper);
        this.baseTypeRepository = baseTypeRepository;
        this.supplierInfoRepository = supplierInfoRepository;
    }

    @Override
    public Set<String> actionsMapping(Supplier supplier) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }

}
