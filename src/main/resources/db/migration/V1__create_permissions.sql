-- Supplier permission
INSERT INTO permission (name, description, type, entity, module, custom_permission)
VALUES ('CREATE_Supplier', 'Permission to create supplier', 'CREATE', 'Supplier', 'PRODUCTION', NULL),
       ('READ_Supplier', 'Permission to read supplier', 'READ', 'Supplier', 'PRODUCTION', NULL),
       ('UPDATE_Supplier', 'Permission to update supplier', 'UPDATE', 'Supplier', 'PRODUCTION', NULL),
       ('DELETE_Supplier', 'Permission to delete supplier', 'DELETE', 'Supplier', 'PRODUCTION', NULL),
       ('APPROVE_SUPPLIER', 'Permission to approve supplier', 'CUSTOM', 'Supplier', 'PRODUCTION', 'APPROVE_SUPPLIER'),
       ('MANAGE_SUPPLIER_CONTRACTS', 'Permission to manage supplier contracts', 'CUSTOM', 'Supplier', 'PRODUCTION',
        'MANAGE_SUPPLIER_CONTRACTS');

-- Quality Control Rule permission
INSERT INTO permission (name, description,tenent_id, type, entity, module, custom_permission)
VALUES ('CREATE_QualityControlRule', 'Permission to create quality control rule', 'CREATE', 'QualityControlRule',
        'PRODUCTION', NULL),
       ('READ_QualityControlRule', 'Permission to read quality control rule', 'READ', 'QualityControlRule',
        'PRODUCTION', NULL),
       ('UPDATE_QualityControlRule', 'Permission to update quality control rule', 'UPDATE', 'QualityControlRule',
        'PRODUCTION', NULL),
       ('DELETE_QualityControlRule', 'Permission to delete quality control rule', 'DELETE', 'QualityControlRule',
        'PRODUCTION', NULL),
       ('ACTIVATE_QUALITY_RULE', 'Permission to activate quality rule', 'CUSTOM', 'QualityControlRule', 'PRODUCTION',
        'ACTIVATE_QUALITY_RULE'),
       ('DEACTIVATE_QUALITY_RULE', 'Permission to deactivate quality rule', 'CUSTOM', 'QualityControlRule',
        'PRODUCTION', 'DEACTIVATE_QUALITY_RULE'),
       ('VIEW_QUALITY_RULE_HISTORY', 'Permission to view quality rule history', 'CUSTOM', 'QualityControlRule',
        'PRODUCTION', 'VIEW_QUALITY_RULE_HISTORY');

-- Machine Plan permission
INSERT INTO permission (name, description, type, entity, module, custom_permission)
VALUES ('CREATE_MachinePlan', 'Permission to create machine plan', 'CREATE', 'MachinePlan', 'PRODUCTION', NULL),
       ('READ_MachinePlan', 'Permission to read machine plan', 'READ', 'MachinePlan', 'PRODUCTION', NULL),
       ('UPDATE_MachinePlan', 'Permission to update machine plan', 'UPDATE', 'MachinePlan', 'PRODUCTION', NULL),
       ('DELETE_MachinePlan', 'Permission to delete machine plan', 'DELETE', 'MachinePlan', 'PRODUCTION', NULL),
       ('APPROVE_MACHINE_PLAN', 'Permission to approve machine plan', 'CUSTOM', 'MachinePlan', 'PRODUCTION',
        'APPROVE_MACHINE_PLAN'),
       ('VIEW_MACHINE_PLAN_HISTORY', 'Permission to view machine plan history', 'CUSTOM', 'MachinePlan', 'PRODUCTION',
        'VIEW_MACHINE_PLAN_HISTORY'),
       ('MANAGE_MACHINE_SCHEDULE', 'Permission to manage machine schedule', 'CUSTOM', 'MachinePlan', 'PRODUCTION',
        'MANAGE_MACHINE_SCHEDULE');

-- Transporter permission
INSERT INTO permission (name, description, type, entity, module, custom_permission)
VALUES ('CREATE_Transporter', 'Permission to create transporter', 'CREATE', 'Transporter', 'PRODUCTION', NULL),
       ('READ_Transporter', 'Permission to read transporter', 'READ', 'Transporter', 'PRODUCTION', NULL),
       ('UPDATE_Transporter', 'Permission to update transporter', 'UPDATE', 'Transporter', 'PRODUCTION', NULL),
       ('DELETE_Transporter', 'Permission to delete transporter', 'DELETE', 'Transporter', 'PRODUCTION', NULL),
       ('ASSIGN_TRANSPORT', 'Permission to assign transport', 'CUSTOM', 'Transporter', 'PRODUCTION',
        'ASSIGN_TRANSPORT'),
       ('TRACK_TRANSPORT', 'Permission to track transport', 'CUSTOM', 'Transporter', 'PRODUCTION', 'TRACK_TRANSPORT'),
       ('MANAGE_TRANSPORTER_INFO', 'Permission to manage transporter information', 'CUSTOM', 'Transporter',
        'PRODUCTION', 'MANAGE_TRANSPORTER_INFO'),
       ('VIEW_TRANSPORT_HISTORY', 'Permission to view transport history', 'CUSTOM', 'Transporter', 'PRODUCTION',
        'VIEW_TRANSPORT_HISTORY'),
       ('RATE_TRANSPORTER', 'Permission to rate transporter', 'CUSTOM', 'Transporter', 'PRODUCTION',
        'RATE_TRANSPORTER');

-- Base Type permission
INSERT INTO permission (name, description, type, entity, module, custom_permission)
VALUES ('CREATE_BaseType', 'Permission to create base type', 'CREATE', 'BaseType', 'PRODUCTION', NULL),
       ('READ_BaseType', 'Permission to read base type', 'READ', 'BaseType', 'PRODUCTION', NULL),
       ('UPDATE_BaseType', 'Permission to update base type', 'UPDATE', 'BaseType', 'PRODUCTION', NULL),
       ('DELETE_BaseType', 'Permission to delete base type', 'DELETE', 'BaseType', 'PRODUCTION', NULL),
       ('MANAGE_BASE_TYPES', 'Permission to manage base types', 'CUSTOM', 'BaseType', 'PRODUCTION',
        'MANAGE_BASE_TYPES'),
       ('VIEW_BASE_TYPE_HISTORY', 'Permission to view base type history', 'CUSTOM', 'BaseType', 'PRODUCTION',
        'VIEW_BASE_TYPE_HISTORY');