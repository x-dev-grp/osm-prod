package com.osm.oilproductionservice.feignClients.services;


import com.osm.oilproductionservice.feignClients.controllers.OilCreditFeignController;
import com.xdev.communicator.exceptions.ServiceException;
import com.xdev.communicator.feignControllers.BaseFeignController;
import com.xdev.communicator.feignServices.BaseFeignService;
import com.xdev.communicator.models.shared.dto.OilCreditDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OilCeditFeignService extends BaseFeignService<OilCreditDto, OilCreditDto> {
    private final OilCreditFeignController oilCreditFeignController;

    public OilCeditFeignService(BaseFeignController<OilCreditDto, OilCreditDto> baseFeignController, OilCreditFeignController oilCreditFeignController) {
        super(baseFeignController);
        this.oilCreditFeignController = oilCreditFeignController;
    }

    @CircuitBreaker(name = "genericService", fallbackMethod = "approveOilCreditFallBack")
    @Retry(name = "genericService")
    @TimeLimiter(name = "genericService")
    public CompletableFuture<ResponseEntity<Void>> approveOilCredit(UUID id) {
        if (super.feignExecutor != null) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return oilCreditFeignController.approveOilCredit(id);
                } catch (Exception e) {
                    log.error("Error approving oilCredit by id: {}", id, e);
                    throw new ServiceException("Error approving oilCredit by id", e);
                }
            }, feignExecutor);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return oilCreditFeignController.approveOilCredit(id);
                } catch (Exception e) {
                    log.error("Error approving oilCredit by id: {}", id, e);
                    throw new ServiceException("Error approving oilCredit by id", e);
                }
            });
        }
    }

    public CompletableFuture<?> approveOilCreditFallBack(UUID id, Exception ex) {
        log.warn("Fallback approving oilCredit by id: {}, error: {}", id, ex.getMessage());
        return CompletableFuture.completedFuture(
                null
        );
    }


}
