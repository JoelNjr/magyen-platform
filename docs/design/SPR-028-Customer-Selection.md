# SPR-028 — Selección de Clientes en Cotizaciones

## Objetivo

Mejorar la experiencia de creación de cotizaciones permitiendo seleccionar un cliente de forma amigable, evitando que el usuario tenga que introducir manualmente un UUID.

Actualmente el formulario de creación de cotizaciones utiliza `customerId` como un campo de texto y requiere un UUID válido.

El objetivo de este sprint es analizar e implementar, de forma incremental y alineada con la arquitectura existente, una experiencia de selección de clientes basada en los recursos que ya existan en el backend.

---

## 1. Problema actual

El formulario:

`/commercial/new`

actualmente solicita:

- Cliente
- Fecha de entrega
- Vendedor
- Observaciones

El campo Cliente actualmente representa directamente `customerId`.

Por ejemplo:

```text
11111111-1111-1111-1111-111111111111