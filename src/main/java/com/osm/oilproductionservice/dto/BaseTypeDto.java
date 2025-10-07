package com.osm.oilproductionservice.dto;

import com.xdev.communicator.models.enums.TypeCategory;
import com.osm.oilproductionservice.model.BaseType;
import com.xdev.xdevbase.dtos.BaseDto;


public class BaseTypeDto extends BaseDto<BaseType> {
    private String name; // The name of the type (e.g., "Plastic Waste", "Local SupplierInfo")
    private String description; // Description of the type

    private TypeCategory type;

    public BaseTypeDto() {
    }


    public TypeCategory getType() {
        return type;
    }

    public void setType(TypeCategory type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}