# SPR-039 — Cierre funcional final de V1 y preparación de release

**Incremento:** SPR-039 — cierre funcional  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Este documento describe el cierre funcional de Magyen Platform V1 antes de la fase visual / release.

**SPR-040 no se inicia.** No hay rediseño visual, responsive, color, animación, PDF ni pulido estético.

La base de datos de V1 contiene datos reales de Magyen. Este incremento no resetea la base, no trunca tablas, no recrea volúmenes Docker y no reescribe filas de negocio.

---

## 1. UX final de pago externo de Plotter

En Plotter → listado de trabajos, la columna **Acciones** de un trabajo `EXTERNAL` muestra:

| Condición | Acción visible |
|---|---|
| Siempre | **Ver detalle** |
| `outstandingAmount > 0` | **Registrar pago** |
| `outstandingAmount = 0` | **Pago completado** (texto, no es un segundo flujo) |

La familia no necesita abrir el detalle para registrar un abono.

El diálogo es el mismo de detalle (`RegisterPlotterPaymentDialog`) y llama al mismo servicio:

`POST /api/v1/plotter/jobs/{id}/payments` → `RegisterPlotterPaymentUseCase`

Campos del diálogo:

- Total del trabajo
- Total pagado
- Saldo pendiente
- Valor a registrar
- Fecha del pago
- Observación

Tras un pago exitoso el listado se recarga: se actualizan Pagado, Saldo y la acción pasa a **Pago completado** cuando el saldo llega a cero.

Trabajos `INTERNAL_MAGYEN` y `WASTE` no exponen acciones de pago. Solo **Ver detalle**.

---

## 2. Métricas de cobranza de Plotter

En Plotter → Rentabilidad hay dos lecturas distintas, ambas de solo lectura:

**RENTABILIDAD**

- Papel impreso
- Total generado (externo / interno / combinado)
- Gastado en papel (adquisiciones del período)
- Gastado en tinta (adquisiciones del período)
- Resultado = ingreso combinado − papel − tinta
- Conteos externo / interno / merma

**COBRANZA (trabajos EXTERNAL)**

- Total generado = suma de `totalAmount` de trabajos EXTERNAL del período
- Total pagado = suma de `PlotterPayment` de esos mismos trabajos
- Saldo pendiente por cobrar = generado − pagado

No se confunde costo de papel/tinta con cobranza.

Abrir el reporte no crea ni modifica `FinancialTransaction`.

Alcance `INTERNAL` o `WASTE`: cobranza externa = 0.

---

## 3. Contabilidad de pago externo

Modelo vigente, sin segundo mecanismo:

```text
EXTERNAL PlotterJob
        |
        +--> representa el servicio / venta
        |
        +--> NO crea pago automático al crear el trabajo
        |
        +--> cada PlotterPayment
                |
                +--> un FinancialTransaction INCOME
                     sourceType=PLOTTER
                     sourceId=plotterPaymentId
                     category=PLOTTER_REVENUE
```

Reglas:

- Varios pagos están permitidos.
- Saldo = total del trabajo − pagos acumulados.
- Un pago mayor al saldo se rechaza (`PlotterDomainException`).
- Idempotencia del ledger: un INCOME por `plotterPaymentId`. Reintentar `ensureIncomeForPlotterPayment` no duplica el asiento.
- El saldo se calcula desde `PlotterPayment`, no desde el ledger.

---

## 4. Contabilidad de Plotter interno

`INTERNAL_MAGYEN` es operación de material de producción, no venta a cliente.

- Orden comercial obligatoria.
- Consume papel una sola vez (`Inventory OUT`, `sourceType=PLOTTER`).
- El valor del servicio es analítico / de atribución a la orden.
- Crea el par interno de ledger ya existente (EXPENSE + INCOME de servicio interno).
- No admite `PlotterPayment`.
- Saldo cobrable = 0.
- El costo de papel atribuible al pedido usa el snapshot histórico del OUT, no la compra del período.

---

## 5. Comportamiento de merma

`WASTE`:

- Consume papel (`Inventory OUT`).
- Sin cliente y sin orden.
- Precio forzado a 0. Sin INCOME. Sin EXPENSE extra. Sin pagos.
- Aparece en analítica (`wasteJobCount`, `wastePrintedMeters`).
- No entra a cobranza externa ni a ingreso generado.

`plotter_jobs.customer_id` es nullable para este tipo. El cambio de esquema ya aplicado no reescribe filas.

---

## 6. Costeo analítico de tinta

La tinta de Plotter → Rentabilidad **no** es un consumo por trabajo.

Es la suma de movimientos de Inventario `PURCHASE` de materiales `INK` en el período seleccionado.

