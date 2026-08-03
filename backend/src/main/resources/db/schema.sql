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
