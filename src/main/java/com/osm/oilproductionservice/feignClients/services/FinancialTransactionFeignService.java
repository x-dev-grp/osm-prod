package com.osm.oilproductionservice.feignClients.services;


import com.osm.oilproductionservice.feignClients.controllers.FinancialTransactionFeignController;
import com.osm.oilproductionservice.feignClients.controllers.OilCreditFeignController;
import com.xdev.communicator.exceptions.ServiceException;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.feignServices.BaseFeignService;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class FinancialTransactionFeignService extends BaseFeignService<FinancialTransactionDto, FinancialTransactionDto> {
    final private FinancialTransactionFeignController financialTransactionFeignController;
    
    public FinancialTransactionFeignService(BaseFeignController<FinancialTransactionDto, FinancialTransactionDto> baseFeignController, OilCreditFeignController oilCreditFeignController, FinancialTransactionFeignController financialTransactionFeignController) {
        super(baseFeignController);
        this.financialTransactionFeignController = financialTransactionFeignController;
    }

    // The base service already provides the create method that returns CompletableFuture<ApiSingleResponse<FinancialTransactionDto>>
    // No need for custom methods
}
