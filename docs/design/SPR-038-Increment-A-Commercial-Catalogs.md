# SPR-038 — Incremento A: Catálogos comerciales, UX de órdenes y cobros de pedidos completados

**Incremento:** A  
**Estado:** Implemented  
**Fecha:** 16 de agosto de 2026

Este documento cubre únicamente el Incremento A. No describe autenticación (ver `SPR-038-Auth-Security.md`) ni los incrementos B/C/D/E/F.

---

## 1. Objetivo

Hacer que Comercial sea operable por usuarios de negocio con datos controlados:

* catálogos de prenda, tela, cuello, manga y puño;
* descripción humana del pedido, distinta del número de orden;
* origen de cotización mostrado como identificador comercial (`C000001`);
* navegación lateral por Cotizaciones y Órdenes, sin el ítem genérico «Comercial»;
* visibilidad en Home del dinero pendiente de pedidos ya entregados o cerrados.

No implementa consumo de inventario, compras, unificación de nómina, ni atribución interna de Plotter.

---

## 2. Catálogos comerciales

En el Incremento A los catálogos vivían en el dominio Commercial como enums. Desde el Incremento F la fuente autoritativa es Administración → Catálogos (prendas, telas, cuellos, mangas). Comercial consume valores activos vía puerto. El puño sigue siendo boolean. Ver `SPR-038-Increment-F-Administration-Catalogs-Inventory-Identity.md`.

Se persiste la etiqueta de negocio, no un UUID de catálogo. La reconstitución histórica no valida, para no romper pedidos anteriores.

| Catálogo | Valores iniciales V1 |
|---|---|
| Tipo de prenda | Camiseta, Camiseta tipo polo, Conjunto deportivo, Conjunto de presentación, Pantaloneta, Otro |
| Tipo de cuello | Redondo, En V recto, En V cruzado, Tejido |
| Tipo de manga | Manga corta sisa, Manga corta rangla, Manga larga sisa, Manga larga rangla |
| Tela | Sudáfrica, Piqué, Hydrotech |
| Lleva puño | Sí / No (`cuffRequired` boolean) |

La tela es un catálogo comercial independiente del stock de Inventario. Cotizar Sudáfrica con 0 metros es válido. El color sigue siendo texto libre con la etiqueta «Color de tela / base».

El frontend no duplica las etiquetas: las obtiene de `GET /api/v1/commercial-catalogs`.

La columna histórica `garment_variant` permanece en base de datos sin mapeo JPA, para no nular datos existentes.

---

## 3. Descripción del pedido

`Order.description` es un campo nuevo, distinto de `orderNumber` y de `observations`.

* El número de orden sigue siendo el identificador comercial.
* La descripción explica de qué se trata el pedido.
* Se captura al crear la orden desde cotización y se muestra en listado, detalle, producción y Home.

---

## 4. Identificador de cotización de origen

La orden conserva `quotationId` (UUID) para navegación interna.

La API de detalle y listado expone además:

* `quotationNumber` (consecutivo persistido, SPR-031);
* `quotationNumberDisplay` (`C` + 6 dígitos, p. ej. `C000001`).

La UI muestra el display comercial, nunca el UUID como etiqueta primaria.

---

## 5. Navegación lateral

Estructura V1:

* Inicio
* Cotizaciones (`/commercial`), con Clientes y Vendedores anidados
* Órdenes (`/commercial/orders`)
* Producción
* Inventario
* Plotter
* Finanzas
* Administración (ADMIN): Usuarios y Catálogos

`/commercial/orders` no se marca como Cotizaciones. Las rutas existentes se conservan. Intelligence no está en el sidebar. Desde Incremento F, Usuarios deja de ser un ítem de primer nivel.

---

## 6. Home — dinero por cobrar de pedidos completados

Nueva proyección de solo lectura, separada de las cuentas por cobrar genéricas.

Incluye órdenes `DELIVERED` o `CLOSED` con `outstandingAmount > 0`.

Fuente de verdad:

* total del pedido → Commercial Order;
* recaudado → `OrderPaymentCollectionPort` de Finance;
* pendiente → total − recaudado.

Home no escribe en Finance ni crea transacciones. Ordenamiento: pendiente DESC, fecha de entrega prometida DESC, número de orden.

La fila navega al detalle de la orden comercial existente.

---

## 7. UX de orden comercial

En el detalle, «Crear orden de producción» queda al final, después de cliente, vendedor, número, descripción, productos, tallas, especificaciones y pagos.

La especificación ya no muestra «Variante»; muestra «Lleva puño».

---

## 8. Esquema

Cambios aditivos (ver `backend/src/main/resources/db/manual/SPR-038-increment-a-catalogs-and-order-description.sql`):

* `orders.description varchar(2000) NULL`
* `quotation_items.cuff_required boolean NULL`
* `order_items.cuff_required boolean NULL`
* `production_items.cuff_required boolean NULL`

`schema.sql` refleja el mismo esquema. No se recrean volúmenes ni se truncan datos.

---

## 9. Fuera de alcance (Incrementos B+)

* Consumo automático de inventario
* Compras de inventario y gasto financiero asociado
* Unificación de empleados / nómina
* Migración de operarios
* Trabajos internos de Plotter y sus costos
* Comisiones, PDF, notificaciones, Intelligence
