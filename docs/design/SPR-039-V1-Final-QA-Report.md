# SPR-039 — Informe de QA final y readiness de release V1

**Incremento:** SPR-039 — FINAL V1 QA AND RELEASE READINESS  
**Estado:** Completado  
**Fecha:** 19 de agosto de 2026  
**Decisión:** **RELEASE READY**

Este documento es el pase final de calidad de Magyen Platform V1. No inicia SPR-040. No añade funcionalidad. No cambia reglas de negocio, fórmulas, PDFs, identidad visual, sistema responsive ni esquema de base de datos.

La base de datos de aceptación de Magyen **no se reseteó**, no se truncó y no se reescribieron filas de negocio.

Git: no se hizo commit ni push. El working tree previo (pulido visual de la fase 5) se conservó.

---

## 1. Alcance del QA

Se auditó V1 como QA engineer, dueño de negocio, usuario final e ingeniero de release.

Prioridades cubiertas:

1. Corrección funcional
2. Integridad de datos
3. Integridad financiera
4. Permisos
5. Navegación
6. Comportamiento responsive
7. Generación de PDF
8. Manejo de errores
9. Consistencia de UI
10. Seguridad de regresión

Método:

- Compilación y suite completa de backend contra PostgreSQL local, con rollback transaccional salvo tests de concurrencia que limpian filas sintéticas.
- Lint, tests y build de frontend.
- Inspección de código de autorización, rentabilidad, Plotter, nómina, comisión y navegación mensual.
- Humo de API en vivo (solo lecturas y login) contra datos reales de Magyen.
- Generación real de una cotización PDF y una remisión PDF ya persistidas.
- Conteos de tablas operativas antes y después de los tests.

No se crearon pedidos, pagos, nóminas ni trabajos Plotter persistentes. No se pagó nómina. No se generó nómina ni ocurrencias de obligaciones sobre datos reales.

---

## 2. Resultados de tests

### Backend

Comandos:

```text
.\mvnw.cmd compile
.\mvnw.cmd test
```

| Resultado | Valor |
|---|---|
| Compile | BUILD SUCCESS |
| Tests run | **526** |
| Failures | **0** |
| Errors | **0** |
| Skipped | **0** |
| Tiempo | 48.359 s |
| Maven | BUILD SUCCESS |

La suite cubre autenticación, autorización, comercial, PDF, rentabilidad, producción, inventario, Plotter (EXTERNAL / INTERNAL_MAGYEN / WASTE), finanzas, Home, comisión analítica y listados por mes.

### Frontend

Comandos:

```text
npm run lint
npm run test
npm run build
```

| Comando | Resultado |
|---|---|
| `npm run lint` (oxlint) | pass, sin hallazgos |
| `npm run test` | **29 pass / 0 fail / 0 skipped** |
| `npm run build` (vite) | success |

Tests frontend ejecutados (script `test` del `package.json`):

- home access (`authPresentation.homeAccess.test.js`)
- Plotter payment UX (`plotterJobPresentation.paymentActions.test.js`)
- commercial PDF (`commercialDocumentDownload.test.js`)
- responsive navigation (`responsiveNavigation.test.js`)
- theme (`magyenColors.test.js`, `magyenMotion.test.js`)

El build emite un warning de chunk > 500 kB. No falla el build. Clasificado P4.

---

## 3. Integridad de base de datos

Conteos **antes** y **después** de `mvnw test`. Sin cambios.

| Tabla | Antes | Después |
|---|---|---|
| users | 3 | 3 |
| customers | 19 | 19 |
| quotations | 4 | 4 |
| quotation_items | 6 | 6 |
| orders | 4 | 4 |
| order_items | 6 | 6 |
| payments | 5 | 5 |
| inventory_items | 6 | 6 |
| inventory_movements | 22 | 22 |
| production_orders | 4 | 4 |
| production_material_consumptions | 5 | 5 |
| production_labor_work | 5 | 5 |
| plotter_jobs | 11 | 11 |
| plotter_payments | 1 | 1 |
| payroll_employees | 9 | 9 |
| payroll_deductions | 0 | 0 |
| payroll_periods | 0 | 0 |
| financial_transactions | 28 | 28 |
| recurring_financial_obligations | 5 | 5 |
| recurring_financial_obligation_occurrences | 4 | 4 |

