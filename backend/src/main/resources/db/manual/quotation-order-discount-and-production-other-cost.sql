-- Additive only. Does not insert, update or delete business data.
-- Do not run against production from the application.

-- Discount on the commercial quotation/order total (existing rows become 0).
ALTER TABLE quotations
    ADD COLUMN IF NOT EXISTS discount_amount numeric(19, 2) NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS discount_amount numeric(19, 2) NOT NULL DEFAULT 0;

-- Direct cost category OTHER (envíos, empaques, accesorios, etc.).
CREATE TABLE IF NOT EXISTS production_additional_costs (
    id                          uuid            NOT NULL,
    production_order_id         uuid            NOT NULL,
    category                    varchar(30)     NOT NULL,
    description                 varchar(2000)   NOT NULL,
    amount                      numeric(19, 2)  NOT NULL,
    incurred_date               date            NOT NULL,
    financial_transaction_id    uuid            NULL,
    CONSTRAINT production_additional_costs_pkey PRIMARY KEY (id),
    CONSTRAINT fk_production_additional_costs_production_order
        FOREIGN KEY (production_order_id)
        REFERENCES production_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_production_additional_costs_production_order_id
    ON production_additional_costs (production_order_id);

-- One PRODUCTION ledger row per additional cost (prevents double accounting).
CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_production_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'PRODUCTION'
      AND source_id IS NOT NULL;
