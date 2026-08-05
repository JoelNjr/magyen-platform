# SPR-013 — Order Application Layer

## Objective

Implement the complete Application Layer for creating an Order from an approved Quotation.

This sprint introduces the first Application flow for the Order aggregate while preserving Clean Architecture.

---

# Business Goal

Once a quotation has been approved by the customer, the commercial department confirms it and creates an Order.

An Order always originates from exactly one approved Quotation.

The Application Layer is responsible only for orchestrating the process.

Business rules remain inside the Domain.

Persistence remains behind a Repository Port.

---

# Scope

Included

- OrderRepository Port
- CreateOrderFromQuotationCommand
- CreateOrderFromQuotationResult
- CreateOrderFromQuotationUseCase

Not Included

- REST Controller
- Persistence
- JPA
- PostgreSQL
- Production module
- Inventory module

---

# Architectural Flow

Presentation

↓

CreateOrderFromQuotationCommand

↓

CreateOrderFromQuotationUseCase

↓

Order.create(...)

↓

OrderRepository.save(...)

↓

CreateOrderFromQuotationResult

---

# Responsibilities

## Controller

Receive HTTP request.

Never execute business rules.

---

## Command

Carry input data.

No business logic.

---

## Use Case

Coordinate:

- validate command
- load quotation
- create order
- save order
- return result

---

## Repository

Persistence abstraction.

Owned by Domain.

Implemented later by Infrastructure.

---

## Domain

Owns every business rule.

Application never changes Order state directly.

---

# Initial API

Input

- quotationId
- orderNumber
- deliveryDate
- salesperson
- observations

Output

- orderId
- orderNumber
- status
- confirmationDate

---

# Non Goals

No production.

No inventory.

No finance.

No payments.

No notifications.

No events.

Those belong to future sprints.
