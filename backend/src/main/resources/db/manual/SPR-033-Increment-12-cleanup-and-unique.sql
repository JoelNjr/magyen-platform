-- =============================================================================
-- SPR-033 Increment 12 — Manual cleanup + UNIQUE(quotation_id)
-- =============================================================================
-- SAFETY:
--   - Do NOT run this file blindly end-to-end.
--   - Choose KEEP vs DELETE for each duplicate group first.
--   - Only one DELETE per group must remain uncommented.
--   - Deleting an Order cascades to order_items and order_item_sizes
--     (fk_order_items_order / fk_order_item_sizes_order_item ON DELETE CASCADE).
--   - This does NOT delete quotations or customers.
--   - No production_orders currently reference these duplicate Orders.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0) Pre-check (non-destructive)
-- -----------------------------------------------------------------------------
SELECT quotation_id, COUNT(*) AS cnt
FROM orders
GROUP BY quotation_id
HAVING COUNT(*) > 1
ORDER BY quotation_id;

SELECT COUNT(*) AS order_count_before FROM orders;

-- =============================================================================
-- GROUP A — quotation_id = 4536e642-6c11-4ec6-9038-c5d73e13c289
-- =============================================================================
-- Candidates:
--
-- A1) KEEP candidate (has SizeBreakdown S:10, M:4)
--     id           = 78d3813d-54f5-440a-9325-a83e5c597323
--     order_number = Sudaderas San Jose
--     items        = Sudadera x14 (Fastra / Azul) 910000
--
-- A2) KEEP candidate (same commercial line, NO sizes)
--     id           = c33cc169-beda-43a8-ae45-101cd059fd5b
--     order_number = 12
--     items        = Sudadera x14 (Fastra / Azul) 910000
--
-- >>> Choose ONE delete option below (uncomment exactly one).
-- =============================================================================

-- OPTION A-KEEP-SanJose / DELETE-12
-- KEEP:  78d3813d-54f5-440a-9325-a83e5c597323  (Sudaderas San Jose)
-- DELETE:
-- DELETE FROM orders WHERE id = 'c33cc169-beda-43a8-ae45-101cd059fd5b';

-- OPTION A-KEEP-12 / DELETE-SanJose
-- KEEP:  c33cc169-beda-43a8-ae45-101cd059fd5b  (12)
-- DELETE:
-- DELETE FROM orders WHERE id = '78d3813d-54f5-440a-9325-a83e5c597323';

-- =============================================================================
-- GROUP B — quotation_id = 84e00121-98d0-4021-b189-29c43aebe089
-- =============================================================================
-- Candidates (commercially equivalent snapshots; both order_number ORD-SPR033-INC3;
--             no sizes; empty ProductSpecification flags):
--
-- B1) id = 8316709c-8918-4ea6-8999-bca7d8c36769
--     items:
--       camisa x6 (hydrotech/azul) 300000
--       Camiseta x10 (Algodon/Negro) 150000
--       Uniforme de futbol x10 (Sudafrica/Blanco) 600000
--
-- B2) id = 94945b03-24f2-4d69-bab5-ac3afa1a6590
--     items: same commercial content as B1 (different item UUIDs)
--
-- >>> Choose ONE delete option below (uncomment exactly one).
-- =============================================================================

-- OPTION B-KEEP-8316709c / DELETE-94945b03
-- KEEP:  8316709c-8918-4ea6-8999-bca7d8c36769
-- DELETE:
-- DELETE FROM orders WHERE id = '94945b03-24f2-4d69-bab5-ac3afa1a6590';

-- OPTION B-KEEP-94945b03 / DELETE-8316709c
-- KEEP:  94945b03-24f2-4d69-bab5-ac3afa1a6590
-- DELETE:
-- DELETE FROM orders WHERE id = '8316709c-8918-4ea6-8999-bca7d8c36769';

-- -----------------------------------------------------------------------------
-- 1) Post-cleanup verification (must return 0 rows)
-- -----------------------------------------------------------------------------
-- SELECT quotation_id, COUNT(*) AS cnt
-- FROM orders
-- GROUP BY quotation_id
-- HAVING COUNT(*) > 1;

-- SELECT COUNT(*) AS order_count_after FROM orders;

-- -----------------------------------------------------------------------------
-- 2) Apply UNIQUE only after duplicates = 0
-- -----------------------------------------------------------------------------
-- SELECT conname
-- FROM pg_constraint
-- WHERE conrelid = 'orders'::regclass
--   AND contype = 'u'
--   AND conname = 'orders_quotation_id_key';

-- ALTER TABLE orders
--     ADD CONSTRAINT orders_quotation_id_key UNIQUE (quotation_id);

-- Redundant non-unique index becomes unnecessary once UNIQUE exists:
-- DROP INDEX IF EXISTS idx_orders_quotation_id;

-- -----------------------------------------------------------------------------
-- 3) Final verification
-- -----------------------------------------------------------------------------
-- SELECT conname
-- FROM pg_constraint
-- WHERE conrelid = 'orders'::regclass
--   AND contype = 'u';

-- SELECT indexname
-- FROM pg_indexes
-- WHERE tablename = 'orders';
