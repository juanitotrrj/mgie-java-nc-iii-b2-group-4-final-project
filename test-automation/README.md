# G4IMS Test Automation

Cucumber BDD suite for API, E2E, and UAT scenarios. Root-login scenarios are tagged `@root-login @deferred` and excluded from default CI runs.

## Quick start

```bash
# From monorepo root
docker compose -f docker-compose.test.yml up -d
cp inventory-server/.env.test.example inventory-server/.env.test
./scripts/test/reset-db.sh initialized

# Start server (separate terminal)
cd inventory-server && mvn -q package -DskipTests && java -Dg4ims.env.file=.env.test -jar target/inventory-server-1.0.0.jar serve

# Run E2E Cucumber
cd test-automation
mvn test -Pe2e -De2e.server.url=http://localhost:18080/api
```

See [docs/TESTING.md](../docs/TESTING.md) for the full pyramid.
