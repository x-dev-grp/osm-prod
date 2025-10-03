package com.osm.oilproductionservice.feignClients.controllers;

import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.shared.WasteDTO;
import com.xdev.xdevsecurity.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "financeservice",     contextId = "WasteClient",
        path = "/api/finance/transactions", configuration = FeignConfiguration.class)
public interface WasteFeignController extends BaseFeignController<WasteDTO, WasteDTO> {
    // The base controller already provides the standard POST endpoint for create operations
}
