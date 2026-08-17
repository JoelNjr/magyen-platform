# SPR-038 — Incremento C: Unificación de empleado y mano de obra de producción

**Incremento:** C  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento C. No describe autenticación (`SPR-038-Auth-Security.md`), el Incremento A (catálogos comerciales), el Incremento B (costos de inventario), el Incremento D (`SPR-038-Increment-D-Plotter-Internal-External.md`) ni el Incremento E (`SPR-038-Increment-E-Employees-Sellers-Deductions.md`).

---

## 1. Objetivo

Eliminar la duplicación conceptual entre empleados de Finanzas y operarios de Producción.

Magyen necesita **un solo registro de persona interna** para quien participa en nómina o en mano de obra de producción.

**Production operators are Finance PayrollEmployees with compensation type PRODUCTION_BASED.**

---

## 2. Propiedad de la identidad

Finance es dueño de:

* `PayrollEmployee`
* `PayrollPeriod`
* tipo de compensación
* estado activo/inactivo
* identidad del empleado

Production es dueño de:

* `ProductionLaborWork`
* historial de operación/trabajo
* cantidad, unidad, tarifa y monto calculado
* estado de la mano de obra (`PENDING`, `PAID`, `CANCELLED`)

Production **no** mantiene un segundo catálogo de personas.

`AuthenticationUser` permanece separado. No hay vínculo entre login y empleado en este incremento.

Vendedores y clientes no eran empleados automáticamente en este incremento. La unificación de vendedores comerciales con `PayrollEmployee` FIXED quedó en `SPR-038-Increment-E-Employees-Sellers-Deductions.md`.

---

## 3. Tipos de compensación

| Tipo de pago (UI) | Valor interno | Nómina fija | Selector de mano de obra |
|---|---|---|---|
| Fijo | `FIXED_PAYROLL` | Sí | Nunca |
| Por producción | `PRODUCTION_BASED` | No | Solo si está activo |

Reglas:

* Un empleado inactivo no es seleccionable para **nuevo** trabajo.
* El historial de mano de obra permanece válido si el empleado se desactiva después.
* Cambiar el tipo de compensación no reescribe trabajos históricos.
* No se borran empleados.

La familia ve las etiquetas **Fijo** y **Por producción**. No se exponen los nombres técnicos del enum.

---

## 4. Relación Production ↔ Finance

```
Finance PayrollEmployee
        |
        | ProductionLaborEmployeePort
        v
Selector de operarios (Mano de obra)
        |
        v
ProductionLaborWork.operatorEmployeeId = PayrollEmployee.id
        |
        v
pago explícito
        |
        v
Finance EXPENSE / PAYROLL
        |
        v
misma acumulación de ganancias del empleado
```

`operatorEmployeeId` es un UUID suave a `payroll_employees.id`. No hay FK JPA entre módulos.

No queda `ProductionOperator` como fuente de verdad. La tabla física `production_operators` se retiene en el esquema live para no destruir la base V1; Hibernate ya no la mapea y la aplicación no la usa.

En la base local ya existía un trabajo de mano de obra histórico cuyo `operator_employee_id` apunta al UUID leftover de `production_operators`, no al `PayrollEmployee` homónimo. **No se mutó esa fila.** El trabajo nuevo sí usa `PayrollEmployee.id`. Un remap explícito de esa fila histórica queda fuera de este incremento por seguridad de datos.

---

## 5. Pago de mano de obra

Registrar trabajo **no** crea gasto en Finanzas.

Pagar trabajo:

1. Production: `PENDING` → `PAID`
2. Finance: exactamente un `EXPENSE`
   * categoría `PAYROLL`
   * `sourceType = PAYROLL`
   * `sourceId = laborWorkId`
   * descripción: `Pago de mano de obra - {nombre}`

Un segundo pago del mismo trabajo sigue siendo idempotente / 409 según el contrato existente.

El empleado permanece identificable por el nombre en la descripción y por el `PayrollEmployee.id` del trabajo.

---

## 6. Resumen de ganancias por producción

Es un modelo de lectura. **No** es un motor de nómina.

Fuente de verdad: historial de `ProductionLaborWork`.

Finance consulta Production por puerto (`EmployeeProductionEarningsPort`). No se duplica persistencia.

Para un empleado `PRODUCTION_BASED` y un rango de fechas:

* trabajos (PENDING + PAID)
* unidades/cantidad
* generado (PENDING + PAID)
* pagado (solo PAID)
* pendiente (solo PENDING)

`CANCELLED` no entra.

`FIXED_PAYROLL` no muestra ganancias de producción. Mensaje:

«Este empleado recibe pago fijo y no registra mano de obra por producción.»

Fuera de alcance de este resumen:

* impuestos
* seguridad social
* deducciones
* comisiones
* salarios automáticos
* tarifas automáticas de mano de obra

---

## 7. Nómina fija

El flujo SPR-036 de períodos `FIXED_PAYROLL` no cambia.

Los empleados `PRODUCTION_BASED` siguen excluidos de la generación de períodos fijos.

Los dos modelos de compensación permanecen distintos.

---

## 8. Autorización

Sin rediseño de JWT.

* `ADMIN`: gestiona empleados y nómina; accede a Finanzas.
* `OPERATOR`: puede registrar y pagar mano de obra de producción (si la matriz V1 lo permite); no gestiona el maestro de empleados (`/api/v1/finance/**` sigue siendo ADMIN).

---

## 9. UI

**Finanzas → Empleados** es el lugar para crear y editar personas.

Tipo de pago: `[ Fijo ] [ Por producción ]`.

Los empleados por producción se entienden visualmente como operarios seleccionables en Mano de obra.

**Producción → Mano de obra** carga solo empleados activos `PRODUCTION_BASED` vía `/api/v1/production/labor-operators`.

Estado vacío:

«No hay operarios disponibles. Crea un empleado con pago por producción en Finanzas → Empleados.»

La pantalla Producción → Operarios se elimina. Asignar operario a una **operación** de fabricación (texto libre) no es el catálogo de empleados y permanece.

---

## 10. Esquema

No se aplicó SQL manual en este incremento.

`schema.sql`:

* `payroll_employees` sigue siendo la tabla de empleados.
* `production_labor_work.operator_employee_id` documentado como UUID suave a `payroll_employees.id`.
* `production_operators` se conserva como tabla leftover, sin mapeo JPA.

Hibernate `ddl-auto` permanece `validate`. No se resetea, trunca ni recrea la base.

---

## 11. Fuera de alcance

* Comisiones
* Tarifas automáticas de mano de obra
* Motor de nómina (impuestos, seguridad social, deducciones)
* Vínculo empleado ↔ usuario de autenticación
* PDF, notificaciones, Intelligence

Los trabajos internos de Plotter quedaron en `SPR-038-Increment-D-Plotter-Internal-External.md`.

La unificación de vendedores y la fundación de descuentos de nómina quedaron en `SPR-038-Increment-E-Employees-Sellers-Deductions.md`.
