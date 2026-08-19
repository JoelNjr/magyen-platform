# SPR-039 — V1 RELEASE PHASE 1: documentos PDF

**Incremento:** SPR-039 RELEASE V1 — PHASE 1 — PDF / documentos  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Este documento describe la generación de PDF de V1 para Magyen Platform.

**Alcance exclusivo de esta fase:** cotización y remisión.

**No forma parte de esta fase:**

- rediseño visual de la aplicación web
- trabajo responsive, color, animaciones o pulido estético
- facturas, reportes de producción, reportes financieros, nómina, Plotter u otros documentos
- SPR-040
- despliegue a producción

La base de datos de V1 contiene datos reales de Magyen. Esta fase no resetea la base, no trunca tablas, no recrea volúmenes Docker, no reescribe filas de negocio y no requiere cambios de esquema.

---

## 1. Arquitectura

Los PDF se generan en el backend. El frontend solo solicita y descarga el archivo.

Los valores monetarios salen de las lecturas comerciales existentes. El adaptador de PDF no recalcula totales de negocio.

```text
Presentation (QuotationController / OrderController)
        ↓
Application (GenerateQuotationPdfUseCase / GenerateOrderRemissionPdfUseCase)
        ↓
Lecturas existentes (GetQuotationUseCase / GetOrderUseCase)
        + CustomerNameResolver
        + OrderPaymentCollectionPort
        ↓
CommercialPdfDocumentMapper
        ↓
Puerto CommercialDocumentPdfPort
        ↑
Infrastructure OpenPdfCommercialDocumentAdapter
```

No hay un modelo persistido de “documento”. No se duplican cotizaciones ni órdenes para imprimir. Cada generación refleja el estado actual persistido.

El módulo `home` no interviene. Commercial no usa repositorios JPA de otros módulos: la cobranza de remisión entra por `OrderPaymentCollectionPort`, el mismo puerto de rentabilidad.

---

## 2. Cotización PDF

Acción en el detalle de cotización: **Generar PDF**.

El documento se identifica como **COTIZACIÓN**.

Contenido, cuando existe en el modelo actual:

| Sección | Origen |
|---|---|
| Marca | Texto `MAGYEN · Confecciones Magyen` |
| Título | COTIZACIÓN |
| Número | `QuotationNumberFormat` (`C000001`) |
| Fecha | `creationDate` |
| Entrega estimada | `deliveryDate` |
| Cliente | nombre resuelto con `CustomerNameResolver` |
| Vendedor | `sellerName` |
| Observaciones | `observations` |
| Productos | ítems de la cotización |
| Totales | `unitPrice`, `subtotal` de línea y `totalAmount` del servidor |

Por cada ítem se incluyen, si existen: producto, tipo de prenda, descripción/observación, cantidad, tela principal, tela secundaria, color, cuello, manga, puño (Sí / No), precio unitario y total de línea.

Las tallas **no** existen en el ítem de cotización. El PDF de cotización no las inventa. Las tallas aparecen en la remisión cuando están registradas en la orden.

El cliente de Magyen hoy solo tiene `id` + `name`. No hay NIT, teléfono, correo ni dirección. Esos campos no se muestran.

No se inventan IVA, descuentos, flete ni condiciones de pago.

---

## 3. Remisión PDF

Acción en el detalle de orden comercial: **Generar remisión**.

El documento se identifica como **REMISIÓN**.

No es una factura. El PDF incluye la leyenda:

`Documento de entrega. No es una factura.`

Contenido, cuando existe:

| Sección | Origen |
|---|---|
| Marca | Texto `MAGYEN · Confecciones Magyen` |
| Título | REMISIÓN |
| Pedido | `orderNumber` existente, sin inventar prefijos |
| Descripción | `description` |
| Fecha de confirmación | `confirmationDate` |
| Fecha de entrega | `deliveryCommitment.promisedDeliveryDate` |
| Cliente | `customerName` |
| Vendedor | `sellerName` |
| Productos | snapshot de la orden, con tallas si existen |
| Total | `totalAmount` |
| Total pagado / saldo | `OrderPaymentCollectionPort` (pagos de Finance) |
| Recibido | campos en blanco para firma física |

Campos de confirmación al pie, vacíos, para uso físico:

- Recibido por
- Fecha de entrega
- Firma

No se generan firmas, nombres ni fechas falsas.

---

## 4. Endpoints

| Documento | Método | Ruta |
|---|---|---|
| Cotización | `GET` | `/api/v1/quotations/{quotationId}/pdf` |
| Remisión | `GET` | `/api/v1/orders/{orderId}/remission/pdf` |

Respuesta:

- `Content-Type: application/pdf`
- `Content-Disposition: attachment; filename="…"`
- cuerpo binario del PDF

Una cotización u orden inexistente sigue la semántica comercial vigente: `IllegalArgumentException` → HTTP 400. No se introdujo un 404 nuevo.

---

## 5. Biblioteca PDF

Antes de esta fase el `pom.xml` no tenía biblioteca de PDF.

