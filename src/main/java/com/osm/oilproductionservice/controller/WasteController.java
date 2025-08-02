package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.WasteDTO;
import com.osm.oilproductionservice.model.Waste;
import com.osm.oilproductionservice.service.WasteService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
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


    @Override
    protected String getResourceName() {
        return "WASTE";
    }
}

