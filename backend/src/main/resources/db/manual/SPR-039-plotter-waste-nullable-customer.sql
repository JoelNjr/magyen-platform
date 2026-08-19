-- SPR-039 — WASTE plotter jobs do not require a customer.
-- Additive only. Does not insert, update, or delete business rows.
-- Existing EXTERNAL / INTERNAL_MAGYEN rows keep their customer_id.

ALTER TABLE plotter_jobs
    ALTER COLUMN customer_id DROP NOT NULL;
