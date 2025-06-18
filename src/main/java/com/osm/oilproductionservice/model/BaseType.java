package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.enums.TypeCategory;
import com.xdev.xdevbase.entities.BaseEntity;
import jakarta.persistence.Entity;

@Entity
public class BaseType extends BaseEntity {
    private String name; // The name of the type (e.g., "Plastic Waste", "Local SupplierInfo")
    private String description; // Description of the type

    private TypeCategory type;


    public BaseType() {

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

    public TypeCategory getType() {
        return type;
    }

    public void setType(TypeCategory type) {
        this.type = type;
    }
}
