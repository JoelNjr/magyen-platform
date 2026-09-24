-- SPR-041 — fecha real de entrega comercial (Order.actualDeliveryDate)
-- Additive only. Does not backfill. Does not rewrite historical rows.
-- Existing DELIVERED orders remain actual_delivery_date = NULL.

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS actual_delivery_date date NULL;
