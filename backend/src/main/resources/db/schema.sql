-- Magyen Platform — Commercial module schema
-- Aggregates: Customer / Quotation / QuotationItem
-- Compatible with PostgreSQL 17
-- Hibernate ddl-auto remains validate; this SQL owns schema creation.

CREATE TABLE customers (
    id      uuid            NOT NULL,
    name    varchar(255)    NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

CREATE TABLE quotations (
    id              uuid            NOT NULL,
    customer_id     uuid            NOT NULL,
    creation_date   date            NOT NULL,
    delivery_date   date            NOT NULL,
    status          varchar(20)     NOT NULL,
    salesperson     varchar(255)    NOT NULL,
    observations    varchar(2000)   NULL,
    total_amount    numeric(19, 2)  NOT NULL,
    CONSTRAINT quotations_pkey PRIMARY KEY (id)
);

CREATE TABLE quotation_items (
    id              uuid            NOT NULL,
    quotation_id    uuid            NOT NULL,
    product_name    varchar(255)    NOT NULL,
    quantity        integer         NOT NULL,
    fabric          varchar(255)    NOT NULL,
    color           varchar(100)    NOT NULL,
    unit_price      numeric(19, 2)  NOT NULL,
    subtotal        numeric(19, 2)  NOT NULL,
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
    CONSTRAINT orders_pkey PRIMARY KEY (id)
);

CREATE TABLE order_items (
    id              uuid            NOT NULL,
    order_id        uuid            NOT NULL,
    product_name    varchar(255)    NOT NULL,
    quantity        integer         NOT NULL,
    fabric          varchar(255)    NOT NULL,
    color           varchar(100)    NOT NULL,
    unit_price      numeric(19, 2)  NOT NULL,
    subtotal        numeric(19, 2)  NOT NULL,
    CONSTRAINT order_items_pkey PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_orders_status
    ON orders (status);

CREATE INDEX idx_orders_customer_id
    ON orders (customer_id);

CREATE INDEX idx_orders_quotation_id
    ON orders (quotation_id);

CREATE INDEX idx_order_items_order_id
    ON order_items (order_id);

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

CREATE INDEX idx_production_operations_production_order_id
    ON production_operations (production_order_id);

-- Inventory module
-- Aggregate: InventoryItem

CREATE TABLE inventory_items (
    id                  uuid            NOT NULL,
    material_code       varchar(100)    NOT NULL,
    name                varchar(255)    NOT NULL,
    category            varchar(255)    NOT NULL,
    unit_of_measure     varchar(50)     NOT NULL,
    stock               numeric(19, 4)  NOT NULL,
    minimum_stock       numeric(19, 4)  NOT NULL,
    status              varchar(30)     NOT NULL,
    CONSTRAINT inventory_items_pkey PRIMARY KEY (id),
    CONSTRAINT inventory_items_material_code_key UNIQUE (material_code)
);

CREATE INDEX idx_inventory_items_material_code
    ON inventory_items (material_code);

CREATE INDEX idx_inventory_items_status
    ON inventory_items (status);

-- Finance module
-- Aggregate: Payment

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
