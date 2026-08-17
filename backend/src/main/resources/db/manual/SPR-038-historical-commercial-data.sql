-- =============================================================================
-- SPR-038 / V1 Acceptance Fix 1 — Historical Commercial Data
-- Additive migration for the clean V1 Docker database.
-- =============================================================================
-- SAFETY:
--   - Does NOT reset, truncate, or delete business rows.
--   - quotations and orders are expected to be empty after the V1 reset.
--   - Replaces free-text salesperson with seller_id (stable Commercial seller).
--   - Does NOT create David or any other seller row.
-- =============================================================================

CREATE TABLE IF NOT EXISTS sellers (
    id      uuid            NOT NULL,
    name    varchar(255)    NOT NULL,
    active  boolean         NOT NULL,
    CONSTRAINT sellers_pkey PRIMARY KEY (id),
    CONSTRAINT sellers_name_key UNIQUE (name)
);

ALTER TABLE quotations
    ADD COLUMN IF NOT EXISTS seller_id uuid;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS seller_id uuid;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'quotations'
          AND column_name = 'salesperson'
    ) THEN
        IF EXISTS (SELECT 1 FROM quotations) THEN
            RAISE EXCEPTION 'Cannot drop quotations.salesperson while rows exist';
        END IF;
        ALTER TABLE quotations DROP COLUMN salesperson;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'orders'
          AND column_name = 'salesperson'
    ) THEN
        IF EXISTS (SELECT 1 FROM orders) THEN
            RAISE EXCEPTION 'Cannot drop orders.salesperson while rows exist';
        END IF;
        ALTER TABLE orders DROP COLUMN salesperson;
    END IF;
END
$$;

ALTER TABLE quotations
    ALTER COLUMN seller_id SET NOT NULL;

ALTER TABLE orders
    ALTER COLUMN seller_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_quotations_seller_id
    ON quotations (seller_id);

CREATE INDEX IF NOT EXISTS idx_orders_seller_id
    ON orders (seller_id);
