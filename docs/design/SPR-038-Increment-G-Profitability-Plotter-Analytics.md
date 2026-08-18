# SPR-038 — Incremento G: Rentabilidad individual y analítica de Plotter

**Incremento:** G  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento G. No inicia el Incremento H ni SPR-039.

---

## 1. Propósito

Hacer auditable y comprensible la rentabilidad en dos lecturas independientes:

1. Pedido comercial individual.
2. Plotter como vista analítica propia.

Home conserva el resumen agregado. La acción pasa de «Ver órdenes» a **«Ver rentabilidad individual»** y abre el listado de rentabilidad, no el listado operativo de pedidos.

Esta lectura no crea transacciones de Finanzas ni movimientos de Inventario.

---

## 2. Fórmula canónica de pedido

Una sola fuente de verdad: `GetOrderProfitabilityUseCase`.

El listado (`GetOrderProfitabilityListUseCase`) y el resumen de Home (`OrderProfitabilityAggregator`) reutilizan ese resultado. No hay una segunda fórmula en Presentation ni en el frontend.

```
totalProductionCost =
    materialCost
    + laborCost
    + internalPlotterMaterialCost

directProfit =
    orderValue
    - totalProductionCost

marginPercentage =
    directProfit / orderValue * 100
```

Si `orderValue <= 0`, el margen es `null`. Nunca Infinity ni NaN.

`orderValue` es `Order.getTotal()` (valor comprometido). El saldo por cobrar no es un costo de producción.

---

## 3. Fuentes de costo

| Componente | Fuente | Qué no se usa |
|---|---|---|
| Materiales | Snapshot histórico de Inventory OUT (`sourceType=PRODUCTION`) atribuido a la orden de producción | `InventoryItem.unitCost` actual después del consumo |
| Mano de obra | `ProductionLaborWork.calculatedAmount` congelado (PENDING + PAID) | Tarifa actual del empleado; labor CANCELLED |
| Papel / Plotter interno | Snapshot histórico de Inventory OUT (`sourceType=PLOTTER`) de trabajos `INTERNAL_MAGYEN` con `orderId` | Valor/venta del trabajo Plotter; EXPENSE de la compra de papel |

Valorización V1 de inventario: último costo configurado / de compra **en el momento del consumo**. Sin FIFO. Sin WAC. Sin lotes.

La compra de material sigue siendo Finance EXPENSE. El consumo de producción o Plotter **no** crea un segundo EXPENSE.

---

## 4. Estados COMPLETE / PARTIALLY_UNVALUED / NO_COST_DATA

Terminología existente, sin inventar ceros:

* `NO_COST_DATA` — no hay actividad de producción ni Plotter interno.
* `PARTIALLY_UNVALUED` — hay consumos o trabajos internos sin snapshot valorizado. Ese hueco **no** se trata como $0.
* `COMPLETE` — costos directos disponibles sin huecos de valorización.

Home y el listado individual **solo agregan montos de pedidos COMPLETE**. Un pedido sin costos no infla el margen ponderado ni se muestra como «100 % de rentabilidad».

UI (español):

* Completa → «Rentabilidad completa»
* Parcial → «Rentabilidad parcial — faltan costos por valorar»
* Sin datos → «Sin datos de costo»

---

## 5. Elegibilidad y margen ponderado

Elegibles (sin cambio respecto a Home):

* `CONFIRMED`
* `IN_PRODUCTION`
* `READY_FOR_DELIVERY`
* `DELIVERED`

`CLOSED` queda fuera.

Margen ponderado (no promedio de porcentajes):

```
weightedMargin = totalDirectProfit / totalOrderValue * 100
```

Solo sobre pedidos `COMPLETE`. Si el valor total COMPLETE es 0, el margen ponderado es `null`.

Home expone ese valor en el campo histórico `averageMarginPercentage` (nombre conservado; el cálculo es ponderado).

---

## 6. Plotter interno vs externo

