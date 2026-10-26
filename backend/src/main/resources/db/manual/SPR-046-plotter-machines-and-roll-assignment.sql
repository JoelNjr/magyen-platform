-- SPR-046 — dos máquinas de Plotter y asignación permanente de rollo.
-- Additive e idempotente. No borra filas, no recrea movimientos ni asientos.
-- No asigna máquina a tela, tinta ni ningún ítem sin paper_roll_number.
--
-- Inspección local previa (magyen_prod_clone, solo lectura):
--   15 ítems PAPER, todos con paper_roll_number (RP-001..RP-015).
--   16 FABRIC y 2 INK, sin número de rollo.
--   Movimientos de esos rollos: 15 IN PURCHASE y 152 OUT PLOTTER. Ningún OUT de otro origen.
--   152 plotter_jobs (47 INTERNAL_MAGYEN, 71 EXTERNAL, 34 WASTE), todos apuntan a un rollo PAPER.
--   Confirmación de negocio: esos trabajos se hicieron en Plotter 1.
-- Por eso el backfill cubre los 152 trabajos y los 15 rollos. No quedan registros ambiguos
-- en esa inspección. Un rollo sin OUT PLOTTER, o con un OUT de otro origen, no se toca.
--
-- Aplicar manualmente sobre la base existente antes de desplegar código con ddl-auto=validate.
-- No ejecutar este script contra producción desde el entorno de desarrollo sin revisión.

CREATE TABLE IF NOT EXISTS plotter_machines (
    id                          uuid            NOT NULL,
    name                        varchar(100)    NOT NULL,
    active                      boolean         NOT NULL,
    default_for_registration    boolean         NOT NULL,
    CONSTRAINT plotter_machines_pkey PRIMARY KEY (id),
    CONSTRAINT uq_plotter_machines_name UNIQUE (name)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_plotter_machines_single_default
    ON plotter_machines (default_for_registration)
    WHERE default_for_registration = true;

INSERT INTO plotter_machines (id, name, active, default_for_registration)
VALUES
    ('6f0c1a10-0001-4a01-8a01-000000000001', 'Plotter 1', true, true),
    ('6f0c1a10-0002-4a02-8a02-000000000002', 'Plotter 2', true, false)
ON CONFLICT (id) DO NOTHING;

ALTER TABLE plotter_jobs
    ADD COLUMN IF NOT EXISTS machine_id uuid;

ALTER TABLE inventory_items
    ADD COLUMN IF NOT EXISTS plotter_machine_id uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'inventory_items_plotter_machine_paper_only'
    ) THEN
        ALTER TABLE inventory_items
            ADD CONSTRAINT inventory_items_plotter_machine_paper_only
            CHECK (plotter_machine_id IS NULL OR paper_roll_number IS NOT NULL);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_plotter_jobs_machine_id
    ON plotter_jobs (machine_id);

CREATE INDEX IF NOT EXISTS idx_inventory_items_plotter_machine_id
    ON inventory_items (plotter_machine_id);

-- Trabajos ya registrados: solo los que referencian un rollo de papel real.
UPDATE plotter_jobs AS job
SET machine_id = '6f0c1a10-0001-4a01-8a01-000000000001'
WHERE job.machine_id IS NULL
  AND EXISTS (
      SELECT 1
      FROM inventory_items AS item
      WHERE item.id = job.paper_inventory_item_id
        AND item.material_type = 'PAPER'
        AND item.paper_roll_number IS NOT NULL
  );

-- Rollos ya consumidos solo por Plotter. Conserva stock, costo y movimientos.
UPDATE inventory_items AS item
SET plotter_machine_id = '6f0c1a10-0001-4a01-8a01-000000000001'
WHERE item.plotter_machine_id IS NULL
  AND item.material_type = 'PAPER'
  AND item.paper_roll_number IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM inventory_movements AS movement
      WHERE movement.inventory_item_id = item.id
        AND movement.movement_type = 'OUT'
        AND movement.source_type = 'PLOTTER'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM inventory_movements AS movement
      WHERE movement.inventory_item_id = item.id
        AND movement.movement_type = 'OUT'
        AND COALESCE(movement.source_type, '') <> 'PLOTTER'
  );
