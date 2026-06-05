# Run Full Test Pyramid

## Ordered orchestration

1. `docker compose -f docker-compose.test.yml up -d`
2. `cp inventory-server/.env.test.example inventory-server/.env.test`
3. `./scripts/test/reset-db.sh initialized`
4. `mvn test -pl inventory-server,inventory-client` — unit
5. `mvn verify -Pcoverage-check -pl inventory-server` — JaCoCo gate (80% line / 70% branch)
6. `mvn verify -Pintegration -pl inventory-server -Dg4ims.env.file=.env.test`
7. `mvn verify -Papi -pl inventory-server -Dg4ims.env.file=.env.test`
8. `xvfb-run mvn verify -Pui -pl inventory-client` (optional)
9. `mvn test -Pe2e -pl test-automation`
10. `mvn test -Puat -pl test-automation`
11. `mvn test -Pperf -pl inventory-server`
12. `mvn verify -Pload -pl test-automation` (nightly)

### Coverage report (optional, non-blocking)

After step 4, open the HTML report without enforcing thresholds:

```bash
mvn verify -Pcoverage -pl inventory-server
# inventory-server/target/site/jacoco/index.html
```

## Script shortcuts

```bash
./scripts/test/run-linux.sh unit-only
./scripts/test/run-linux.sh full
./scripts/test/run-linux.sh e2e
```

Windows: `.\scripts\test\run-windows.ps1 -Profile full`

## Performance and load (isolated)

```bash
mvn test -Pperf -pl inventory-server
mvn verify -Pload -pl test-automation -Dserver.host=localhost -Dserver.port=18080
```
