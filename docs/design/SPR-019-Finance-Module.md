# SPR-019 — Finance Module

## Objetivo
Implementar el módulo financiero del ERP Magyen siguiendo la misma arquitectura utilizada en Commercial, Orders, Production e Inventory.

El módulo permitirá registrar pagos realizados por los clientes sobre una Orden, calcular automáticamente el saldo pendiente y consultar el historial financiero de cada pedido.

No se implementarán facturas electrónicas, integración bancaria ni contabilidad general en este Sprint.

---

# Reglas de Arquitectura

- Mantener estrictamente Clean Architecture.
- Mantener el mismo estilo de DDD ligero utilizado en los módulos anteriores.
- Todo el dominio debe permanecer libre de Spring.
- La lógica de negocio pertenece únicamente al Aggregate Root.
- Application solamente orquesta.
- Infrastructure adapta.
- Presentation expone HTTP.

---

# Objetivos Funcionales

Implementar:

- Registrar un pago.
- Consultar un pago.
- Consultar los pagos de una Orden.
- Calcular automáticamente el saldo restante.
- Evitar pagos negativos.
- Evitar pagos superiores al saldo pendiente.
- Actualizar automáticamente el estado financiero de la Orden.

---

# Aggregate Root

Payment

---

# Entidades

- Payment

(No existirán entidades hijas en este Sprint.)

---

# Value Objects

Durante el análisis arquitectónico se determinarán los Value Objects necesarios.

---

# Endpoints esperados

POST /api/v1/payments

GET /api/v1/payments/{paymentId}

GET /api/v1/orders/{orderId}/payments

---

# Restricciones

No implementar:

- Facturación electrónica.
- Impuestos.
- Contabilidad.
- Integraciones bancarias.
- Reportes financieros.

Todo eso pertenece a futuros Sprints.

---

# Criterios de aceptación

- Clean Architecture respetada.
- DDD ligero consistente.
- Aggregate Root único.
- Endpoints funcionando E2E.
- PostgreSQL actualizado.
- Pruebas completas en Postman.
- Validaciones de negocio implementadas en Domain.