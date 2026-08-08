# SPR-022-Frontend-Architecture.md

## Objetivo

Construir la arquitectura base del frontend del ERP Magyen utilizando React, siguiendo los mismos principios arquitectónicos implementados en el backend.

Este sprint NO implementa funcionalidades de negocio.

Su objetivo es dejar lista una base sólida, escalable y mantenible para los siguientes sprints.

---

# Objetivos

- Crear el proyecto React utilizando Vite.
- Definir la arquitectura del frontend.
- Implementar el Layout principal.
- Configurar React Router.
- Configurar Material UI.
- Configurar Axios.
- Organizar el proyecto por módulos (features).
- Mantener separación de responsabilidades.
- Preparar el frontend para consumir las APIs del backend.

---

# Reglas arquitectónicas

El frontend deberá seguir una arquitectura modular.

No se permitirá una carpeta enorme con todas las páginas mezcladas.

La estructura deberá ser similar al backend.

Cada módulo será responsable únicamente de su propia funcionalidad.

---

# Tecnologías

- React
- Vite
- React Router
- Material UI
- Axios

---

# Organización del proyecto

src/

- app/
- router/
- layout/
- features/
- services/
- shared/
- assets/

Cada feature podrá contener posteriormente:

- pages/
- components/
- hooks/
- services/

---

# Comunicación con Backend

Toda comunicación con Spring Boot deberá realizarse mediante Axios.

Los componentes React nunca consumirán directamente la API.

Siempre utilizarán servicios especializados.

---

# Layout

El sistema tendrá inicialmente:

- Sidebar
- Topbar
- Área principal (Content)

---

# Navegación

React Router será el único responsable de la navegación.

No se permitirá navegación manual mediante window.location.

---

# Material UI

Todo el sistema utilizará Material UI como librería de componentes.

No se utilizará Bootstrap.

---

# Estado

En este sprint no se implementará manejo global del estado.

Se utilizará únicamente estado local de React.

Redux, Context o Zustand se evaluarán en futuros sprints si llegan a ser necesarios.

---

# Calidad

Todo cambio deberá:

- Compilar correctamente.
- Mantener la arquitectura.
- No generar código duplicado.
- Mantener componentes pequeños.
- Mantener responsabilidades separadas.

---

# Resultado esperado

Al finalizar este sprint deberá existir un frontend React funcional ejecutándose localmente, con su arquitectura base completamente preparada para comenzar el desarrollo del Dashboard y el consumo de las APIs del ERP.