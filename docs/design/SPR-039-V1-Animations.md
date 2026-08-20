# SPR-039 — V1 RELEASE PHASE 4: animaciones y microinteracciones

**Incremento:** SPR-039 RELEASE V1 — PHASE 4 — Animations  
**Estado:** Implemented  
**Fecha:** 19 de agosto de 2026

Esta fase añade movimiento sutil y corrige el contraste del sidebar. La identidad visual de la fase 3 permanece.

**No forma parte de esta fase:**

- rediseño de paleta o layout
- nueva funcionalidad de negocio
- cambios de PDF, APIs o base de datos
- SPR-040
- despliegue

---

## 1. Tokens de movimiento

Definición central: `frontend/src/theme/magyenMotion.js`

| Token | Valor | Uso |
|---|---|---|
| `fast` | 120 ms | hover, focus, botones, filas, chips |
| `normal` | 220 ms | diálogos, drawer, snackbar, tabs |
| `slow` | 320 ms | entrada única del login |
| `easing.standard` | `cubic-bezier(0.4, 0, 0.2, 1)` | microinteracciones |
| `easing.emphasized` | `cubic-bezier(0.2, 0, 0, 1)` | entrada / énfasis |

El helper `motionCss()` evita duraciones arbitrarias en JSX.

El tema MUI (`transitions`) mapea estos valores para que Dialog, Drawer y Snackbar usen los mismos tiempos.

---

## 2. Sidebar (ajuste visual)

El header **sigue carbón/negro** (fase 3).

El sidebar pasa a superficie clara:

- fondo blanco (`surface.paper`)
- texto carbón
- borde `#E4E1D8`
- hover: `surfaceMuted`
- activo: tint oro muy claro (`rgba(201, 162, 39, 0.10)`) + barra interior oro 3px
- el texto activo permanece oscuro (legible); el oro es el indicador, no el relleno

El logo Magyen se conserva en el drawer. No hay fondo oro.

Roles sin cambio: Home / Finanzas / Administración siguen ADMIN-only. OPERATOR conserva las mismas restricciones. Breakpoint compacto: 899px.

---

## 3. Transiciones por componente

| Superficie | Motion |
|---|---|
| Ítems de navegación | hover/activo `fast` (color + barra oro) |
| Drawer temporal | slide 220 ms entrada / 180 ms salida; overlay fade |
| Header | hover/focus en menú y logout; el header no se mueve |
| Botones | color `fast`; pressed `scale(0.98)`; focus-visible oro |
| Campos | borde `fast`; anillo de foco oro |
| Tablas | hover de fila `fast`; sin stagger de entrada |
| Diálogos | fade 220 / 180 ms; sin zoom grande |
| Snackbar existente | misma duración; no se duplican toasts |
| Login | fade + 8px de elevación, una sola vez |
| Métricas | estables; no se animan cifras |
| Navegación de páginas | no se fuerza fade de ruta (evitar remount y fetches extra) |

---

## 4. Loading y éxito

- Skeletons MUI existentes se conservan (indicador de carga permitido).
- Snackbars/alerts ya usados en crear cotización, inventario, Plotter, finanzas, catálogos, etc. Solo se suaviza la transición. No hay mensajes de éxito inventados.

---

## 5. Accesibilidad

`prefers-reduced-motion: reduce` en `CssBaseline`:

- transiciones y animaciones a ~0
- ripple desactivado
- focus-visible se conserva
- login no anima la tarjeta

La funcionalidad no cambia.

---

## 6. Rendimiento

- Solo CSS `transition` / `transform` / `opacity`
- Sin bucles JS
- Tablas: hover de color, no animación por fila al montar
- Sin parallax, gradients animados ni motion continuo del logo

---

## 7. Responsive (preservado)

| Viewport | Comportamiento |
|---|---|
| 1440, 1280, 1024 | sidebar permanente 240px, ahora claro |
| 900+ (`md`) | sidebar permanente |
| 768–360 | drawer temporal + overlay; slide 220 ms |

Header compacto, scroll horizontal de tablas y diálogos acotados al viewport se mantienen.

---

## 8. Limitaciones conocidas

- No hay transición de página entre rutas: React Router remountaría el árbol y retrasaría la navegación.
- El pressed `scale(0.98)` de botones se anula con reduced-motion.
- El PNG del logo sigue siendo sello oro sobre negro; en sidebar blanco se lee como medallón (intencional, no se alteró el archivo).
