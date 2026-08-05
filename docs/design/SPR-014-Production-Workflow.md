# SPR-014 — Production Workflow

## Goal

Transform confirmed commercial Orders into executable production work.

Production is modeled as an independent workflow that follows the commercial module.

---

## Scope

Included

- Production Order
- Production lifecycle
- Production operations
- Assignment
- Priorities
- Planned dates

Not included

- Inventory reservation
- Plotter consumption
- Finance
- Notifications
- Metrics
- Dashboards

---

## Business Vision

A confirmed commercial Order may generate exactly one Production Order.

The Production Order coordinates every manufacturing activity until completion.

Commercial commitments and manufacturing execution remain independent aggregates.

---

## Core Concept

Production is not represented by many unrelated entities.

Instead, a Production Order owns multiple Production Operations.

Examples:

- Cutting
- Calendering
- Sublimation
- Sewing
- Quality Control

Future operations can be added without redesigning the aggregate.

---

## Initial Production Status

CREATED

↓

PLANNED

↓

IN_PROGRESS

↓

COMPLETED

---

## First Operations

CUTTING

CALENDERING

SUBLIMATION

SEWING

QUALITY_CONTROL

---

## Clean Architecture

Presentation

↓

Application

↓

Domain

↓

Infrastructure

Production remains isolated from Commercial.

Commercial never depends on Production.

Production references the commercial Order by ID only.