- `inkCostRecorded=true` siempre.
- Sin compras en el período → `COP 0.00`.
- Resultado = ingreso combinado − adquisiciones de papel − adquisiciones de tinta.
- La rentabilidad individual de pedido **no** incluye tinta.
- Consultar el reporte no crea OUT de tinta ni asientos Finance.

---

## 7. Navegación mensual

Los listados operativos de Cotizaciones, Pedidos, Producción y Plotter filtran por mes calendario.

- Sin fechas: el backend conserva el histórico completo (lookups no se recortan).
- Con `fromDate` + `toDate`: filtra por la fecha de negocio del agregado.
  - Cotización: `creationDate`
  - Pedido: `confirmationDate`
  - Producción: `creationDate`
  - Plotter: `creationDate`
- UI reutilizable: `MonthPeriodNavigator` / `monthPeriod.js`.

La rentabilidad de Plotter ya tenía rango de fechas; no se altera esa semántica.

---

## 8. Analítica de comisión de vendedor

Regla V1 (`SellerCommissionPolicy`), sin cambio de fórmula ni de ledger:

- Solo empleados `FIXED_PAYROLL`.
- Pedidos `DELIVERED` o `CLOSED`.
- Comisión = total vendido elegible × 5 %.
- Fecha V1 = `confirmationDate`.
- No crea `FinancialTransaction`.
- No entra a «Generar nómina» ni a «Generar pagos».

Los pedidos reales actuales en `CONFIRMED` no acumulan comisión. Eso es la regla aprobada, no un defecto de cálculo.

---

## 9. Cuentas por cobrar de Home

Home es un módulo de lectura (SPR-037). No posee datos de otros módulos.

- Muestra cuentas por cobrar vigentes y **cuentas por cobrar de pedidos completados**.
- El cliente se muestra por **nombre**, nunca por UUID.
- Solo lectura: abrir Home no crea asientos ni movimientos.
- Solo `ADMIN` puede acceder a `/api/v1/home/**` y a la ruta `/home`.
- `OPERATOR` no ve Home en el menú y no puede entrar por URL.

---

## 10. Comportamiento actual de nómina

Inspección documentada en `docs/design/SPR-039-Payroll-Current-Behavior.md`.

Resumen:

- **Generar pagos** crea ocurrencias `PENDING` de obligaciones recurrentes. No liquida empleados. No crea ledger.
- **Generar nómina** crea `PayrollPeriod` `PENDING` solo para `FIXED_PAYROLL` activos. Pagar el período crea un EXPENSE por el snapshot fijo.
- Comisiones, labores de producción y deducciones permanecen analíticas / paralelas.
- La fórmula unificada salario + producción + comisión − deducciones **no** está implementada.

---

## 11. Límites de autorización

`SecurityConfiguration` V1:

| Recurso | ADMIN | OPERATOR |
|---|---|---|
| Home | Sí | No |
| Administración | Sí | No |
| Finanzas | Sí | No |
| PATCH costo unitario de inventario | Sí | No |
| Reportes / notificaciones admin | Sí | No |
| Comercial, Inventario, Producción, Plotter | Autenticado | Autenticado |

El rol vive en infraestructura de seguridad, no en el dominio de negocio.

---

## 12. Limitaciones conocidas de V1

Estas limitaciones son deliberadas. No se documentan como hechas ni se inician en SPR-040 desde este incremento.

- No hay rediseño visual, responsive, paleta, animaciones ni PDF.
- No hay motor unificado de liquidación de nómina.
- La comisión del 5 % no se paga; es analítica y exige pedido entregado/cerrado.
- Las deducciones no se restan al pagar un período ni una labor.
- La tinta no se consume por trabajo de Plotter.
- El papel de rentabilidad de Plotter es adquisición del período, no consumo OUT.
- Inventario V1 usa el último costo al momento del movimiento. Sin FIFO, sin WAC, sin lotes.
- Crear un trabajo EXTERNAL no genera un pago inicial.
- Home no es un ERP de caja: es un tablero de lectura para ADMIN.
- Los tests de integración usan `@Transactional` y rollback. No hay fixtures persistentes contra datos reales.

---

## Arquitectura

El cierre reutiliza el caso de uso y el puerto de Finance ya existentes.

Presentation (listado / diálogo) → Application (`RegisterPlotterPaymentUseCase`, `GetPlotterProfitabilityUseCase`) → Domain (`PlotterPayment`, saldos) ← Infrastructure (JPA, adaptador de INCOME).

No hay un segundo flujo de pago. La cobranza analítica no escribe el ledger.

---

## Fuera de alcance

- SPR-040
- Pulido visual
- Responsive / colores / animaciones
- PDF
- Reset o reescritura de datos reales
- Generación automática de pagos de Plotter
