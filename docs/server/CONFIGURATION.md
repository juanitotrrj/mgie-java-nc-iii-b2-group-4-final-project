# Server Configuration

All server runtime configuration uses a **`.env` file** in the `inventory-server/` directory.

## Loading Order

1. `DotEnvLoader.load()` reads `.env` from the working directory
2. `EnvConfig.get(key)` resolves `G4IMS_SERVER_<KEY>`
3. If missing in `.env`, falls back to OS environment variable of the same name

**Do not commit `.env`.** Use `.env.example` as the template.

## Quick Start

```bash
cp .env.example .env
# Edit required values, or:
java -jar target/inventory-server-1.0.0.jar setup
java -jar target/inventory-server-1.0.0.jar config:validate
```

## Required Keys (minimum)

| Variable | Description |
|----------|-------------|
| `G4IMS_SERVER_DB_HOST` | MySQL host |
| `G4IMS_SERVER_DB_PORT` | MySQL port |
| `G4IMS_SERVER_DB_NAME` | Schema name (e.g. `g4ims_local`) |
| `G4IMS_SERVER_DB_USER` | DB user |
| `G4IMS_SERVER_DB_PASSWORD` | DB password |
| `G4IMS_SERVER_AUTH_TOKEN_SECRET` | JWT signing secret (32+ chars) |

Validated by `config:validate` CLI command.

## Sections (from `.env.example`)

### Application / Runtime

| Variable | Default | Purpose |
|----------|---------|---------|
| `G4IMS_SERVER_APP_NAME` | Inventory Management System... | Display name |
| `G4IMS_SERVER_APP_ENV` | development | `development` / `production` |
| `G4IMS_SERVER_APP_BASE_URL` | http://localhost:8080 | Public base URL |
| `G4IMS_SERVER_APP_CONTEXT_PATH` | /api | API mount path |
| `G4IMS_SERVER_SERVER_HOST` | 0.0.0.0 | Bind address |
| `G4IMS_SERVER_SERVER_PORT` | 8080 | HTTP port |
| `G4IMS_SERVER_SERVER_BACKLOG` | 100 | Socket backlog |
| `G4IMS_SERVER_SERVER_MAX_REQUEST_BODY_BYTES` | 10485760 | Max JSON body |
| `G4IMS_SERVER_SERVER_MAX_UPLOAD_BYTES` | 5242880 | Max upload |
| `G4IMS_SERVER_SERVER_REQUEST_TIMEOUT_SECONDS` | 30 | Request timeout |

### Database

| Variable | Default | Purpose |
|----------|---------|---------|
| `G4IMS_SERVER_DB_DRIVER` | com.mysql.cj.jdbc.Driver | JDBC driver |
| `G4IMS_SERVER_DB_*` | see example | Connection |
| `G4IMS_SERVER_DB_POOL_*` | Hikari settings | Pool sizing |
| `G4IMS_SERVER_DB_RUN_MIGRATIONS_ON_STARTUP` | false | Auto-migrate on serve |
| `G4IMS_SERVER_DB_RUN_SEED_ON_STARTUP` | false | Auto-seed on serve |

`G4IMS_SERVER_DB_URL` is optional; built from host/port/name if omitted.

### Authentication / Security

| Variable | Purpose |
|----------|---------|
| `G4IMS_SERVER_AUTH_TOKEN_SECRET` | JWT HMAC secret |
| `G4IMS_SERVER_AUTH_TOKEN_EXPIRY_SECONDS` | Access token TTL (default 1800) |
| `G4IMS_SERVER_PASSWORD_BCRYPT_COST` | BCrypt cost factor |
| `G4IMS_SERVER_PASSWORD_MIN_LENGTH` | Password policy |
| `G4IMS_SERVER_SECURITY_LOCK_AFTER_FAILED_ATTEMPTS` | Account lockout |
| `G4IMS_SERVER_SECURITY_SESSION_TIMEOUT_MINUTES` | Session policy |

### Business Rules

Inventory, sales, purchase, currency, and tax defaults (e.g. `G4IMS_SERVER_INVENTORY_LOW_STOCK_THRESHOLD`, `G4IMS_SERVER_DEFAULT_TAX_RATE`).

Overrides in **database settings** after setup may take precedence for application-facing values.

### File Storage

| Variable | Default |
|----------|---------|
| `G4IMS_SERVER_FILE_STORAGE_ROOT` | ./storage |
| `G4IMS_SERVER_UPLOAD_DIR` | ./storage/uploads |
| `G4IMS_SERVER_EXPORT_DIR` | ./storage/exports |
| `G4IMS_SERVER_BACKUP_DIR` | ./storage/backups |

### Reports / Export

`G4IMS_SERVER_EXPORT_ALLOWED_FORMATS`, `G4IMS_SERVER_REPORT_DEFAULT_PAGE_SIZE`, etc.

### Email / Backup / CORS / Logging

See `.env.example` for SMTP, backup schedule, CORS origins, log rotation.

### Swagger

| Variable | Purpose |
|----------|---------|
| `G4IMS_SERVER_SWAGGER_ENABLED` | Enable `/api/docs` |
| `G4IMS_SERVER_SWAGGER_OPENAPI_FILE` | Path to OpenAPI YAML (optional override) |

### Development

| Variable | Purpose |
|----------|---------|
| `G4IMS_SERVER_DEV_SHOW_STACKTRACE` | Include stack traces in API errors |
| `G4IMS_SERVER_DEV_ENABLE_DEMO_DATA` | Demo data flag |

## Application Settings (Database)

Post-setup values (company profile, notification toggles, etc.) are stored in DB tables managed via:

- `GET/PUT /api/settings?section=<name>`
- Client **Settings** panel

These are **not** in `.env`; `.env` is for server/infrastructure only.

## Production Recommendations

```properties
G4IMS_SERVER_APP_ENV=production
G4IMS_SERVER_DEV_SHOW_STACKTRACE=false
G4IMS_SERVER_AUTH_TOKEN_SECRET=<long-random-value>
G4IMS_SERVER_DB_RUN_MIGRATIONS_ON_STARTUP=false
```

Use absolute paths for `FILE_STORAGE_ROOT`, `LOG_DIR`, and `BACKUP_DIR`.

## Related

- [CLI.md](CLI.md) — `config:validate`, `setup`
- [TECHNICAL.md](TECHNICAL.md) — architecture
