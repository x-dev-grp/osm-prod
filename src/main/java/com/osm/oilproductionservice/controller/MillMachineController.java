package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.MillMachineDto;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/millers")
public class MillMachineController extends BaseControllerImpl<MillMachine, MillMachineDto, MillMachineDto> {

    private final UnifiedDeliveryService UnifiedDeliveryService;

    public MillMachineController(BaseService<MillMachine, MillMachineDto, MillMachineDto> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
    }

    @Override
    protected String getResourceName() {
        return "MillMachine";
    }
}
