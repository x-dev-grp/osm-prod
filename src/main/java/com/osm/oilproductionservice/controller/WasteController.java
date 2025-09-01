package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.dto.WasteDTO;
import com.osm.oilproductionservice.model.Waste;
import com.osm.oilproductionservice.service.WasteService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/waste")
public class WasteController extends BaseControllerImpl<Waste, WasteDTO, WasteDTO> {
    public static final String X_TENANT_ID = "X-Tenant-ID";
    private final WasteService wasteService;

    public WasteController(BaseService<Waste, WasteDTO, WasteDTO> baseService, ModelMapper modelMapper, WasteService wasteService) {
        super(baseService, modelMapper);
        this.wasteService = wasteService;
    }
    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            wasteService.processPayment(paymentDTO);
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @Override
    protected String getResourceName() {
        return "WASTE".toUpperCase();
    }
}

