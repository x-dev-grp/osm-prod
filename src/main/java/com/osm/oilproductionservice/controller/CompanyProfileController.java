package com.osm.oilproductionservice.controller;


import com.osm.oilproductionservice.dto.CompanyProfileDTO;
import com.osm.oilproductionservice.model.CompanyProfile;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/production/company-profile")

public class CompanyProfileController extends BaseControllerImpl<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> {


    public CompanyProfileController(BaseService<CompanyProfile, CompanyProfileDTO, CompanyProfileDTO> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "CompanyProfile";
    }


}
