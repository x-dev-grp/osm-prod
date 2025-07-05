package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.service.OilTransactionService;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.apiDTOs.ApiSingleResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
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

        ApiResponse<OilTransaction, OilTransactionDTO> ff = new ApiResponse<>(true, "", oilTransactionService
                .findByStorageUnitId(storageUnitId)
                .stream()
                .map(tx -> modelMapper.map(tx, OilTransactionDTO.class))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(ff);
    }

    @PutMapping("/approve")
    public ResponseEntity<ApiSingleResponse<OilTransaction, OilTransactionDTO>> approveOilTransaction(@RequestBody OilTransactionDTO dto) {
        try {
            OilTransactionDTO oilTransaction = oilTransactionService.approveOilTransaction(dto);
            return ResponseEntity.ok(new ApiSingleResponse<>(true, "", oilTransaction));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }

    }

    @Override
    protected String getResourceName() {
        return "OILTRANSACTION";
    }
}