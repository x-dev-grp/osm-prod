package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.service.OilTransactionService;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.apiDTOs.ApiSingleResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/production/oil_transaction")
public class OilTransactionController extends BaseControllerImpl<OilTransaction, OilTransactionDTO, OilTransactionDTO> {

    private final com.osm.oilproductionservice.service.UnifiedDeliveryService UnifiedDeliveryService;
    private final OilTransactionService oilTransactionService;

    public OilTransactionController(BaseService<OilTransaction, OilTransactionDTO, OilTransactionDTO> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService, OilTransactionService oilTransactionService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
        this.oilTransactionService = oilTransactionService;
    }

    /**
     * GET /api/production/oil_transaction/storage-unit/{storageUnitId}
     * Returns all transactions whose destination tank is the given storage-unit.
     */
    @GetMapping("/storage-unit/{storageUnitId}")
    public ResponseEntity<ApiResponse<OilTransaction, OilTransactionDTO>> getByStorageUnit(
            @PathVariable UUID storageUnitId) {

        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getByStorageUnit", storageUnitId);
        try {
            ApiResponse<OilTransaction, OilTransactionDTO> ff = new ApiResponse<>(true, "", oilTransactionService
                    .findByStorageUnitId(storageUnitId)
                    .stream()
                    .map(tx -> modelMapper.map(tx, OilTransactionDTO.class))
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
        return "OILTRANSACTION";
    }
}