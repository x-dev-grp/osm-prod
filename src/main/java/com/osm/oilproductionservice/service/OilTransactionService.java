package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OilTransactionService extends BaseServiceImpl<OilTransaction, OilTransactionDTO, OilTransactionDTO> {
    private final OilTransactionRepository oilTransactionRepository;
    private final StorageUnitRepo storageUnitRepo;
    public OilTransactionService(OilTransactionRepository repository, ModelMapper modelMapper, StorageUnitService storageUnitService, StorageUnitRepo storageUnitRepo) {
        super(repository, modelMapper);
        this.oilTransactionRepository = repository;
        this.storageUnitRepo = storageUnitRepo;
    }


    @Override
    public OilTransactionDTO save(OilTransactionDTO request) {
        OilTransaction oilTransaction = modelMapper.map(request, OilTransaction.class);
        oilTransaction.setTotalPrice();
        oilTransaction = oilTransactionRepository.save(oilTransaction);
        StorageUnit storageUnitDestination = oilTransaction.getStorageUnitDestination();
        StorageUnit storageUnitSource = oilTransaction.getStorageUnitSource();
        if(storageUnitDestination != null ) {
            storageUnitDestination.updateCurrentVolume(oilTransaction.getQuantityKg(),1,oilTransaction.getUnitPrice());
            storageUnitRepo.save(storageUnitDestination);
        }
        if(storageUnitSource != null ) {
            storageUnitSource.updateCurrentVolume(oilTransaction.getQuantityKg(),0,null);
            storageUnitRepo.save(storageUnitSource);
        }

        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }
    public OilTransactionDTO approveOilCredit(OilTransactionDTO request) {


     return null;
    }
    public List<OilTransaction> findByStorageUnitId(UUID storageUnitId) {
        return oilTransactionRepository.findByStorageUnitDestinationId(storageUnitId);
    }
    @Override
    public Set<String> actionsMapping(OilTransaction oilTransaction) {
        Set<String> actions = new HashSet<>();
        actions.add("READ");
        actions.addAll(Set.of("UPDATE", "DELETE"));
        return actions;
    }
}
