-- Existing example you shared
INSERT INTO parameter
(id, created_by, created_date, tenant_id, category, code, description, is_active, type, value)
VALUES
    -- Pricing
    (gen_random_uuid(), 'system', NOW(),
     '4b322fea-6825-4c4c-9534-021cd150d112'::uuid,
     'PRICING', 'OLIVE_UNIT_PRICE', 'Default price per KG of olives', TRUE, 'DOUBLE', '3.50'),

    -- Daily metric (single param, JSON payload: current + rolling history)
    (gen_random_uuid(), 'system', NOW(),
     '4b322fea-6825-4c4c-9534-021cd150d112'::uuid,
     'DASHBOARD',                              -- pick any category you already use (e.g., 'DASHBOARD')
     'DAILY_OIL_METRIC',                       -- the key you’ll read/write from the app
     'Daily dashboard metric (current and history JSON)',
     TRUE,
     'STRING',                                   -- keep as TEXT; we’ll store JSON as a string
     '{"current":0,"history":[]}');
