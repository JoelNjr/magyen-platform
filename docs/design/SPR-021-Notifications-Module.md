# SPR-021 — Notifications Module

# SPR-021 — Módulo de Notificaciones

## Objetivo

Implementar el módulo de Notificaciones siguiendo la arquitectura de la plataforma y los principios de Clean Architecture.

El propósito de este módulo es informar proactivamente a los usuarios sobre situaciones relevantes detectadas dentro del ERP.

A diferencia del módulo de Inteligencia, que responde consultas analíticas, el módulo de Notificaciones genera alertas operativas que requieren la atención del usuario.

---

## Requerimientos Funcionales

El módulo deberá generar notificaciones para:

### Inventario

- Materiales con stock por debajo del mínimo establecido.

### Comercial

- Pedidos cuya fecha de entrega esté próxima.

### Producción

- Órdenes de producción retrasadas con respecto a la planificación.

### Finanzas

- Pedidos con saldo pendiente por cobrar.

---

## Características de una Notificación

Cada notificación deberá contener:

- notificationId
- type
- title
- message
- severity
- createdAt
- referenceId
- module

Las notificaciones se generan dinámicamente a partir del estado actual del sistema.

Durante este sprint no se almacenará un historial de notificaciones.

---

## Niveles de Severidad

```
INFO
WARNING
CRITICAL
```

---

## Características del Módulo

El módulo de Notificaciones es un módulo de solo lectura.

No posee entidades de dominio.

No posee agregados.

No posee Value Objects.

No posee persistencia propia.

No posee tablas en la base de datos.

Su responsabilidad consiste únicamente en consolidar información proveniente de los módulos existentes para generar alertas operativas.

---

## API REST

### Obtener Notificaciones

```
GET /api/v1/notifications
```

Retorna todas las notificaciones activas detectadas por el sistema.

---

## Arquitectura

Presentation

↓

Application

↓

Repository Ports existentes

---

## Criterios de Aceptación

- No existe capa Domain.
- No existen cambios sobre schema.sql.
- No existen nuevas tablas.
- No existen entidades JPA.
- No existen repositorios propios.
- El módulo es completamente de solo lectura.
- Utiliza los Repository Ports existentes.
- El controlador permanece delgado.
- Toda la generación de notificaciones ocurre en la capa Application.
- El proyecto compila correctamente.