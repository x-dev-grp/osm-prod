package com.osm.oilproductionservice.feignClients.controllers;

 import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
 import com.xdev.communicator.models.shared.dto.UnifiedDeliveryDTO;
 import com.xdev.xdevsecurity.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(name = "financeservice",     contextId = "UnifiedDeliveryClient",
        path = "/api/finance/transactions", configuration = FeignConfiguration.class)
public interface UnifiedDeliveryFeignController extends BaseFeignController<UnifiedDeliveryDTO, UnifiedDeliveryDTO> {
    // The base controller already provides the standard POST endpoint for create operations
}
