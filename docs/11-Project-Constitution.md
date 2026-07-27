# Magyen Platform

# Project Constitution

**Versión:** 1.0

**Estado:** Aprobado

**Autor:** Joel David Vásquez

**Fecha:** 27 de julio de 2026

---

# 1. Propósito

La Constitución del Proyecto define los principios que gobiernan el desarrollo de Magyen Platform.

Su objetivo es garantizar que todas las decisiones técnicas, funcionales y arquitectónicas sean consistentes durante la vida del proyecto.

Esta constitución tiene prioridad sobre cualquier decisión de implementación.

Cuando exista una duda técnica, primero deberá consultarse este documento.

---

# 2. Filosofía del Proyecto

Magyen Platform no es únicamente un ERP.

Es una plataforma diseñada para ayudar a las empresas de confección a tomar mejores decisiones.

Todo desarrollo deberá contribuir a alguno de estos objetivos:

- Automatizar procesos.
- Reducir errores.
- Facilitar el trabajo diario.
- Generar información útil.
- Apoyar la toma de decisiones.

El software nunca deberá convertirse en un simple almacenamiento de datos.

---

# 3. Principios Fundamentales

Todos los desarrollos deberán respetar los siguientes principios.

## El dominio es el centro.

La lógica del negocio tendrá prioridad sobre la tecnología.

El framework nunca definirá el diseño del software.

---

## La simplicidad gana.

Siempre se elegirá la solución más sencilla que resuelva correctamente el problema.

Se evitará la complejidad innecesaria.

---

## La arquitectura no se rompe.

Las reglas arquitectónicas son obligatorias.

Ningún desarrollador podrá ignorarlas por comodidad.

---

## El código comunica.

El código debe ser fácil de leer.

Debe explicar claramente qué hace el negocio.

El código será escrito para personas.

---

## La documentación forma parte del software.

Toda decisión importante deberá quedar documentada.

Código sin documentación suficiente se considera incompleto.

---

# 4. Convenciones de Código

## Idioma

El código será escrito en inglés.

La documentación será escrita en español.

---

## Nombres

Los nombres deberán describir claramente la responsabilidad.

Evitar abreviaturas.

Incorrecto

CustomerSvc

Correcto

CustomerService

---

## Métodos

Los métodos deberán expresar acciones.

Ejemplos

createQuotation()

approveOrder()

calculateProductionCost()

assignOperator()

---

## Variables

Las variables deberán tener nombres descriptivos.

Nunca utilizar nombres como:

data

temp

value

obj

item

excepto en casos extremadamente puntuales.

---

## Clases

Cada clase deberá tener una única responsabilidad.

Si una clase necesita explicar demasiado lo que hace, probablemente tiene más de una responsabilidad.

---

# 5. Organización del Proyecto

La estructura oficial del proyecto será la definida en el Software Architecture Document.

No podrán crearse módulos nuevos sin una decisión arquitectónica formal.

Toda nueva capacidad del negocio deberá pertenecer a un módulo existente o justificar la creación de uno nuevo mediante un ADR.

---

# 6. Git

## Rama principal

main

representará siempre una versión estable.

---

## Commits

Los commits deberán ser pequeños.

Cada commit deberá representar una unidad lógica de trabajo.

Ejemplos

docs: define backend architecture

feat: implement quotation aggregate

fix: validate production dates

refactor: simplify inventory service

test: add quotation application tests

---

## Nunca

No realizar commits con mensajes como:

update

changes

fix

aaa

final

último

---

# 7. Definition of Done

Una tarea únicamente se considera terminada cuando cumple todos los siguientes puntos.

- Funciona correctamente.
- Compila.
- No rompe la arquitectura.
- Tiene nombres claros.
- Está documentada cuando aplica.
- Se realizaron pruebas correspondientes.
- El código fue revisado.

---

# 8. Inteligencia Artificial

La Inteligencia Artificial será utilizada como acelerador del desarrollo.

Nunca como reemplazo del criterio técnico.

Todo código generado deberá ser entendido antes de aceptarse.

Ninguna sugerencia de IA será aceptada automáticamente.

La decisión final siempre pertenecerá al desarrollador.

---

# 9. Calidad

La calidad tendrá prioridad sobre la velocidad.

Es preferible entregar una funcionalidad correcta que varias funcionalidades incompletas.

Todo cambio deberá mejorar el proyecto o mantener su nivel de calidad.

---

# 10. Evolución

Magyen Platform evolucionará mediante pequeñas mejoras continuas.

Se evitarán grandes reestructuraciones.

Las decisiones arquitectónicas importantes deberán registrarse mediante ADR.

---

# 11. Visión

Magyen Platform aspira a convertirse en una plataforma moderna para empresas de confección.

Su crecimiento estará guiado por la calidad del software, la claridad de su arquitectura y el profundo entendimiento del negocio.

Toda decisión deberá acercar al proyecto a esa visión.

---

# 12. Compromiso

Toda persona que participe en el desarrollo de Magyen Platform acepta respetar esta Constitución.

La arquitectura, la calidad y el dominio del negocio tendrán siempre prioridad sobre soluciones rápidas o atajos técnicos.