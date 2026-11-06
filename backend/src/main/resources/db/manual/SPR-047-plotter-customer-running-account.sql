-- SPR-047 Cuenta corriente de clientes externos de Plotter.
--
-- Aditiva. No borra trabajos, pagos ni ingresos de Finanzas.
-- No ejecutar desde la aplicación: ddl-auto permanece en validate.
--
-- El valor comercial del trabajo sigue siendo plotter_jobs.total_amount.
-- En trabajos externos ese monto puede diferir de printed_meters × price_per_meter.
-- Los registros históricos ya tienen total_amount = metros × precio, así que el
-- débito de apertura de cada trabajo es el valor que ya estaba guardado.
-- No se insertan cargos manuales ni se duplican financial_transactions.
--
-- Los pagos históricos siguen en plotter_payments, con su fecha y su monto.
-- Esta migración les copia el cliente del trabajo para que reduzcan la cuenta
-- una sola vez. El ingreso PLOTTER ya existente sigue apuntando al id del pago.

ALTER TABLE plotter_payments
    ADD COLUMN IF NOT EXISTS customer_id uuid;

ALTER TABLE plotter_payments
    ADD COLUMN IF NOT EXISTS registration_key uuid;

ALTER TABLE plotter_payments
    ALTER COLUMN plotter_job_id DROP NOT NULL;

UPDATE plotter_payments AS payment
SET customer_id = job.customer_id
FROM plotter_jobs AS job
WHERE payment.customer_id IS NULL
  AND payment.plotter_job_id = job.id
  AND job.customer_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_plotter_payments_customer_id
    ON plotter_payments (customer_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_plotter_payments_registration_key
    ON plotter_payments (registration_key)
    WHERE registration_key IS NOT NULL;

-- Comentario de negocio sobre el valor comercial externo.
COMMENT ON COLUMN plotter_jobs.total_amount IS
    'Valor del trabajo. En EXTERNAL es el debito de la cuenta y puede diferir de metros x precio. En INTERNAL es metros x precio del servicio.';
