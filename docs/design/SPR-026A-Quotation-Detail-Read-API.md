# SPR-026A — API de Consulta de Detalle de Cotización

## Objetivo

Implementar el endpoint de consulta que permita obtener una cotización completa, incluyendo su información general, los productos registrados y el valor total.

Este endpoint será la fuente única de información para la pantalla de detalle de cotización del frontend.

No se desarrollará ninguna funcionalidad del frontend durante este sprint.

---

## Alcance

### Incluye

- Consulta de una cotización por identificador.
- Retorno de la información general de la cotización.
- Retorno del listado completo de productos.
- Retorno del valor total actualizado.
- Caso de uso de consulta.
- DTOs de aplicación.
- DTOs de presentación.
- Endpoint REST.

### No incluye

- Crear cotización.
- Editar cotización.
- Eliminar cotización.
- Agregar productos.
- Editar productos.
- Eliminar productos.

---

## Objetivo funcional

Al finalizar este sprint el backend deberá exponer el endpoint:

GET /api/v1/quotations/{quotationId}

retornando:

- Información general.
- Productos registrados.
- Total de la cotización.

Todo reutilizando el Aggregate existente.

---

## Restricciones arquitectónicas

Mantener la Arquitectura Limpia.

Presentación

↓

Aplicación

↓

Dominio

↓

Repositorio

↓

Infraestructura

No modificar:

- Base de datos.
- Entidades JPA.
- Aggregate.
- Reglas de negocio.

---

## Entregables

- Caso de uso de consulta.
- DTOs de aplicación.
- DTOs de presentación.
- Endpoint REST.
- Validación mediante Postman.

---

## Criterios de aceptación

El frontend podrá consultar una cotización completa mediante una única petición HTTP al backend, obteniendo toda la información necesaria para construir la pantalla de detalle.