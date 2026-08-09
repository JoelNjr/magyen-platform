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

# Incremento 2 — Customer Domain y Persistencia

## Objetivo

Introducir Customer como un concepto de dominio real dentro de la plataforma, incluyendo su persistencia y repositorio.

Este incremento no expone todavía ningún endpoint HTTP y no modifica el frontend.

---

## Alcance

Se implementará:

- Agregado `Customer`.
- Identificador UUID.
- Información mínima de identificación del cliente.
- Regla básica de creación válida.
- Entidad JPA `CustomerEntity`.
- Tabla `customers`.
- Repositorio de dominio.
- Adaptador JPA.
- Mapper entre persistencia y dominio.
- Migración/schema SQL necesaria.

---

## Información mínima del cliente

El cliente necesita inicialmente una identidad que pueda ser mostrada al usuario.

El modelo mínimo previsto es:

- `customerId`
- `name`

`name` representa el nombre comercial, nombre de la persona o razón social que permitirá identificar al cliente visualmente.

No se agregarán todavía campos como:

- documento
- teléfono
- dirección
- correo
- ciudad
- estado
- contacto

Estos campos podrán incorporarse posteriormente cuando exista una necesidad funcional concreta.

---

## Identificador

El cliente utilizará UUID como identificador.

Esto mantiene consistencia con:

- Quotation
- Order
- QuotationItem
- demás agregados existentes

El UUID continuará siendo el identificador técnico interno.

La interfaz de usuario posteriormente mostrará `name` en lugar del UUID.

---

## Persistencia

Se agregará una tabla:

```text
customers