Los tests `@SpringBootTest` + `@Transactional` hicieron rollback. Los tests de concurrencia de inventario (`ConsumeInventoryMaterialConcurrencyTest`, `PaperRollNumberConcurrencyTest`) no son transaccionales: crean filas sintéticas y las borran en `@AfterEach`. No quedaron filas de negocio extra.

**Efecto colateral conocido de la suite (no es corrupción de filas):** las secuencias PostgreSQL `quotation_number_seq`, `material_code_seq` y `paper_roll_number_seq` avanzan con `nextval` y **no** retroceden al hacer rollback/delete. Valores observados al cierre del QA:

| Secuencia | last_value | Identificadores de negocio reales |
|---|---|---|
| `quotation_number_seq` | 499 | Cotizaciones 1–4 |
| `material_code_seq` | 293 | MAT-001 … MAT-005 |
| `paper_roll_number_seq` | 632 | RP-001, RP-002 |

La siguiente cotización/material/rollo nuevos tendrán un número posterior al último de aceptación. Es un hueco de numeración, no pérdida de datos. **No se reseteó ninguna secuencia** (prohibido en este QA).

`.env`, credenciales de bootstrap y esquema permanecieron intactos. `ddl-auto: validate`.

---

## 4. Autenticación y autorización

### Usuarios persistidos (solo lectura)

| Username | Rol | Enabled |
|---|---|---|
| `joimar-admin` | ADMIN | sí |
| `local-admin` | ADMIN | sí |
| `josekiba` | OPERATOR | sí |

### ADMIN — API en vivo (`local-admin`)

Login 200. Token emitido. `/api/v1/auth/me` 200.

| Recurso | HTTP |
|---|---|
| Home dashboard | 200 |
| Finanzas (transacciones y resumen) | 200 |
| Empleados / desempeño vendedores | 200 |
| Administración usuarios | 200 |
| Catálogos administración | 200 |
| Clientes, vendedores, cotizaciones, pedidos | 200 |
| Producción | 200 |
| Inventario | 200 |
| Plotter jobs y rentabilidad | 200 |
| Rentabilidad de pedidos | 200 |

Frontend: menú ADMIN incluye Inicio, Finanzas y Administración. Ruta por defecto `/home`. `AdminOnlyPage` protege Home, Finanzas, Usuarios y Catálogos.

### OPERATOR

Verificado por contrato (`AuthorizationApiContractTest`, 15 casos, 0 fallos) y por tests de presentación frontend.

| Recurso | Comportamiento aprobado |
|---|---|
| Login | permitido |
| Home menú | oculto |
| `/home` directo | redirige a `/commercial` |
| API `/api/v1/home/**` | 403, mensaje `You do not have permission to perform this action.` |
| Finanzas / Administración / Usuarios / Catálogos admin | ocultos en nav; API 403 |
| PATCH costo unitario inventario | 403 |
| Comercial, Producción, Inventario, Plotter | autenticado, 200 |
| PDF cotización / remisión | permitido (ID inexistente → 400 de negocio, no 403) |

No autenticado: 401 `Authentication is required.`  
Login inválido: 401 `Invalid credentials.` Sin stack trace ni SQL.

No se modificaron reglas de autorización: coinciden con SPR-038 / SPR-039 cierre funcional.

---

## 5. Flujo comercial

Datos reales (agosto 2026):

- 19 clientes con nombres de persona/institución (no UUID).
- 4 cotizaciones APPROVED numeradas 1–4 (UI/PDF: `C000001` … `C000004`).
- 4 pedidos CONFIRMED numerados 1–4, cada uno con cotización.
- 4 órdenes de producción COMPLETED (1:1 con pedido).
- Vendedor de la cotización 1: Joel David Vasquez.
- Producto de la cotización 1: Camisetas de voleibol; tela principal Sudáfrica; totales de backend `400.000,00`.

Verificaciones:

- El listado de cotizaciones resuelve el cliente por `customerId` + `GET /customers` (lookups **sin** filtro de mes).
- Pedidos y rentabilidad exponen `customerName`.
- `GET /sellers` devolvió 6 vendedores: exactamente los 6 `FIXED_PAYROLL` activos. Los 3 `PRODUCTION_BASED` no aparecen.
- La UI de nueva cotización filtra `seller.active`. El puerto Commercial rechaza un `FIXED_PAYROLL` inactivo para selección nueva.
- Totales de cotización/pedido vienen del backend.
- Navegación mensual: agosto 2026 tiene 4 cotizaciones; julio 2026 tiene 0. Histórico sin fechas permanece en API.
- Clientes y vendedores no aceptan `fromDate`/`toDate`; crear relaciones no queda recortado por el mes del listado.

