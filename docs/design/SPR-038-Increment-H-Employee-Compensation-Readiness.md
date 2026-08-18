# SPR-038 — Incremento H: Compensación de empleados, comisiones y preparación de nómina

**Incremento:** H  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento H. **SPR-039 no se inicia.**

---

## 1. Comisión de vendedor (V1)

Regla:

```
pedidos elegibles (DELIVERED o CLOSED) del vendedor
        ↓
totalSales = suma de Order.getTotal()
        ↓
accumulatedCommission = totalSales × 5 %   (escala 2, HALF_UP)
```

Es un cálculo analítico / de preparación. No se guarda un saldo mutable. No hay tabla de comisiones.

No crea:

* Finance EXPENSE
* Finance INCOME
* pago de nómina
* cambio en `directProfit` de la orden

La base es el valor comercial del pedido, no el dinero cobrado.

---

## 2. Pedidos elegibles

`Order` no tiene DRAFT ni CANCELLED. Una orden nace `CONFIRMED`.

Elegibles para comisión:

* `DELIVERED`
* `CLOSED`

No elegibles:

* `CONFIRMED`
* `IN_PRODUCTION`
* `READY_FOR_DELIVERY`
* cotizaciones (DRAFT u otras) que no se convirtieron en pedido

El filtro de fechas, si se envía, usa `confirmationDate` (única fecha de negocio siempre persistida). `fromDate` y `toDate` van juntos o ambos nulos (todo el historial).

---

## 3. Quién puede ser vendedor

Sin cambio respecto al Incremento E:

* activo + `FIXED_PAYROLL` → aparece en el selector de cotización
* `PRODUCTION_BASED` → nunca vendedor, comisión no aplicable (ceros)
* inactivo → no se selecciona en cotizaciones nuevas; el historial de comisión de pedidos ya entregados/cerrados permanece

---

## 4. Mano de obra y descuentos

Sin cambio de motor:

* PRODUCTION_BASED: PENDING + PAID cuentan; CANCELLED no. Un EXPENSE PAYROLL al pagar labor.
* FIXED_PAYROLL: producción = 0. Puede tener comisión y descuentos.
* Descuentos LOAN / ADVANCE / OTHER, ACTIVE / CANCELLED, sin borrado físico, sin asiento al crear o cancelar. Aplican a ambos tipos.

---

## 5. Resumen del empleado

`GET /api/v1/finance/payroll/employees/{id}/summary`

No muestra salario neto. La UI aclara: «Acumulado / pendiente de liquidación».

FIXED_PAYROLL: tipo sueldo fijo, ventas, total vendido, comisión 5 %, deducciones activas.

PRODUCTION_BASED: tipo pago por producción, generado / pagado / pendiente, deducciones activas.

---

## 6. API

| Método | Ruta |
|---|---|
| GET | `/api/v1/finance/payroll/employees/performance` |
| GET | `/api/v1/finance/payroll/employees/{id}/commissions` |
| GET | `/api/v1/finance/payroll/employees/{id}/summary` |

`production-earnings` y `deductions` se conservan.

Arquitectura:

```
Finance application
  EmployeeSellerCommissionsPort
      ↓
Finance infrastructure adapter
      ↓
Commercial GetSellerCommissionPerformanceUseCase
      ↓
OrderRepository
```

Finance no usa JPA de Commercial. La fórmula vive en `SellerCommissionPolicy`.

ADMIN only (`/api/v1/finance/**`). JWT sin cambios.

---

## 7. Home

Home no agrega comisión ni pagos de producción pendientes.

Motivo: esa información es de preparación de nómina (ADMIN, Finanzas → Empleados). Meterla en Home acoplaría el dashboard operativo a un read model de compensación y duplicaría lo que ya se consulta al pagar gente. OPERATOR no administra Finanzas.

---

## 8. Persistencia

Cero cambios de esquema. Cálculo desde pedidos existentes.

---

## 9. Semántica Finance (sin doble conteo)

| Hecho | Efecto |
|---|---|
| Venta comercial completada | valor de pedido; comisión 5 % analítica |
| Pago de pedido | Finance INCOME (existente) |
| Labor pagada | un EXPENSE PAYROLL |
| Compra de inventario | Inventory IN + EXPENSE |
| Consumo producción / Plotter interno | Inventory OUT + costo de producción / rentabilidad |
| Comisión V1 | ningún asiento |

---

## 10. Limitaciones V1 / diferido a SPR-039+

* Liquidación de nómina (fijo + producción + comisión − descuentos)
* Comisión como EXPENSE
* Impuestos, seguridad social, motor amplio de descuentos
* FIFO/WAC, PDF, notificaciones, Intelligence
* Fecha real de entrega (hoy se usa `confirmationDate` + estado DELIVERED/CLOSED)
