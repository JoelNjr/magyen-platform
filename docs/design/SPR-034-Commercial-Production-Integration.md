# SPR-034 — Integración Comercial → Producción

## 1. Objetivo

Conectar formalmente el módulo Comercial con el módulo Producción para que una Orden Comercial confirmada pueda convertirse en una Orden de Producción real.

La integración debe permitir que Producción reciba la información necesaria del pedido sin depender de consultas improvisadas al módulo Comercial durante la ejecución.

El objetivo principal es establecer una separación clara entre:

- La Orden Comercial: representa lo que el cliente compró y las condiciones comerciales del pedido.
- La Orden de Producción: representa cómo Magyen debe fabricar y ejecutar ese pedido.

La Orden Comercial seguirá siendo la fuente comercial y la Orden de Producción será la fuente operativa de fabricación.

---

## 2. Problema resuelto

Antes de SPR-034 el sistema permitía:

Cliente → Cotización → Aprobación → Orden Comercial

y también trabajar independientemente con:

Orden de Producción → Operaciones → Ejecución

sin una integración formal entre ambos flujos.

SPR-034 estableció la conversión controlada de una Orden Comercial confirmada en una Orden de Producción, conservando un snapshot productivo independiente.

---

## 3. Resultado esperado

El flujo objetivo para V1 será:

Cliente
↓
Cotización
↓
Aprobación
↓
Orden Comercial
↓
Crear Orden de Producción
↓
Planificación
↓
Operaciones
↓
Ejecución
↓
Completada

La creación de la Orden de Producción debe utilizar la información existente en la Orden Comercial.

---

## 4. Regla principal de negocio

Una Orden Comercial podrá tener como máximo una Orden de Producción asociada.

No debe ser posible crear dos Ordenes de Producción para la misma Orden Comercial.

La protección debe existir en más de una capa cuando corresponda:

- Regla de aplicación.
- Restricción de persistencia cuando el modelo lo permita.
- Manejo correcto de errores de concurrencia.

---

## 5. Responsabilidad de cada módulo

### Comercial

Es responsable de:

- Cliente.
- Cotización.
- Orden Comercial.
- Información comercial.
- Precio de venta.
- Cantidad solicitada.
- Condiciones de entrega.
- Información original proporcionada por el cliente.

### Producción

Es responsable de:

- Orden de Producción.
- Planificación.
- Operaciones.
- Ejecución.
- Operadores.
- Fechas reales de producción.
- Estado de fabricación.
- Consumo y ejecución productiva.

Producción no debe modificar la información comercial original.

---

## 6. Información que debe pasar de Comercial a Producción

Al crear una Orden de Producción se debe conservar una representación operativa del pedido.

Por cada OrderItem se debe poder disponer de:

### Identificación del producto

- Nombre del producto.
- Cantidad.

### Especificación del producto

- Tipo de prenda.
- Tipo de cuello.
- Tipo de manga.
- Variante.
- Requiere sublimación.
- Requiere bordado.
- Requiere DTF.
- Incluye nombres.
- Incluye números.
- Incluye logos.
- Notas de decoración.
- Notas de personalización.
- Observaciones del ítem.

### Tallas

Debe conservarse la distribución de tallas registrada en la Orden Comercial.

Ejemplo:

S → 10
M → 15
L → 20
XL → 5

La suma de las tallas debe mantener las reglas existentes de OrderItem.

---

## 7. Snapshot de producción

La información recibida desde Comercial debe convertirse en una representación propia de Producción.

Producción no debe depender permanentemente de que el OrderItem comercial permanezca sin modificaciones para poder ejecutar una producción existente.

La Orden de Producción debe conservar un snapshot de la información relevante para fabricar.

Esto permite que:

- La Orden Comercial mantenga su responsabilidad comercial.
- Producción mantenga su responsabilidad operativa.
- Una modificación posterior de información comercial no destruya el contexto histórico de una producción ya creada.

---

## 8. Identidad

Las identidades técnicas deben mantenerse separadas.

### Orden Comercial

`orderId` UUID.

### Orden de Producción

`productionOrderId` UUID.

La relación entre ambas debe conservar el `orderId` como referencia técnica.

No se debe introducir en este sprint un número comercial de Producción como `OP######` o `P######`.

El tema de numeración de Producción permanece diferido.

---

## 9. Creación de Orden de Producción

La creación debe partir de una Orden Comercial existente y válida.

Antes de crear la Orden de Producción se debe validar:

