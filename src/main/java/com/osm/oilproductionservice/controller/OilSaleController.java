package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.OilSaleCreateRequest;
import com.osm.oilproductionservice.dto.OilSaleDTO;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.model.OilSale;
import com.osm.oilproductionservice.service.OilSaleService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing oil sales
 */
@RestController
@RequestMapping("/api/production/oil_sale")
public class OilSaleController extends BaseControllerImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleService oilSaleService;

    public OilSaleController(BaseService<OilSale, OilSaleDTO, OilSaleDTO> baseService, ModelMapper modelMapper, OilSaleService oilSaleService) {
        super(baseService, modelMapper);
        this.oilSaleService = oilSaleService;
    }

    @Override
    protected String getResourceName() {
        return "OILSALE".toUpperCase();
    }

    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            oilSaleService.processPayment(paymentDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<OilSale> create(@RequestBody OilSaleCreateRequest request) {
        OilSale saved = oilSaleService.createWithContainers(request);
        return ResponseEntity.ok(saved);
    }

}