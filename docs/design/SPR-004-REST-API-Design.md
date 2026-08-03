# SPR-004 — REST API Design

**Sprint:** 6 – REST API Layer

**Estado:** Approved

**Autor:** David Vásquez

**Arquitecto:** ChatGPT

---

# 1. Objetivo

Definir los principios de diseño que regirán toda la API REST de Magyen Platform.

Este documento establece las convenciones oficiales para:

- Endpoints
- Versionado
- Requests
- Responses
- Validaciones
- Errores
- HTTP Status Codes
- Organización de Controllers

Todas las APIs futuras deberán cumplir estas reglas.

---

# 2. Filosofía

La API de Magyen Platform debe ser:

- Consistente.
- Predecible.
- Fácil de consumir.
- Fácil de documentar.
- Independiente del Frontend.

La API nunca estará diseñada pensando únicamente en React.

Será una API profesional que podría ser consumida por:

- React
- Aplicaciones móviles
- Aplicaciones de escritorio
- Integraciones futuras
- Inteligencia Artificial
- Clientes externos autorizados

---

# 3. Versionado

Todas las rutas deberán comenzar con:

```
/api/v1
```

Ejemplos:

```
POST   /api/v1/quotations

GET    /api/v1/quotations

GET    /api/v1/quotations/{id}

PUT    /api/v1/quotations/{id}

DELETE /api/v1/quotations/{id}
```

Nunca existirán rutas sin versión.

---

# 4. Convenciones de URLs

Las URLs representan recursos.

No representan acciones.

Correcto:

```
/quotations
```

Incorrecto:

```
/createQuotation
/saveQuotation
/newQuotation
```

Las acciones las determina el método HTTP.

---

# 5. Métodos HTTP

| Método | Uso |
|----------|-----------------------------|
| GET | Consultar |
| POST | Crear |
| PUT | Reemplazar completamente |
| PATCH | Actualización parcial (más adelante) |
| DELETE | Eliminar |

---

# 6. Formato JSON

Toda la comunicación será JSON.

Ejemplo Request:

```json
{
    "customerId": "2fd7...",
    "deliveryDate": "2026-08-20",
    "salesperson": "David",
    "observations": "Cliente nuevo"
}
```

Ejemplo Response:

```json
{
    "quotationId": "7fd9...",
    "status": "DRAFT",
    "creationDate": "2026-08-02"
}
```

---

# 7. DTOs

Los Controllers nunca expondrán el dominio.

Siempre trabajarán mediante DTOs.

Ejemplo:

```
CreateQuotationRequest
```

↓

```
CreateQuotationCommand
```

↓

```
Quotation
```

↓

```
CreateQuotationResult
```

↓

```
CreateQuotationResponse
```

El dominio nunca será serializado directamente.

---

# 8. Controllers

Cada módulo tendrá su propio Controller.

Ejemplo:

```
QuotationController

InventoryController

ProductionController

FinanceController
```

Nunca existirá un Controller gigante.

---

# 9. Validaciones

Las validaciones de formato vivirán en Presentation.

Ejemplo:

- @NotNull
- @NotBlank
- @Future
- @Size

Las reglas de negocio vivirán exclusivamente en Domain.

Ejemplo:

"No se puede aprobar una cotización sin productos."

Eso jamás irá en un Controller.

---

# 10. HTTP Status

| Código | Significado |
|----------|-----------------------------|
| 200 | Consulta exitosa |
| 201 | Recurso creado |
| 204 | Eliminación exitosa |
| 400 | Request inválido |
| 404 | Recurso inexistente |
| 409 | Conflicto de negocio |
| 500 | Error inesperado |

---

# 11. Manejo de errores

Todos los errores tendrán el mismo formato.

Ejemplo:

```json
{
    "timestamp": "2026-08-02T17:15:22",
    "status": 400,
    "error": "Validation Error",
    "message": "Delivery date must not be in the past.",
    "path": "/api/v1/quotations"
}
```

Nunca devolveremos StackTrace al cliente.

---

# 12. Organización de paquetes

```
presentation/

└── quotation/

      ├── controller

      ├── request

      ├── response

      └── mapper
```

Cada módulo mantiene su propia Presentation.

No existirá una carpeta global de controllers.

---

# 13. OpenAPI

Toda la API será documentada automáticamente mediante OpenAPI.

Objetivos:

- Swagger UI
- Documentación automática
- Testing rápido
- Integración sencilla

---

# 14. Principios

La API debe cumplir siempre:

- RESTful.
- Stateless.
- Resource Oriented.
- Versionada.
- Documentada.
- Consistente.

---

# 15. Regla de Oro

Los Controllers solamente coordinan.

Nunca toman decisiones de negocio.

Su responsabilidad será únicamente:

HTTP

↓

DTO

↓

Use Case

↓

DTO

↓

HTTP

Toda decisión pertenece al Dominio.

---

# 16. Definición de Hecho

Este documento se considera completo cuando:

- Todas las rutas siguen REST.
- Todos los módulos usan la misma estructura.
- Ningún Controller conoce reglas de negocio.
- Toda respuesta utiliza JSON.
- Toda API futura sigue este estándar.

A partir de este Sprint, cualquier endpoint implementado que incumpla estas reglas deberá considerarse incorrecto y deberá ser refactorizado antes de integrarse a la rama principal.