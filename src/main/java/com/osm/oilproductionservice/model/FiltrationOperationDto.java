package com.osm.oilproductionservice.model;

import com.osm.oilproductionservice.dto.FiltrationStatus;
import com.osm.oilproductionservice.dto.StorageUnitDto;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link FiltrationOperation}
 */
@Value
public class FiltrationOperationDto implements Serializable {
    UUID id;
    UUID tenantId;
    Boolean isDeleted;
    String createdBy;
    LocalDateTime createdDate;
    String lastModifiedBy;
    LocalDateTime lastModifiedDate;
    UUID externalId;
    String qrHex;
    String qrImageBase64;
    StorageUnitDto sourceStorageUnit;
    FiltrationStatus status;
    LocalDateTime operationDate;
    Double volumeToFilter;
    Double volumeAfter;
    Double lossVolume;
    Double lossPercent;
    String note;
    StorageUnitDto targetStorageUnit;
    String sourceLotNumber;
    String targetLotNumber;
}