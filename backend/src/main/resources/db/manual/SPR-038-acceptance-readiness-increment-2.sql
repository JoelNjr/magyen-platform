-- SPR-038 / V1 ACCEPTANCE READINESS — INCREMENT 2
-- Additive only. Does not insert business data. Does not reset or truncate.

CREATE TABLE IF NOT EXISTS production_operators (
    id      uuid            NOT NULL,
    name    varchar(255)    NOT NULL,
    active  boolean         NOT NULL,
    CONSTRAINT production_operators_pkey PRIMARY KEY (id),
    CONSTRAINT production_operators_name_key UNIQUE (name)
);

ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS actual_start_date date NULL;

ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS actual_completion_date date NULL;

ALTER TABLE plotter_jobs
    ADD COLUMN IF NOT EXISTS order_id uuid NULL;

CREATE INDEX IF NOT EXISTS idx_plotter_jobs_order_id
    ON plotter_jobs (order_id);