1. Que la Orden Comercial exista.
2. Que la Orden Comercial esté en un estado válido para producción.
3. Que no exista ya una Orden de Producción asociada.
4. Que la Orden Comercial tenga sus elementos necesarios para producción.
5. Que los datos transferidos respeten las reglas del dominio de Producción.

La creación debe producir una Orden de Producción en estado `CREATED`.

La planificación, inicio y finalización continuarán utilizando el lifecycle existente de SPR-032.

---

## 10. Estado de la Orden Comercial

SPR-034 no debe rediseñar el lifecycle comercial existente.

Inicialmente la integración debe utilizar los estados y contratos actuales.

No se debe introducir en este sprint un sistema nuevo de estados comerciales.

La sincronización automática de estados entre Comercial y Producción queda fuera del alcance inicial.

---

## 11. Operaciones de Producción

Las operaciones existentes de Producción permanecen vigentes:

- CUTTING
- CALENDERING
- SUBLIMATION
- SEWING
- QUALITY_CONTROL

SPR-034 no debe crear automáticamente operaciones arbitrarias basándose únicamente en el tipo de producto.

La definición de operaciones automáticas podrá analizarse posteriormente cuando exista suficiente información de producción.

---

## 12. Materiales y costos

SPR-034 no implementa todavía:

- Consumo de tela.
- Consumo de papel.
- Consumo de DTF.
- Costos de confección.
- Costos de producción.
- Ganancia.
- Margen.
- Descuento automático de inventario.

Estos elementos serán construidos posteriormente en los sprints de Producción, Inventario, Plotter y Finanzas.

SPR-034 solamente debe preparar una base correcta para que dichos módulos puedan consumir la información de Producción posteriormente.

---

## 13. Integración con Inventario

No se debe modificar Inventario en SPR-034.

La Orden de Producción debe quedar preparada para que posteriormente pueda registrar consumos reales de materiales.

---

## 14. Integración con Plotter

No se debe implementar el módulo de Plotter en SPR-034.

El registro de impresiones, metros de papel, cliente, precio y consumo de papel será desarrollado posteriormente.

---

## 15. Integración con Finanzas

No se debe implementar cálculo de costos o utilidad en SPR-034.

La información financiera será construida posteriormente a partir de:

- Orden Comercial.
- Producción.
- Inventario.
- Plotter.
- Gastos.

---

## 16. Frontend esperado

El resultado final del sprint deberá permitir que desde una Orden Comercial exista una acción equivalente a:

**Crear Orden de Producción**

Una vez creada:

- Debe mostrarse que la Orden ya tiene Producción asociada.
- Debe existir una acción para **Ver Orden de Producción**.
- No debe aparecer nuevamente la opción de crear otra Orden de Producción.

La ruta deberá utilizar `productionOrderId` como identidad técnica.

---

## 17. Principios arquitectónicos

SPR-034 debe mantener la arquitectura existente:

- Domain
- Application
- Infrastructure
- Presentation

El módulo Comercial no debe acceder directamente a entidades JPA de Producción.

El módulo Producción no debe acceder directamente a entidades JPA de Comercial.

La comunicación debe realizarse mediante contratos de aplicación y puertos apropiados.

No se deben introducir dependencias innecesarias entre módulos.

---

## 18. Compatibilidad

Debe preservarse:

- Flujo actual de Cotizaciones.
- Aprobación de Cotizaciones.
- Creación de Orden Comercial.
- Una Orden por Cotización.
- ProductSpecification.
- SizeBreakdown.
- Lifecycle actual de ProductionOrder.
- Operaciones de Producción.
- Frontend Comercial existente.
- Frontend Producción existente.

---

## 19. Fuera del alcance

No implementar en SPR-034:

- Numeración comercial de Producción.
- Inventario.
- Consumo de materiales.
- Plotter.
- Finanzas.
- Cálculo de utilidad.
- PDF.
- Login.
- Roles.
- Home/Dashboard.
- Notificaciones.
- Catálogos avanzados.
- Automatización inteligente de operaciones.
- Rediseño del lifecycle comercial.
- Eliminación de funcionalidades existentes.

---

## 20. Resultado esperado del sprint

Al finalizar SPR-034 debe ser posible realizar:

Cliente
→ Cotización
→ Aprobar
→ Crear Orden
→ Crear Orden de Producción
→ Ver Orden de Producción
→ Planificar
→ Iniciar
→ Gestionar operaciones
→ Completar

La Orden de Producción debe conservar el contexto necesario del pedido comercial, incluyendo especificaciones y distribución de tallas.

