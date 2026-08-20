# SPR-039 — V1 RELEASE PHASE 5: pulido visual final

**Incremento:** SPR-039 RELEASE V1 — PHASE 5 — Final visual polish  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Pase de consistencia sobre la identidad y el motion ya aprobados. No hay rediseño ni cambio de paleta.

**Fuera de alcance:** QA final, checklist de release, despliegue, SPR-040, PDF, APIs, base de datos.

---

## 1. Reglas de consistencia

Fuente de verdad: `frontend/src/theme/` + `PageHeader`, `SectionHeader`, `MetricCard`, `EmptyState`, `MagyenLogo`.

- No hex sueltos en páginas. El tint de nav activo vive en `magyenColors.gold.selectedWash`.
- Sombras: `magyenColors.shadow.card` y `shadow.login`.
- Listados: `Stack spacing={3}`. Dashboards (Inicio, Finanzas): `spacing={4}`.
- Títulos de módulo: `PageHeader` (h3 + línea oro).
- Títulos de sección: `SectionHeader` (h5, subtítulo `body2`).
- Métricas: `MetricCard` con `tone` semántico.

---

## 2. Espaciado

| Superficie | Decisión |
|---|---|
| Celdas de tabla | `py: 1.25`, `px: 2`; encabezado `py: 1.5` |
| CardContent | 20px |
| DialogContent | `paddingTop: 16` (evita el primer campo pegado al título) |
| DialogActions | `px: 3`, `py: 2`, wrap + gap |
| EmptyState | `p: { xs: 3, sm: 4 }` |
| PageHeader actions | 100% de ancho en `xs` |

---

## 3. Tipografía

- Familia de sistema existente, sin cambio.
- Encabezados de tabla: 700, nowrap (el scroll horizontal cubre columnas densas).
- Valores numéricos: `font-variant-numeric: tabular-nums`.
- Subtítulos: `text.secondary` / `body2`.
- Asterisco de campo requerido: color `error` (el significado no depende solo del color: el helper text sigue existiendo).

---

## 4. Tablas, formularios y diálogos

- Scroll horizontal contenido se mantiene.
- No se ocultan columnas ni se pagina.
- Focus de campos: borde oro (fase 3/4).
- Diálogos: mismos max-width de viewport (fase 2) + padding de contenido más predecible.

---

## 5. Empty / loading / error

`EmptyState` unifica listados:

- título (qué está vacío)
- mensaje (por qué / qué hacer)
- acción opcional (sin inventar datos)

Errores de listado usan `Alert`. Skeletons de tabla se conservan. No hay loading falso.

---

## 6. Accesibilidad (revisión ligera)

- Contraste: texto carbón sobre blanco; oro no se usa como texto de cuerpo.
- `focus-visible` oro en botones del header y botones globales.
- Estados: chips semánticos + etiqueta de texto.
- `prefers-reduced-motion` de la fase 4 intacto.
- Asterisco requerido en rojo además del helper.

---

## 7. Responsive y motion

Sin cambios de breakpoint (899px), drawer, ni tokens `fast/normal/slow`.

---

## 8. Limitaciones conocidas

- Algunos vacíos embebidos en tablas (Finanzas) siguen siendo `EmptyState` con Paper dentro de la celda; es el patrón ya usado, no un rediseño de esas tablas.
- Detalles de documento (cotización/orden/producción) conservan el identificador en `h4` bajo `BrandAccentLine`; no se forzó `PageHeader` encima de cada chip de estado.
- El PNG del logo no se tocó.
