# SPR-039 — V1 RELEASE PHASE 2: diseño responsive

**Incremento:** SPR-039 RELEASE V1 — PHASE 2 — Responsive  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Esta fase hace usable la aplicación web existente en escritorio, portátil, tablet y teléfono.

**No forma parte de esta fase:**

- rediseño de color, marca, tipografía o animaciones
- nueva funcionalidad de negocio
- cambios de PDF
- cambios de base de datos o esquema
- SPR-040
- despliegue

La UI de escritorio aceptada se conserva. Solo cambia el comportamiento de layout cuando el viewport es estrecho.

---

## 1. Estrategia

No hay un segundo frontend móvil. Un solo árbol de componentes usa:

- CSS Grid / Flexbox ya presentes en las páginas
- `minWidth: 0` en el contenedor principal (evita overflow en flex)
- `overflow-x: clip` en `html`/`body`
- scroll horizontal **contenido** en tablas densas
- un tema MUI de **solo layout** (sin cambiar la paleta)

Los anchos de referencia del requerimiento se cubren con los breakpoints MUI ya usados por el proyecto:

| Viewport | Comportamiento |
|---|---|
| 1440, 1280, 1024 | escritorio: sidebar permanente 240px |
| 900+ (`md`) | sidebar permanente |
| 768, 600 | drawer temporal + menú |
| 480, 390, 375, 360 | drawer temporal, campos apilados, 1 columna en métricas |

No se añadieron decenas de media queries por página.

---

## 2. Breakpoints

Se respetan los defaults de MUI:

- `xs` 0
- `sm` 600
- `md` 900
- `lg` 1200
- `xl` 1536

La navegación compacta aplica bajo `md` (`viewportWidth <= 899`).

Las páginas que ya usaban `direction={{ xs: 'column', sm: 'row' }}` y `Grid size={{ xs: 12, md: 6 }}` se mantienen.

Ajuste tipográfico mínimo: `h3`/`h4` reducen un poco el tamaño bajo 600px para que el título no ocupe la pantalla. No es un rediseño tipográfico.

---

## 3. Navegación / sidebar

**Escritorio (`md+`):** Drawer permanente, mismo ancho 240px, mismos ítems.

**Tablet/móvil:** Drawer temporal de `min(280px, 86vw)`. El contenido usa todo el ancho. Un botón de menú en el header abre el drawer.

Al navegar, el drawer se cierra. Overlay modal de MUI.

Los ítems siguen filtrados por rol (`filterNavigationItems`):

- ADMIN: Inicio, Cotizaciones (+ Clientes, Vendedores), Órdenes, Producción, Inventario, Plotter, Finanzas, Administración (Usuarios, Catálogos)
- OPERATOR: no ve Inicio, Finanzas ni Administración

Home sigue siendo ADMIN-only en rutas (`AdminOnlyPage`).

---

## 4. Header

Una sola fila, altura ~56–64px.

- menú (solo compacto)
- título: `Magyen` en xs, `Magyen Platform` desde `sm`
- usuario con ellipsis (también al pie del drawer en compacto)
- **Cerrar sesión** siempre visible

---

## 5. Tablas

Estrategia 2 del requerimiento para tablas de negocio densas:

- `TableContainer` tiene `overflow-x: auto` y `max-width: 100%` por tema
- el scroll queda dentro de la tabla, no en toda la página
- no se eliminan columnas financieras

Tablas simples ya existentes (listas con pocas columnas) siguen igual; el scroll horizontal solo aparece si el contenido lo necesita.

---

## 6. Modales

Tema global:

- `fullWidth` por defecto
- `max-width: min(<tamaño MUI>, calc(100vw - 32px))`
- `max-height: calc(100dvh - 32px)`
- contenido con scroll vertical
- acciones con `flex-wrap`

Los diálogos de desactivar usuario/catálogo ahora también usan `fullWidth` + `maxWidth="sm"`.

---

## 7. Formularios

En desktop se conservan filas múltiples donde ya existían.

En anchos bajos:

- campos a 100% (período Finanzas/Plotter, navegador de mes)
- botones de crear cotización apilados y tocables a lo ancho
- `min-height: 40px` en botones bajo 900px

---

## 8. Tarjetas / métricas

Home y Finanzas: grid `1 / 2 / 4` columnas (`xs` / `sm` / `md`).

Plotter rentabilidad: grid `1 / 2 / 3`.

Los valores largos pueden partir palabra (`overflow-wrap`) para no empujar el layout.

---

## 9. Viewports revisados

Mapeo de las medidas pedidas contra el layout implementado:

| Tamaño | Sidebar | Contenido |
|---|---|---|
| 1440×900 | permanente | desktop original |
| 1280×800 | permanente | desktop original |
| 1024×768 | permanente | desktop; tablas pueden scrollear internamente |
| 768×1024 | drawer | campos apilados, 2 columnas en métricas |
| 600×800 | drawer | igual, más apilado |
| 480×900 | drawer | 1 columna |
| 390×844 | drawer | 1 columna |
| 375×812 | drawer | 1 columna |
| 360×800 | drawer | 1 columna; título `Magyen` |

La validación automatizada cubre el corte 899/900 y el filtrado de navegación por rol. La inspección visual en un navegador real sigue siendo recomendable al levantar `npm run dev`.

---

## 10. Limitaciones conocidas

- Las tablas densas (Plotter, Finanzas, Home, inventario) no se convierten en cards; se hace scroll horizontal interno.
- En 360px el título de producto se acorta a `Magyen` para no empujar el logout.
- No hay pruebas E2E de viewport (el proyecto no tiene ese framework; no se añadió uno).
- No se cambió el color, la marca ni el comportamiento de PDF/backend.

---

## 11. Seguridad de datos

Cero cambios de base de datos, esquema, `.env` o credenciales.
