# SPR-037 — Home / Dashboard Operativo y Alertas

## 1. Objetivo del Sprint

Construir una vista Home/Dashboard operativa para Magyen que consolide información relevante de Commercial, Production, Inventory, Plotter y Finance.

El objetivo principal es que, al ingresar al sistema, el usuario pueda identificar rápidamente:

* situación financiera del período;
* dinero pendiente por cobrar;
* obligaciones financieras pendientes o vencidas;
* materiales con stock bajo;
* rollos de papel próximos a agotarse;
* órdenes comerciales pendientes;
* órdenes de producción activas;
* rentabilidad que aún no puede valorarse completamente;
* situaciones que requieren atención.

El Dashboard será principalmente un **read model/orchestration layer**.

No debe convertirse en un nuevo propietario de datos ni duplicar información de otros módulos.

---

# 2. Principios arquitectónicos

## 2.1 Home no será dueño del dominio

Home no tendrá entidades propias para representar:

* Orders;
* ProductionOrders;
* InventoryItems;
* FinancialTransactions;
* PayrollPeriods;
* PlotterJobs.

Home solamente consultará los módulos propietarios y construirá una representación orientada a lectura.

---

## 2.2 Cada módulo conserva la propiedad de sus datos

```text
Commercial
   └── Orders / Quotations / Payments

Production
   └── ProductionOrders / Consumptions / Labor

Inventory
   └── Materials / Stock / Movements

Plotter
   └── PlotterJobs / Payments / Paper Rolls

Finance
   └── Ledger / Obligations / Payroll / Financial summaries

                 ↓

              HOME
          Read / Orchestration
```

Home no modificará datos de estos módulos.

---

# 3. Información que debe poder mostrar Home

## 3.1 Resumen financiero

Mostrar para el período seleccionado:

* Ingresos;
* Gastos;
* Resultado neto;
* Cantidad de movimientos.

La fuente será Finance.

No se recalcularán manualmente los valores si Finance ya dispone de un summary confiable.

---

## 3.2 Cobros pendientes

Mostrar información como:

* órdenes con saldo pendiente;
* valor total pendiente;
* cliente;
* número de orden;
* valor de la orden;
* valor pagado;
* saldo pendiente.

Debe distinguirse:

**valor de la venta** ≠ **dinero efectivamente cobrado**.

---

## 3.3 Compromisos financieros

Mostrar:

* pagos pendientes;
* pagos vencidos;
* próximos pagos;
* valor total pendiente;
* fecha de vencimiento.

Esto incluirá posteriormente:

* servicios;
* créditos;
* nómina;
* otras obligaciones.

No se deben crear transacciones automáticamente solamente porque aparezcan en Home.

---

# 4. Inventario

Home debe poder identificar materiales que requieren atención.

## Stock bajo

Regla existente:

```text
stock <= minimumStock
```

Si `minimumStock == null`, el material no está siendo monitoreado.

Mostrar:

* material;
* código;
* stock actual;
* unidad;
* stock mínimo.

---

# 5. Rollos de papel

Los rollos de Plotter deben tener prioridad visual independiente.

Ejemplo:

```text
RP-001 — 18.5 m restantes
RP-002 — 42.0 m restantes
RP-003 — 7.2 m restantes
```

Posteriormente podrá existir una regla específica para:

* crítico;
* bajo;
* normal.

La regla exacta de alerta deberá definirse antes de implementarla.

No inventar un porcentaje arbitrario sin decisión de producto.

---

# 6. Producción

Mostrar órdenes de producción que requieran atención.

Inicialmente:

* CREATED;
* PLANNED;
* IN_PROGRESS.

La información debe permitir identificar:

* orden comercial (`orderNumber`);
* cliente (`customerName` cuando Commercial lo resuelve);
* estado;
* prioridad;
* fecha de creación;
* fechas planificadas.

Los UUID de producción y de orden comercial permanecen como identificadores técnicos internos (navegación/API). No se inventó un número de producción tipo `PROD-00015`.

---

# 7. Rentabilidad

Home podrá mostrar pedidos cuya rentabilidad necesita atención.

Especialmente:

```text
PARTIALLY_UNVALUED
```

y

```text
NO_COST_DATA
```

Esto no significa que el pedido sea malo.

Significa que el sistema todavía no tiene información suficiente para calcular correctamente su rentabilidad.

---

# 8. Alertas

Las alertas serán derivadas de datos existentes.

Ejemplos:

### 🔴 Críticas

* obligación vencida;
* material por debajo del mínimo;
* rollo de papel agotado o críticamente bajo;
* error operacional que requiera atención.

### 🟡 Atención

* pago próximo;
* material cercano al mínimo;
* orden de producción pendiente;
* rentabilidad parcialmente valorada.

