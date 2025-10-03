package com.osm.oilproductionservice.feignClients.services;


import com.osm.oilproductionservice.feignClients.controllers.FinancialTransactionFeignController;
import com.osm.oilproductionservice.feignClients.controllers.OilCreditFeignController;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.feignServices.BaseFeignService;
import com.xdev.communicator.models.shared.FinancialTransactionDto;
import org.springframework.stereotype.Service;

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
