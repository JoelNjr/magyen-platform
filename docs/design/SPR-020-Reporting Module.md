# SPR-020 – Reporting Module

## Objetivo

Implementar el módulo de Reportes del ERP Magyen siguiendo la arquitectura Clean Architecture ya utilizada en los módulos Commercial, Production, Inventory y Finance.

A diferencia de los módulos anteriores, Reporting será un módulo de consultas (Read Model). Su responsabilidad será obtener información consolidada proveniente de otros módulos y transformarla en indicadores útiles para apoyar la toma de decisiones.

El módulo no será propietario de información de negocio, no modificará estados y no persistirá datos propios.

---

# Alcance

Durante este Sprint se implementarán los primeros reportes operativos del ERP.

## Reporte de Ventas

Permite consultar:

- Total vendido
- Cantidad de órdenes
- Promedio por venta

---

## Reporte de Producción

Permite consultar:

- Cantidad de órdenes por estado:
    - Pending
    - In Progress
    - Completed

---

## Reporte de Inventario

Permite listar los materiales cuyo stock se encuentre por debajo del stock mínimo.

---

## Reporte de Pagos

Permite consultar:

- Total recibido
- Cantidad de pagos
- Promedio por pago

---

# Restricciones

El módulo Reporting:

- No tendrá Aggregate Root.
- No tendrá Value Objects propios.
- No tendrá reglas de negocio.
- No persistirá información.
- No tendrá tablas nuevas.
- No modificará información existente.
- No reemplazará la lógica de los demás módulos.
- Solamente consolidará información.

---

# Arquitectura esperada

El módulo deberá seguir Clean Architecture.

Presentation
↓
Application
↓
Infrastructure

No existirá una capa Domain tradicional ya que Reporting no representa conceptos del dominio, únicamente consultas.

---

# Casos de uso

El módulo deberá permitir:

- Consultar reporte de ventas.
- Consultar reporte de producción.
- Consultar reporte de inventario.
- Consultar reporte de pagos.

---

# Endpoints

GET /api/v1/reports/sales

GET /api/v1/reports/production

GET /api/v1/reports/inventory

GET /api/v1/reports/payments

---

# Persistencia

Reporting utilizará únicamente los repositorios existentes de:

- Commercial
- Production
- Inventory
- Finance

No tendrá repositorios propios de escritura.

---

# SQL

No se crearán tablas nuevas.

No se modificará schema.sql.

---

# Objetivo arquitectónico

El propósito del módulo Reporting es separar completamente las consultas de la lógica transaccional del sistema.

Commercial, Production, Inventory y Finance continúan siendo los propietarios de las reglas de negocio.

Reporting únicamente consume información ya existente para generar indicadores consolidados.

Esto mantiene la arquitectura limpia, evita duplicar reglas de negocio y prepara la plataforma para futuros dashboards y reportes gerenciales.

---

# Incrementos

## Incremento 1

Architecture Review

## Incremento 2

Application Layer

## Incremento 3

Infrastructure Layer

## Incremento 4

Presentation Layer

## Incremento 5

End-to-End Verification