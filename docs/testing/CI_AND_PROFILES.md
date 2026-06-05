# CI and Maven Profiles

| Profile | Module | Effect |
|---------|--------|--------|
| *(default)* | server, client | Unit tests only |
| `integration` | server | `*IT` in `integration/` |
| `api` | server | `*IT` in `api/` |
| `ui` | client | AssertJ-Swing in `ui/` |
| `e2e` | test-automation | `CucumberE2eRunner` |
| `uat` | test-automation | `CucumberUatRunner` |
| `perf` | server | `perf/*Test` |
| `load` | test-automation | JMeter smoke plan |

## Environment variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `g4ims.env.file` | `.env.test` | Server DB config for tests |
| `e2e.server.url` | `http://localhost:18080/api` | Override API base URL |
| `G4IMS_TEST_DB_PORT` | `3307` | MySQL probe for `Assume` |

## Cucumber tags

```bash
-Dcucumber.filter.tags="@api and not @root-login"
```
