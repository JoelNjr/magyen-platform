# Magyen Platform

# System Context

Este diagrama representa el contexto general del sistema y los actores principales.

```mermaid
flowchart TB

    Owner["Gerencia"]
    Production["Producción"]
    Sales["Comercial"]
    Inventory["Inventario"]
    Finance["Finanzas"]

    Platform["Magyen Platform"]

    Owner --> Platform
    Production --> Platform
    Sales --> Platform
    Inventory --> Platform
    Finance --> Platform
```