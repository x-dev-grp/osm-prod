-- ====== 0) Pré-requis & idempotence ======
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- pour gen_random_uuid()

-- Index d'unicité pour éviter les doublons
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM   pg_indexes
            WHERE  schemaname = 'public'
              AND    indexname = 'uq_permission_mod_entity_name'
        ) THEN
            EXECUTE 'CREATE UNIQUE INDEX uq_permission_mod_entity_name
             ON public.permission(module, entity, permission_name)';
        END IF;
    END;
$$ LANGUAGE plpgsql;

-- ====== 1) Fonction de seed depuis un JSON ======
CREATE OR REPLACE FUNCTION public.seed_permissions_from_json(spec jsonb)
    RETURNS void
    LANGUAGE plpgsql
AS $$
DECLARE
    ent_key  text;     -- nom d'entité (clé du JSON)
    ent_val  jsonb;    -- objet {module, permissions, ...}
    mod_txt  text;     -- "FINANCE" | "RECEPTION" | ...
    mod_int  smallint; -- 0..4
    action   text;     -- "READ" | "CREATE" | ...
    v_created int := 0;
    v_skipped int := 0;
BEGIN
    -- Parcourt spec.entities
    FOR ent_key, ent_val IN
        SELECT key, value
        FROM jsonb_each(spec->'entities')
        LOOP
            mod_txt := UPPER(trim((ent_val->>'module')));
            -- Mapping texte -> smallint (conforme à OSMModule)
            mod_int :=
                    CASE mod_txt
                        WHEN 'HR'           THEN 0
                        WHEN 'RECEPTION'    THEN 1
                        WHEN 'PRODUCTION'   THEN 2
                        WHEN 'FINANCE'      THEN 3
                        WHEN 'HABILITATION' THEN 4
                        ELSE NULL
                        END;

            IF mod_int IS NULL THEN
                RAISE WARNING 'Module inconnu "%" pour entité "%", on ignore.', mod_txt, ent_key;
                CONTINUE;
            END IF;

            -- Itère le tableau permissions
            FOR action IN
                SELECT trim(UPPER(value::text), '"')
                FROM jsonb_array_elements_text(ent_val->'permissions')
                LOOP
                    -- Ignore lignes vides
                    IF action IS NULL OR action = '' THEN
                        CONTINUE;
                    END IF;

                    -- Insert idempotent: si (module, entity, permission_name) existe déjà -> DO NOTHING
                    INSERT INTO public.permission
                    (id, created_by, created_date, external_id, is_deleted, last_modified_by, last_modified_date,
                     tenant_id, entity, module, permission_name)
                    VALUES
                        (gen_random_uuid(), NULL, NOW(), gen_random_uuid(), FALSE, NULL, NOW(),
                         NULL, ent_key, mod_int, action)
                    ON CONFLICT (module, entity, permission_name) DO NOTHING;

                    IF FOUND THEN
                        v_created := v_created + 1;
                    ELSE
                        v_skipped := v_skipped + 1;
                    END IF;
                END LOOP;
        END LOOP;

    RAISE NOTICE 'Seed terminé. Créés=%, Ignorés(existaient déjà)=%', v_created, v_skipped;
END;
$$;

