# Run Guide

How to start, stop, and verify the inventory **server** and **client**, including first-time two-stage setup.

## Startup Order (First Install)

```
MySQL running
    → Server .env configured (setup CLI or manual)
    → Migrations applied
    → Server JAR running
    → Client JAR (setup wizard OR guest/login)
    → Application setup wizard (if not INITIALIZED)
    → Daily use: guest → login → main app
```

## Run the Server

### Working directory

Run from `inventory-server/` so `.env`, `./storage`, and `./logs` resolve correctly:

```bash
cd inventory-server
java -jar target/inventory-server-1.0.0.jar
```

Equivalent:

```bash
java -jar target/inventory-server-1.0.0.jar serve
```

### First-time server CLI setup

```bash
java -jar target/inventory-server-1.0.0.jar setup
```

Then migrate (if setup did not run migrations):

```bash
java -jar target/inventory-server-1.0.0.jar migrate
```

### Default URLs

| Endpoint | URL |
|----------|-----|
| Health | `http://localhost:8080/api/health` |
| API base | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/api/docs` |

Port and context path come from `.env` (`G4IMS_SERVER_SERVER_PORT`, `G4IMS_SERVER_APP_CONTEXT_PATH`).

### Stop the server

Press **Ctrl+C** in the terminal. Shutdown hook closes the connection pool.

### CLI utilities (server not listening)

| Command | Purpose |
|---------|---------|
| `status` | `.env`, DB connectivity, setup state |
| `health` | Exit 0 if DB reachable |
| `config:validate` | Required `.env` keys present |
| `migrate` | Apply pending SQL migrations |
| `migrate:status` | List migration history |
| `migrate:rollback` | Roll back last migration |
| `setup:resume` | Resume interactive setup |
| `setup:unlock` | Unlock setup (root credentials) |
| `setup:reset` | **Dev only** — reset installation state |

Full list: [server/CLI.md](server/CLI.md).

### Verify server

```bash
curl -s http://localhost:8080/api/health
java -jar target/inventory-server-1.0.0.jar health
```

## Run the Client

```bash
cd inventory-client
java -jar target/inventory-client-1.0.0.jar
```

### Custom server URL

```bash
java -jar target/inventory-client-1.0.0.jar --server-url http://192.168.1.10:8080/api
```

### What opens on launch

1. Client calls `GET /api/setup/status`
2. If `setupState` is **not** `INITIALIZED` → **Setup Wizard** (`WizardFrame`)
3. If `INITIALIZED` → **Guest shell** (`GuestFrame`): Welcome, About, Contact, Test Connection, Login

After login → **Main application** (`MainFrame`) with role-based sidebar.

### Stop the client

Close the window or Ctrl+C from the launching terminal.

## Two-Stage Setup (Server + Client)

| Stage | Where | What |
|-------|-------|------|
| **1 — Server** | CLI `setup` | `.env`, DB schema, migrations, root credentials |
| **2 — Application** | Client wizard or `/api/setup/*` | Business settings, roles, permissions, users, finish |

During stage 2, only `/api/health`, `/api/setup`, and `/api/docs` are available for business APIs; other routes return **503** until `INITIALIZED`.

Client wizard steps:

1. Status  
2. Root authentication (`X-Setup-Token`)  
3. Business settings  
4. Role seed  
5. Permission seed  
6. Create users  
7. Review and finish  

After finish, restart client or proceed to guest/login.

## Daily Operation

1. Ensure MySQL and server are running
2. Launch client JAR on each PC
3. **Test Connection** (optional) on guest screen
4. **Login** with username/password
5. Use sidebar modules (visibility depends on role permissions)
6. **Logout** returns to guest shell

Default bootstrap admin (from migration seed, if not changed): username `admin` — change password after first login.

## Troubleshooting

| Problem | Likely cause | Action |
|---------|--------------|--------|
| Client cannot connect | Server down or wrong URL | Check `--server-url`, firewall, `curl /api/health` |
| 503 on API calls | Setup not finished | Complete setup wizard; check `GET /api/setup/status` |
| 401 on API calls | Missing/expired token | Log in again |
| DB connection failed | Wrong `.env` or MySQL down | `java -jar ... status`, test MySQL CLI |
| Port in use | Another process on 8080 | Change `G4IMS_SERVER_SERVER_PORT` or stop other service |
| Migrations fail | Schema drift | `migrate:status`, fix SQL, restore backup if needed |
| Swagger 404 | Disabled in `.env` | Set `G4IMS_SERVER_SWAGGER_ENABLED=true` |

## Logs

| Component | Location (default) |
|-----------|-------------------|
| Server | `G4IMS_SERVER_LOG_FILE` (e.g. `./logs/inventory-api.log`) |
| Client | Console only (no file log) |

## Related

- [USER_GUIDE.md](USER_GUIDE.md) — screen-by-screen usage
- [DEPLOYMENT.md](DEPLOYMENT.md) — production deployment
