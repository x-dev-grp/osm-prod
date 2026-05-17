UPDATE parameter
SET category = 'PRODUCTION'
WHERE code IN ('OLIVE_UNIT_PRICE', 'DAILY_OIL_METRIC')
  AND category <> 'PRODUCTION';

WITH tenant_ids AS (
    SELECT DISTINCT tenant_id
    FROM parameter
    WHERE tenant_id IS NOT NULL
    UNION
    SELECT '4b322fea-6825-4c4c-9534-021cd150d112'::uuid
)
INSERT INTO parameter
(id, created_by, created_date, tenant_id, category, code, description, is_active, type, value)
SELECT
    gen_random_uuid(),
    'system',
    NOW(),
    tenant_id,
    'PRODUCTION',
    'OLIVE_UNIT_PRICE',
    'Default price per KG of olives',
    TRUE,
    'DOUBLE',
    '3.50'
FROM tenant_ids t
WHERE NOT EXISTS (
    SELECT 1
    FROM parameter p
    WHERE p.tenant_id = t.tenant_id
      AND p.code = 'OLIVE_UNIT_PRICE'
);

WITH tenant_ids AS (
    SELECT DISTINCT tenant_id
    FROM parameter
    WHERE tenant_id IS NOT NULL
    UNION
    SELECT '4b322fea-6825-4c4c-9534-021cd150d112'::uuid
)
INSERT INTO parameter
(id, created_by, created_date, tenant_id, category, code, description, is_active, type, value)
SELECT
    gen_random_uuid(),
    'system',
    NOW(),
    tenant_id,
    'PRODUCTION',
    'DAILY_OIL_METRIC',
    'Daily dashboard metric (current and history JSON)',
    TRUE,
    'STRING',
    '{"current":0,"history":[]}'
FROM tenant_ids t
WHERE NOT EXISTS (
    SELECT 1
    FROM parameter p
    WHERE p.tenant_id = t.tenant_id
      AND p.code = 'DAILY_OIL_METRIC'
);
