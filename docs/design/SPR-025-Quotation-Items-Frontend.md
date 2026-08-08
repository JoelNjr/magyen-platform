# SPR-025 — Quotation Items Frontend

## Objetivo

Implementar la primera versión de la interfaz para administrar los productos de una cotización existente.

Este sprint solamente construye la arquitectura y el flujo de navegación.

No modifica el backend.

No modifica el dominio.

No modifica SQL.

No agrega nuevas APIs.

Se reutilizarán únicamente los endpoints existentes.

---

## Alcance

Frontend únicamente.

React.

Material UI.

Feature Commercial.

---

## Objetivos funcionales

El usuario podrá:

- abrir una cotización existente

- navegar a una pantalla de detalle

- visualizar la información general

- visualizar la sección "Productos"

En este sprint NO se agregarán productos.

Solamente se construirá la arquitectura para soportarlos.

---

## Restricciones

No crear componentes genéricos.

No modificar otros módulos.

No mover carpetas.

No crear Redux.

No usar Context.

No usar TanStack Query.

Mantener arquitectura Feature First.

---

## Arquitectura esperada

Commercial

↓

Quotation List

↓

Quotation Detail

↓

Quotation Items

---

## Resultado esperado

Al finalizar este sprint existirá una pantalla preparada para administrar los productos de una cotización.

La lógica de agregar productos será implementada posteriormente.