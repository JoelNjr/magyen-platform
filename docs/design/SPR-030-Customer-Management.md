# SPR-030 — Gestión de Clientes

## 1. Identificación

- Sprint: SPR-030
- Módulo: Commercial
- Funcionalidad: Gestión de Clientes
- Estado: Planificado
- Dependencias: SPR-029 — Customer Read and Selection
- Arquitectura: Hexagonal / Ports and Adapters
- Frontend: React + MUI
- Backend: Spring Boot
- Persistencia: PostgreSQL + JPA

---

## 2. Contexto

SPR-029 incorporó el concepto Customer como parte del dominio de Commercial y permitió consultar clientes mediante:

GET /api/v1/customers

También se integró la selección de clientes en la creación de cotizaciones mediante un CustomerSelector.

Actualmente el sistema puede leer clientes, pero no permite crearlos desde la aplicación.

La tabla `customers` existe en PostgreSQL y actualmente contiene los clientes creados mediante preparación de datos.

Esto limita la operación real de Magyen, ya que para registrar un nuevo cliente es necesario modificar directamente la base de datos.

SPR-030 tiene como objetivo incorporar la creación y administración básica de clientes desde la plataforma.

---

## 3. Problema actual

El flujo actual permite:

- Consultar clientes.
- Mostrar el nombre del cliente.
- Seleccionar un cliente al crear una cotización.
- Mantener `customerId` como referencia técnica.

Pero actualmente no permite:

- Crear un cliente desde la aplicación.
- Editar un cliente.
- Eliminar un cliente.
- Consultar una pantalla propia de gestión de clientes.

Como consecuencia, el catálogo de clientes no puede crecer mediante el uso normal de la plataforma.

---

## 4. Objetivo general

Implementar una primera versión de gestión de clientes que permita registrar y administrar clientes desde la plataforma, manteniendo la arquitectura existente y sin alterar el contrato actual de creación de cotizaciones.

---

## 5. Objetivos específicos

1. Implementar la creación de clientes en el backend.
2. Exponer el endpoint REST correspondiente.
3. Implementar los DTOs de Application y Presentation.
4. Mantener el agregado Customer como responsable de sus invariantes.
5. Incorporar el servicio de clientes en el frontend.
6. Crear una interfaz de gestión de clientes.
7. Permitir registrar nuevos clientes desde la plataforma.
8. Actualizar el selector de clientes después de crear un cliente.
9. Mantener `customerId` como valor enviado al crear cotizaciones.
10. Preparar la arquitectura para futuras operaciones de edición y eliminación.

---

## 6. Alcance

SPR-030 contempla inicialmente:

### Backend

- Caso de uso para crear Customer.
- DTO de comando.
- DTO de resultado.
- Endpoint POST `/api/v1/customers`.
- Mapper de Presentation.
- Registro del caso de uso en `CommercialConfiguration`.
- Reutilización de `CustomerRepository.save()` existente.

### Frontend

- Método `createCustomer()` en `commercialService`.
- Pantalla o flujo para crear clientes.
- Formulario de nombre del cliente.
- Validación básica.
- Mensajes de éxito y error.
- Actualización del listado después de crear.

---

## 7. Modelo actual de Customer

El agregado actual contiene:

- `id`: UUID
- `name`: String

El sprint mantendrá inicialmente este modelo mínimo.

No se incorporarán todavía:

- Documento/NIT
- Teléfono
- Correo
- Dirección
- Ciudad
- Contacto
- Estado
- Tipo de cliente

Estos campos podrán incorporarse en futuros sprints cuando exista una necesidad funcional definida.

---

## 8. Contrato propuesto para creación

El endpoint será:

POST `/api/v1/customers`

Request:

{
  "name": "Institución Educativa San José"
}

Response exitosa:

{
  "customerId": "UUID",
  "name": "Institución Educativa San José"
}

El contrato deberá mantener la separación entre Application y Presentation.

---

## 9. Arquitectura

El flujo esperado será:

Frontend
→ commercialService
→ Axios / httpClient
→ CustomerController
→ CreateCustomerUseCase
→ CustomerRepository
→ JpaCustomerRepository
→ PostgreSQL

La creación no deberá comunicarse directamente con JPA desde el frontend ni desde Presentation.

---

## 10. Integración con cotizaciones

La creación de clientes no modificará el contrato existente:

POST `/api/v1/quotations`

Continuará recibiendo:

{
  "customerId": "UUID",
  "deliveryDate": "YYYY-MM-DD",
  "salesperson": "string",
  "observations": "string"
}

El selector continuará mostrando:

Institución Educativa San José

pero enviará:

customerId

al backend.