No debe ser posible generar accidentalmente múltiples Ordenes de Producción para una misma Orden Comercial.

---

## 21. Criterios de aceptación

SPR-034 será considerado completo cuando:

1. Una Orden Comercial válida pueda generar una Orden de Producción.
2. La Orden de Producción quede inicialmente en `CREATED`.
3. La relación Order → ProductionOrder quede persistida.
4. ProductSpecification sea conservada.
5. SizeBreakdown sea conservada.
6. La información productiva sea independiente del estado posterior del OrderItem comercial.
7. Una segunda creación para la misma Orden Comercial sea rechazada.
8. La relación pueda consultarse desde Comercial.
9. Desde Comercial pueda navegarse a Producción.
10. El lifecycle actual de Producción continúe funcionando.
11. Las operaciones existentes continúen funcionando.
12. Los módulos no incluidos en SPR-034 no sufran regresiones.
13. Backend compile y tests pasen.
14. Frontend compile y build pase.
15. Se realice verificación end-to-end antes del cierre.

---

## 22. Orden de implementación ejecutada

Los incrementos entregados fueron:

1. Fundamento de dominio/persistencia del snapshot productivo.
2. Creación de ProductionOrder desde Orden Comercial con captura de snapshot.
3. Frontend Comercial → creación y navegación a Producción.
4. Exposición REST/detail del snapshot y UI de productos a fabricar.
5. Robustez E2E y semántica HTTP 409 para duplicados.
6. Cierre del sprint y verificación de aceptación.

---

## 23. V1

SPR-034 forma parte del núcleo operativo de V1.

La integración Comercial → Producción es necesaria para que Magyen Platform represente el flujo real del negocio.

Los módulos de Inventario, Plotter y Finanzas utilizarán posteriormente la Orden de Producción como una de sus principales fuentes operativas.

---

## 24. Estado

SPR-034 **completado**.

---

## 25. Cierre del sprint

### Frontera arquitectónica

- Comercial permanece dueño de la Orden comercial, precios y lifecycle comercial.
- Producción permanece dueño de la Orden de Producción, operaciones y lifecycle productivo.
- La comunicación Comercial → Producción usa contratos de aplicación (`GetOrderUseCase` / `GetOrderResult`), no entidades JPA cruzadas.
- Tras la creación, Producción trabaja sobre su snapshot propio (`ProductionOrder.items`).

### Propiedad del snapshot

Cada `ProductionItem` conserva:

- productName
- quantity
- ProductSpecification productiva
- SizeBreakdown productiva

El snapshot se captura al crear la ProductionOrder y no se sincroniza con cambios posteriores de Comercial.

### Exposición REST

- `POST /api/v1/production-orders` crea la ProductionOrder con snapshot.
- `GET /api/v1/production-orders/{productionOrderId}` expone `items` con especificación y tallas.
- El listado de Producción permanece sin items (contrato aditivo solo en detalle).

### Integración frontend

- Orden comercial `CONFIRMED` sin ProductionOrder: acción **Crear orden de producción**.
- Con ProductionOrder existente: **Orden de producción creada** + **Ver producción**.
- Navegación a `/production/orders/{productionOrderId}` usando UUID de producción.
- Detalle de Producción muestra **Productos a fabricar**, especificaciones y tallas.

### Protección de duplicados

- Regla de aplicación: `findByOrderId` → `ProductionOrderAlreadyExistsException`.
- Persistencia: `UNIQUE(order_id)` en `production_orders`.
- HTTP: **409 CONFLICT** con mensaje  
  `Ya existe una orden de producción para esta orden comercial.`
- Otras `ProductionDomainException` continúan en **400**.

### Verificación E2E

Cubierta por tests de aplicación, persistencia y contrato API (`ProductionOrderDuplicateAndLifecycleApiContractTest`), incluyendo:

- creación 201 con snapshot
- independencia del snapshot
- lifecycle CREATED → PLANNED → IN_PROGRESS → COMPLETED
- operaciones y rechazos inválidos
- duplicado 409

### Trabajo diferido

Permanece fuera de SPR-034:

- Inventario
- Plotter
- Finanzas
- Costos / utilidad
- Consumo de materiales
- Numeración comercial de Producción
- Generación automática de operaciones
- Motor de scheduling
- PDF
- Home/Dashboard
- Autenticación / autorización
- Sincronización Comercial → Producción
- Edición de tallas en Producción
- Catálogos/enums de producto en Producción