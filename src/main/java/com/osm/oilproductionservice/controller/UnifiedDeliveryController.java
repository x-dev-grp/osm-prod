package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.ApiResponse;
import com.osm.oilproductionservice.dto.ExchangePricingDto;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.enums.OliveLotStatus;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/deliveries")

public class UnifiedDeliveryController extends BaseControllerImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    private final UnifiedDeliveryService UnifiedDeliveryService;

    public UnifiedDeliveryController(BaseService<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
    }
    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            this.UnifiedDeliveryService.processPayment(paymentDTO);
            return ResponseEntity.ok().build();
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
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
    }// Get deliveries by supplier ID
    @GetMapping("/getDeliveryByOliveLotNumber/{id}")
    public ResponseEntity<ApiResponse<UnifiedDeliveryDTO>> getDeliveryByOliveLotNumber(@PathVariable UUID id) {
        ApiResponse<UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getByOliveLotNumber(id));
        return ResponseEntity.ok(response);
    } @GetMapping("/getDeliveryByLotNumber/{lotNumber}")
    public ResponseEntity<ApiResponse<UnifiedDeliveryDTO>> getDeliveryByLotNumber(@PathVariable String lotNumber) {
        ApiResponse<UnifiedDeliveryDTO> response = new ApiResponse<>(true, "Deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getByLotNumber(lotNumber));
        return ResponseEntity.ok(response);
    }

    // Get paid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/paid")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getPaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Paid deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getPaidDeliveriesBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/updateStatue/{id}/{status}")
    public ResponseEntity<ApiResponse<Void>> updateStatue(@PathVariable("id") UUID id, @PathVariable("status") OliveLotStatus status) {
        // delegate to your service
       try{
           this.UnifiedDeliveryService.updateStatus(id, status);
           ApiResponse<Void> response = new ApiResponse<>(true, "Status updated successfully", null);
           return ResponseEntity.ok(response);
       } catch (Exception e) {

           throw new RuntimeException(e);
       }


    }@GetMapping("/updateprice/{id}/{updateprice}")
    public ResponseEntity<ApiResponse<Void>> updatePrice(@PathVariable("id") UUID id, @PathVariable("updateprice") Double unitPrice) {
        // delegate to your service
       try{
           this.UnifiedDeliveryService.updateprice(id, unitPrice);
           ApiResponse<Void> response = new ApiResponse<>(true, "price  updated successfully", null);
           return ResponseEntity.ok(response);
       } catch (Exception e) {

           throw new RuntimeException(e);
       }


    }
    // Get unpaid deliveries by supplier ID
    @GetMapping("/supplier/{supplierId}/unpaid")
    public ResponseEntity<ApiResponse<List<UnifiedDeliveryDTO>>> getUnpaidDeliveriesBySupplier(@PathVariable UUID supplierId) {
        ApiResponse<List<UnifiedDeliveryDTO>> response = new ApiResponse<>(true, "Unpaid deliveries for supplier fetched successfully", this.UnifiedDeliveryService.getUnpaidDeliveriesBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }


    @PostMapping("/update-exchange-pricing")
    public ResponseEntity<?> updateExchangePricingAndCreateOilTransactionOut(
            @RequestBody ExchangePricingDto dto) {
      try{
          this.UnifiedDeliveryService.updateExchangePricingAndCreateOilTransactionOut(dto)   ;
          return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", null));
      }catch (Exception e) {
          throw new RuntimeException(e);
      }

    }
    @PostMapping("/update-payment-pricing")
    public ResponseEntity<?> updatePrincingForPaymentreception(
            @RequestBody ExchangePricingDto dto) {
      try{
          this.UnifiedDeliveryService.updatePrincingForPaymentreception(dto)   ;
          return ResponseEntity.ok(new ApiResponse<>(true, "Pricing updated successfully", null));
      }catch (Exception e) {
          throw new RuntimeException(e);
      }

    }
    @Override
    protected String getResourceName() {
        return "DELIVERY".toUpperCase();
    }

}
