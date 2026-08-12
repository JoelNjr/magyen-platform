# Magyen Platform

# Software Architecture Document (SAD)

**Versión:** 1.0

**Estado:** Draft

**Autor:** Joel David Vásquez

**Fecha:** 27 de julio de 2026

---

# 1. Objetivo

Este documento define la arquitectura oficial de Magyen Platform.

Su propósito es establecer las decisiones técnicas que guiarán el desarrollo del software durante toda su evolución, garantizando mantenibilidad, escalabilidad y claridad estructural.

Todas las decisiones de implementación deberán respetar este documento.

---

# 2. Visión General

Magyen Platform es una plataforma inteligente para la gestión de empresas de confección.

El objetivo del sistema no es únicamente administrar información, sino transformar los datos operativos en decisiones de negocio.

La plataforma se desarrollará inicialmente para Confecciones Magyen, pero será diseñada para permitir futuras adaptaciones a otras empresas del sector textil.

---

# 3. Objetivos Arquitectónicos

La arquitectura deberá cumplir los siguientes objetivos:

- Mantener una alta cohesión entre componentes.
- Reducir el acoplamiento entre módulos.
- Facilitar el crecimiento del sistema.
- Permitir pruebas automatizadas.
- Mantener independencia del framework.
- Reflejar correctamente el dominio del negocio.
- Facilitar futuras integraciones.
- Permitir una evolución hacia arquitecturas distribuidas si algún día fuera necesario.

---

# 4. Principios Arquitectónicos

La arquitectura de Magyen Platform se fundamenta en los siguientes principios.

## 4.1 Arquitectura Modular

El sistema estará dividido por capacidades del negocio.

Cada módulo representará un área funcional claramente definida.

No existirán módulos basados únicamente en entidades técnicas.

---

## 4.2 Clean Architecture

La lógica del negocio será independiente de Spring Boot.

Spring Boot será considerado un detalle de infraestructura.

Las reglas del negocio nunca dependerán del framework.

---

## 4.3 Domain Driven Design (DDD Ligero)

El lenguaje del código deberá representar el lenguaje utilizado dentro de la empresa.

Las entidades, servicios y casos de uso utilizarán el mismo vocabulario que emplean los usuarios del negocio.

---

## 4.4 Modular Monolith

Durante las primeras versiones, Magyen Platform será un Monolito Modular.

Todos los módulos vivirán dentro de una misma aplicación.

La separación entre módulos será lógica y arquitectónica, no física.

Esta decisión reduce la complejidad inicial sin impedir una futura evolución.

---

## 4.5 Documentación como parte del software

Todo cambio arquitectónico deberá quedar documentado.

La documentación tendrá el mismo nivel de importancia que el código fuente.

---

# 5. Arquitectura General

Magyen Platform estará organizada en dos grandes niveles.

## Core Platform

Responsable de ejecutar la operación diaria de la empresa.

Incluye:

- Comercial
- Producción
- Inventario
- Finanzas
- Administración

---

## Operational Intelligence

Responsable de analizar la información generada por la operación.

Este componente transformará los datos del negocio en recomendaciones, alertas y apoyo para la toma de decisiones.

Operational Intelligence nunca reemplazará al usuario.

Su función será asistir la toma de decisiones mediante reglas de negocio y, en versiones futuras, mediante Inteligencia Artificial.

---

# 6. Organización del Backend

El backend de Magyen Platform seguirá una arquitectura basada en capacidades del negocio (Business Capabilities).

Cada módulo representará una responsabilidad claramente definida dentro de la empresa.

Los módulos serán independientes entre sí y se comunicarán únicamente mediante interfaces bien definidas y servicios de aplicación.

No se permitirá compartir lógica de negocio entre módulos sin pasar por el Shared Kernel o por contratos explícitos.

---

## 6.1 Estructura General

El proyecto backend estará organizado bajo el siguiente paquete base.

```
com.magyen.platform
```

