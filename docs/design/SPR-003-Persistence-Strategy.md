# SPR-003 – Persistence Strategy

**Sprint:** 5

**Estado:** Approved

**Autor:** Joel David Vásquez

---

# Objetivo

Definir la estrategia de persistencia para Magyen Platform respetando Clean Architecture y DDD.

Este sprint no implementará JPA.

Únicamente definirá cómo el dominio será persistido.

---

# Principios

La persistencia nunca deberá modificar el diseño del dominio.

El dominio no conocerá JPA.

El dominio no conocerá Hibernate.

El dominio no dependerá de anotaciones de persistencia.

Infrastructure será responsable de adaptar el dominio al modelo relacional.

---

# Estrategia

El Aggregate Root será persistido mediante una entidad JPA equivalente.

El Repository Port continuará viviendo en Domain.

Infrastructure implementará dicho Port.

La conversión entre el dominio y las entidades JPA será responsabilidad exclusiva de Infrastructure.

---

# Decisiones pendientes

- Estrategia para Money.
- Estrategia para UUID.
- Estrategia para QuotationItem.
- Estrategia para relaciones.
- Estrategia para reconstrucción del Aggregate.

Estas decisiones serán resueltas antes de implementar Infrastructure.

---

# Fuera del alcance

Controllers.

REST.

DTOs HTTP.

Casos de uso.

Eventos.

Base de datos física.