package com.osm.oilproductionservice.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.osm.oilproductionservice.model.QualityControlResult;
import com.xdev.xdevbase.dtos.BaseDto;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link com.osm.oilproductionservice.model.QualityControlResult}
 */
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QualityControlResultDto extends BaseDto<QualityControlResult> implements Serializable {

    QualityControlRuleDto rule;
    String measuredValue;
    private UUID deliveryId;



    public QualityControlRuleDto getRule() {
        return rule;
    }

    public void setRule(QualityControlRuleDto rule) {
        this.rule = rule;
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(String measuredValue) {
        this.measuredValue = measuredValue;
    }

    public UUID getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }
}