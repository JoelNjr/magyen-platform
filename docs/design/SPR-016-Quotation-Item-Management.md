# SPR-016 — Quotation Item Management

## Goal

Implement complete management of quotation items.

A quotation must be able to receive products through the Application layer and REST API before it can be approved.

The sprint will implement:

- Add quotation item
- Remove quotation item
- Persistence integration
- REST endpoint
- End-to-end validation

Business rules:

1. Items can only be modified while Quotation is DRAFT.
2. Quantity must be greater than zero.
3. Unit price must be greater than zero.
4. Product name cannot be blank.
5. Fabric cannot be blank.
6. Color cannot be blank.
7. Quotation total must always be recalculated.
8. Approval rules remain unchanged.
9. No direct repository manipulation from Presentation.
10. Application orchestrates only.
11. Domain owns all quotation item rules.

Out of scope

- Editing existing items
- Discounts
- Taxes
- Inventory reservation
- Automatic production creation

Expected result

POST /api/v1/quotations

↓

POST /api/v1/quotations/{id}/items

↓

PATCH /approve

↓

POST /api/v1/orders

↓

POST /api/v1/production-orders