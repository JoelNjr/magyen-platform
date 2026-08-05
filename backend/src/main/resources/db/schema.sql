-- Magyen Platform — Commercial module schema
-- Aggregate: Quotation / QuotationItem
-- Compatible with PostgreSQL 17
-- Hibernate ddl-auto remains validate; this SQL owns schema creation.

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