A partir de este paquete se construirán los módulos principales.

```
com.magyen.platform

├── commercial
├── production
├── inventory
├── finance
├── administration
├── intelligence
├── home
├── shared
└── config
```

`home` es un módulo de read model / orquestación (SPR-037). No es dueño de datos operativos de otros módulos y se comunica únicamente mediante ports de aplicación.
---

## 6.2 Responsabilidad de cada módulo

### Commercial

Gestiona toda la relación con el cliente.

Responsabilidades:

- Prospectos
- Clientes
- Cotizaciones
- Conversaciones
- Seguimiento comercial

No tendrá responsabilidades relacionadas con producción.

---

### Production

Representa el proceso de fabricación.

Responsabilidades:

- Órdenes de Producción
- Planeación
- Etapas
- Asignaciones
- Estado de Producción
- Seguimiento

Será considerado el núcleo operativo de la plataforma.

---

### Inventory

Gestiona los recursos físicos.

Responsabilidades:

- Materias primas
- Telas
- Insumos
- Entradas
- Salidas
- Existencias

No conocerá información financiera.

---

### Finance

Gestiona el flujo económico.

Responsabilidades:

- Pagos
- Anticipos
- Facturación
- Cuentas por cobrar
- Indicadores financieros

No gestionará inventario ni producción.

---

### Administration

Gestiona la configuración general del sistema.

Responsabilidades:

- Usuarios
- Roles
- Permisos
- Parámetros
- Configuración

---

### Intelligence

Representa el cerebro de Magyen Platform.

Responsabilidades:

- Reglas de decisión
- Alertas
- Priorización
- Recomendaciones
- Analítica

Este módulo nunca modificará directamente el estado del negocio.

Su función será generar conocimiento para apoyar la toma de decisiones.

---

### Home

Módulo de read model / orquestación operativa (SPR-037).

Responsabilidades:

- Consolidar lecturas de otros módulos para el Dashboard
- Exponer un contrato REST de solo lectura
- Orquestar ports de aplicación hacia Finance, Commercial, Inventory, Production y Plotter

Home no es dueño de Orders, FinancialTransactions, InventoryItems, ProductionOrders ni PlotterJobs.

No accede a repositorios JPA ni entidades de otros módulos.

---

### Shared

Contendrá únicamente componentes reutilizables por toda la plataforma.

Ejemplos:

- Excepciones comunes
- Objetos de Valor
- Eventos de Dominio
- Utilidades
- Interfaces compartidas

Nunca contendrá lógica específica de un módulo.

---

### Config

Contendrá la configuración técnica del sistema.

Ejemplos:

- Spring Configuration
- Seguridad
- Beans
- Configuración global

No contendrá reglas del negocio.

---

## 6.3 Arquitectura Interna de cada módulo

Todos los módulos seguirán exactamente la misma estructura.

```
module/

application/

domain/

infrastructure/

presentation/
```

### application

Contendrá los Casos de Uso del negocio.

Será el punto de entrada para ejecutar operaciones.

---

### domain

Contendrá el corazón del negocio.

Aquí vivirán:

- Entidades
- Objetos de Valor
- Servicios de Dominio
- Eventos de Dominio
- Interfaces (Ports)

Este paquete nunca dependerá de Spring Boot.

---

### infrastructure

Implementará los detalles técnicos.

Ejemplos:

- JPA
- Repositorios
- Adaptadores
- Integraciones externas

---

### presentation

Representará los puntos de entrada del sistema.

Ejemplos:

- REST Controllers
- DTOs
- Validaciones
- Mappers

La lógica del negocio nunca será implementada aquí.

---

## 6.4 Reglas de Dependencia

Las dependencias deberán seguir el siguiente flujo.

Presentation

↓

Application

↓

Domain

↑

Infrastructure

El dominio nunca dependerá de infraestructura.

Spring Boot nunca será conocido por el dominio.

Esta regla será obligatoria durante todo el desarrollo de Magyen Platform.