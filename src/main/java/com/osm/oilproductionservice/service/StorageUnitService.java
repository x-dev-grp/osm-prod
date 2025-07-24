package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.osm.oilproductionservice.repository.SupplierRepository;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageUnitService extends BaseServiceImpl<StorageUnit, StorageUnitDto, StorageUnitDto> {
    private final StorageUnitRepo storageUnitRepo;
    private final SupplierRepository supplierRepository;
    public StorageUnitService(BaseRepository<StorageUnit> repository, ModelMapper modelMapper, StorageUnitRepo storageUnitRepo, SupplierRepository supplierRepository1) {
        super(repository, modelMapper);
        this.storageUnitRepo = storageUnitRepo;
        this.supplierRepository = supplierRepository1;
    }
    @Transactional
    public void changeSupplier(UUID storageId, UUID supplierId) {
        StorageUnit storageUnit = storageUnitRepo.findById(storageId)
                .orElseThrow(() -> new EntityNotFoundException("Storage unit with id " + storageId + " not found"));

        Supplier currentSupplier = storageUnit.getSupplier();

        if (currentSupplier != null) {
            currentSupplier.setHasStorage(false);
            currentSupplier.setStorageUnit(null);
            storageUnit.setSupplier(null);
            supplierRepository.save(currentSupplier);
        }

        if (supplierId != null) {
            Supplier newSupplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier with id " + supplierId + " not found"));

            newSupplier.setHasStorage(true);
            newSupplier.setStorageUnit(storageUnit);
            storageUnit.setSupplier(newSupplier);

            supplierRepository.save(newSupplier);
        }

        storageUnitRepo.save(storageUnit);
    }



    @Override
    public Set<Action> actionsMapping(StorageUnit storageUnit) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ,Action.ASSIGN_SUPPLIER));

        return actions;
    }
}