### 🔵 Información

* nuevos pedidos;
* producción recientemente creada;
* movimientos recientes.

Las severidades definitivas se establecerán conforme se implementen los casos reales.

---

# 9. Arquitectura esperada

Home deberá seguir una estructura similar:

```text
HomeController
      ↓
GetHomeDashboardUseCase
      ↓
      ├── Commercial query port
      ├── Finance query port
      ├── Inventory query port
      ├── Production query port
      └── Plotter query port
      ↓
HomeDashboardResult
      ↓
HomePresentationMapper
      ↓
HomeDashboardResponse
```

No se permite:

```text
Home → JPA Repository de otro módulo
```

ni:

```text
Home → Entity de otro módulo
```

---

# 10. Performance

Home no debe ejecutar una consulta independiente por cada tarjeta si puede evitarse.

Preferir:

* queries agregadas;
* read models;
* consultas específicas;
* endpoints existentes cuando sean suficientes.

Evitar N+1.

---

# 11. Tolerancia a fallos

Una falla de un bloque no debería necesariamente dejar toda la Home inutilizable.

Ejemplo:

```text
Finance      ✅
Inventory    ✅
Production   ❌
Plotter      ✅
```

Home deberá poder comunicar que Production no pudo cargarse sin ocultar toda la información restante cuando técnicamente sea posible.

---

# 12. Frontend

La Home deberá ser:

* clara;
* rápida de interpretar;
* responsive;
* orientada a acciones;
* consistente con MUI y el diseño existente.

No se priorizarán gráficas decorativas.

Primero:

**información → jerarquía → acciones → estética.**

---

# 13. No objetivos del Sprint

No implementar en este sprint:

* autenticación/autorización;
* PDF;
* compras/proveedores;
* impuestos;
* scheduler;
* BOM;
* tarifas automáticas;
* WAC;
* vacaciones/feriados;
* Plotter → Order attribution;
* auto-consumo;
* rediseño de Commercial;
* rediseño de Production;
* rediseño de Inventory;
* rediseño de Finance.

---

# 14. Seguridad

Home debe ser inicialmente read-only.

No debe existir ninguna acción destructiva ni modificación de dominio desde el Dashboard sin que exista un caso de uso explícito.

---

# 15. Calidad

Cada incremento debe mantener:

```text
mvnw test
npm run lint
npm run build
```

en estado verde.

Los cambios deben ser incrementales y evitar duplicación de modelos.

---

# 16. Estrategia del Sprint

### Increment 1

Dashboard read model foundation + backend orchestration.

### Increment 2

Commercial / cuentas por cobrar.

### Increment 3

Finance commitments + financial summary.

### Increment 4

Inventory + paper-roll alerts.

### Increment 5

Production + profitability attention.

### Increment 6

Frontend Home foundation.

### Increment 7

Alert center + navigation/action links.

### Increment 8

E2E verification + UX stabilization.

### Increment 9

Final sprint review and closure.

La cantidad puede reducirse si algún incremento queda completamente cubierto por otro sin sacrificar calidad.

---

# 17. Criterio de finalización

SPR-037 se considerará completo cuando:

* Home pueda consolidar información real de los módulos;
* no existan duplicaciones innecesarias de dominio;
* las alertas estén basadas en reglas explícitas;
* los datos financieros distingan cash de obligaciones;
* inventario y rollos de papel puedan generar alertas confiables;
* producción y rentabilidad tengan indicadores útiles;
* exista navegación desde indicadores hacia la información original;
* frontend sea estable y usable;
* backend y frontend estén completamente verificados;
* no existan defectos bloqueantes.

---

# 18. Increment 1 — Read Model Foundation (implementado)

## 18.1 Objetivo del incremento

Establecer el módulo `com.magyen.platform.home` como capa de read model / orquestación, con el primer bloque real integrado: resumen financiero de Finance.

## 18.2 Límites de ownership

| Módulo | Ownership | Rol de Home |
|--------|-----------|-------------|
| Finance | Ledger, summary, obligaciones | Consume vía `FinanceDashboardPort` |
| Commercial | Orders, Quotations | Consume vía `CommercialDashboardPort` (Increment 2) |
| Inventory | Stock / movements | Port reservado |
| Production | ProductionOrders | Port reservado |
| Plotter | Jobs / paper rolls | Port reservado |
| Home | Ningún dato operativo propio | Solo consolida lecturas |

Home **no** usa:

* `OrderRepository`
* `FinancialTransactionRepository`
* `InventoryItemRepository`
* `ProductionOrderRepository`
* `PlotterJobRepository`
* entidades JPA de otros módulos

Tampoco crea tablas, migraciones ni entidades de dominio propias.

## 18.3 Arquitectura del read model

