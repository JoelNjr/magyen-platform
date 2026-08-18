# SPR-038 — Incremento F: Catálogos de Administración e identidad de material de Inventario

**Incremento:** F  
**Estado:** Implemented  
**Fecha:** 17 de agosto de 2026

Este documento cubre únicamente el Incremento F. No inicia el Incremento G ni SPR-039.

---

## 1. Propiedad de catálogos

Administración es dueña de cuatro catálogos configurables:

| Catálogo | Kind interno | Valores iniciales V1 |
|---|---|---|
| Prendas | `GARMENT` | Camiseta, Camiseta tipo polo, Conjunto deportivo, Conjunto de presentación, Pantaloneta, Otro |
| Telas | `FABRIC` | Sudáfrica, Piqué, Hydrotech |
| Cuellos | `COLLAR` | Redondo, En V recto, En V cruzado, Tejido |
| Mangas | `SLEEVE` | Manga corta sisa, Manga corta rangla, Manga larga sisa, Manga larga rangla |

No es un motor genérico de catálogos. El modelo es `AdministrationCatalogEntry` con kind explícito.

El puño permanece boolean (`Sí` / `No`). No es catálogo.

Una tela de catálogo **no** implica stock en Inventario ni un gasto en Finanzas.

---

## 2. Propiedad de API

Gestión (solo ADMIN, `/api/v1/admin/**`):

* `GET /api/v1/admin/catalogs`
* `GET /api/v1/admin/catalogs/{garments|fabrics|collars|sleeves}`
* `POST /api/v1/admin/catalogs/{kind}`
* `PATCH /api/v1/admin/catalogs/{kind}/{id}/activate`
* `PATCH /api/v1/admin/catalogs/{kind}/{id}/deactivate`

No hay DELETE. OPERATOR sobre estas rutas recibe 403.

Consumo comercial (ADMIN y OPERATOR autenticados):

* `GET /api/v1/commercial-catalogs` — solo valores **activos**, más opciones de puño.

No se abre `/api/v1/admin/**` a OPERATOR. El consumo vive en Comercial.

---

## 3. Commercial → Administration (puerto / adaptador)

```
Commercial application
  CommercialCatalogPort
      ↓
Commercial infrastructure
  CommercialCatalogAdapter
      ↓
Administration application
  ListAdministrationCatalogEntriesUseCase
```

Comercial no usa repositorios ni entidades JPA de Administración. No hay FK cruzadas.

Las escrituras nuevas (`AddQuotationItem`, `UpdateOrderItemProductSpecification`) validan nombres activos vía `CommercialCatalogValidator`.

---

## 4. Activo / inactivo

* Activo: aparece en selectores comerciales y puede usarse en registros nuevos.
* Inactivo: permanece en Administración, no se puede seleccionar en altas/ediciones nuevas.
* No se borra la fila.

---

## 5. Compatibilidad histórica

`quotation_items`, `order_items` y el snapshot de especificación de producción siguen guardando **nombres** (etiquetas en español), no UUIDs de catálogo.

La reconstitución no valida el catálogo. Un valor desactivado o ausente del catálogo actual sigue renderizándose con el texto persistido.

No se inventaron mapeos para valores históricos desconocidos.

---

## 6. Dos telas en un producto comercial

Columnas aditivas:

* `quotation_items.secondary_fabric` (nullable)
* `order_items.secondary_fabric` (nullable)

`fabric` sigue siendo la tela principal, obligatoria.

La tela secundaria es opcional. No es un BOM. No consume inventario. Producción sigue eligiendo `InventoryItem` explícito al registrar consumo.

---

## 7. Identidad de material y generación de código

Los tests no transaccionales de concurrencia (rollos de papel y consumo) limpian las filas sintéticas al terminar. No deben quedar leftovers `CNR-*` / `Papel concurrente` en la base viva.

El código de material identifica el **tipo** de material, no el rollo físico.

* Nuevos materiales no-papel: código consecutivo `MAT-001`, `MAT-002`, … (`material_code_seq`).
* El cliente ya no escribe el código.
* Los códigos existentes (`001`, `002`, `CNR-*`, etc.) no se reescriben.

Índice único parcial:

```
UNIQUE (material_code) WHERE paper_roll_number IS NULL
```

Así los no-papel siguen con código único y el papel puede compartir código.

---

## 8. Identidad de rollo de papel

* Cada rollo sigue siendo un `InventoryItem` con `paper_roll_number` único (`RP-001`, `RP-002`, …).
* Todos los rollos **nuevos** reutilizan el código de material del primer rollo de papel existente; si no hay ninguno, se genera un `MAT-###` y se comparte.
* No se unificaron códigos de rollos históricos ya persistidos (incluidos leftovers `CNRP-*` de pruebas de concurrencia).
* Crear papel no pide nombre, precio de venta ni costo por metro. El nombre por defecto es `Papel Plotter`.
* Todo papel nuevo recibe `RP-###` y es un rollo Plotter. Home sigue clasificando la sección de rollos por `plotterPaperRoll` (RP presente); un PAPER histórico sin RP, si existiera, no entra en esa sección.
* El valor de venta de Plotter se captura al registrar el trabajo, no al crear el rollo.

---

## 9. Compra / Finanzas

Crear una entrada de catálogo **no** crea Inventario ni `INVENTORY_PURCHASE`.

Al crear material:

| Tipo | Compra en el alta |
|---|---|
| PAPER | No. Solo el rollo físico y metros iniciales. |
| FABRIC | Sí: metros + costo por metro → un `INVENTORY_PURCHASE` EXPENSE. |
| Otros | Sí: cantidad + costo total → un `INVENTORY_PURCHASE` EXPENSE. |

El flujo de compra Increment B permanece para recepciones posteriores. El `purchaseId` sigue siendo idempotente: un reintento no duplica el gasto.

---

## 10. No-goals V1 (explícitos)

* Incremento G y SPR-039
* Motor genérico de catálogos
* BOM / receta de producto
* Consumo automático de inventario por tela de cotización
* Comisiones, tarifas de mano de obra, liquidación de nómina
* PDFs, notificaciones, Intelligence, animaciones, rediseño responsive
* Rediseño de JWT
* Reset de base de datos o datos reales de Magyen
* Borrado de entradas de catálogo
* Precio de venta fijo de papel en el alta de inventario

---

## 11. Navegación

ADMIN ve:

```
Administración
  - Usuarios
  - Catálogos
```

OPERATOR no ve Administración. Acceso directo: UX «Sin permisos» y API 403. Intelligence no vuelve al sidebar.
