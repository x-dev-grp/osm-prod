package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.service.OilTransactionService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.apiDTOs.ApiSingleResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/production/oil_transaction")
public class OilTransactionController extends BaseControllerImpl<OilTransaction, OilTransactionDTO, OilTransactionDTO> {

     private final OilTransactionService oilTransactionService;

    public OilTransactionController(BaseService<OilTransaction, OilTransactionDTO, OilTransactionDTO> baseService, ModelMapper modelMapper,   OilTransactionService oilTransactionService) {
        super(baseService, modelMapper);
         this.oilTransactionService = oilTransactionService;
    }

    /**
     * GET /api/production/oil_transaction/storage-unit/{storageUnitId}
     * Returns all transactions whose destination tank is the given storage-unit.
     */
    @GetMapping("/storage-unit/{storageUnitId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<OilTransaction, OilTransactionDTO>> getByStorageUnit(
            @PathVariable UUID storageUnitId) {

        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getByStorageUnit", storageUnitId);
        try {
            ApiResponse<OilTransaction, OilTransactionDTO> ff = new ApiResponse<>(true, "", oilTransactionService
                    .findByStorageUnitId(storageUnitId)
                    .stream()
                    .map(this::toSafeDto)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(ff);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "getByStorageUnit", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getByStorageUnit", null);
            OSMLogger.logPerformance(this.getClass(), "getByStorageUnit", startTime, System.currentTimeMillis());
        }
    }

    /**
     * POST /api/production/oil_transaction/create-for-sale
     * Creates an oil transaction for a sale operation
     * Takes oil sale data and creates a corresponding oil transaction
     *
     * @param oilTransactionDTO The oil transaction  data from the frontend form
     * @return ResponseEntity with the created oil transaction
     */
    @PostMapping("/create-for-sale")
    public ResponseEntity<ApiSingleResponse<OilTransaction, OilTransactionDTO>> createOilTransactionForSale(@RequestBody OilTransactionDTO oilTransactionDTO) {
        OSMLogger.logMethodEntry(this.getClass(), "createOilTransactionForSale", oilTransactionDTO);

        try {
            if (oilTransactionDTO.getQuantityKg() == null || oilTransactionDTO.getQuantityKg().doubleValue() <= 0) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Invalid quantity: must be greater than 0", null));
            }

            if (oilTransactionDTO.getUnitPrice() == null || oilTransactionDTO.getUnitPrice().doubleValue() <= 0) {
                return ResponseEntity.badRequest().body(new ApiSingleResponse<>(false, "Invalid unit price: must be greater than 0", null));
            }
            OilTransactionDTO createdTransaction = oilTransactionService.createOilTransactionForSale(oilTransactionDTO);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "Oil transaction created successfully for sale", createdTransaction));

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error creating oil transaction for sale", e);
            return ResponseEntity.internalServerError().body(new ApiSingleResponse<>(false, "Error creating oil transaction for sale: " + e.getMessage(), null));
        }
    }


    @PutMapping("/approve")
    public ResponseEntity<ApiSingleResponse<OilTransaction, OilTransactionDTO>> approveOilTransaction(@RequestBody OilTransactionDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "approveOilTransaction", dto);
        try {
            OilTransactionDTO oilTransaction = oilTransactionService.approveOilTransaction2(dto);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "", oilTransaction));
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "approveOilTransaction", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "approveOilTransaction", null);
            OSMLogger.logPerformance(this.getClass(), "approveOilTransaction", startTime, System.currentTimeMillis());
        }

    }

    @Override
    protected String getResourceName() {
        return "OILTRANSACTION".toUpperCase();
    }

    private OilTransactionDTO toSafeDto(OilTransaction tx) {
        OilTransactionDTO dto = new OilTransactionDTO();
        dto.setId(tx.getId());
        dto.setTransactionType(tx.getTransactionType());
        dto.setTransactionState(tx.getTransactionState());
        dto.setQualityGrade(tx.getQualityGrade());
        dto.setOilType(tx.getOilType());
        dto.setQuantityKg(tx.getQuantityKg());
        dto.setUnitPrice(tx.getUnitPrice());
        dto.setTotalPrice(tx.getTotalPrice());
        dto.setOilSaleId(tx.getOilSaleId());
        dto.setStorageUnitSource(toSafeStorageUnitDto(tx.getStorageUnitSource()));
        dto.setStorageUnitDestination(toSafeStorageUnitDto(tx.getStorageUnitDestination()));
        dto.setReception(toSafeReceptionDto(tx.getReception()));
        return dto;
    }

    private StorageUnitDto toSafeStorageUnitDto(StorageUnit storageUnit) {
        if (storageUnit == null) {
            return null;
        }

        StorageUnitDto dto = new StorageUnitDto();
        dto.setId(storageUnit.getId());

        if (Hibernate.isInitialized(storageUnit)) {
            dto.setName(storageUnit.getName());
            dto.setLotNumber(storageUnit.getLotNumber());
            dto.setCurrentVolume(storageUnit.getCurrentVolume());
            dto.setMaxCapacity(storageUnit.getMaxCapacity());
            dto.setQualityGrade(storageUnit.getQualityGrade());
            dto.setFilteredOil(storageUnit.getFilteredOil());
            dto.setStatus(storageUnit.getStatus());
        }

        return dto;
    }

    private UnifiedDeliveryDTO toSafeReceptionDto(UnifiedDelivery reception) {
        if (reception == null) {
            return null;
        }

        UnifiedDeliveryDTO dto = new UnifiedDeliveryDTO();
        dto.setId(reception.getId());
        if (Hibernate.isInitialized(reception)) {
            dto.setDeliveryNumber(reception.getDeliveryNumber());
            dto.setLotNumber(reception.getLotNumber());
            dto.setDeliveryDate(reception.getDeliveryDate());
            dto.setCategoryOliveOil(reception.getCategoryOliveOil());
        }
        return dto;
    }

    @Override
    public ResponseEntity<?> resolve(String publicCode) {
        return null;
    }
}
