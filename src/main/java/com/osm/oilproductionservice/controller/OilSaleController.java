package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.OilSaleDTO;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.model.OilSale;
import com.osm.oilproductionservice.service.OilSaleService;
import com.osm.oilproductionservice.service.OilTransactionService;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
 import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing oil sales
 */
@RestController
@RequestMapping("/api/production/oil_sale")
public class OilSaleController extends BaseControllerImpl<OilSale, OilSaleDTO, OilSaleDTO> {
    private static final Logger log = LoggerFactory.getLogger(OilSaleController.class);

    private final OilSaleService oilSaleService;

    public OilSaleController(BaseService<OilSale, OilSaleDTO, OilSaleDTO> baseService,
                           ModelMapper modelMapper, OilSaleService oilSaleService) {
        super(baseService, modelMapper);
        this.oilSaleService = oilSaleService;
    }
    @Override
    protected String getResourceName() {
        return "OILSALE";
    }
    @PostMapping(
            path = "/payment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        try {
            OilSaleService.OilSalePaymentResponse response = oilSaleService.processPayment(paymentDTO);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException iae) {
            // Domain validation or not-found cases from the service
            log.warn("Payment validation failed: {}", iae.getMessage());
            return badRequest("PAYMENT_INVALID", iae.getMessage());
        } catch (Exception e) {
            // Unexpected server-side error (null arithmetic, etc.)
            log.error("Error processing payment", e);
            return badRequest("PAYMENT_FAILED", e.getMessage());
        }
    }

    // --- Optional: friendlier 400s when JSON/validation fails before hitting the service

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : err.toString())
                .orElse("Validation failed");
        return badRequest("INVALID_REQUEST", firstError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException ex) {
        // Typically enum mismatch or malformed JSON
        return badRequest("MALFORMED_JSON", ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage());
    }

    // --- Helpers

    private ResponseEntity<Map<String, Object>> badRequest(String code, String details) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", code);
        body.put("details", details);
        return ResponseEntity.badRequest().body(body);
    }

}