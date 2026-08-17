-- SPR-038 Increment D — tipo de trabajo Plotter interno vs externo
-- Additive only. Does not insert business data. Does not reset or truncate.
-- Existing rows are classified EXTERNAL so their payment semantics stay unchanged.

ALTER TABLE plotter_jobs
    ADD COLUMN IF NOT EXISTS job_type varchar(30);

UPDATE plotter_jobs
   SET job_type = 'EXTERNAL'
 WHERE job_type IS NULL;

ALTER TABLE plotter_jobs
    ALTER COLUMN job_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_plotter_jobs_job_type
    ON plotter_jobs (job_type);
