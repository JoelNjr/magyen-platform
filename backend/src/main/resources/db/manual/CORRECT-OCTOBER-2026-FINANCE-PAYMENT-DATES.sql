-- Corrección de fechas de pago de obligaciones de octubre 2026 registradas en noviembre 2026.
--
-- NO EJECUTAR este archivo hasta revisarlo. Termina en ROLLBACK.
-- No borra obligaciones, ocurrencias ni movimientos. No modifica montos,
-- categorías, descripciones, estado PAID ni referencias source_type/source_id.
--
-- Contexto
-- --------
-- "Marcar como pagado" persistía LocalDateTime.now() en:
--   recurring_financial_obligation_occurrences.paid_date
--   financial_transactions.transaction_date  (vía paidAt.toLocalDate())
-- El cierre mensual clasifica el gasto por financial_transactions.transaction_date.
-- La ocurrencia conserva su período en due_date. No hay tabla de historial de pagos
-- ni asiento contable adicional para este flujo.
--
-- Vínculo positivo (las dos direcciones deben coincidir):
--   occurrence.financial_transaction_id = financial_transactions.id
--   financial_transactions.source_type  = 'RECURRING_OBLIGATION'
--   financial_transactions.source_id    = occurrence.id
-- Índice único: uq_financial_transactions_recurring_obligation_source.
--
-- Fecha propuesta
-- ---------------
-- No quedó guardada una fecha real distinta del clic. La única fecha de octubre
-- ya persistida en la obligación es due_date. Este script propone esa fecha para
-- transaction_date y para el día de paid_date (00:00:00), que es suficiente para
-- devolver el gasto al mes del período sin recrear el movimiento.
-- Si la fecha real fue otra dentro de octubre (por ejemplo 2026-10-31), ajuste
-- proposed_payment_date en la tabla temporal ANTES del bloque de actualización
-- y deje esa fecha dentro de octubre 2026. El bloque rechaza fechas fuera de octubre.
--
-- Este script no conoce los UUID de antemano. Una lectura del 2026-11-06 sobre
-- las bases locales magyen_platform, magyen_prod_clone y magyen_platform_restore_test
-- no encontró ocurrencias con due_date en octubre 2026 ni movimientos en noviembre 2026.
-- En esas bases el objetivo es 0 filas y 0.00. Los SELECT de abajo vuelven a calcular
-- el objetivo en la base donde se ejecuten. El UPDATE solo toca los id de la tabla
-- temporal construida con el vínculo de arriba.

-- =============================================================================
-- 1. Ocurrencias de octubre 2026 positivamente vinculadas a un gasto de noviembre
-- =============================================================================

SELECT
    occurrence.id                       AS occurrence_id,
    occurrence.recurring_obligation_id  AS obligation_id,
    obligation.name                     AS obligation_name,
    obligation.obligation_type          AS obligation_type,
    occurrence.due_date                 AS occurrence_period_due_date,
    to_char(occurrence.due_date, 'YYYY-MM') AS occurrence_period,
    occurrence.status                   AS occurrence_status,
    occurrence.paid_date                AS current_paid_date,
    occurrence.financial_transaction_id AS financial_transaction_id,
    movement.transaction_date           AS current_transaction_date,
    movement.transaction_type,
    movement.amount,
    movement.category,
    movement.description,
    movement.source_type,
    movement.source_id,
    occurrence.due_date                 AS proposed_payment_date
FROM recurring_financial_obligation_occurrences AS occurrence
JOIN recurring_financial_obligations AS obligation
    ON obligation.id = occurrence.recurring_obligation_id
JOIN financial_transactions AS movement
    ON movement.id = occurrence.financial_transaction_id
WHERE occurrence.status = 'PAID'
  AND occurrence.due_date >= DATE '2026-10-01'
  AND occurrence.due_date < DATE '2026-11-01'
  AND occurrence.paid_date >= TIMESTAMP '2026-11-01'
  AND occurrence.paid_date < TIMESTAMP '2026-12-01'
  AND occurrence.financial_transaction_id IS NOT NULL
  AND movement.source_type = 'RECURRING_OBLIGATION'
  AND movement.source_id = occurrence.id
  AND movement.transaction_type = 'EXPENSE'
  AND movement.transaction_date >= DATE '2026-11-01'
  AND movement.transaction_date < DATE '2026-12-01'
  AND movement.amount = occurrence.expected_amount
ORDER BY obligation.name, occurrence.due_date, occurrence.id;

-- =============================================================================
-- 2. Totales del conjunto positivo. Deben coincidir con el UPDATE de más abajo.
-- =============================================================================

