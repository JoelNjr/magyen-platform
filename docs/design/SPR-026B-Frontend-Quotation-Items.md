# SPR-026B — Frontend de Productos de Cotización

## Objetivo

Permitir agregar productos a una cotización desde la pantalla de detalle consumiendo los endpoints existentes del backend.

Toda la información deberá provenir del backend como única fuente de verdad.

---

## Alcance

### Incluye

- Diálogo para agregar producto.
- Formulario controlado.
- Conexión con el endpoint POST existente.
- Recarga automática de la cotización.
- Visualización del listado de productos.
- Actualización automática de subtotal y total.

### No incluye

- Editar productos.
- Eliminar productos.
- Catálogo de productos.
- Catálogo de clientes.
- Descuentos.
- Impuestos.
- Inventario.
- Producción.

---

## Objetivo funcional

El usuario podrá:

1. Abrir una cotización.
2. Agregar un producto.
3. Guardarlo.
4. Visualizar inmediatamente el producto.
5. Ver el subtotal.
6. Ver el total actualizado.

Toda la información será consultada nuevamente desde el backend después de cada inserción.

---

## Restricciones arquitectónicas

Mantener la arquitectura existente.

QuotationDetailPage

↓

CommercialService

↓

Axios

↓

Spring Boot

No introducir:

- Redux
- Context API
- React Query
- Estado local permanente para productos
- Cálculo de totales en React

El backend seguirá siendo la única fuente de verdad.

---

## Entregables

- Servicio para agregar productos.
- Servicio para consultar detalle.
- Diálogo de creación.
- Tabla de productos.
- Actualización automática del detalle.

---

## Criterios de aceptación

Después de agregar un producto, el usuario deberá visualizar inmediatamente la cotización completamente actualizada sin necesidad de recargar manualmente la página.