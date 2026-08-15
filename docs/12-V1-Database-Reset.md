# Magyen V1 — Procedimiento de reset limpio de base de datos

**Estado:** Prepared only — not executed.  
**Fecha de preparación:** 15 de agosto de 2026  
**Rama al preparar:** `main`  
**Contenedor:** `magyen-postgres`  
**Base:** `magyen_platform`  
**Volumen Docker:** `magyen-platform_postgres_data`

Este documento es un procedimiento operativo para ejecutar **más adelante**, solo después de aprobación explícita.

**No se ha ejecutado ningún reset.**  
**No se ha modificado ningún dato.**  
**No se ha recreado el volumen Docker.**  
**No se ha reiniciado ninguna secuencia.**  
**No se ha creado el ADMIN de V1.**  
**No se han ingresado datos reales de negocio.**

---

## 0. Alcance

Objetivo del reset futuro:

- Base Magyen V1 vacía, con el esquema actual intacto.
- Sin datos de QA/test acumulados en SPR-034 … SPR-038.
- Secuencias de numeración en su estado inicial V1.
- Bootstrap de autenticación listo para crear el primer ADMIN.
- Home sigue siendo solo lectura: no tiene tablas propias.

Esto **no** es un rediseño de esquema.  
`schema.sql` sigue siendo el dueño del DDL. Hibernate permanece en `ddl-auto: validate`.

---

## 1. Inventario del esquema actual (inspección 15/08/2026)

### 1.1 Conteos

| Fuente | Tablas | Secuencias | Triggers de usuario | Vistas | Funciones public |
|--------|--------|------------|---------------------|--------|------------------|
| Live `magyen_platform` | **23** | **2** | 0 | 0 | 0 |
| `backend/src/main/resources/db/schema.sql` | **23** | **2** | 0 | 0 | 0 |
| Entidades JPA `*Entity.java` | **23** | — | — | — | — |

No hay columnas `IDENTITY`/`SERIAL`. Todas las PK de negocio son UUID.

### 1.2 Tablas live (orden alfabético)

customers, financial_transactions, inventory_items, inventory_movements, order_item_sizes, order_items, orders, payments, payroll_employees, payroll_periods, plotter_jobs, plotter_payments, production_item_sizes, production_items, production_labor_work, production_material_consumptions, production_operations, production_orders, quotation_items, quotations, recurring_financial_obligation_occurrences, recurring_financial_obligations, users

### 1.3 Secuencias live

| Secuencia | start | increment | last_value actual | is_called | Owned by column |
|-----------|-------|-----------|-------------------|-----------|-----------------|
| `quotation_number_seq` | 1 | 1 | **1047** | true | **ninguna** |
| `paper_roll_number_seq` | 1 | 1 | **902** | true | **ninguna** |

`TRUNCATE … RESTART IDENTITY` **no** reinicia estas secuencias: no están ligadas a una columna SERIAL/IDENTITY. Tras un reset hay que recrearlas (Opción B) o hacer `ALTER SEQUENCE … RESTART` (Opción A).

### 1.4 Claves foráneas reales (PostgreSQL)

Solo **9 FK**. El resto de referencias entre módulos son blandas (UUID sin FK), por diseño.

| Tabla hija | Columna | Padre | ON DELETE |
|------------|---------|-------|-----------|
| `quotation_items` | `quotation_id` | `quotations` | CASCADE |
| `order_items` | `order_id` | `orders` | CASCADE |
| `order_item_sizes` | `order_item_id` | `order_items` | CASCADE |
| `production_items` | `production_order_id` | `production_orders` | CASCADE |
| `production_item_sizes` | `production_item_id` | `production_items` | CASCADE |
| `production_operations` | `production_order_id` | `production_orders` | CASCADE |
| `production_material_consumptions` | `production_order_id` | `production_orders` | CASCADE |
| `production_labor_work` | `production_order_id` | `production_orders` | CASCADE |
| `inventory_movements` | `inventory_item_id` | `inventory_items` | **NO ACTION** |

