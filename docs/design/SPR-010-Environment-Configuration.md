# SPR-010 — Environment Configuration

## Objetivo

Mejorar la experiencia de desarrollo (Developer Experience - DX) permitiendo que Spring Boot cargue automáticamente la configuración de desarrollo sin depender de comandos manuales para establecer variables de entorno.

---

# Problema actual

Actualmente Docker Compose carga automáticamente el archivo `.env`.

Sin embargo Spring Boot no lo hace.

Para ejecutar la aplicación es necesario cargar previamente:

- DB_USERNAME
- DB_PASSWORD

mediante comandos de PowerShell.

Esto introduce pasos manuales innecesarios.

---

# Objetivos

- Mantener un único origen de verdad para las credenciales de desarrollo.
- Evitar duplicar usuario y contraseña.
- Permitir ejecutar Spring Boot con un único comando.
- No afectar producción.
- No romper Clean Architecture.

---

# Restricciones

- No hardcodear credenciales.
- No mover configuración al código Java.
- No romper Docker Compose.
- No modificar Domain.
- No modificar Application.
- No modificar Infrastructure fuera de configuración.

---

# Alternativas a evaluar

## Opción A

Variables de entorno permanentes del sistema operativo.

Ventajas:
- estándar

Desventajas:
- configuración manual en cada equipo.

---

## Opción B

Carga automática del archivo `.env`.

Ventajas:
- misma configuración para Docker y Spring.
- más simple para desarrollo.

Desventajas:
- requiere soporte adicional.

---

## Opción C

application-local.yml

Ventajas:
- perfiles Spring.

Desventajas:
- duplica configuración.

---

# Criterios de decisión

Debe elegirse la alternativa que:

- reduzca pasos manuales;
- mantenga una única fuente de configuración;
- facilite el onboarding de nuevos desarrolladores;
- sea mantenible.

---

# Fuera de alcance

Vault

AWS Secrets Manager

Azure Key Vault

Kubernetes Secrets

Producción

CI/CD