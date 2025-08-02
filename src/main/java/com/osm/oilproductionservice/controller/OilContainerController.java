package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.OilContainerDTO;
import com.osm.oilproductionservice.model.OilContainer;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.service.OilContainerService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/oil_container")
public class OilContainerController extends BaseControllerImpl<OilContainer, OilContainerDTO, OilContainerDTO> {
    public static final String X_TENANT_ID = "X-Tenant-ID";
    private final OilContainerService oilContainerService;

    public OilContainerController(BaseService<OilContainer, OilContainerDTO, OilContainerDTO> baseService, ModelMapper modelMapper, OilContainerService oilContainerService) {
        super(baseService, modelMapper);
        this.oilContainerService = oilContainerService;
    }


    @Override
    protected String getResourceName() {
        return "Parameter";
    }
}