No se crearon cotizaciones ni pedidos persistentes en este QA.

---

## 6. Rentabilidad de pedido

Pedido real `1` (Sofía Vergara), lectura:

| Campo | Valor |
|---|---|
| Valor de pedido | 400.000,00 |
| Material | 56.550,00 |
| Mano de obra | 30.000,00 |
| Costo de papel Plotter (snapshot) | 9.333,31 |
| Servicio Plotter interno | 56.000,00 |
| Costo directo total | 142.550,00 |
| Resultado directo | 257.450,00 |
| Margen | 64,36 % |
| Estado | COMPLETE |

`56.550 + 30.000 + 56.000 = 142.550`. El snapshot de papel **no** se suma encima del servicio interno. Confirmado también por `OrderProfitabilityUseCaseTest.includesInternalPlotterServiceCostWithoutDuplicatingPhysicalPaper` y el caso con servicio 60.000 ≠ papel 48.000, donde el costo directo es el servicio.

Home incluye `profitabilitySummary`. El listado `/api/v1/orders/profitability` devolvió 4 pedidos. El backend permanece autoritativo. No se cambiaron fórmulas.

---

## 7. Producción

- 4 órdenes COMPLETED, filtrables por mes (`creationDate`).
- 5 consumos de material (`inventory_movements` OUT / PRODUCTION = 5).
- 5 labores PAID; 0 CANCELLED en datos reales.
- Operarios de labor: solo `PRODUCTION_BASED` activos (`ProductionLaborEmployeeAdapter`).
- `FIXED_PAYROLL` no recibe ganancias de producción (`EmployeeProductionEarningsUseCaseTest.fixedPayrollEmployeeDoesNotExposeProductionEarnings`).
- Labores pagadas: 5 EXPENSE `PAYROLL` por 392.000,00. Coinciden en conteo con las 5 labores PAID. `payroll_periods = 0`: el gasto de labor pagada no es un período de nómina fija.
- Labores canceladas se excluyen en tests. No se pagó labor adicional en QA.

---

## 8. Inventario

Datos reales:

| Código | Rollo | Tipo | Stock |
|---|---|---|---|
| MAT-001 | RP-001 | PAPER | 4,0000 |
| MAT-001 | RP-002 | PAPER | 53,1000 |
| MAT-002 | — | FABRIC | 373,8000 |
| MAT-003 | — | FABRIC | 20,0000 |
| MAT-004 | — | INK | 8,0000 |
| MAT-005 | — | FABRIC | 0,0000 |

- Papel comparte `MAT-001`; rollos `RP-001` / `RP-002`.
- 6 movimientos IN / PURCHASE y 6 EXPENSE `INVENTORY_PURCHASE` (papel 2, materiales 3, tinta 1).
- Consumo OUT no duplica el gasto de compra (11 OUT Plotter + 5 OUT Production; el conteo de compras sigue en 6).
- Idempotencia de compra cubierta por `InventoryPurchaseUseCaseTest`.
- Home expone `inventoryAlerts` y `paperRollAlerts`.
- MAT-005 en 0 es una alerta esperada de stock, no un defecto.

No se alteró inventario real.

---

## 9. Plotter

### Trabajos reales

| Tipo | Cantidad | Metros | Generado |
|---|---|---|---|
| EXTERNAL | 7 | 88,3000 | 792.800,00 |
| INTERNAL_MAGYEN | 4 | 154,6000 | 1.264.400,00 |
| WASTE | 0 en datos reales | — | cubierto por tests |

EXTERNAL: cliente requerido, monto, metros, 1 pago registrado (50.000,00). Saldo externo 742.800,00. `50.000 + 742.800 = 792.800`. Un INCOME `PLOTTER` / `PLOTTER_REVENUE`. Overpayment rechazado por dominio. UX: “Registrar pago” si hay saldo; “Pago completado” si saldo 0.

INTERNAL: 4 trabajos con orden comercial. Ledger:

