INSERT INTO parameter
(id, created_by, created_date, tenant_id, category, code, description, is_active, type, value)
VALUES
    -- Pricing
    (gen_random_uuid(), 'system', NOW(),
     '00000000-0000-0000-0000-000000000000'::uuid,
     'PRICING', 'OLIVE_UNIT_PRICE', 'Default price per KG of olives', TRUE, 'DOUBLE', '3.50'),

 