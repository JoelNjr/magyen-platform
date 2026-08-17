# SPR-038 — Incremento B: Costos de inventario y consumo de material

**Incremento:** B  
**Estado:** Implemented  
**Fecha:** 16 de agosto de 2026

Este documento cubre únicamente el Incremento B. No describe autenticación (`SPR-038-Auth-Security.md`) ni el Incremento A (catálogos comerciales).

---

## 1. Objetivo

Establecer la relación V1 entre compra de material, stock de Inventario, costo histórico de producción y rentabilidad, **sin doble conteo** en Finanzas.

Principio de negocio:

* Comprar material y consumir material son hechos financieros distintos.
* El desembolso de caja se reconoce **al comprar**.
* El costo de producción se atribuye **al consumir**, usando el snapshot histórico del movimiento de Inventario.
* El consumo **nunca** crea un segundo gasto en Finance.

---

## 2. Separación de conceptos

| Módulo | Pregunta que responde |
|---|---|
| Inventario | ¿Qué material físico tenemos? |
| Finanzas | ¿Cuánto efectivo salió para adquirirlo? |
| Producción | ¿Cuánto de ese material consumió esta orden? |
| Rentabilidad | ¿Cuánto costo directo incurrió el pedido? |

No se unifican en una sola entidad. No hay motor de costo promedio (WAC). La valoración actual del ítem es el **último costo de compra**; el costo de producción usa el snapshot del movimiento OUT, no el `unitCost` vigente.

---

## 3. Identidad de material vs catálogo comercial

### Catálogo comercial (cotización)

`CommercialFabric` permanece independiente del stock:

* Sudáfrica
* Piqué
* Hydrotech

Una cotización puede seleccionar una tela del catálogo aunque Inventario tenga 0 metros. No se hace matching por nombre entre catálogo y stock.

El catálogo se extiende agregando un valor al enum `CommercialFabric`. No se inventaron telas adicionales.

### Identidad de inventario (consumo)

`InventoryItem` ya tiene identidad estable:

* `id` (UUID)
* `materialCode` (código de negocio único)

El consumo de producción **exige** `inventoryItemId`. No existe campo de texto libre que cree o empareje inventario.

Mapeo V1: **ninguno automático**. Cotización elige catálogo; producción elige ítem de inventario. Es el mapeo más pequeño y coherente: no se introduce un maestro ERP.

---

## 4. Compra / recepción

`POST /api/v1/inventory/{inventoryItemId}/purchases`

Campos: material (path), cantidad, costo unitario, fecha, observaciones. `purchaseId` opcional para reintentos.

En **una sola transacción**:

1. Inventario IN (`sourceType = PURCHASE`, `sourceId = purchaseId`).
2. Snapshot de `unitCost` / `totalCost` en el movimiento (`cantidad × costo unitario` en servidor).
3. Exactamente un Finance `EXPENSE`.

El frontend no calcula el total ni crea el gasto.

Validación: cantidad > 0, costo unitario > 0, material existente, fecha presente. No se permite stock negativo.

La UI: **Registrar entrada de material** (listado y detalle de Inventario). No hay pantalla de compra en Finanzas.

---

## 5. Gasto de Finance (caja, no producción)

| Campo | Valor |
|---|---|
| Tipo | `EXPENSE` |
| `sourceType` | `INVENTORY_PURCHASE` |
| `sourceId` | `purchaseId` (nunca el `inventoryItemId`: el mismo material se compra varias veces) |
| Categoría | ver sección 6 |
| Descripción | `Compra de inventario - {nombre} ({código})` |

El gasto representa **desembolso de caja por adquirir inventario**. No es un gasto de producción.

Índice único aditivo:

`uq_financial_transactions_inventory_purchase_source` sobre `(source_type, source_id)` donde `source_type = 'INVENTORY_PURCHASE'`.

---

## 6. Categoría de gasto elegida

Se reutilizan categorías existentes. No se creó «Production Material Expense».