Sin FK (referencias lógicas / unique solamente):

- `quotations.customer_id` → customers
- `orders.customer_id` / `orders.quotation_id` → customers / quotations (`orders_quotation_id_key` UNIQUE)
- `production_orders.order_id` → orders (`production_orders_order_id_key` UNIQUE)
- `payments.order_id` → orders
- `plotter_jobs.customer_id` / `paper_inventory_item_id`
- `plotter_payments.plotter_job_id`
- `financial_transactions.source_id`
- `recurring_financial_obligation_occurrences.recurring_obligation_id`
- `payroll_periods.employee_id`
- `users` — independiente

### 1.5 Drift menor live vs `schema.sql`

Live tiene el índice extra `idx_orders_quotation_id`.  
`schema.sql` documenta que las búsquedas por `quotation_id` las cubre `orders_quotation_id_key` (UNIQUE) y **no** crea ese índice.

Tras Opción B ese índice extra desaparece. Eso alinea live con `schema.sql`. Hibernate `validate` no exige índices extra.

No hay otras tablas, vistas, triggers ni funciones fuera de `schema.sql`.

### 1.6 Filas actuales (datos desechables de QA)

Conteos exactos `COUNT(*)` al preparar este documento:

| Tabla | Filas |
|-------|------:|
| customers | 10 |
| quotations | 17 |
| quotation_items | 12 |
| orders | 4 |
| order_items | 6 |
| order_item_sizes | 2 |
| production_orders | 3 |
| production_items | 4 |
| production_item_sizes | 2 |
| production_operations | 3 |
| production_material_consumptions | 7 |
| production_labor_work | 2 |
| inventory_items | 303 |
| inventory_movements | 58 |
| plotter_jobs | 5 |
| plotter_payments | 3 |
| payments | 5 |
| financial_transactions | 13 |
| recurring_financial_obligations | 2 |
| recurring_financial_obligation_occurrences | 3 |
| payroll_employees | 5 |
| payroll_periods | 6 |
| users | 3 |

Usuarios actuales (QA, no V1 de negocio):

| username | role | enabled |
|----------|------|---------|
| local-admin | ADMIN | true |
| qa-operator | OPERATOR | true |
| inc4-live-operator | OPERATOR | false |

Home no persiste datos.

---

## 2. Clasificación de tablas

### A. Datos operativos/negocio — DEBEN vaciarse

Todas las tablas de Commercial, Production, Inventory, Plotter y Finance. Son registros de prueba/QA. No hay catálogo estático de sistema.

| Tabla | Por qué |
|-------|---------|
| customers | Clientes de prueba. Commercial es dueño. |
| quotations / quotation_items | Cotizaciones QA. |
| orders / order_items / order_item_sizes | Órdenes comerciales QA. |
| production_orders / production_items / production_item_sizes / production_operations / production_material_consumptions / production_labor_work | Producción QA (incluye consumos y labor). |
| inventory_items / inventory_movements | Stock, rollos de papel y movimientos QA. |
| plotter_jobs / plotter_payments | Trabajos y pagos Plotter QA. |
| payments | Pagos de cliente QA (Finance/Commercial). |
| financial_transactions | Ledger QA. |
| recurring_financial_obligations / recurring_financial_obligation_occurrences | Obligaciones QA. |
| payroll_employees / payroll_periods | Nómina QA. No son usuarios de login. |

### B. Autenticación/bootstrap — DEBE vaciarse

| Tabla | Por qué |
|-------|---------|
| users | Identidades QA (`local-admin`, `qa-operator`, `inc4-live-operator`). El primer ADMIN V1 lo crea el bootstrap, no un INSERT manual. |

### C. Estructuras de esquema — NO eliminar

