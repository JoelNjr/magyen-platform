# SPR-001 – Quotation Domain

**Sprint:** 3

**Estado:** Approved

**Autor:** David Vásquez

---

# Objetivo

Definir el primer modelo de dominio de Magyen Platform.

Este sprint implementará el Aggregate Root del módulo Commercial.

No se implementará persistencia, controladores REST ni lógica de infraestructura.

Únicamente el dominio.

---

# Contexto del Negocio

Una cotización representa una propuesta comercial realizada a un cliente.

La cotización describe qué productos desea fabricar el cliente, bajo qué condiciones comerciales y cuál será el valor económico de la propuesta.

Una cotización es el punto de entrada del proceso operativo de Magyen.

Una vez aprobada podrá convertirse en una Orden de Producción.

---

# Aggregate Root

Quotation

Toda la consistencia del agregado será responsabilidad de Quotation.

Ninguna otra entidad podrá modificar directamente los elementos internos del agregado.

---

# Entidades

## Quotation

Responsabilidades

- Crear una cotización.
- Agregar productos.
- Eliminar productos.
- Calcular el total.
- Cambiar su estado.
- Mantener la consistencia del agregado.

---

## QuotationItem

Representa un producto solicitado por el cliente.

Cada elemento de la cotización representa un producto independiente.

Ejemplos:

- Camiseta
- Pantaloneta
- Sudadera
- Gorra
- Chaqueta

---

# Información de Quotation

- Id
- CustomerId
- CreationDate
- DeliveryDate
- Status
- Salesperson
- Observations
- Items
- Total

---

# Información de QuotationItem

- ProductName
- Quantity
- Fabric
- Color
- UnitPrice
- Subtotal

La tela pertenece al producto y no a la cotización.

Cada producto puede utilizar una tela diferente.

---

# Value Objects

Este sprint utilizará el siguiente Value Object compartido.

Money

Responsabilidades

- Representar valores monetarios.
- Evitar operaciones inválidas.
- Mantener precisión decimal.

El Value Object Money será reutilizado por toda la plataforma.

---

# Enumeraciones

QuotationStatus

Estados iniciales

DRAFT

SENT

APPROVED

REJECTED

EXPIRED

---

# Reglas del Negocio

Una cotización debe tener al menos un producto.

No pueden existir cantidades negativas.

El precio unitario debe ser mayor que cero.

El total siempre será calculado por la cotización.

Los usuarios externos nunca podrán modificar directamente el total.

Una cotización aprobada no podrá volver a estado Draft.

Una cotización rechazada no podrá aprobarse nuevamente.

---

# Responsabilidades

Quotation

Es responsable de mantener la consistencia del agregado.

QuotationItem

Es responsable únicamente de representar un producto cotizado.

Money

Es responsable del comportamiento monetario.

QuotationStatus

Representa el ciclo de vida de la cotización.

---

# Fuera del alcance de este Sprint

Persistencia.

JPA.

REST.

Base de datos.

DTOs.

Repositorios Spring Data.

Servicios externos.

Eventos de Dominio.

Conversión a Orden de Producción.

---

# Resultado esperado

Al finalizar este Sprint existirá el primer modelo de dominio completamente implementado siguiendo Clean Architecture y DDD Ligero.