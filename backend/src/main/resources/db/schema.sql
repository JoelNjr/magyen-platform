-- Magyen Platform — Commercial module schema
-- Aggregates: Customer / Quotation / QuotationItem
-- Compatible with PostgreSQL 17
-- Hibernate ddl-auto remains validate; this SQL owns schema creation.

CREATE TABLE customers (
    id      uuid            NOT NULL,
    name    varchar(255)    NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

-- Sequence for commercial quotation numbers (concurrency-safe source of next values).
-- Application generation wiring is deferred; column remains nullable until historical backfill.
CREATE SEQUENCE quotation_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE quotations (
    id                  uuid            NOT NULL,
    quotation_number    bigint          NULL,
    customer_id         uuid            NOT NULL,
    creation_date       date            NOT NULL,
    delivery_date       date            NOT NULL,
    status              varchar(20)     NOT NULL,
    salesperson         varchar(255)    NOT NULL,
    observations        varchar(2000)   NULL,
    total_amount        numeric(19, 2)  NOT NULL,
    CONSTRAINT quotations_pkey PRIMARY KEY (id),
    CONSTRAINT quotations_quotation_number_key UNIQUE (quotation_number)
);

CREATE TABLE quotation_items (
    id                      uuid            NOT NULL,
    quotation_id            uuid            NOT NULL,
    product_name            varchar(255)    NOT NULL,
    quantity                integer         NOT NULL,
    fabric                  varchar(255)    NOT NULL,
    color                   varchar(100)    NOT NULL,
    unit_price              numeric(19, 2)  NOT NULL,
    subtotal                numeric(19, 2)  NOT NULL,
    garment_type            varchar(100)    NULL,
    collar_type             varchar(100)    NULL,
    sleeve_type             varchar(100)    NULL,
    garment_variant         varchar(100)    NULL,
    sublimation_required    boolean         NOT NULL DEFAULT FALSE,
    embroidery_required     boolean         NOT NULL DEFAULT FALSE,
    dtf_required            boolean         NOT NULL DEFAULT FALSE,
    decoration_notes        varchar(2000)   NULL,
    includes_names          boolean         NOT NULL DEFAULT FALSE,
    includes_numbers        boolean         NOT NULL DEFAULT FALSE,
    includes_logos          boolean         NOT NULL DEFAULT FALSE,
    personalization_notes   varchar(2000)   NULL,
    item_observations       varchar(2000)   NULL,
    CONSTRAINT quotation_items_pkey PRIMARY KEY (id),
    CONSTRAINT fk_quotation_items_quotation
        FOREIGN KEY (quotation_id)
        REFERENCES quotations (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_quotations_status
    ON quotations (status);

CREATE INDEX idx_quotations_customer_id
    ON quotations (customer_id);

CREATE INDEX idx_quotation_items_quotation_id
    ON quotation_items (quotation_id);

CREATE TABLE orders (
    id                          uuid            NOT NULL,
    order_number                varchar(100)    NOT NULL,
    customer_id                 uuid            NOT NULL,
    quotation_id                uuid            NOT NULL,
    confirmation_date           date            NOT NULL,
    status                      varchar(30)     NOT NULL,
    promised_delivery_date      date            NOT NULL,
    delivery_observations       varchar(2000)   NULL,
    advance_acknowledged        boolean         NOT NULL,
    final_payment_acknowledged  boolean         NOT NULL,
    committed_total             numeric(19, 2)  NOT NULL,
    remaining_balance           numeric(19, 2)  NOT NULL,
    salesperson                 varchar(255)    NOT NULL,
    observations                varchar(2000)   NULL,
    total_amount                numeric(19, 2)  NOT NULL,
    CONSTRAINT orders_pkey PRIMARY KEY (id),
    CONSTRAINT orders_quotation_id_key UNIQUE (quotation_id)
);

CREATE TABLE order_items (
    id                      uuid            NOT NULL,
    order_id                uuid            NOT NULL,
    product_name            varchar(255)    NOT NULL,
    quantity                integer         NOT NULL,
    fabric                  varchar(255)    NOT NULL,
    color                   varchar(100)    NOT NULL,
    unit_price              numeric(19, 2)  NOT NULL,
    subtotal                numeric(19, 2)  NOT NULL,
    garment_type            varchar(100)    NULL,
    collar_type             varchar(100)    NULL,
    sleeve_type             varchar(100)    NULL,
    garment_variant         varchar(100)    NULL,
    sublimation_required    boolean         NOT NULL DEFAULT FALSE,
    embroidery_required     boolean         NOT NULL DEFAULT FALSE,
    dtf_required            boolean         NOT NULL DEFAULT FALSE,
    decoration_notes        varchar(2000)   NULL,
    includes_names          boolean         NOT NULL DEFAULT FALSE,
    includes_numbers        boolean         NOT NULL DEFAULT FALSE,
    includes_logos          boolean         NOT NULL DEFAULT FALSE,
    personalization_notes   varchar(2000)   NULL,
    item_observations       varchar(2000)   NULL,
    CONSTRAINT order_items_pkey PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);

CREATE TABLE order_item_sizes (
    id              uuid            NOT NULL,
    order_item_id   uuid            NOT NULL,
    size            varchar(50)     NOT NULL,
    quantity        integer         NOT NULL,
    CONSTRAINT order_item_sizes_pkey PRIMARY KEY (id),
    CONSTRAINT order_item_sizes_order_item_id_size_key UNIQUE (order_item_id, size),
    CONSTRAINT fk_order_item_sizes_order_item
        FOREIGN KEY (order_item_id)
        REFERENCES order_items (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_orders_status
    ON orders (status);

CREATE INDEX idx_orders_customer_id
    ON orders (customer_id);

-- quotation_id lookups are served by orders_quotation_id_key (UNIQUE).

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);

CREATE INDEX idx_order_item_sizes_order_item_id
    ON order_item_sizes (order_item_id);

-- Production module
-- Aggregate: ProductionOrder / ProductionOperation

CREATE TABLE production_orders (
    id                  uuid            NOT NULL,
    order_id            uuid            NOT NULL,
    creation_date       date            NOT NULL,
    status              varchar(30)     NOT NULL,
    priority            varchar(30)     NOT NULL,
    planned_start_date  date            NULL,
    planned_end_date    date            NULL,
    observations        varchar(2000)   NULL,
    CONSTRAINT production_orders_pkey PRIMARY KEY (id),
    CONSTRAINT production_orders_order_id_key UNIQUE (order_id)
);

CREATE TABLE production_items (
    id                      uuid            NOT NULL,
    production_order_id     uuid            NOT NULL,
    product_name            varchar(255)    NOT NULL,
    quantity                integer         NOT NULL,
    garment_type            varchar(100)    NULL,
    collar_type             varchar(100)    NULL,
    sleeve_type             varchar(100)    NULL,
    garment_variant         varchar(100)    NULL,
    sublimation_required    boolean         NOT NULL DEFAULT FALSE,
    embroidery_required     boolean         NOT NULL DEFAULT FALSE,
    dtf_required            boolean         NOT NULL DEFAULT FALSE,
    decoration_notes        varchar(2000)   NULL,
    includes_names          boolean         NOT NULL DEFAULT FALSE,
    includes_numbers        boolean         NOT NULL DEFAULT FALSE,
    includes_logos          boolean         NOT NULL DEFAULT FALSE,
    personalization_notes   varchar(2000)   NULL,
    item_observations       varchar(2000)   NULL,
    CONSTRAINT production_items_pkey PRIMARY KEY (id),
    CONSTRAINT fk_production_items_production_order
        FOREIGN KEY (production_order_id)
        REFERENCES production_orders (id)
        ON DELETE CASCADE
);

CREATE TABLE production_item_sizes (
    id                      uuid            NOT NULL,
    production_item_id      uuid            NOT NULL,
    size                    varchar(50)     NOT NULL,
    quantity                integer         NOT NULL,
    CONSTRAINT production_item_sizes_pkey PRIMARY KEY (id),
    CONSTRAINT production_item_sizes_production_item_id_size_key UNIQUE (production_item_id, size),
    CONSTRAINT fk_production_item_sizes_production_item
        FOREIGN KEY (production_item_id)
        REFERENCES production_items (id)
        ON DELETE CASCADE
);

CREATE TABLE production_operations (
    id                      uuid            NOT NULL,
    production_order_id     uuid            NOT NULL,
    type                    varchar(30)     NOT NULL,
    status                  varchar(30)     NOT NULL,
    assigned_operator       varchar(255)    NULL,
    planned_start_date      date            NULL,
    planned_end_date        date            NULL,
    actual_start_date       date            NULL,
    actual_end_date         date            NULL,
    observations            varchar(2000)   NULL,
    CONSTRAINT production_operations_pkey PRIMARY KEY (id),
    CONSTRAINT fk_production_operations_production_order
        FOREIGN KEY (production_order_id)
        REFERENCES production_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_production_orders_status
    ON production_orders (status);

CREATE INDEX idx_production_orders_order_id
    ON production_orders (order_id);

CREATE INDEX idx_production_items_production_order_id
    ON production_items (production_order_id);

CREATE INDEX idx_production_item_sizes_production_item_id
    ON production_item_sizes (production_item_id);

CREATE INDEX idx_production_operations_production_order_id
    ON production_operations (production_order_id);

CREATE TABLE production_material_consumptions (
    id                      uuid            NOT NULL,
    production_order_id     uuid            NOT NULL,
    inventory_item_id       uuid            NOT NULL,
    quantity                numeric(19, 4)  NOT NULL,
    unit_of_measure         varchar(50)     NOT NULL,
    consumption_date        timestamp       NOT NULL,
    observation             varchar(2000)   NULL,
    CONSTRAINT production_material_consumptions_pkey PRIMARY KEY (id),
    CONSTRAINT fk_production_material_consumptions_production_order
        FOREIGN KEY (production_order_id)
        REFERENCES production_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_production_material_consumptions_production_order_id
    ON production_material_consumptions (production_order_id);

CREATE INDEX idx_production_material_consumptions_inventory_item_id
    ON production_material_consumptions (inventory_item_id);

CREATE INDEX idx_production_material_consumptions_consumption_date
    ON production_material_consumptions (consumption_date);

CREATE TABLE production_labor_work (
    id                          uuid            NOT NULL,
    production_order_id         uuid            NOT NULL,
    operator_employee_id        uuid            NOT NULL,
    work_date                   date            NOT NULL,
    operation                   varchar(255)    NOT NULL,
    quantity                    numeric(19, 4)  NOT NULL,
    unit_of_measure             varchar(50)     NOT NULL,
    unit_rate                   numeric(19, 2)  NOT NULL,
    calculated_amount           numeric(19, 2)  NOT NULL,
    status                      varchar(30)     NOT NULL,
    observation                 varchar(2000)   NULL,
    paid_at                     timestamp       NULL,
    financial_transaction_id    uuid            NULL,
    CONSTRAINT production_labor_work_pkey PRIMARY KEY (id),
    CONSTRAINT fk_production_labor_work_production_order
        FOREIGN KEY (production_order_id)
        REFERENCES production_orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_production_labor_work_production_order_id ON production_labor_work (production_order_id);
CREATE INDEX idx_production_labor_work_operator_employee_id ON production_labor_work (operator_employee_id);
CREATE INDEX idx_production_labor_work_status ON production_labor_work (status);

-- Inventory module
-- Aggregate: InventoryItem + InventoryMovement history

CREATE TABLE inventory_items (
    id                  uuid            NOT NULL,
    material_code       varchar(100)    NOT NULL,
    name                varchar(255)    NOT NULL,
    category            varchar(255)    NOT NULL,
    material_type       varchar(30)     NOT NULL,
    paper_roll_number   varchar(50)     NULL,
    description         varchar(2000)   NULL,
    unit_of_measure     varchar(50)     NOT NULL,
    stock               numeric(19, 4)  NOT NULL,
    minimum_stock       numeric(19, 4)  NULL,
    unit_cost           numeric(19, 2)  NULL,
    status              varchar(30)     NOT NULL,
    CONSTRAINT inventory_items_pkey PRIMARY KEY (id),
    CONSTRAINT inventory_items_material_code_key UNIQUE (material_code),
    CONSTRAINT inventory_items_paper_roll_number_key UNIQUE (paper_roll_number)
);

CREATE INDEX idx_inventory_items_material_code
    ON inventory_items (material_code);

CREATE INDEX idx_inventory_items_status
    ON inventory_items (status);

CREATE INDEX idx_inventory_items_material_type
    ON inventory_items (material_type);

-- Numeración operacional de rollos de papel Plotter (RP-001, RP-002, ...).
-- Los huecos son aceptables si una transacción falla tras reservar el valor.
CREATE SEQUENCE paper_roll_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE inventory_movements (
    id                  uuid            NOT NULL,
    inventory_item_id   uuid            NOT NULL,
    movement_type       varchar(30)     NOT NULL,
    quantity            numeric(19, 4)  NOT NULL,
    unit_of_measure     varchar(50)     NOT NULL,
    movement_date       timestamp       NOT NULL,
    observation         varchar(2000)   NULL,
    resulting_stock     numeric(19, 4)  NOT NULL,
    unit_cost           numeric(19, 2)  NULL,
    total_cost          numeric(19, 2)  NULL,
    source_type         varchar(30)     NULL,
    source_id           uuid            NULL,
    CONSTRAINT inventory_movements_pkey PRIMARY KEY (id),
    CONSTRAINT fk_inventory_movements_inventory_item
        FOREIGN KEY (inventory_item_id)
        REFERENCES inventory_items (id)
);

CREATE INDEX idx_inventory_movements_inventory_item_id
    ON inventory_movements (inventory_item_id);

CREATE INDEX idx_inventory_movements_movement_date
    ON inventory_movements (movement_date);

CREATE INDEX idx_inventory_movements_source
    ON inventory_movements (source_type, source_id);

-- Un movimiento por origen no nulo (idempotencia Production/Plotter → Inventory).
CREATE UNIQUE INDEX uq_inventory_movements_source
    ON inventory_movements (source_type, source_id)
    WHERE source_type IS NOT NULL
      AND source_id IS NOT NULL;

-- Plotter module
-- Aggregate: PlotterJob
-- Soft refs: customer_id (Commercial), paper_inventory_item_id (Inventory) — no FKs

CREATE TABLE plotter_jobs (
    id                          uuid            NOT NULL,
    customer_id                 uuid            NOT NULL,
    creation_date               date            NOT NULL,
    paper_inventory_item_id     uuid            NOT NULL,
    printed_meters              numeric(19, 4)  NOT NULL,
    price_per_meter             numeric(19, 2)  NOT NULL,
    total_amount                numeric(19, 2)  NOT NULL,
    status                      varchar(30)     NOT NULL,
    observations                varchar(2000)   NULL,
    CONSTRAINT plotter_jobs_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_plotter_jobs_customer_id
    ON plotter_jobs (customer_id);

CREATE INDEX idx_plotter_jobs_creation_date
    ON plotter_jobs (creation_date);

CREATE INDEX idx_plotter_jobs_status
    ON plotter_jobs (status);

CREATE INDEX idx_plotter_jobs_paper_inventory_item_id
    ON plotter_jobs (paper_inventory_item_id);

-- Aggregate: PlotterPayment (pagos de cliente sobre trabajo de Plotter — SPR-036 Inc. 8)
-- Soft ref: plotter_job_id — no FK JPA
-- No se generan automáticamente al crear el PlotterJob

CREATE TABLE plotter_payments (
    id                  uuid            NOT NULL,
    plotter_job_id      uuid            NOT NULL,
    amount              numeric(19, 2)  NOT NULL,
    payment_date        date            NOT NULL,
    observations        varchar(2000)   NULL,
    CONSTRAINT plotter_payments_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_plotter_payments_plotter_job_id
    ON plotter_payments (plotter_job_id);

CREATE INDEX idx_plotter_payments_payment_date
    ON plotter_payments (payment_date);

-- Finance module
-- Aggregate: Payment (pagos de cliente sobre Orden comercial — SPR-019)

CREATE TABLE payments (
    id                  uuid            NOT NULL,
    order_id            uuid            NOT NULL,
    amount              numeric(19, 2)  NOT NULL,
    payment_date        date            NOT NULL,
    observations        varchar(2000)   NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_payments_order_id
    ON payments (order_id);

-- Aggregate: FinancialTransaction (ledger financiero — SPR-036)
-- Soft refs: source_id (Commercial/Plotter/Production/etc.) — no FKs

CREATE TABLE financial_transactions (
    id                  uuid            NOT NULL,
    transaction_type    varchar(30)     NOT NULL,
    amount              numeric(19, 2)  NOT NULL,
    transaction_date    date            NOT NULL,
    category            varchar(2000)   NOT NULL,
    description         varchar(2000)   NULL,
    observation         varchar(2000)   NULL,
    source_type         varchar(30)     NULL,
    source_id           uuid            NULL,
    CONSTRAINT financial_transactions_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_financial_transactions_transaction_date
    ON financial_transactions (transaction_date);

CREATE INDEX idx_financial_transactions_source
    ON financial_transactions (source_type, source_id);

-- Aggregate: RecurringFinancialObligation (obligaciones fijas/recurrentes — SPR-036 Inc. 2)
-- No genera financial_transactions automáticamente.

CREATE TABLE recurring_financial_obligations (
    id                  uuid            NOT NULL,
    name                varchar(255)    NOT NULL,
    obligation_type     varchar(30)     NOT NULL,
    expected_amount     numeric(19, 2)  NOT NULL,
    frequency           varchar(30)     NOT NULL,
    due_day             integer         NULL,
    start_date          date            NOT NULL,
    end_date            date            NULL,
    active              boolean         NOT NULL,
    description         varchar(2000)   NULL,
    observation         varchar(2000)   NULL,
    CONSTRAINT recurring_financial_obligations_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_recurring_financial_obligations_active
    ON recurring_financial_obligations (active);

CREATE INDEX idx_recurring_financial_obligations_name
    ON recurring_financial_obligations (name);

-- Aggregate: RecurringFinancialObligationOccurrence (ocurrencias concretas — SPR-036 Inc. 3)
-- Soft ref: recurring_obligation_id — no FK JPA
-- financial_transaction_id se asigna solo al pagar (PAID)

CREATE TABLE recurring_financial_obligation_occurrences (
    id                          uuid            NOT NULL,
    recurring_obligation_id     uuid            NOT NULL,
    due_date                    date            NOT NULL,
    expected_amount             numeric(19, 2)  NOT NULL,
    status                      varchar(30)     NOT NULL,
    paid_date                   timestamp       NULL,
    financial_transaction_id    uuid            NULL,
    observation                 varchar(2000)   NULL,
    CONSTRAINT recurring_financial_obligation_occurrences_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_recurring_financial_obligation_occurrences_obligation_due
    ON recurring_financial_obligation_occurrences (recurring_obligation_id, due_date);

CREATE INDEX idx_recurring_financial_obligation_occurrences_obligation_id
    ON recurring_financial_obligation_occurrences (recurring_obligation_id);

CREATE INDEX idx_recurring_financial_obligation_occurrences_due_date
    ON recurring_financial_obligation_occurrences (due_date);

CREATE INDEX idx_recurring_financial_obligation_occurrences_status
    ON recurring_financial_obligation_occurrences (status);

-- Un pago de ocurrencia no puede generar dos movimientos del ledger.
CREATE UNIQUE INDEX uq_financial_transactions_recurring_obligation_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'RECURRING_OBLIGATION'
      AND source_id IS NOT NULL;

-- Un Payment comercial no puede generar dos ingresos del ledger.
CREATE UNIQUE INDEX uq_financial_transactions_commercial_order_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'COMMERCIAL_ORDER'
      AND source_id IS NOT NULL;

-- Un pago de Plotter no puede generar dos ingresos del ledger.
CREATE UNIQUE INDEX uq_financial_transactions_plotter_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'PLOTTER'
      AND source_id IS NOT NULL;

-- Aggregate: PayrollEmployee (nómina fija — SPR-036 Inc. 10)
CREATE TABLE payroll_employees (
    id                  uuid            NOT NULL,
    display_name        varchar(255)    NOT NULL,
    active              boolean         NOT NULL,
    compensation_type   varchar(30)     NOT NULL,
    fixed_amount        numeric(19, 2)  NULL,
    frequency           varchar(30)     NULL,
    effective_from      date            NULL,
    effective_to        date            NULL,
    CONSTRAINT payroll_employees_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_payroll_employees_active ON payroll_employees (active);
CREATE INDEX idx_payroll_employees_compensation_type ON payroll_employees (compensation_type);

-- Aggregate: PayrollPeriod (período/pago — soft ref employee_id, no FK)
CREATE TABLE payroll_periods (
    id                          uuid            NOT NULL,
    employee_id                 uuid            NOT NULL,
    period_start                date            NOT NULL,
    period_end                  date            NOT NULL,
    expected_payment_date       date            NOT NULL,
    amount_snapshot             numeric(19, 2)  NOT NULL,
    status                      varchar(30)     NOT NULL,
    actual_payment_date         date            NULL,
    paid_at                     timestamp       NULL,
    financial_transaction_id    uuid            NULL,
    CONSTRAINT payroll_periods_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_payroll_periods_employee_period_start
    ON payroll_periods (employee_id, period_start);

CREATE INDEX idx_payroll_periods_employee_id ON payroll_periods (employee_id);
CREATE INDEX idx_payroll_periods_expected_payment_date ON payroll_periods (expected_payment_date);
CREATE INDEX idx_payroll_periods_status ON payroll_periods (status);

CREATE UNIQUE INDEX uq_financial_transactions_payroll_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'PAYROLL'
      AND source_id IS NOT NULL;