- Las 23 tablas
- PK / UNIQUE / CHECK implícitos
- 9 FK
- Índices de `schema.sql` (incluidos índices parciales únicos de Finance/Inventory)
- Secuencias `quotation_number_seq` y `paper_roll_number_seq` (reiniciar, no DROP permanente)
- Extensiones/roles de Postgres del contenedor
- Volumen Docker **después** del reset (se recrea vacío; no se borra el motor)

### D. Datos de referencia/configuración a preservar

**Ninguno.** No hay tablas de catálogo, parámetros ni seed de negocio. Los únicos “parámetros” V1 viven en `.env` (`JWT_*`, `AUTH_BOOTSTRAP_*`, `DB_*`).

### E. Tablas que podrían estar vacías y aun así entran en el reset

Hoy no hay tablas a cero, pero el procedimiento debe incluir **las 23** siempre, para que un reset futuro no deje residuos si alguna queda vacía.

---

## 3. Estrategia recomendada

### Opción A — `TRUNCATE … CASCADE` + `ALTER SEQUENCE`

Pros: no toca el volumen Docker; el contenedor sigue existiendo.  
Contras: hay que enumerar 23 tablas; las 2 secuencias **no** se reinician con `RESTART IDENTITY` porque no tienen owner; riesgo de olvidar una tabla o secuencia nueva.

### Opción B — Recrear el volumen Postgres y dejar que `schema.sql` inicialice

`docker-compose.yml` monta:

`./backend/src/main/resources/db/schema.sql` → `/docker-entrypoint-initdb.d/schema.sql`

Ese script **solo corre** cuando el data dir está vacío (primer init del volumen).

Pros:

- Reproduce exactamente “base V1 fresca”.
- Las 23 tablas y 2 secuencias salen de `schema.sql` (`START WITH 1`).
- Compatible con `ddl-auto: validate`.
- No depende de recordar `ALTER SEQUENCE`.
- Repetible: borrar volumen + `up` = mismo resultado.

Contras:

- Hay que identificar el volumen por nombre y no usar `docker volume prune` / `docker system prune`.
- El índice extra live `idx_orders_quotation_id` desaparece (deseable: alinea con `schema.sql`).

### Recomendación: **Opción B**

Es la que el propio repositorio ya implementa (init por `schema.sql`). Es la más reproducible para el punto de partida oficial de Magyen V1.

La Opción A queda documentada como fallback si, en el momento de ejecutar, no se puede recrear el volumen.

**No usar** `docker system prune`, `docker volume prune`, ni `down -v` si el compose llega a tener más volúmenes. Hoy el único volumen del compose es `postgres_data` → nombre live `magyen-platform_postgres_data`.

---

## 4. Numeración de negocio (sin cambiar lógica)

| Identificador | Mecanismo | Tras el reset |
|---------------|-----------|----------------|
| Cotización | Secuencia `quotation_number_seq` + VO `QuotationNumber` (long). UI: `C000001` | Primer `nextval` = 1 → UI `C000001` |
| Orden comercial | Texto ingresado por el usuario (`orderNumber`). Sin secuencia | El usuario escribe el número; no hay autonumeración |
| Orden de producción | UUID interno. UI usa `orderNumber` comercial (1:1) | Sin número PROD-##### |
| Código de material | Texto ingresado (`materialCode`) | El usuario lo escribe |
| Rollo de papel | `paper_roll_number_seq` → `RP-%03d` | Primer rollo `RP-001` |
| Plotter job | UUID | Sin número de negocio |
| Usuario | UUID + username | Primer ADMIN por bootstrap |
| Pagos, ledger, obligaciones, nómina | UUID | Sin secuencia |

No inventar formatos. No modificar generadores.

SQL que **se ejecutaría** solo en Opción A (no ejecutar ahora):

```sql
ALTER SEQUENCE quotation_number_seq RESTART WITH 1;
ALTER SEQUENCE paper_roll_number_seq RESTART WITH 1;
```

