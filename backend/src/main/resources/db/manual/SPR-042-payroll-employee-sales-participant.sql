-- SPR-042 — participación comercial explícita del empleado de nómina.
-- Additive. Default false. Does not rewrite orders or payments.
-- A participant is an employee id already stored as orders.seller_id.
-- Names are not used. Safe to run again: the column is added once and the
-- update only turns the flag on for employees who already sell.

ALTER TABLE payroll_employees
    ADD COLUMN IF NOT EXISTS sales_participant boolean NOT NULL DEFAULT false;

UPDATE payroll_employees employee
SET sales_participant = true
WHERE EXISTS (
    SELECT 1
    FROM orders
    WHERE orders.seller_id = employee.id
);
