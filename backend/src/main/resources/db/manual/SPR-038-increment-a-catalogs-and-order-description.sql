-- SPR-038 Increment A — catálogos comerciales y descripción de pedido
-- Additive only. Does not insert business data. Does not reset or truncate.

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS description varchar(2000) NULL;

ALTER TABLE quotation_items
    ADD COLUMN IF NOT EXISTS cuff_required boolean NULL;

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS cuff_required boolean NULL;

ALTER TABLE production_items
    ADD COLUMN IF NOT EXISTS cuff_required boolean NULL;
