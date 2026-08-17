-- SPR-038 Increment E — payroll deductions foundation
-- Additive only. Does not insert business data. Does not reset, truncate, or remap sellers.

CREATE TABLE IF NOT EXISTS payroll_deductions (
    id              uuid            NOT NULL,
    employee_id     uuid            NOT NULL,
    type            varchar(30)     NOT NULL,
    amount          numeric(19, 2)  NOT NULL,
    deduction_date  date            NOT NULL,
    description     varchar(2000)   NULL,
    status          varchar(30)     NOT NULL,
    created_at      timestamp       NOT NULL,
    CONSTRAINT payroll_deductions_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_payroll_deductions_employee_id
    ON payroll_deductions (employee_id);

CREATE INDEX IF NOT EXISTS idx_payroll_deductions_status
    ON payroll_deductions (status);

CREATE INDEX IF NOT EXISTS idx_payroll_deductions_employee_status
    ON payroll_deductions (employee_id, status);