En Opción B no hace falta: `schema.sql` crea ambas con `START WITH 1`.

---

## 5. Autenticación / bootstrap

El reset deja `users` vacía. **No** crear el ADMIN a mano.

Flujo oficial:

1. Reset de base (Opción B).
2. Confirmar `.env` V1 (secretos reales, no los placeholders de ejemplo).
3. Arrancar backend con `AUTH_BOOTSTRAP_ENABLED=true`.
4. Bootstrap crea el primer ADMIN **solo si** ese username no existe y la tabla está vacía respecto a ese usuario.
5. Verificar login ADMIN y `GET /api/v1/auth/me` (sin `password` / `passwordHash`).
6. Poner `AUTH_BOOTSTRAP_ENABLED=false` y reiniciar el backend para no crear otro usuario si cambia el username del `.env`.

Variables requeridas (valores en `.env` local; **no** copiar secretos a git):

| Variable | Rol V1 |
|----------|--------|
| `AUTH_BOOTSTRAP_ENABLED` | `true` solo en el primer arranque post-reset |
| `AUTH_BOOTSTRAP_USERNAME` | Username del primer ADMIN V1 (no usar `qa-operator`) |
| `AUTH_BOOTSTRAP_PASSWORD` | Password fuerte; se hashea con BCrypt |
| `AUTH_BOOTSTRAP_ROLE` | `ADMIN` |
| `JWT_SECRET` | ≥ 32 caracteres |
| `JWT_EXPIRATION_MS` | `3600000` (1 h) salvo decisión distinta |
| `DB_USERNAME` / `DB_PASSWORD` | Deben coincidir con el contenedor Postgres |

Bootstrap **no se modifica** en esta preparación.

---

## 6. Procedimiento futuro (fases)

Sustituir `YYYYMMDD-HHMMSS` por el timestamp real al ejecutar.

### PHASE 0 — Preconditions

- [ ] Directorio: `C:\Projects\Magyen-Platform`
- [ ] Rama confirmada (`git branch --show-current`)
- [ ] Contenedor `magyen-postgres` identificado (`docker ps --filter name=magyen-postgres`)
- [ ] Volumen `magyen-platform_postgres_data` identificado (`docker volume inspect magyen-platform_postgres_data`)
- [ ] Base `magyen_platform`, usuario de conexión `magyen`
- [ ] Backend Spring **detenido** (si no, hay conexiones y el volumen puede no liberarse)
- [ ] Frontend irrelevante para el reset (puede quedar en Vite)
- [ ] Este entorno **no** es producción AWS
- [ ] Todos los datos actuales confirmados como QA desechable
- [ ] `.env` V1 preparado (bootstrap `true`, rol `ADMIN`, JWT ≥ 32 chars)
- [ ] Backup pendiente de PHASE 1

### PHASE 1 — Backup (no modifica la base)

PowerShell, desde la raíz del repo:

```powershell
New-Item -ItemType Directory -Force -Path .\backups | Out-Null
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupName = "magyen_platform_pre_v1_reset_$stamp.dump"
docker exec magyen-postgres pg_dump -U magyen -d magyen_platform -Fc -f "/tmp/$backupName"
docker exec magyen-postgres pg_restore -l "/tmp/$backupName"
docker cp "magyen-postgres:/tmp/$backupName" ".\backups\$backupName"
docker exec magyen-postgres rm "/tmp/$backupName"
Get-Item ".\backups\$backupName"
if ((Get-Item ".\backups\$backupName").Length -le 0) { throw 'Backup vacío' }
```

`pg_restore -l` lista el TOC del dump **antes** de borrarlo del contenedor. No modifica datos.

El dump es formato custom (`-Fc`). No commitear: `backups/` está en `.gitignore`.

**No ejecutar el backup en esta preparación** salvo petición explícita.

### PHASE 2 — Reset (Opción B, recomendada)