```text
home
├── domain
│   └── exception (HomeDomainException)
├── application
│   ├── dto (query / result / financial summary)
│   ├── port (Finance + ports futuros)
│   └── usecase (GetHomeDashboardUseCase)
├── infrastructure
│   ├── configuration (HomeConfiguration)
│   └── finance (FinanceDashboardAdapter)
└── presentation
    └── dashboard (controller / mapper / response)
```

Flujo:

```text
GET /api/v1/home/dashboard
  → HomeDashboardController
  → GetHomeDashboardUseCase
  → FinanceDashboardPort
  → FinanceDashboardAdapter
  → GetFinancialPeriodSummaryUseCase (Finance)
  → GetHomeDashboardResult
  → HomeDashboardResponse
```

## 18.4 Application ports

| Port | Estado Increment 1 |
|------|--------------------|
| `FinanceDashboardPort` | Implementado (`FinanceDashboardAdapter`) |
| `CommercialDashboardPort` | Interface reservada, sin adaptador |
| `InventoryDashboardPort` | Interface reservada, sin adaptador |
| `ProductionDashboardPort` | Interface reservada, sin adaptador |
| `PlotterDashboardPort` | Interface reservada, sin adaptador |

No se crean implementaciones fake ni datos fabricados.

## 18.5 Integración Finance

* Home reutiliza `GetFinancialPeriodSummaryUseCase`.
* No duplica sumas del ledger.
* No llama HTTP a `/api/v1/finance/summary`.
* Campos expuestos: `income`, `expense`, `netResult`, `transactionCount`.
* Período vacío → ceros válidos (no es error).

## 18.6 Semántica de fechas

* Rango inclusivo `[fromDate, toDate]`, igual que Finance summary.
* Si ambos parámetros omitidos → **mes calendario actual** (`dayOfMonth=1` … último día del mes) vía `Clock` de aplicación (misma semántica que `getCalendarMonthRange` del frontend Finance).
* Si solo uno de los dos está presente → `400 BAD_REQUEST`.
* Si `fromDate > toDate` → `400 BAD_REQUEST`.
* Formato inválido de fecha → `400 BAD_REQUEST` (`MethodArgumentTypeMismatchException`).

## 18.7 Contrato REST

```http
GET /api/v1/home/dashboard
GET /api/v1/home/dashboard?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD
```

Respuesta (Increment 1 — histórico; superseded en Increment 2 para `receivables`):

```json
{
  "fromDate": "2026-08-01",
  "toDate": "2026-08-31",
  "generatedAt": "2026-08-10T05:00:00Z",
  "financialSummary": {
    "income": 0,
    "expense": 0,
    "netResult": 0,
    "transactionCount": 0
  },
  "receivables": [],
  "financialCommitments": [],
  "inventoryAlerts": [],
  "paperRollAlerts": [],
  "productionAttention": [],
  "profitabilityAttention": []
}
```

En Increment 1, `receivables` era un arreglo vacío placeholder. A partir de Increment 2 es un objeto tipado (ver §19).

## 18.8 No-objetivos de Increment 1

* Frontend Home / cards / charts / widgets
* Inventory alerts / paper-roll alerts
* Production dashboard / profitability dashboard
* Receivables / payroll widgets / Plotter widgets
* Notifications / PDF / scheduler / authentication
* Nuevas tablas o migraciones
* Compromisos financieros en Home (Increment 3)

## 18.9 Persistencia / DBeaver

* Sin tablas nuevas
* Sin migraciones
* Sin SQL destructivo
* Sin modificación de datos requerida

## 18.10 Trabajo diferido a Increment 2+

* Increment 2: Commercial / cuentas por cobrar (`CommercialDashboardPort` + adaptador real)
* Increment 3: compromisos financieros
* Increment 4: Inventory + paper-roll alerts
* Increment 5: Production + profitability attention
* Increment 6+: Frontend Home foundation

---

# 19. Increment 2 — Commercial Receivables (implementado)

## 19.1 Objetivo

Exponer en Home las cuentas por cobrar reales a partir de Órdenes comerciales y Payments de Finance, sin crear un agregado de receivables ni tablas nuevas.

## 19.2 Ownership

| Concepto | Dueño | Rol de Home |
|----------|-------|-------------|
| Order / `orderValue` | Commercial | Lectura vía `GetOrdersUseCase` |
| Payment / cobrado | Finance | Lectura vía `OrderPaymentCollectionPort` → `GetPaymentsByOrderUseCase` |
| Cuentas por cobrar (read model) | Home (orquestación) | Consolida outstanding actuales |

Home **no** introduce:

* tabla de receivables;
* entidad de dominio de receivables;
* dependencia JPA a Commercial o Finance;
* uso de `FinancialTransaction` como fuente de saldo pendiente.

