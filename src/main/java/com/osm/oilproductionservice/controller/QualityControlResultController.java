package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.QualityControlResultDto;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.osm.oilproductionservice.service.OilTransactionService;
import com.osm.oilproductionservice.service.QualityControlResultService;
import com.osm.oilproductionservice.service.StorageUnitService;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/qualitycontrolresult")
public class QualityControlResultController extends BaseControllerImpl<QualityControlResult, QualityControlResultDto, QualityControlResultDto> {

    private static final Logger log = LoggerFactory.getLogger(QualityControlResultController.class);
    private final QualityControlResultService qualityControlResultService;


    public QualityControlResultController(BaseService<QualityControlResult, QualityControlResultDto, QualityControlResultDto> baseService, ModelMapper modelMapper, QualityControlResultService qualityControlResultService) {
        super(baseService, modelMapper);
        this.qualityControlResultService = qualityControlResultService;

    }

    @PostMapping("/save-batch")
    public ResponseEntity<ApiResponse<QualityControlResult, QualityControlResultDto>> saveBatch(@RequestBody List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveBatch", dtos.size());
        try {

            ApiResponse<QualityControlResult, QualityControlResultDto> ff = new ApiResponse<>(true, "", qualityControlResultService.saveAll(dtos));
            return ResponseEntity.ok(ff);
        } catch (IllegalArgumentException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error saving quality control results: " + e.getMessage(), null));
        } catch (HttpMessageNotWritableException e) {
            log.error("Serialization error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error serializing response: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "saveBatch", null);
            OSMLogger.logPerformance(this.getClass(), "saveBatch", startTime, System.currentTimeMillis());
        }
    }

    @PostMapping("/save-batch-direct/{idx}")
    public ResponseEntity<ApiResponse<QualityControlResult, QualityControlResultDto>> saveBatchDirect(
            @PathVariable UUID idx,
            @RequestBody List<QualityControlResultDto> dtos) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "saveBatchDirect", dtos.size());
        try {
            // Call a new service method (to be implemented) that saves results for idx
            ApiResponse<QualityControlResult, QualityControlResultDto> response = new ApiResponse<>(true, "", qualityControlResultService.saveOilQcForOliveRec(idx, dtos));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error saving quality control results: " + e.getMessage(), null));
        } catch (HttpMessageNotWritableException e) {
            log.error("Serialization error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error serializing response: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "saveBatchDirect", null);
            OSMLogger.logPerformance(this.getClass(), "saveBatchDirect", startTime, System.currentTimeMillis());
        }
    }

    @GetMapping("/fetchByDelivery/{deliveryId}")
    public ResponseEntity<ApiResponse<QualityControlResult, QualityControlResultDto>> getResultsByDelivery(@PathVariable UUID deliveryId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getResultsByDelivery", deliveryId);
        try {
            List<QualityControlResultDto> results = qualityControlResultService.findByDeliveryId(deliveryId);
            log.debug("Successfully fetched {} results for deliveryId: {}", results.size(), deliveryId);
            return ResponseEntity.ok(new ApiResponse<>(true, "", results));
        } catch (IllegalArgumentException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error fetching quality control results: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getResultsByDelivery", null);
            OSMLogger.logPerformance(this.getClass(), "getResultsByDelivery", startTime, System.currentTimeMillis());
        }
    }
    @Override
    protected String getResourceName() {
        return "QUALITY_CONTROL_RESULT";
    }
}