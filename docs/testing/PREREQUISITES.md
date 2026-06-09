# Test Prerequisites

## Required software

- JDK 8+ (17 recommended for running Maven)
- Maven 3.9+
- Docker (MySQL test container on port **3307**)
- Linux UI tests: `xvfb` (`sudo apt install xvfb`)

## Test database

```bash
docker compose -f docker-compose.test.yml up -d
cp inventory-server/.env.test.example inventory-server/.env.test
./scripts/test/reset-db.sh initialized
```

Credentials: `g4ims_test` / `g4ims_test` on `127.0.0.1:3307`.

## Blockers checklist (this plan)

- [x] Permission codes aligned (`MainFrame` ↔ V004 seed)
- [x] Export URLs aligned (`/products/export`, `/reports/export?reportType=...`)
- [ ] Root-login API — **deferred** (separate plan)

## Default login (after `initialized` fixture)

- Username: `admin`
- Password: `Admin@123`