## 19.3 Semántica Order value vs collected cash

```text
orderValue        = Order.total
collectedAmount   = suma de Payments válidos de Finance por orderId
outstandingAmount = orderValue - collectedAmount
```

* **Payment** es la relación autoritativa de cobro contra la Orden.
* **FinancialTransaction** (ledger income) es representación contable/caja y **no** se usa para el saldo pendiente en Home.
* No se usa el `PaymentSummary` comercial de anticipos/reconocimientos.

Solo se incluyen órdenes con:

```text
outstandingAmount > 0
```

Órdenes totalmente pagadas o con saldo cero/negativo se excluyen.

## 19.4 Current outstanding vs período financiero

| Bloque | Semántica de fechas |
|--------|---------------------|
| `financialSummary` | Respeta `fromDate`/`toDate` (mes calendario si se omiten) |
| `receivables` | **Saldos pendientes actuales**, sin filtrar por el período |

`GET /api/v1/home/dashboard?fromDate=...&toDate=...` cambia el resumen financiero del período; las cuentas por cobrar siguen mostrando el outstanding vigente.

## 19.5 Puertos y adaptadores

```text
GetHomeDashboardUseCase
  ├── FinanceDashboardPort → FinanceDashboardAdapter → GetFinancialPeriodSummaryUseCase
  └── CommercialDashboardPort → CommercialDashboardAdapter
        ├── GetOrdersUseCase
        └── OrderPaymentCollectionPort → GetPaymentsByOrderUseCase
```

Ordenamiento determinístico de ítems:

1. `outstandingAmount` descendente;
2. `orderNumber` ascendente;
3. `orderId` como desempate final.

## 19.6 Contrato REST (receivables)

```json
{
  "financialSummary": { "...": "..." },
  "receivables": {
    "totalOutstandingAmount": 0,
    "totalCollectedAmount": 0,
    "orderCount": 0,
    "items": [
      {
        "orderId": "...",
        "orderNumber": "...",
        "customerId": "...",
        "orderValue": 0,
        "collectedAmount": 0,
        "outstandingAmount": 0
      }
    ]
  }
}
```

Estado vacío válido: totales en cero e `items: []`.

`customerName` no se incluye en este incremento (solo `customerId`) para evitar acoplar Home a resolución de clientes más allá de lo necesario.

## 19.7 Errores

Si la carga de receivables falla, la excepción se propaga (vía `GlobalExceptionHandler`). No se fabrican ceros fingiendo un resultado exitoso.

## 19.8 Persistencia

Sin tablas, migraciones ni mutaciones. Solo lectura.

## 19.9 No-objetivos Increment 2

* Frontend Home
* Compromisos financieros (Increment 3)
* Inventory / paper-roll / Production / profitability / Plotter
* Refunds / credit notes / cobranza automática

## 19.10 Diferido a Increment 3+

* Finance commitments en Home
* Enrichment de nombre de cliente (si se decide)
* Optimización batch de Payments (evitar N+1) si el volumen lo requiere

---

# 20. Increment 3 — Finance Commitments (implementado)

## 20.1 Objetivo

Exponer en Home los compromisos financieros PENDING reales reutilizando los read models de Finance (SPR-036), sin crear agregado ni tablas de commitments en Home.

## 20.2 Ownership

| Concepto | Dueño | Rol de Home |
|----------|-------|-------------|
| RecurringFinancialObligation / Occurrence | Finance | Lectura vía use cases pending/overdue/upcoming |
| Compromisos (read model Home) | Home (orquestación) | Consolida snapshot actual |
| FinancialTransaction (cash) | Finance | **No** es fuente de pending commitments |

Distinción explícita:

* **Receivables** = dinero que clientes deben a Magyen (Commercial + Payments).
* **Commitments** = dinero que Magyen espera pagar (obligaciones PENDING).
* No se mezclan totales.

## 20.3 Pending vs cash

```text
PENDING   → compromiso outstanding
PAID      → excluido
CANCELLED → excluido
```

`FinancialTransaction` representa movimiento de caja real; no se usa para inferir pendientes.

## 20.4 Semántica overdue / upcoming

Reutiliza Finance sin redefinir ventanas:

| Vista | Regla Finance |
|-------|---------------|
| Pending (items) | Todas las ocurrencias `PENDING` (incluye vencidas) |
| Overdue | `PENDING` con `dueDate < today` |
| Upcoming | `PENDING` con `today <= dueDate <= today + daysAhead` |
| Default upcoming | `daysAhead = 7` (`GetUpcomingFinancialObligationOccurrencesUseCase.DEFAULT_DAYS_AHEAD`) |

Due today es upcoming, no overdue. Overdue no cuenta en `upcomingCount`.

