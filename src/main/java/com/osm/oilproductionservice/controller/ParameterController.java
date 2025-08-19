package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.ParameterDto;
import com.osm.oilproductionservice.model.Parameter;
import com.osm.oilproductionservice.service.ParameterService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/production/parameter")
public class ParameterController extends BaseControllerImpl<Parameter, ParameterDto, ParameterDto> {
    public static final String X_TENANT_ID = "X-Tenant-ID";
    private final ParameterService parameterService;

    public ParameterController(BaseService<Parameter, ParameterDto, ParameterDto> baseService, ModelMapper modelMapper, ParameterService parameterService) {
        super(baseService, modelMapper);
        this.parameterService = parameterService;
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Parameter, ParameterDto>> getByCode(@PathVariable String code, @RequestHeader(X_TENANT_ID) UUID tenantId // or resolve from token
    ) {
         ApiResponse<Parameter, ParameterDto> ff = new ApiResponse<>(true, "", List.of(modelMapper.map(parameterService.getByCode(code, tenantId), ParameterDto.class)));

        return ResponseEntity.ok(ff);
    }

    @Override
    protected String getResourceName() {
        return "Parameter".toUpperCase();
    }
}
