# SPR-032 — Production Operations Consolidation

## Objective

Review, consolidate and progressively complete the Production module.

The objective is not to rebuild Production from scratch.

The existing implementation must be inspected first so that future increments extend the current architecture instead of duplicating or replacing existing functionality.

The final goal is to establish a reliable Production flow connected to the existing Commercial quotation process and prepared for the future Inventory integration.

## Current project context

Commercial currently provides:

- Customer Create / Read / Update
- Customer selection in quotations
- Quotation creation
- Quotation approval
- Persistent quotation commercial numbering
- UUID quotation technical identity
- quotationNumber commercial identity

SPR-031 established persistent quotation numbering using a PostgreSQL sequence.

The next major functional area is Production.

## Target conceptual flow

The intended high-level business flow is:

Customer
→ Quotation
→ Quotation Approval
→ Production
→ Production Operations
→ Material / Inventory
→ Completion

This flow is a conceptual target only.

The actual implementation must be determined from the existing codebase before new functionality is created.

## Main objectives

### 1. Production architecture

Understand and document the existing Production implementation.

Review:

- Domain
- Application
- Infrastructure
- Persistence
- Presentation
- REST
- Frontend
- Database
- Tests
- Documentation

### 2. Production domain

Identify the domain concepts that already exist.

Examples to investigate:

- Production
- Production Order
- Production Operation
- Production Item
- Material
- Quantity
- Status
- Assignment
- Completion
- Cancellation

Only concepts that actually exist in the codebase or documentation should be treated as existing architecture.

Do not invent new concepts during the architecture review.

### 3. Commercial integration

Determine the current relationship between:

- Quotations
- Approved quotations
- Quotation items
- Production

Determine whether:

- quotation approval creates production
- Production references quotationId
- Production references quotation items
- a production order already exists
- production creation is manual
- integration is partial
- integration is missing

### 4. Production lifecycle

Document the existing Production lifecycle and status transitions.

Identify:

- valid states
- valid transitions
- invalid transitions
- actions available to users
- persistence of state

### 5. Production persistence

Review:

- database tables
- columns
- primary keys
- foreign keys
- indexes
- constraints
- entity mappings
- persistence mappers
- repositories

Determine whether the current schema supports the existing Production flow.

### 6. Production REST API

Review:

- controllers
- endpoints
- request DTOs
- response DTOs
- mappers
- use cases
- error handling

Determine which endpoints are complete, partial or missing.

### 7. Production frontend

Review:

- pages
- routes
- components
- forms
- tables
- actions
- API services
- loading states
- empty states
- error states

Determine which functionality is actually connected to the backend.

### 8. Production → Inventory relationship

Determine whether Production currently:

- references materials
- reserves materials
- consumes materials
- creates inventory movements
- deducts inventory
- tracks material quantities

Do not implement Inventory functionality during the architecture review.

## Architectural assessment

Each relevant feature must be classified as:

- Complete
- Partially implemented
- Missing
- Inconsistent
- Technical debt

## Risks to evaluate

Pay particular attention to:

- invalid Production state transitions
- duplicate production records
- missing quotation references
- inconsistent quantities
- missing quotation item references
- lack of transaction boundaries
- Production / Inventory inconsistency
- stale frontend state
- frontend/backend contract mismatch
- persistence inconsistencies

## Implementation principles

Future increments must:

- preserve the existing architecture
- prefer small changes
- avoid unnecessary rewrites
- maintain domain/application/presentation separation
- preserve existing Commercial contracts
- preserve quotation UUID identity
- preserve quotation commercial numbering
- avoid introducing unnecessary libraries
- verify each increment independently

## Dependencies

Production may interact with:

- Commercial
- Quotations
- Customers
- Inventory
- Finance

These relationships must be documented before implementation.

## Out of scope

The following are not part of the initial architecture review:

- Authentication
- Authorization
- Frontend UX/UI polish
- Finance implementation
- Intelligence implementation
- Customer Delete
- Quotation numbering changes
- Database cleanup for V1
- General refactoring
- New libraries unrelated to Production

## Proposed incremental roadmap

| Increment | Focus |
|---|---|
| 1 | Production architecture and current-state review |
| 2 | Domain/Application completion |
| 3 | Persistence/Infrastructure completion |
| 4 | REST/API completion |
| 5 | Commercial → Production integration |
| 6 | Production frontend |
| 7 | Initial Production → Inventory integration |
| 8 | End-to-end verification |
| 9 | Final review and sprint closure |

The exact roadmap may be adjusted after Increment 1 based on the actual codebase.

## V1 relationship

Production must be sufficiently stable before V1.

However, authentication, authorization, final UX/UI polish and global V1 hardening will be handled later after the core functional modules are consolidated.

## Final status

SPR-032 begins with architecture and discovery.

No implementation should be performed until the current Production architecture has been reviewed.

The purpose of the first increment is to establish an evidence-based implementation roadmap rather than assume what already exists.