## 20.5 Current commitments vs período financiero

| Bloque | Semántica de fechas |
|--------|---------------------|
| `financialSummary` | Respeta `fromDate`/`toDate` |
| `receivables` | Outstanding actual (sin filtro de período) |
| `commitments` | Compromisos PENDING actuales (sin filtro de período) |

Un compromiso de julio aún PENDING aparece aunque el usuario mire el resumen financiero de agosto.

## 20.6 Puerto / adaptador

```text
FinanceDashboardPort
  ├── getPeriodSummary(...)
  └── getCurrentFinancialCommitments()
        → FinanceDashboardAdapter
             ├── GetPendingFinancialObligationOccurrencesUseCase  → totalPendingAmount + items
             ├── GetOverdueFinancialObligationOccurrencesUseCase  → totalOverdueAmount + overdueCount
             └── GetUpcomingFinancialObligationOccurrencesUseCase → upcomingCount (default 7)
```

Orden de `items`: se preserva el de Finance pending (`dueDate ASC`, `expectedAmount DESC`, `id ASC`), que coloca vencidos primero por fecha.

## 20.7 Contrato REST

Campo JSON: `commitments` (reemplaza el placeholder `financialCommitments` de Increment 1).

```json
{
  "commitments": {
    "totalPendingAmount": 0,
    "totalOverdueAmount": 0,
    "overdueCount": 0,
    "upcomingCount": 0,
    "items": [
      {
        "occurrenceId": "...",
        "obligationId": "...",
        "name": "...",
        "type": "SERVICE",
        "expectedAmount": 0,
        "dueDate": "2026-08-15",
        "status": "PENDING",
        "overdue": false,
        "daysUntilDue": 5,
        "daysOverdue": null
      }
    ]
  }
}
```

Estado vacío válido: ceros e `items: []`.

## 20.8 Errores / persistencia

* Fallo de lectura → excepción propagada; no se fabrican ceros falsos.
* Sin tablas, migraciones ni mutaciones.

## 20.9 No-objetivos Increment 3

* Frontend Home
* Inventory / paper-roll / Production / profitability / Plotter
* Generación automática de ocurrencias / pagos automáticos
* Widgets de nómina dedicados

## 20.10 Diferido a Increment 4+

* Inventory alerts + paper-roll alerts
* Production / profitability attention
* Frontend Home foundation

---

# 21. Increment 4 — Inventory and Paper Roll Alerts (implementado)

## 21.1 Objetivo

Exponer en Home alertas reales de stock bajo (inventario general) y de rollos de papel Plotter, reutilizando la semántica de Inventory (SPR-035).

## 21.2 Ownership

| Concepto | Dueño | Rol de Home |
|----------|-------|-------------|
| Low-stock (`isLowStock`) | Inventory | Consume flag vía `GetInventoryItemsUseCase` |
| Clasificación rollo Plotter (`isPlotterPaperRoll`) | Inventory | Filtra con `plotterPaperRoll` del read model |
| Alertas Home | Home (orquestación) | Consolida secciones sin recalcular reglas |

Home **no** crea:

* agregado Alert;
* notificaciones;
* tablas;
* duplicado de `stock <= minimumStock`.

## 21.3 Semántica low-stock

Regla autoritativa Inventory:

```text
minimumStock != null && stock <= minimumStock
```

* `minimumStock == null` → monitoreo desactivado → **no** aparece en alertas.
* `minimumStock == 0` es válido (p. ej. stock 0 es low-stock).
* Home usa `GetInventoryItemResult.lowStock` — no reimplementa la comparación.

## 21.4 Clasificación paper-roll

Rollo Plotter (Inventory):

```text
PAPER + METER + paperRollNumber != null
```

Expuestos en el read model como `plotterPaperRoll == true`.

| Material | inventoryAlerts (si low) | paperRollAlerts (si low) |
|----------|--------------------------|--------------------------|
| FABRIC / INK / THREAD / DTF / OTHER | Sí | No |
| PAPER sin RP | Sí | No |
| PAPER + METER + RP | Sí | Sí |

El dominio Inventory también impide persistir PAPER+RP con UoM distinto de METER.

## 21.5 Puerto / adaptador

```text
InventoryDashboardPort.getCurrentInventoryAlerts()
  → InventoryDashboardAdapter
       → GetInventoryItemsUseCase.execute()
            ├── filter lowStock → inventoryAlerts
            └── filter lowStock && plotterPaperRoll → paperRollAlerts
```

Orden (Inventory no define orden low-stock): `stock - minimumStock` ascendente, luego stock, código/RP, id.

## 21.6 Contrato REST

