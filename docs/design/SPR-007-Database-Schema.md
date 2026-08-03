# SPR-007 — Database Schema

## Estado

Approved

---

# Objetivo

Diseñar el primer esquema relacional de Magyen Platform a partir del modelo de dominio ya implementado.

Este sprint NO busca añadir nuevas reglas de negocio.

Su objetivo es traducir correctamente el Aggregate Root `Quotation` y su entidad hija `QuotationItem` a un esquema PostgreSQL profesional que pueda ser validado por Hibernate sin modificar el dominio.

---

# Alcance

Este sprint incluye únicamente:

- diseño del esquema SQL
- tablas iniciales
- claves primarias
- claves foráneas
- tipos PostgreSQL
- restricciones
- índices básicos
- validación mediante Hibernate

No incluye:

- Flyway
- migraciones
- datos iniciales
- consultas complejas
- optimización de rendimiento

---

# Principios Arquitectónicos

El esquema debe respetar completamente:

- ADR-001
- SAD
- Project Constitution
- Clean Architecture
- DDD Lite

En consecuencia:

- el dominio sigue siendo la fuente de verdad
- PostgreSQL es únicamente la representación persistente
- Hibernate valida el esquema, pero no lo crea
- ningún cambio del esquema puede modificar el modelo de dominio

---

# Aggregate Root

El primer Aggregate persistido será:

Quotation

El agregado contiene:

- información general de la cotización
- colección de QuotationItem

Toda modificación continúa realizándose exclusivamente mediante el Aggregate Root.

---

# Tablas

## quotations

Representa el Aggregate Root.

Campos iniciales:

- id
- customer_id
- creation_date
- delivery_date
- status
- salesperson
- observations
- total_amount

---

## quotation_items

Representa los elementos internos del agregado.

Campos iniciales:

- id
- quotation_id
- product_name
- quantity
- fabric
- color
- unit_price
- subtotal

---

# Relaciones

Existe una relación:

Quotation

1 ------ N

QuotationItem

Mediante:

quotation_items.quotation_id

FK → quotations.id

---

# Claves primarias

Todas las tablas utilizarán:

UUID

como clave primaria.

No se utilizarán IDs autoincrementales.

---

# Claves foráneas

quotation_items

→ quotation_id

referencia:

quotations.id

con integridad referencial.

---

# Tipos PostgreSQL

UUID

→ uuid

Money

→ numeric(19,2)

String

→ varchar

Observations

→ text

LocalDate

→ date

Quantity

→ integer

Status

→ varchar(20)

---

# Restricciones

Se definirán restricciones básicas:

NOT NULL

para todos los campos obligatorios.

Las reglas de negocio continúan viviendo en Domain.

La base de datos únicamente protege la integridad estructural.

---

# Índices iniciales

Inicialmente se crearán índices para:

quotations.status

quotations.customer_id

quotation_items.quotation_id

No se crearán índices adicionales hasta disponer de métricas reales.

---

# Estrategia de creación

Durante este sprint:

el esquema será creado mediante SQL explícito.

Hibernate continuará configurado con:

ddl-auto = validate

Esto garantiza que el dominio nunca delega la creación del esquema al framework.

---

# Convenciones

Tablas

snake_case

Columnas

snake_case

Claves foráneas

nombre_del_padre_id

Restricciones

nombradas explícitamente

---

# Criterios de aceptación

El sprint se considera terminado cuando:

- PostgreSQL contiene las tablas requeridas
- Hibernate valida correctamente el esquema
- Spring Boot inicia sin errores
- el dominio permanece intacto
- ningún @Entity invade Domain
- el endpoint REST puede utilizar la base de datos

---

# Resultado esperado

Al finalizar este sprint tendremos el primer esquema relacional completamente funcional de Magyen Platform.

Este esquema servirá como base para todos los módulos futuros:

- Production
- Inventory
- Finance
- Purchasing
- Reports