SELECT
    COUNT(*)                         AS affected_records,
    COALESCE(SUM(movement.amount), 0) AS affected_amount
FROM recurring_financial_obligation_occurrences AS occurrence
JOIN financial_transactions AS movement
    ON movement.id = occurrence.financial_transaction_id
WHERE occurrence.status = 'PAID'
  AND occurrence.due_date >= DATE '2026-10-01'
  AND occurrence.due_date < DATE '2026-11-01'
  AND occurrence.paid_date >= TIMESTAMP '2026-11-01'
  AND occurrence.paid_date < TIMESTAMP '2026-12-01'
  AND occurrence.financial_transaction_id IS NOT NULL
  AND movement.source_type = 'RECURRING_OBLIGATION'
  AND movement.source_id = occurrence.id
  AND movement.transaction_type = 'EXPENSE'
  AND movement.transaction_date >= DATE '2026-11-01'
  AND movement.transaction_date < DATE '2026-12-01'
  AND movement.amount = occurrence.expected_amount;

-- =============================================================================
-- 3. Ambiguos: parecen relacionados, pero falta un vínculo o un dato no cuadra.
--    Quedan fuera del UPDATE. Revíselos y no los fuerce dentro del objetivo.
-- =============================================================================

-- 3a. Gasto RECURRING_OBLIGATION de noviembre 2026 que no cumple el vínculo positivo
--     con una ocurrencia PAID de octubre del mismo monto.
SELECT
    movement.id                AS financial_transaction_id,
    movement.transaction_date,
    movement.amount,
    movement.category,
    movement.description,
    movement.source_type,
    movement.source_id,
    occurrence.id              AS occurrence_id,
    occurrence.due_date,
    occurrence.status          AS occurrence_status,
    occurrence.paid_date,
    occurrence.financial_transaction_id,
    occurrence.expected_amount,
    'November RECURRING_OBLIGATION expense is not a positive October link' AS ambiguity_reason
FROM financial_transactions AS movement
LEFT JOIN recurring_financial_obligation_occurrences AS occurrence
    ON occurrence.id = movement.source_id
WHERE movement.source_type = 'RECURRING_OBLIGATION'
  AND movement.transaction_type = 'EXPENSE'
  AND movement.transaction_date >= DATE '2026-11-01'
  AND movement.transaction_date < DATE '2026-12-01'
  AND NOT (
        occurrence.id IS NOT NULL
        AND occurrence.status = 'PAID'
        AND occurrence.due_date >= DATE '2026-10-01'
        AND occurrence.due_date < DATE '2026-11-01'
        AND occurrence.paid_date >= TIMESTAMP '2026-11-01'
        AND occurrence.paid_date < TIMESTAMP '2026-12-01'
        AND occurrence.financial_transaction_id = movement.id
        AND movement.source_id = occurrence.id
        AND movement.amount = occurrence.expected_amount
      )
ORDER BY movement.transaction_date, movement.id;

-- 3b. Ocurrencia PAID con vencimiento en octubre 2026 y paid_date en noviembre,
--     cuyo movimiento no está fechado en noviembre o no cierra el vínculo.
SELECT
    occurrence.id              AS occurrence_id,
    occurrence.due_date,
    occurrence.status,
    occurrence.paid_date,
    occurrence.expected_amount,
    occurrence.financial_transaction_id,
    movement.id                AS financial_transaction_id,
    movement.transaction_date,
    movement.amount,
    movement.source_type,
    movement.source_id,
    'October occurrence paid in November does not match its November expense' AS ambiguity_reason
FROM recurring_financial_obligation_occurrences AS occurrence
LEFT JOIN financial_transactions AS movement
    ON movement.id = occurrence.financial_transaction_id
WHERE occurrence.status = 'PAID'
  AND occurrence.due_date >= DATE '2026-10-01'
  AND occurrence.due_date < DATE '2026-11-01'
  AND occurrence.paid_date >= TIMESTAMP '2026-11-01'
  AND occurrence.paid_date < TIMESTAMP '2026-12-01'
  AND NOT (
        movement.id IS NOT NULL
        AND movement.source_type = 'RECURRING_OBLIGATION'
        AND movement.source_id = occurrence.id
        AND movement.transaction_type = 'EXPENSE'
        AND movement.transaction_date >= DATE '2026-11-01'
        AND movement.transaction_date < DATE '2026-12-01'
        AND movement.amount = occurrence.expected_amount
      )
ORDER BY occurrence.due_date, occurrence.id;

-- 3c. Informativo, fuera de alcance: gastos de noviembre que no son obligaciones recurrentes.
--     No se actualizan. Sirve para confirmar que no se trató todo noviembre como octubre.
SELECT
    COUNT(*)                          AS november_expenses_not_recurring_obligations,
    COALESCE(SUM(movement.amount), 0) AS amount_left_unchanged
