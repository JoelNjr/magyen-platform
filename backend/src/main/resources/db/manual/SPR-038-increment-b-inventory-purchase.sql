-- SPR-038 Increment B — compra de inventario e idempotencia Finance
-- Additive only. Does not insert business data. Does not reset or truncate.

CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_inventory_purchase_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'INVENTORY_PURCHASE'
      AND source_id IS NOT NULL;
