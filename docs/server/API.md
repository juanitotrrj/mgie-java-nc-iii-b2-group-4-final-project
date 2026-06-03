# Server API Reference

REST API served at `{baseUrl}{contextPath}` — default **`http://localhost:8080/api`**.

## Contract Source

- **OpenAPI 3:** `inventory-server/src/main/resources/docs/openapi.yaml`
- **Interactive UI:** `GET /api/docs` (Swagger UI, when enabled)

## Response Envelope

Successful JSON responses generally follow:

```json
{
  "success": true,
  "data": { },
  "message": "optional",
  "meta": { "page": 1, "pageSize": 10, "totalPages": 5, "totalItems": 42 }
}
```

Errors:

```json
{
  "success": false,
  "message": "Human-readable error",
  "error": "ERROR_CODE"
}
```

## Authentication

### Login

```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "..." }
```

Response `data` includes `token`, `expiresInSeconds`, and `user` (profile + `permissions[]`).

### Authenticated requests

```http
Authorization: Bearer <token>
```

### Logout

```http
POST /api/auth/logout
Authorization: Bearer <token>
```

### Current user

```http
GET /api/auth/me
Authorization: Bearer <token>
```

## Setup API (pre-INITIALIZED)

Base: `/api/setup`

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| GET | `/status` | None | Setup state |
| GET | `/progress` | Setup token | Completed steps |
| PUT | `/business-settings` | `X-Setup-Token` | Company settings |
| POST | `/roles/seed` | Setup token | Seed roles |
| POST | `/permissions/seed` | Setup token | Role-permission map |
| POST | `/users` | Setup token | Create users |
| POST | `/finish` | Setup token | Mark INITIALIZED |

Root login endpoint (`POST /api/setup/root-login`) may be implemented separately by your team.

## Guest / Public

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/health` | Liveness + DB check |
| GET | `/public/welcome` | Welcome content |
| GET | `/public/about` | About content |
| GET | `/public/contact` | Contact info |
| POST | `/public/inquiries` | Submit inquiry |

## Core Resources

| Resource | Base path | Notes |
|----------|-----------|-------|
| Dashboard | `/dashboard/admin`, `/manager`, `/clerk`, `/cashier` | Role-specific |
| Products | `/products` | CRUD, pagination, `/products/export` |
| Categories | `/categories` | CRUD, export |
| Suppliers | `/suppliers` | CRUD, export |
| Purchases | `/purchases` | Create, receive, cancel, export |
| Sales | `/sales` | POS create, cancel, export |
| Users | `/users` | Admin, deactivate, reset-password, export |
| ICR | `/inventory-change-requests` | Submit, approve, reject |
| Stock | `/stock-movements` | Read-only list |
| Reports | `/reports` | Generate; `/reports/export` |
| Settings | `/settings` | Section GET/PUT, test-connection, backup |
| Audit | `/audit-logs` | Filtered pagination |

Exact query parameters and bodies are defined in **openapi.yaml**.

## Pagination

List endpoints typically accept:

- `page` (1-based)
- `pageSize` or `limit`
- `search`, `status`, role-specific filters

`meta` in the response carries pagination totals.

## Permissions (common codes)

| Code | Area |
|------|------|
| `PRODUCT_READ` / `PRODUCT_WRITE` / `PRODUCT_DELETE` | Products |
| `CATEGORY_*` | Categories |
| `SUPPLIER_*` | Suppliers |
| `PURCHASE_READ` / `PURCHASE_WRITE` / `PURCHASE_RECEIVE` | Purchases |
| `SALE_READ` / `SALE_WRITE` / `SALE_CANCEL` | Sales |
| `INVENTORY_CHANGE_REQUEST_CREATE` / `_REVIEW` | ICR |
| `STOCK_MOVEMENT_READ` | Stock movements |
| `REPORT_READ` | Reports |
| `EXPORT_DATA` | Exports |
| `USER_MANAGE` | Users |
| `SETTINGS_MANAGE` | Settings |
| `AUDIT_LOG_READ` | Audit logs |
| `BACKUP_MANAGE` | Backup/restore |

Missing permission → **403**.

## HTTP Status Codes

| Code | Typical cause |
|------|----------------|
| 200 / 201 | Success |
| 400 | Validation error |
| 401 | Missing/invalid token |
| 403 | Forbidden / setup locked |
| 404 | Resource not found |
| 503 | Setup not complete |
| 500 | Server error |

## CORS

When `G4IMS_SERVER_CORS_ENABLED=true`, browser clients may call from origins listed in `G4IMS_SERVER_CORS_ALLOWED_ORIGINS`. The Swing client does not use CORS (not a browser).

## Export Endpoints

Examples (see OpenAPI for parameters):

- `GET /api/products/export?format=csv`
- `GET /api/reports/export?type=...&format=csv&dateFrom=...&dateTo=...`

Requires `EXPORT_DATA` or module read permission per handler rules.

## Related

- [TECHNICAL.md](TECHNICAL.md)
- [USER_GUIDE.md](../USER_GUIDE.md)
