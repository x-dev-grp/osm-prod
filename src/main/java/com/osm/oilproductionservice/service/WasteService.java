package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.dto.WasteDTO;
import com.osm.oilproductionservice.feignClients.services.FinancialTransactionFeignService;
import com.osm.oilproductionservice.model.UnifiedDelivery;
import com.osm.oilproductionservice.model.Waste;
import com.osm.oilproductionservice.repository.WasteRepository;
import com.xdev.communicator.models.shared.dto.FinancialTransactionDto;
import com.xdev.communicator.models.shared.enums.Currency;
import com.xdev.communicator.models.shared.enums.PaymentMethod;
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

@Service
public class WasteService extends BaseServiceImpl<Waste, WasteDTO, WasteDTO> {

    private final FinancialTransactionFeignService financialTransactionFeignService;
    private final WasteRepository wasteRepository;

    public WasteService(WasteRepository repository,
                        ModelMapper modelMapper,
                        FinancialTransactionFeignService financialTransactionFeignService, WasteRepository wasteRepository) {
        super(repository, modelMapper);
        this.financialTransactionFeignService = financialTransactionFeignService;
        this.wasteRepository = wasteRepository;
    }

    @Override
    public Set<Action> actionsMapping(Waste millMachine) {
        Set<Action> actions = new HashSet<>();
        actions.add(Action.UPDATE);
        actions.add(Action.DELETE);
        actions.add(Action.READ);
        return actions;
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            return;
        }
        Waste waste = wasteRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);
        if (waste == null) {
            throw new IllegalArgumentException("Waste Sale not found for ID: " + paymentDTO.getIdOperation());
        }
        BigDecimal paidAmount = waste.getPaidAmount() != null ? BigDecimal.valueOf(waste.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = waste.getUnpaidAmount() != null ? BigDecimal.valueOf(waste.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        waste.setPaid(payment > 0 && payment == unpaidAmount.doubleValue());
        waste.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
        waste.setUnpaidAmount((unpaidAmount.subtract(BigDecimal.valueOf(payment))).doubleValue());

        wasteRepository.save(waste);
        prepareFinanacalTransaction(paymentDTO, payment, waste, TransactionDirection.INBOUND, TransactionType.WASTE_SALE);

    }
    private void prepareFinanacalTransaction(PaymentDTO paymentDTO, double amount, Waste delivery, TransactionDirection direction, TransactionType transactionType) {
        // Build Financial Transaction DTO
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(transactionType);
        financialTransactionDto.setDirection(direction);
        financialTransactionDto.setAmount(BigDecimal.valueOf(amount));
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount() : null);
        financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber() != null ? paymentDTO.getCheckNumber() : null);
        financialTransactionDto.setLotNumber("N/A");
        financialTransactionDto.setsupplier(paymentDTO.getSupplier() != null ? paymentDTO.getSupplier() : null);
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setApprovedBy(null);

        // Send to finance service
        financialTransactionFeignService.create(financialTransactionDto);
    }
}
