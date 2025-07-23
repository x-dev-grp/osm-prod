package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.service.StorageUnitService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/production/storage-units")

public class StorageUnitController extends BaseControllerImpl<StorageUnit, StorageUnitDto, StorageUnitDto> {
    private final StorageUnitService storageUnitService;
    public StorageUnitController(BaseService<StorageUnit, StorageUnitDto, StorageUnitDto> baseService, ModelMapper modelMapper, StorageUnitService storageUnitService) {
        super(baseService, modelMapper);
        this.storageUnitService = storageUnitService;
    }
    @PutMapping("/{storageId}/assign-supplier")
    public ResponseEntity<Void> changeSupplier(
            @PathVariable UUID storageId,
            @RequestParam(required = false) UUID supplierId) {
        storageUnitService.changeSupplier(storageId, supplierId);
        return ResponseEntity.noContent().build();
    }
    @Override
    protected String getResourceName() {
        return "StorageUnit".toUpperCase();
    }
}
