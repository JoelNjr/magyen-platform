# SPR-002 – Create Quotation Use Case

**Sprint:** 4

**Estado:** Approved

**Autor:** Joel David Vásquez

---

# Objetivo

Implementar el primer Caso de Uso de Magyen Platform.

El objetivo es coordinar la creación de una nueva cotización respetando Clean Architecture.

---

# Actor

Asesor Comercial.

---

# Flujo Principal

1. El asesor solicita crear una cotización.

2. El sistema recibe un CreateQuotationCommand.

3. El Use Case valida la información básica.

4. El Use Case solicita al Aggregate Root crear una nueva cotización.

5. El Use Case guarda la cotización mediante QuotationRepository.

6. El Use Case devuelve un CreateQuotationResult.

---

# Responsabilidades

## CreateQuotationUseCase

- Coordinar el proceso.
- Utilizar el Aggregate Root.
- Utilizar el Repository Port.
- Nunca contener reglas del dominio.

---

## CreateQuotationCommand

Representa la información necesaria para crear una cotización.

Contendrá inicialmente:

- CustomerId
- DeliveryDate
- Salesperson
- Observations

---

## CreateQuotationResult

Representa el resultado del caso de uso.

Contendrá inicialmente:

- QuotationId
- Status
- CreationDate

---

# Fuera del alcance

REST.

JPA.

DTOs HTTP.

Persistencia.

Eventos.

Conversión a Orden de Producción.