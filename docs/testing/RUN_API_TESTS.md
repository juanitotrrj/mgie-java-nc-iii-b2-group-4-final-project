# Run API Tests

## Purpose

HTTP contract tests via `E2eHttpClient` and ephemeral `ServerLauncher` (port 18080).

## Commands

```bash
docker compose -f docker-compose.test.yml up -d
./scripts/test/reset-db.sh initialized
cd inventory-server
mvn verify -Papi -Dg4ims.env.file=.env.test
```

## Coverage

- `AuthApiIT`, `GuestApiIT`, `ProductsApiIT`, `SetupApiIT` (minted `X-Setup-Token`, no root-login HTTP)
