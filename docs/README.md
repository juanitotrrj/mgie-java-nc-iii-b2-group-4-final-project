# G4IMS Documentation

Technical and operational documentation for the Group 4 Inventory Management System monorepo.

![Coverage](https://img.shields.io/badge/coverage-80%25%20lines%20%7C%2070%25%20branches-blue)
*(Run `mvn verify -Pcoverage-check -pl inventory-server` locally to refresh; badge reflects enforced JaCoCo gate.)*

## Applications

| Application | Directory | Description |
|-------------|-----------|-------------|
| **Server** | `inventory-server/` | Java SE 8 REST API (`com.sun.net.httpserver`), MySQL, JWT auth |
| **Client** | `inventory-client/` | Java Swing desktop application |

## Documentation Index

### Operations (start here)

| Document | Contents |
|----------|----------|
| [DEPLOYMENT.md](DEPLOYMENT.md) | Production deployment: prerequisites, database, `.env`, first-time setup, Windows service notes |
| [DEVELOPMENT.md](DEVELOPMENT.md) | Developer workstation setup (Windows, macOS, Linux), IDE, MySQL, conventions |
| [BUILD.md](BUILD.md) | Maven build commands for server and client |
| [RUN.md](RUN.md) | How to start server and client, CLI commands, health checks, troubleshooting |
| [TESTING.md](TESTING.md) | Automated test pyramid hub (unit → load), JaCoCo coverage, scripts, CI profiles |
| [USER_GUIDE.md](USER_GUIDE.md) | End-user instructions: setup wizard, guest mode, login, modules by role |

### Server

| Document | Contents |
|----------|----------|
| [server/TECHNICAL.md](server/TECHNICAL.md) | Architecture, layers, middleware, security, migrations |
| [server/CONFIGURATION.md](server/CONFIGURATION.md) | `.env` variables reference |
| [server/CLI.md](server/CLI.md) | JAR command-line interface |
| [server/API.md](server/API.md) | REST API overview, auth, modules, Swagger |
| [../inventory-server/docs/REQUEST_LIFECYCLE.md](../inventory-server/docs/REQUEST_LIFECYCLE.md) | HTTP request lifecycle (PlantUML diagrams) |

### Client

| Document | Contents |
|----------|----------|
| [client/TECHNICAL.md](client/TECHNICAL.md) | Swing architecture, packages, API layer, session, UI flow |

## Code coverage

JaCoCo reports are generated when running server unit tests:

```bash
mvn test -pl inventory-server
open inventory-server/target/site/jacoco/index.html   # macOS
xdg-open inventory-server/target/site/jacoco/index.html   # Linux
```

Enforce project thresholds (80% lines, 70% branches):

```bash
mvn verify -Pcoverage-check -pl inventory-server
```

See [TESTING.md](TESTING.md#jacoco-coverage) for profile details.

## Quick Start (local)

```bash
# 1. Server: configure and build
cd inventory-server
cp .env.example .env
# Edit .env OR run: java -jar target/inventory-server-1.0.0.jar setup
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar migrate
java -jar target/inventory-server-1.0.0.jar

# 2. Client: build and run (separate terminal)
cd ../inventory-client
mvn clean package -DskipTests
java -jar target/inventory-client-1.0.0.jar
```

Verify server: `http://localhost:8080/api/health`  
API explorer: `http://localhost:8080/api/docs`

See [RUN.md](RUN.md) for full startup order (including two-stage setup).
