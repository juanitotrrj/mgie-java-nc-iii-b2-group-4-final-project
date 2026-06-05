# Testing Guide (Hub)

Automated test pyramid for G4IMS (`inventory-server`, `inventory-client`, `test-automation`).

**Root-login** (`POST /api/setup/root-login`) is **out of scope** — scenarios tagged `@root-login @deferred` are excluded from CI. See [PRD_TRACEABILITY.md](testing/PRD_TRACEABILITY.md).

## Pyramid

| Layer | Module | Command | Docker? | ~Duration |
|-------|--------|---------|---------|-----------|
| Unit | server + client | `mvn test -pl inventory-server,inventory-client` | No | 1–2 min |
| Integration | server | `mvn verify -Pintegration -pl inventory-server` | Yes | 2–5 min |
| API | server | `mvn verify -Papi -pl inventory-server` | Yes | 3–8 min |
| UI (Swing) | client | `mvn verify -Pui -pl inventory-client` | Server up | 2–5 min |
| BDD / E2E | test-automation | `mvn test -Pe2e -pl test-automation` | Yes | 5–15 min |
| UAT | test-automation | `mvn test -Puat -pl test-automation` | Yes | 5–20 min |
| Performance | server | `mvn test -Pperf -pl inventory-server` | No | 1–3 min |
| Load | test-automation | `mvn verify -Pload -pl test-automation` | Yes + server | nightly |
| Coverage | server | `mvn test -Pcoverage -pl inventory-server` | No | 1–2 min |
| Coverage gate | server | `mvn verify -Pcoverage-check -pl inventory-server` | No | 1–2 min |

## Quick start (Linux)

```bash
docker compose -f docker-compose.test.yml up -d
cp inventory-server/.env.test.example inventory-server/.env.test
./scripts/test/reset-db.sh initialized
./scripts/test/run-linux.sh unit-only   # fast
./scripts/test/run-linux.sh full          # integration + api + e2e + perf (needs MySQL)
```

Windows: `.\scripts\test\run-windows.ps1 -Profile full`

## JaCoCo coverage

JaCoCo instruments unit tests during `mvn test` and writes HTML/XML reports under:

```
inventory-server/target/site/jacoco/
inventory-server/target/site/jacoco/jacoco.xml
```

### Commands

| Goal | Command |
|------|---------|
| Run tests with report | `mvn test -pl inventory-server` |
| Report only (after test) | `mvn verify -Pcoverage -pl inventory-server` |
| Enforce thresholds | `mvn verify -Pcoverage-check -pl inventory-server` |

### Thresholds (`-Pcoverage-check`)

| Metric | Minimum |
|--------|---------|
| Line coverage | 80% |
| Branch coverage | 70% |

Open `inventory-server/target/site/jacoco/index.html` in a browser for package- and class-level drill-down.

## Coverage profiles

| Profile | Module | Effect |
|---------|--------|--------|
| *(default)* | server | JaCoCo agent + report on `test` phase |
| `coverage` | server | Explicit report at `verify` |
| `coverage-check` | server | Fails build if line &lt; 80% or branch &lt; 70% |

Combine with other profiles as needed, e.g. unit tests then gate:

```bash
mvn test verify -Pcoverage-check -pl inventory-server
```

## Test class inventory

### Unit tests — `inventory-server` (default `mvn test`)

| Package / area | Test classes |
|----------------|--------------|
| `config` | `AppConfigTest`, `DotEnvLoaderTest`, `EnvConfigTest` |
| `dto` | `ApiResponseTest`, `ErrorResponseTest`, `HealthResponseTest`, `PaginatedResponseTest`, `PaginationMetaTest`, `PaginationParamsTest`, `auth/LoginRequestTest`, `category/*`, `guest/*`, `product/*`, `purchase/*`, `sale/*`, `setup/*`, `user/*` |
| `handler` | `AboutHandlerTest`, `AuditLogHandlerTest`, `BaseHandlerTest`, `CategoryHandlerTest`, `ContactHandlerTest`, `CurrentUserHandlerTest`, `DashboardHandlerTest`, `HealthHandlerTest`, `IcrHandlerTest`, `InquiryHandlerTest`, `LoginHandlerTest`, `LogoutHandlerTest`, `ProductHandlerTest`, `PurchaseHandlerTest`, `ReportHandlerTest`, `SaleHandlerTest`, `SettingsHandlerTest`, `SetupHandlerTest`, `StockMovementHandlerTest`, `SupplierHandlerTest`, `SwaggerHandlerTest`, `UserHandlerTest`, `WelcomeHandlerTest` |
| `middleware` | `AuthFilterTest`, `CorsFilterTest`, `LoggingFilterTest`, `SetupGuardFilterTest` |
| `migration` | `MigrationRunnerTest`, `MigrationTest` |
| `security` | `JwtUtilTest`, `PasswordUtilTest` |
| `server` | `JsonResponseTest`, `RequestContextTest`, `RouterTest` |
| `service` | `AuditServiceTest`, `AuthServiceTest`, `CategoryServiceTest`, `IcrServiceTest`, `ProductServiceTest`, `PurchaseServiceTest`, `SaleServiceTest`, `SetupServiceTest`, `SupplierServiceTest`, `UserServiceTest` |
| `setup` | `SetupCommandTest` |
| `util` | `DatabaseBackupUtilTest`, `DateUtilTest`, `ExportUtilTest`, `FileUploadTest`, `HashUtilTest`, `JsonUtilTest`, `MultipartParserTest`, `ValidationUtilTest` |
| root | `ServerLauncherTest` |

