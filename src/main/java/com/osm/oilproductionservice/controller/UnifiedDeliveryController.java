package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.ApiResponse;
import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/deliveries")

public class UnifiedDeliveryController extends BaseControllerImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private final UnifiedDeliveryService UnifiedDeliveryService;

    public UnifiedDeliveryController(BaseService<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
    }


    @GetMapping("/planning")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getPlanning() {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Delleveirs for planning fetched  successfully", this.UnifiedDeliveryService.getForPlanning());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/findForQuality")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getDeliveriesWithoutQualityControl(@RequestParam("types") String types) {
        List<String> typeList = Arrays.asList(types.split(","));
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Delleveirs for planning fetched  successfully", this.UnifiedDeliveryService.findByDeliveryTypeInAndQualityControlResultsIsNull(typeList));

        return ResponseEntity.ok(response);
    }

    // Get deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getDeliveriesBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }

    // Get paid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/paid")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getPaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Paid deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getPaidDeliveriesBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }

    // Get unpaid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/unpaid")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getUnpaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Unpaid deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getUnpaidDeliveriesBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }

    @Override
    protected String getResourceName() {
        return "UNIFIEDDELIVERY".toUpperCase();
    }

}
