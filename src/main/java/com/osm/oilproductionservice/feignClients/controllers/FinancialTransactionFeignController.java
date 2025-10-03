package com.osm.oilproductionservice.feignClients.controllers;

import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.shared.FinancialTransactionDto;
import com.xdev.xdevsecurity.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "financeservice",     contextId = "financialTransactionClient",
        path = "/api/finance/transactions", configuration = FeignConfiguration.class)
public interface FinancialTransactionFeignController extends BaseFeignController<FinancialTransactionDto, FinancialTransactionDto> {
    // The base controller already provides the standard POST endpoint for create operations
}
