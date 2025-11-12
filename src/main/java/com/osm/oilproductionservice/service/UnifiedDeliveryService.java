package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.ExchangePricingDto;
import com.osm.oilproductionservice.dto.PaymentDTO;
import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.dto.UnifiedDeliveryDTO;
import com.osm.oilproductionservice.feignClients.services.FinancialTransactionFeignService;
import com.osm.oilproductionservice.model.*;
import com.osm.oilproductionservice.repository.*;
import com.xdev.communicator.models.enums.*;
import com.xdev.communicator.models.enums.Currency;
import com.xdev.communicator.models.shared.FinancialTransactionDto;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedDeliveryService extends BaseServiceImpl<UnifiedDelivery, UnifiedDeliveryDTO, UnifiedDeliveryDTO> {

    public static final String DELIVERY_NUMBER = "deliveryNumber";
    public static final String D = "%03d";
    public static final String D1 = "%02d";
    public static final String ID = "id";
    public static final String SUPPLIER = "supplier";
    public static final String STORAGE_UNIT = "storageUnit";
    public static final String EXTERNAL_ID = "externalId";
    public static final String PAID = "paid";
    private final DeliveryRepository deliveryRepository;
    private final SupplierRepository supplierRepository;
    private final StorageUnitRepo storageUnitRepo;
    private final GenericRepository genericRepository;
    private final OilTransactionService oilTransactionService;
    private final FinancialTransactionFeignService financialTransactionFeignService;
    private final QualityControlResultRepository qualityControlResultRepository;

    public UnifiedDeliveryService(BaseRepository<UnifiedDelivery> repository, ModelMapper modelMapper, DeliveryRepository deliveryRepository, SupplierRepository supplierRepository, StorageUnitRepo storageUnitRepo, GenericRepository genericRepository, OilTransactionService oilTransactionService, FinancialTransactionFeignService financialTransactionFeignService, QualityControlResultRepository qualityControlResultRepository) {
        super(repository, modelMapper);
        this.deliveryRepository = deliveryRepository;
        this.supplierRepository = supplierRepository;
        this.storageUnitRepo = storageUnitRepo;
        this.genericRepository = genericRepository;
        this.oilTransactionService = oilTransactionService;
        this.financialTransactionFeignService = financialTransactionFeignService;
        this.qualityControlResultRepository = qualityControlResultRepository;
    }

    private static double r3(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private static double safe(Double v) {
        return (v == null || v.isNaN() || v.isInfinite()) ? 0.0 : v;
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // Helper: treat null/0/<=0 as invalid
    private static boolean isValidPoids(java.math.BigDecimal w) {
        return w != null && w.compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    /**
     * Determines if a delivery is fully paid by comparing unpaid amount with a tolerance threshold.
     * This method is critical for payment workflow decisions.
     *
     * @param delivery The delivery to check for payment status
     * @return true if the delivery is considered fully paid, false otherwise
     */
    private boolean isFullyPaid(UnifiedDelivery delivery) {
        if (delivery == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[isFullyPaid] Delivery is null, returning false");
            return false;
        }

        // Extract payment amounts with null safety
        double price = Optional.ofNullable(delivery.getPrice()).orElse(0d);
        double paid = Optional.ofNullable(delivery.getPaidAmount()).orElse(0d);
        double unpaid = Optional.ofNullable(delivery.getUnpaidAmount()).orElse(price - paid); // fallback calculation

        // Log payment details for debugging
        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[isFullyPaid] Payment check for delivery %s: price=%.2f, paid=%.2f, unpaid=%.2f", delivery.getLotNumber(), price, paid, unpaid);

        // Use tolerance for floating point comparison (0.0001 TND = 0.01 centimes)
        boolean fullyPaid = unpaid <= 0.0001;

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[isFullyPaid] Delivery %s is %s", delivery.getLotNumber(), fullyPaid ? "FULLY PAID" : "NOT FULLY PAID");

        return fullyPaid;
    }

    @Override
    public UnifiedDeliveryDTO save(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "save", dto);
        // Map DTO to entity

        UnifiedDelivery delivery = modelMapper.map(dto, UnifiedDelivery.class);
        if (dto.getDeliveryType() == DeliveryType.OIL) {
            delivery.setStatus(OliveLotStatus.NEW);
        } else if (delivery.getPoidsCamionVide() != null &&delivery.getPoidsCamionVide() != 0 && dto.getDeliveryType() == DeliveryType.OLIVE) {
            delivery.setStatus(OliveLotStatus.NEW);
        } else {
            delivery.setStatus(OliveLotStatus.WAITING);
        }

        if (dto.getSupplier() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId()).orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getId()));
            delivery.setSupplierType(supplier);
        }


        // Save entity
        UnifiedDelivery savedDelivery = deliveryRepository.saveAndFlush(delivery);

        // Map back to DTO and return
        OSMLogger.logMethodExit(this.getClass(), "save", savedDelivery);
        OSMLogger.logPerformance(this.getClass(), "save", startTime, System.currentTimeMillis());
        return modelMapper.map(savedDelivery, UnifiedDeliveryDTO.class);
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO update(UnifiedDeliveryDTO dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "update", dto);
        // 1. Load existing or fail
        UnifiedDelivery existing = deliveryRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("UnifiedDelivery not found with id: " + dto.getId()));

        // 2. Copy simple fields (exclude those we manage manually, including status)
        BeanUtils.copyProperties(dto, existing, "id", "supplier", "storageUnit", "externalId", "paid", "oliveVariety", "parcel", "status");

        // 3. Resolve Supplier
        if (dto.getSupplier() != null && dto.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + dto.getSupplier().getId()));
            existing.setSupplierType(supplier);
        } else {
            existing.setSupplierType(null);
        }

        // 4. Resolve StorageUnit
        if (dto.getStorageUnit() != null && dto.getStorageUnit().getId() != null) {
            StorageUnit stu = storageUnitRepo.findById(dto.getStorageUnit().getId())
                    .orElseThrow(() -> new RuntimeException("StorageUnit not found with id: " + dto.getStorageUnit().getId()));
            existing.setStorageUnit(stu);
        } else {
            existing.setStorageUnit(null);
        }

        // 5. Resolve OliveVariety
        if (dto.getOliveVariety() != null && dto.getOliveVariety().getId() != null) {
            BaseType oliveVariety = genericRepository.findById(dto.getOliveVariety().getId())
                    .orElseThrow(() -> new RuntimeException("OliveVariety not found with id: " + dto.getOliveVariety().getId()));
            existing.setOliveVariety(oliveVariety);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[update] Set oliveVariety to: %s (ID: %s)", oliveVariety.getName(), oliveVariety.getId());
        } else {
            existing.setOliveVariety(null);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[update] Set oliveVariety to null");
        }

        // 6. Merge poidsCamionVide explicitly (do NOT clear it when DTO omits it)
        if (dto.getPoidsCamionVide() != null) {
            if (dto.getPoidsCamionVide() < 0) {
                throw new IllegalArgumentException("poidsCamionVide cannot be negative: " + dto.getPoidsCamionVide());
            }
            existing.setPoidsCamionVide(dto.getPoidsCamionVide());
        }



        // 8. Handle status for OLIVE deliveries
        if (existing.getDeliveryType() == DeliveryType.OLIVE) {
            OliveLotStatus prev = existing.getStatus();

            // Check if poidsCamionVide or status is being updated
            boolean isPoidsUpdated = dto.getPoidsCamionVide() != null;
            boolean isStatusUpdated = dto.getStatus() != null;

            if (isPoidsUpdated || isStatusUpdated) {
                // Apply status logic only if poidsCamionVide or status is explicitly updated
                boolean poidsValid = isValidPoids(existing.getPoidsCamionVide() != null ? BigDecimal.valueOf(existing.getPoidsCamionVide()) : null);
                if (poidsValid && prev == OliveLotStatus.WAITING) {
                    existing.setStatus(OliveLotStatus.NEW);
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                            "[update] poidsCamionVide valid (>0) and previous status WAITING: status -> NEW (prev=%s, poids=%s)",
                            prev, existing.getPoidsCamionVide());
                } else if (!poidsValid && prev == OliveLotStatus.NEW) {
                    existing.setStatus(OliveLotStatus.WAITING);
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                            "[update] poidsCamionVide invalid (null/0/<=0) and previous status NEW: status -> WAITING (prev=%s, poids=%s)",
                            prev, existing.getPoidsCamionVide());
                } else {
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                            "[update] Status unchanged: poidsValid=%s, prev=%s", poidsValid, prev);
                }
            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                        "[update] No update to poidsCamionVide or status: retaining status %s", prev);
            }
        } else if (dto.getStatus() != null) {
            // For non-OLIVE deliveries, allow direct status update
            existing.setStatus(dto.getStatus());
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO,
                    "[update] Non-OLIVE delivery: status updated to %s", dto.getStatus());
        }

        // 9. Persist
        UnifiedDelivery updated = deliveryRepository.save(existing);

        OSMLogger.logMethodExit(this.getClass(), "update", updated);
        OSMLogger.logPerformance(this.getClass(), "update", startTime, System.currentTimeMillis());
        return modelMapper.map(updated, UnifiedDeliveryDTO.class);
    }

    public List<UnifiedDeliveryDTO> getForPlanning() {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getForPlanning", null);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findOliveDeliveriesControlled().stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getForPlanning", result);
        OSMLogger.logPerformance(this.getClass(), "getForPlanning", startTime, System.currentTimeMillis());
        return result;
    }

    public List<UnifiedDeliveryDTO> findByDeliveryTypeInAndQualityControlResultsIsNull(List<String> types) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", types);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findByDeliveryTypeInAndQualityControlResultsIsNull(types).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", result);
        OSMLogger.logPerformance(this.getClass(), "findByDeliveryTypeInAndQualityControlResultsIsNull", startTime, System.currentTimeMillis());
        return result;
    }

    // Get deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get paid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getPaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getPaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findFullyPaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getPaidDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getPaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    // Get unpaid deliveries by supplier ID
    public List<UnifiedDeliveryDTO> getUnpaidDeliveriesBySupplier(UUID supplierId) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "getUnpaidDeliveriesBySupplier", supplierId);
        List<UnifiedDeliveryDTO> result = deliveryRepository.findUnpaidDeliveriesBySupplierId(supplierId).stream().map((element) -> modelMapper.map(element, UnifiedDeliveryDTO.class)).collect(Collectors.toList());
        OSMLogger.logMethodExit(this.getClass(), "getUnpaidDeliveriesBySupplier", result);
        OSMLogger.logPerformance(this.getClass(), "getUnpaidDeliveriesBySupplier", startTime, System.currentTimeMillis());
        return result;
    }

    @Override
    public Set<Action> actionsMapping(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "actionsMapping", delivery);
        if (delivery.getDeliveryType() == DeliveryType.OIL) {
            Set<Action> actions = mapOilDeliveryActions(delivery);
            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        } else {
            Set<Action> actions = mapOliveDeliveryActions(delivery);
            OSMLogger.logMethodExit(this.getClass(), "actionsMapping", actions);
            OSMLogger.logPerformance(this.getClass(), "actionsMapping", startTime, System.currentTimeMillis());
            return actions;
        }
    }

    /**
     * Maps available actions for olive deliveries based on their status and operation type.
     * This method is crucial for determining the payment workflow steps.
     *
     * @param delivery The olive delivery to map actions for
     * @return Set of available actions for the delivery
     */
    private Set<Action> mapOliveDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "mapOliveDeliveryActions", delivery);

        if (delivery == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[mapOliveDeliveryActions] Delivery is null, returning empty action set");
            return new HashSet<>();
        }

        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);

        switch (delivery.getStatus()) {
            case WAITING -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE));
            }
            case NEW -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.OLIVE_QUALITY));
                actions.add(Action.GEN_PDF);
            }
            case PROD_READY -> {
                actions.add(Action.DELETE);
                actions.add(Action.GEN_PDF);
                actions.add(Action.GEN_PDF_QC_OLIVE);
            }
            case OLIVE_CONTROLLED -> {
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.GEN_PDF_QC_OLIVE));
                actions.add(Action.GEN_PDF);

                switch (delivery.getOperationType()) {
                    case EXCHANGE, OLIVE_PURCHASE -> {
                        actions.add(Action.SET_PRICE);
                    }
                }
            }
            case COMPLETED -> {
                actions.add(Action.GEN_PDF_QC_OLIVE);
                actions.add(Action.GEN_PDF_PRODUCTION); // ✅ Bon de production quand réception olive terminée
                actions.add(Action.GEN_INVOICE);
                actions.add(Action.GEN_PDF);

                switch (delivery.getOperationType()) {
                    case SIMPLE_RECEPTION -> {
                        // CRITICAL: Check payment status for simple reception
                        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] SIMPLE_RECEPTION payment check for delivery %s: fullyPaid=%s", delivery.getLotNumber(), delivery.getPaid());
                        actions.add(Action.GEN_PDF);

                        if (!delivery.getPaid()) {
                            actions.add(Action.OIL_QUALITY);
                            actions.add(Action.PAY);
                            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Added OIL_QUALITY action for unpaid delivery " + delivery.getLotNumber());
                        }
                    }
                    case BASE, OLIVE_PURCHASE -> {
                        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Adding OIL_RECEPTION for %s operation delivery %s", delivery.getOperationType(), delivery.getLotNumber());

                        actions.add(Action.OIL_RECEPTION);
                        actions.add(Action.GEN_PDF_QC_OIL);
                        actions.add(Action.GEN_PDF);

                        if (!delivery.getPaid()) {
                            actions.add(Action.PAY);
                            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Added OIL_QUALITY action for unpaid delivery " + delivery.getLotNumber());
                        }
                    }
                }
            }
            case IN_STOCK -> {
                // ✅ Même logique que COMPLETED → possibilité de générer bon de production
                actions.add(Action.GEN_PDF_PRODUCTION);
                actions.add(Action.GEN_INVOICE);
                actions.add(Action.GEN_PDF);

            }
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOliveDeliveryActions] Final actions for delivery %s: %s", delivery.getLotNumber(), actions);
        OSMLogger.logMethodExit(this.getClass(), "mapOliveDeliveryActions", actions);
        OSMLogger.logPerformance(this.getClass(), "mapOliveDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }


    /**
     * Maps available actions for oil deliveries based on their status.
     * This method handles the oil delivery workflow including quality control and payment processing.
     *
     * @param delivery The oil delivery to map actions for
     * @return Set of available actions for the delivery
     */
    private Set<Action> mapOilDeliveryActions(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "mapOilDeliveryActions", delivery);

        if (delivery == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[mapOilDeliveryActions] Delivery is null, returning empty action set");
            return new HashSet<>();
        }

        Set<Action> actions = new HashSet<>();
        actions.add(Action.READ);


        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Mapping actions for oil delivery %s (Status: %s, Operation: %s)", delivery.getLotNumber(), delivery.getStatus(), delivery.getOperationType());

        switch (delivery.getStatus()) {
            case NEW -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding NEW status actions for oil delivery " + delivery.getLotNumber());
                actions.addAll(Set.of(Action.DELETE, Action.UPDATE, Action.OIL_QUALITY));
                actions.add(Action.GEN_PDF);
            }
            case OIL_CONTROLLED -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding OIL_CONTROLLED status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.GEN_PDF_QC_OIL);
                actions.add(Action.SET_PRICE);
                actions.add(Action.GEN_PDF);
            }
            case WAITING_FOR_PAYMENT_DETAILS -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding WAITING_FOR_PAYMENT_DETAILS status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.COMPLETE_PAYMENT_DETAILS);

                actions.add(Action.GEN_PDF);
            }
            case COMPLETED, IN_STOCK -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Adding GEN_PDF_BON_PROD status actions for oil delivery " + delivery.getLotNumber());
                actions.add(Action.GEN_PDF_PRODUCTION);
                actions.add(Action.GEN_PDF_QC_OIL);
                actions.add(Action.GEN_INVOICE);

            }
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[mapOilDeliveryActions] Final actions for oil delivery %s: %s", delivery.getLotNumber(), actions);
        OSMLogger.logMethodExit(this.getClass(), "mapOilDeliveryActions", actions);
        OSMLogger.logPerformance(this.getClass(), "mapOilDeliveryActions", startTime, System.currentTimeMillis());
        return actions;
    }

    /**
     * Creates oil reception records from olive deliveries based on operation type and payment flag.
     * This method handles different scenarios: exchange operations, base operations, olive purchases, and payment operations.
     *
     * @param uuid      The UUID of the original olive delivery
     * @param isPayment Boolean flag indicating if this is for payment purposes
     * @param std
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if uuid is null or delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public UnifiedDelivery createOilRecFromOliveRecImpl(UUID uuid, Boolean isPayment, String std) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createOilRecFromOliveRecImpl", String.format("uuid=%s, isPayment=%s", uuid, isPayment));

        // Validate input parameters
        if (uuid == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] UUID is null");
            throw new IllegalArgumentException("UUID cannot be null");
        }

        if (isPayment == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] isPayment flag is null");
            throw new IllegalArgumentException("isPayment flag cannot be null");
        }

        try {
            // Find the original delivery
            UnifiedDelivery delivery = repository.findById(uuid).orElseThrow(() -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Original delivery not found with UUID: " + uuid);
                return new EntityNotFoundException("Original delivery not found: " + uuid);
            });

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Found original delivery %s (Type: %s, Operation: %s, Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getOperationType(), delivery.getStatus());

            // Validate delivery type
            if (delivery.getDeliveryType() != DeliveryType.OLIVE) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Invalid delivery type for oil reception creation: %s (expected OLIVE)", delivery.getDeliveryType());
                throw new IllegalArgumentException("Oil reception can only be created from OLIVE deliveries");
            }

            // Validate delivery status
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[createOilRecFromOliveRecImpl] Creating oil reception for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            UnifiedDelivery oilDelivery = null;

            // Process based on operation type and payment flag
            if (delivery.getOperationType() == OperationType.EXCHANGE || delivery.getOperationType() == OperationType.BASE || delivery.getOperationType() == OperationType.OLIVE_PURCHASE) {

                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Creating oil reception for %s operation", delivery.getOperationType());
                oilDelivery = creatOilRecForOtherOPS(delivery);

            } else if (isPayment) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Creating oil reception for payment purposes");
                oilDelivery = createOilRecForPayment(delivery, std);

            } else {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[createOilRecFromOliveRecImpl] No oil reception created for delivery %s (Operation: %s, isPayment: %s)", delivery.getLotNumber(), delivery.getOperationType(), isPayment);
                throw new IllegalArgumentException("No oil reception can be created for this operation type and payment flag combination");
            }

            if (oilDelivery != null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecFromOliveRecImpl] Successfully created oil reception %s from olive delivery %s", oilDelivery.getLotNumber(), delivery.getLotNumber());
            }

            return oilDelivery;

        } catch (EntityNotFoundException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecFromOliveRecImpl] Unexpected error during oil reception creation: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception from olive delivery", e);
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "createOilRecFromOliveRecImpl", null);
            OSMLogger.logPerformance(this.getClass(), "createOilRecFromOliveRecImpl", startTime, System.currentTimeMillis());
        }
    }

    /**
     * Creates an oil reception record for operations other than payment (EXCHANGE, BASE, OLIVE_PURCHASE).
     * This method creates a new oil delivery linked to the original olive delivery.
     *
     * @param delivery The original olive delivery
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if delivery is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    private UnifiedDelivery creatOilRecForOtherOPS(UnifiedDelivery delivery) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "creatOilRecForOtherOPS", delivery);

        if (delivery == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[creatOilRecForOtherOPS] Original delivery is null");
            throw new IllegalArgumentException("Original delivery cannot be null");
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Creating oil reception for %s operation from olive delivery %s", delivery.getOperationType(), delivery.getLotNumber());

        try {
            UnifiedDelivery newDelivery = new UnifiedDelivery();
            newDelivery.setDeliveryType(DeliveryType.OIL);
            newDelivery.setStatus(OliveLotStatus.NEW);
            newDelivery.setLotNumber(delivery.getLotNumber());
            newDelivery.setDeliveryNumber(delivery.getDeliveryNumber());
            newDelivery.setDeliveryDate(LocalDateTime.now());

            // Set oil quantity with null safety
            Double oilQuantity = delivery.getOilQuantity();
            if (oilQuantity == null || oilQuantity <= 0) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[creatOilRecForOtherOPS] Invalid oil quantity for delivery %s: %s, setting to 0.0", delivery.getLotNumber(), oilQuantity);
                oilQuantity = 0.0;
            }
            newDelivery.setOilQuantity(oilQuantity);

            // Initialize pricing
            newDelivery.setUnitPrice(0.0);

            // Copy relevant information from original delivery
            newDelivery.setOilType(delivery.getOliveType());
            newDelivery.setOliveQuantity(delivery.getPoidsNet());
            newDelivery.setOliveType(delivery.getOliveType());
            newDelivery.setRegion(delivery.getRegion());
            newDelivery.setSupplier(delivery.getSupplier());
            newDelivery.setLotOliveNumber(delivery.getLotNumber()); // Link to original olive delivery
            newDelivery.setOperationType(delivery.getOperationType());
            newDelivery.setOilVariety(delivery.getOliveVariety());

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Created oil reception with operation type INTERNAL_RECEPTION, linked to olive lot: %s", delivery.getLotNumber());

            // Save both deliveries to maintain consistency
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Saving original olive delivery to maintain consistency");
            repository.save(delivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Saving new oil reception");
            UnifiedDelivery savedOilDelivery = repository.save(newDelivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[creatOilRecForOtherOPS] Successfully created oil reception %s from olive delivery %s", savedOilDelivery.getLotNumber(), delivery.getLotNumber());

            return savedOilDelivery;

        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[creatOilRecForOtherOPS] Error creating oil reception for other operations: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception for other operations", e);
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "creatOilRecForOtherOPS", null);
            OSMLogger.logPerformance(this.getClass(), "creatOilRecForOtherOPS", startTime, System.currentTimeMillis());
        }
    }

    /**
     * Creates an oil reception record specifically for payment purposes.
     * This method is called when an olive delivery needs to be paid in oil.
     *
     * @param delivery The original olive delivery that needs oil payment
     * @return The newly created oil reception delivery
     * @throws IllegalArgumentException if delivery is null or invalid
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    protected UnifiedDelivery createOilRecForPayment(UnifiedDelivery delivery, String std) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "createOilRecForPayment", delivery);

        if (delivery == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Original delivery is null");
            throw new IllegalArgumentException("Original delivery cannot be null");
        }

        // Validate delivery type
        if (delivery.getDeliveryType() != DeliveryType.OLIVE) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Invalid delivery type for payment oil reception: %s (expected OLIVE)", delivery.getDeliveryType());
            throw new IllegalArgumentException("Payment oil reception can only be created from OLIVE deliveries");
        }

        // Validate supplier
        if (delivery.getSupplier() == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Cannot create payment oil reception for delivery without supplier");
            throw new IllegalArgumentException("Delivery must have a supplier for payment oil reception");
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] Creating oil reception for payment from olive delivery %s (Supplier: %s)", delivery.getLotNumber(), delivery.getSupplier().getName());

        try {

            UnifiedDelivery newDelivery = new UnifiedDelivery();
            if (std != null && !std.isEmpty()) {
                Optional<StorageUnitDto> stdModel = storageUnitRepo.findById(UUID.fromString(std)).map((element) -> modelMapper.map(element, StorageUnitDto.class));
                newDelivery.setStorageUnit(modelMapper.map(stdModel.get(), StorageUnit.class));
            }
            newDelivery.setDeliveryType(DeliveryType.OIL);
            newDelivery.setStatus(OliveLotStatus.OIL_CONTROLLED);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] used the original olive reception lot number as new lot number: %s, delivery number: %s", delivery.getLotNumber(), delivery.getDeliveryNumber());

            // Set basic delivery information
            newDelivery.setLotNumber(delivery.getLotNumber());
            newDelivery.setDeliveryNumber(delivery.getDeliveryNumber());
            newDelivery.setDeliveryDate(LocalDateTime.now());

            // Initialize payment-related fields
            newDelivery.setOilQuantity(delivery.getOilQuantity()); // Will be set during payment processing
            newDelivery.setUnitPrice(0.0);   // Will be set during payment processing

            // Copy relevant information from original delivery
            newDelivery.setOilType(delivery.getOliveType());
            newDelivery.setOliveType(delivery.getOliveType());
            newDelivery.setRegion(delivery.getRegion());
            newDelivery.setSupplier(delivery.getSupplier());
            newDelivery.setLotOliveNumber(delivery.getLotNumber()); // Link to original olive delivery
            newDelivery.setOperationType(OperationType.PAYMENT);
            newDelivery.setParcel(delivery.getParcel());
            newDelivery.setOilVariety(delivery.getOliveVariety());
            newDelivery.setOliveVariety(delivery.getOliveVariety());

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] Created oil reception with operation type PAYMENT, linked to olive lot: %s", delivery.getLotNumber());

            // Save both deliveries to maintain consistency
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] Saving original olive delivery to maintain consistency");
            repository.save(delivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] Saving new oil reception for payment");
            UnifiedDelivery savedOilDelivery = repository.save(newDelivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[createOilRecForPayment] Successfully created oil reception %s for payment from olive delivery %s", savedOilDelivery.getLotNumber(), delivery.getLotNumber());

            return savedOilDelivery;

        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[createOilRecForPayment] Error creating oil reception for payment: " + e.getMessage(), e);
            throw new RuntimeException("Failed to create oil reception for payment", e);
        } finally {
            OSMLogger.logMethodExit(this.getClass(), "createOilRecForPayment", null);
            OSMLogger.logPerformance(this.getClass(), "createOilRecForPayment", startTime, System.currentTimeMillis());
        }
    }


    private Map<String, Object> generateDeliveryNumber(UnifiedDelivery del) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "generateDeliveryNumber", del);
        // 1) Find last delivery to calculate next sequence
        UnifiedDelivery last = deliveryRepository.findTopByOrderByCreatedDateDesc().orElse(null);

        int nextSeq = 1;
        if (last != null) {
            try {
                nextSeq = Integer.parseInt(last.getDeliveryNumber()) + 1;
            } catch (NumberFormatException e) {
                // log.warn("Invalid deliveryNumber on last record, resetting to 1", e);
                nextSeq = 1;
            }
        }

        // 2) Build each piece
        String sequencePart = String.format(D, nextSeq);          // zero-padded to 4 digits
//        String oliveTypeCode = del.getOilType().getName().toUpperCase();           // e.g. "OB"
        int year = del.getDeliveryDate().getYear();                    // e.g. 2025
//        String yearPart = String.format(D1, year % 100);
        // last two digits: "25"

        // 3) Concatenate into final lot number
//        String lotNumber = sequencePart + yearPart;    // "0005OB25"

        // 4) Return both if you still need the raw sequence
        Map<String, Object> map = new HashMap<>();
        map.put(DELIVERY_NUMBER, nextSeq);
//        map.put(LOT_NUMBER, lotNumber);
        OSMLogger.logMethodExit(this.getClass(), "generateDeliveryNumber", map);
        OSMLogger.logPerformance(this.getClass(), "generateDeliveryNumber", startTime, System.currentTimeMillis());
        return map;
    }


    /**
     * Updates the status of a delivery.
     * This method validates the delivery exists and updates its status with proper logging.
     *
     * @param id     The delivery ID to update
     * @param status The new status to set
     * @throws IllegalArgumentException if id or status is null
     * @throws EntityNotFoundException  if delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void updateStatus(UUID id, OliveLotStatus status, String cause) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updateStatus", String.format("id=%s, status=%s", id, status));

        // Validate input parameters
        if (id == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        if (status == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Status is null");
            throw new IllegalArgumentException("Status cannot be null");
        }

        try {
            // Find the delivery
            UnifiedDelivery delivery = repository.findById(id).orElseThrow(() -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Delivery not found with ID: " + id);
                return new EntityNotFoundException("Delivery not found: " + id);
            });
            if (cause != null) {
                delivery.setDescription(cause);
            }
            OliveLotStatus oldStatus = delivery.getStatus();
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateStatus] Found delivery %s (Type: %s, Old Status: %s, New Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), oldStatus, status);

            // Validate status transition
            if (oldStatus == status) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[updateStatus] Delivery %s status is already %s, no update needed", delivery.getLotNumber(), status);
                return;
            }

            // Update status
            delivery.setStatus(status);
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateStatus] Successfully updated delivery %s status from %s to %s", savedDelivery.getLotNumber(), oldStatus, savedDelivery.getStatus());

        } catch (EntityNotFoundException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateStatus] Unexpected error during status update: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update delivery status", e);
        }

        OSMLogger.logMethodExit(this.getClass(), "updateStatus", null);
        OSMLogger.logPerformance(this.getClass(), "updateStatus", startTime, System.currentTimeMillis());
    }

    /**
     * Updates unit price and calculates total price for deliveries.
     * This method handles both OIL and OLIVE delivery types with appropriate status updates.
     *
     * @param id        The delivery ID to update
     * @param unitPrice The new unit price (must be positive)
     * @throws IllegalArgumentException if unitPrice is null or negative
     * @throws EntityNotFoundException  if delivery is not found
     * @throws RuntimeException         if there's an error during processing
     */
    @Transactional
    public void updateprice(UUID id, Double unitPrice) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updateprice", String.format("id=%s, unitPrice=%.2f", id, unitPrice));

        // Validate input parameters
        if (id == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        if (unitPrice == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Unit price is null");
            throw new IllegalArgumentException("Unit price cannot be null");
        }

        if (unitPrice <= 0) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Unit price must be positive: " + unitPrice);
            throw new IllegalArgumentException("Unit price must be positive");
        }

        try {
            // Find the delivery
            UnifiedDelivery delivery = repository.findById(id).orElseThrow(() -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Delivery not found with ID: " + id);
                return new EntityNotFoundException("Delivery not found: " + id);
            });

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateprice] Found delivery %s (Type: %s, Status: %s)", delivery.getLotNumber(), delivery.getDeliveryType(), delivery.getStatus());

            // Validate delivery state
            if (delivery.getStatus() == OliveLotStatus.IN_STOCK) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[updateprice] Updating price for delivery %s that is already IN_STOCK", delivery.getLotNumber());
            }

            // Update unit price
            delivery.setUnitPrice(unitPrice);

            // Process based on delivery type
            switch (delivery.getDeliveryType()) {
                case OIL -> {
                    if (delivery.getOilQuantity() == null || delivery.getOilQuantity() <= 0) {
                        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Invalid oil quantity for delivery %s: %s", delivery.getLotNumber(), delivery.getOilQuantity());
                        throw new IllegalArgumentException("Oil quantity must be positive for OIL deliveries");
                    }
                    double totalPrice = unitPrice * delivery.getOilQuantity();
                    delivery.setPrice(totalPrice);
                    delivery.setStatus(OliveLotStatus.IN_STOCK);
                    delivery.setUnpaidAmount(totalPrice);
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateprice] Updated OIL delivery %s: unitPrice=%.2f, oilQuantity=%.2f, totalPrice=%.2f", delivery.getLotNumber(), unitPrice, delivery.getOilQuantity(), totalPrice);

                    // Create oil transaction
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateprice] Creating oil transaction for delivery " + delivery.getLotNumber());
                    oilTransactionService.createSingleOilTransactionIn(delivery);
                }
                case OLIVE -> {
                    if (delivery.getPoidsNet() == null || delivery.getPoidsNet() <= 0) {
                        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Invalid poids net for delivery %s: %s", delivery.getLotNumber(), delivery.getPoidsNet());
                        throw new IllegalArgumentException("Poids net must be positive for OLIVE deliveries");
                    }

                    double totalPrice = unitPrice * delivery.getPoidsNet();
                    delivery.setPrice(totalPrice);
                    delivery.setStatus(OliveLotStatus.PROD_READY);
                    delivery.setUnpaidAmount(totalPrice);

                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateprice] Updated OLIVE delivery %s: unitPrice=%.2f, poidsNet=%.2f, totalPrice=%.2f", delivery.getLotNumber(), unitPrice, delivery.getPoidsNet(), totalPrice);
                }
                case null, default -> {
                    OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Unsupported delivery type for delivery %s: %s", delivery.getLotNumber(), delivery.getDeliveryType());
                    throw new IllegalArgumentException("Unsupported delivery type: " + delivery.getDeliveryType());
                }
            }

            // Save the updated delivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateprice] Successfully saved delivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

        } catch (EntityNotFoundException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Entity not found error: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Validation error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateprice] Unexpected error during price update: " + e.getMessage(), e);
            throw new RuntimeException("Failed to update delivery price", e);
        }

        OSMLogger.logMethodExit(this.getClass(), "updateprice", null);
        OSMLogger.logPerformance(this.getClass(), "updateprice", startTime, System.currentTimeMillis());
    }

    /**
     * Updates pricing for payment reception and creates oil transaction.
     * This method is called when payment details are completed for oil receptions.
     *
     * @param dto The exchange pricing data containing delivery ID, unit price, and total price
     * @throws EntityNotFoundException  if delivery is not found
     * @throws IllegalArgumentException if pricing data is invalid
     */
    @Transactional
    public void updatePrincingForPaymentreception(ExchangePricingDto dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updatePrincingForPaymentreception", dto);

        // Validate input parameters
        if (dto == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] ExchangePricingDto is null");
            throw new IllegalArgumentException("ExchangePricingDto cannot be null");
        }

        if (dto.getDeliveryId() == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Processing payment reception for delivery %s with unit price %.2f and total price %.2f", dto.getDeliveryId(), dto.getUnitPrice(), dto.getPrice());

        try {
            // Find the oilDelivery
            UnifiedDelivery oilDelivery = deliveryRepository.findById(dto.getDeliveryId()).orElseThrow(() -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Delivery not found with ID: " + dto.getDeliveryId());
                return new EntityNotFoundException("Delivery not found: " + dto.getDeliveryId());
            });
            UnifiedDelivery originalOliveDelivery = this.deliveryRepository.findByLotNumberAndDeliveryType(oilDelivery.getLotOliveNumber(), DeliveryType.OLIVE);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Found oilDelivery %s (Status: %s, Type: %s)", oilDelivery.getLotNumber(), oilDelivery.getStatus(), oilDelivery.getDeliveryType());

            // Validate oilDelivery type
            if (oilDelivery.getDeliveryType() != DeliveryType.OIL) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid oilDelivery type for payment reception: %s (expected OIL)", oilDelivery.getDeliveryType());
                throw new IllegalArgumentException("Payment reception can only be processed for OIL deliveries");
            }

            // Validate pricing data
            if (dto.getUnitPrice() == null || dto.getUnitPrice() <= 0) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid unit price: " + dto.getUnitPrice());
                throw new IllegalArgumentException("Unit price must be positive");
            }

            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Invalid total price: " + dto.getPrice());
                throw new IllegalArgumentException("Total price must be positive");
            }

            // Update pricing on the Delivery
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Updating pricing for oilDelivery %s: unitPrice=%.2f, price=%.2f", oilDelivery.getLotNumber(), dto.getUnitPrice(), dto.getPrice());

            oilDelivery.setUnitPrice(dto.getUnitPrice());
            oilDelivery.setOilQuantity(dto.getOilQuantity());
            oilDelivery.setPoidsNet(dto.getOilQuantity());
            oilDelivery.setPrice(dto.getPrice());
            oilDelivery.setPaidAmount(dto.getPrice());
            originalOliveDelivery.setPaidAmount(dto.getPrice());
            originalOliveDelivery.setUnpaidAmount(originalOliveDelivery.getUnpaidAmount() - dto.getPrice());
            oilDelivery.setStatus(OliveLotStatus.STOCK_READY);

            // Save the updated oilDelivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(oilDelivery);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Successfully saved oilDelivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

            // Create oil transaction
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Creating oil transaction for oilDelivery " + oilDelivery.getLotNumber());
            oilTransactionService.createSingleOilTransactionIn(savedDelivery);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updatePrincingForPaymentreception] Successfully completed payment reception processing for oilDelivery %s", oilDelivery.getLotNumber());

        } catch (EntityNotFoundException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Entity not found error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updatePrincingForPaymentreception] Unexpected error during payment reception processing: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process payment reception", e);
        }

        OSMLogger.logMethodExit(this.getClass(), "updatePrincingForPaymentreception", null);
        OSMLogger.logPerformance(this.getClass(), "updatePrincingForPaymentreception", startTime, System.currentTimeMillis());
    }

    /**
     * Updates exchange pricing and creates oil transaction out.
     * This method is used for exchange operations where oil is being transferred out.
     *
     * @param dto The exchange pricing data containing delivery ID, unit price, and total price
     * @throws EntityNotFoundException  if delivery is not found
     * @throws IllegalArgumentException if pricing data is invalid
     */
    @Transactional
    public void updateExchangePricingAndCreateOilTransactionOut(ExchangePricingDto dto) {
        long startTime = System.currentTimeMillis();
        OSMLogger.logMethodEntry(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", dto);

        // Validate input parameters
        if (dto == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] ExchangePricingDto is null");
            throw new IllegalArgumentException("ExchangePricingDto cannot be null");
        }

        if (dto.getDeliveryId() == null) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Delivery ID is null");
            throw new IllegalArgumentException("Delivery ID cannot be null");
        }

        OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Processing exchange pricing for delivery %s with unit price %.2f and total price %.2f", dto.getDeliveryId(), dto.getUnitPrice(), dto.getPrice());

        try {
            // Find the delivery
            UnifiedDelivery delivery = deliveryRepository.findById(dto.getDeliveryId()).orElseThrow(() -> {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Delivery not found with ID: " + dto.getDeliveryId());
                return new EntityNotFoundException("Delivery not found: " + dto.getDeliveryId());
            });

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Found delivery %s (Status: %s, Type: %s, Operation: %s)", delivery.getLotNumber(), delivery.getStatus(), delivery.getDeliveryType(), delivery.getOperationType());


            // Validate operation type for exchange
            if (delivery.getOperationType() != OperationType.EXCHANGE) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "[updateExchangePricingAndCreateOilTransactionOut] Processing exchange pricing for non-exchange operation: %s", delivery.getOperationType());
            }

            // Validate pricing data
            if (dto.getUnitPrice() == null || dto.getUnitPrice() <= 0) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Invalid unit price: " + dto.getUnitPrice());
                throw new IllegalArgumentException("Unit price must be positive");
            }

            if (dto.getPrice() == null || dto.getPrice() <= 0) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Invalid total price: " + dto.getPrice());
                throw new IllegalArgumentException("Total price must be positive");
            }

            // Update pricing on the Delivery
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Updating exchange pricing for delivery %s: unitPrice=%.2f, price=%.2f", delivery.getLotNumber(), dto.getUnitPrice(), dto.getPrice());

            delivery.setUnitPrice(dto.getUnitPrice());
            delivery.setPrice(dto.getPrice());
            delivery.setUnpaidAmount(dto.getPrice());
            delivery.setStatus(OliveLotStatus.PROD_READY);


            // TODO: Uncomment when quality grade is implemented
            // delivery.setQualityGrade(dto.getQualityGrade());

            // Save the updated delivery
            UnifiedDelivery savedDelivery = deliveryRepository.save(delivery);
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Successfully saved delivery %s with new status: %s", savedDelivery.getLotNumber(), savedDelivery.getStatus());

            // Create oil transaction out
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Creating oil transaction out for delivery " + delivery.getLotNumber());
            oilTransactionService.createSingleOilTransactionOut(savedDelivery, dto);

            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.INFO, "[updateExchangePricingAndCreateOilTransactionOut] Successfully completed exchange pricing processing for delivery %s", delivery.getLotNumber());

        } catch (EntityNotFoundException e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Entity not found error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            OSMLogger.log(this.getClass(), OSMLogger.LogLevel.ERROR, "[updateExchangePricingAndCreateOilTransactionOut] Unexpected error during exchange pricing processing: " + e.getMessage(), e);
            throw new RuntimeException("Failed to process exchange pricing", e);
        }

        OSMLogger.logMethodExit(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", null);
        OSMLogger.logPerformance(this.getClass(), "updateExchangePricingAndCreateOilTransactionOut", startTime, System.currentTimeMillis());
    }

    public UnifiedDeliveryDTO getByOliveLotNumber(UUID id) {
        var t = deliveryRepository.findByLotOliveNumber(id);
        if (Objects.isNull(t)) {
            return null;
        }
        return modelMapper.map(t, UnifiedDeliveryDTO.class);
    }

    public UnifiedDeliveryDTO getByLotNumber(String lotNumber) {
        var t = deliveryRepository.findByLotNumberAndDeliveryType(lotNumber, DeliveryType.OLIVE);
        return modelMapper.map(t, UnifiedDeliveryDTO.class);
    }

    @Transactional
    public void processPayment(PaymentDTO paymentDTO) {
        if (paymentDTO.getIdOperation() == null) {
            return;
        }
        double amount = paymentDTO.getAmount() != null ? paymentDTO.getAmount() : 0d;

        UnifiedDelivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(paymentDTO.getIdOperation()).orElse(null);

        if (delivery == null) {
            throw new IllegalArgumentException("Oil Sale not found for ID: " + paymentDTO.getIdOperation());
        } else {
            updateDelivery(delivery, amount);
        }

        switch (delivery.getOperationType()) {
            case OIL_PURCHASE ->
                    prepareFinanacalTransaction(paymentDTO, amount, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.OIL_PURCHASE);
            case OLIVE_PURCHASE ->
                    prepareFinanacalTransaction(paymentDTO, amount, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.OLIVE_PURCHASE);
            case BASE -> {
//                UnifiedDelivery olivedelivery = deliveryRepository.findByLotNumberAndDeliveryType(delivery.getLotNumber(), DeliveryType.OLIVE);
//                olivedelivery.setPaid(true);
//                deliveryRepository.save(olivedelivery);
                prepareFinanacalTransaction(paymentDTO, amount, delivery, TransactionDirection.OUTBOUND, TransactionType.PURCHASE, OperationType.BASE);
            }
            case SIMPLE_RECEPTION ->
                    prepareFinanacalTransaction(paymentDTO, amount, delivery, TransactionDirection.INBOUND, TransactionType.PAYMENT, OperationType.SIMPLE_RECEPTION);
        }

    }

    @Transactional
    public void updateDelivery(UnifiedDelivery d, double amountDelta) {
        // 1) Resolve total (use stored total; fallback to unit * qty if missing)
        double total = r3(safe(d.getPrice()));
        if (total <= 0.0) {
            total = r3(safe(d.getUnitPrice()) * safe(d.getOilQuantity())); // remove this block if you never want fallback
        }

        // 2) Current paid/unpaid
        double paid = r3(safe(d.getPaidAmount()));
        paid = clamp(paid, 0.0, total);
        double unpaid = r3(Math.max(0.0, total - paid));

        // 3) Normalize delta: cap overpay/refund
        double change = r3(amountDelta);
        if (change >= 0.0) {
            change = Math.min(change, unpaid);          // cannot overpay
        } else {
            change = -Math.min(Math.abs(change), paid); // cannot refund more than paid
        }

        // 4) Apply & round
        double newPaid = r3(clamp(paid + change, 0.0, total));
        double newUnpaid = r3(Math.max(0.0, total - newPaid));
        boolean fullyPaid = (newUnpaid == 0.0); // safe since rounded to 3

        // 5) Persist
        d.setPrice(r3(total));
        d.setPaidAmount(newPaid);
        d.setUnpaidAmount(newUnpaid);
        d.setPaid(fullyPaid);

        deliveryRepository.save(d);
    }

    private void prepareFinanacalTransaction(PaymentDTO paymentDTO, double amount, UnifiedDelivery delivery, TransactionDirection direction, TransactionType transactionType, OperationType simpleReception) {
        // Build Financial Transaction DTO
        FinancialTransactionDto financialTransactionDto = new FinancialTransactionDto();
        financialTransactionDto.setTransactionType(transactionType);
        financialTransactionDto.setDirection(direction);
        financialTransactionDto.setAmount(BigDecimal.valueOf(amount));
        financialTransactionDto.setCurrency(paymentDTO.getCurrency() != null ? paymentDTO.getCurrency() : Currency.TND);
        financialTransactionDto.setPaymentMethod(paymentDTO.getPaymentMethod() != null ? paymentDTO.getPaymentMethod() : PaymentMethod.CASH);
        financialTransactionDto.setBankAccount(paymentDTO.getBankAccount() != null ? paymentDTO.getBankAccount() : null);
        financialTransactionDto.setCheckNumber(paymentDTO.getCheckNumber() != null ? paymentDTO.getCheckNumber() : null);
        financialTransactionDto.setLotNumber(delivery.getLotNumber());
        financialTransactionDto.setsupplier((paymentDTO.getSupplier() != null) ? paymentDTO.getSupplier() : null);
        financialTransactionDto.setTransactionDate(LocalDateTime.now());
        financialTransactionDto.setApproved(true);
        financialTransactionDto.setApprovalDate(LocalDateTime.now());
        financialTransactionDto.setOperationType(simpleReception);
        financialTransactionDto.setExternalTransactionId(delivery.getExternalId().toString());
        financialTransactionDto.setResourceName(ResourceName.UnifiedDelivery);

        // Send to finance service
        financialTransactionFeignService.create(financialTransactionDto);
    }

    @Override
    @Transactional
    public UnifiedDeliveryDTO delete(UUID id) {
        OSMLogger.logMethodEntry(this.getClass(), "delete", id);
        try {
            if (id == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Delete ID is null: {}", id);
                return null;
            }
            UnifiedDelivery entity = repository.findById(id).orElse(null);
            if (entity == null) {
                OSMLogger.log(this.getClass(), OSMLogger.LogLevel.WARN, "Entity with ID {} not found for deletion", id);
                return null;
            }
            entity.setDeleted(true);
            UnifiedDelivery updatedEntity = repository.save(entity);
            Set<QualityControlResult> controlResults = entity.getQualityControlResults().stream().map(qc -> {
                QualityControlResult result = qualityControlResultRepository.findById(qc.getId()).orElse(null);
                if (result != null) {
                    result.setDeleted(true);
                    return result;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
            qualityControlResultRepository.saveAll(controlResults);
            UnifiedDeliveryDTO result = modelMapper.map(updatedEntity, outDTOClass);
            return result;
        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error deleting entity with ID: " + id, e);
            throw e;
        }
    }

    public UnifiedDeliveryDTO getByLotNumberAndType(String lotNumber, DeliveryType deliveryType) {

        try {
            // Make enum lookup case-insensitive and trim spaces

            var entity = deliveryRepository.findByLotNumberAndDeliveryType(lotNumber, deliveryType);
            if (entity == null) {
                return null;
            }

            return modelMapper.map(entity, UnifiedDeliveryDTO.class);

        } catch (Exception e) {
            OSMLogger.logException(this.getClass(), "Error getByLotNumberAndType  : ", e);
            return null;
        }
    }

    public List<UnifiedDeliveryDTO> getDeliveriesByGlobalLotNumber(String lotNumber) {
        List<UnifiedDelivery> deliveries = deliveryRepository.findByGlobalLotNumberAndDeliveryType(lotNumber, DeliveryType.OLIVE);
        List<UnifiedDeliveryDTO> result = new ArrayList<>(deliveries.size());
        deliveries.forEach(d -> result.add(modelMapper.map(d, UnifiedDeliveryDTO.class)));
        return result;
    }


}
