-- Categoría única de cliente: MAGYEN, PLOTTER o UNCLASSIFIED.
-- Additive e idempotente. No cambia ids, cotizaciones, órdenes, trabajos de Plotter,
-- pagos, transacciones financieras ni secuencias.
-- INTERNAL_MAGYEN no clasifica al cliente como PLOTTER.
-- Si un cliente tiene cotización y trabajo EXTERNAL, permanece UNCLASSIFIED.
-- No fusiona ni elimina clientes.

ALTER TABLE customers
    ADD COLUMN IF NOT EXISTS category varchar(30);

ALTER TABLE customers
    ALTER COLUMN category SET DEFAULT 'UNCLASSIFIED';

DO $$
DECLARE
    conflict_count integer;
    conflict_names text;
BEGIN
    SELECT COUNT(*), string_agg(c.name, ', ' ORDER BY c.name)
    INTO conflict_count, conflict_names
    FROM customers c
    WHERE EXISTS (
            SELECT 1 FROM quotations q WHERE q.customer_id = c.id
        )
      AND EXISTS (
            SELECT 1
            FROM plotter_jobs j
            WHERE j.customer_id = c.id
              AND j.job_type = 'EXTERNAL'
        );

    IF conflict_count > 0 THEN
        RAISE NOTICE 'Customer category conflicts left UNCLASSIFIED (%): %',
            conflict_count, conflict_names;
    ELSE
        RAISE NOTICE 'Customer category conflicts: 0';
    END IF;
END $$;

UPDATE customers c
SET category = 'UNCLASSIFIED'
WHERE c.category IS NULL
  AND EXISTS (
        SELECT 1 FROM quotations q WHERE q.customer_id = c.id
    )
  AND EXISTS (
        SELECT 1
        FROM plotter_jobs j
        WHERE j.customer_id = c.id
          AND j.job_type = 'EXTERNAL'
    );

UPDATE customers c
SET category = 'MAGYEN'
WHERE c.category IS NULL
  AND EXISTS (
        SELECT 1 FROM quotations q WHERE q.customer_id = c.id
    )
  AND NOT EXISTS (
        SELECT 1
        FROM plotter_jobs j
        WHERE j.customer_id = c.id
          AND j.job_type = 'EXTERNAL'
    );

UPDATE customers c
SET category = 'PLOTTER'
WHERE c.category IS NULL
  AND NOT EXISTS (
        SELECT 1 FROM quotations q WHERE q.customer_id = c.id
    )
  AND EXISTS (
        SELECT 1
        FROM plotter_jobs j
        WHERE j.customer_id = c.id
          AND j.job_type = 'EXTERNAL'
    );

UPDATE customers
SET category = 'UNCLASSIFIED'
WHERE category IS NULL;

ALTER TABLE customers
    ALTER COLUMN category SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'customers_category_check'
    ) THEN
        ALTER TABLE customers
            ADD CONSTRAINT customers_category_check
            CHECK (category IN ('MAGYEN', 'PLOTTER', 'UNCLASSIFIED'));
    END IF;
END $$;
