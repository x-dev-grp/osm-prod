package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.enums.TransactionType;
import com.osm.oilproductionservice.feignClients.services.OilCeditFeignService;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
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
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", request);
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
        OSMLogger.logMethodExit(this.getClass(), "save", modelMapper.map(oilTransaction, OilTransactionDTO.class));
        OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    public OilTransactionDTO approveOilTransaction(OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "approveOilTransaction", dto);
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
        OSMLogger.logMethodExit(this.getClass(), "approveOilTransaction", modelMapper.map(oilTransaction, OilTransactionDTO.class));
        OSMLogger.logPerformance(this.getClass(), "approveOilTransaction", startTime, System.currentTimeMillis());
        return modelMapper.map(oilTransaction, OilTransactionDTO.class);
    }

    public List<OilTransaction> findByStorageUnitId(UUID storageUnitId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByStorageUnitId", storageUnitId);
        List<OilTransaction> transactions = oilTransactionRepository.findByStorageUnitDestinationId(storageUnitId);
        OSMLogger.logMethodExit(this.getClass(), "findByStorageUnitId", transactions);
        OSMLogger.logPerformance(this.getClass(), "findByStorageUnitId", startTime, System.currentTimeMillis());
        return transactions;
    }

    @Override
    public Set<Action> actionsMapping(OilTransaction oilTransaction) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", oilTransaction);
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
        OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
        return actions;
    }

    // Helper: create exactly one oil reception for a single delivery
    void createSingleOilTransaction(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createSingleOilTransaction", delivery);
        OilTransaction tx = new OilTransaction();
        tx.setStorageUnitDestination(delivery.getStorageUnit());
        tx.setStorageUnitSource(null);
        tx.setTransactionType(TransactionType.RECEPTION_IN);
        tx.setTransactionState(TransactionState.COMPLETED);
        tx.setQuantityKg(delivery.getOilQuantity());
        tx.setUnitPrice(delivery.getUnitPrice());
        tx.setReception(delivery);
        tx.setOilType(delivery.getOilType());

        save(
                modelMapper.map(tx, OilTransactionDTO.class)
        );
        OSMLogger.logMethodExit(this.getClass(), "createSingleOilTransaction", null);
        OSMLogger.logPerformance(this.getClass(), "createSingleOilTransaction", startTime, System.currentTimeMillis());
    }
}
