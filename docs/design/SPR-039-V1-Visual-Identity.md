# SPR-039 — V1 RELEASE PHASE 3: identidad visual y sistema de color Magyen

**Incremento:** SPR-039 RELEASE V1 — PHASE 3 — Visual identity  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Esta fase transforma la apariencia genérica de MUI en la identidad visual de **Magyen / Confecciones Magyen**.

**No forma parte de esta fase:**

- cambios de flujos de negocio, APIs, base de datos o permisos
- cambios de cálculos (órdenes, rentabilidad, inventario, Plotter, finanzas, nómina)
- cambios de PDF (COTIZACIÓN / REMISIÓN)
- animaciones
- SPR-040
- despliegue

La fase 2 (responsive) permanece vigente. Esta fase solo añade color, marca y consistencia de componentes.

---

## 1. Logo

**Asset exacto del propietario, sin redibujar ni recolorear:**

`frontend/public/assets/magyen-logo.png`

Ruta pública: `/assets/magyen-logo.png`

Uso:

- pantalla de login (identidad principal)
- header de la aplicación
- sidebar / drawer
- favicon (`frontend/index.html`)

No se replica el logo en cada página de negocio.

El archivo original `frontend/public/assets/logo magyen.PNG` se conserva como copia del propietario; la aplicación usa `magyen-logo.png`.

---

## 2. Paleta

Balance orientativo: ~80% blanco / neutros, ~15% negro carbón, ~5% oro Magyen.

El oro es acento de marca, no fondo de la aplicación.

| Token | Hex | Uso |
|---|---|---|
| `primary` | `#C9A227` | acento Magyen gold |
| `primaryHover` | `#B8911F` | hover de acciones primarias |
| `primaryActive` | `#A07E18` | estado activo / pressed |
| `secondary` | `#1A1A1A` | carbón (header, sidebar, contraste) |
| `background` | `#F7F6F3` | fondo de aplicación |
| `surface` | `#FFFFFF` | tarjetas, papeles, campos |
| `surfaceMuted` | `#F3F1EC` | encabezados de tabla, superficies suaves |
| `textPrimary` | `#1A1A1A` | texto principal |
| `textSecondary` | `#5C5C5C` | texto de apoyo |
| `border` | `#E4E1D8` | bordes y separadores |
| `success` | `#2E7D32` | estados positivos |
| `warning` | `#ED6C02` | alerta / pendiente / merma |
| `error` | `#C62828` | error / cancelado / gasto |
| `info` | `#1565C0` | información / externo |

Definición central: `frontend/src/theme/magyenColors.js`  
Tema MUI: `frontend/src/theme/appTheme.js`

No se deben esparcir hex en JSX. Los componentes consumen el tema.

---

## 3. Colores semánticos (estados)

Los estados de negocio **no** se pintan todos de oro.

| Estado | Tratamiento |
|---|---|
| SUCCESS / PAID / DELIVERED / COMPLETE / ACTIVE / Disponible | verde |
| WARNING / PENDING / IN_PRODUCTION / IN_PROGRESS / Stock bajo (urgencia) / WASTE | ámbar |
| ERROR / CANCELLED / REJECTED / Stock bajo | rojo |
| INFO / CONFIRMED / EXTERNAL | azul |
| READY_FOR_DELIVERY | `primary` (oro, un solo estado destacado) |
| CLOSED / DRAFT / INACTIVE / INTERNAL_MAGYEN | neutro / carbón |
| INCOME | verde |
| EXPENSE | rojo |
| Outstanding / pendiente de cobro | ámbar |

---

## 4. Navegación

- Header: superficie carbón, logo Magyen, wordmark, usuario y **Cerrar sesión** sin cambio de comportamiento.
- Debajo de `md` (900px): hamburger + drawer temporal `min(280px, 86vw)` (fase 2 intacta).
- Escritorio: sidebar permanente 240px, fondo carbón.
- Ítem normal: texto claro.
- Hover: velo blanco muy suave.
- Activo: lavado oro + barra interior oro + texto oro claro.
- Los filtros por rol no cambian: Home, Finanzas y Administración siguen siendo ADMIN-only. OPERATOR conserva las mismas restricciones.

---

## 5. Login

Composición de marca:

- fondo carbón
- tarjeta blanca con línea superior oro
- logo real
- wordmark Confecciones Magyen
- acento oro corto
- acción primaria oro