- EXPENSE `PLOTTER_INTERNAL_EXPENSE` = 1.264.400,00
- INCOME `PLOTTER_INTERNAL_INCOME` = 1.264.400,00
- Neto = 0

Sin `PlotterPayment`. El servicio interno entra a rentabilidad del pedido; el papel físico no se duplica.

WASTE: sin filas reales de aceptación. `PlotterWasteUseCaseTest` verifica: consume papel, sin cliente, sin orden, sin INCOME, sin pago, aparece en analítica.

### Rentabilidad Plotter (1–31 ago 2026, API en vivo)

| Métrica | Valor |
|---|---|
| Combinado generado | 2.057.200,00 |
| Adquisiciones papel | 399.999,00 |
| Adquisiciones tinta | 630.000,00 |
| Resultado | 1.027.201,00 |
| Externo pagado | 50.000,00 |
| Externo pendiente | 742.800,00 |
| Conteos | 7 / 4 / 0 |

`2.057.200 − 399.999 − 630.000 = 1.027.201`. La tinta es analítica por adquisiciones INK del período, no por trabajo. No se cambió el modelo V1.

---

## 10. Finanzas

Lectura del ledger real (28 transacciones):

| Tipo | Origen | Conteo | Suma |
|---|---|---|---|
| INCOME | COMMERCIAL_ORDER | 5 | 4.341.989,00 |
| INCOME | PLOTTER | 1 | 50.000,00 |
| INCOME | PLOTTER_INTERNAL_INCOME | 4 | 1.264.400,00 |
| EXPENSE | PLOTTER_INTERNAL_EXPENSE | 4 | 1.264.400,00 |
| EXPENSE | INVENTORY_PURCHASE | 6 | 5.238.300,00 |
| EXPENSE | PAYROLL | 5 | 392.000,00 |

Home dashboard (ADMIN) expone resumen financiero, cuentas por cobrar vigentes y completadas, compromisos, alertas de inventario y resumen de rentabilidad.

**Generar pagos:** UI y caso de uso crean ocurrencias `PENDING` de obligaciones recurrentes. No crea `FinancialTransaction`. No liquida salario, comisión, producción ni deducciones. El diálogo lo declara explícitamente.

**Generar nómina:** crea `PayrollPeriod` `PENDING` solo para `FIXED_PAYROLL` activos. No crea gasto hasta pagar el período. En datos reales `payroll_periods = 0` y `payroll_deductions = 0`. No se generó ni pagó nómina en este QA.

Comisión 5 %: analítica, `SellerCommissionPolicy`, pedidos `DELIVERED`/`CLOSED`. Los 4 pedidos reales están `CONFIRMED`, por lo que desempeño de vendedores muestra ventas 0 y comisión 0. Eso es la regla aprobada, no un error de cálculo. Solo empleados `FIXED_PAYROLL` (6). No crea asientos Finance.

---

## 11. PDFs

Generados en vivo desde datos reales (GET, sin escritura):

| Documento | Archivo | Resultado visual / textual |
|---|---|---|
| Cotización 1 | `Cotizacion-C000001.pdf` | MAGYEN · Confecciones Magyen; COTIZACIÓN; C000001; cliente Sofía Vergara; vendedor Joel David Vasquez; producto, tela, color, cuello, manga, puño; 10 × $40.000 = $400.000; fechas 01/08/2026 y entrega 06/08/2026. Sin UUID. |
| Remisión pedido 1 | `Remision-1.pdf` | REMISIÓN; “No es una factura”; pedido 1; cliente; vendedor; tallas XS/S/M; productos y totales; total pagado / saldo; Recibido por; Fecha de entrega; Firma. Sin UUID. |

`CommercialDocumentPdfExistingDataReadTest` también pasó contra estos registros. No se rediseñó el PDF. No se observó defecto visual bloqueante en el contenido extraído.

---

## 12. Navegación mensual

UI: `MonthPeriodNavigator` (mes anterior, siguiente, actual, selectores mes/año) en Cotizaciones, Pedidos, Producción y Plotter.

API: filtro `fromDate`+`toDate` en esos listados. Un solo extremo de fecha → 400.

Lookups de clientes y vendedores **no** están recortados por mes. Plotter carga clientes y órdenes de relación sin el mes del listado.

