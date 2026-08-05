# SPR-017 — Production Operations

## Objetivo

Implementar la gestión de Operaciones de Producción dentro de una Production Order.

Cada Production Order podrá contener múltiples Production Operations (CUTTING, CALENDERING, SUBLIMATION, SEWING, QUALITY_CONTROL).

Las operaciones tendrán su propio ciclo de vida independiente:

PENDING
→ IN_PROGRESS
→ COMPLETED

La Production Order seguirá siendo el Aggregate Root y será la única responsable de crear, administrar y coordinar las operaciones.

---

## Reglas de negocio

1. Solo ProductionOrder puede crear ProductionOperation.

2. No pueden existir operaciones duplicadas del mismo tipo dentro de una misma ProductionOrder.

3. Una operación solo puede iniciarse desde PENDING.

4. Una operación solo puede completarse desde IN_PROGRESS.

5. Una operación completada no puede volver a iniciarse.

6. Una operación debe existir antes de poder asignarle un operario.

7. Solo ProductionOrder administra la colección de operaciones.

8. Las operaciones pertenecen exclusivamente a una ProductionOrder.

9. Presentation nunca modifica operaciones directamente.

10. Application únicamente orquesta.

11. Domain contiene todas las reglas de negocio.

---

## Objetivo del Sprint

Permitir:

- Crear operaciones
- Asignar operarios
- Iniciar operaciones
- Finalizar operaciones

sin romper Clean Architecture.