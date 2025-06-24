package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.TransporterDTO;
import com.osm.oilproductionservice.model.Transporter;
import com.osm.oilproductionservice.service.TransporterService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/transporter")

public class TransporterController extends BaseControllerImpl<Transporter, TransporterDTO, TransporterDTO> {

    private final TransporterService transporterDTOService;

    public TransporterController(BaseService<Transporter, TransporterDTO, TransporterDTO> baseService, ModelMapper modelMapper, TransporterService transporterDTOService) {
        super(baseService, modelMapper);
        this.transporterDTOService = transporterDTOService;
    }


    @Override
    protected String getResourceName() {
        return "Transporter".toUpperCase();
    }
}
