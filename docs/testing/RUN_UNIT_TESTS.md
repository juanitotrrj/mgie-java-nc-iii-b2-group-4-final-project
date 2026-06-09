# Run Unit Tests

## Purpose

Fast feedback on services, DTOs, client session/API helpers without Docker.

## Linux / macOS

```bash
mvn test -pl inventory-server,inventory-client
```

## Windows (PowerShell)

```powershell
mvn test -pl inventory-server,inventory-client
```

## Reports

`inventory-server/target/surefire-reports/`, `inventory-client/target/surefire-reports/`
