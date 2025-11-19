package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.OilContainerSaleDto;
import com.osm.oilproductionservice.model.OilContainerSale;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/oil_container_sale")
public class OilContainerSalesController extends BaseControllerImpl<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> {
    public static final String X_TENANT_ID = "X-Tenant-ID";


    public OilContainerSalesController(BaseService<OilContainerSale, OilContainerSaleDto, OilContainerSaleDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);

    }


    @Override
    protected String getResourceName() {
        return "OilContainer".toUpperCase();
    }
}

