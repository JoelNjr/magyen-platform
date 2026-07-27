# Magyen Platform

# ADR-001 - Backend Architecture

**Estado:** Aprobado

**Fecha:** 27 de julio de 2026

**Autor:** Joel David Vásquez

---

# Contexto

Magyen Platform será una plataforma de gestión para la industria textil.

El sistema deberá poder evolucionar durante muchos años sin perder mantenibilidad, claridad ni flexibilidad.

Por esta razón era necesario definir una arquitectura base antes de comenzar el desarrollo.

---

# Decisión

El backend será desarrollado utilizando una combinación de los siguientes enfoques:

- Modular Monolith
- Clean Architecture
- Domain Driven Design (DDD) ligero

---

# Justificación

## Modular Monolith

Durante las primeras versiones la plataforma será utilizada por una única empresa.

No existe necesidad de dividir el sistema en microservicios.

Un Monolito Modular reduce la complejidad inicial y facilita el desarrollo.

---

## Clean Architecture

La lógica del negocio deberá permanecer independiente del framework.

Spring Boot será únicamente una herramienta de infraestructura.

El dominio nunca dependerá de Spring.

---

## Domain Driven Design (DDD) ligero

El negocio posee procesos complejos como:

- Producción
- Cotizaciones
- Inventario
- Planeación
- Producción
- Finanzas

Por esta razón el lenguaje del código deberá reflejar el lenguaje del negocio.

---

# Consecuencias

Esta decisión permitirá:

- Escalar el proyecto sin reescribir la arquitectura.
- Mantener bajo acoplamiento entre módulos.
- Facilitar las pruebas.
- Permitir una futura migración a microservicios si el crecimiento del negocio lo requiere.

---

# Estado

Esta decisión será considerada oficial para todas las versiones 1.x de Magyen Platform.