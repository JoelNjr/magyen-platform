# SPR-023A — Commercial Read API

## Objective

Implement the read API for Commercial quotations.

This sprint introduces the first quotation retrieval endpoint required by the Commercial frontend.

The implementation must strictly follow the existing Clean Architecture and Modular Monolith conventions already established in previous sprints.

The endpoint will expose existing quotations stored in PostgreSQL without introducing any business rule changes.

## Scope

- Read existing quotations
- Backend only
- Reuse existing domain
- Reuse existing persistence
- No frontend
- No authentication
- No pagination
- No filtering
- No sorting

## Out of scope

- Create quotation
- Edit quotation
- Delete quotation
- Approve quotation
- Orders
- Production
- Inventory
- Finance

## Expected Result

A new REST endpoint:

GET /api/v1/quotations

returns the existing quotations stored in the database and becomes the official read endpoint for the Commercial frontend.

The implementation must preserve the existing architecture and compile successfully.