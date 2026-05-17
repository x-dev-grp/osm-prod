UPDATE public.parameter
SET is_deleted = false
WHERE code IN ('OLIVE_UNIT_PRICE', 'DAILY_OIL_METRIC')
  AND tenant_id = '4b322fea-6825-4c4c-9534-021cd150d112'
  AND is_deleted IS NULL;