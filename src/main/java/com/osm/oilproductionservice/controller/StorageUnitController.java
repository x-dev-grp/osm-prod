package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.model.StorageUnit;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/storage-units")

public class StorageUnitController extends BaseControllerImpl<StorageUnit, StorageUnitDto, StorageUnitDto> {
    public StorageUnitController(BaseService<StorageUnit, StorageUnitDto, StorageUnitDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "StorageUnit".toUpperCase();
    }
}
