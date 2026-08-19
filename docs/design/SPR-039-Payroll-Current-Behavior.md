# SPR-039 — Comportamiento actual de nómina

**Incremento:** SPR-039  
**Estado:** Inspección solamente  
**Fecha:** 19 de agosto de 2026

Este documento describe el comportamiento **actual** de Finanzas → Nómina y de «Generar pagos».  
No implementa un motor nuevo de liquidación. No inicia SPR-040.

---

## 1. Qué hace hoy «Generar pagos»

«Generar pagos» **no** liquida nómina de empleados.

Es la acción de `FinancePage` → `GenerateOccurrencesDialog` → `POST /api/v1/finance/obligation-occurrences/generate`.

Genera **ocurrencias pendientes** de obligaciones financieras recurrentes (arriendo, servicios, créditos, plantillas clasificadas como `PAYROLL`) dentro de un rango de fechas.

- No paga nada.
- No crea `FinancialTransaction`.
- No toca `PayrollEmployee` ni `PayrollPeriod`.
- Es idempotente por `(obligationId, dueDate)`.

La nómina de empleados fijos se genera con otra acción: **«Generar nómina»** (`POST /api/v1/finance/payroll/periods/generate`).

---

## 2. Entidades que crea o modifica

| Acción del usuario | Crea | Modifica | Ledger |
|---|---|---|---|
| Generar pagos | `RecurringFinancialObligationOccurrence` en `PENDING` | Nada | No |
| Marcar ocurrencia pagada | `FinancialTransaction` `EXPENSE` (`sourceType=RECURRING_OBLIGATION`) | Ocurrencia → `PAID` | Sí |
| Cancelar ocurrencia | — | Ocurrencia → `CANCELLED` | No |
| Generar nómina | `PayrollPeriod` en `PENDING` (solo `FIXED_PAYROLL` activos) | Nada | No |
| Pagar nómina | `FinancialTransaction` `EXPENSE` / `PAYROLL` | Período → `PAID` | Sí |
| Cancelar período | — | Período → `CANCELLED` | No |
| Registrar deducción | `PayrollDeduction` `ACTIVE` | Nada | No |
| Ver comisiones / producción / resumen | — | Nada | No |

---

## 3. Cómo funcionan hoy los períodos de nómina

`GeneratePayrollPeriodsUseCase` recorre empleados de nómina y **solo** genera períodos para empleados **activos** con compensación `FIXED_PAYROLL`.

- Frecuencia: quincenal (`BIWEEKLY`, 14 días) alineada a `effectiveFrom`.
- El monto congelado (`amountSnapshot`) es el `fixedAmount` vigente al generar.
- `expectedPaymentDate` se ajusta a día hábil.
- Actualizar la compensación **no** cambia períodos ya generados.
- `PRODUCTION_BASED` se omite (`skippedProductionBased`).

Pagar un período crea **un** gasto de caja por el snapshot fijo. No recalcula salario.

---

## 4. Cómo intervienen las obligaciones recurrentes

`RecurringFinancialObligation` es un agregado independiente de compromisos de la empresa.

- Crear o editar la obligación no crea ocurrencias ni asientos.
- El usuario debe ejecutar «Generar pagos» (o crear una ocurrencia manual).
- El tipo `PAYROLL` en una obligación es solo una **categoría de gasto** al pagar la ocurrencia.
- **No** está enlazado a `PayrollEmployee` ni a `PayrollPeriod`.

Hoy coexisten dos mecanismos paralelos y sin sincronización:

1. Períodos de nómina de empleados fijos.
2. Obligaciones recurrentes, inclusive las etiquetadas `PAYROLL`.

---

## 5. Cómo se relaciona el salario fijo con el empleado

`PayrollEmployee` con `FIXED_PAYROLL` tiene:

- `fixedAmount` — valor quincenal
- `frequency` — siempre `BIWEEKLY`
- `effectiveFrom` / `effectiveTo` — vigencia para generar períodos
- `active` — debe ser verdadero para generar períodos nuevos

Capacidades derivadas:

