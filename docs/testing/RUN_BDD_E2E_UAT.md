# Run BDD / E2E / UAT (Cucumber)

## Module

`test-automation/` — features under `src/test/resources/features/`.

## Tag filter (default)

```text
not @root-login and not @deferred
```

## E2E

```bash
mvn test -Pe2e -pl test-automation -Dg4ims.env.file=../inventory-server/.env.test
```

## UAT

```bash
mvn test -Puat -pl test-automation -Dg4ims.env.file=../inventory-server/.env.test
```

## Bootstrap

Scenarios use `TestSetupBootstrap.markInitialized()` or `@infra_ready` + minted setup token — **not** `POST /api/setup/root-login`.

Deferred feature: `features/setup/root_login.feature`
