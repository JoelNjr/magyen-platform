📘 SPR-036 — FINANZAS MAGYEN PLATFORM
1. Objetivo del Sprint

Construir el módulo financiero de Magyen Platform para permitir:

Registrar ingresos.
Registrar gastos.
Organizar gastos por categorías.
Administrar gastos fijos/recurrentes.
Registrar créditos y obligaciones con fechas de vencimiento.
Gestionar servicios y recibos pendientes de pago.
Gestionar pagos de nómina.
Diferenciar nómina fija/quincenal de pagos por producción.
Registrar ingresos y gastos asociados al Plotter.
Preparar la plataforma para calcular utilidad, costos y márgenes.
Mantener trazabilidad entre movimientos financieros y módulos origen.
Objetivo principal

No construir simplemente un CRUD de "ingresos y gastos".

El objetivo es construir un Ledger financiero de Magyen, sobre el cual posteriormente puedan construirse:

Ingresos
    ↓
Gastos
    ↓
Costos
    ↓
Obligaciones
    ↓
Nómina
    ↓
Plotter
    ↓
Rentabilidad
    ↓
Dashboard financiero
2. Principios arquitectónicos

SPR-036 debe mantener los principios utilizados en SPR-033/034/035.

Clean Architecture
Presentation
     ↓
Application
     ↓
Domain
     ↑
Infrastructure
Reglas
Finance no debe acceder directamente a JPA de Commercial.
Finance no debe acceder directamente a JPA de Inventory.
Finance no debe acceder directamente a JPA de Production.
Finance no debe acceder directamente a JPA de Plotter.
Cuando sea necesario relacionar información externa, utilizar:
application ports,
adapters,
referencias UUID,
snapshots cuando corresponda.
3. Modelo financiero general

La base del módulo será un movimiento financiero.

Conceptualmente:

FinancialTransaction
│
├── id
├── type
├── amount
├── date
├── category
├── description
├── sourceType
├── sourceId
└── observation

Tipos principales:

INCOME
EXPENSE

Ejemplos:

INCOME
  Venta de uniforme
  Trabajo de Plotter
  Venta de papel
  Otro ingreso

EXPENSE
  Compra de tela
  Compra de papel
  Tinta
  Servicio
  Nómina
  Crédito
  Transporte
  Otro gasto

## Cost Attribution vs Cash Ledger

Magyen separa dos conceptos contables que no deben mezclarse:

### Movimiento de caja (Cash ledger)

`FinancialTransaction` representa dinero que entra o sale de Magyen.

Ejemplos:

- Pago de cliente (Commercial Payment) → Finance INCOME
- Pago de Plotter (PlotterPayment) → Finance INCOME
- Pago de obligación recurrente → Finance EXPENSE
- Compra/pago a proveedor (futuro) → Finance EXPENSE

### Atribución de costo (Cost attribution)

El consumo de material en Production (y el consumo de papel en Plotter) es un evento de **atribución de costo**, no un gasto de caja automático.

Flujo correcto:

```
Inventory purchase/payment:
→ Finance EXPENSE (cuando exista el pago real de compra)

Production consumption:
→ Inventory OUT + Production COST ATTRIBUTION
(NO FinancialTransaction EXPENSE)

Customer payment:
→ Finance INCOME
```

Reglas:

- El consumo de inventario reduce la valoración de inventario.
- Production recibe el **costo histórico** del snapshot del movimiento OUT (`InventoryMovement.unitCost` / `totalCost`) para análisis de rentabilidad futura.
- `Finance.FinancialTransaction` representa movimiento real de caja.
- Production consumption **no** crea automáticamente Finance EXPENSE.
- Las futuras compras/pagos a proveedores crearán el gasto real de caja.
- Esto evita el doble conteo: comprar $500.000 y consumir $200.000 no debe registrar $700.000 de gastos.

Fuente de verdad del costo de material de producción:

- `InventoryMovement` con `sourceType = PRODUCTION` y `sourceId = productionMaterialConsumptionId`
- No se recalcula con el `InventoryItem.unitCost` actual (que puede cambiar después)

Consumos sin costo configurado:

- Pueden existir físicamente (`unitCost` / `totalCost` null)
- Se reportan como **sin valorizar**
- No se inventa $0

## Payroll

Magyen tiene dos modelos laborales distintos. No deben mezclarse.

### Empleados de nómina fija (`FIXED_PAYROLL`)

Ejemplos: diseñador José, miembros de familia con salario fijo quincenal.

