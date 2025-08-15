package com.osm.oilproductionservice.feignClients.services;


 import com.osm.oilproductionservice.feignClients.controllers.FinancialTransactionFeignController;
import com.osm.oilproductionservice.feignClients.controllers.OilCreditFeignController;
import com.osm.oilproductionservice.feignClients.controllers.UnifiedDeliveryFeignController;
import com.osm.oilproductionservice.service.UnifiedDeliveryService;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.feignServices.BaseFeignService;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
 import com.xdev.communicator.models.shared.dto.UnifiedDeliveryDTO;
 import org.springframework.stereotype.Service;

@Service
public class UnifiedDeliveryFeignService extends BaseFeignService<FinancialTransactionDto, FinancialTransactionDto> {
    final private UnifiedDeliveryFeignController unifiedDeliveryFeignController;

    public UnifiedDeliveryFeignService(BaseFeignController<FinancialTransactionDto, FinancialTransactionDto> baseFeignController,  UnifiedDeliveryFeignController unifiedDeliveryFeignController) {
        super(baseFeignController);
        this.unifiedDeliveryFeignController = unifiedDeliveryFeignController;
    }

    // The base service already provides the create method that returns CompletableFuture<ApiSingleResponse<FinancialTransactionDto>>
    // No need for custom methods
}
