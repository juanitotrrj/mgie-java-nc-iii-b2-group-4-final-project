# PRD Traceability

Maps product requirements to automated tests. **Root-login** HTTP is deferred until `POST /api/setup/root-login` exists.

## BDD feature files

| PRD asset | Feature file | Tags | Layer | Status |
|-----------|--------------|------|-------|--------|
| OpenAPI auth | `auth/login.feature` | `@api @e2e @uat` | API + BDD | Active |
| OpenAPI guest | `guest/guest_inquiry.feature` | `@api @uat` | API + BDD | Active |
| Setup PRD (post-token) | `setup/server_setup_api.feature` | `@api @infra_ready` | API + BDD | Active |
| Setup PRD (root auth) | `setup/root_login.feature` | `@root-login @deferred` | — | **Deferred** |
| Use case: receive PO | `journeys/purchase_receive_stock.feature` | `@uat @e2e` | BDD | Partial |
| Two-stage setup impl | CLI + minted token | — | Integration | Active |

## API integration tests (`inventory-server`)

| PRD / OpenAPI module | IT class | Key endpoints |
|----------------------|----------|---------------|
| Authentication | `AuthApiIT` | `/api/auth/login`, `/api/auth/logout`, `/api/auth/me` |
| Guest inquiry | `GuestApiIT` | `/api/guest/inquiry` |
| Setup | `SetupApiIT` | `/api/setup/*` |
| Products | `ProductsApiIT` | `/api/products`, `/api/products/export` |
| Categories | `CategoriesApiIT` | `/api/categories` |
| Suppliers | `SuppliersApiIT` | `/api/suppliers` |
| Purchases | `PurchasesApiIT` | `/api/purchases` |
| Sales (POS) | `SalesApiIT` | `/api/sales`, `/api/sales/{id}/receipt` |
| ICR | `IcrApiIT` | `/api/inventory-change-requests` |
| Stock movements | `StockMovementsApiIT` | `/api/stock-movements` |
| Reports | `ReportsApiIT` | `/api/reports/options`, `/api/reports/sales-summary` |
| Dashboard | `DashboardApiIT` | `/api/dashboard/*` |
| Users | `UsersApiIT` | `/api/users` |
| Settings | `SettingsApiIT` | `/api/settings` |
| Audit logs | `AuditLogsApiIT` | `/api/audit-logs` |
| Permissions | `PermissionMatrixIT` | Role-based access matrix |
| Swagger | `SwaggerApiIT` | `/api/docs`, OpenAPI spec |

## Performance tests (`-Pperf`)

| PRD concern | Perf class | Requirement covered |
|-------------|------------|---------------------|
| Auth token lifecycle | `JwtUtilPerfTest` | JWT issue/validate throughput |
| Password security | `PasswordUtilPerfTest` | BCrypt hash/verify SLA |
| JSON API payloads | `JsonUtilPerfTest` | Request/response serialization |
| Input validation | `ValidationUtilPerfTest` | DTO field rules |
| Export / reporting data | `ExportUtilPerfTest` | CSV generation at scale |
| List pagination | `PaginationParamsPerfTest` | Query param parsing |

## Load tests (`-Pload`)

| PRD user journey | JMeter plan | Scenarios |
|------------------|-------------|-----------|
| System availability | `g4ims-smoke.jmx` | Health probe |
| Login / logout | `g4ims-auth.jmx` | Authenticated session cycle |
| Inventory CRUD | `g4ims-crud.jmx` | List and create products |
| Point of sale | `g4ims-pos.jmx` | Create sale with stock deduction |
| Management reports | `g4ims-reports.jmx` | Options, sales summary, low stock |
| Daily operations mix | `g4ims-full.jmx` | Health + auth + catalog + sales + reports |

## Coverage gate

| Quality gate | Profile | Threshold |
|--------------|---------|-----------|
| Line coverage | `-Pcoverage-check` | ≥ 80% |
| Branch coverage | `-Pcoverage-check` | ≥ 70% |

Root-login HTTP tests will be added when `POST /api/setup/root-login` is implemented (separate plan).
