# SPR-015 — Commercial ↔ Production Integration

## Objetivo

Integrar el módulo Comercial con Producción respetando Clean Architecture y DDD.

La Producción nunca debe depender directamente del agregado Order.

La referencia será únicamente por identidad (OrderId).

---

## Alcance

Este sprint implementa:

- ProductionRepository
- CreateProductionOrderFromOrderUseCase
- DTOs
- Persistence
- REST API
- Integración con Commercial mediante Application Layer

No implementa:

- Eventos de dominio
- Sincronización automática de estados
- Inventario
- Finanzas
- Dashboard

---

## Flujo

Quotation

↓

APPROVED

↓

Order

↓

POST /production-orders

↓

ProductionOrder CREATED

---

## Reglas

Una Production Order:

- siempre proviene de una Order

- referencia Order solamente por UUID

- inicia en CREATED

- prioridad NORMAL por defecto

- no crea operaciones automáticamente (por ahora)

- no modifica el estado del Order

---

## Arquitectura

Presentation

↓

Application

↓

Production Domain

↓

Production Repository

↓

Infrastructure

Commercial y Production permanecen desacoplados.

La sincronización de estados llegará en un sprint posterior.

---

## Incrementos

Incremento 1

Architecture Review

Incremento 2

Repository Port

Incremento 3

Application DTOs

Incremento 4

CreateProductionOrderFromOrderUseCase

Incremento 5

Persistence

Incremento 6

REST API

Incremento 7

End-to-End Review