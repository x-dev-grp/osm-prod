package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.SupplierDto;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.service.SupplierTypeService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/production/suppliers_type")
public class SupplierTypeController extends BaseControllerImpl<Supplier, SupplierDto, SupplierDto> {

    private final SupplierTypeService supplierTypeService;

    public SupplierTypeController(BaseService<Supplier, SupplierDto, SupplierDto> baseService, ModelMapper modelMapper, SupplierTypeService supplierTypeService) {
        super(baseService, modelMapper);
        this.supplierTypeService = supplierTypeService;
    }

    // Get count of paid payments for a supplier
    @GetMapping("/{supplierId}/payments/paid/count")
    public ResponseEntity<Long> getPaidPaymentsCount(@PathVariable UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPaidPaymentsCount", supplierId);
        try {
            long count = supplierTypeService.getPaidPaymentsCount(supplierId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "getPaidPaymentsCount", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getPaidPaymentsCount", null);
            OSMLogger.logPerformance(this.getClass(), "getPaidPaymentsCount", startTime, System.currentTimeMillis());
        }
    }

    // Get count of unpaid payments for a supplier
    @GetMapping("/{supplierId}/payments/unpaid/count")
    public ResponseEntity<Long> getUnpaidPaymentsCount(@PathVariable UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getUnpaidPaymentsCount", supplierId);
        try {
            long count = supplierTypeService.getUnpaidPaymentsCount(supplierId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "getUnpaidPaymentsCount", e);
            throw e;
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "getUnpaidPaymentsCount", null);
            OSMLogger.logPerformance(this.getClass(), "getUnpaidPaymentsCount", startTime, System.currentTimeMillis());
        }
    }

    @Override
    protected String getResourceName() {
        return "Supplier".toUpperCase();
    }
}
