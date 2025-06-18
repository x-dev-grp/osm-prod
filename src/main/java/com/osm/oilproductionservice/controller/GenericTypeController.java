package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.BaseTypeDto;
import com.osm.oilproductionservice.enums.TypeCategory;
import com.osm.oilproductionservice.model.BaseType;
import com.osm.oilproductionservice.service.GenericTypeService;
import com.xdev.xdevbase.apiDTOs.ApiResponse;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.models.OSMModule;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/production/types")
@CrossOrigin
public class GenericTypeController extends
        BaseControllerImpl<BaseType, BaseTypeDto, BaseTypeDto> {
    private final GenericTypeService genericTypeService;

    public GenericTypeController(BaseService<BaseType, BaseTypeDto, BaseTypeDto> baseService, ModelMapper modelMapper, GenericTypeService genericTypeService) {
        super(baseService, modelMapper);
        this.genericTypeService = genericTypeService;
    }


    //     @PostMapping("/")
//    public ResponseEntity<ApiResponse<BaseTypeDto>> createType( @RequestBody BaseTypeDto baseType) {
//        try {
//            BaseTypeDto createdType = genericTypeService.createType(baseType);
//            ApiResponse<BaseTypeDto> response = new ApiResponse<>(true, "Type created successfully", createdType);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            ApiResponse<BaseTypeDto> response = new ApiResponse<>(false, "Error creating type: " + e.getMessage(), null);
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
    // Get all types (e.g., all WasteTypes, SupplierTypes, OliveLotStatusTypes)
    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<BaseType, BaseTypeDto>> getAllTypes(@PathVariable TypeCategory type) {
        try {
            List<BaseType> types = this.genericTypeService.getAllTypes(type);
            List<BaseTypeDto> typeDtos = types.stream().map((element) -> modelMapper.map(element, BaseTypeDto.class)).toList();
            modelMapper.map(types, BaseTypeDto.class);
            ApiResponse<BaseType, BaseTypeDto> response = new ApiResponse<>(true, "Types fetched successfully", typeDtos);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<BaseType, BaseTypeDto> response = new ApiResponse<>(false, "Error fetching types: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Override
    protected String getResourceName() {
        return "BaseType";
    }
}