Flujo:

```
PayrollEmployee (FIXED_PAYROLL)
  ↓ compensación fija + BIWEEKLY + effectiveFrom
Generate payroll periods (controlado, sin scheduler)
  ↓ períodos PENDING con amount snapshot
Pay payroll period
  ↓
FinancialTransaction EXPENSE
  sourceType = PAYROLL
  sourceId = payrollPeriodId
  category = PAYROLL
```

### Operadores por producción (`PRODUCTION_BASED`)

Existen en el modelo de empleados, pero:

- No entran en generación de nómina fija.
- No generan períodos biweekly automáticos.
- Su compensación operativa se registra como `ProductionLaborWork` (propiedad de Production).

## Profitability

Fundamento de **rentabilidad directa** de una Orden comercial (solo lectura).

Pregunta que responde:

> ¿Cuánto dinero deja este pedido considerando sus ingresos y sus costos directos conocidos?

### Valor vs caja

| Concepto | Fuente | Uso en rentabilidad |
|----------|--------|---------------------|
| Valor del pedido (`orderValue`) | `Order.total` / `totalAmount` | Base del resultado y margen |
| Dinero recibido (`collectedAmount`) | Suma de `Payment` Finance por `orderId` | Informativo de cobranza |
| Pendiente (`outstandingAmount`) | `orderValue − collected` | Informativo |

El resultado directo **no** usa el dinero cobrado como ingreso:

`directProfit = orderValue − totalDirectCost`

### Costos directos

- **Materiales:** atribución histórica Inventory (`InventoryMovement` PRODUCTION OUT). No usa `InventoryItem.unitCost` actual. Consumos sin valorizar se reportan (`unvaluedMaterialConsumptionCount`) → status `PARTIALLY_UNVALUED`.
- **Mano de obra:** suma de `ProductionLaborWork.calculatedAmount` en PENDING+PAID. CANCELLED excluido. Labor impaga **sí** cuenta (costo incurrido, no caja pagada).
- **Plotter:** diferido. `PlotterJob` no tiene `orderId` confiable; no se infiere por `customerId`. `plotterMaterialCost = 0`, `plotterCostAttributable = false`.

### Estados

- `NO_COST_DATA` — sin ProductionOrder o sin material/labor registrados
- `PARTIALLY_UNVALUED` — hay consumos de material sin costo histórico
- `COMPLETE` — costos directos disponibles sin material sin valorizar

### Qué NO es

No es ganancia neta, utilidad contable, EBITDA, flujo de caja, contribución tras overhead, ni cálculo fiscal. No escribe en el ledger Finance.

Endpoint: `GET /api/v1/orders/{orderId}/profitability`.

## Production-Based Labor

Mano de obra por producción es distinta de la nómina fija.

- **Ownership:** Production posee `ProductionLaborWork` como hijo del agregado `ProductionOrder` (cascade/orphanRemoval, igual que consumos de material).
- **Operario:** `operatorEmployeeId` es UUID suave (sin FK a `payroll_employees`). Solo empleados activos `PRODUCTION_BASED` pueden recibir labor; `FIXED_PAYROLL` se rechaza.
- **Ciclo de orden:** solo se registra labor cuando `ProductionOrder.status == IN_PROGRESS`. Crear/completar la orden NO crea ni paga labor.
- **Cálculo:** `calculatedAmount = quantity × unitRate` (escala 2, HALF_UP), autoritativo en servidor. Snapshot: el monto histórico no se recalcula.
- **Estados:** `PENDING → PAID | CANCELLED` (terminales). Crear/cancelar NO crea Finance. Pagar crea exactamente un `FinancialTransaction` EXPENSE.
- **Finance:** `category = PAYROLL`, `sourceType = PAYROLL`, `sourceId = laborWorkId`. Reutiliza `uq_financial_transactions_payroll_source`. PAID→PAID → 409.
- **Atomicidad:** `@Transactional` en pay: crear expense → `markPaid` → save del agregado.
- **Costos:** `totalLaborCost` (PENDING+PAID; null si no hay labor no cancelada) + `totalMaterialCost` → `totalProductionCost`. Sin rentabilidad aún.

### Decisiones de arquitectura

- **No** se reutiliza `RecurringFinancialObligation` como agregado de nómina: falta identidad de empleado, exclusión PRODUCTION_BASED y `sourceType=PAYROLL`.
- Sí se reutilizan los patrones: snapshot, generación controlada e idempotente, gasto solo al pagar, `@Transactional`, 409 en duplicados.

