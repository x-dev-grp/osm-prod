package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.MachinePlanDto;
import com.osm.oilproductionservice.model.MachinePlan;
import com.osm.oilproductionservice.service.MachinePlanService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

public class MachinePlanController extends BaseControllerImpl<MachinePlan, MachinePlanDto, MachinePlanDto> {

    private final MachinePlanService machinePlanService;

    public MachinePlanController(BaseService<MachinePlan, MachinePlanDto, MachinePlanDto> baseService,
                                 ModelMapper modelMapper,
                                 MachinePlanService machinePlanService) {
        super(baseService, modelMapper);
        this.machinePlanService = machinePlanService;
    }

    @Override
    protected String getResourceName() {
        return "MachinePlan".toUpperCase();
    }
}