| `InventoryMaterialType` | Categoría Finance |
|---|---|
| `FABRIC`, `THREAD`, `OTHER` | `MATERIALS` |
| `PAPER` | `PAPER` |
| `INK` | `INK` |
| `DTF` | `DTF` |

Para tela (el caso Magyen típico) la categoría es **MATERIALS**: gasto de adquisición de material/inventario.

---

## 7. Idempotencia

* Identidad de compra: `purchaseId` (UUID). El cliente lo genera al abrir el diálogo y lo reenvía en reintentos.
* Inventario: lookup `(PURCHASE, purchaseId)` + índice único `uq_inventory_movements_source`.
* Finance: lookup `(INVENTORY_PURCHASE, purchaseId)` + índice único nuevo.
* Si hay carrera, `DataIntegrityViolationException` se resuelve releyendo el registro existente.
* Reintento: HTTP 200 + `alreadyProcessed=true`; no duplica stock ni gasto.

---

## 8. Consumo de producción

Sin cambio de fórmula de atribución (SPR-036):

* OUT de Inventario con `sourceType = PRODUCTION`, `sourceId = consumptionId`.
* Congela `unitCost` / `totalCost` del momento del OUT.
* Disminuye stock.
* **No** llama al puerto de Finance.
* Falla atómicamente si no hay stock suficiente (400 / `InventoryDomainException`). Sin movimiento parcial, sin gasto, sin consumo de producción persistido.

La UI muestra un selector de ítems reales, por ejemplo:

`Sudáfrica — 25,00 m disponibles — $10.000 / m`

El backend es la autoridad del costo y del stock.

---

## 9. Rentabilidad

Sin rediseño.

```
Costo directo = costo material histórico + mano de obra
Utilidad directa = valor de orden − costo directo
```

El `EXPENSE` de la compra **no** entra en esa fórmula. Ejemplo:

* Orden = COP 400.000
* Consumo 6,5 m × COP 10.000 = COP 65.000
* Mano de obra = COP 30.000
* Costo directo = COP 95.000
* Utilidad directa = COP 305.000
* Finance sigue mostrando el gasto de la compra de 100 m (COP 1.000.000) como evento de caja aparte.

Así se evita el doble conteo: el efectivo ya salió al comprar; la orden solo carga lo que realmente consumió.

---

## 10. Arquitectura

```
Presentation (InventoryController)
    ↓
RegisterInventoryPurchaseUseCase
    ↓
InventoryItem.registerPurchase  →  InventoryMovement IN
    ↓
InventoryPurchaseFinancePort  (application)
    ↓
InventoryPurchaseFinanceAdapter  (infrastructure)
    ↓
EnsureInventoryPurchaseExpenseUseCase  (Finance application)
```

Producción sigue: puerto de aplicación → adaptador Inventory → `ConsumeInventoryMaterialUseCase`.

No hay relaciones JPA entre módulos.

---

## 11. Esquema

Único cambio aditivo (`backend/src/main/resources/db/manual/SPR-038-increment-b-inventory-purchase.sql`):

```sql
CREATE UNIQUE INDEX IF NOT EXISTS uq_financial_transactions_inventory_purchase_source
    ON financial_transactions (source_type, source_id)
    WHERE source_type = 'INVENTORY_PURCHASE'
      AND source_id IS NOT NULL;
```

`schema.sql` incluye el mismo índice. No se recrean volúmenes, no se truncan tablas, no se borra el ADMIN bootstrap.

Tras aplicar el SQL, refrescar en DBeaver el nodo de índices de `financial_transactions`.

---

## 12. Fuera de alcance (Incremento C+)

* Unificación de empleados / nómina y tarifas automáticas
* Comisiones
* Trabajos internos de Plotter
* PDF, notificaciones, Intelligence
* Rediseño de Home o animaciones
* Motor WAC / costo promedio
* Segundo catálogo o segundo inventario
* Telas Magyen adicionales