-- ====== 2) Exemple d'appel : colle ton JSON entre $$ ... $$ ======
-- Remplace le contenu par TON fichier "permisisons and modules .json"
SELECT public.seed_permissions_from_json($$
{
  "entities": {
    "BANKACCOUNT": {
      "description": "Bank account management",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "base_type": {
      "description": "Generic type system",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "EXPENSE": {
      "description": "Expense management",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "APPROVE",
        "REJECT",
        "VALIDATE",
        "PAY",
        "GEN_PDF"
      ]
    },
    "FINANCIALTRANSACTION": {
      "description": "Universal financial transactions",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "APPROVE",
        "REJECT",
        "VALIDATE",
        "PAY",
        "GEN_PDF",
        "COMPLETE_PAYMENT_DETAILS"
      ]
    },
    "OILCREDIT": {
      "description": "Oil credit management",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "APPROVE",
        "REJECT",
        "VALIDATE",
        "COMPLETE",
        "GEN_PDF"
      ]
    },
    "SUPPLIER": {
      "description": "Supplier management",
      "module": "RECEPTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ASSIGN_SUPPLIER",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "SUPPLIERINFO": {
      "description": "Supplier information details",
      "module": "RECEPTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "GEN_PDF"
      ]
    },
    "UNIFIEDDELIVERY": {
      "description": "Delivery management",
      "module": "RECEPTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "COMPLETE",
        "TO_PROD",
        "GEN_PDF",
        "GEN_PDF_QC_OIL",
        "GEN_PDF_QC_OLIVE",
        "PLANNING"
      ]
    },
    "MACHINEPLAN": {
      "description": "Machine planning and scheduling",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "APPROVE",
        "REJECT",
        "MAINTENANCE",
        "GEN_PDF"
      ]
    },
    "MILLMACHINE": {
      "description": "Mill machine management",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "MAINTENANCE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "OILTRANSACTION": {
      "description": "Oil transaction management",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "OIL_IN_TRANSACTION",
        "OIL_OUT_TRANSACTION",
        "OIL_PAYMENT",
        "VALIDATE",
        "GEN_PDF",
        "COMPLETE"
      ]
    },
    "PARAMETER": {
      "description": "System parameters management",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "QUALITYCONTROLRESULT": {
      "description": "Quality control results",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "OLIVE_QUALITY",
        "OIL_QUALITY",
        "UPDATE_OLIVE_QUALITY",
        "UPDATE_OIL_QUALITY",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "QUALITYCONTROLRULE": {
      "description": "Quality control rules",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "STORAGEUNIT": {
      "description": "Storage unit management",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "SET_PRICE",
        "GEN_PDF"
      ]
    },
    "TRANSPORTER": {
      "description": "Transporter management",
      "module": "RECEPTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "OILSALE": {
      "description": "Oil sales management",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "CANCEL",
        "VALIDATE",
        "APPROVE",
        "REJECT",
        "PAY",
        "GEN_PDF",
        "COMPLETE"
      ]
    },
    "COMPANYPROFILE": {
      "description": "Company profile management",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "ROLE": {
      "description": "Role management",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "GEN_PDF"
      ]
    },
    "PERMISSION": {
      "description": "Permission management",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "GEN_PDF"
      ]
    },
    "OSMUSER": {
      "description": "User management",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "GEN_PDF"
      ]
    },
    "CONTRACT": {
      "description": "Employee contracts",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "DEPARTMENT": {
      "description": "Departments",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "EMPLOYEE": {
      "description": "Employee profiles",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "LEAVEREQUEST": {
      "description": "Employee leave requests",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "PAYROLL": {
      "description": "Payroll runs and items",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "POINTAGE": {
      "description": "Time clock entries",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "POSTE": {
      "description": "Job positions",
      "module": "HR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    }
  },
  "modules": {
    "HR": {
      "value": 0,
      "description": "Human Resources module"
    },
    "RECEPTION": {
      "value": 1,
      "description": "Reception and supplier management"
    },
    "PRODUCTION": {
      "value": 2,
      "description": "Production and quality control"
    },
    "FINANCE": {
      "value": 3,
      "description": "Financial management"
    },
    "HABILITATION": {
      "value": 4,
      "description": "System administration and configuration"
    }
  },
  "actions": {
    "READ": "View entity records",
    "CREATE": "Create new entity records",
    "UPDATE": "Modify existing entity records",
    "DELETE": "Remove entity records",
    "CANCEL": "Cancel operations or transactions",
    "OLIVE_QUALITY": "Manage olive quality control",
    "OIL_QUALITY": "Manage oil quality control",
    "UPDATE_OLIVE_QUALITY": "Update olive quality parameters",
    "UPDATE_OIL_QUALITY": "Update oil quality parameters",
    "TO_PROD": "Move to production workflow",
    "COMPLETE": "Complete operations or transactions",
    "OIL_PAYMENT": "Process oil-related payments",
    "OIL_OUT_TRANSACTION": "Process oil output transactions",
    "OIL_IN_TRANSACTION": "Process oil input transactions",
    "OIL_RECEPTION": "Manage oil reception processes",
    "SET_PRICE": "Set pricing for entities",
    "ASSIGN_SUPPLIER": "Assign suppliers to operations",
    "COMPLETE_PAYMENT_DETAILS": "Complete payment information",
    "VALIDATE": "Validate operations or data",
    "PAY": "Process payments",
    "GEN_PDF": "Generate PDF documents",
    "APPROVE": "Approve operations or requests",
    "REJECT": "Reject operations or requests",
    "MAINTENANCE": "Perform maintenance operations",
    "PLANNING": "Manage planning and scheduling",
    "DELIVERYHISTORY": "Manage the history of deliveries"
  },
  "security_entities": {
    "COMPANYPROFILE": {
      "table": "osmsecurity.public.company_profile",
      "description": "Company profile information",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "ROLE": {
      "table": "osmsecurity.public.role",
      "description": "User roles and permissions",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "PERMISSION": {
      "table": "osmsecurity.public.permission",
      "description": "System permissions",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "OSMUSER": {
      "table": "osmsecurity.public.osmuser",
      "description": "System users",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    }
  }
}
$$::jsonb);