| | Externo | Interno Magyen |
|---|---|---|
| `jobType` | `EXTERNAL` | `INTERNAL_MAGYEN` |
| Ingreso Finance | Sí, al registrar el pago (un INCOME) | No |
| Costo de papel | Snapshot OUT; entra a la analítica de Plotter | Snapshot OUT; entra a la rentabilidad del pedido y a la analítica interna de Plotter |
| Venta a Magyen | No aplica | Magyen no se vende papel a sí misma |

Relación Incremento D: el pedido se descubre por `orderId` del trabajo interno.

```
Commercial application
    PlotterOrderCostPort
        ↓
Plotter adapter
    GetInternalPlotterOrderCostsUseCase
```

Comercial no accede a tablas JPA de Plotter, Inventario ni Finanzas.

---

## 7. Métricas analíticas de Plotter

Fuente: `GetPlotterProfitabilityUseCase`. Vista de solo lectura.

| Métrica | Definición V1 |
|---|---|
| Total de papel impreso | Suma de metros de los trabajos del período/alcance |
| Total generado | Suma de `totalAmount` de trabajos **EXTERNAL** (ingreso externo). El interno no se mezcla aquí |
| Total gastado en papel | Suma de snapshots históricos OUT de papel (externo + interno valorizados) |
| Total gastado en tintas | **No registrado** en V1. Plotter no consume tinta en Inventario. `inkCostRecorded=false`, `inkCost=null`. No se fabrica un 0 medido |
| Resultado del Plotter | `externalRevenue - externalPaperCost`. `null` si falta valorizar papel externo, o si el alcance es solo internos |

Filtros: `fromDate`, `toDate`, `scope=ALL|EXTERNAL|INTERNAL` (también `TODOS|EXTERNOS|INTERNOS`). Período por defecto: mes calendario actual, igual que Home.

Trazabilidad interna (sin UUID como etiqueta): número de pedido, cliente, descripción, fecha, metros, costo histórico del papel.

---

## 8. Integridad Finance

Este incremento **no** introduce transacciones nuevas.

Reglas que se conservan:

* Venta/pago Plotter externo → un Finance INCOME.
* Compra de papel → Inventory ↑ y Finance EXPENSE.
* Consumo de papel Plotter → un Inventory OUT + atribución de costo. Sin segundo EXPENSE.
* Trabajo interno Magyen → sin INCOME y sin segundo EXPENSE.
* Rentabilidad → cálculo analítico / read model.

La consulta de rentabilidad no duplica papel, no duplica OUT y no duplica asientos.

«Dinero por cobrar de pedidos completados» en Home permanece independiente. El saldo pendiente no es costo de producción.

---

## 9. API

* `GET /api/v1/orders/profitability` — listado individual + resumen ponderado (misma regla que Home). Ruta literal **antes** de `/{orderId}`.
* `GET /api/v1/orders/{orderId}/profitability` — detalle. Campos de identidad: `orderNumber`, `description`, `customerName`, `promisedDeliveryDate`.
* `GET /api/v1/plotter/profitability?fromDate=&toDate=&scope=` — analítica de Plotter.

ADMIN y OPERATOR pueden leer, según la matriz V1. Administración sigue siendo solo ADMIN. Sin cambio de JWT.

---

## 10. Persistencia

Sin cambio de esquema. El modelo existente cubre pedidos, snapshots OUT, labor congelada y `plotter_jobs.job_type` + `order_id`.

Sin reset, DROP, TRUNCATE ni datos reales de Magyen.

Hibernate `ddl-auto=validate`.

---

## 11. Limitaciones V1

* Sin comisiones.
* Sin tarifas automáticas de mano de obra.
* Sin liquidación de nómina.
* Sin FIFO / WAC / lotes.
* Sin consumo de tinta de Plotter.
* Sin PDF, notificaciones ni Intelligence.
* El campo Home `averageMarginPercentage` conserva el nombre; el valor es margen ponderado.
* Un trabajo leftover `EXTERNAL` con `order_id` histórico no se reinterpreta como interno.

---

## 12. Fuera de alcance (Incremento H / SPR-039+)

* Incremento H
* SPR-039 y siguientes
* Rediseño visual
* Cualquier reset de base de datos
* Datos reales de negocio Magyen
