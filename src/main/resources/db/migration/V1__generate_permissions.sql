-- Function to add basic CRUD permissions for an entity
CREATE
OR
REPLACE FUNCTION add_basic_permissions(
    p_entity_name VARCHAR,
    p_module_value INTEGER
) RETURNS void AS $$
BEGIN
-- Add basic CRUD permissions
INSERT INTO permission (id, permission_name, entity, module, tenant_id,create_date_time, modified_date_time,external_id)
VALUES (gen_random_uuid(), 'CREATE', p_entity_name, p_module_value, null,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,gen_random_uuid()),
       (gen_random_uuid(), 'READ', p_entity_name, p_module_value, null,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,gen_random_uuid()),
       (gen_random_uuid(), 'UPDATE', p_entity_name, p_module_value, null,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,gen_random_uuid()),
       (gen_random_uuid(), 'DELETE', p_entity_name, p_module_value, null,CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,gen_random_uuid());
END;
$$ LANGUAGE plpgsql;

-- Function to add custom permission for an entity
CREATE OR
REPLACE FUNCTION add_custom_permission(
    p_entity_name VARCHAR,
    p_permission_name VARCHAR,
    p_module_value INTEGER
) RETURNS void AS $$
BEGIN
-- Add custom permission
INSERT INTO permission (id, permission_name, entity, module, tenant_id,create_date_time, modified_date_time,external_id)
VALUES (gen_random_uuid(),
        p_permission_name,
        p_entity_name,
        p_module_value,
        null, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,gen_random_uuid());
END;
$$ LANGUAGE plpgsql;

-- Generate basic permissions for all entities
DO $$
BEGIN
    -- Production Service Entities (2)
    PERFORM add_basic_permissions('DELIVERY', 2);
PERFORM add_basic_permissions('COMPANY_PROFILE', 2);
PERFORM add_basic_permissions('MILL_MACHINE', 2);
PERFORM add_basic_permissions('TRANSPORTER', 2);
PERFORM add_basic_permissions('LOT', 2);
PERFORM add_basic_permissions('QUALITY_CONTROL_RULE', 2);
PERFORM add_basic_permissions('BASE_TYPE', 2);
PERFORM add_basic_permissions('SUPPLIER', 2);
PERFORM add_basic_permissions('GLOBAL_LOT', 2);
PERFORM add_basic_permissions('MACHINE_PLAN', 2);
PERFORM add_basic_permissions('STORAGE_UNIT', 2);
PERFORM add_basic_permissions('QUALITY_CONTROL_RESULT', 2);
    
    -- HR Service Entities (0)
PERFORM add_basic_permissions('USER', 0);
PERFORM add_basic_permissions('ROLE', 0);
PERFORM add_basic_permissions('PERMISSION', 0);
PERFORM add_basic_permissions('MODULE', 0);
PERFORM add_basic_permissions('EMPLOYEE', 0);

    -- Example of how to add custom permissions (commented out for now)
    /*
    -- Example: Add custom permission for DELIVERY entity
    PERFORM add_custom_permission(
        'DELIVERY',
        'APPROVE_DELIVERY',
        2  -- PRODUCTION module
    );
    */
END;
$$; 