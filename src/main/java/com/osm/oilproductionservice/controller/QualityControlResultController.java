package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.QualityControlResultDto;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.osm.oilproductionservice.service.QualityControlResultService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.services.BaseService;
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
        log.debug("Received save-batch request with {} DTOs", dtos.size());
        try {
            List<QualityControlResultDto> savedDtos = qualityControlResultService.saveAll(dtos);
            log.debug("Successfully saved {} DTOs", savedDtos.size());
            ApiResponse<QualityControlResult, QualityControlResultDto> ff = new ApiResponse<>(true, "", savedDtos);
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
        }
    }

    @GetMapping("/fetchByDelivery/{deliveryId}")
    public ResponseEntity<ApiResponse<QualityControlResult, QualityControlResultDto>> getResultsByDelivery(@PathVariable UUID deliveryId) {
        log.debug("Received request to fetch quality control results for deliveryId: {}", deliveryId);
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
        }
    }

//    @PutMapping("/update-batch")
//    public ResponseEntity<ApiResponse<QualityControlResult, QualityControlResultDto>> updateBatch(@RequestBody List<QualityControlResultDto> dtos) {
//        log.debug("Received update-batch request with {} DTOs", dtos.size());
//        try {
//            List<QualityControlResultDto> updatedDtos = qualityControlResultService.updateAll(dtos);
//            log.debug("Successfully updated {} DTOs", updatedDtos.size());
//            ApiResponse<QualityControlResult, QualityControlResultDto> response = new ApiResponse<>(true, "", updatedDtos);
//            return ResponseEntity.ok(response);
//        } catch (IllegalArgumentException e) {
//            log.error("Bad request: {}", e.getMessage());
//            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error updating quality control results: " + e.getMessage(), null));
//        } catch (HttpMessageNotWritableException e) {
//            log.error("Serialization error: {}", e.getMessage(), e);
//            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Error serializing response: " + e.getMessage(), null));
//        } catch (Exception e) {
//            log.error("Unexpected error: {}", e.getMessage(), e);
//            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Unexpected error: " + e.getMessage(), null));
//        }
//    }

    @Override
    protected String getResourceName() {
        return "QualityControlResult".toUpperCase();
    }
}