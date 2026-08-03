# SPR-008 — Create Quotation Integration

## Estado

Approved

---

# Objetivo

Realizar la primera integración completa del caso de uso **Create Quotation**, recorriendo todas las capas de la arquitectura hasta PostgreSQL.

El objetivo principal de este sprint es comprobar que todas las piezas implementadas en los sprints anteriores trabajan conjuntamente sin romper los principios de Clean Architecture.

Este será el primer caso de uso completamente integrado del proyecto.

---

# Alcance

Este sprint incluye:

- revisión completa del flujo
- integración entre Presentation y Application
- integración entre Application y Domain
- integración entre Domain e Infrastructure
- persistencia real en PostgreSQL
- validación del endpoint REST
- primera inserción real en la base de datos

No incluye:

- autenticación
- clientes
- productos
- inventario
- producción
- frontend
- validaciones avanzadas
- manejo global de excepciones

---

# Flujo completo

La integración esperada será:

HTTP Request

↓

QuotationController

↓

QuotationPresentationMapper

↓

CreateQuotationCommand

↓

CreateQuotationUseCase

↓

Quotation

↓

QuotationRepository

↓

JpaQuotationRepository

↓

QuotationPersistenceMapper

↓

SpringDataQuotationJpaRepository

↓

Hibernate

↓

PostgreSQL

Cada componente mantiene una única responsabilidad.

---

# Responsabilidades

## Presentation

Responsable únicamente de HTTP.

No contiene reglas de negocio.

Debe:

- recibir requests
- devolver responses
- convertir DTOs

Nunca debe acceder directamente a Infrastructure.

---

## Application

Coordina el caso de uso.

Debe:

- validar datos básicos
- ejecutar el flujo
- utilizar únicamente Ports

No conoce JPA.

No conoce PostgreSQL.

No conoce Spring Data.

---

## Domain

Representa el negocio.

Debe:

- proteger invariantes
- crear agregados
- mantener reglas de negocio

No conoce frameworks.

---

## Infrastructure

Implementa los Ports.

Debe:

- persistir entidades
- convertir Domain ↔ Persistence
- utilizar JPA

Nunca contiene reglas de negocio.

---

# Objetos involucrados

Request

↓

CreateQuotationRequest

↓

CreateQuotationCommand

↓

Quotation

↓

QuotationEntity

↓

Database

↓

QuotationEntity

↓

Quotation

↓

CreateQuotationResult

↓

CreateQuotationResponse

↓

HTTP Response

Cada capa utiliza únicamente sus propios objetos.

---

# Criterios arquitectónicos

Durante la integración se verificará que:

- ningún DTO llega al dominio
- ninguna entidad JPA sale de Infrastructure
- ningún Repository concreto es conocido por Application
- ningún Controller contiene lógica de negocio
- todos los mappers cumplen una única responsabilidad

---

# Estrategia de pruebas

La integración se validará mediante una petición HTTP real.

El endpoint esperado será:

POST

/api/v1/quotations

La petición recorrerá todas las capas hasta PostgreSQL.

Posteriormente se verificará el contenido almacenado en la base de datos.

---

# Riesgos

Durante la integración podrían aparecer:

- errores de mapeo
- errores de persistencia
- errores de Hibernate
- errores de configuración
- errores de serialización
- errores de UUID

Todos deberán resolverse manteniendo intacta la arquitectura.

---

# Criterios de aceptación

El sprint finalizará cuando:

- el endpoint responda correctamente
- la cotización quede almacenada en PostgreSQL
- la respuesta HTTP sea correcta
- Hibernate no genere errores
- el dominio permanezca limpio
- todas las capas respeten Clean Architecture

---

# Resultado esperado

Al finalizar este sprint Magyen Platform será capaz de registrar la primera cotización real utilizando toda la arquitectura implementada desde el Sprint 1.

Este hito representa la primera funcionalidad completamente operativa del sistema.