Autenticación, campos y mensajes de error sin cambio.

---

## 6. Encabezados de página

Componente `frontend/src/layout/PageHeader.jsx`:

- línea de acento oro (36×3)
- título `h3` en carbón
- subtítulo opcional
- acciones a la derecha (se apilan en móvil)

Páginas cubiertas: Inicio, Cotizaciones, Clientes, Vendedores, Órdenes, Producción, Inventario, Plotter, Finanzas, Administración (Usuarios, Catálogos), rentabilidad, detalle y login/branding.

---

## 7. Botones

| Jerarquía | Tratamiento |
|---|---|
| PRIMARY (`contained`) | oro Magyen + texto oscuro |
| SECONDARY (`outlined`) | blanco / borde neutro |
| DANGER (`color="error"`) | rojo semántico |
| SUCCESS (`color="success"`) | verde semántico |
| Baja prioridad (`text`) | neutro |

Las etiquetas y `onClick` no cambian. No todas las acciones son primarias.

Toque mínimo 40px bajo 900px (fase 2).

---

## 8. Tarjetas

- superficie blanca
- borde `#E4E1D8`
- radio 8px
- sombra mínima (`0 1px 2px rgba(17,17,17,0.04)`)
- `MetricCard` puede llevar acento lateral semántico (`tone`: income, expense, pending, external, internal, waste)

No hay fondos oro ni radios tipo píldora.

---

## 9. Tablas

- encabezado `surfaceMuted`
- separadores suaves
- hover de fila
- `overflow-x: auto` contenido (fase 2)
- celdas con `overflow-wrap`
- columnas y datos sin cambio

---

## 10. Formularios

- bordes neutros
- focus: borde oro Magyen (2px)
- labels claros
- checkbox / radio marcados en oro
- validación y mensajes sin cambio

---

## 11. Finanzas (solo visual)

- ingresos: verde
- gastos: rojo
- resultado neto: color según signo existente
- PENDING: ámbar
- PAID: verde
- CANCELLED: rojo
- outstanding / alertas: ámbar

Ningún cálculo ni transacción nueva.

---

## 12. Plotter (solo visual)

Tipos:

- EXTERNAL → info
- INTERNAL_MAGYEN → carbón (`secondary`)
- WASTE → warning

Métricas de rentabilidad conservan los mismos valores: papel impreso, generado, pagado, outstanding, costo de papel, tinta, resultado.

---

## 13. Tipografía

No se sustituyó la familia por una fuente de marketing. El tema usa el stack de sistema ya limpio (`Segoe UI` / system-ui). La identidad viene de color, jerarquía, logo y superficies.

`h3`/`h4` siguen reduciendo tamaño bajo 600px (fase 2).

---

## 14. Espaciado

Unidad MUI: 8px.

- padding de página: `xs: 2`, `md: 3` (se mantiene)
- secciones de listado: `spacing={3}` o `4`
- tarjetas / grids: `gap: 2`
- diálogos: margen 16px y `maxHeight: calc(100dvh - 32px)` (fase 2)

---

## 15. Iconos

Se conserva `@mui/icons-material`. No hay librería nueva ni iconos decorativos masivos.

---

## 16. Responsive (preservado)

Sigue aplicando:

| Viewport | Comportamiento |
|---|---|
| 1440, 1280, 1024 | sidebar permanente 240px |
| 900+ (`md`) | sidebar permanente |
| 768, 600 | drawer temporal + menú |
| 480, 390, 375, 360 | drawer, campos apilados, métricas 1 columna |

No se introduce overflow horizontal de página. Las tablas densas siguen con scroll horizontal contenido.

---

## 17. Limitaciones visuales conocidas

- El PNG del logo es circular sobre negro; en header/sidebar carbón el fondo del archivo se funde. No se recortó ni se vectorizó el original.
- `READY_FOR_DELIVERY` usa `primary` (oro) a propósito, como único estado de pedido con acento de marca.
- El texto de cuerpo no usa oro (contraste insuficiente sobre blanco).
- No hay animaciones (fuera de alcance).
- El PDF de cotización/remisión no se tocó; su paleta interna puede diferir levemente de la UI web.

---

## 18. Base de datos y backend

**Cero cambios de esquema, datos, `.env` o credenciales.**

El backend no se modifica en esta fase.