```json
{
  "inventoryAlerts": {
    "lowStockCount": 0,
    "items": [
      {
        "inventoryItemId": "...",
        "materialCode": "...",
        "name": "...",
        "description": "...",
        "materialType": "FABRIC",
        "paperRollNumber": null,
        "stock": 0,
        "unitOfMeasure": "METER",
        "minimumStock": 0,
        "lowStock": true
      }
    ]
  },
  "paperRollAlerts": {
    "lowStockCount": 0,
    "items": [
      {
        "inventoryItemId": "...",
        "materialCode": "...",
        "name": "...",
        "paperRollNumber": "RP-001",
        "stock": 0,
        "unitOfMeasure": "METER",
        "minimumStock": 0,
        "lowStock": true
      }
    ]
  }
}
```

Estados vacíos válidos: `lowStockCount = 0`, `items = []`.

## 21.7 Persistencia / numeración RP

* Sin mutaciones, migraciones ni tablas.
* No se modifica `paper_roll_number_seq` ni se crean/eliminan rollos.
* Recordatorio futuro (reset V1): recrear/resetear `paper_roll_number_seq` como parte del reset completo — no en este incremento.

## 21.8 No-objetivos Increment 4

* Frontend Home / notificaciones / scheduler
* Producción / rentabilidad
* Mutaciones de Inventory / renumeración RP

## 21.9 Diferido a Increment 5+

* Production + profitability attention
* Frontend Home foundation
* Centro de alertas / notificaciones

---

# 22. Increment 5 — Production and Profitability (implementado)

## 22.1 Objetivo

Exponer en Home un resumen operativo de Production Orders y un resumen de rentabilidad directa comercial, reutilizando use cases existentes sin nuevas fórmulas.

## 22.2 Ownership

| Concepto | Dueño | Rol de Home |
|----------|-------|-------------|
| ProductionOrder lifecycle | Production | Lectura vía `GetProductionOrdersUseCase` |
| Rentabilidad directa | Commercial (`GetOrderProfitabilityUseCase`) | Agregación de resultados existentes |
| Resumen Home | Home (orquestación) | Consolida counts / totales sin recalcular |

## 22.3 Production

Estados: `CREATED`, `PLANNED`, `IN_PROGRESS`, `COMPLETED`.

```text
productionSummary
├── totalOrders
├── createdCount / plannedCount / inProgressCount / completedCount
└── items[]  → solo CREATED / PLANNED / IN_PROGRESS
```

Orden de `items`:

1. IN_PROGRESS → PLANNED → CREATED  
2. prioridad URGENT → HIGH → NORMAL → LOW  
3. creationDate ASC  
4. productionOrderId

Campos de presentación en cada ítem (aditivos; los UUID se conservan):

* `orderNumber` — número comercial existente, ingresado al crear la Orden
* `customerId` / `customerName` — cliente Commercial; `customerName` solo si el cliente existe

No existe `productionNumber`. Home muestra `orderNumber` como identidad de negocio de la OP (relación 1:1).

COMPLETED solo contribuye al conteo, no a `items`.

## 22.4 Profitability — fuente de verdad

`GetOrderProfitabilityUseCase` (SPR-036 / `GET /api/v1/orders/{orderId}/profitability`).

Home **no** recalcula material/labor/margen.

Estados: `COMPLETE`, `PARTIALLY_UNVALUED`, `NO_COST_DATA`.

### Selección de órdenes

Incluye: `CONFIRMED`, `IN_PRODUCTION`, `READY_FOR_DELIVERY`, `DELIVERED`.  
Excluye: `CLOSED`.  
No existe `DRAFT` en el dominio de Order.

### Agregación monetaria (evitar tratar desconocido como 0)

Totales `totalOrderValue`, `totalDirectCost`, `totalDirectProfit` y el margen promedio **solo agregan órdenes COMPLETE**.

`PARTIALLY_UNVALUED` / `NO_COST_DATA` se cuentan y suman `unvaluedCostCount`, pero **no** entran en los totales monetarios (aunque el API individual pueda devolver `0.00` en costos).

### Margen ponderado

```text
averageMarginPercentage = totalDirectProfit / totalOrderValue × 100
```

Si `totalOrderValue == 0` → `averageMarginPercentage = null`.

Direct profit ≠ net profit. No incluye overhead, impuestos ni compromisos Finance.

## 22.5 Puertos / adaptadores

```text
ProductionDashboardPort → ProductionDashboardAdapter → GetProductionOrdersUseCase
CommercialDashboardPort
  ├── getCurrentOutstandingReceivables()
  └── getCurrentProfitabilitySummary() → GetOrdersUseCase + GetOrderProfitabilityUseCase
```

## 22.6 Contrato REST

