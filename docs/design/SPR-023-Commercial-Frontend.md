# SPR-023 Commercial Frontend

## Objetivo

Construir la primera funcionalidad completa del módulo Comercial en el frontend de Magyen Platform.

Este sprint introduce la primera pantalla funcional de negocio consumiendo información real desde el backend mediante la arquitectura definida en SPR-022.

El objetivo NO es construir todavía formularios de creación o edición.

El objetivo es establecer el patrón definitivo para consumir módulos del backend desde React.

---

## Alcance

Este sprint incluye únicamente:

- Servicio del módulo Comercial.
- Primera página funcional de Cotizaciones.
- Consumo del endpoint existente.
- Tabla simple con Material UI.
- Estados de carga.
- Estados de error.
- Navegación desde el menú lateral.

---

## Fuera de alcance

No incluye:

- Crear cotizaciones.
- Editar cotizaciones.
- Eliminar cotizaciones.
- Formularios.
- Validaciones.
- Paginación.
- Búsquedas.
- Filtros.
- Autenticación.
- Dashboard avanzado.

---

## Restricciones

Mantener exactamente la arquitectura definida en SPR-022.

UI

↓

Feature Service

↓

Axios

↓

Spring Boot

No consumir Axios directamente desde componentes.

No crear lógica de negocio en React.

Toda comunicación deberá pasar por los Feature Services.

---

## Resultado esperado

Al finalizar el sprint deberá existir una primera pantalla funcional del módulo Comercial mostrando información obtenida desde Spring Boot.

Esta pantalla servirá como patrón para todos los demás módulos del frontend.