### Snapshots

Al generar un período se congela `amountSnapshot` con la compensación fija vigente.

Cambiar José de $1.500.000 a $1.700.000:

- No muta períodos ya existentes (PENDING o PAID).
- Solo afecta períodos futuros generados después del cambio.

### Día hábil (business day)

Fecha prevista = ajuste del `periodEnd` (ancla):

- Lunes–viernes → sin cambio
- Sábado → viernes anterior
- Domingo → lunes siguiente

Festivos colombianos: diferidos (sin calendario aún).

### PENDING vs PAID

- Crear/generar período → compromiso; Finance expense = $0
- Pagar período → exactamente un EXPENSE; idempotente (`sourceType`+`sourceId` únicos)
- PAID → PAID rechazado (409)

### Idempotencia

- Unicidad de período: `(employee_id, period_start)`
- Unicidad de ledger: partial unique index `uq_financial_transactions_payroll_source`

4. Categorías financieras

Las categorías deben permitir posteriormente generar reportes.

Ejemplos de gastos:

Operativos
Materiales
Tela
Papel
Tintas
DTF
Bordado
Insumos
Transporte
Servicios
Energía
Agua
Internet
Telefonía
Otros servicios
Personal
Nómina
Pago por producción
Diseño
Otros servicios profesionales
Obligaciones
Crédito
Cuota de crédito
Intereses
Administración
Software
Publicidad
Mantenimiento
Otros

No queremos convertir esto todavía en una estructura rígida imposible de modificar.

5. Gastos fijos

Magyen debe poder registrar gastos que se repiten.

Ejemplo:

Internet
Valor: $120.000
Frecuencia: mensual
Día de vencimiento: 15
Activo: sí

Posteriormente el sistema podrá generar:

Agosto      → pendiente
Septiembre  → pendiente
Octubre     → pendiente

Importante: en este sprint inicial no debemos generar automáticamente todas las obligaciones futuras si todavía no es necesario.

Primero construimos correctamente el modelo.

6. Créditos y obligaciones

Finance debe contemplar:

Crédito
├── Nombre
├── Entidad
├── Valor inicial
├── Saldo
├── Número de cuotas
├── Valor cuota
├── Fecha próxima de pago
├── Fecha de inicio
├── Fecha final
└── Estado

Posteriormente:

Crédito
   ↓
Cuotas
   ↓
Pagos
   ↓
Movimientos financieros

Esto permitirá que Home posteriormente pueda decir:

🔴 Crédito vence en 3 días.

7. Servicios

Los recibos de servicios deben poder manejarse como obligaciones.

Ejemplo:

Energía
Vencimiento: 15/08/2026
Valor: $450.000
Estado: PENDIENTE

Cuando se pague:

PENDIENTE
    ↓
PAGADO

Y el pago debe poder generar/relacionarse con un movimiento financiero.

8. Nómina Magyen

Este punto queda explícitamente dentro de la arquitectura.

Personal fijo

Actualmente:

José — diseñador.
4 miembros de la familia — pago quincenal hábil.

El sistema debe permitir posteriormente:

Empleado
├── Nombre
├── Tipo de pago
├── Periodicidad
├── Valor
├── Día/criterio de pago
└── Estado
Operarios por producción

NO deben modelarse como nómina fija.

Su pago corresponde a producción:

Producción
   ↓
Trabajo realizado
   ↓
Cantidad / operación
   ↓
Valor a pagar

Esto será importantísimo para calcular correctamente los costos de cada pedido.

9. Finanzas del Plotter

El Plotter tendrá una lectura financiera propia.

Actualmente ya tenemos:

PlotterJob
 ├── printedMeters
 ├── pricePerMeter
 └── totalAmount

Y SPR-035 ya nos permite conocer:

PlotterJob
     ↓
Papel consumido
     ↓
InventoryMovement
     ↓
Costo histórico

Posteriormente Finance podrá calcular:

INGRESO
Trabajo Plotter
$100.000

COSTOS
Papel
$56.250

Tinta
$...

Otros
$...

────────────────
UTILIDAD
$...

No vamos a inventar costos de tinta todavía, porque todavía no tenemos consumo de tinta modelado.

10. Rentabilidad

La arquitectura debe prepararnos para posteriormente conocer:

Por pedido
Pedido
├── Ingreso
├── Materiales
├── Mano de obra
├── Otros costos
├── Costo total
├── Utilidad
└── Margen %
Por Plotter
Ingresos Plotter
- Papel
- Tinta
- Otros costos
= Utilidad Plotter
Global
Ingresos totales
- Gastos
- Costos
= Resultado financiero

Pero SPR-036 no debe intentar construir todo esto en el primer incremento.

11. Trazabilidad

Los movimientos financieros deben poder indicar su origen.

Ejemplo:

MANUAL
COMMERCIAL_ORDER
PLOTTER
PRODUCTION
PAYROLL
SERVICE
CREDIT

Pero inicialmente no debemos acoplar Finance a los módulos.

La relación puede ser:

sourceType = PLOTTER
sourceId   = <plotterJobId>

De esta manera:

Finance
   │
   └── sourceId
          ↓
      PlotterJob

sin que Finance tenga que importar la entidad de Plotter.

12. Reglas de dinero

Todo valor monetario deberá utilizar:

BigDecimal

Nunca:

double
float

Reglas:

Valores monetarios con escala 2.
No permitir gastos negativos.
No permitir ingresos negativos.
Cero debe rechazarse salvo que exista una razón explícita de dominio.
Redondeo monetario consistente HALF_UP.
13. Lo que NO construiremos todavía

Queda deliberadamente fuera de los primeros incrementos:

Dashboard financiero.
Gráficas.
Presupuestos.
Flujo de caja avanzado.
Contabilidad formal.
Impuestos.
DIAN.
Facturación electrónica.
Conciliación bancaria.
Bancos/cuentas bancarias.
Crédito automático.
Cálculo completo de margen por pedido.
Nómina automática.
Alertas Home.
PDF financiero.

Primero:

datos correctos → reglas correctas → trazabilidad → reportes → UX.

🧭 Plan propuesto SPR-036
SPR-036
│
├── Increment 1
│   Finance Ledger Foundation
│
├── Increment 2
│   Categorías + ingresos/gastos
│
├── Increment 3
│   Gastos fijos y obligaciones
│
├── Increment 4
│   Créditos + servicios + vencimientos
│
├── Increment 5
│   Nómina y pagos por producción
│
├── Increment 6
│   Integración financiera con Commercial / Plotter
│
├── Increment 7
│   Costos y rentabilidad
│
├── Increment 8
│   Frontend financiero
│
└── Increment 9
    E2E + Architecture Review + cierre

> Nota: la implementación evolucionó con incrementos adicionales (obligaciones,
> pagos Plotter, nómina fija, mano de obra por producción, rentabilidad, QA E2E).
> El cierre funcional se documenta en **SPR-036 Completion Review** (Increment 13).

---

## SPR-036 Completion Review

Fecha de revisión: 2026-08-10  
Incremento: 13 — Finance Completion & End-to-End Verification  
Recomendación: **APPROVE Increment 13**

### Finance ledger

- `FinancialTransaction` es el único registro de dinero real (INCOME / EXPENSE).
- Montos siempre positivos (`FinancialAmount`); la dirección la da `type`.
- Precisión monetaria `numeric(19,2)` / `BigDecimal` con escala 2.
- Registro manual: `POST /api/v1/finance/transactions` → exactamente 1 movimiento, `sourceType=MANUAL`.
- Resumen de período: ingresos, gastos, neto, conteo; fechas inclusivas.
- Verificado live: ingreso y gasto manual incrementan el ledger en +1 cada uno.

### Recurring obligations

- Plantilla (`RecurringFinancialObligation`) ≠ caja.
- Generación controlada de ocurrencias: **0** movimientos del ledger.
- Pago de ocurrencia PENDING → exactamente 1 EXPENSE (`sourceType=RECURRING_OBLIGATION`, `sourceId=occurrenceId`).
- Segundo pago → HTTP 409; cancelación → 0 ledger.
- Lecturas: pending / overdue / upcoming con totales de compromiso (no caja).
- Verificado live: generate → 0 ledger; pay → +1 EXPENSE; repay → 409; cancel → 0 ledger.

### Payroll

- Empleados `FIXED_PAYROLL` vs `PRODUCTION_BASED`.
- Generación de períodos: solo FIXED activos; idempotente; **0** ledger.
- Pago → 1 EXPENSE (`category=PAYROLL`, `sourceType=PAYROLL`, `sourceId=periodId`).
- Segundo pago → 409; cancelación de PENDING → 0 EXPENSE.
- Verificado live: generate → 0 ledger; pay → +1; repay → 409; cancel PENDING → 0.

### Production-based labor