```json
{
  "productionSummary": {
    "totalOrders": 0,
    "createdCount": 0,
    "plannedCount": 0,
    "inProgressCount": 0,
    "completedCount": 0,
    "items": []
  },
  "profitabilitySummary": {
    "evaluatedOrderCount": 0,
    "completeOrderCount": 0,
    "partiallyUnvaluedOrderCount": 0,
    "noCostDataOrderCount": 0,
    "totalOrderValue": 0,
    "totalDirectCost": 0,
    "totalDirectProfit": 0,
    "averageMarginPercentage": null,
    "unvaluedCostCount": 0
  }
}
```

Reemplaza los placeholders `productionAttention` / `profitabilityAttention`.

## 22.7 Persistencia

Sin tablas, migraciones ni mutaciones. Solo lectura.

## 22.8 No-objetivos Increment 5

* Frontend Home / notificaciones
* Nuevas fórmulas de rentabilidad / net profit / overhead
* Cambios al ciclo de vida de Production

## 22.9 Diferido a Increment 6+

* Frontend Home foundation → **implementado en Increment 6**
* Alert center / navigation links
* Optimización batch de rentabilidad si el volumen lo requiere

---

# 23. Increment 6 — Home Dashboard Frontend Foundation (implementado)

## 23.1 Objetivo

Crear la base funcional del Home Dashboard en el frontend, consumiendo exclusivamente:

`GET /api/v1/home/dashboard`

Sin nuevos endpoints, sin persistencia en frontend, sin cálculos de negocio.

## 23.2 Arquitectura frontend

```
frontend/src/features/home/
├── services/homeService.js
├── presentation/homePresentation.js
├── components/
│   ├── EmptyState.jsx
│   ├── MetricCard.jsx
│   └── SectionHeader.jsx
└── pages/HomePage.jsx
```

Patrones alineados con Finance / Inventory / Production / Commercial / Plotter:

* `httpClient` (`/api/v1`)
* estado local de página (`useState` / `useEffect`)
* sin librería nueva de estado ni dependencias adicionales

## 23.3 Contrato REST consumido

Campos del read model (backend = fuente de verdad):

| Sección UI | Campo JSON |
|------------|------------|
| Período | `fromDate`, `toDate`, `generatedAt` |
| A — Resumen financiero | `financialSummary` (`income`, `expense`, `netResult`, `transactionCount`) |
| B — Cuentas por cobrar | `receivables` |
| C — Compromisos | `commitments` |
| D — Alertas inventario | `inventoryAlerts` |
| E — Rollos de papel | `paperRollAlerts` |
| F — Producción | `productionSummary` |
| G — Rentabilidad | `profitabilitySummary` |

Query opcionales: `fromDate`, `toDate` (solo afectan el resumen financiero según semántica backend).

## 23.4 Regla backend-as-source-of-truth

El frontend **no** calcula:

* resultado neto, márgenes, utilidad;
* overdue / daysUntilDue;
* stock bajo (`stock <= minimumStock`);
* contadores de producción o rentabilidad;
* outstanding de receivables.

Solo formatea y presenta valores del backend. Etiquetas en español (p. ej. rentabilidad COMPLETE → «Completa») sin renombrar enums en la API.

## 23.5 Selector de período

* Controles: Desde, Hasta, Mes actual, Mes anterior, Aplicar.
* Default: mes calendario actual.
* La petición HTTP se dispara solo al aplicar o al elegir un preset (no en cada tecla).
* Receivables, commitments, alertas, producción y rentabilidad permanecen “actuales” (independientes del período), según backend.

## 23.6 Navegación

Rutas existentes usadas (sin inventar rutas):

| Destino | Ruta |
|---------|------|
| Home (default app) | `/` → `/home` |
| Orden comercial | `/commercial/orders/:orderId` |
| Orden de producción | `/production/orders/:productionOrderId` |
| Ítem inventario / rollo | `/inventory/:inventoryItemId` |
| Hubs | `/finance`, `/inventory`, `/plotter`, `/production`, `/commercial/orders` |

No hay ruta de detalle de compromiso financiero → enlace al hub Finance.

Sidebar: ítem «Inicio» → `/home`.

## 23.7 Loading / error / empty

* Loading: skeletons en cards y tablas (patrones MUI existentes).
* Error: mensaje en español + «Reintentar»; no se muestran ceros inventados.
* Empty válidos (no son error): mensajes por sección (sin cobros, sin compromisos, sin stock bajo, sin rollos bajos, sin OP activas, sin rentabilidad).

## 23.8 No-objetivos Increment 6

* Pulido UX/UI final
* Centro de notificaciones / scheduler / PDF
* Nuevos endpoints o tablas
* Cálculos de rentabilidad u otras reglas de negocio en React

## 23.9 Diferido a Increment 7+

