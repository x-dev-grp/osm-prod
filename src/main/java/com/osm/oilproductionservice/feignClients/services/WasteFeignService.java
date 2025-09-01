package com.osm.oilproductionservice.feignClients.services;


import com.osm.oilproductionservice.feignClients.controllers.UnifiedDeliveryFeignController;
import com.osm.oilproductionservice.feignClients.controllers.WasteFeignController;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.feignServices.BaseFeignService;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import org.springframework.stereotype.Service;

@Service
public class WasteFeignService extends BaseFeignService<FinancialTransactionDto, FinancialTransactionDto> {
    final private WasteFeignController wasteFeignController;

    public WasteFeignService(BaseFeignController<FinancialTransactionDto, FinancialTransactionDto> baseFeignController, WasteFeignController wasteFeignController) {
        super(baseFeignController);
        this.wasteFeignController = wasteFeignController;
    }

    // The base service already provides the create method that returns CompletableFuture<ApiSingleResponse<FinancialTransactionDto>>
    // No need for custom methods
}
