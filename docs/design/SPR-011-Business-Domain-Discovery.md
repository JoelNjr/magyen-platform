# SPR-011 — Business & Domain Discovery

## Objective

Define the complete business vision of Magyen Platform before implementing the remaining business modules.

This document establishes the ubiquitous language, bounded contexts, aggregates, business responsibilities, and strategic roadmap that will guide future development.

---

# Vision

Magyen Platform is not simply an ERP.

It is the operating system of Magyen.

Every commercial, production, inventory, financial, and operational process should be represented inside the platform.

The objective is not only to store information, but to help management make better decisions through real-time information and intelligent recommendations.

---

# Ubiquitous Language

## Customer

Person or company that requests products or services from Magyen.

---

## Quotation

Commercial proposal sent to a customer.

A quotation may have multiple versions but only one approved version.

An approved quotation becomes the origin of one Order.

---

## Order

Commercial commitment accepted by the customer.

Represents real work that the company has agreed to produce.

---

## Production Order

Internal manufacturing instruction generated from an Order.

Responsible for coordinating production stages.

---

## Plotter Job

Record of every print performed by the plotter.

Includes:

- paper consumption
- waste
- operator
- production cost
- execution time

---

## Inventory Movement

Any stock modification.

Examples:

- purchase
- production consumption
- adjustment
- waste

Inventory is never modified directly.

Every stock change must generate a movement.

---

## Financial Movement

Any movement of money.

Examples:

- customer payment
- supplier payment
- utility payment
- payroll
- bank credit
- operating expense

Every financial change generates one Financial Movement.

---

## Fixed Expense

Recurring company expense.

Examples:

- electricity
- rent
- internet
- bank loan
- salaries

---

# Bounded Contexts

## Commercial

Responsible for:

- Customers
- Quotations
- Orders

---

## Production

Responsible for:

- Production Orders
- Production stages
- Timeline
- Quality
- Operators

---

## Plotter

Responsible for:

- Print jobs
- Paper rolls
- Paper consumption
- Waste
- Maintenance
- Printing costs

---

## Inventory

Responsible for:

- Fabrics
- Paper
- Ink
- Threads
- Supplies

Tracks:

- entries
- exits
- adjustments
- waste

---

## Purchasing

Responsible for:

- Suppliers
- Purchase Orders
- Material reception

---

## Financial Center

Responsible for:

- Treasury
- Accounts Receivable
- Accounts Payable
- Cash Flow
- Fixed Expenses
- Credits
- Profitability
- Budgets

---

## Intelligence

Consumes information from every module.

Generates:

- KPIs
- Alerts
- Predictions
- Recommendations

Does not own business data.

---

## Personnel

Responsible for every employee.

One employee may have multiple roles.

Examples:

- Sales
- Design
- Plotter
- Calender
- Sewing
- Administration

---

# Main Aggregates

- Customer
- Quotation
- Order
- ProductionOrder
- PlotterJob
- InventoryItem
- InventoryMovement
- PurchaseOrder
- FinancialMovement
- FixedExpense
- Employee

---

# Business Flow

Customer

↓

Quotation

↓

Design Approval

↓

Customer Approval

↓

Advance Payment

↓

Order

↓

Production

↓

Delivery

↓

Final Payment

↓

Closed

---

# Strategic Vision

Version 1

Complete ERP capable of operating Magyen daily.

Includes:

- Commercial
- Production
- Plotter
- Inventory
- Purchasing
- Financial Center
- Dashboard

---

Version 2

Business Intelligence.

Includes:

- intelligent alerts
- profitability analysis
- financial projections
- inventory prediction
- customer analysis

---

Version 3

Digital transformation.

Includes:

- Mobile App
- QR tracking
- Customer Portal
- Electronic invoicing
- WhatsApp notifications
- Conversational AI

---

# Architectural Principles

- Domain drives the software.
- Business before implementation.
- Every module owns its own responsibilities.
- Communication between modules must respect bounded contexts.
- Clean Architecture remains the foundation.
- Domain Events will coordinate future integrations.