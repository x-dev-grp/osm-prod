package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.OilSaleCreateRequest;
import com.osm.oilproductionservice.dto.OilSaleDTO;
import com.osm.oilproductionservice.dto.OilTransactionDTO;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.feignClients.services.FinancialTransactionFeignService;
import com.osm.oilproductionservice.model.*;
import com.osm.oilproductionservice.repository.*;
import com.xdev.communicator.models.enums.*;
import com.xdev.communicator.models.shared.FinancialTransactionDto;
import com.xdev.communicator.models.shared.SupplierDto;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import jakarta.validation.ValidationException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.xdev.communicator.models.enums.OperationType.OIL_CONTAINER_SALE;
import static com.xdev.communicator.models.enums.OperationType.OIL_SALE;

/**
 * Minimal service that:
 * - validates/locks inventory
 * - persists OilSale + container lines
 * - adjusts inventory
 * - creates FinancialTransactionDto entries (revenue + optional payment)
 */
@Service
public class OilSaleService extends BaseServiceImpl<OilSale, OilSaleDTO, OilSaleDTO> {

    private final OilSaleRepository oilSaleRepository;

    private final StorageUnitRepo storageUnitRepo;
    private final OilContainerRepository containerRepo;
    private final OilContainerSaleRepo lineRepo;
    private final SupplierRepository supplierRepo;
    private final FinancialTransactionFeignService financialTransactionFeignService;
    private final ModelMapper modelMapper;
    private final OilContainerRepository oilContainerRepository;
    private final OilTransactionService oilTransactionService;

    public OilSaleService(OilSaleRepository oilSaleRepository, StorageUnitRepo storageUnitRepo, OilContainerRepository containerRepo, OilContainerSaleRepo lineRepo, SupplierRepository supplierRepo, FinancialTransactionFeignService financialTransactionFeignService, ModelMapper modelMapper, OilContainerRepository oilContainerRepository, OilTransactionService oilTransactionService) {
        super(oilSaleRepository, modelMapper);

        this.oilSaleRepository = oilSaleRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.containerRepo = containerRepo;
        this.lineRepo = lineRepo;
        this.supplierRepo = supplierRepo;
        this.financialTransactionFeignService = financialTransactionFeignService;
        this.modelMapper = modelMapper;
        this.oilContainerRepository = oilContainerRepository;
        this.oilTransactionService = oilTransactionService;
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
        BigDecimal paidAmount = oilSale.getPaidAmount() != null ? BigDecimal.valueOf(oilSale.getPaidAmount()) : BigDecimal.ZERO;
        BigDecimal unpaidAmount = oilSale.getUnpaidAmount() != null ? BigDecimal.valueOf(oilSale.getUnpaidAmount()) : BigDecimal.ZERO;

        double payment = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        oilSale.setPaid(payment > 0 && payment == unpaidAmount.doubleValue());
        if (payment > 0 && payment == unpaidAmount.doubleValue()) {
            oilSale.setStatus(SaleStatus.DELIVERED);
        }
        oilSale.setPaidAmount((paidAmount.add(BigDecimal.valueOf(payment))).doubleValue());
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
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setLotNumber(null);
        financialTransactionDto.setsupplier(paymentDTO.getSupplier() != null ? modelMapper.map(paymentDTO.getSupplier(), SupplierDto.class) : null);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(OIL_SALE);
        financialTransactionDto.setExternalTransactionId(oilSale.getId().toString());
        financialTransactionFeignService.create(financialTransactionDto);

    }