**Decisión:** [OpenPDF 3.0.5](https://github.com/LibrePDF/OpenPDF) (`com.github.librepdf:openpdf`).

Motivos:

- biblioteca Java de PDF consolidada, compatible con JDK 21
- paquete `org.openpdf.*` (fork activo de iText 2 / LGPL + MPL)
- suficiente para tablas, encabezados repetidos, pie y numeración
- no introduce un framework de reportes

No se usó iText 7/8 comercial, JasperReports, Flying Saucer ni generación en el navegador.

El adaptador vive en:

`backend/src/main/java/com/magyen/platform/commercial/infrastructure/pdf/OpenPdfCommercialDocumentAdapter.java`

---

## 6. Autorización

No hay endpoint público de PDF.

La cadena de seguridad existente aplica:

- sin JWT → HTTP 401
- `ADMIN` y `OPERATOR` autenticados pueden generar cotización y remisión, igual que el resto de Commercial operativo
- no se crearon permisos nuevos

El PDF no incluye contraseñas, JWT, UUID como identificador de negocio, ni datos de infraestructura.

---

## 7. Nombres de archivo

| Documento | Convención |
|---|---|
| Cotización | `Cotizacion-C000001.pdf` |
| Remisión | `Remision-{orderNumber}.pdf` |

Si el número de orden persistido es `1`, el archivo es `Remision-1.pdf`. No se inventa `ORD-0001`.

Si falta el identificador de negocio, se usa `Cotizacion.pdf` o `Remision.pdf`. Nunca el UUID.

---

## 8. Datos ausentes

Política:

- no se inventa información de negocio
- un campo ausente se omite en el detalle de producto
- etiquetas de cabecera/cliente/vendedor usan `No registrado` cuando el valor no existe
- no se muestran UUID
- no se fabrican direcciones, teléfonos, correos, NIT, impuestos ni firmas

El repositorio no contiene un logo Magyen reutilizable (solo `favicon.svg` / `icons.svg` de Cursor). El PDF usa marca tipográfica. No se rediseñó el logo.

---

## 9. Comportamiento multipágina

El documento es A4, con márgenes fijos.

- encabezado repetido: marca + título + identificador de negocio
- pie repetido: `Página N de M`
- la tabla de productos declara `setHeaderRows(1)` para repetir encabezados
- `setSplitLate(false)` para no dejar filas a medias de forma agresiva
- totales y bloque de firma van en tablas `keepTogether`

No se asume una sola página.

---

## 10. Frontend

El frontend no arma el PDF ni recalcula totales.

- Cotización: botón **Generar PDF**
- Orden: botón **Generar remisión**
- `responseType: 'blob'`, timeout 30 s
- descarga con el `filename` de `Content-Disposition`
- el botón se deshabilita mientras genera
- un error de API se muestra en un `Alert` y no rompe el resto de la página

No se añadió un motor de render PDF en el navegador.

---

## 11. Enfoque de pruebas

Los tests no comparan bytes exactos del PDF. Extraen texto con `PdfReader` + `PdfTextExtractor`.

**Cotización (fixtures transaccionales):** HTTP 200, `application/pdf`, PDF no vacío, contiene `COTIZACIÓN`, número de negocio, cliente, vendedor, producto, valores monetarios del servidor, sin UUID como etiqueta.

**Remisión:** HTTP 200, `application/pdf`, PDF no vacío, contiene `REMISIÓN`, número de orden, cliente, producto, tallas, fecha de entrega, total pagado / saldo cuando el puerto de cobranza los entrega, campos de recibido/firma, sin UUID, no se etiqueta como factura.

**Errores:** identificador desconocido → HTTP 400 (semántica comercial existente). Sin autenticación → HTTP 401.

**Multipágina:** adaptador unitario con 28 líneas; verifica más de una página, encabezado y totales.

**Datos reales:** `CommercialDocumentPdfExistingDataReadTest` genera PDF de la primera cotización/orden ya persistida. Solo lectura. No inserta ni borra filas.

Los fixtures de contrato son `@Transactional` y se revierten. No se limpian registros reales a mano.

Algunos tests de Plotter/rentabilidad comparaban `countPlotterIncome() == 0` contra toda la base. V1 ya tiene un ingreso Plotter real (`PLOTTER` / `INCOME`). Esos asserts ahora comparan contra el conteo previo del mismo test para no exigir una base vacía ni borrar datos reales.

---

## 12. Limitaciones conocidas

- No hay logo vectorial de Magyen en el repositorio; la marca es tipográfica.
- El modelo de cliente no tiene datos de contacto ni identificación tributaria.
- La cotización no tiene tallas; solo la orden.
- No hay IVA, descuentos ni condiciones de pago en el modelo de cotización.
- Identificador desconocido responde 400, no 404, porque así funciona hoy Commercial.
- La numeración de páginas se dibuja en el content stream del pie; la extracción de texto puede omitirla según el parser.
- Esta fase no pulió la UI web más allá de los botones de descarga.
- No se implementaron factura, reportes ni SPR-040.

---

## 13. Seguridad de datos

Esta fase no modifica `.env`, credenciales, esquema SQL ni migraciones.

Cualquier generación de PDF es de solo lectura respecto al estado persistido.
