# SPR-006 — Database Configuration

## Objetivo

Configurar PostgreSQL como base de datos principal de Magyen Platform utilizando Spring Boot y Spring Data JPA, manteniendo la arquitectura Clean Architecture definida por ADR-001.

---

## Alcance

Este sprint habilita la conexión real entre:

Presentation
↓

Application
↓

Domain
↓

Infrastructure
↓

PostgreSQL

---

## Objetivos

- Configurar DataSource.
- Configurar PostgreSQL.
- Configurar Spring Data JPA.
- Configurar Hibernate.
- Mantener Domain independiente.
- Mantener Application independiente.
- No agregar lógica de negocio.

---

## Incrementos

### Incremento 1

Configurar dependencias Maven.

### Incremento 2

Configurar application.yml.

### Incremento 3

Crear docker-compose para PostgreSQL.

### Incremento 4

Levantar la base de datos.

### Incremento 5

Probar la conexión desde Spring Boot.

---

## Restricciones

No modificar:

- Domain
- Application

No agregar reglas de negocio.

Toda configuración pertenece a Infrastructure.