    @Transactional
    public OilSaleDTO createWithContainers(OilSaleCreateRequest req) {

        // ---- 0) Basic validation
        if (req.getQuantity() == null || req.getQuantity().signum() <= 0) {
            throw new ValidationException("quantity must be > 0");
        }
        if (req.getUnitPrice() == null || req.getUnitPrice().signum() < 0) {
            throw new ValidationException("unitPrice must be >= 0");
        }
        if (req.getSaleDate() == null) {
            throw new ValidationException("saleDate is required");
        }

        // Supplier is optional
        Supplier supplier = null;
        if (req.getSupplier() != null && !req.getSupplier().isBlank()) {
            supplier = supplierRepo.findByIdAndIsDeletedFalse(UUID.fromString(req.getSupplier())).orElse(null);
        }

        // ---- 1) Load storage unit & check existence
        StorageUnit su = storageUnitRepo.findByIdAndIsDeletedFalse(UUID.fromString(req.getStorageUnit())).orElse(null);
        if (su == null) {
            throw new IllegalArgumentException("Storage unit not found: " + req.getStorageUnit());
        }

        // ---- 2) Lock storage unit and check oil stock
        BigDecimal suQty = BigDecimal.valueOf(su.getCurrentVolume());
        if (suQty.compareTo(req.getQuantity()) < 0) {
            throw new IllegalStateException("Insufficient oil in storage unit");
        }

        // ---- 3) Branch on containers presence
        final boolean hasContainers = req.getContainerSales() != null && !req.getContainerSales().isEmpty();

        BigDecimal containerTotal = BigDecimal.ZERO;
        if (hasContainers) {
            // Compute total for containers only when list is provided
            for (var line : req.getContainerSales()) {
                OilContainer c = containerRepo.findById(line.getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + line.getId()));
                BigDecimal lineTotal = c.getSellingPrice().multiply(BigDecimal.valueOf(line.getCount()));
                containerTotal = containerTotal.add(lineTotal);
            }
        }

        // ---- 4) Oil totals
        BigDecimal oilTotal = req.getUnitPrice().multiply(req.getQuantity());
        BigDecimal saleTotal = oilTotal.add(containerTotal);

        BigDecimal paid = (req.getPaidAmount() == null) ? BigDecimal.ZERO : BigDecimal.valueOf(req.getPaidAmount());
        if (paid.signum() < 0) paid = BigDecimal.ZERO;
        if (paid.compareTo(saleTotal) > 0) paid = saleTotal;

        BigDecimal unpaid = saleTotal.subtract(paid);

        // ---- 5) Create sale
        OilSale sale = new OilSale();
        sale.setSupplier(supplier);
        sale.setStorageUnit(su.getId());
        sale.setQuantity(BigDecimal.valueOf(req.getQuantity().doubleValue()));
        sale.setUnitPrice(BigDecimal.valueOf(req.getUnitPrice().doubleValue()));
        sale.setTotalAmount(BigDecimal.valueOf(saleTotal.doubleValue()));
        sale.setPaidAmount(paid.doubleValue());
        sale.setUnpaidAmount(unpaid.doubleValue());
        sale.setCurrency(req.getCurrency());
        sale.setPaymentMethod(req.getPaymentMethod());
        sale.setSaleDate(req.getSaleDate());
        sale.setQualityGrade(req.getQualityGrade());
        sale.setInvoiceNumber(req.getInvoiceNumber());
        sale.setDescription(req.getDescription());
        sale.setStatus(unpaid.signum() == 0 ? SaleStatus.CONFIRMED : SaleStatus.PENDING);