- **Production-based labor remains manually registered per work.**
- **No automatic tariff engine was implemented.**
- Operador debe ser `PRODUCTION_BASED` activo; `FIXED_PAYROLL` se rechaza.
- `calculatedAmount` / amount histórico congelado al registrar.
- Lifecycle: PENDING → PAID | CANCELLED.
- Pago → 1 EXPENSE (`category=PAYROLL`, `sourceType=PAYROLL`, `sourceId=laborWorkId`).
- CANCELLED no entra en costo de rentabilidad; PENDING sí (costo incurrido).
- Cubierto por tests de use case / API contract / cálculo.

### Commercial payments

- Registrar `Payment` → exactamente 1 INCOME (`category=SALES`, `sourceType=COMMERCIAL_ORDER`, `sourceId=paymentId`).
- Pagos independientes → ingresos independientes.
- Idempotencia por índice único parcial + synchronizer (sin duplicar).
- **No hay backfill automático** de pagos históricos previos a la integración.
- No modifica total de Orden ni contratos Commercial.
- Verificado live: nuevo pago → +1 INCOME SALES.

### Plotter payments

- `PlotterJob.totalAmount` = valor facturado; `PlotterPayment` = dinero recibido.
- Crear job **no** crea `FinancialTransaction`.
- Registrar pago → 1 INCOME (`category=PLOTTER_REVENUE`, `sourceType=PLOTTER`, `sourceId=plotterPaymentId`).
- Rechaza montos superiores al saldo; soporta pagos parciales; no duplica ingresos.
- Consumo de papel: Inventory OUT con `sourceType=PLOTTER`, `sourceId=plotterJobId`, costo histórico congelado.
- **Plotter is not attributed to Commercial Orders until a reliable relationship exists.**
- Verificado live: overpay 400; partial pay → +1 INCOME; outstanding actualizado.

### Production material costs

- Consumo solo según lifecycle de ProductionOrder.
- Inventory OUT una vez (`sourceType=PRODUCTION`, `sourceId=consumptionId`).
- Costo histórico desde snapshot del movimiento; cambiar `InventoryItem.unitCost` no altera el histórico.
- Consumos sin `unitCost` → `unvaluedConsumptionCount`; **no** se muestran como $0 falsos.
- Consumo **no** crea EXPENSE en Finance (evita doble conteo con compras futuras).

### Commercial profitability

- Endpoint: `GET /api/v1/orders/{orderId}/profitability` (solo lectura).
- Calcula: orderValue, collectedAmount, outstandingAmount, materialCost, laborCost, totalDirectCost, directProfit, margin.
- Estados: `COMPLETE` | `PARTIALLY_UNVALUED` | `NO_COST_DATA`.
- Plotter material cost = 0 / no atribuible (diferido).
- No escribe `FinancialTransaction`.
- Verificado live: orden con costos → `PARTIALLY_UNVALUED` (1 consumo sin valuación); otra → `COMPLETE`; labor CANCELLED excluida.

### Frontend Finance

- Ruta única `/finance`: summary, transacciones, obligaciones, pending/overdue/upcoming, nómina.
- Snackbar + Alert (sin `window.alert`).
- Botones deshabilitados durante submit; mensajes en español; montos/fechas formateados (`es-CO`).
- Acciones de nómina solo en períodos PENDING.
- Lint (`oxlint`) y build (`vite build`) OK.
- Sin rediseño; no se encontraron defectos UX bloqueantes en código.

### Idempotency

Índices únicos parciales / de dominio:

| Origen | Garantía |
|--------|----------|
| Commercial Payment → INCOME | `uq_financial_transactions_commercial_order_source` |
| Plotter Payment → INCOME | `uq_financial_transactions_plotter_source` |
| Recurring occurrence → EXPENSE | `uq_financial_transactions_recurring_obligation_source` |
| Payroll period / labor → EXPENSE | `uq_financial_transactions_payroll_source` |
| Inventory OUT por origen | `uq_inventory_movements_source` |
| Occurrences (obligation, dueDate) | `uq_recurring_financial_obligation_occurrences_obligation_due` |
| Payroll periods (employee, periodStart) | `uq_payroll_periods_employee_period_start` |
| Paper roll numbers | `inventory_items_paper_roll_number_key` |

### Historical cost semantics

- Inventory OUT congela `unitCost` / `totalCost` al consumir.
- Labor congela `calculatedAmount` / `unitRate` al registrar.
- Payroll / obligation congelan amount snapshot al generar / pagar.
- Cambios posteriores de costo de ítem **no** reescriben historial.
- Rentabilidad lee snapshots históricos, no precios actuales.

