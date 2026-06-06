package com.osm.oilproductionservice.service;

import com.osm.oilproductionservice.dto.StorageUnitDto;
import com.osm.oilproductionservice.model.StorageUnit;
import com.osm.oilproductionservice.model.Supplier;
import com.osm.oilproductionservice.repository.StorageUnitRepo;
import com.osm.oilproductionservice.repository.SupplierRepository;
import com.xdev.xdevbase.config.TenantContext;
import com.xdev.xdevbase.models.Action;
import com.xdev.xdevbase.qr.model.QrResolveResponse;
import com.xdev.xdevbase.repos.BaseRepository;
import com.xdev.xdevbase.services.impl.BaseServiceImpl;
import com.xdev.xdevbase.utils.OSMLogger;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageUnitService extends BaseServiceImpl<StorageUnit, StorageUnitDto, StorageUnitDto> {
    private final StorageUnitRepo storageUnitRepo;
    private final SupplierRepository supplierRepository;
    public StorageUnitService(BaseRepository<StorageUnit> repository, ModelMapper modelMapper, StorageUnitRepo storageUnitRepo, SupplierRepository supplierRepository1) {
        super(repository, modelMapper);
        this.storageUnitRepo = storageUnitRepo;
        this.supplierRepository = supplierRepository1;
    }
    @Transactional
    public void changeSupplier(UUID storageId, UUID supplierId) {
        StorageUnit storageUnit = storageUnitRepo.findById(storageId)
                .orElseThrow(() -> new EntityNotFoundException("Storage unit with id " + storageId + " not found"));

        Supplier currentSupplier = storageUnit.getSupplier();

        if (currentSupplier != null) {
            currentSupplier.setHasStorage(false);
            currentSupplier.setStorageUnit(null);
            storageUnit.setSupplier(null);
            supplierRepository.save(currentSupplier);
        }

        if (supplierId != null) {
            Supplier newSupplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier with id " + supplierId + " not found"));

            newSupplier.setHasStorage(true);
            newSupplier.setStorageUnit(storageUnit);
            storageUnit.setSupplier(newSupplier);

            supplierRepository.save(newSupplier);
        }

        storageUnitRepo.save(storageUnit);
    }

    @Override
    @Transactional(readOnly = true)
    public StorageUnitDto findById(UUID id) {
        return enrichQrFields(super.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StorageUnitDto> findAll() {
        return super.findAll().stream().map(this::enrichQrFields).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StorageUnitDto> findAll(int page, int size, String sort, String direction) {
        return super.findAll(page, size, sort, direction).map(this::enrichQrFields);
    }

    private String normalizeHex(String code) {
        if (isBlank(code)) {
            throw new IllegalArgumentException("Code cannot be null or blank");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private StorageUnitDto enrichQrFields(StorageUnitDto dto) {
        if (dto == null) {
            return null;
        }

        if (isBlank(dto.getPublicCode()) && !isBlank(dto.getQrHex())) {
            dto.setPublicCode(dto.getQrHex());
        }
        if (isBlank(dto.getQrHex()) && !isBlank(dto.getPublicCode())) {
            dto.setQrHex(dto.getPublicCode());
        }

        if (isBlank(dto.getQrUrl()) && !isBlank(dto.getPublicCode())) {
            try {
                dto.setQrUrl(getQrUrlForPublicCode(dto.getPublicCode()));
            } catch (UnsupportedOperationException ignored) {
                // QR URL config is optional in some environments.
            }
        }

        if (dto.getQrImageBase64() != null && dto.getQrImageBase64().isBlank()) {
            dto.setQrImageBase64(null);
        }

        return dto;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    protected String getEntityType() {
        return "STORAGEUNIT";
    }

    @Override
    protected String getLabel(StorageUnit entity) {
        if (entity == null) {
            return "Storage Unit";
        }
        if (!isBlank(entity.getName())) {
            return entity.getName();
        }
        return "Storage Unit " + entity.getId();
    }

    @Override
    protected String getStatus(StorageUnit entity) {
        if (entity == null || entity.getStatus() == null) {
            return "UNKNOWN";
        }
        return entity.getStatus().name();
    }

    @Override
    protected String getMobileRoute() {
        return "/storage";
    }

    @Override
    protected String getWebRoute(StorageUnit entity) {
        if (entity == null || entity.getId() == null) {
            return "/storage";
        }
        return "/storage/" + entity.getId() + "/view";
    }

    @Override
    protected Object getData(StorageUnit entity) {
        return enrichQrFields(modelMapper.map(entity, StorageUnitDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(String publicCode) {
        if (publicCode == null || publicCode.isBlank()) {
            throw new IllegalArgumentException("Le code est obligatoire");
        }

        String normalizedCode = normalizeHex(publicCode);
        UUID tenantId = TenantContext.getCurrentTenant();

        Optional<StorageUnit> entity = (tenantId == null)
                ? storageUnitRepo.findByQrHexIgnoreCaseAndIsDeletedFalse(normalizedCode)
                : storageUnitRepo.findByQrHexIgnoreCaseAndTenantIdAndIsDeletedFalse(normalizedCode, tenantId);

        if (entity.isEmpty() && tenantId != null) {
            entity = storageUnitRepo.findByQrHexIgnoreCaseAndIsDeletedFalse(normalizedCode);
        }

        return entity.map(unit -> {
                    QrResolveResponse response = new QrResolveResponse();
                    response.setEntityType(getEntityType());
                    response.setPublicCode(normalizedCode);
                    response.setEntityId(unit.getId().toString());
                    response.setLabel(getLabel(unit));
                    response.setStatus(getStatus(unit));
                    response.setMobileRoute(getMobileRoute());
                    response.setWebRoute(getWebRoute(unit));
                    response.setData(getData(unit));
                    return response;
                })
                .orElseThrow(() -> new EntityNotFoundException("Storage unit not found for code: " + publicCode));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QrResolveResponse> searchByCode(String code) {
        try {
            return Optional.ofNullable(resolve(code));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<Action> actionsMapping(StorageUnit storageUnit) {
        Set<Action> actions = new HashSet<>();
        actions.addAll(Set.of(Action.UPDATE, Action.DELETE, Action.READ,Action.ASSIGN_SUPPLIER));

        return actions;
    }
}
