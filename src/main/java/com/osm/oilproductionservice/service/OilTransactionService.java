package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.feignClients.services.OilCeditFeignService;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.xdevbase.models.Action;
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
    private final OilCeditFeignService oilCeditFeignService;

    public OilTransactionService(OilTransactionRepository repository, ModelMapper modelMapper, StorageUnitService storageUnitService, StorageUnitRepo storageUnitRepo, OilCeditFeignService oilCeditFeignService) {
        super(repository, modelMapper);
        this.oilTransactionRepository = repository;
        this.storageUnitRepo = storageUnitRepo;
        this.oilCeditFeignService = oilCeditFeignService;
    }


    @Override
    public OilTransactionDTO save(OilTransactionDTO request) {
        OilTransaction oilTransaction = modelMapper.map(request, OilTransaction.class);
        oilTransaction.setTotalPrice();
        oilTransaction = oilTransactionRepository.save(oilTransaction);
        StorageUnit storageUnitDestination = oilTransaction.getStorageUnitDestination();
        StorageUnit storageUnitSource = oilTransaction.getStorageUnitSource();
        if (storageUnitDestination != null) {
            storageUnitDestination.updateCurrentVolume(oilTransaction.getQuantityKg(), 1, oilTransaction.getUnitPrice());
            storageUnitRepo.save(storageUnitDestination);
        }
        if (storageUnitSource != null) {
            storageUnitSource.updateCurrentVolume(oilTransaction.getQuantityKg(), 0, null);
            storageUnitRepo.save(storageUnitSource);
        }

        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    public OilTransactionDTO approveOilTransaction(OilTransactionDTO dto) {
        if (dto == null || dto.getId() == null) return null;
        OilTransaction oilTransaction = oilTransactionRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Oil transaction not found"));
        if (dto.getStorageUnitSource() != null && dto.getStorageUnitSource().getId() != null) {
            StorageUnit storageUnitSource = storageUnitRepo.findById(dto.getStorageUnitSource().getId()).orElse(null);
            if (storageUnitSource != null) {
                oilTransaction.setStorageUnitSource(storageUnitSource);
                oilTransaction.setUnitPrice(storageUnitSource.getAvgCost());
                oilTransaction.setTotalPrice();
                storageUnitSource.updateCurrentVolume(oilTransaction.getQuantityKg(), 0, null);
                storageUnitRepo.save(storageUnitSource);
                oilTransaction.setTransactionState(TransactionState.COMPLETED);
                oilCeditFeignService.approveOilCredit(oilTransaction.getExternalId()).thenAccept(response -> {
                    oilTransactionRepository.save(oilTransaction);
                });
            }
        }
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    public List<OilTransaction> findByStorageUnitId(UUID storageUnitId) {
        return oilTransactionRepository.findByStorageUnitDestinationId(storageUnitId);
    }

    @Override
    public Set<Action> actionsMapping(OilTransaction oilTransaction) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }
}