### E2E verification

Ejecutado:

| Escenario | Resultado |
|-----------|-----------|
| Manual INCOME / EXPENSE | +1 cada uno |
| Obligation generate / pay / repay / cancel | 0 / +1 / 409 / 0 |
| Payroll generate / pay / repay / cancel | 0 / +1 / 409 / 0 |
| Commercial Payment → INCOME SALES | +1, sourceId=paymentId |
| Plotter overpay / partial pay | 400 / +1 INCOME |
| Profitability COMPLETE / PARTIALLY_UNVALUED / NO_COST_DATA | OK |
| Backend `mvnw compile` + `mvnw test` | 278 tests, 0 failures |
| Frontend `npm run lint` + `npm run build` | OK |
| Live API en `:8080` | instancia Spring Boot válida |

### Database integrity results

| Check | Issues |
|-------|--------|
| Duplicate ledger sources (idempotent types) | 0 |
| Duplicate payroll periods / occurrences | 0 |
| Duplicate inventory source movements | 0 |
| Duplicate paper roll numbers | 0 |
| Orphan FT → payments/plotter/occurrences/payroll+labor | 0 |
| Orphan production consumptions/labor vs production_orders | 0 |
| Paid periods/occurrences/labor sin ledger | 0 |
| Plotter payments sin INCOME | 0 |
| Commercial payments sin INCOME | **1** (histórico pre-integración; sin backfill) |
| Consumptions sin Inventory OUT | **1** (dato de prueba histórico) |
| PlotterJobs sin Inventory OUT | **2** (dato de prueba histórico; código actual es transaccional) |

No se borraron datos automáticamente.

### schema.sql vs live PostgreSQL

- Contenedor Docker `magyen-postgres` (Postgres 17) activo.
- 22 tablas de negocio presentes y alineadas con `backend/src/main/resources/db/schema.sql`.
- Índices de idempotencia Finance/Inventory presentes.
- FKs de production consumptions/labor e inventory movements presentes.
- Sequences: `paper_roll_number_seq`, `quotation_number_seq` presentes.
- `ddl-auto: validate` arranca sin drift.
- **No se aplicaron migraciones ni DROP** en este incremento.

### Known limitations

- Un Payment comercial histórico (2026-08-06, anticipo) no tiene INCOME (política explícita: sin backfill).
- Existe `SynchronizeCommercialPaymentFinancialTransactionUseCase` sin endpoint REST público de resync masivo.
- `GET /finance/payroll/periods` no filtra por query `status` (lista completa; UI filtra acciones PENDING).
- Categoría enum `PRODUCTION_PAYMENT` existe pero labor/payroll escriben `PAYROLL` (coherente con V1).
- Datos de prueba E2E creados durante Increment 13 (manual ledger, obligaciones, pagos).

### Deferred items

Fuera de SPR-036 / explícitamente no implementados:

- Home / dashboard financiero
- PDF
- Autenticación / autorización
- Scheduler automático de ocurrencias
- Holidays / calendario laboral avanzado
- Purchasing / suppliers
- Taxes / DIAN / facturación electrónica
- Weighted-average costing
- Refunds de Plotter
- BOM
- Motor de tarifas automáticas de mano de obra
- Auto-generación de labor desde operaciones de Production
- Nuevas arquitecturas de integración cross-module
- **Plotter → Commercial Order attribution** (sin relación confiable)
- Alertas Home
- Conciliación bancaria / cuentas bancarias

### Principios V1 preservados

1. Ledger = dinero real  
2. Obligations = compromisos futuros  
3. Occurrence = compromiso concreto  
4. Payment = movimiento real  
5. Historical cost snapshots are immutable  
6. Profitability is read-only  
7. No fake $0 costs  
8. No automatic consumption merely because a ProductionOrder exists  
9. No automatic payroll from Production operations  
10. Idempotency wherever an integration creates ledger/inventory movements  
11. Cross-module communication through application ports/adapters  
12. No cross-module JPA coupling  

### Architecture status

Finance V1 queda **cerrado funcionalmente** como ledger + obligaciones + nómina fija + labor manual por producción + integraciones Commercial/Plotter + atribución de costos históricos + rentabilidad read-only + frontend `/finance`, con comunicación entre módulos vía ports/adapters.

**STOP after Increment 13. Do not start SPR-037.**