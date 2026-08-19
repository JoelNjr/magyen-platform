-- SPR-039 Increment I — idempotencia del servicio Plotter interno
-- Additive only. Does not insert business data. Does not reset or truncate.

CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_plotter_internal_expense_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'PLOTTER_INTERNAL_EXPENSE'
      AND source_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_plotter_internal_income_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'PLOTTER_INTERNAL_INCOME'
      AND source_id IS NOT NULL;
