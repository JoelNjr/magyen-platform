-- Production order reference image
-- Additive only. Does not insert, update or delete business data.

ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS reference_image_object_key varchar(255) NULL;

ALTER TABLE production_orders
    ADD COLUMN IF NOT EXISTS reference_image_content_type varchar(50) NULL;