Después de crear un nuevo cliente, el frontend deberá poder actualizar la colección utilizada por `CustomerSelector`.

---

## 11. UX esperada

La gestión de clientes deberá seguir los patrones visuales existentes en Commercial:

- MUI.
- Layout existente.
- Snackbar para operaciones exitosas.
- Alert para errores de formulario.
- Skeleton cuando corresponda.
- Formularios simples y claros.
- Sin nuevas librerías.
- Sin Redux.
- Sin Context.
- Sin React Query.

El usuario debe poder entender claramente:

- dónde se encuentran los clientes;
- cómo crear un cliente;
- si la creación fue exitosa;
- qué cliente acaba de registrar.

---

## 12. Alcance incremental

SPR-030 se desarrollará en incrementos.

### Incremento 1

Revisión arquitectónica del Customer actual y definición del flujo de creación.

### Incremento 2

Backend Application + Domain integration para Create Customer.

### Incremento 3

Backend Presentation + endpoint POST `/api/v1/customers`.

### Incremento 4

Frontend service + interfaz de creación de clientes.

### Incremento 5

Integración final, actualización del selector y UX polish.

---

## 13. Fuera de alcance

No se implementará en SPR-030:

- Búsqueda avanzada.
- Paginación.
- Filtros avanzados.
- Importación masiva.
- Exportación.
- Gestión de contactos.
- NIT/documentos.
- Clientes inactivos.
- Historial de clientes.
- Relaciones FK nuevas.
- Cambio del contrato de cotizaciones.
- Numeración consecutiva de cotizaciones.
- Módulo completo de CRM.

La numeración consecutiva de cotizaciones será analizada como una funcionalidad independiente en un sprint posterior.

---

## 14. Reglas arquitectónicas

- Mantener arquitectura hexagonal.
- No acceder directamente a JPA desde Application.
- No colocar reglas de negocio en Controller.
- Reutilizar `CustomerRepository`.
- Mantener Customer como agregado.
- Mantener separación Application / Presentation.
- No modificar el contrato de creación de cotizaciones.
- No introducir librerías innecesarias.
- Mantener el patrón Page → Service → Axios → API en frontend.

---

## 15. Riesgos

### Duplicación de clientes

Dos clientes podrían registrarse con el mismo nombre.

Para este sprint no se implementará una regla de unicidad por nombre sin una decisión de negocio explícita.

### Integración con selector

Después de crear un cliente, el selector debe recibir el nuevo cliente sin requerir una recarga completa de la aplicación.

### Compatibilidad

Las cotizaciones existentes deben continuar funcionando con sus `customerId`.

---

## 16. Criterios de aceptación

SPR-030 será considerado terminado cuando:

1. Se pueda crear un cliente desde la aplicación.
2. El cliente quede persistido en PostgreSQL.
3. `GET /api/v1/customers` lo devuelva.
4. El selector pueda mostrarlo.
5. Una nueva cotización pueda utilizarlo.
6. El POST de cotización continúe enviando solamente `customerId`.
7. Los clientes existentes continúen funcionando.
8. El backend compile correctamente.
9. El frontend compile correctamente.
10. No existan regresiones en Commercial.
11. Customer CRUD adicional no se implemente fuera del alcance definido.
12. La numeración de cotizaciones permanezca fuera de este sprint.

---

## 17. Resultado esperado

Al finalizar SPR-030, Magyen Platform deberá permitir pasar de:

Base de datos
→ crear cliente manualmente
→ utilizar cliente en cotización

a:

Plataforma
→ crear cliente
→ cliente aparece en catálogo
→ seleccionar cliente
→ crear cotización

Esto constituye la primera capacidad real de gestión del catálogo de clientes dentro de Commercial.

---

## 18. Fuera de alcance futuro

Las siguientes funcionalidades quedan preparadas para futuros sprints:

- Edición de clientes.
- Eliminación de clientes.
- Búsqueda.
- Paginación.
- Datos adicionales del cliente.
- Validación de duplicados.
- Clientes activos/inactivos.
- Historial.
- Numeración consecutiva de cotizaciones.

---

## 19. Dependencias

SPR-030 depende de SPR-029 porque reutiliza:

- Customer aggregate.
- CustomerRepository.
- Customer persistence.
- GET `/api/v1/customers`.
- CustomerSelector.
- commercialService.
- Integración existente con CreateQuotationPage.

---

## 20. Definición de terminado

El sprint se considera terminado cuando:

- Backend implementado.
- Frontend integrado.
- Cliente persistido.
- Cliente visible en catálogo.
- Cliente seleccionable en cotización.
- Pruebas manuales completadas.
- Build backend exitoso.
- Build frontend exitoso.
- Commit realizado.
- Documentación del sprint actualizada.