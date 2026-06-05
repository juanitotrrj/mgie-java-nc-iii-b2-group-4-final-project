# Run Integration Tests

## Purpose

Real MySQL + `ServerLauncher`, migrations, `TestSetupBootstrap`.

## Prerequisites

Docker MySQL on 3307, `inventory-server/.env.test`.

## Commands

```bash
docker compose -f docker-compose.test.yml up -d
cd inventory-server
mvn verify -Pintegration -Dg4ims.env.file=.env.test
```

Tests skip automatically if port 3307 is unreachable (`Assume.assumeTrue`).

## Classes

- `MigrationRunnerIT`
- `TestSetupBootstrapIT`
- `ServerLauncherIT`