Mes con datos (agosto 2026) y mes vacío (julio 2026) verificados en cotizaciones.

---

## 13. Responsive

No hubo laboratorio de dispositivos físicos en este pase. Se verificó el sistema **ya aprobado**:

| Viewport | Comportamiento esperado (código + tests) |
|---|---|
| 1440, 1280, 1024, 900 | sidebar permanente |
| 768, 600, 480, 390, 375, 360 | drawer temporal |

`responsiveNavigation.test.js` cubre esos anchos. `html`/`body` usan `overflowX: clip`. Las tablas densas hacen scroll horizontal interno. Header compacto, login, diálogos y `PageHeader` apilan acciones en `xs`.

No se cambió la arquitectura responsive.

---

## 14. Identidad visual

Coincide con la identidad aprobada:

| Superficie | Token |
|---|---|
| Header | carbón `#111111` |
| Sidebar | blanco, texto oscuro, indicador oro (`selectedWash` + barra inset 3px) |
| Fondos | blanco / `#F7F6F3` |
| Acento | oro Magyen `#C9A227` |
| Semántica | verde / ámbar / rojo / azul |

No hay hex sueltos fuera de `magyenColors.js`. Logo estático `frontend/public/assets/magyen-logo.png`. Motion centralizado; `prefers-reduced-motion` intacto. Tests de theme y motion: 9 pass.

No se rediseñó nada.

---

## 15. Manejo de errores

| Caso | Resultado |
|---|---|
| Login inválido | 401, `Invalid credentials.`, UI: “Usuario o contraseña incorrectos.” |
| Sin token | 401, `Authentication is required.` |
| OPERATOR en Home/Finance | 403, mensaje de permiso, sin payload de negocio |
| ID mal formado | 400, `Invalid value for parameter '…'` |
| Pedido inexistente | 400, `Order not found: {uuid}` |
| Overpayment Plotter | dominio rechaza |
| Integridad duplicada | mensajes de negocio, no SQL crudo |

`DataIntegrityViolationException` genérico no expone el detalle de PostgreSQL; usa “La operación viola una restricción de integridad de datos.”

Limitación P3: algunos 400/500 pueden incluir UUID o el `getMessage()` técnico (`IllegalArgumentException`, `HttpMessageNotReadableException`, `Exception` genérico). No se vieron stack traces en las respuestas de humo.

---

## 16. Regresión de áreas aprobadas

| Área | Estado |
|---|---|
| Autenticación | Intacta |
| Autorización ADMIN / OPERATOR | Intacta |
| Home (ADMIN, solo lectura) | Intacta |
| Comercial | Intacta |
| Producción | Intacta |
| Inventario | Intacta |
| Plotter | Intacta |
| Finanzas | Intacta |
| Administración | Intacta |
| Rentabilidad pedido / Plotter | Intacta |
| PDFs | Intactos |
| Responsive | Intacto |
| Identidad visual | Intacta |
| Animaciones | Intactas |

---

## 17. Hallazgos

No hay P0 ni P1.

### P3 — Menor (no retrasan V1)

| ID | Hallazgo | Notas |
|---|---|---|
| QA-039-01 | Mensajes de “no encontrado” pueden incluir UUID | Ej. `Order not found: 00000000-0000-4000-8000-000000000001`. Útil para soporte, ruidoso para la familia. |
| QA-039-02 | Mezcla inglés API / español UI | `Invalid credentials.`, `Authentication is required.`, parámetros inválidos en inglés. La UI de login ya traduce el 401. |
| QA-039-03 | Fallback de cliente a UUID si falla el catálogo | `resolveCustomerName` usa `customerId` cuando no hay mapa de nombres. Con clientes cargados se muestran nombres. |
| QA-039-04 | OPERATOR en `/finance` o `/admin/*` ve “Sin permisos” | Home redirige; Finanzas/Admin no. Comportamiento de `AdminOnlyPage` ya aprobado. |
| QA-039-05 | Huecos de numeración por `nextval` de tests | Siguiente C/MAT/RP no continúa 5 / 006 / 003. Las filas reales no se alteraron. No se reseteó la secuencia. |

### P4 — Cosmético

| ID | Hallazgo |
|---|---|
| QA-039-06 | Warning Vite: chunk JS > 500 kB. El build pasa. |
| QA-039-07 | Existe ruta `/intelligence` (no está en el menú). Es leftover; no se desarrolló Inteligencia ni SPR-040. |