- `canSell()` — solo `FIXED_PAYROLL`
- `canDoProduction()` — solo `PRODUCTION_BASED`

El salario fijo se congela en `PayrollPeriod.amountSnapshot` al generar y se paga sin recálculo.

---

## 6. Cómo se relaciona hoy la mano de obra de producción con la nómina

La mano de obra es **analítica y de pago unitario**, no parte de «Generar nómina».

Finance lee `ProductionLaborWork` vía `GetPayrollEmployeeProductionEarningsUseCase`.  
El pago real ocurre en Producción, trabajo por trabajo:

`PayProductionLaborWorkUseCase` → un `FinancialTransaction` `EXPENSE` / `PAYROLL` por cada labor.

`GeneratePayrollPeriodsUseCase` no incluye empleados `PRODUCTION_BASED` ni suma labores al período fijo.

---

## 7. Cómo se relacionan hoy las deducciones con el empleado

`PayrollDeduction` registra un compromiso del empleado con Magyen (`LOAN`, `ADVANCE`, `OTHER`).

- Crear o cancelar **no** crea asiento Finance.
- El total activo se muestra en el resumen financiero del empleado.
- **No** se resta al pagar un `PayrollPeriod` ni al pagar una labor de producción.

Son pasivos registrados para una liquidación futura.

---

## 8. Las comisiones de vendedor: ¿se liquidan o son analíticas?

**Permanecen analíticas. No se liquidan.**

Regla V1 (`SellerCommissionPolicy`):

- Solo empleados `FIXED_PAYROLL`
- Pedidos `DELIVERED` o `CLOSED`
- El pedido debe tener vendedor
- Comisión = total vendido elegible × 5 %
- Fecha V1 = `confirmationDate`
- Empleados fijos inactivos conservan el histórico
- `PRODUCTION_BASED` no es vendedor

No crea `FinancialTransaction`, no entra a «Generar nómina» ni a «Generar pagos».

Inspección de datos reales (19 ago 2026): hay 6 empleados `FIXED_PAYROLL` y 4 pedidos, todos en `CONFIRMED`. Por la regla aprobada esos pedidos **no** acumulan comisión. El listado de desempeño debe mostrar a los vendedores fijos con ventas 0, no ocultarlos.

---

## 9. Qué ocurre cuando el usuario genera pagos

1. Elige un rango (por defecto hoy → hoy + 30 días).
2. El backend recorre obligaciones activas.
3. Calcula fechas de vencimiento en el rango.
4. Crea ocurrencias `PENDING` que aún no existen, con el monto esperado congelado.
5. Devuelve conteos: creadas, ya existentes, inactivas omitidas, fuera de vigencia.

El dinero sale de caja solo cuando el usuario **paga** una ocurrencia o un período de nómina de forma explícita.

---

## 10. Qué no está implementado aún

La fórmula futura:

```
salario fijo
+ mano de obra de producción
+ comisión de vendedor
− deducciones
= neto a pagar
```

**no** está implementada en ningún caso de uso de pago.

Tampoco existen:

- Motor unificado de liquidación de nómina
- Comisión como `EXPENSE`
- Aplicación de deducciones al pagar
- Inclusión de producción en la generación de períodos
- Impuestos, seguridad social o motor avanzado de descuentos
- Vínculo entre obligación recurrente tipo `PAYROLL` y `PayrollEmployee`
- Generación automática / programada
- SPR-040 ni el rediseño visual de V1

---

## Fuentes de código

- `GenerateRecurringFinancialObligationOccurrencesUseCase`
- `PayRecurringFinancialObligationOccurrenceUseCase`
- `GeneratePayrollPeriodsUseCase`
- `PayPayrollPeriodUseCase`
- `GetPayrollEmployeeCommissionsUseCase`
- `GetPayrollEmployeeProductionEarningsUseCase`
- `GetPayrollEmployeeFinancialSummaryUseCase`
- `SellerCommissionPolicy`
- `frontend/src/features/finance/components/GenerateOccurrencesDialog.jsx`
- `frontend/src/features/finance/components/PayrollFinanceSection.jsx`
