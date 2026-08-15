# SPR-038 — Auth / Security

**Incremento:** 4 — Internal User Administration  
**Estado:** Implemented  
**Fecha:** 15 de agosto de 2026

---

## 1. Inspección de la línea base

Antes de implementar se inspeccionó el repositorio. Hallazgos:

| Área | Estado encontrado |
|------|-------------------|
| Spring Boot | 4.1.0 (Java 21) |
| Spring Security | No existía dependencia ni configuración |
| Modelos de usuario / cuenta | No existían. `payroll_employees` es nómina, no identidad de login |
| Roles / permisos | No existían. SAD asigna Usuarios/Roles/Permisos al módulo `administration` |
| Convención de BD | PostgreSQL 17, UUID PK, `schema.sql` es dueño del esquema, `ddl-auto: validate` |
| Cliente HTTP frontend | Axios en `frontend/src/services/httpClient.js`, `baseURL: /api/v1`, sin token ni interceptores |
| Manejo de excepciones | `GlobalExceptionHandler` + `ErrorResponse` (`timestamp`, `status`, `error`, `message`, `path`) |
| Configuración | `.env` en la raíz + `springboot4-dotenv`; secretos no van en código |
| Docker | `magyen-postgres` (Postgres 17), volumen persistente, `schema.sql` solo en init |
| Endpoints públicos | Toda la API `/api/v1/**` estaba expuesta sin autenticación. No hay Actuator/health |

No había una implementación de autenticación que extender. Se creó la fundación en `administration` + `config`, sin un modelo competidor.

---

## 2. Arquitectura de autenticación

La autenticación es un concern de plataforma. El agregado de identidad vive en **Administration**. Spring Security y JWT viven fuera del dominio.

```
presentation (AuthController)
        ↓
application (AuthenticateUserUseCase / GetAuthenticatedUserUseCase)
        ↓
domain (AuthenticationUser + ports de repositorio)
        ↑
infrastructure (JPA, BCrypt, Nimbus JWT, bootstrap)
```

`config.security` ensambla el `SecurityFilterChain` y el filtro JWT.

Los módulos Commercial, Production, Inventory, Plotter, Finance y Home **no** dependen de Spring Security ni de entidades JPA de autenticación.

### Ports

| Port | Responsabilidad | Adapter |
|------|-----------------|---------|
| `AuthenticationUserRepository` | Persistencia del agregado | `JpaAuthenticationUserRepository` |
| `PasswordHasher` | Hash y verificación | `BcryptPasswordHasher` |
| `AuthenticationTokenIssuer` | Emisión de JWT | `NimbusJwtTokenAdapter` |
| `AuthenticationTokenValidator` | Validación de JWT | `NimbusJwtTokenAdapter` |

---

## 3. Mecanismo seleccionado: JWT stateless

**Decisión:** JWT HMAC-SHA256 (Nimbus) enviado como `Authorization: Bearer`.

**Por qué encaja en la arquitectura actual:**

- El frontend es un SPA React (Vite) que consume la API por Axios y un proxy a `:8080`.
- La API ya es REST versionada y stateless (`SPR-004`).
- No existe sesión de servidor ni cookies de autenticación.
- JWT evita introducir estado de sesión en el monolito y no requiere refresh-token en V1.

**No se implementaron refresh tokens** en este incremento: el SPA reautenticará cuando el token expire.

**Claims mínimos del JWT:**

- `sub` — UUID del usuario
- `username`
- `role`
- `iat` / `exp`

No se incluyen hash de contraseña, secretos ni datos personales.

El filtro valida firma y expiración en cada request autenticada. No consulta la base en cada request (stateless). `GET /api/v1/auth/me` sí recarga la identidad persistida.

---

## 4. Modelo de usuario

Agregado `AuthenticationUser` (tabla `users`):

| Campo | Uso V1 |
|-------|--------|
| `id` | UUID |
| `username` | Identificador de login, único |
| `password_hash` | BCrypt. Nunca plaintext |
| `enabled` | Permite desactivar la cuenta |
| `role` | `AuthenticationRole` V1: `ADMIN` o `OPERATOR`. Persistido y emitido en el JWT. La autorización de negocio la aplica Spring Security |

No hay email, nombre, empleado, cliente ni perfil. `toString()` no expone el hash.

El dominio rechaza un valor que no parezca un hash BCrypt (`$2a$` / `$2b$` / `$2y$`).

---

## 5. Seguridad de contraseñas

