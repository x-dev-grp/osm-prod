package com.osm.oilproductionservice.feignClients.controllers;

import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import com.xdev.communicator.models.shared.dto.OilCreditDto;
import com.xdev.xdevsecurity.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;


@FeignClient(name = "financeservice",     contextId = "financialTransactionClient",
        path = "/api/finance/transactions", configuration = FeignConfiguration.class)
public interface FinancialTransactionFeignController extends BaseFeignController<FinancialTransactionDto, FinancialTransactionDto> {
    // The base controller already provides the standard POST endpoint for create operations
}
