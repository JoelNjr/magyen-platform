# Magyen Platform

# ADR-002 - Ciclo comercial de la Orden independiente de Producción

**Estado:** Aprobado

**Fecha:** 23 de septiembre de 2026

**Autor:** Equipo Magyen Platform

---

# Contexto

La Orden comercial y la Orden de Producción conviven, pero no describen el mismo hecho.

Producción responde si el taller ya fabricó. Comercial responde si el pedido fue confirmado, puesto en producción a ojos del negocio, dejado listo para entrega, entregado al cliente y cerrado.

En agosto de 2026 varias órdenes ya estaban operativamente terminadas en producción y seguían `CONFIRMED` en comercial. Copiar el estado de producción, o inventar la fecha de entrega desde el cierre de taller, la fecha prometida o la fecha de un pago, mezclaría dos responsabilidades y reescribiría historia que el negocio todavía no ha declarado.

---

# Decisión

El ciclo comercial es explícito y lo decide un usuario:

`CONFIRMED` → `IN_PRODUCTION` → `READY_FOR_DELIVERY` → `DELIVERED` → `CLOSED`

- Completar o crear producción no cambia el estado comercial.
- Crear producción sigue permitido en `CONFIRMED` e `IN_PRODUCTION`, y queda prohibido desde `READY_FOR_DELIVERY`, `DELIVERED` y `CLOSED`.
- La fecha real de entrega la informa el usuario. No la pone el reloj del servidor, ni la fecha de terminación de producción, ni la fecha prometida, ni la fecha de un pago.
- Esa fecha es obligatoria, no puede ser anterior a la confirmación y no puede ser posterior al día de negocio.
- Repetir la entrega con la misma fecha es idempotente. Otra fecha se rechaza y no sobrescribe la guardada.
- Entregar no crea pago, asiento financiero, comisión ni cambio de producción. Un pedido entregado puede seguir con saldo.
- Cerrar es otro comando. Solo aplica a `DELIVERED` cuando la suma de los pagos ya registrados cubre el total. Si no cubre, no cambia nada. Si cubre, reconoce el pago final ya existente y cierra. No crea un pago ni un asiento.
- Un pago que alcanza el total no cierra la orden.
- `CLOSED` queda fuera de la rentabilidad individual. `DELIVERED` se clasifica por `actualDeliveryDate`; si esa fecha falta en un histórico, el mes usa la fecha prometida solo para clasificar y no la escribe.

---

# Alternativas consideradas

- Derivar el estado comercial del estado de producción. Se descartó: el taller y la entrega al cliente no son el mismo hecho.
- Usar la fecha del servidor como fecha de entrega. Se descartó: impide registrar una entrega histórica y convierte el reloj en dato de negocio.
- Copiar la fecha de terminación de producción, la fecha prometida o la fecha de pago. Se descartó: ninguna de esas fechas es la entrega real.
- Cerrar automáticamente cuando los pagos cubren el total. Se descartó: cobrar y cerrar son decisiones distintas. El cierre sigue siendo un comando.

---

# Consecuencias

Las órdenes históricas no se regularizan solas. Quien conozca la fecha real de entrega recorre el ciclo normal. No hay un endpoint aparte de entrega histórica ni una tabla de auditoría de entregas en esta decisión.

La comisión del vendedor no nace de estas transiciones. Sigue siendo un módulo posterior, con su propia liquidación.
