# SPR-038 — Incremento E: Empleados, vendedores y fundación de descuentos de nómina

**Incremento:** E  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento E. No inicia SPR-039. No describe autenticación, catálogos comerciales A, costos de inventario B, unificación de operarios C ni Plotter interno/externo D, salvo las dependencias explícitas.

---

## 1. Identidad de empleado

Finance es dueño de la única identidad interna de persona:

`PayrollEmployee`

No hay un segundo catálogo de vendedores comerciales ni de operarios de producción.

`AuthenticationUser` permanece separado. Este incremento no vincula login con empleado.

---

## 2. Elegibilidad por tipo de compensación

| Tipo de pago (UI) | Valor interno | Vendedor comercial | Nómina fija | Mano de obra |
|---|---|---|---|---|
| Fijo | `FIXED_PAYROLL` | Sí, si está activo | Sí | Nunca |
| Por producción | `PRODUCTION_BASED` | Nunca | No | Sí, si está activo |

`Puede vender` y `Puede hacer producción` se derivan del tipo. No son permisos configurables en V1.

Un empleado inactivo:

* no aparece en el selector de vendedor
* no aparece en el selector de mano de obra
* permanece visible en historial

Los empleados no se borran.

---

## 3. Unificación de vendedores

Commercial ya no es fuente de verdad de vendedores.

```
Finance PayrollEmployee (FIXED_PAYROLL + activo)
        |
        | CommercialSellerEmployeePort
        v
Selector de vendedor (cotización)
        |
        v
Quotation/Order.sellerId = PayrollEmployee.id
```

`sellerId` conserva el nombre de columna y del API. El UUID nuevo es `payroll_employees.id`. No hay FK JPA entre módulos.

La UI muestra **Empleado vendedor / Vendedor**. El usuario selecciona un empleado. No hay texto libre.

El nombre se resuelve desde Finance en lectura. No se duplica el nombre como fuente de verdad.

---

## 4. Compatibilidad histórica de sellers

La base live ya tenía:

* 4 filas en `sellers`
* 2 cotizaciones y 1 orden cuyo `seller_id` apunta a `sellers.id`, no a `payroll_employees.id`
* un `PayrollEmployee` homónimo (`Joel David Vasquez`) con UUID distinto al leftover `sellers.id`

**No se remapearon ni se borraron esas filas.** Inventar un mapeo por nombre sería inseguro.

La tabla `sellers` queda leftover, igual que `production_operators` en el Incremento C:

* Hibernate sigue mapeándola solo para resolver el nombre histórico
* `GET /api/v1/sellers` ya no lista ese catálogo
* `POST /api/v1/sellers` se elimina
* las lecturas históricas resuelven primero Finance y, si no hay empleado, el leftover `sellers`

Las cotizaciones/órdenes nuevas usan `PayrollEmployee.id`.

---

## 5. Operarios de producción

Sin regresión del Incremento C.

El selector de mano de obra sigue usando empleados activos `PRODUCTION_BASED` vía `ProductionLaborEmployeePort`.

Pagar un trabajo de mano de obra sigue creando exactamente un `EXPENSE` / `PAYROLL`.

---

## 6. Descuentos de nómina

Agregado Finance: `PayrollDeduction`.

Campos:

* `id`
* `employeeId` (UUID suave a `payroll_employees.id`)
* `type`: `LOAN` | `ADVANCE` | `OTHER`
* `amount` (escala 2, HALF_UP, mayor que cero)
* `deductionDate`
* `description` (opcional)
* `status`: `ACTIVE` | `CANCELLED`
* `createdAt`

Etiquetas UI:

* Préstamo
* Anticipo
* Otro descuento

Un descuento **no** es un gasto de Magyen. Registrar o cancelar no crea `FinancialTransaction` de `EXPENSE` ni de `INCOME`.

Puede pertenecer a un empleado `FIXED_PAYROLL` o `PRODUCTION_BASED`. No hay restricción por tipo de compensación. El empleado debe existir. Un inactivo conserva historial.

No hay liquidación automática contra la próxima nómina. El modelo deja el punto de extensión:

`compensación bruta - descuentos ACTIVE = compensación neta`

Ese cálculo no se implementa en este incremento.

Fuera de alcance: cuotas, interés, recurrencia, impuestos, seguridad social.

---

## 7. Ciclo de vida

* `POST` registra un descuento `ACTIVE`
* listar por empleado
* listar activos (`?status=ACTIVE`)
* `PATCH .../cancel` cancela sin borrar

Un descuento cancelado permanece en el historial y no entra en `activeCount` / `activeTotal`.

---

## 8. API

Empleados (existente):

* `GET/POST /api/v1/finance/payroll/employees`
* `PATCH .../activate` y `.../deactivate`

La respuesta de empleado ahora incluye `canSell` y `canDoProduction` derivados.

Descuentos:

* `POST /api/v1/finance/payroll/employees/{employeeId}/deductions`
* `GET /api/v1/finance/payroll/employees/{employeeId}/deductions`
* `GET .../deductions?status=ACTIVE`
* `PATCH .../deductions/{deductionId}/cancel`

Vendedores comerciales (lectura de elegibles, no catálogo):

* `GET /api/v1/sellers` — empleados activos `FIXED_PAYROLL`
* `POST /api/v1/sellers` eliminado

---

## 9. Autorización

Sin cambio de JWT ni roles.

* `ADMIN`: Finanzas, empleados, descuentos, Comercial, Producción
* `OPERATOR`: puede crear cotizaciones y consultar `GET /api/v1/sellers`; no administra `/api/v1/finance/**`

---

## 10. Arquitectura

```
Presentation → Application → Domain
                     ↑
               Infrastructure
```

Commercial y Production consumen Finance por puertos de aplicación. No importan entidades JPA de Finance.

---

## 11. Migración

SQL aditivo: `backend/src/main/resources/db/manual/SPR-038-increment-e-payroll-deductions.sql`

Crea `payroll_deductions` e índices. No inserta datos de negocio. No trunca. No remapea `sellers`. No resetea secuencias ni el volumen Docker.

`schema.sql` se actualizó para instalaciones nuevas. `ddl-auto` permanece `validate`.

---

## 12. Frontera V1 / V2 y diferido

V1 de este incremento:

* una identidad de empleado
* selector de vendedor sobre empleados fijos activos
* fundación de descuentos

Los catálogos de Administración y la identidad de material de Inventario quedaron en `SPR-038-Increment-F-Administration-Catalogs-Inventory-Identity.md`.

Diferido (Incremento G / posterior):

* comisión del 5% del vendedor
* reportes de comisión
* tarifas automáticas de mano de obra
* impuestos y seguridad social
* liquidación automática de nómina con descuentos
* cuotas e interés de préstamos
* vínculo empleado ↔ `AuthenticationUser`
* PDF, notificaciones, Intelligence, rediseño visual

---

## 13. Fuera de alcance

No se implementó:

* comisiones
* motor de nómina
* vínculo de autenticación
* auditoría
* PDFs / notificaciones / Intelligence
* rediseño visual, animaciones o responsive
* SPR-039
* reset de base de datos
* datos reales de Magyen