* Pulido UX/UI final + jerarquía operativa → **implementado en Increment 7**
* Navegación en español + retiro de Intelligence del menú V1 → **implementado en Increment 7**
* Optimización batch de rentabilidad si el volumen lo requiere (sigue diferido / V2 si aplica)

---

# 24. Increment 7 — Final Home UX/UI, Operational Priority and Navigation Cleanup (implementado)

## 24.1 Objetivo

Cerrar SPR-037 V1 con:

* jerarquía operativa del Home;
* limpieza de navegación en español;
* retiro de Intelligence del menú V1 (sin borrar el módulo);
* pulido visual sin nuevos endpoints ni reglas de negocio.

## 24.2 Orden final de secciones (obligatorio)

1. **Producción**
2. **Rentabilidad**
3. **Compromisos pendientes**
4. **Rollos de papel**
5. **Cuentas por cobrar**
6. **Período financiero**
7. **Resumen financiero**
8. **Alertas de inventario** (materiales generales)

Home responde primero a operación y obligaciones; el contexto financiero queda al final.

## 24.3 Jerarquía visual

| Prioridad | Sección | Tratamiento |
|-----------|---------|-------------|
| 1 | Producción | Contenedor destacado, tipografía primaria, chip «En proceso» enfatizado |
| 2 | Rentabilidad | Secundaria; etiqueta «Resultado directo» (no ganancia neta) |
| 3 | Compromisos | Estándar; overdue / hoy / próximo vía campos backend |
| 4 | Rollos de papel | Estándar; solo `paperRollAlerts` |
| 5 | Cuentas por cobrar | Estándar; período-independiente |
| 6–7 | Período + resumen financiero | Contexto; período solo afecta `financialSummary` |
| 8 | Alertas de inventario | Contexto; sin duplicar rollos ya mostrados en §4 (filtro UI) |

## 24.4 Navegación sidebar (español)

| Label V1 | Ruta |
|----------|------|
| Inicio | `/home` |
| Comercial | `/commercial` |
| Producción | `/production` |
| Inventario | `/inventory` |
| Plotter | `/plotter` |
| Finanzas | `/finance` |

La ruta `/intelligence` **permanece** en el router para no romper el módulo; **no** aparece en el menú V1.

## 24.5 Intelligence → V2

No se implementan en SPR-037 V1:

* recomendaciones automáticas;
* compras predictivas;
* alertas inteligentes por patrones históricos;
* proyecciones de caja;
* resúmenes generados por IA;
* “qué debe hacer Magyen ahora”.

Intelligence como producto de recomendaciones queda **diferido a V2**.

## 24.6 Semántica REST intacta

* Sin cambios de backend, contrato ni BD.
* Frontend presenta valores del backend; no calcula neto, márgenes, overdue ni stock bajo.
* Período: fetch solo en Aplicar / Mes actual / Mes anterior.

## 24.7 QA Increment 7

* `npm run lint` / `npm run build` (frontend).
* Verificación manual del orden de secciones, labels ES, ausencia de Intelligence en sidebar, navegación a módulos existentes, período vs receivables/commitments, empty/error/loading.

## 24.8 Límite V1 / V2

**V1 (cerrado en este incremento):** dashboard operativo determinista + navegación limpia.

**V2 (fuera de SPR-037):** Intelligence / recomendaciones / analytics predictivo / centros de alerta avanzados.

---

# 25. Corrección V1 pre-reset — identificadores legibles de Producción

Antes del reset limpio de base de datos, Home y Producción mostraban UUID truncados como identidad visible.

Eso era una representación interna expuesta por error en la UI. No era un identificador de negocio.

## Decisión

* No se introdujo un número persistente de producción (`PROD-#####`).
* No hay `productionNumber` en el dominio ni en el esquema.
* La identidad de negocio reutilizada es el `orderNumber` comercial (texto ingresado al confirmar la orden).
* El nombre de cliente se resuelve desde Commercial (`GetCustomersUseCase`) cuando el cliente existe.
* Los UUID (`productionOrderId`, `orderId`) siguen siendo identificadores técnicos para API y navegación.

## Flujo

Presentation → Application → `GetProductionOrdersUseCase` / `GetProductionOrderUseCase` → `CommercialOrderIdentityResolver` → `GetOrdersUseCase` + `GetCustomersUseCase`.

Home solo orquesta el read model ya enriquecido. Sin JPA cruzado, sin nombres inventados a partir de UUID, sin reset de base de datos.

## UI

| Columna | Valor visible | Navegación |
|---------|---------------|------------|
| Producción | `orderNumber` | `/production/orders/:productionOrderId` |
| Orden comercial | `orderNumber` | `/commercial/orders/:orderId` |
| Cliente | `customerName` | — |

Si falta el número o el nombre, se muestra `—`. No se recorta un UUID como etiqueta.
