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
            NULL;
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
            FROM jsonb_each(COALESCE(spec->'entities', '{}'::jsonb))
            UNION ALL
            SELECT key, value
            FROM jsonb_each(COALESCE(spec->'security_entities', '{}'::jsonb))
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
                        WHEN 'INVENTAIR'     THEN 5
                        WHEN 'CONDITIONING'  THEN 6
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
                    SELECT gen_random_uuid(), NULL, NOW(), gen_random_uuid(), FALSE, NULL, NOW(),
                           NULL, ent_key, mod_int, action
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM public.permission p
                        WHERE p.module = mod_int
                          AND UPPER(p.entity) = UPPER(ent_key)
                          AND UPPER(p.permission_name) = UPPER(action)
                          AND COALESCE(p.is_deleted, FALSE) = FALSE
                    );

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

-- ====== 1.1) Rename legacy product permission resources ======
DO $$
DECLARE
    legacy_permission RECORD;
    target_permission_id UUID;
BEGIN
    FOR legacy_permission IN
        SELECT id, module, permission_name
        FROM public.permission
        WHERE UPPER(entity) IN ('SKU', 'PRODUCT')
        LOOP
            SELECT id
            INTO target_permission_id
            FROM public.permission
            WHERE module = legacy_permission.module
              AND UPPER(entity) = 'PRODUITFINAL'
              AND permission_name = legacy_permission.permission_name
            LIMIT 1;

            IF target_permission_id IS NULL THEN
                UPDATE public.permission
                SET entity = 'PRODUITFINAL'
                WHERE id = legacy_permission.id;
            ELSE
                INSERT INTO public.role_permissions (role_id, permissions_id)
                SELECT rp.role_id, target_permission_id
                FROM public.role_permissions rp
                WHERE rp.permissions_id = legacy_permission.id
                  AND NOT EXISTS (
                      SELECT 1
                      FROM public.role_permissions existing_rp
                      WHERE existing_rp.role_id = rp.role_id
                        AND existing_rp.permissions_id = target_permission_id
                  );

                DELETE FROM public.role_permissions
                WHERE permissions_id = legacy_permission.id;

                DELETE FROM public.permission
                WHERE id = legacy_permission.id;
            END IF;
        END LOOP;
