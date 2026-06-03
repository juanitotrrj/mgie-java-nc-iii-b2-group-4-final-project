# Deployment Guide

This guide covers deploying the **inventory-server** and **inventory-client** for production or lab use, primarily on **Windows** (project target platform). Steps also apply to macOS and Linux with path/command adjustments.

## Deployment Topology

```
┌─────────────────┐     HTTP (REST)      ┌──────────────────┐     JDBC      ┌─────────┐
│ inventory-client│ ───────────────────► │ inventory-server │ ────────────► │ MySQL 8 │
│  (Swing JAR)    │   Bearer JWT         │  (fat JAR)       │               │         │
└─────────────────┘                      └──────────────────┘               └─────────┘
```

- One server instance per site (binds `G4IMS_SERVER_SERVER_HOST` / `G4IMS_SERVER_SERVER_PORT`).
- Multiple desktop clients may connect to the same server URL.
- All business data lives in MySQL; the client has no local database.

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| JRE | 8+ | Runtime for deployed JARs (JDK 17+ only needed on build machines) |
| MySQL | 8.0+ | Dedicated instance or managed service |
| Network | TCP | Client → server HTTP; server → MySQL |

Build machines additionally need **JDK 17+** and **Maven 3.9+** (see [BUILD.md](BUILD.md)).

## Step 1: Prepare MySQL

1. Create a database (example local name from `.env.example`):

```sql
CREATE DATABASE g4ims_local
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. Create an application user with least privilege on that schema:

```sql
CREATE USER 'inventory_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON g4ims_local.* TO 'inventory_user'@'%';
FLUSH PRIVILEGES;
```

3. Note host, port, database name, username, and password for `.env`.

For Docker MySQL, map host port (e.g. `3307:3306`) and set `G4IMS_SERVER_DB_HOST=127.0.0.1` and matching port in `.env`.

## Step 2: Build Artifacts

On a build machine:

```bash
cd inventory-server
mvn clean package -DskipTests
# Output: target/inventory-server-1.0.0.jar

cd ../inventory-client
mvn clean package -DskipTests
# Output: target/inventory-client-1.0.0.jar
```

Copy both JARs to the deployment host. See [BUILD.md](BUILD.md).

## Step 3: Server Configuration (`.env`)

On the server host, create `inventory-server/.env` (never commit this file):

```bash
cp .env.example .env
```

**Required before production:**

| Variable | Action |
|----------|--------|
| `G4IMS_SERVER_DB_*` | Point to production MySQL |
| `G4IMS_SERVER_AUTH_TOKEN_SECRET` | Long random secret (32+ characters) |
| `G4IMS_SERVER_APP_ENV` | Set to `production` |
| `G4IMS_SERVER_DEV_SHOW_STACKTRACE` | Set to `false` |
| `G4IMS_SERVER_SERVER_HOST` | `0.0.0.0` for LAN access, or specific interface |
| `G4IMS_SERVER_FILE_STORAGE_ROOT` | Absolute path with write permissions |
| `G4IMS_SERVER_LOG_DIR` | Absolute path for logs |

Full reference: [server/CONFIGURATION.md](server/CONFIGURATION.md).

### Interactive first-time setup (recommended)

Instead of hand-editing every key:

```bash
cd inventory-server
java -jar inventory-server-1.0.0.jar setup
```

This wizard:

- Prompts for server, database, storage paths, and root credentials
- Writes `.env`
- Creates the schema if needed
- Runs migrations
- Stores root credentials in `system_root_credentials` for setup unlock

After CLI setup, complete **application setup** (business settings, roles, users) via the **client setup wizard** or setup API (see [USER_GUIDE.md](USER_GUIDE.md)).

Validate configuration:

```bash
java -jar inventory-server-1.0.0.jar config:validate
```

## Step 4: Database Migrations

Run before first serve (or after upgrades):

```bash
java -jar inventory-server-1.0.0.jar migrate
java -jar inventory-server-1.0.0.jar migrate:status
```

Optional: set `G4IMS_SERVER_DB_RUN_MIGRATIONS_ON_STARTUP=true` to migrate on every server start (not recommended for large production DBs without review).

## Step 5: Start the Server

```bash
java -jar inventory-server-1.0.0.jar serve
# Or with no args (default is serve):
java -jar inventory-server-1.0.0.jar
```

Health check:

```bash
curl http://localhost:8080/api/health
java -jar inventory-server-1.0.0.jar health
```

## Step 6: Deploy the Client

1. Install a JRE on each workstation.
2. Distribute `inventory-client-1.0.0.jar`.
3. Configure server URL (one of):
   - Edit `client.properties` inside the JAR or ship an external copy on the classpath
   - Launch with: `java -jar inventory-client-1.0.0.jar --server-url http://your-server:8080/api`

Default URL: `http://localhost:8080/api` (see `inventory-client/src/main/resources/client.properties`).

4. Ensure firewall allows outbound HTTP from client PCs to the server port.

## Step 7: Post-Deployment Verification

| Check | How |
|-------|-----|
| API up | `GET /api/health` returns success |
| Setup state | `GET /api/setup/status` → `INITIALIZED` when ready |
| Swagger | `http://<host>:8080/api/docs` (if `G4IMS_SERVER_SWAGGER_ENABLED=true`) |
| Login | Client guest screen → Login with seeded admin (after setup) |
| Audit | Admin → Audit Logs in client |

## Windows Production Notes

- Run the server as a **Windows Service** using `nssm`, `winsw`, or Task Scheduler with restart on failure.
- Set working directory to `inventory-server/` so `.env` and relative paths (`./storage`, `./logs`) resolve correctly.
- Install **MySQL 8** locally or use a network instance; install **Visual C++ redistributables** if using native MySQL tools for backup.
- Place `mysqldump` on PATH or set `G4IMS_SERVER_MYSQLDUMP_BIN` for backup from Settings API.

## Security Checklist

- [ ] Unique `G4IMS_SERVER_AUTH_TOKEN_SECRET` per environment
- [ ] Strong MySQL password; restricted DB user host (`@'app-subnet'` not `@'%'` if possible)
- [ ] HTTPS termination via reverse proxy (IIS/nginx) in production
- [ ] `.env` file ACL: administrators only
- [ ] Disable `setup:reset` in production workflows
- [ ] Rotate default admin password after first login
- [ ] Restrict `G4IMS_SERVER_CORS_ALLOWED_ORIGINS` to known origins

## Backup and Restore

- **Automated:** configure `G4IMS_SERVER_BACKUP_*` in `.env`
- **Manual:** Admin → Settings → Database → Backup in client, or `POST /api/settings/backup`
- Backup files: `G4IMS_SERVER_BACKUP_DIR` (default `./storage/backups`)

## Upgrading

1. Stop server
2. Backup database
3. Replace JAR
4. `java -jar inventory-server-1.0.0.jar migrate`
5. Start server
6. Replace client JAR on workstations if UI/API contract changed
