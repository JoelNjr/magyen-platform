-- SPR-040 — trazabilidad comercial QuotationItem → OrderItem
-- Additive only. Does not backfill. Does not rewrite historical rows.
-- Existing order_items remain quotation_item_id = NULL.

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS quotation_item_id uuid NULL;

-- Soft UUID: no FK to quotation_items (same pattern as orders.quotation_id).
-- Partial unique index allows many historical NULLs.
CREATE UNIQUE INDEX IF NOT EXISTS uq_order_items_quotation_item_id
    ON order_items (quotation_item_id)
    WHERE quotation_item_id IS NOT NULL;
