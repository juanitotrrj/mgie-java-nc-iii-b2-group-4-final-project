# Test Troubleshooting

## MySQL connection refused

- Ensure `docker compose -f docker-compose.test.yml up -d`
- Use port **3307**, not 3306
- Check `inventory-server/.env.test` matches compose credentials

## Setup 503 / not INITIALIZED

Run `./scripts/test/reset-db.sh initialized` or let tests call `TestSetupBootstrap.markInitialized()`.

## Integration tests skipped

`Assume.assumeTrue` — MySQL not reachable on 3307. Start Docker.

## Swing / headless failures (Linux)

```bash
xvfb-run mvn verify -Pui -pl inventory-client
```

## Permission nav missing in UI

Verify session permissions match V004 codes (`USER_MANAGE`, `STOCK_MOVEMENT_READ`, etc.).

## Root-login scenarios

Tagged `@deferred` — implement in separate plan before enabling.