### Integration / API / repository — `inventory-server` (Failsafe)

| Layer | Location | Examples |
|-------|----------|----------|
| Integration | `integration/*IT.java` | `MigrationRunnerIT`, `ServerLauncherIT`, `TestSetupBootstrapIT` |
| API | `api/*IT.java` | `AuthApiIT`, `ProductsApiIT`, `SalesApiIT`, `ReportsApiIT`, … |
| Repository | `repository/*IT.java` | `ProductRepositoryIT`, `SaleRepositoryIT`, … |

### Performance — `inventory-server` (`-Pperf`)

| Class | Benchmark focus | SLA (JUnit) |
|-------|-----------------|-------------|
| `PaginationParamsPerfTest` | Query param parsing | 100k &lt; 2000 ms |
| `JwtUtilPerfTest` | Token create / parse | 10k round-trip &lt; 5000 ms |
| `PasswordUtilPerfTest` | BCrypt hash / verify | verify 100 &lt; 15 s; hash 10 &lt; 5 s |
| `JsonUtilPerfTest` | Gson serialize / deserialize | 10k &lt; 3000 ms |
| `ValidationUtilPerfTest` | Field validation rules | 10k &lt; 1500 ms |
| `ExportUtilPerfTest` | CSV export | 1000 rows &lt; 500 ms |

All perf classes include JMH `@Benchmark` methods (`@Warmup`, `@Measurement`, `@Fork(1)`) for micro-benchmark runs outside Surefire.

### Load — `test-automation/jmeter/` (`-Pload`)

| Plan | Workload |
|------|----------|
| `g4ims-smoke.jmx` | Health check |
| `g4ims-auth.jmx` | Login / logout loop |
| `g4ims-crud.jmx` | Product list + create |
| `g4ims-pos.jmx` | Sale creation |
| `g4ims-reports.jmx` | Report options + sales summary + low stock |
| `g4ims-full.jmx` | Mixed health, auth, products, sales, reports |

Server target: `${__P(server.host,localhost)}:${__P(server.port,18080)}`.

### BDD — `test-automation` (`-Pe2e`, `-Puat`)

| Feature file | Runner tags |
|--------------|-------------|
| `auth/login.feature` | `@api @e2e @uat` |
| `guest/guest_inquiry.feature` | `@api @uat` |
| `setup/server_setup_api.feature` | `@api @infra_ready` |
| `setup/root_login.feature` | `@root-login @deferred` |
| `journeys/purchase_receive_stock.feature` | `@uat @e2e` |

## Run everything

See [testing/RUN_FULL_PYRAMID.md](testing/RUN_FULL_PYRAMID.md).

## Deep dives

- [PREREQUISITES.md](testing/PREREQUISITES.md)
- [RUN_UNIT_TESTS.md](testing/RUN_UNIT_TESTS.md)
- [RUN_INTEGRATION_TESTS.md](testing/RUN_INTEGRATION_TESTS.md)
- [RUN_API_TESTS.md](testing/RUN_API_TESTS.md)
- [RUN_UI_TESTS.md](testing/RUN_UI_TESTS.md)
- [RUN_BDD_E2E_UAT.md](testing/RUN_BDD_E2E_UAT.md)
- [RUN_PERFORMANCE_TESTS.md](testing/RUN_PERFORMANCE_TESTS.md)
- [RUN_LOAD_TESTS.md](testing/RUN_LOAD_TESTS.md)
- [CI_AND_PROFILES.md](testing/CI_AND_PROFILES.md)
- [TROUBLESHOOTING.md](testing/TROUBLESHOOTING.md)
- [PRD_TRACEABILITY.md](testing/PRD_TRACEABILITY.md)
