package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.QualityControlRuleDto;
import com.osm.oilproductionservice.model.QualityControlRule;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import com.xdev.xdevbase.utils.OSMLogger;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/qualitycontrolrules")
public class QualityControlRuleController extends BaseControllerImpl<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> {
    public QualityControlRuleController(BaseService<QualityControlRule, QualityControlRuleDto, QualityControlRuleDto> baseService, ModelMapper modelMapper) {
        super(baseService, modelMapper);
    }

    @Override
    protected String getResourceName() {
        return "QualityControlRule".toUpperCase();
    }


}