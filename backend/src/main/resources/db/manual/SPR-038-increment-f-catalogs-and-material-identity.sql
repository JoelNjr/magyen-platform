-- SPR-038 Increment F — Administration catalogs + inventory material identity
-- Additive. Does not reset, truncate, or insert Magyen business orders.
-- Apply with UTF-8 client encoding so names such as Piqué / Sudáfrica are not stored as '?'.
SET client_encoding = 'UTF8';

-- Commercial product specification: optional second fabric (snapshot name, not inventory).
ALTER TABLE quotation_items
    ADD COLUMN IF NOT EXISTS secondary_fabric varchar(255) NULL;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS secondary_fabric varchar(255) NULL;

-- Administration-owned configurable catalogs (prendas, telas, cuellos, mangas).
CREATE TABLE IF NOT EXISTS administration_catalog_entries (
    id              uuid            NOT NULL,
    catalog_kind    varchar(30)     NOT NULL,
    name            varchar(100)    NOT NULL,
    active          boolean         NOT NULL,
    CONSTRAINT administration_catalog_entries_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_administration_catalog_entries_kind_name
    ON administration_catalog_entries (catalog_kind, lower(name));

CREATE INDEX IF NOT EXISTS idx_administration_catalog_entries_kind
    ON administration_catalog_entries (catalog_kind);

CREATE INDEX IF NOT EXISTS idx_administration_catalog_entries_kind_active
    ON administration_catalog_entries (catalog_kind, active);

-- Initial V1 catalog values. Do not duplicate if already present.
INSERT INTO administration_catalog_entries (id, catalog_kind, name, active)
SELECT gen_random_uuid(), seed.catalog_kind, seed.name, TRUE
FROM (VALUES
    ('GARMENT', 'Camiseta'),
    ('GARMENT', 'Camiseta tipo polo'),
    ('GARMENT', 'Conjunto deportivo'),
    ('GARMENT', 'Conjunto de presentación'),
    ('GARMENT', 'Pantaloneta'),
    ('GARMENT', 'Otro'),
    ('FABRIC', 'Sudáfrica'),
    ('FABRIC', 'Piqué'),
    ('FABRIC', 'Hydrotech'),
    ('COLLAR', 'Redondo'),
    ('COLLAR', 'En V recto'),
    ('COLLAR', 'En V cruzado'),
    ('COLLAR', 'Tejido'),
    ('SLEEVE', 'Manga corta sisa'),
    ('SLEEVE', 'Manga corta rangla'),
    ('SLEEVE', 'Manga larga sisa'),
    ('SLEEVE', 'Manga larga rangla')
) AS seed(catalog_kind, name)
WHERE NOT EXISTS (
    SELECT 1
    FROM administration_catalog_entries existing
    WHERE existing.catalog_kind = seed.catalog_kind
      AND lower(existing.name) = lower(seed.name)
);

-- Material codes identify the material type. Paper rolls share one material code
-- and keep unique RP-### physical identifiers.
ALTER TABLE inventory_items
    DROP CONSTRAINT IF EXISTS inventory_items_material_code_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_inventory_items_material_code_non_paper
    ON inventory_items (material_code)
    WHERE paper_roll_number IS NULL;

-- Consecutive business-facing material codes (MAT-001, MAT-002, ...).
CREATE SEQUENCE IF NOT EXISTS material_code_seq START WITH 1 INCREMENT BY 1;
