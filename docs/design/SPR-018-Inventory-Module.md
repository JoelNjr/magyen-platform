# SPR-018 – Módulo de Inventario

## Objetivo

Implementar el primer módulo de Inventario del ERP Magyen siguiendo la misma arquitectura utilizada en los módulos Comercial y Producción.

Este sprint introduce el concepto de inventario físico de materiales, permitiendo registrar los insumos utilizados por la empresa para fabricar los pedidos de los clientes.

El objetivo principal es construir la base del inventario para que posteriormente Producción, Compras y demás módulos puedan consumir estos materiales.

---

# Objetivos funcionales

Al finalizar este sprint el sistema deberá permitir:

- Registrar un nuevo material.
- Consultar un material existente.
- Incrementar existencias.
- Disminuir existencias.
- Consultar el stock actual.
- Evitar que el stock sea negativo.
- Mantener toda la lógica de inventario dentro del Dominio.

---

# Alcance del Sprint

Este sprint implementa únicamente el inventario actual.

No incluye:

- Historial de movimientos.
- Kardex.
- Compras.
- Proveedores.
- Costeo.
- Entradas automáticas desde compras.
- Salidas automáticas desde producción.
- Reservas de inventario.

Todo esto será desarrollado en sprints posteriores.

---

# Modelo de Dominio

Durante este sprint existirá un único Aggregate Root.

## InventoryItem

Representa un material físico almacenado dentro de la empresa.

Ejemplos:

- Tela Hydrotech
- Tela Lafayette
- Tela Sudáfrica
- Hilo blanco
- Cierre #5
- Botón metálico
- Vinilo textil
- Papel de sublimación

Cada material tendrá:

- Id
- Código
- Nombre
- Categoría
- Unidad de medida
- Stock disponible
- Stock mínimo
- Estado

---

# Reglas de negocio

El Dominio deberá garantizar las siguientes reglas:

- No se permiten cantidades negativas.
- No se puede disminuir más stock del disponible.
- Las entradas deben ser mayores que cero.
- Las salidas deben ser mayores que cero.
- El código del material debe ser único.
- El nombre no puede estar vacío.
- La unidad de medida no puede estar vacía.
- El stock mínimo no puede ser negativo.

---

# Casos de uso

Durante este sprint se implementarán:

- Crear material.
- Consultar material.
- Incrementar stock.
- Disminuir stock.

---

# API REST

El módulo expondrá la siguiente API:

POST /api/v1/inventory

Crear material.

GET /api/v1/inventory/{inventoryItemId}

Consultar material.

PATCH /api/v1/inventory/{inventoryItemId}/increase-stock

Incrementar existencias.

PATCH /api/v1/inventory/{inventoryItemId}/decrease-stock

Disminuir existencias.

---

# Arquitectura

El módulo seguirá exactamente la misma estructura utilizada en Commercial y Production.

domain

application

presentation

infrastructure

Se respetarán completamente las reglas de Clean Architecture.

---

# Domain

Será responsable de:

- Reglas de negocio.
- Validaciones.
- Estado del inventario.
- Cambios de stock.
- Invariantes del Aggregate.

Nunca dependerá de Spring.

---

# Application

Será responsable de:

- Orquestar los casos de uso.
- Cargar el Aggregate.
- Ejecutar la lógica del Dominio.
- Persistir cambios.
- Construir los Result.

No contendrá reglas de negocio.

---

# Presentation

Será responsable de:

- Endpoints REST.
- Request DTOs.
- Response DTOs.
- Mappers.

Nunca accederá directamente a los repositorios.

---

# Infrastructure

Será responsable de:

- Persistencia JPA.
- Spring Data.
- Adaptadores de repositorio.
- Configuración de Beans.
- Mappers de persistencia.

---

# Persistencia

Se incorporará una nueva tabla.

inventory_items

La tabla almacenará:

- Identificador.
- Código.
- Nombre.
- Categoría.
- Unidad de medida.
- Stock actual.
- Stock mínimo.
- Estado.

Durante este sprint no existirán tablas de movimientos de inventario.

---

# Criterios de aceptación

El sprint se considerará finalizado cuando sea posible:

- Crear un material.
- Consultarlo mediante la API.
- Incrementar el stock.
- Disminuir el stock.
- Validar el flujo completo desde Postman.
- Verificar los cambios directamente en PostgreSQL mediante DBeaver.
- Mantener la arquitectura limpia respetando las responsabilidades entre capas.
- Compilar correctamente mediante Maven.
- Ejecutar correctamente Spring Boot.

---

# Resultado esperado

Al finalizar este sprint el ERP Magyen dispondrá de un módulo de Inventario completamente funcional, preparado para integrarse posteriormente con Compras, Producción, Costos y demás módulos del sistema, manteniendo la misma filosofía de Domain-Driven Design, Clean Architecture y separación de responsabilidades implementada en todo el proyecto.