FROM financial_transactions AS movement
WHERE movement.transaction_type = 'EXPENSE'
  AND movement.transaction_date >= DATE '2026-11-01'
  AND movement.transaction_date < DATE '2026-12-01'
  AND COALESCE(movement.source_type, '') <> 'RECURRING_OBLIGATION';

-- =============================================================================
-- 4. Corrección. Revisar los SELECT anteriores. Confirmar que affected_records
--    es el número que espera y que las secciones 3a y 3b no contienen filas
--    que debieran corregirse con otra regla. Luego ejecute este bloque.
--    El archivo termina en ROLLBACK. Cambie esa línea por COMMIT solo después
--    de leer el SELECT final y el NOTICE con el mismo conteo y la misma suma.
-- =============================================================================

BEGIN;

DROP TABLE IF EXISTS pg_temp.october_2026_payment_date_targets;

CREATE TEMP TABLE october_2026_payment_date_targets ON COMMIT DROP AS
SELECT
    occurrence.id                       AS occurrence_id,
    occurrence.due_date                 AS due_date,
    occurrence.paid_date                AS current_paid_date,
    occurrence.expected_amount          AS amount,
    occurrence.financial_transaction_id AS financial_transaction_id,
    movement.transaction_date           AS current_transaction_date,
    occurrence.due_date                 AS proposed_payment_date
FROM recurring_financial_obligation_occurrences AS occurrence
JOIN financial_transactions AS movement
    ON movement.id = occurrence.financial_transaction_id
WHERE occurrence.status = 'PAID'
  AND occurrence.due_date >= DATE '2026-10-01'
  AND occurrence.due_date < DATE '2026-11-01'
  AND occurrence.paid_date >= TIMESTAMP '2026-11-01'
  AND occurrence.paid_date < TIMESTAMP '2026-12-01'
  AND occurrence.financial_transaction_id IS NOT NULL
  AND movement.source_type = 'RECURRING_OBLIGATION'
  AND movement.source_id = occurrence.id
  AND movement.transaction_type = 'EXPENSE'
  AND movement.transaction_date >= DATE '2026-11-01'
  AND movement.transaction_date < DATE '2026-12-01'
  AND movement.amount = occurrence.expected_amount;

-- Ajuste opcional, solo tras revisar los id. La fecha debe seguir en octubre 2026.
-- UPDATE october_2026_payment_date_targets
-- SET proposed_payment_date = DATE '2026-10-31'
-- WHERE occurrence_id IN (
--     -- pegue aquí únicamente UUID revisados
-- );

SELECT
    COUNT(*)                  AS target_records_inside_transaction,
    COALESCE(SUM(amount), 0)  AS target_amount_inside_transaction
FROM october_2026_payment_date_targets;

DO $correct$
DECLARE
    target_count integer;
    target_amount numeric(19, 2);
    obligations_before integer;
    obligations_after integer;
    occurrences_before integer;
    occurrences_after integer;
    transactions_before integer;
    transactions_after integer;
    updated_transactions integer;
    updated_occurrences integer;
    still_november integer;
    paid_date_mismatch integer;
    identity_mismatch integer;
