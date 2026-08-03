# SPR-005 — Presentation Layer Design

## Estado

Approved

---

# Objetivo

Definir la arquitectura de la capa Presentation de Magyen Platform.

La capa Presentation será la única responsable de exponer la aplicación al mundo exterior (HTTP, REST, JSON).

Su responsabilidad es recibir solicitudes, transformarlas en objetos de Application, invocar los casos de uso y devolver respuestas HTTP.

Presentation nunca contendrá reglas de negocio.

---

# Responsabilidades

La capa Presentation deberá:

- Exponer endpoints REST.
- Validar el formato de entrada.
- Convertir Request → Command.
- Invocar casos de uso.
- Convertir Result → Response.
- Traducir excepciones técnicas a respuestas HTTP.

No podrá:

- Implementar reglas de negocio.
- Acceder directamente a la base de datos.
- Instanciar entidades de dominio.
- Manipular JPA.
- Calcular lógica de negocio.

---

# Organización

Cada recurso tendrá su propio módulo Presentation.

Ejemplo:

presentation/

    quotation/

        controller/

        mapper/

        request/

        response/

    customer/

    production/

    inventory/

No existirán carpetas globales de controllers.

Cada recurso encapsula completamente su API.

---

# Controller

Responsabilidad:

Recibir solicitudes HTTP.

Delegar al caso de uso.

Nunca implementar lógica de negocio.

Ejemplo:

POST /api/v1/quotations

↓

CreateQuotationRequest

↓

Presentation Mapper

↓

CreateQuotationCommand

↓

CreateQuotationUseCase

↓

CreateQuotationResult

↓

Presentation Mapper

↓

CreateQuotationResponse

↓

HTTP 201

---

# Request Objects

Representan exactamente los datos enviados por el cliente.

Características:

- Inmutables.
- Sin comportamiento.
- Sin reglas de negocio.
- Solo transporte de datos.

Ejemplo:

CreateQuotationRequest

UpdateQuotationRequest

ApproveQuotationRequest

---

# Response Objects

Representan exclusivamente la respuesta enviada al cliente.

Nunca exponen directamente entidades de dominio.

Ejemplo:

CreateQuotationResponse

QuotationDetailResponse

QuotationSummaryResponse

---

# Presentation Mapper

Responsabilidad exclusiva:

Request

↓

Command

Result

↓

Response

No implementa reglas de negocio.

No accede a infraestructura.

No consulta repositorios.

---

# Dependencias

Presentation puede depender únicamente de:

Application

Spring MVC

Bean Validation

Jackson

Nunca dependerá de:

Infrastructure

Persistence

JPA

Repositories concretos

---

# Flujo Arquitectónico

HTTP

↓

Controller

↓

Presentation Mapper

↓

Application

↓

Domain

↓

Repository Port

↓

Infrastructure

↓

Base de Datos

---

# Validaciones

Presentation valida únicamente:

- Campos requeridos.
- Formato JSON.
- Longitud máxima.
- Email válido.
- UUID válido.
- Fechas válidas.

Las reglas de negocio permanecen en Domain.

Ejemplo:

✔ customerId obligatorio

✔ body válido

✘ fecha de entrega posterior a creación

(Esa regla pertenece al dominio.)

---

# Manejo de errores

Las excepciones del dominio nunca serán expuestas directamente.

Presentation deberá traducirlas mediante un Global Exception Handler.

Ejemplo:

QuotationDomainException

↓

HTTP 400

JSON Error Response

---

# Versionado

Todos los endpoints utilizarán:

/api/v1/

Ejemplo:

POST /api/v1/quotations

GET /api/v1/quotations/{id}

PUT /api/v1/quotations/{id}

---

# Convenciones

Controller

QuotationController

Request

CreateQuotationRequest

Response

CreateQuotationResponse

Mapper

QuotationPresentationMapper

---

# Principios

Presentation será completamente delgada.

Toda la lógica vivirá en Application y Domain.

Presentation solo conecta el exterior con el núcleo del sistema.

---

# Resultado esperado

Una API REST consistente.

Controladores pequeños.

Objetos HTTP independientes del dominio.

Arquitectura alineada con Clean Architecture.

Facilidad para agregar nuevos módulos reutilizando exactamente el mismo patrón.