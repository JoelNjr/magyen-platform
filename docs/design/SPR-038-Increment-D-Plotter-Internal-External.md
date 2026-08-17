# SPR-038 — Incremento D: Plotter interno / externo y atribución de costo

**Incremento:** D (último de SPR-038)  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento D. No describe autenticación, catálogos comerciales, costos de inventario genéricos ni unificación de empleados. No inicia SPR-039.

---

## 1. Objetivo

Resolver la inconsistencia real descubierta en el primer flujo de aceptación Magyen:

el papel usado para una orden de producción puede registrarse como consumo de Inventario, pero la misma operación física no quedaba representada de forma automática y correcta como trabajo de Plotter. Eso invitaba a la familia a registrar dos veces el mismo papel.

Este incremento establece **una sola operación física de Plotter**.

An internal Plotter operation is a production material operation, not a second purchase or sale.

Paper consumption for an internal Magyen order is recorded exactly once.

---

## 2. Dos modos de negocio

| Modo | Valor interno | Qué es | Orden comercial | Pago de cliente |
|---|---|---|---|---|
| Producción Magyen | `INTERNAL_MAGYEN` | Operación de material de producción | Obligatoria | No. No es una venta a Magyen |
| Cliente externo | `EXTERNAL` | Servicio de impresión | Prohibida | Sí. INCOME existente |

La familia elige el tipo **antes** de llenar el resto del formulario. Los campos irrelevantes no se muestran.

Ejemplo interno:

* Tipo: Producción Magyen
* Orden: `#1 — Camisetas de voleibol`
* Cliente: Sofia Vergara (solo lectura, tomado de la orden)

Ejemplo externo:

* Tipo: Cliente externo
* Cliente: selector de cliente existente
* Sin selector de orden comercial

No se crean órdenes comerciales falsas para clientes de Plotter.

---

## 3. Regla de consumo único

```text
Trabajo interno de Plotter
        |
        +--> Inventory OUT (sourceType=PLOTTER, sourceId=plotterJobId)
        |
        +--> Costo histórico de material
        |
        +--> Atribución a la Orden comercial
        |
        +--> Historial de Plotter
```

Una operación física. Un OUT. Un costo atribuible.

No se pide:

1. Crear trabajo de Plotter
2. Registrar aparte el consumo de papel en Producción
3. Crear otro trabajo de Plotter

Production sigue registrando telas y materiales normales. El rollo de papel Plotter (`plotterPaperRoll`) se rechaza en el consumo genérico de Producción, en backend y en el selector de UI.

---

## 4. Propiedad por módulo

| Módulo | Dueño de |
|---|---|
| Plotter | `PlotterJob`, modo interno/externo, pagos de cliente externo |
| Inventory | stock físico, `InventoryMovement`, snapshot histórico |
| Production | consumo de tela y demás materiales, mano de obra |
| Finance | ledger de caja (EXPENSE de compra, INCOME de pago externo) |
| Commercial | Órdenes y Clientes |

Puertos de aplicación:

* Plotter → Commercial: validar orden y enriquecer número/cliente
* Plotter → Inventory: validar rollo, consumir metros, leer snapshot
* Commercial → Plotter: leer costo interno atribuible a la orden

No hay relaciones JPA cruzadas. `orderId` y `customerId` permanecen UUID suaves.

---

## 5. Semántica Finance

**Compra de papel**

* Inventory IN
* Finance EXPENSE

**Consumo de papel (interno o externo)**

* Inventory OUT
* Costo de producción / operación
* **No** crea Finance EXPENSE

**Pago de cliente externo**

* PlotterPayment
* Finance INCOME (`PLOTTER_REVENUE`)

**Trabajo interno**

* No crea INCOME
* No crea un pago de Magyen a Magyen
* El impacto de caja del papel ya ocurrió en la compra

Por lo tanto:

```text
Valor de la orden
− costo de tela/material de producción
− costo de mano de obra
− costo de papel de Plotter interno
= resultado directo
```

El EXPENSE original de la compra no se resta otra vez.

---

## 6. Costo histórico y rentabilidad

El costo interno es:

`metros impresos × unitCost snapshot del OUT de Inventory`

No se introduce FIFO ni WAC. Se preserva el snapshot inmutable del movimiento.

`GET /api/v1/orders/{orderId}/profitability` incluye `plotterMaterialCost` solo para trabajos `INTERNAL_MAGYEN` de esa orden.

Estados existentes:

* `NO_COST_DATA` — sin actividad de producción ni Plotter interno
* `PARTIALLY_UNVALUED` — hay consumos o trabajos internos sin snapshot valorizado
* `COMPLETE` — costos directos disponibles sin huecos de valorización

Un trabajo interno sin snapshot **no** se trata como $0: incrementa `unvaluedJobCount` y el estado pasa a `PARTIALLY_UNVALUED`.

Un trabajo `EXTERNAL` no entra a la rentabilidad de una Orden comercial.

---

## 7. Identidad visible

La UI no usa UUID como etiqueta de negocio.

Interno:

* Orden `#n — descripción`
* Cliente por nombre

Externo:

* Cliente por nombre
* Sin orden

Los UUID técnicos permanecen en persistencia y en campos secundarios del API.

---

## 8. Idempotencia

El cliente puede enviar `plotterJobId` estable al crear.

El mismo identificador:

* no consume papel dos veces
* no crea un segundo PlotterJob
* no crea un segundo costo de producción
* no crea un segundo INCOME

`InventoryMovement.sourceId = plotterJobId` con `sourceType = PLOTTER`. El índice único existente de Inventory es la salvaguarda física.

Stock insuficiente: HTTP 400, sin PlotterJob parcial, sin OUT, sin transacción Finance.

---

## 9. Persistencia

Columna aditiva `plotter_jobs.job_type` (`INTERNAL_MAGYEN` | `EXTERNAL`), `NOT NULL`.

Migración manual: `backend/src/main/resources/db/manual/SPR-038-increment-d-plotter-job-type.sql`.

Las filas existentes se clasifican `EXTERNAL` para no alterar semántica de pago. Un `order_id` leftover en una fila `EXTERNAL` no se remapea a interno y no entra a rentabilidad.

No se resetea, trunca ni borra la base. Hibernate `ddl-auto=validate`.

---

## 10. Autorización

Sin permisos nuevos. Sin cambios JWT.

* ADMIN: administración completa de Plotter
* OPERATOR: trabajo operacional según la matriz V1 vigente

---

## 11. Home

Home no agrega una sección nueva. La rentabilidad del dashboard ya consume `GetOrderProfitabilityUseCase`, que ahora incluye papel de Plotter interno. El subtítulo operacional aclara esa inclusión.

---

## 12. Limitaciones V1

* No hay tarifas automáticas de Plotter
* No hay FIFO/WAC
* No hay comisiones
* No hay PDF ni notificaciones
* No hay recomendaciones Intelligence
* Un trabajo leftover `EXTERNAL` con `order_id` histórico no se reinterpreta
* El papel genérico que no es rollo Plotter sigue pudiendo consumirse en Producción

---

## 13. Fuera de alcance (SPR-039+)

* SPR-039 y siguientes
* Comisiones
* Motor de inventario avanzado
* Rediseño visual o animaciones
* Cualquier reset de base de datos
