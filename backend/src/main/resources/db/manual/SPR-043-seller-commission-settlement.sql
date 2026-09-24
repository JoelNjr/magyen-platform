-- SPR-043 — liquidación pagada de comisión de vendedor.
-- Additive. Does not insert settlements or financial transactions.
-- Does not modify orders, payments, or existing finance rows.

CREATE TABLE IF NOT EXISTS seller_commission_settlements (
    id                          uuid            NOT NULL,
    employee_id                 uuid            NOT NULL,
    period_start                date            NOT NULL,
    period_end                  date            NOT NULL,
    sales_snapshot              numeric(19, 2)  NOT NULL,
    order_count_snapshot        integer         NOT NULL,
    commission_snapshot         numeric(19, 2)  NOT NULL,
    status                      varchar(30)     NOT NULL,
    actual_payment_date         date            NOT NULL,
    paid_at                     timestamp       NOT NULL,
    financial_transaction_id    uuid            NOT NULL,
    observation                 varchar(2000)   NULL,
    CONSTRAINT seller_commission_settlements_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_seller_commission_settlements_employee_period
    ON seller_commission_settlements (employee_id, period_start);

CREATE INDEX IF NOT EXISTS idx_seller_commission_settlements_employee_id
    ON seller_commission_settlements (employee_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_seller_commission_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'SELLER_COMMISSION'
      AND source_id IS NOT NULL;