BEGIN
    SELECT COUNT(*), COALESCE(SUM(amount), 0)
      INTO target_count, target_amount
      FROM october_2026_payment_date_targets;

    IF EXISTS (
        SELECT 1
        FROM october_2026_payment_date_targets
        WHERE proposed_payment_date < DATE '2026-10-01'
           OR proposed_payment_date >= DATE '2026-11-01'
    ) THEN
        RAISE EXCEPTION
            'A proposed payment date is outside October 2026. No correction will remain applied.';
    END IF;

    SELECT COUNT(*) INTO obligations_before FROM recurring_financial_obligations;
    SELECT COUNT(*) INTO occurrences_before FROM recurring_financial_obligation_occurrences;
    SELECT COUNT(*) INTO transactions_before FROM financial_transactions;

    WITH updated AS (
        UPDATE financial_transactions AS movement
        SET transaction_date = target.proposed_payment_date
        FROM october_2026_payment_date_targets AS target
        WHERE movement.id = target.financial_transaction_id
          AND movement.source_type = 'RECURRING_OBLIGATION'
          AND movement.source_id = target.occurrence_id
          AND movement.transaction_type = 'EXPENSE'
          AND movement.amount = target.amount
          AND movement.transaction_date = target.current_transaction_date
        RETURNING movement.id
    )
    SELECT COUNT(*) INTO updated_transactions FROM updated;

    WITH updated AS (
        UPDATE recurring_financial_obligation_occurrences AS occurrence
        SET paid_date = target.proposed_payment_date::timestamp
        FROM october_2026_payment_date_targets AS target
        WHERE occurrence.id = target.occurrence_id
          AND occurrence.status = 'PAID'
          AND occurrence.due_date = target.due_date
          AND occurrence.expected_amount = target.amount
          AND occurrence.financial_transaction_id = target.financial_transaction_id
          AND occurrence.paid_date = target.current_paid_date
        RETURNING occurrence.id
    )
    SELECT COUNT(*) INTO updated_occurrences FROM updated;

    IF updated_transactions IS DISTINCT FROM target_count
       OR updated_occurrences IS DISTINCT FROM target_count THEN
        RAISE EXCEPTION
            'Row count mismatch. expected=%, transaction_dates_updated=%, paid_dates_updated=%.',
            target_count, updated_transactions, updated_occurrences;
    END IF;

    SELECT COUNT(*) INTO still_november
    FROM october_2026_payment_date_targets AS target
    JOIN financial_transactions AS movement
        ON movement.id = target.financial_transaction_id
    WHERE movement.transaction_date <> target.proposed_payment_date
       OR movement.transaction_date < DATE '2026-10-01'
       OR movement.transaction_date >= DATE '2026-11-01';

    SELECT COUNT(*) INTO paid_date_mismatch
    FROM october_2026_payment_date_targets AS target
    JOIN recurring_financial_obligation_occurrences AS occurrence
        ON occurrence.id = target.occurrence_id
    WHERE occurrence.status <> 'PAID'
       OR occurrence.due_date <> target.due_date
       OR occurrence.expected_amount <> target.amount
       OR occurrence.financial_transaction_id <> target.financial_transaction_id
       OR occurrence.paid_date <> target.proposed_payment_date::timestamp;

    SELECT COUNT(*) INTO identity_mismatch
    FROM october_2026_payment_date_targets AS target
    JOIN financial_transactions AS movement
        ON movement.id = target.financial_transaction_id
    WHERE movement.source_type <> 'RECURRING_OBLIGATION'
       OR movement.source_id <> target.occurrence_id
       OR movement.amount <> target.amount
       OR movement.transaction_type <> 'EXPENSE';

    IF still_november <> 0 OR paid_date_mismatch <> 0 OR identity_mismatch <> 0 THEN
        RAISE EXCEPTION
            'Verification failed. still_november=%, paid_date_or_identity=%, source_or_amount=%.',
            still_november, paid_date_mismatch, identity_mismatch;
    END IF;

    SELECT COUNT(*) INTO obligations_after FROM recurring_financial_obligations;
    SELECT COUNT(*) INTO occurrences_after FROM recurring_financial_obligation_occurrences;
    SELECT COUNT(*) INTO transactions_after FROM financial_transactions;

    IF obligations_before <> obligations_after
       OR occurrences_before <> occurrences_after
       OR transactions_before <> transactions_after THEN
        RAISE EXCEPTION
            'A table gained or lost rows. obligations % -> %, occurrences % -> %, transactions % -> %.',
            obligations_before, obligations_after,
            occurrences_before, occurrences_after,
            transactions_before, transactions_after;
    END IF;

    RAISE NOTICE
        'Verification passed for % records, total amount %. Review the SELECT below. This script still ROLLBACK.',
        target_count, target_amount;
END
$correct$;

-- Debe mostrar transaction_date y paid_date en la fecha propuesta, estado PAID
-- y el mismo source_id. affected_records de la sección 2 debe ser igual a este conteo.
SELECT
    target.occurrence_id,
    target.financial_transaction_id,
    occurrence.due_date,
    occurrence.status,
    occurrence.paid_date,
    movement.transaction_date,
    movement.amount,
    movement.category,
    movement.description,
    movement.source_type,
    movement.source_id
FROM october_2026_payment_date_targets AS target
JOIN recurring_financial_obligation_occurrences AS occurrence
    ON occurrence.id = target.occurrence_id
JOIN financial_transactions AS movement
    ON movement.id = target.financial_transaction_id
ORDER BY occurrence.due_date, target.occurrence_id;

-- Antes de cambiar esto por COMMIT, confirme:
--   * target_records_inside_transaction = filas del SELECT anterior
--   * target_amount_inside_transaction = suma de amount de ese SELECT
--   * las secciones 3a y 3b no quedaron dentro de este resultado
--   * ninguna fila sigue con transaction_date o paid_date en noviembre 2026
--   * el NOTICE no reportó excepción
ROLLBACK;
