# SPR-029 — Customer Read API & Selection

## 1. Objetivo

Completar la primera integración funcional del concepto Customer con el módulo Commercial.

SPR-028 introdujo Customer como concepto de dominio y persistencia, incluyendo:

- Agregado Customer.
- CustomerRepository.
- Persistencia JPA.
- CustomerEntity.
- Tabla customers.

SPR-029 expone ahora la información de clientes mediante una API de lectura y posteriormente reemplaza el ingreso manual del UUID en la creación de cotizaciones por una selección de cliente basada en datos reales.

El objetivo final es permitir que el usuario seleccione visualmente un cliente mientras el sistema continúa enviando el UUID como `customerId` al endpoint existente de creación de cotizaciones.

---

## 2. Estado actual

Actualmente:

- Customer existe como agregado de dominio.
- Customer existe en PostgreSQL.
- Existe CustomerRepository.
- Existe JpaCustomerRepository.
- No existe API REST de Customer.
- El frontend no tiene servicio de clientes.
- CreateQuotationPage todavía solicita manualmente el UUID.
- QuotationsPage y QuotationDetailPage todavía muestran el UUID del cliente.

El contrato actual de creación de cotizaciones permanece:

```json
{
  "customerId": "UUID",
  "deliveryDate": "YYYY-MM-DD",
  "salesperson": "string",
  "observations": "string"
}

No se modificará este contrato.

3. Alcance

SPR-029 contempla:

Crear el caso de uso de lectura de clientes.
Crear los DTOs de Application necesarios.
Crear los DTOs de Presentation necesarios.
Exponer GET /api/v1/customers.
Probar la lectura de clientes.
Agregar el método getCustomers() al servicio frontend.
Crear un selector de clientes para CreateQuotationPage.
Mantener customerId como valor enviado al backend.
Mejorar la experiencia de selección sin cambiar el contrato de cotizaciones.
4. API de lectura

Se agregará:

GET /api/v1/customers

La respuesta deberá representar los clientes disponibles para selección.

Estructura conceptual:

[
  {
    "customerId": "UUID",
    "name": "Colegio San José"
  }
]

Los nombres exactos de los DTOs deberán seguir las convenciones existentes del proyecto.

5. Arquitectura backend

La lectura seguirá la arquitectura existente:

CustomerController
        ↓
GetCustomersUseCase
        ↓
CustomerRepository
        ↓
JpaCustomerRepository
        ↓
Spring Data JPA
        ↓
customers

El Controller no accederá directamente al repositorio.

El dominio no conocerá Spring, JPA ni HTTP.

6. Aplicación

Se creará un caso de uso de lectura de clientes.

El caso de uso:

obtiene los clientes mediante CustomerRepository;
transforma el agregado a un resultado de Application;
no modifica ningún Customer;
no contiene lógica HTTP.

No se introducirán filtros, paginación ni búsqueda avanzada en este sprint.

7. Presentation

Se creará la respuesta HTTP correspondiente.

El Controller será delgado y delegará la operación al caso de uso.

No se modificará el endpoint existente de cotizaciones.

8. Frontend

Una vez disponible el endpoint:

GET /api/v1/customers

se agregará al commercialService:

getCustomers()

El servicio continuará siendo un wrapper HTTP delgado utilizando el httpClient existente.

No se utilizará:

Redux
Context
React Query
Zustand
nuevo cliente HTTP
9. CustomerSelector

Se incorporará un componente específico del módulo Commercial:

features/commercial/components/CustomerSelector.jsx

El componente deberá:

cargar clientes mediante el servicio proporcionado por la página;
mostrar el nombre del cliente;
mantener el UUID como valor interno;
permitir seleccionar un único cliente;
informar el customerId seleccionado a CreateQuotationPage.

No deberá conocer reglas de creación de cotizaciones.

10. CreateQuotationPage

El campo manual de UUID será reemplazado progresivamente por el selector.

El usuario deberá ver:

Cliente
[ Colegio San José ▼ ]

pero el payload enviado continuará siendo:

{
  "customerId": "UUID_DEL_CLIENTE",
  "deliveryDate": "YYYY-MM-DD",
  "salesperson": "string",
  "observations": "string"
}

No se modificará el contrato del backend.

11. Estado del formulario

CreateQuotationPage continuará utilizando estado local.

El estado deberá contener:

customerId
deliveryDate
salesperson
observations
estado de carga de clientes
estado de envío
errores

No se agregará estado global.

12. Carga de clientes

Mientras los clientes estén cargando:

el selector deberá mostrar un estado de carga;
el usuario no deberá poder seleccionar un cliente inexistente;
el formulario no deberá enviar un customerId vacío.

Si la carga falla:

mostrar un mensaje claro al usuario;
impedir el envío hasta contar con un cliente válido.
13. Lista vacía

Si la API responde correctamente pero no existen clientes:

[]

el selector deberá informar que no existen clientes disponibles.

No se deben crear clientes ficticios desde el frontend.

La creación de clientes queda fuera del alcance de SPR-029.

14. Validación

La selección de cliente será obligatoria.

La validación mínima será:

debe existir un customerId;
debe corresponder a una opción cargada desde la API.

No se realizará todavía validación adicional de existencia desde CreateQuotationUseCase.

15. QuotationsPage y QuotationDetailPage

No se cambiará todavía la estructura de respuesta de las cotizaciones.

Por lo tanto, si las APIs actuales solamente proporcionan:

customerId

las páginas existentes podrán continuar mostrando el UUID.

El enriquecimiento de cotizaciones con nombre de cliente será evaluado posteriormente cuando exista una necesidad concreta y una API adecuada.

16. UX

El selector debe integrarse visualmente con los componentes MUI existentes.

Debe conservar:

estilo del formulario actual;
espaciado;
estados de error;
botones existentes;
comportamiento de Guardar y Cancelar.

No se agregará una librería externa de selección.

17. Restricciones arquitectónicas

No implementar:

creación de clientes;
edición de clientes;
eliminación de clientes;
búsqueda avanzada;
paginación;
filtros complejos;
Redux;
Context;
React Query;
Zustand;
nuevo cliente HTTP;
modificación del contrato POST /quotations;
FK adicional entre customers y quotations/orders;
cambios en el dominio Quotation.
18. Riesgos
Catálogo vacío

Si no existen clientes en PostgreSQL, el selector aparecerá vacío.

Esto no debe resolverse con datos falsos en React.

Datos existentes

Las cotizaciones existentes pueden continuar utilizando UUID que todavía no correspondan a registros de Customer.

SPR-029 no debe modificar esas cotizaciones automáticamente.

Escalabilidad

La primera versión puede utilizar un listado completo de clientes.

La búsqueda, paginación y filtrado pueden agregarse posteriormente cuando el volumen real de clientes lo justifique.

19. Orden de implementación
Incremento 1

Backend: arquitectura del Customer Read API.

Incremento 2

Implementación del caso de uso y DTOs de Application.

Incremento 3

Presentation + Controller + GET /api/v1/customers.

Incremento 4

Integración frontend del servicio y CustomerSelector.

Incremento 5

Integración final con CreateQuotationPage y UX polish.

20. Resultado esperado

Al finalizar SPR-029 el flujo deberá ser:

Usuario
   ↓
CreateQuotationPage
   ↓
CustomerSelector
   ↓
GET /api/v1/customers
   ↓
Selecciona cliente
   ↓
customerId
   ↓
POST /api/v1/quotations

El usuario ya no deberá escribir manualmente UUIDs para crear una cotización.

El backend continuará utilizando UUID como identificador técnico.

21. Definición de terminado

SPR-029 estará terminado cuando:

GET /api/v1/customers funcione correctamente.
La API devuelva clientes reales almacenados en PostgreSQL.
El frontend pueda consumir la API.
CreateQuotationPage permita seleccionar un cliente.
El POST de cotización continúe funcionando con el customerId.
No existan datos ficticios en frontend.
npm run build sea exitoso.
mvnw compile sea exitoso.
Las funcionalidades anteriores de Commercial continúen funcionando.
Todo el trabajo de SPR-029 esté consolidado en un único commit.
22. Fuera de alcance

Customer CRUD completo, edición, eliminación, búsqueda avanzada, paginación, enriquecimiento de listas de cotizaciones y relaciones FK quedan para futuros sprints.

SPR-029 se concentra exclusivamente en lectura y selección de clientes.