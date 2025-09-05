package com.osm.oilproductionservice.service;


import com.osm.oilproductionservice.dto.OilSaleDTO;
import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.feignClients.services.FinancialTransactionFeignService;
import com.osm.oilproductionservice.model.OilSale;
import com.osm.oilproductionservice.repository.OilSaleRepository;
 import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import com.xdev.communicator.models.shared.enums.TransactionDirection;
import com.xdev.communicator.models.shared.enums.TransactionType;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Service class for managing oil sales operations
 */
@Service
public class OilSaleService extends BaseServiceImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleRepository oilSaleRepository;
    private final OilTransactionService oilTransactionService;
    private final FinancialTransactionFeignService financialTransactionFeignService;
    public OilSaleService(OilSaleRepository repository, ModelMapper modelMapper, OilTransactionService oilTransactionService, FinancialTransactionFeignService financialTransactionFeignService
    ) {
        super(repository, modelMapper);
        this.oilSaleRepository = repository;

        this.oilTransactionService = oilTransactionService;
        this.financialTransactionFeignService = financialTransactionFeignService;
    }

    @Override
    public Set<Action> actionsMapping(OilSale oilSale) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ,Action.GEN_INVOICE));
        return actions;
    }

    @Override
    @Transactional
    public OilSaleDTO save(OilSaleDTO request) {
        OilSale oilSale = modelMapper.map(request, OilSale.class);
        oilSale.setUnpaidAmount(oilSale.getTotalAmount().doubleValue());
        oilSale = oilSaleRepository.save(oilSale);
        OilTransactionDTO oiltTransactionDto = new OilTransactionDTO();
        oiltTransactionDto.setUnitPrice(request.getUnitPrice().doubleValue());
        oiltTransactionDto.setQuantityKg(request.getQuantity().doubleValue());
        oiltTransactionDto.setTotalPrice(request.getQuantity().doubleValue() * request.getUnitPrice().doubleValue());
        oiltTransactionDto.setOilSaleId(oilSale.getId());
        oiltTransactionDto.setOilSaleId(oilSale.getId());
        oilTransactionService.createOilTransactionForSale(oiltTransactionDto);
        return modelMapper.map(oilSale, OilSaleDTO.class);
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            return;
        }
        OilSale oilSale = oilSaleRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (oilSale == null) {
            throw new IllegalArgumentException("Oil Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = oilSale.getPaiedAmount() != null ? BigDecimal.valueOf(oilSale.getPaiedAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = oilSale.getUnpaidAmount() != null ? BigDecimal.valueOf(oilSale.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        oilSale.setPaid(payment > 0 && payment == unpaidAmount.doubleValue());
        oilSale.setPaiedAmount( (paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        oilSale.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        oilSaleRepository.save(oilSale);
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setAmount(BigDecimal.valueOf(paymentDTO.getAmount()));
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        financialTransactionDto.setApproved(true);
         financialTransactionDto.setCurrency(paymentDTO.getCurrency());
        financialTransactionDto.setDirection(TransactionDirection.INBOUND);
        if (paymentDTO.getBankAccount() != null) {
            financialTransactionDto.setBankAccount(paymentDTO.getBankAccount());
        }
        if (paymentDTO.getCheckNumber() != null) {
            financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber());
        }
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionFeignService.create(financialTransactionDto);

    }
}