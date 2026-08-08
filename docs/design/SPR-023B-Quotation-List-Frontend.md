# SPR-023B — Quotation List Frontend

## Objetivo

Implementar la primera pantalla comercial funcional del frontend para visualizar las cotizaciones existentes.

Este sprint NO crea nuevas reglas de negocio.

NO modifica el backend.

NO modifica SQL.

Consume exclusivamente el endpoint existente:

GET /api/v1/quotations

---

## Objetivos del sprint

Construir la primera pantalla profesional de listado utilizando Material UI.

La información debe provenir únicamente del backend.

Se reutilizará la infraestructura creada en SPR-022:

- Axios
- Proxy Vite
- React Router
- MainLayout
- Feature Commercial

---

## Arquitectura

Presentation

↓

Commercial Service

↓

Axios

↓

Spring Boot

↓

Commercial Module

No lógica de negocio en React.

Toda la lógica permanece en el backend.

---

## Componentes esperados

Feature:

commercial

Pages

Components

Services

La página consumirá el servicio.

El servicio consumirá Axios.

---

## Vista esperada

Título:

Cotizaciones

Debajo:

Tabla Material UI

Columnas:

Número

Cliente

Fecha creación

Fecha entrega

Estado

Vendedor

Total

---

## Estilo

Material UI

Espaciado consistente

Tabla responsive

Cabecera diferenciada

Estados mediante Chips

Moneda formateada

Fechas legibles

Preparado para futuras acciones.

---

## Restricciones

No crear formularios.

No crear edición.

No crear eliminación.

No crear nuevas APIs.

No modificar backend.

No crear estado global.

No Redux.

No TanStack Query todavía.

---

## Resultado esperado

El usuario podrá abrir:

/commercial

y visualizar todas las cotizaciones registradas en la plataforma mediante una tabla profesional.