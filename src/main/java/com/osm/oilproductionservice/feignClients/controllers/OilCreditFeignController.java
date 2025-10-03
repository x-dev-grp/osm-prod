package com.osm.oilproductionservice.feignClients.controllers;

import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.shared.OilCreditDto;
import com.xdev.xdevsecurity.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;


@FeignClient(name = "financeservice",    contextId = "oilCreditClient",
        path = "/api/finance/oil-credit", configuration = FeignConfiguration.class)
public interface OilCreditFeignController extends BaseFeignController<OilCreditDto, OilCreditDto> {
    @PutMapping("/{transactionId}/approve")
    ResponseEntity<Void> approveOilCredit(@PathVariable UUID transactionId);
}