```powershell
docker compose stop postgres
docker compose rm -f postgres
docker volume rm magyen-platform_postgres_data
docker compose up -d postgres
```

Esperar ready:

```powershell
docker exec magyen-postgres pg_isready -U magyen -d magyen_platform
```

`schema.sql` corre una sola vez sobre el volumen vacío.

### PHASE 2 (fallback Opción A — solo si no se puede borrar el volumen)

**No ejecutar ahora.** Backend detenido. Lista explícita de las 23 tablas:

```sql
BEGIN;

TRUNCATE TABLE
    production_item_sizes,
    production_items,
    production_operations,
    production_material_consumptions,
    production_labor_work,
    production_orders,
    order_item_sizes,
    order_items,
    orders,
    quotation_items,
    quotations,
    payments,
    plotter_payments,
    plotter_jobs,
    inventory_movements,
    inventory_items,
    financial_transactions,
    recurring_financial_obligation_occurrences,
    recurring_financial_obligations,
    payroll_periods,
    payroll_employees,
    customers,
    users
RESTART IDENTITY CASCADE;

ALTER SEQUENCE quotation_number_seq RESTART WITH 1;
ALTER SEQUENCE paper_roll_number_seq RESTART WITH 1;

COMMIT;
```

`CASCADE` cubre las 9 FK. Las tablas sin FK se listan igual para no dejar residuos.

### PHASE 3 — Secuencias

Opción B: verificar, no alterar.

```sql
SELECT last_value, is_called FROM quotation_number_seq;
SELECT last_value, is_called FROM paper_roll_number_seq;
```

Esperado tras init fresco: `last_value = 1`, `is_called = false`.  
El primer `nextval` devolverá 1.

Opción A: los `ALTER SEQUENCE` del bloque anterior.

### PHASE 4 — Verificación de esquema

```sql
SELECT COUNT(*) FROM information_schema.tables
 WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
-- 23

SELECT sequencename FROM pg_sequences WHERE schemaname = 'public';
-- quotation_number_seq, paper_roll_number_seq

SELECT COUNT(*) FROM users;
-- 0
```

Arrancar backend: Hibernate `ddl-auto: validate` debe pasar. Si falla, **no** improvisar DDL; detener y revisar.

### PHASE 5 — Bootstrap V1

1. `.env`: `AUTH_BOOTSTRAP_ENABLED=true`, `AUTH_BOOTSTRAP_ROLE=ADMIN`, username/password V1.
2. Arrancar backend.
3. Log esperado (sin secretos): bootstrap user created with role ADMIN.
4. `SELECT username, role, enabled FROM users;` → una fila ADMIN enabled.
5. `password_hash` empieza por `$2a$` / `$2b$`. Nunca plaintext.
6. `.env`: `AUTH_BOOTSTRAP_ENABLED=false` y reiniciar backend.

### PHASE 6 — Verificación de autenticación

- `POST /api/v1/auth/login` ADMIN → 200, `role=ADMIN`, sin password/hash.
- `GET /api/v1/auth/me` → identidad, `enabled=true`, sin hash.
- `GET /api/v1/admin/users` → solo el ADMIN bootstrap.
- Credenciales inválidas → 401.
- No deben existir `qa-operator` ni `inc4-live-operator`.

### PHASE 7 — Sistema vacío

| Módulo | Esperado |
|--------|----------|
| Commercial | 0 customers / quotations / orders |
| Production | 0 production orders / consumos / labor |
| Inventory | 0 items / movements; próximo rollo `RP-001` |
| Plotter | 0 jobs / payments |
| Finance | 0 transactions / obligations / payroll |
| Home | estados vacíos ya existentes (sin OP activas, sin cobros, etc.) |
| Cotización nueva | UI `C000001` |

### PHASE 8 — Primera validación de negocio real

**Solo después** de que el reset se apruebe y se ejecute. No crear registros ahora.