        sale = oilSaleRepository.save(sale);
        OilTransactionDTO oiltTransactionDto = new OilTransactionDTO();
        oiltTransactionDto.setUnitPrice(req.getUnitPrice().doubleValue());
        oiltTransactionDto.setQuantityKg(req.getQuantity().doubleValue());
        oiltTransactionDto.setTotalPrice(req.getQuantity().doubleValue() * req.getUnitPrice().doubleValue());
        oiltTransactionDto.setOilSaleId(sale.getId());
        oilTransactionService.createOilTransactionForSale(oiltTransactionDto);
        // ---- 6) Persist container lines (only if present)
        if (hasContainers) {
            for (OilContainerSale l : req.getContainerSales()) {
                OilContainer c = oilContainerRepository.findByIdAndIsDeletedFalse(l.getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + l.getId()));

                OilContainerSale line = new OilContainerSale();
                line.setOilSale(sale);
                line.setContainer(c);
                line.setCount(l.getCount());
                line.setUnitPrice(c.getSellingPrice());
                line.setLineTotal(c.getSellingPrice().multiply(BigDecimal.valueOf(l.getCount())));
                lineRepo.save(line);

                c.setStockQuantity(c.getStockQuantity() - l.getCount());
                containerRepo.save(c);
            }
        }

        // ---- 7) Inventory updates (oil)
        su.setCurrentVolume(suQty.subtract(req.getQuantity()).doubleValue());
        storageUnitRepo.save(su);


        // ---- 8) Financial transactions
        // Revenue: OIL
        if (oilTotal.signum() > 0) {
            FinancialTransactionDto financialTransactionDto = getFinancialTransactionDto(req, oilTotal, supplier, sale);
            financialTransactionFeignService.create(financialTransactionDto);
        }

        // Revenue: CONTAINERS (only if we had container lines)
        if (hasContainers && containerTotal.signum() > 0) {
            FinancialTransactionDto financialTransactionDto = getTransactionDto(req, containerTotal, supplier, sale);
            financialTransactionFeignService.create(financialTransactionDto);
        }


        return modelMapper.map(sale,OilSaleDTO.class);
    }

    private FinancialTransactionDto getTransactionDto(OilSaleCreateRequest req, BigDecimal containerTotal, Supplier supplier, OilSale sale) {
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.OIL_CONTAINER_SALE);
        return getFinancialTransactionDto(req, containerTotal, supplier, sale, financialTransactionDto, OIL_CONTAINER_SALE);
    }

    private FinancialTransactionDto getFinancialTransactionDto(OilSaleCreateRequest req, BigDecimal containerTotal, Supplier supplier, OilSale sale, FinancialTransactionDto financialTransactionDto, OperationType operationType) {
        financialTransactionDto.setDirection(TransactionDirection.INBOUND);
        financialTransactionDto.setAmount(containerTotal);
        financialTransactionDto.setCurrency(req.getCurrency() != null ? req.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(null);
        financialTransactionDto.setCheckNumber(null);
        financialTransactionDto.setLotNumber(null);
        if (supplier != null) {
            financialTransactionDto.setsupplier(modelMapper.map(supplier, SupplierDto.class));          // NOTE: if your DTO uses setsupplier(...), change this line.
        }
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(operationType);
        financialTransactionDto.setExternalTransactionId(sale.getId().toString());
        financialTransactionDto.setResourceName(ResourceName.OILSALE);
        return financialTransactionDto;
    }

    private FinancialTransactionDto getFinancialTransactionDto(OilSaleCreateRequest req, BigDecimal oilTotal, Supplier supplier, OilSale sale) {
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(TransactionType.OIL_SALE);
        return getFinancialTransactionDto(req, oilTotal, supplier, sale, financialTransactionDto, OIL_SALE);
    }

    @Transactional
    public void cancelSale(UUID saleId) {
        // 1. Retrieve and validate the sale
        OilSale sale = oilSaleRepository.findByIdAndIsDeletedFalse(saleId).orElseThrow(() -> new IllegalArgumentException("Oil Sale not found: " + saleId));

        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new IllegalStateException("Sale is already canceled: " + saleId);
        }

        // Optionally, restrict cancellation based on status
        if (sale.getStatus() == SaleStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered sale: " + saleId);
        }

        // 2. Restore oil quantity in storage unit
        StorageUnit storageUnit = storageUnitRepo.findByIdAndIsDeletedFalse(sale.getStorageUnit()).orElseThrow(() -> new IllegalArgumentException("Storage unit not found: " + sale.getStorageUnit()));

        storageUnit.setCurrentVolume(storageUnit.getCurrentVolume() + sale.getQuantity().doubleValue());
        storageUnitRepo.save(storageUnit);

        // 3. Restore container stock (if applicable)
        var containerSales = lineRepo.findByOilSaleId(saleId);
        for (OilContainerSale containerSale : containerSales) {
            OilContainer container = containerRepo.findByIdAndIsDeletedFalse(containerSale.getContainer().getId()).orElseThrow(() -> new IllegalArgumentException("Container not found: " + containerSale.getContainer().getId()));
            container.setStockQuantity(container.getStockQuantity() + containerSale.getCount());
            containerRepo.save(container);
        }

        // 4. Reverse financial transactions
        // Oil sale transaction
        if (sale.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            FinancialTransactionDto reversalOilTransaction = new FinancialTransactionDto();
            reversalOilTransaction.setTransactionType(TransactionType.OIL_SALE);
            reversalOilTransaction.setDirection(TransactionDirection.OUTBOUND);
            reversalOilTransaction.setAmount(sale.getTotalAmount().negate()); // Negative to reverse
            reversalOilTransaction.setCurrency(sale.getCurrency());
            reversalOilTransaction.setPaymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod() : PaymentMethod.CASH);
            reversalOilTransaction.setTransactionDate(LocalDateTime.now());
            reversalOilTransaction.setApproved(true);
            reversalOilTransaction.setApprovalDate(LocalDateTime.now());
            reversalOilTransaction.setOperationType(OIL_SALE);
            reversalOilTransaction.setExternalTransactionId(sale.getId().toString());
            reversalOilTransaction.setResourceName(ResourceName.OILSALE);
            if (sale.getSupplier() != null) {
                reversalOilTransaction.setsupplier(modelMapper.map(sale.getSupplier(), SupplierDto.class));
            }
            financialTransactionFeignService.create(reversalOilTransaction);
        }

        // Container sale transaction (if containers exist)
        if (!containerSales.isEmpty()) {
            BigDecimal containerTotal = containerSales.stream().map(OilContainerSale::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (containerTotal.compareTo(BigDecimal.ZERO) > 0) {
                FinancialTransactionDto reversalContainerTransaction = new FinancialTransactionDto();
                reversalContainerTransaction.setTransactionType(TransactionType.OIL_CONTAINER_SALE);
                reversalContainerTransaction.setDirection(TransactionDirection.OUTBOUND);
                reversalContainerTransaction.setAmount(containerTotal.negate()); // Negative to reverse
                reversalContainerTransaction.setCurrency(sale.getCurrency());
                reversalContainerTransaction.setPaymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod() : PaymentMethod.CASH);
                reversalContainerTransaction.setTransactionDate(LocalDateTime.now());
                reversalContainerTransaction.setApproved(true);
                reversalContainerTransaction.setApprovalDate(LocalDateTime.now());
                reversalContainerTransaction.setOperationType(OIL_CONTAINER_SALE);
                reversalContainerTransaction.setExternalTransactionId(sale.getId().toString());
                reversalContainerTransaction.setResourceName(ResourceName.OILSALE);
                if (sale.getSupplier() != null) {
                    reversalContainerTransaction.setsupplier(modelMapper.map(sale.getSupplier(), SupplierDto.class));
                }
                financialTransactionFeignService.create(reversalContainerTransaction);
            }
        }

        // 5. Reverse oil transaction (if applicable)
        oilTransactionService.reverseOilTransactionForSale(sale.getId());

        // 6. Reverse payments (if any)
        if (sale.getPaidAmount() != null && sale.getPaidAmount() > 0) {
            FinancialTransactionDto refundTransaction = new FinancialTransactionDto();
            refundTransaction.setTransactionType(TransactionType.OIL_SALE);
            refundTransaction.setDirection(TransactionDirection.OUTBOUND);
            refundTransaction.setAmount(BigDecimal.valueOf(sale.getPaidAmount()).negate());
            refundTransaction.setCurrency(sale.getCurrency());
            refundTransaction.setPaymentMethod(sale.getPaymentMethod() != null ? sale.getPaymentMethod() : PaymentMethod.CASH);
            refundTransaction.setTransactionDate(LocalDateTime.now());
            refundTransaction.setApproved(true);
            refundTransaction.setApprovalDate(LocalDateTime.now());
            refundTransaction.setOperationType(OIL_SALE);
            refundTransaction.setExternalTransactionId(sale.getId().toString());
            refundTransaction.setResourceName(ResourceName.OILSALE);
            if (sale.getSupplier() != null) {
                refundTransaction.setsupplier(modelMapper.map(sale.getSupplier(), SupplierDto.class));
            }
            financialTransactionFeignService.create(refundTransaction);

            // Reset payment fields
            sale.setPaidAmount(0.0);
            sale.setUnpaidAmount(sale.getTotalAmount().doubleValue());
        }

        // 7. Update sale status
        sale.setStatus(SaleStatus.CANCELLED);
        sale.setDeleted(true); // Optional: soft-delete
        oilSaleRepository.save(sale);

        // 8. Delete container sale lines (optional, depending on requirements)
        lineRepo.deleteAll(containerSales);
    }
    @Override
    public Set<Action> actionsMapping(OilSale oilSale) {
        return new HashSet<>(Set.of(Action.UPDATE, Action.DELETE, Action.READ));
    }
}