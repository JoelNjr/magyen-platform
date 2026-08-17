# SPR-035 — Inventario, Consumo de Producción y Plotter

## 1. Objetivo

Construir la base funcional del módulo de Inventario de Magyen Platform y sus primeras integraciones operativas con Production y Plotter.

El módulo debe permitir conocer qué materiales existen, cuánto hay disponible, qué movimientos han ocurrido, cuándo un material se encuentra por debajo de su nivel mínimo, y cómo Production/Plotter consumen material físico de forma auditable.

SPR-035 establece una base sólida para Finance (SPR-036) y Home/Dashboard, sin implementar esos módulos todavía.

---

## 2. Problema de negocio

Magyen utiliza diferentes materiales e insumos para fabricar productos y prestar servicios:

- telas
- papel para sublimación (rollos Plotter)
- tintas
- hilos
- DTF
- insumos de confección
- empaques
- otros materiales de producción

La empresa necesita saber:

- qué material tiene disponible
- qué cantidad existe y en qué unidad
- qué costo unitario configurado tiene
- qué cantidad mínima debería mantenerse
- cuándo ingresó/salió material
- por qué ocurrió un movimiento
- qué consumo real hizo Production
- qué metros de papel consumió Plotter
- cuánto se cobró al cliente por un trabajo de Plotter

---

## 3. Alcance final del sprint

SPR-035 cubre:

### Inventario

- catálogo de materiales (`InventoryItem`)
- unidad de medida
- stock actual
- stock mínimo / `lowStock`
- costo unitario configurado (`unitCost`, nullable)
- movimientos históricos (`InventoryMovement`)
- costos históricos inmutables en movimientos (`unitCost` / `totalCost`)
- origen auditable (`sourceType` / `sourceId`)
- clasificación tipada (`materialType`)
- rollos de papel Plotter (`paperRollNumber` = `RP-###`)

### Production

- consumo real de material (`ProductionMaterialConsumption`)
- permitido solo en `IN_PROGRESS`
- integración con Inventory vía puerto de aplicación
- OUT con `sourceType=PRODUCTION` y `sourceId=productionMaterialConsumptionId`
- idempotencia por origen

### Plotter

- registro operacional de trabajos (`PlotterJob`)
- selección exclusiva de rollos de papel válidos
- numeración automática `RP-###`
- consumo exacto de metros impresos
- ingreso/revenue = `printedMeters × pricePerMeter`
- OUT con `sourceType=PLOTTER` y `sourceId=plotterJobId`
- costo de material histórico en Inventory (no en Plotter)

---

## 4. Arquitectura

Mantener Clean Architecture / modular monolith:

```text
Presentation
↓
Application
↓
Domain
↑
Infrastructure
```

Principios de ownership:

| Módulo | Dueño de |
|--------|----------|
| Inventory | existencia física, movimientos, valoración histórica de materiales |
| Production | ejecución productiva y hecho de consumo |
| Plotter | operación de impresión e ingreso cobrado al cliente |
| Finance | ingresos/gastos/resultados (futuro, fuera de SPR-035) |
| Home | agregación/alertas (futuro, fuera de SPR-035) |

Reglas de dependencia:

- Production/Plotter no importan entidades JPA ni repositorios Spring Data de Inventory.
- Inventory no depende de objetos de dominio Production/Plotter.
- Las referencias cruzadas son soft references (UUID), sin FK entre módulos.

---

## 5. Inventory — modelo final

### InventoryItem

Campos relevantes:

- `id` (UUID)
- `materialCode`
- `name`
- `category` (texto libre legado)
- `materialType` (`FABRIC | PAPER | INK | THREAD | DTF | OTHER`)
- `paperRollNumber` (solo rollos Plotter; formato `RP-###`)
- `unitOfMeasure`
- `stock`
- `minimumStock` (nullable; `null` desactiva monitoreo)
- `unitCost` (nullable; escala 2)
- `status` (`ACTIVE | INACTIVE`)
- `description`

### InventoryMovement

Campos relevantes:

- `movementType` (`IN | OUT | ADJUSTMENT`)
- `quantity`
- `unitOfMeasure`
- `movementDate`
- `observation`
- `resultingStock`
- `unitCost` / `totalCost` (snapshot histórico)
- `sourceType` (`MANUAL | PRODUCTION | PLOTTER`)
- `sourceId` (requerido para PRODUCTION/PLOTTER)

### Reglas de stock

- el stock solo cambia por movimientos válidos
- OUT no puede dejar stock negativo
- fallos de movimiento no alteran stock
- actualizar `unitCost` o `minimumStock` no crea movimientos

### Costos

- `unitCost` actual es configuración mutable
- el movimiento congela `unitCost` y `totalCost = quantity × unitCost`
- si `unitCost` es `null`, el OUT físico puede ocurrir sin valoración
- costos históricos son inmutables

### Low stock

- `lowStock = true` cuando `minimumStock != null` y `stock <= minimumStock`
- `minimumStock = 0` es válido
- `minimumStock = null` desactiva monitoreo
- Home/Dashboard NO se implementa en SPR-035

---

## 6. Rollos de papel Plotter

Un rollo Plotter válido requiere:

- `materialType = PAPER`
- `unitOfMeasure = METER`
- `paperRollNumber` no nulo (`RP-###`)

Numeración:

- generada por secuencia PostgreSQL `paper_roll_number_seq`
- formato `RP-%03d`
- única
- **puede tener huecos** (aceptable y preferible a reutilizar números)
- el frontend no captura el número manualmente

Para un reset limpio de base de datos V1:

> El proceso de BD debe recrear/reiniciar también `paper_roll_number_seq` para que el primer rollo productivo comience en `RP-001`.

---

## 7. Production — consumo de material

### Hecho de consumo

`ProductionMaterialConsumption` pertenece a ProductionOrder y registra:

- `inventoryItemId` (soft ref)
- `quantity`
- `unitOfMeasure`
- `consumptionDate`
- `observation`

### Ciclo de vida

| Estado ProductionOrder | Consumo |
|------------------------|---------|
| CREATED | rechazado |
| PLANNED | rechazado |
| IN_PROGRESS | permitido |
| COMPLETED | rechazado |

### Integración Inventory

```text
RegisterProductionMaterialConsumption
        ↓
ProductionMaterialConsumption (domain)
        ↓
ProductionMaterialConsumptionInventoryPort
        ↓
ConsumeInventoryMaterialUseCase
        ↓
InventoryMovement OUT
  sourceType = PRODUCTION
  sourceId   = productionMaterialConsumptionId
```

### Principio crítico

**NO** implementar:

```text
ProductionOrder created → consumo automático de inventario
```

El consumo representa un evento productivo real.

### Idempotencia

- aplicación: lookup por `(PRODUCTION, consumptionId)`
- base de datos: índice único parcial `uq_inventory_movements_source`
- reintentos no descuentan dos veces

### Atomicidad

Production + Inventory comparten la misma transacción local PostgreSQL/Spring.
Si Inventory falla, el consumo de Production no queda persistido.

---

## 8. Plotter — trabajos e integración Inventory

### PlotterJob

Campos:

- `customerId` (soft ref)
- `jobType` (`INTERNAL_MAGYEN` | `EXTERNAL`)
- `orderId` (soft ref; obligatorio solo en `INTERNAL_MAGYEN`)
- `creationDate` (servidor o enviada)
- `paperInventoryItemId` (soft ref a rollo)
- `printedMeters`
- `pricePerMeter` (cobro al cliente externo; `0` en interno)
- `totalAmount` (calculado: metros × precio; interno no es una venta)
- `status` (`REGISTERED | IN_PROGRESS | COMPLETED | CANCELLED`)
- `observations`

Un trabajo interno es una operación de material de producción, no una segunda compra ni una venta. El consumo de papel de una orden Magyen se registra exactamente una vez (`sourceId = plotterJobId`). Ver `SPR-038-Increment-D-Plotter-Internal-External.md`.

### Ingreso vs costo

| Concepto | Dónde vive |
|----------|------------|
| Revenue cobrado al cliente | `PlotterJob.totalAmount` |
| Costo material histórico | `InventoryMovement.unitCost/totalCost` |

Plotter **no** calcula margen/utilidad.

### Integración Inventory

```text
CreatePlotterJob
        ↓
validar rollo PAPER/METER/RP
        ↓
validar stock >= printedMeters
        ↓
persistir PlotterJob
        ↓
PlotterJobInventoryPort.consumePaperMeters
        ↓
InventoryMovement OUT
  sourceType = PLOTTER
  sourceId   = plotterJobId
```

### Principio crítico

```text
PlotterJob creado → consume exactamente printedMeters del rollo seleccionado una sola vez
```

No hay consumo genérico arbitrario.

Trabajos históricos creados antes de la integración (materiales no-rollo) permanecen legibles como legado.

---

## 9. Persistencia

Estructuras finales relevantes en `schema.sql`:

### inventory_items

- columnas de stock/costo
- `material_type`
- `paper_roll_number` (unique)
- secuencia `paper_roll_number_seq`

### inventory_movements

- costos históricos
- `source_type` / `source_id`
- índice único parcial `uq_inventory_movements_source`

### production_material_consumptions

- soft ref a inventory item
- FK solo hacia `production_orders`

### plotter_jobs

- soft refs a customer e inventory item
- sin FK cross-module

---

## 10. Frontend

### Inventory

- listado / detalle
- creación con tipo de material
- opción “Rollo de papel para Plotter”
- stock mínimo / costo unitario
- historial de movimientos con origen

### Plotter

- listado / detalle / creación
- selector solo de rollos elegibles
- preview de total cobrado
- sección de consumo de material (costo histórico desde Inventory)
- trabajos legado marcados como histórico cuando aplica

### Production

- consumos de material se operan vía API; UI de consumo puede evolucionar después

---

## 11. Fuera de alcance (explícito)

No implementado en SPR-035:

- Finance (SPR-036)
- Home/Dashboard
- PDF
- autenticación/autorización
- BOM / estimación automática
- consumo de tinta Plotter
- multi-material Plotter
- weighted-average costing
- purchasing/suppliers
- numeración gapless de rollos
- notificaciones avanzadas

---

## 12. Criterio de éxito (cierre)

SPR-035 se considera completo cuando:

1. Inventory registra materiales, movimientos, costos históricos y low-stock.
2. Production puede consumir material real solo en `IN_PROGRESS`.
3. Plotter selecciona rollos `RP-###` y consume metros exactos.
4. Cada consumo crea a lo sumo un OUT auditable (`PRODUCTION` / `PLOTTER`).
5. La idempotencia evita doble descuento.
6. Los datos históricos previos siguen legibles.
7. Backend tests y frontend lint/build pasan.
8. No se introduce Finance ni Home.

---

## 13. Estado de cierre

**Estado:** COMPLETE WITH DEFERRED ITEMS

Los ítems diferidos son intencionales (Finance, Home, tinta, BOM, etc.) y no bloquean la base operacional de Inventory/Production/Plotter.
