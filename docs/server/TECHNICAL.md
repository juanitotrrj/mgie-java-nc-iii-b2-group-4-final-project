# Server — Technical Documentation

Native **Java SE 8** REST API for the G4 Inventory Management System. No Spring, servlet container, or application server.

## Stack

| Layer | Technology |
|-------|------------|
| HTTP | `com.sun.net.httpserver.HttpServer` |
| JSON | Gson |
| JDBC | MySQL Connector/J + HikariCP |
| Auth | JWT (JJWT) + BCrypt passwords |
| Logging | SLF4J + Logback |
| Migrations | Custom SQL runner (`db/migrations/`) |
| API contract | OpenAPI 3 (`src/main/resources/docs/openapi.yaml`) |

Bytecode: **Java 8**. Build JDK: **17**.

## Package Structure

```
com.group4.inventoryserver/
├── Main.java                 # Entry, CLI, route registration
├── config/                   # EnvConfig, DotEnvLoader, AppConfig, DatabaseConfig
├── server/                   # HttpServerBootstrap, Router, RequestContext, JsonResponse
├── middleware/               # LoggingFilter, CorsFilter, SetupGuardFilter, AuthFilter
├── handler/                  # HTTP handlers per resource
├── service/                  # Business logic
├── repository/               # JDBC data access
├── dto/                      # Request/response POJOs
├── migration/                # MigrationRunner, MigrationGenerator
└── setup/                    # SetupCommand (CLI), setup services
```

## Request Pipeline

Order of processing for `/api/*` requests:

1. **HttpServer** accepts connection (thread pool: `processors × 2`)
2. **Router** matches longest registered path prefix
3. **Filters** (in order):
   - `LoggingFilter`
   - `CorsFilter` (if enabled)
   - `SetupGuardFilter` — blocks non-setup traffic until `INITIALIZED`
   - `AuthFilter` — JWT for protected routes
4. **Handler** (`BaseHandler` subclass) — method routing, permission checks
5. **Service** → **Repository** → MySQL
6. **JsonResponse** — standard envelope `{ success, data, message, meta }`

Detailed diagrams: [inventory-server/docs/REQUEST_LIFECYCLE.md](../../inventory-server/docs/REQUEST_LIFECYCLE.md).

## Configuration

- Loaded from `inventory-server/.env` via `DotEnvLoader` → `EnvConfig`
- Keys prefixed `G4IMS_SERVER_*`
- OS environment variables override as fallback
- Application settings (company name, tax, etc.) stored in **database** after setup

See [CONFIGURATION.md](CONFIGURATION.md).

## Route Registration

Registered in `Main.startServer()` under `G4IMS_SERVER_APP_CONTEXT_PATH` (default `/api`):

| Prefix | Handler |
|--------|---------|
| `/setup` | SetupHandler |
| `/health` | HealthHandler |
| `/public/welcome`, `/about`, `/contact` | Guest content |
| `/public/inquiries` | InquiryHandler |
| `/docs` | SwaggerHandler (if enabled) |
| `/auth/login`, `/logout`, `/me` | Authentication |
| `/dashboard` | Role dashboards |
| `/products`, `/categories`, `/suppliers` | Master data |
| `/purchases`, `/sales` | Transactions |
| `/users` | User management |
| `/inventory-change-requests` | ICR workflow |
| `/stock-movements` | Stock audit trail |
| `/reports` | Reports and export |
| `/settings` | System settings, backup |
| `/audit-logs` | Audit log query |

## Security Model

### Public routes (no Bearer token)

- `/api/health`
- `/api/auth/login`
- `/api/public/*`
- `/api/setup/*` (setup token for write steps)
- `/api/docs` (when enabled)

### Authenticated routes

`Authorization: Bearer <token>` — validated in `AuthFilter`; session stored in DB (hashed token); permissions attached to request context.

### Permission checks

Handlers call `requirePermission(ctx, "PERMISSION_CODE")` (e.g. `PRODUCT_READ`, `SALE_WRITE`). Codes seeded in migrations and setup.

### Setup guard

Until `system_installation.setup_state = INITIALIZED`:

- Allowed: `/api/health`, `/api/setup`, `/api/docs`
- Other paths: **503** “System setup not complete”

After initialized, setup **write** endpoints return **403**.

## Database

- **Engine:** MySQL 8, InnoDB, utf8mb4
- **Pool:** HikariCP (`DatabaseConfig`)
- **Migrations:** versioned SQL in `src/main/resources/db/migrations/`, index in `migrations.index`
- **Setup tables:** `system_installation`, `system_root_credentials`, `setup_sessions`

CLI: `migrate`, `migrate:status`, `migrate:rollback`, `migrate:generate`.

## Two-Stage Setup

| Stage | Mechanism |
|-------|-----------|
| Server infrastructure | CLI `setup` → `.env`, schema, migrations, root creds |
| Application bootstrap | REST `/api/setup/*` + client wizard → settings, roles, users, `finish` |

## File Storage

Under `G4IMS_SERVER_FILE_STORAGE_ROOT` (default `./storage`):

- `uploads/` — including ICR proof images
- `exports/` — generated export files
- `backups/` — database dumps

## Error Handling

- Typed exceptions mapped to HTTP status in handlers
- `ErrorResponse` DTO for failures
- Stack traces in responses only when `G4IMS_SERVER_DEV_SHOW_STACKTRACE=true`

## Extension Points

1. Add migration SQL + `migrations.index` entry  
2. Create repository + service + handler  
3. Register route in `Main.startServer()`  
4. Add OpenAPI paths in `openapi.yaml`  
5. Add permission codes and role mappings (migration or setup seed)  

## Related

- [CLI.md](CLI.md)
- [API.md](API.md)
- [CONFIGURATION.md](CONFIGURATION.md)
