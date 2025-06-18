package com.osm.oilproductionservice.enums;


/**
 * Possible statuses for an oil storage unit.
 */
public enum StorageStatus {
    AVAILABLE,        // The storage unit is ready to be used.
    FILLING,          // It is currently being filled.
    FULL,             // It has reached its max capacity.
    MAINTENANCE,      // It is undergoing maintenance or not available.
    CLEANING,         // Cleaning process is ongoing.
    RESERVED,         // Reserved for future use or an upcoming batch.
    OUT_OF_SERVICE    // Decommissioned or unavailable for a longer period.
}
