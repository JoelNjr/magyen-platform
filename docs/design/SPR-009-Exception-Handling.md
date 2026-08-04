# SPR-009 — Global Exception Handling

## Objetivo

Implementar un mecanismo global de manejo de excepciones para toda la API REST de Magyen Platform.

La arquitectura debe garantizar que:

- Presentation traduzca excepciones a respuestas HTTP.
- Application y Domain nunca conozcan HTTP.
- Domain siga lanzando excepciones de negocio.
- Spring sea responsable únicamente del transporte.

---

# Principios

- Clean Architecture
- Dependency Rule
- Single Responsibility
- Fail Fast
- Consistent API Responses

---

# Componentes

## ErrorResponse

DTO de salida para errores HTTP.

Debe contener:

- timestamp
- status
- error
- message
- path

---

## GlobalExceptionHandler

Clase anotada con:

@RestControllerAdvice

Responsable de convertir excepciones en respuestas HTTP.

---

## Excepciones a soportar

### IllegalArgumentException

HTTP 400

---

### QuotationDomainException

HTTP 400

---

### Exception

HTTP 500

Último fallback.

---

## Objetivos del Sprint

- ErrorResponse
- GlobalExceptionHandler
- Manejo de IllegalArgumentException
- Manejo de QuotationDomainException
- Manejo genérico de Exception

---

## Fuera de alcance

Bean Validation

@NotBlank

@NotNull

@NotEmpty

Validation groups

Problem Details (RFC7807)

Logging avanzado

Auditoría

Seguridad