### No son defectos

- Pedidos `CONFIRMED` no acumulan comisión 5 % (regla V1).
- No hay trabajos WASTE en datos de aceptación (cubierto por tests).
- `payroll_periods = 0` y `payroll_deductions = 0`: la familia aún no ha usado “Generar nómina”.
- Tinta de Plotter no se asigna por trabajo (modelo analítico aprobado).
- Nómina unificada salario + producción + comisión − deducciones no está en V1.

---

## 18. Correcciones aplicadas

Ninguna.

No se encontraron regresiones P0/P1. Los P3/P4 no se expandieron a desarrollo, conforme a la política de este QA.

---

## 19. Limitaciones conocidas de V1 (deliberadas)

Documentadas en el cierre funcional y vigentes:

- No hay motor unificado de liquidación de nómina.
- La comisión 5 % es analítica; no se paga.
- Las deducciones no se restan al pagar un período.
- Tinta de Plotter = adquisiciones INK del período.
- Papel de rentabilidad Plotter = adquisiciones del período, no consumo OUT.
- Inventario V1 usa último costo al movimiento. Sin FIFO / WAC / lotes.
- Crear un EXTERNAL no genera pago inicial.
- Home es tablero ADMIN de lectura, no un ERP de caja.
- SPR-040, impuestos, seguridad social, notificaciones operativas y nuevos reportes/PDF quedan fuera.

---

## 20. Estado Git al cierre (sin commit, sin push)

Rama: `main` (al inicio de este QA: `18a0c74`, al día con `origin/main`).

Cambios **preexistentes** de la fase de pulido visual (no descartados, no incluidos en un commit de este QA):

```text
 M frontend/src/features/auth/pages/AdminCatalogsPage.jsx
 M frontend/src/features/auth/pages/LoginPage.jsx
 M frontend/src/features/commercial/pages/CustomersPage.jsx
 M frontend/src/features/commercial/pages/OrderProfitabilityPage.jsx
 M frontend/src/features/commercial/pages/OrdersPage.jsx
 M frontend/src/features/commercial/pages/QuotationsPage.jsx
 M frontend/src/features/commercial/pages/SellersPage.jsx
 M frontend/src/features/finance/components/PayrollFinanceSection.jsx
 M frontend/src/features/finance/pages/FinancePage.jsx
 M frontend/src/features/home/components/EmptyState.jsx
 M frontend/src/features/home/components/SectionHeader.jsx
 M frontend/src/features/inventory/pages/InventoryPage.jsx
 M frontend/src/features/plotter/pages/PlotterJobsPage.jsx
 M frontend/src/features/plotter/pages/PlotterProfitabilityPage.jsx
 M frontend/src/features/production/pages/ProductionOrdersPage.jsx
 M frontend/src/layout/MainLayout.jsx
 M frontend/src/layout/PageHeader.jsx
 M frontend/src/theme/appTheme.js
 M frontend/src/theme/magyenColors.js
 M frontend/src/theme/magyenColors.test.js
?? docs/design/SPR-039-V1-Final-Visual-Polish.md
?? docs/design/SPR-039-V1-Final-QA-Report.md
```

Este QA **solo añade** este informe. No hay commit. No hay push.

---

## 21. Recomendación de release

V1 es usable para que la familia Magyen comience operación real, con las limitaciones deliberadas ya documentadas.

Criterios de RELEASE READY:

- [x] Backend tests pasan (526 / 0 / 0 / 0)
- [x] Frontend lint pasa
- [x] Frontend build pasa
- [x] Frontend tests pasan (29 / 0 / 0)
- [x] Sin P0/P1 pendientes
- [x] Integridad de filas de negocio confirmada
- [x] Autenticación funciona
- [x] Autorización funciona
- [x] Integridad financiera confirmada
- [x] PDFs funcionan (cotización y remisión reales abiertas)
- [x] Responsive aceptable (sistema aprobado + tests)
- [x] Flujos de negocio núcleo usables

**RELEASE READY**

Fuera de alcance explícito: SPR-040, V2, inteligencia, notificaciones, motor de liquidación de nómina, impuestos, seguridad social, FIFO/WAC, nuevos reportes, nuevos PDF, nuevos módulos.
