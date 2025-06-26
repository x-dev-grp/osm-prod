package com.osm.oilproductionservice.controller;

import com.osm.oilproductionservice.dto.MillMachineDto;
import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.model.MillMachine;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.xdevbase.controllers.impl.BaseControllerImpl;
import com.xdev.xdevbase.services.BaseService;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production/oil_transaction")
public class OilTransactionController extends BaseControllerImpl<OilTransaction, OilTransactionDTO, OilTransactionDTO> {

    private final com.osm.oilproductionservice.service.UnifiedDeliveryService UnifiedDeliveryService;

    public OilTransactionController(BaseService<OilTransaction, OilTransactionDTO, OilTransactionDTO> baseService, ModelMapper modelMapper, UnifiedDeliveryService UnifiedDeliveryService) {
        super(baseService, modelMapper);
        this.UnifiedDeliveryService = UnifiedDeliveryService;
    }

    @Override
    protected String getResourceName() {
        return "OILTRANSACTION";
    }
}