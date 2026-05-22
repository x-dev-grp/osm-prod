ALTER TABLE IF EXISTS quality_control_result
    ADD COLUMN IF NOT EXISTS filtration_operation_id uuid,
    ADD COLUMN IF NOT EXISTS traceability_lot_id uuid;

CREATE INDEX IF NOT EXISTS idx_quality_control_result_filtration_operation
    ON quality_control_result (filtration_operation_id);

CREATE INDEX IF NOT EXISTS idx_quality_control_result_traceability_lot
    ON quality_control_result (traceability_lot_id);
