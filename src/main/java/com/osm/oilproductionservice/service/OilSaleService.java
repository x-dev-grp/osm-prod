package com.osm.oilproductionservice.service;


import com.osm.oilproductionservice.dto.OilSaleDTO;
import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.enums.TransactionState;
import com.osm.oilproductionservice.feignClients.services.FinancialTransactionFeignService;
import com.osm.oilproductionservice.model.OilSale;
import com.osm.oilproductionservice.model.OilTransaction;
import com.osm.oilproductionservice.repository.OilSaleRepository;
import com.osm.oilproductionservice.repository.OilTransactionRepository;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import com.xdev.communicator.models.shared.enums.TransactionDirection;
import com.xdev.communicator.models.shared.enums.TransactionType;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import com.xdev.communicator.models.common.dtos.apiDTOs.ApiSingleResponse;

/**
 * Service class for managing oil sales operations
 */
@Service
public class OilSaleService extends BaseServiceImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleRepository oilSaleRepository;
    private final OilTransactionService oilTransactionService;
    private final FinancialTransactionFeignService financialTransactionFeignService;
    private final OilTransactionRepository oilTransactionRepository;
    public OilSaleService(OilSaleRepository repository, ModelMapper modelMapper, OilTransactionService oilTransactionService, FinancialTransactionFeignService financialTransactionFeignService, OilTransactionRepository oilTransactionRepository) {
        super(repository, modelMapper);
        this.oilSaleRepository = repository;

        this.oilTransactionService = oilTransactionService;
        this.financialTransactionFeignService = financialTransactionFeignService;
        this.oilTransactionRepository = oilTransactionRepository;
    }

    @Override
    public Set<Action> actionsMapping(OilSale oilSale) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
        return actions;
    }

    @Override
    @Transactional
    public OilSaleDTO save(OilSaleDTO request) {
        OilSale oilSale = modelMapper.map(request, OilSale.class);
        oilSale.setUnpaiedAmount(oilSale.getTotalAmount().doubleValue());
        oilSale = oilSaleRepository.save(oilSale);
        OilTransactionDTO oiltTransactionDto = new OilTransactionDTO();
        oiltTransactionDto.setUnitPrice(request.getUnitPrice().doubleValue());
        oiltTransactionDto.setQuantityKg(request.getQuantity().doubleValue());
        oiltTransactionDto.setTotalPrice(request.getQuantity().doubleValue() * request.getUnitPrice().doubleValue());
        oiltTransactionDto.setOilSaleId(oilSale.getId());
        oilTransactionService.createOilTransactionForSale(oiltTransactionDto);
        return modelMapper.map(oilSale, OilSaleDTO.class);
    }

    @Transactional
    public OilSalePaymentResponse processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO == null) {
            throw new IllegalArgumentException("Payment payload is required");
        }
        if (paymentDTO.getIdOperation() == null) {
            throw new IllegalArgumentException("Operation ID is required");
        }
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount() <= 0d) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        // Load sale
        OilSale oilSale = oilSaleRepository
                .findByIdAndIsDeletedFalse(paymentDTO.getIdOperation())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Oil Sale not found for ID: " + paymentDTO.getIdOperation()
                ));
        // Check if already fully paid
        if (oilSale.isPaid() || nvl(oilSale.getUnpaiedAmount()) <= 0d) {
            throw new IllegalArgumentException("This sale is already fully paid");
        }
        // Check related oil transaction state
        OilTransaction relatedTx = oilTransactionRepository.findFirstByOilSaleIdOrderByCreatedDateDesc(oilSale.getId())
                .orElseThrow(() -> new IllegalArgumentException("No oil transaction found for this sale"));
        if (relatedTx.getTransactionState() != com.osm.oilproductionservice.enums.TransactionState.COMPLETED) {
            throw new IllegalArgumentException("Cannot pay: related oil transaction is not COMPLETED");
        }
        // Validate payment method fields
        if (paymentDTO.getPaymentMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        switch (paymentDTO.getPaymentMethod()) {
            case CHEQUE -> {
                if (paymentDTO.getCheckNumber() == null || paymentDTO.getCheckNumber().isEmpty()) {
                    throw new IllegalArgumentException("Check number is required for cheque payments");
                }
            }
            case TRANSFER -> {
                if (paymentDTO.getBankAccount() == null) {
                    throw new IllegalArgumentException("Bank account is required for transfer payments");
                }
            }
            default -> {}
        }
        // Null-safe numbers
        double total       = oilSale.getTotalAmount().doubleValue();
        double alreadyPaid = nvl(oilSale.getPaiedAmount());
        double unpaid      = Math.max(0d, total - alreadyPaid);
        double incoming    = paymentDTO.getAmount();
        if (incoming > unpaid) {
            throw new IllegalArgumentException("Amount exceeds unpaid amount");
        }
        // Compute new balances
        double newPaid   = alreadyPaid + incoming;
        double newUnpaid = Math.max(0d, total - newPaid);
        boolean fullyPaid = newUnpaid == 0d;
        // Persist sale state
        oilSale.setPaiedAmount(newPaid);
        oilSale.setUnpaiedAmount(newUnpaid);
        oilSale.setPaid(fullyPaid);
        oilSaleRepository.save(oilSale);
        // Create the financial transaction with the ACTUAL applied amount
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setAmount(java.math.BigDecimal.valueOf(incoming));
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setCurrency(paymentDTO.getCurrency());
        financialTransactionDto.setDirection(TransactionDirection.INBOUND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod());
        if (paymentDTO.getBankAccount() != null) {
            financialTransactionDto.setBankAccount(paymentDTO.getBankAccount());
        }
        if (paymentDTO.getCheckNumber() != null) {
            financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber());
        }
        financialTransactionDto.setTransactionDate(java.time.LocalDateTime.now());
        // Optional cross-reference for traceability:
        financialTransactionDto.setExternalTransactionId(oilSale.getId().toString());
        // Call Feign client and handle CompletableFuture response
        try {
            CompletableFuture<ApiSingleResponse<FinancialTransactionDto>> future = financialTransactionFeignService.create(financialTransactionDto);
            ApiSingleResponse<FinancialTransactionDto> response = future.get(); // Wait for the result
            FinancialTransactionDto createdTx = response.getData(); // Extract the actual DTO from the response
            // Return both updated sale and created transaction
            return new OilSalePaymentResponse(modelMapper.map(oilSale, OilSaleDTO.class), createdTx);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create financial transaction", e);
        }
    }
    private static double nvl(Double v) { return v == null ? 0d : v; }

    public static class OilSalePaymentResponse {
        private final OilSaleDTO oilSale;
        private final FinancialTransactionDto transaction;
        public OilSalePaymentResponse(OilSaleDTO oilSale, FinancialTransactionDto transaction) {
            this.oilSale = oilSale;
            this.transaction = transaction;
        }
        public OilSaleDTO getOilSale() { return oilSale; }
        public FinancialTransactionDto getTransaction() { return transaction; }
    }
}