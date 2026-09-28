-- Asociación opcional de un movimiento financiero a una Orden comercial.
-- Additive e idempotente. No modifica movimientos históricos: order_id queda NULL.
-- No crea una segunda transacción ni cambia source_type/source_id.
-- order_id es referencia UUID blanda (sin FK), igual que source_id.
-- Aplicar manualmente sobre la base existente antes de desplegar el código
-- que valida el esquema (spring.jpa.hibernate.ddl-auto=validate).

ALTER TABLE financial_transactions
    ADD COLUMN IF NOT EXISTS order_id uuid;

CREATE INDEX IF NOT EXISTS idx_financial_transactions_order_id
    ON financial_transactions (order_id);