Ver sección 7.

---

## 7. Plan de primera validación de negocio real (no ejecutar ahora)

Pedido real de agosto 2026, con el flujo que la aplicación ya permite. Sin inventar numeración.

1. Login ADMIN (bootstrap).
2. Crear **cliente** real (nombre comercial verdadero).
3. Crear **cotización** → debe nacer `C000001`.
4. Ítems, especificación, tallas si aplica; aprobar cotización.
5. Crear **orden comercial** con `orderNumber` ingresado por Magyen (el identificador que usa la familia).
6. Crear **orden de producción** desde esa orden (1:1). Home/Producción deben mostrar `orderNumber` + nombre de cliente, no UUID.
7. **Inventario:** dar de alta materiales reales; si hay papel Plotter, el primer rollo debe ser `RP-001`.
8. Consumir materiales en producción.
9. Registrar **labor** con monto/tarifa manual por trabajo (modelo actual).
10. Completar operaciones y la OP.
11. **Pago de cliente** sobre la orden.
12. Verificar **Finance** (ingreso de orden / ledger) y **rentabilidad**.
13. Verificar **Home** (producción, cobros, alertas, rentabilidad).
14. Si el pedido usa Plotter: trabajo + pago Plotter + rollo `RP-001` (o el siguiente correlativo real).

No es un rediseño. Es validar V1 con un pedido verdadero sobre base limpia.

---

## 8. Rollback / recuperación

El rollback es restaurar el dump de PHASE 1, no “deshacer un TRUNCATE a mano”.

```powershell
# 1. Detener backend
# 2. Recrear volumen vacío (igual que PHASE 2) para tener Postgres escuchando
docker compose stop postgres
docker compose rm -f postgres
docker volume rm magyen-platform_postgres_data
docker compose up -d postgres
# esperar pg_isready

# 3. Copiar dump al contenedor y restaurar (incluye esquema+datos del backup)
docker cp ".\backups\<archivo.dump>" magyen-postgres:/tmp/restore.dump
docker exec magyen-postgres pg_restore -U magyen -d magyen_platform --clean --if-exists --no-owner /tmp/restore.dump
```

`--clean --if-exists` elimina objetos creados por `schema.sql` y deja el estado del dump (QA anterior).  
Si el restore falla a mitad, no improvisar: repetir desde volumen vacío.

No hay PITR. El dump es el único seguro.

---

## 9. Checklist de seguridad (llenar al ejecutar, no ahora)

- [ ] Correct database identified (`magyen_platform`)
- [ ] Correct Docker container identified (`magyen-postgres`)
- [ ] Backup created
- [ ] Backup verified (tamaño > 0 / TOC `pg_restore -l`)
- [ ] No production environment
- [ ] No real business data will be deleted (solo QA actual)
- [ ] All current data confirmed disposable
- [ ] users table intentionally cleared (vía volumen fresco / TRUNCATE)
- [ ] bootstrap configuration prepared
- [ ] all sequences identified (`quotation_number_seq`, `paper_roll_number_seq`)
- [ ] paper_roll_number_seq identified
- [ ] generated business numbering behavior verified (`C000001`, `RP-001`, orderNumber manual)
- [ ] FK dependencies verified (9 FK)
- [ ] schema.sql reviewed (23 tablas, 2 secuencias)
- [ ] ddl-auto validate considered
- [ ] reset commands reviewed (Opción B)
- [ ] rollback/recovery approach documented

---

## 10. Lo que este documento no autoriza

Hasta nueva aprobación explícita, está prohibido:

- `TRUNCATE` / `DELETE` / `DROP`
- `CREATE DATABASE`
- recrear el volumen Docker
- `ALTER SEQUENCE`
- modificar `users` o cualquier fila
- migraciones / cambios de `schema.sql`
- deploy
- crear el primer ADMIN
- ingresar el pedido real de agosto 2026
