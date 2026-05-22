-- Immutable production lot backbone for end-to-end oil traceability.
-- NOTE:
--   This repository currently does not include a Flyway/Liquibase runtime dependency,
--   so this script should be executed manually during rollout unless a migration runner
--   is added later.

CREATE TABLE IF NOT EXISTS traceability_lot (
    id uuid PRIMARY KEY,
    tenant_id uuid,
    is_deleted boolean NOT NULL DEFAULT false,
    created_by varchar(255),
    created_date timestamp,
    last_modified_by varchar(255),
    last_modified_date timestamp,
    external_id uuid,
    qr_hex varchar(6),
    qr_image_base64 text,

    lot_number varchar(120) NOT NULL,
    source_type varchar(40) NOT NULL,
    source_entity_id uuid,
    root_reception_id uuid,
    parent_lot_id uuid,
    storage_unit_id uuid,
    filtration_operation_id uuid,
    quality_grade varchar(255),
    oil_variety varchar(255),
    quantity double precision,
    captured_at timestamp NOT NULL,
    active boolean NOT NULL DEFAULT true,
    source_snapshot_json text
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_traceability_lot_external_id
    ON traceability_lot (external_id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_storage_unit
    ON traceability_lot (storage_unit_id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_root_reception
    ON traceability_lot (root_reception_id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_parent
    ON traceability_lot (parent_lot_id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_filtration_operation
    ON traceability_lot (filtration_operation_id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_active_storage
    ON traceability_lot (storage_unit_id, active, is_deleted);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_lot_number
    ON traceability_lot (lot_number);

CREATE TABLE IF NOT EXISTS traceability_lot_aud (
    rev integer NOT NULL,
    revtype smallint,

    id uuid NOT NULL,
    tenant_id uuid,
    is_deleted boolean,
    created_by varchar(255),
    created_date timestamp,
    last_modified_by varchar(255),
    last_modified_date timestamp,
    external_id uuid,
    qr_hex varchar(6),
    qr_image_base64 text,

    lot_number varchar(120),
    source_type varchar(40),
    source_entity_id uuid,
    root_reception_id uuid,
    parent_lot_id uuid,
    storage_unit_id uuid,
    filtration_operation_id uuid,
    quality_grade varchar(255),
    oil_variety varchar(255),
    quantity double precision,
    captured_at timestamp,
    active boolean,
    source_snapshot_json text,

    PRIMARY KEY (rev, id)
);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_aud_id
    ON traceability_lot_aud (id);

CREATE INDEX IF NOT EXISTS idx_traceability_lot_aud_rev
    ON traceability_lot_aud (rev);

COMMENT ON TABLE traceability_lot IS 'Immutable oil genealogy lots derived from reception or filtration events.';
COMMENT ON COLUMN traceability_lot.parent_lot_id IS 'Previous immutable lot in the genealogy chain.';
COMMENT ON COLUMN traceability_lot.root_reception_id IS 'Reception/delivery root for the entire genealogy branch.';