END;
$$ LANGUAGE plpgsql;

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
        "OLIVE_QUALITY",
        "OIL_QUALITY",
        "UPDATE_OLIVE_QUALITY",
        "UPDATE_OIL_QUALITY",
        "GEN_PDF_QC_OIL",
        "GEN_PDF_QC_OLIVE",
        "GEN_PDF_PRODUCTION",
        "SET_PRICE",
        "PAY",
        "OIL_RECEPTION",
        "COMPLETE_PAYMENT_DETAILS",
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
    "FILTRATIONOPERATION": {
      "description": "Filtration operations",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "START",
        "PAUSE",
        "RESUME",
        "COMPLETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "OILCONTAINER": {
      "description": "Oil containers",
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
    "OILCONTAINERSALE": {
      "description": "Oil container sales",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "CANCEL",
        "VALIDATE",
        "PAY",
        "GEN_PDF"
      ]
    },
    "TRACEABILITYLOT": {
      "description": "Traceability lots",
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
    "WASTE": {
      "description": "Waste management",
      "module": "PRODUCTION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "CANCEL",
        "VALIDATE",
        "PAY",
        "GEN_PDF"
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
    },
    "PAYROLLS": {
      "description": "Payroll runs and items",
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
    },
    "INVENTAIR": {
      "value": 5,
      "description": "Inventory and packaging stock management"
    },
    "CONDITIONING": {
      "value": 6,
      "description": "Conditioning, orders, projects, labels and expedition management"
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
    "DELIVERYHISTORY": "Manage the history of deliveries",
    "START": "Start workflow execution",
    "PAUSE": "Pause workflow execution",
    "RESUME": "Resume workflow execution",
    "CLOSE": "Close workflow execution",
    "SHIP": "Ship expedition or delivery",
    "DELIVER": "Mark expedition or delivery as delivered",
    "ADD_LINE": "Add lines to a document or operation",
    "REMOVE_LINE": "Remove lines from a document or operation",
    "UPDATE_STATUS": "Update business status",
    "DRAFT": "Move content back to draft",
    "FINALIZE": "Finalize content",
    "EXPORT": "Export content or reports",
    "SYNC": "Synchronize mobile/offline data",
    "REPORT": "Generate analytical reports",
    "ENTREE_STOCK":"" ,
    "SORTIE_STOCK":"",
    "AJUSTER_STOCK":"",
    "ASSIGN_EMPLACEMENT":"",
    "RESERVER_STOCK":"",
    "LIBERER_STOCK":"",
    "CHECK_STOCK":"",
    "TRANSFERER_STOCK":""
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
    },
    "CONFIRMATIONCODE": {
      "description": "Security confirmation codes",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "AUTHORIZATION": {
      "description": "OAuth authorizations",
      "module": "HABILITATION",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "CLIENT": {
      "table": "abiooc_inventory.public.clients",
      "description": "System users",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "ARTICLESEC": {
      "description": "Article management",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ENTREE_STOCK",
        "SORTIE_STOCK"
      ]
    },

    "BOM": {
      "description": "Bill of materials",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "BOMLINE": {
      "description": "Bill of materials lines",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "BONCOMMANDE": {
      "description": "Purchase order",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },

    "CLIENT": {
      "description": "Client management",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "EMPLACEMENTSTOCK": {
      "description": "Stock location",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ASSIGN_EMPLACEMENT",
        "RESERVER_STOCK",
        "LIBERER_STOCK",
        "TRANSFERER_STOCK"
      ]
    },

    "FOURNISSEUR": {
      "description": "Supplier management",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "LIGNEBONCOMMANDE": {
      "description": "Purchase order line",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "LIGNECONDITIONNEMENT": {
      "description": "Packaging line",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "MOUVEMENTSTOCKSEC": {
      "description": "Stock movement",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"

      ]
    },

    "PRODUITFINAL": {
      "description": "Finished product",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },

    "STOCKSEC": {
      "description": "Stock management",
      "module": "INVENTAIR",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ENTREE_STOCK" ,
        "SORTIE_STOCK",
        "AJUSTER_STOCK",
        "ASSIGN_EMPLACEMENT",
        "RESERVER_STOCK",
        "LIBERER_STOCK",
        "CHECK_STOCK",
        "TRANSFERER_STOCK"
      ]
    },
    "BASETYPE": {
      "description": "Finance generic type system",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "SUPPLIER": {
      "description": "Finance supplier management",
      "module": "FINANCE",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "OF": {
      "description": "Conditioning manufacturing orders",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "START",
        "PAUSE",
        "RESUME",
        "CLOSE",
        "AJUSTER_STOCK",
        "GEN_PDF"
      ]
    },
    "ORDREFABRICATION": {
      "description": "Conditioning manufacturing orders",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "START",
        "PAUSE",
        "RESUME",
        "CLOSE",
        "AJUSTER_STOCK",
        "GEN_PDF"
      ]
    },
    "LIGNEOF": {
      "description": "Conditioning manufacturing order lines",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "PROJET": {
      "description": "Conditioning projects",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "CANCEL",
        "UPDATE_STATUS",
        "GEN_PDF"
      ]
    },
    "PROJETPRODUIT": {
      "description": "Conditioning project products",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "PROJETRESERVATION": {
      "description": "Conditioning project stock reservations",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "RESERVER_STOCK",
        "LIBERER_STOCK"
      ]
    },
    "CLIENT": {
      "description": "Conditioning clients",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "CERTIFICATION": {
      "description": "Conditioning certifications",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "EXPEDITION": {
      "description": "Conditioning expedition management",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ADD_LINE",
        "REMOVE_LINE",
        "VALIDATE",
        "SHIP",
        "DELIVER",
        "CLOSE",
        "CANCEL",
        "GEN_PDF"
      ]
    },
    "EXPEDITIONARTICLE": {
      "description": "Conditioning expedition lines",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "ADD_LINE",
        "REMOVE_LINE"
      ]
    },
    "QUALITY": {
      "description": "Conditioning quality control",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "UPDATE_STATUS",
        "GEN_PDF"
      ]
    },
    "QCPLAN": {
      "description": "Conditioning quality plans",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "QCCONTROLPOINT": {
      "description": "Conditioning quality control points",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE"
      ]
    },
    "QCRESULT": {
      "description": "Conditioning quality results",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "VALIDATE",
        "GEN_PDF"
      ]
    },
    "LABELCONTENT": {
      "description": "Conditioning label content",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "DRAFT",
        "VALIDATE",
        "FINALIZE",
        "EXPORT",
        "GEN_PDF"
      ]
    },
    "LABELSOURCE": {
      "description": "Conditioning label source snapshots",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE"
      ]
    },
    "OFFLINEOPERATION": {
      "description": "Conditioning offline operations",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "CREATE",
        "UPDATE",
        "DELETE",
        "SYNC"
      ]
    },
    "ANALYTICS": {
      "description": "Conditioning analytics and reports",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "REPORT",
        "GEN_PDF",
        "EXPORT"
      ]
    },
    "MOBILESYNC": {
      "description": "Conditioning mobile synchronization",
      "module": "CONDITIONING",
      "permissions": [
        "READ",
        "SYNC"
      ]
    },
    "AUDIT": {
      "description": "Conditioning audit logs",
      "module": "CONDITIONING",
      "permissions": [
        "READ"
      ]
    }
  }
}
$$::jsonb);
