# Server CLI Reference

The server JAR is both the HTTP service and a command-line tool. Run from `inventory-server/` so `.env` is found.

```bash
java -jar target/inventory-server-1.0.0.jar <command> [args]
```

## Commands

| Command | Description |
|---------|-------------|
| *(no args)* | Same as `serve` — start HTTP server |
| `serve` | Start HTTP server until Ctrl+C |
| `setup` | Interactive first-time server setup (writes `.env`, DB, root creds) |
| `status` | Print config summary, DB connectivity, setup state |
| `health` | Exit **0** if DB OK, **1** if unhealthy (for scripts) |
| `config:validate` | Verify required `.env` keys; exit 1 on failure |
| `setup:resume` | Resume interactive setup wizard |
| `setup:unlock` | Prompt for root credentials to unlock setup |
| `setup:reset` | Reset installation state (**development only**) |
| `migrate` | Apply pending SQL migrations |
| `migrate:status` | Show applied/pending migrations |
| `migrate:rollback` | Roll back the last migration |
| `migrate:generate <name>` | Create new migration file template |

## Examples

```bash
# Start server
java -jar target/inventory-server-1.0.0.jar

# First install
java -jar target/inventory-server-1.0.0.jar setup
java -jar target/inventory-server-1.0.0.jar migrate
java -jar target/inventory-server-1.0.0.jar serve

# Ops checks
java -jar target/inventory-server-1.0.0.jar status
java -jar target/inventory-server-1.0.0.jar health
java -jar target/inventory-server-1.0.0.jar config:validate

# Schema change
java -jar target/inventory-server-1.0.0.jar migrate:generate add_widget_table
# Edit generated SQL, then:
java -jar target/inventory-server-1.0.0.jar migrate
```

## `setup` Command Flow

1. Prompts for server port, context path, storage directories  
2. Prompts for MySQL host, port, database, user, password  
3. Writes `.env`  
4. Creates database schema if needed  
5. Runs migrations  
6. Stores root username/password hash in `system_root_credentials`  

After CLI setup, run the **client setup wizard** for application-level configuration.

## `status` Output

Typical fields:

- `.env` file presence  
- App environment and base URL  
- Database host, name, user (password masked)  
- DB connection: CONNECTED / DISCONNECTED  
- Setup state from `system_installation`  

## `health` Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Database connection valid |
| 1 | Connection failed or invalid |

Use in cron or load balancer scripts:

```bash
java -jar inventory-server-1.0.0.jar health && echo OK || echo FAIL
```

## Migrations

- Files: `src/main/resources/db/migrations/V###__description.sql`  
- Index: `migrations.index` (order matters)  
- Tracking table: `schema_migrations`  

`migrate:generate` creates the next version file; edit SQL before running `migrate`.

## Shutdown

Running server: **Ctrl+C** triggers shutdown hook (stops `HttpServer`, closes Hikari pool).

## Related

- [CONFIGURATION.md](CONFIGURATION.md)
- [RUN.md](../RUN.md)
