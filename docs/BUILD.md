# Build Guide

Both applications are **Maven** projects producing executable **fat JARs** (dependencies shaded into one file).

## Prerequisites

- JDK **17 or newer** (compiles to Java **8** bytecode)
- Maven **3.9+**
- Network access for first-time dependency download

Verify:

```bash
java -version
mvn -version
```

## Build the Server

```bash
cd inventory-server
mvn clean package -DskipTests
```

| Output | Path |
|--------|------|
| Runnable JAR | `target/inventory-server-1.0.0.jar` |
| Compiled classes | `target/classes/` |

### Server build with tests

```bash
mvn clean package
```

### Server compile only (faster)

```bash
mvn clean compile
```

Runs `fmt-maven-plugin` formatting during `process-sources`.

## Build the Client

```bash
cd inventory-client
mvn clean package -DskipTests
```

| Output | Path |
|--------|------|
| Runnable JAR | `target/inventory-client-1.0.0.jar` |

Main class (manifest): `com.group4.inventoryclient.Main`

## Build Both (monorepo root)

There is no parent POM; build each module:

```bash
cd inventory-server && mvn clean package -DskipTests && cd ..
cd inventory-client && mvn clean package -DskipTests
```

## Maven Goals Reference

| Command | Module | Purpose |
|---------|--------|---------|
| `mvn clean` | either | Remove `target/` |
| `mvn compile` | either | Compile + format (server/client) |
| `mvn package` | either | Create shaded JAR |
| `mvn test` | server | Run unit tests |
| `mvn fmt:format` | either | Apply Google Java Style |
| `mvn fmt:check` | either | Check formatting without writing |

## Version and Coordinates

| Module | artifactId | version |
|--------|------------|---------|
| Server | `inventory-server` | `1.0.0` |
| Client | `inventory-client` | `1.0.0` |

Group ID: `com.group4`

## CI / Release Artifacts

Typical pipeline steps:

1. `mvn -B clean package` (server with tests, client with or without)
2. Archive `inventory-server/target/inventory-server-1.0.0.jar`
3. Archive `inventory-client/target/inventory-client-1.0.0.jar`
4. Ship `inventory-server/.env.example` (not `.env`)
5. Ship `inventory-client` default `client.properties` or deployment override instructions

## Common Build Failures

| Symptom | Fix |
|---------|-----|
| `invalid target release: 1.8` | Install JDK 17+ |
| Compilation errors after pull | `mvn clean compile` |
| Formatter changes many files | Expected on first `mvn compile`; commit formatted sources |

## Next Steps

- [RUN.md](RUN.md) — run the JARs
- [DEPLOYMENT.md](DEPLOYMENT.md) — production deployment