- Algoritmo: **BCrypt** (`BCryptPasswordEncoder` de Spring Security).
- El hash se calcula **antes** de persistir (bootstrap, tests y cualquier creación futura).
- El login compara el password recibido contra el hash almacenado.
- Usuario desconocido, password inválido y usuario deshabilitado producen el mismo mensaje: `Invalid credentials.`
- Nunca se registran en logs: passwords, hashes, secretos JWT ni encabezados `Authorization`.

---

## 6. Contrato REST

### `POST /api/v1/auth/login`

Público. No requiere token previo.

Request:

```json
{
  "username": "operator",
  "password": "secret"
}
```

Response 200:

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "userId": "uuid",
  "username": "operator",
  "role": "OPERATOR"
}
```

| Caso | HTTP |
|------|------|
| Credenciales inválidas / usuario inexistente / deshabilitado | 401 `Invalid credentials.` |
| Username o password en blanco | 400 |
| JSON malformado | 400 |

No existe endpoint de registro en V1.

### `GET /api/v1/auth/me`

Requiere Bearer token. Devuelve identidad actual sin hash. Sirve al adaptador de seguridad y a Increment 2 para restaurar sesión.

---

## 7. Límites de seguridad

| Recurso | Regla Increment 3 |
|---------|-------------------|
| `POST /api/v1/auth/login` | Público |
| `/error` | Público (errores del contenedor) |
| `/api/v1/finance/**` | `ADMIN` |
| `PATCH /api/v1/inventory/*/unit-cost` | `ADMIN` |
| `/api/v1/reports/**` | `ADMIN` (Intelligence V2; no está en el sidebar) |
| `/api/v1/notifications` | `ADMIN` |
| Resto de `/api/v1/**` | Autenticado (`ADMIN` o `OPERATOR`) |
| CSRF | Desactivado: el SPA envía JWT en header, no cookies de sesión |
| Sesión | `STATELESS` |
| Form login / HTTP Basic | Deshabilitados |

No hay Actuator. No se desactivó la seguridad global.

**Implicación frontend:** hasta Increment 2, las llamadas Axios a la API de negocio recibirán 401. No se implementó UI de login en este incremento.

Los tests MockMvc preexistentes construyen MockMvc **sin** `springSecurity()`, por lo que no ejercitan el filtro. Los tests de autenticación sí aplican la cadena de filtros.

---

## 8. Persistencia / cambios de base de datos

Tabla nueva `users`. Sin FKs a módulos de negocio.

```sql
CREATE TABLE users (
    id              uuid            NOT NULL,
    username        varchar(100)    NOT NULL,
    password_hash   varchar(255)    NOT NULL,
    enabled         boolean         NOT NULL,
    role            varchar(30)     NOT NULL,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_username_key UNIQUE (username)
);
```

El índice de `username` lo provee `UNIQUE`. No se añadieron índices extra. No se modificaron tablas de Commercial, Production, Inventory, Plotter, Finance ni Home.

`schema.sql` se actualizó para instalaciones nuevas. En el volumen Docker existente se aplicó DDL aditivo (`CREATE TABLE IF NOT EXISTS`). No se hizo DROP, TRUNCATE ni reset.

---

## 9. Configuración y secretos

Variables de entorno (ver `.env.example`):

| Variable | Uso |
|----------|-----|
| `JWT_SECRET` | Clave HMAC-SHA256. Mínimo 32 caracteres. Sin default inseguro |
| `JWT_EXPIRATION_MS` | Expiración del token. Default local: `3600000` |
| `AUTH_BOOTSTRAP_ENABLED` | Crea un usuario local si no existe. Default: `false` |
| `AUTH_BOOTSTRAP_USERNAME` | Username del bootstrap |
| `AUTH_BOOTSTRAP_PASSWORD` | Password del bootstrap (se hashea; no se loguea) |
| `AUTH_BOOTSTRAP_ROLE` | Rol del bootstrap. V1: `ADMIN` o `OPERATOR`. Default: `ADMIN` |

Nunca commitear `.env` ni secretos reales. El secret JWT no está hardcodeado.

---

## 10. Cobertura de tests

| Id | Escenario | Dónde |
|----|-----------|-------|
| A | Credenciales válidas autenticán | `AuthenticateUserUseCaseTest` |
| B | Password inválido rechazado | `AuthenticateUserUseCaseTest` |
| C | Username desconocido rechazado | `AuthenticateUserUseCaseTest` + `AuthApiContractTest` |
| D | Usuario deshabilitado no autentica | `AuthenticateUserUseCaseTest` |
| E | Password nunca se persiste en plaintext | `AuthenticationUserPersistenceTest` + `AuthenticationUserTest` |
| F | Login no expone `passwordHash` | `AuthApiContractTest` |
| G | Token válido aceptado en endpoint protegido | `GET /api/v1/auth/me` |
| H | Token expirado / inválido rechazado | `AuthApiContractTest` |
| I | Login accesible sin autenticación previa | `AuthApiContractTest` |
| J | Tests de módulos existentes | `mvnw test` |

Los tests de autenticación crean usuarios de autenticación; no fabrican datos de negocio.

Verificación Increment 1:

- Backend `mvnw compile`: OK
- Backend `mvnw test`: **341 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS
- Frontend `npm run lint`: OK
- Frontend `npm run build`: OK
- Docker `magyen-postgres`: tabla `users` creada de forma aditiva (23 tablas; las 22 de negocio intactas)

---

## 11. Fuera de alcance (Increment 1)

- UI de login / logout
- Autorización por rol en módulos de negocio
- Registro público de usuarios
- Recuperación de password
- Verificación de email
- OAuth / login social
- 2FA
- Refresh tokens
- CORS (el SPA usa proxy Vite same-origin)
- Relación usuario ↔ empleado / cliente
- Catálogo de permisos

---

## 12. Plan Increment 2 (ejecutado)

Objetivo: integrar el SPA con la fundación de autenticación, sin rediseño visual de los módulos de negocio.

El frontend consume:

1. `POST /api/v1/auth/login` con `{ username, password }`.
2. Guarda `accessToken` + identidad mínima (`userId`, `username`, `role`).
3. Envía `Authorization: Bearer <accessToken>` desde el `httpClient` existente.
4. Trata HTTP 401: limpia estado y redirige a `/login`.
5. Usa `GET /api/v1/auth/me` una vez al arrancar si hay token.
6. Ruta `/login` fuera de `MainLayout`, con guard de rutas autenticadas.
7. Home, Commercial, Production, Inventory, Plotter, Finance e Intelligence no se rediseñaron.

---

## 13. Inspección frontend (Increment 2)

- Un único Axios client: `frontend/src/services/httpClient.js`.
- Rutas de negocio bajo `MainLayout`; no existía `/login`.
- Errores de UI: MUI `Alert` / `Snackbar`; no hay `window.alert`.
- No había `localStorage` / `sessionStorage`.
- SPR-022: estado local por página; Context se habilita ahora solo para autenticación de plataforma.
- No hay framework de tests frontend; no se introdujo uno.

---

## 14. Arquitectura frontend de autenticación

```
features/auth/
  AuthProvider.jsx          Context de sesión (identidad + login/logout)
  pages/LoginPage.jsx
  components/ProtectedRoute.jsx
  components/PublicLoginRoute.jsx
  components/AuthLoadingScreen.jsx
  services/authService.js   login + /auth/me
  services/authStorage.js   sessionStorage
  services/unauthorizedHandler.js
  presentation/authPresentation.js
```

Los módulos de negocio siguen usando `httpClient` sin conocer JWT ni storage.

`AuthProvider` vive dentro de `BrowserRouter` para usar `navigate` (SPR-022: no `window.location`).

---

## 15. Almacenamiento del token

**Decisión:** `sessionStorage`.

Claves:

- `magyen.auth.accessToken`
- `magyen.auth.identity` — `{ userId, username, role }`

**Por qué:**

- El JWT llega en el body (no hay cookie httpOnly). Cambiar eso requeriría rediseñar Increment 1.
- `sessionStorage` sobrevive un refresh (requisito J) y se borra al cerrar la pestaña.
- Es menos persistente que `localStorage`.
- XSS puede leer ambos; httpOnly cookies no están disponibles sin cambio de backend.

**No se guarda:** password, hash, `Authorization` header, `expiresInSeconds`.

**Logout** borra ambas claves. El JWT es stateless: el logout V1 es solo estado de frontend.

---

## 16. Integración Axios

El `httpClient` existente tiene:

- Request interceptor: adjunta `Authorization: Bearer` si hay token. No lo adjunta a `/auth/login`.
- Response interceptor: si 401 y no es login → limpia storage y notifica al `AuthProvider`.
- Los errores no-401 se rechazan igual que antes (las páginas siguen mostrando sus Alert).

No hay un segundo client HTTP.

---

## 17. Rutas protegidas y 401

- `/login` es pública.
- Todo lo que cuelga de `MainLayout` (`/home`, Commercial, Production, Inventory, Plotter, Finance, Intelligence) exige autenticación.
- `/` sigue redirigiendo a `/home`; el guard cubre ese caso.
- Usuario no autenticado → `/login` con `state.from` para volver tras login si la ruta es interna y segura; si no, `/home`.
- 401 de API autenticada: una sola redirección (flag anti-bucle). No snackbar de expiración. El 401 de login se muestra en la página y no redirige.

---

## 18. Inicialización

Al arrancar:

1. Sin token → `unauthenticated`.
2. Con token → un `GET /auth/me` (no en cada cambio de ruta).
3. `/me` 200 → sesión restaurada.
4. `/me` 401 → se limpia el estado.
5. `/me` error de red → se conserva identidad almacenada para no expulsar al usuario si el backend está caído.

Mientras `status === initializing` se muestra `AuthLoadingScreen` (no se pinta el shell protegido).

---

## 19. Logout e identidad

- Botón **Cerrar sesión** en el `AppBar` (junto al username).
- Limpia storage, marca `unauthenticated` y navega a `/login`.
- No hay endpoint de logout en el backend (JWT stateless). Documentado.
- Identidad expuesta por `useAuth().identity`: `userId`, `username`, `role`.
- El rol se usa solo para UX: ocultar Finanzas en el sidebar, ocultar «Configurar costo unitario» y mostrar «Sin permisos» en rutas admin-only. El backend sigue siendo autoritativo.

---

## 20. Fuera de alcance (Increment 2)

- Autorización por rol / permisos
- Registro, recuperación de password, OAuth, 2FA
- Refresh tokens
- Cookie httpOnly
- Rediseño visual de módulos de negocio
- Framework de tests frontend

---

## 21. Plan Increment 3 (ejecutado)

Autorización V1 interna:

- Roles `ADMIN` y `OPERATOR` (sin CLIENT/CUSTOMER)
- Restricción centralizada en `SecurityFilterChain`
- UX mínima en el frontend (navegación y acciones sensibles)
- Corrección del bootstrap del primer administrador
- Sin rediseñar autenticación ni el modelo JWT
- Sin convertir Administration en un módulo de HR

---

## 22. Verificación Increment 2

- Frontend `npm run lint`: OK
- Frontend `npm run build`: OK
- Backend `mvnw test`: **341 tests, 0 failures**, BUILD SUCCESS
- Backend en ejecución (`:8080`) + Vite (`:5173`)

QA manual estructurada:

| Id | Resultado |
|----|-----------|
| A `/login` sin auth | HTTP 200, SPA `root` presente |
| B `/home` sin auth | Guard React redirige a `/login` (código). API `GET /home/dashboard` sin token → 401 |
| C credenciales inválidas | 401 `Invalid credentials.` UI mapea a «Usuario o contraseña incorrectos.» |
| D login válido | 200, `accessToken` presente, sin `passwordHash` |
| E dashboard autenticado | `GET /api/v1/home/dashboard` → 200 |
| F módulos | quotations, production-orders, inventory, plotter/jobs, finance/transactions → 200 con Bearer |
| G/H logout | Frontend limpia `sessionStorage` y navega a `/login`; rutas protegidas quedan detrás del guard |
| I token inválido | `GET /auth/me` con Bearer inválido → 401; interceptor limpia estado y redirige |
| J refresh | Token en `sessionStorage`; al arrancar se llama `/auth/me` una vez |
| K password en storage | Solo `magyen.auth.accessToken` e `identity` (`userId`, `username`, `role`) |
| L logs | No se loguean passwords, tokens ni headers Authorization en el frontend |

Logout backend: no existe endpoint; JWT stateless. Logout es operación de frontend.

---

## 23. Decisión de producto: usuarios internos

Magyen Platform es una aplicación **interna**.

Los clientes del módulo Commercial son entidades de negocio (pedidos, cotizaciones, cartera). **No son usuarios de la aplicación.**

V1 no incluye cuentas CLIENT/CUSTOMER. No hay login de cliente.

Los únicos roles de autenticación son personal Magyen: `ADMIN` y `OPERATOR`.

---

## 24. Modelo de roles V1

Se extendió `AuthenticationRole` (antes solo `USER`). No se creó un modelo competidor ni un catálogo de permisos.

| Rol | Significado |
|-----|-------------|
| `ADMIN` | Administrador interno sin restricción V1 |
| `OPERATOR` | Usuario operativo interno |

`USER` se eliminó. Filas existentes con `role = 'USER'` se migran de forma aditiva a `OPERATOR`.

El JWT sigue emitiendo el claim `role` (`ADMIN` o `OPERATOR`). El filtro asigna `ROLE_{role}` al `Authentication` de Spring Security. No se incluyen listas de permisos ni datos de negocio en el token.

---

## 25. Matriz de autorización V1

Leyenda: **A** = ADMIN, **O** = OPERATOR, **P** = público, **—** = denegado.

### Home

| Operación | A | O | P |
|-----------|---|---|---|
| `GET /api/v1/home/dashboard` | sí | sí | — |

### Commercial

| Operación | A | O | P |
|-----------|---|---|---|
| Lectura customers / quotations / orders / profitability | sí | sí | — |
| Alta y actualización de customers / quotations / orders | sí | sí | — |
| Pagos de pedido `GET/POST /api/v1/payments` | sí | sí | — |

Los pagos de pedido viven en `/api/v1/payments`, no bajo `/api/v1/finance/**`. Son flujo comercial operativo, no administración contable.

### Production

| Operación | A | O | P |
|-----------|---|---|---|
| Lectura de órdenes de producción y operadores | sí | sí | — |
| Ciclo de vida (plan/start/complete, operaciones) | sí | sí | — |
| Consumo de material | sí | sí | — |
| Registro / pago / cancelación de labor de producción | sí | sí | — |

El pago de labor de producción no es nómina. Es operación de planta.

### Inventory

| Operación | A | O | P |
|-----------|---|---|---|
| Lectura de materiales y movimientos | sí | sí | — |
| Alta de material | sí | sí | — |
| Movimientos / stock min / increase / decrease | sí | sí | — |
| `PATCH /api/v1/inventory/{id}/unit-cost` | sí | — | — |

Cambiar el costo unitario altera valoración. Queda restringido a ADMIN.

### Plotter

| Operación | A | O | P |
|-----------|---|---|---|
| Lectura y creación de jobs | sí | sí | — |
| Pagos de Plotter | sí | sí | — |

No hay endpoint separado de consumo de papel; forma parte del flujo de job / inventario operativo.

### Finance (`/api/v1/finance/**`)

| Operación | A | O | P |
|-----------|---|---|---|
| Lectura y alta de transacciones manuales | sí | — | — |
| Obligaciones recurrentes (CRUD / deactivate) | sí | — | — |
| Generación / pago / cancelación de ocurrencias | sí | — | — |
| Nómina (empleados, periodos, pay/cancel) | sí | — | — |
| Resumen financiero | sí | — | — |

OPERATOR no administra contabilidad. La rentabilidad operativa de pedido (`GET /orders/{id}/profitability`) permanece accesible porque es flujo Commercial.

### Administration

| Operación | A | O | P |
|-----------|---|---|---|
| `POST /api/v1/auth/login` | — | — | sí |
| `GET /api/v1/auth/me` | sí | sí | — |
| Administración de usuarios | no existe en V1 | | |

### Intelligence (V2, no está en el sidebar)

| Operación | A | O | P |
|-----------|---|---|---|
| `GET /api/v1/reports/**` | sí | — | — |
| `GET /api/v1/notifications` | sí | — | — |

Ningún API de negocio queda pública.

---

## 26. Clasificación de endpoints

**Público**

- `POST /api/v1/auth/login`
- `/error`

**ADMIN only**

- `/api/v1/finance/**` (transactions, obligations, obligation-occurrences, payroll employees/periods, summary)
- `PATCH /api/v1/inventory/{inventoryItemId}/unit-cost`
- `/api/v1/reports/**`
- `/api/v1/notifications`

**Autenticado (ADMIN o OPERATOR)**

- `GET /api/v1/auth/me`
- `/api/v1/home/**`
- `/api/v1/customers/**`
- `/api/v1/quotations/**`
- `/api/v1/orders/**`
- `/api/v1/payments/**`
- `/api/v1/production-orders/**`
- `/api/v1/production/labor-operators`
- `/api/v1/inventory/**` excepto `PATCH .../unit-cost`
- `/api/v1/plotter/jobs/**`

---

## 27. Estrategia de autorización backend

La autorización se aplica en `SecurityFilterChain` (`config.security`), no en el dominio.

Motivo:

- Los tests MockMvc de negocio construyen el contexto **sin** `springSecurity()`. Anotar `@PreAuthorize` en controladores rompería esos tests o exigiría tocar cada módulo.
- Las reglas de rol no entran en use cases ni entidades de negocio.
- Un matcher de URL es el punto único de política V1.

`JsonAccessDeniedHandler` responde 403 con `ErrorResponse`. `JsonAuthenticationEntryPoint` responde 401. `GlobalExceptionHandler` cubre `AccessDeniedException` si llega al MVC.

Los use cases no contienen `if (role == ADMIN)`.

---

## 28. JWT y roles

Claims sin cambio de diseño: `sub`, `username`, `role`, `iat`, `exp`.

El claim `role` se valida con `AuthenticationRole.valueOf`. Un valor desconocido (`USER`, `CLIENT`, payload alterado) produce token inválido → **401**.

Un JWT de `OPERATOR` firmado correctamente no puede acceder a rutas ADMIN → **403**. Alterar el payload sin re-firmar invalida la firma → **401**.

La expiración sigue aplicándose. No hay lista de permisos en el token.

Si el rol cambia en base de datos, `GET /auth/me` devuelve el rol persistido (el frontend lo refresca al inicializar). Las authorities del JWT actual siguen siendo las del login hasta que expire o se vuelva a autenticar. Es el modelo stateless V1.

---

## 29. Semántica 401 vs 403

| Situación | HTTP | Cuerpo |
|-----------|------|--------|
| Sin token, token inválido, token expirado, claim `role` desconocido | 401 Unauthorized | `ErrorResponse` — `Authentication is required.` (filtro) o `Invalid credentials.` (login) |
| Usuario autenticado sin permiso | 403 Forbidden | `ErrorResponse` — `You do not have permission to perform this action.` |

No se devuelve 401 a un usuario válido que solo carece de rol. No se filtran detalles internos de la matriz.

El interceptor Axios **solo** trata 401 como fin de sesión. Un 403 no cierra la sesión.

---

## 30. Autorización frontend (solo UX)

- `isAdmin(identity)` en `authPresentation.js`.
- Sidebar: Finanzas visible solo para ADMIN. Home, Comercial, Producción, Inventario y Plotter para ambos. Intelligence no vuelve al sidebar. Administration no es un módulo de negocio.
- `/finance` e `/intelligence` envueltos en `AdminOnlyPage` («Sin permisos»).
- Inventario: el botón «Configurar costo unitario» solo para ADMIN.
- No se duplica la matriz completa en React. El backend autoriza.

---

## 31. Bootstrap del primer administrador

### Defecto observado en Increment 2

Con `AUTH_BOOTSTRAP_ENABLED=true` no se creó usuario. QA insertó una fila local.

Causas concurrentes, no un solo fallo:

1. **Boolean de entorno en Windows:** un valor `true` con `\r` o espacios hace que `Boolean.parseBoolean` sea `false`.
2. **Nombres relajados de Spring Boot:** `AUTH_BOOTSTRAP_ENABLED` en `.env` puede resolverse como `auth.bootstrap.enabled`. El placeholder YAML `${AUTH_BOOTSTRAP_ENABLED}` no siempre ve esa clave, así que `magyen.security.bootstrap.enabled` quedaba en `false`.
3. **Transacción:** el adaptador JPA de Administration no declaraba `@Transactional`. `ApplicationRunner` no es un request HTTP; sin transacción explícita el `save` puede no confirmarse.

No fue «configuración mal usada» de forma exclusiva. Había un defecto de implementación.

### Corrección Increment 3

- `AuthenticationBootstrapProperties` (`magyen.security.bootstrap`) + fallback a `AUTH_BOOTSTRAP_*`.
- `trim()` antes de `Boolean.parseBoolean` y de username/password/role.
- `TransactionTemplate` para confirmar el insert.
- Rol por defecto **ADMIN** (el primer usuario de un entorno limpio debe poder operar Finanzas).
- Logs: disabled / created / already exists / blank credentials / invalid role. Nunca password, hash ni secreto.
- Tests unitarios del runner. Los `@SpringBootTest` fuerzan `magyen.security.bootstrap.enabled=false` para no confirmar usuarios fuera de `@Transactional`.

Variables (`.env.example`):

```
AUTH_BOOTSTRAP_ENABLED=false
AUTH_BOOTSTRAP_USERNAME=local-admin
AUTH_BOOTSTRAP_PASSWORD=change-me
AUTH_BOOTSTRAP_ROLE=ADMIN
```

No hay password por defecto en el código fuente. Un entorno V1 limpio crea el primer ADMIN con estas variables, sin SQL manual.

`.env` debe tener saltos de línea reales (no concatenar líneas).

---

## 32. Tests de seguridad Increment 3

`AuthorizationApiContractTest` (MockMvc + `springSecurity()`):

| Id | Escenario |
|----|-----------|
| A | ADMIN accede a endpoints V1 autorizados (home, commercial, production, inventory, plotter, finance, reports) |
| B | OPERATOR accede a endpoints operativos |
| C | OPERATOR → 403 en administración Finance |
| D | OPERATOR no modifica costo unitario → 403 |
| E | OPERATOR no paga nómina → 403 |
| F | OPERATOR no paga obligaciones recurrentes → 403 |
| G | Sin autenticación → 401 |
| H | Autenticado sin permiso → 403 |
| I | Login sigue público |
| J | JWT inválido / expirado → 401 |
| K | Claim `role` alterado no bypass → 401; `CLIENT` → 401 |

`AuthenticationUserBootstrapTest` cubre disabled, blank, existing, create ADMIN, invalid role, fallback de entorno y hash (sin plaintext).

Los tests de negocio existentes siguen construyendo MockMvc sin la cadena de seguridad y deben permanecer verdes.

---

## 33. Fuera de alcance (Increment 3)

- Refresh tokens, OAuth, 2FA, recuperación de password, verificación de email
- UI de administración de usuarios / permisos
- Roles CLIENT/CUSTOMER o catálogo fino de permisos
- Rediseño de JWT / login / sessionStorage
- Intelligence en el sidebar V1
- Relación usuario ↔ empleado
- Cambio de comportamiento de dominio de negocio

---

## 34. Plan Increment 4 (ejecutado)

Administración de identidades internas, sin abrir login de cliente:

- API y UI mínima para listar / crear / habilitar / deshabilitar usuarios internos
- Asignar `ADMIN` o `OPERATOR` a un usuario existente
- Reconciliar enabled/rol persistidos en cada request autenticada (sin revocation list)
- Protección del último ADMIN activo
- Sin catálogo de permisos, sin OAuth, sin 2FA

---

## 35. Verificación Increment 3

- Backend `mvnw compile`: OK
- Backend `mvnw test`: **361 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS
- Frontend `npm run lint`: OK
- Frontend `npm run build`: OK
- Docker `magyen-postgres`: `UPDATE users SET role = 'OPERATOR' WHERE role = 'USER'` (2 filas). `local-admin` promovido a `ADMIN`. Sin DROP/TRUNCATE. Sin cambio de esquema. DBeaver no es obligatorio.

Live API (`:8080`):

| Caso | Resultado |
|------|-----------|
| Login ADMIN (`local-admin`) | 200, `role=ADMIN` |
| ADMIN `GET /auth/me`, `/home/dashboard`, `/finance/transactions` | 200 |
| OPERATOR `GET /home/dashboard`, `/customers` | 200 |
| OPERATOR `GET /finance/transactions` | 403 |
| OPERATOR `PATCH .../unit-cost` | 403 |
| OPERATOR `PATCH .../payroll/periods/{id}/pay` | 403 |
| OPERATOR `PATCH .../obligation-occurrences/{id}/pay` | 403 |
| Sin token `GET /home/dashboard` | 401 |
| JWT inválido | 401 |
| `POST /auth/login` público (credenciales inválidas) | 401 `Invalid credentials.` |
| SPA `/login` | 200 |

Bootstrap tras reinicio completo del JVM: habilitado; usuario ya existía → `already exists; skipping`. Un arranque previo falló con `role is invalid` porque el proceso tenía `AUTH_BOOTSTRAP_ROLE=USER` (el entorno del proceso gana a `.env`). Corregido a `ADMIN`.

---

## 36. Recomendación Increment 3

**APPROVE**

No iniciar Increment 4 automáticamente.

---

## 37. Administración interna de usuarios (Increment 4)

Los usuarios de la aplicación siguen siendo personal interno Magyen. No hay cuentas de cliente.

Se reutiliza el agregado `AuthenticationUser`. No hay un segundo modelo de usuario.

### Ciclo de vida

| Operación | Efecto |
|-----------|--------|
| Crear | Username + password + rol. Queda `enabled=true`. Password hasheado con BCrypt antes de persistir |
| Listar | `GET /api/v1/admin/users` ordenado por username. Sin hash ni password |
| Activar | Idempotente si ya está activo |
| Desactivar | No elimina. Login y tokens existentes dejan de autenticar |
| Cambiar rol | Solo `ADMIN` ↔ `OPERATOR` |

No hay borrado. No hay registro público.

### Protección del último ADMIN

Debe existir al menos un ADMIN activo (`enabled=true` y `role=ADMIN`).

- Desactivar el último ADMIN activo → 400 `The last active administrator cannot be deactivated.`
- Demotar el último ADMIN activo a OPERATOR → 400 `The last active administrator cannot be demoted.`

### Política de password V1

- Obligatorio
- Mínimo 8 caracteres
- Máximo 72 (límite BCrypt)
- Nunca se persiste plaintext
- Nunca se loguea
- Nunca se devuelve en la API

Username: trim, único, máximo 100 caracteres (regla existente). Duplicado → 409.

### JWT / sesión

Claims del token: sin cambio (`sub`, `username`, `role`, `iat`, `exp`). No hay refresh ni revocation list.

Tras validar firma y expiración, el filtro reconcilia el principal contra la base:

- usuario inexistente o `enabled=false` → el request queda sin autenticación → **401**
- el **rol persistido** se usa para las authorities de Spring Security

Consecuencia: un JWT emitido a un OPERATOR que luego es promovido a ADMIN puede usar APIs de ADMIN de inmediato. Un JWT de un usuario deshabilitado no sirve aunque no haya expirado.

`GET /auth/me` sigue leyendo la base. El frontend refresca la identidad tras un cambio de rol/estado sobre el usuario actual.

El claim `role` dentro del JWT puede quedar desactualizado hasta que expire; no gobierna la autorización.

### API ADMIN-only

Prefijo `/api/v1/admin/users`. Matcher `hasRole("ADMIN")`.

| Método | Ruta | HTTP |
|--------|------|------|
| GET | `/api/v1/admin/users` | 200 `{ users: [{ id, username, role, enabled }] }` |
| POST | `/api/v1/admin/users` | 201; 400 inválido; 409 duplicado |
| PATCH | `/api/v1/admin/users/{userId}/activate` | 200 |
| PATCH | `/api/v1/admin/users/{userId}/deactivate` | 200; 400 último ADMIN |
| PATCH | `/api/v1/admin/users/{userId}/role` | 200; 400 rol inválido o último ADMIN |

OPERATOR → 403. Sin token → 401.

### Frontend

Ruta `/admin/users`. Entrada de sidebar **Usuarios** solo para ADMIN. OPERATOR que navega directo ve «Sin permisos»; la API sigue respondiendo 403.

La página lista usuarios, crea con diálogo (usuario / contraseña / rol), cambia rol y activa/desactiva (confirmación al desactivar). No muestra passwords ni hashes.

### Bootstrap

Sin cambio de responsabilidad: solo crea el primer ADMIN si no existe. No crea usuarios adicionales en cada arranque. Tras el primer ADMIN, la gestión es por esta API/UI.

### Tests Increment 4

`UserAdministrationApiContractTest` cubre listar, crear ADMIN/OPERATOR, duplicado 409, OPERATOR 403, desactivar/activar, login de deshabilitado, cambio de rol, último ADMIN, rol inválido, hash ausente, reconciliación de rol en JWT existente.

---

## 38. Fuera de alcance (Increment 4)

- Refresh tokens, OAuth, 2FA, recuperación de password
- Catálogo de permisos o roles CLIENT/CUSTOMER
- Cambio de password por el propio usuario
- Auditoría de quién creó/deshabilitó una cuenta
- Relación usuario ↔ empleado de nómina
- Intelligence en el sidebar

---

## 39. Plan Increment 5 (no iniciado)

Endurecimiento operativo de identidades, sin abrir login de cliente:

- Cambio de password por ADMIN y, si aplica, por el propio usuario autenticado
- Auditoría mínima (quién creó, deshabilitó o cambió el rol)
- Mensaje de UI cuando el rol propio acaba de cambiar
- Sin revocation list, sin OAuth, sin 2FA

---

## 40. Verificación Increment 4

- Backend `mvnw compile`: OK
- Backend `mvnw test`: **376 tests, 0 failures, 0 errors, 0 skipped**, BUILD SUCCESS
- Frontend `npm run lint`: OK
- Frontend `npm run build`: OK
- Docker: sin cambio de esquema. `local-admin` y `qa-operator` intactos. Usuario de QA `inc4-live-operator` creado y dejado desactivado. DBeaver no requerido.

Live API (`:8080`):

| Caso | Resultado |
|------|-----------|
| Login ADMIN | 200, `role=ADMIN` |
| ADMIN lista usuarios | 200, sin `password`/`passwordHash` |
| ADMIN crea OPERATOR | 201, sin hash |
| Login OPERATOR creado | 200 |
| OPERATOR `GET /admin/users` | 403 |
| Último ADMIN desactivar/demotar | 400 |
| ADMIN cambia rol OPERATOR ↔ ADMIN | 200 |
| Usuario deshabilitado login / token | 401 |
| Sin token `GET /admin/users` | 401 |

---

## 41. Reset de base de datos V1 (solo preparación)

El procedimiento de reset limpio de Magyen V1 **no pertenece a este documento de autenticación**.

Documento canónico (preparado, **no ejecutado**):

`docs/12-V1-Database-Reset.md`

Tras el reset futuro, la tabla `users` quedará vacía y el primer ADMIN se creará únicamente por bootstrap (`AUTH_BOOTSTRAP_*`). No se deben reutilizar `qa-operator` ni `inc4-live-operator`.




