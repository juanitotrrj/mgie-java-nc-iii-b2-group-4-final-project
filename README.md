# Inventory Management System — Group 4

A monorepo inventory system with a **Java Swing desktop client** and a **native Java SE 8 REST API server** backed by **MySQL 8**.

## Team Members

- Baillo, Abigail G.
- Cabatac, Alma J.
- De Guzman, Nicko G.
- Dumpit, Luzviminda G.
- Tarroja, Juanito III S.

## Architecture

| Component | Technology | Directory |
|-----------|------------|-----------|
| Client | Java Swing, Gson, HTTP client | `inventory-client/` |
| Server | `com.sun.net.httpserver`, JDBC, JWT, BCrypt | `inventory-server/` |
| Database | MySQL 8 (utf8mb4) | — |
| Build | Maven (JDK 17 → Java 8 bytecode) | — |

```
┌──────────────────┐     REST + JWT      ┌──────────────────┐     JDBC     ┌─────────┐
│ inventory-client │ ──────────────────► │ inventory-server │ ───────────► │ MySQL 8 │
└──────────────────┘                     └──────────────────┘              └─────────┘
```

## Project Structure

```
├── inventory-server/     # REST API, migrations, .env configuration
├── inventory-client/     # Desktop application (setup wizard + main app)
├── docs/                 # Full documentation (deployment, dev, build, run, user guides)
├── LICENSE
└── README.md             # This file
```

## Documentation

All detailed guides live under **[docs/](docs/)**:

| Topic | Document |
|-------|----------|
| **Index** | [docs/README.md](docs/README.md) |
| Deployment (production) | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |
| Development environment | [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) |
| Build (Maven / JARs) | [docs/BUILD.md](docs/BUILD.md) |
| Run (startup, CLI, troubleshooting) | [docs/RUN.md](docs/RUN.md) |
| **User guide** (client + server for operators) | [docs/USER_GUIDE.md](docs/USER_GUIDE.md) |
| Server technical reference | [docs/server/TECHNICAL.md](docs/server/TECHNICAL.md) |
| Server `.env` configuration | [docs/server/CONFIGURATION.md](docs/server/CONFIGURATION.md) |
| Server CLI commands | [docs/server/CLI.md](docs/server/CLI.md) |
| REST API overview | [docs/server/API.md](docs/server/API.md) |
| HTTP request lifecycle (PlantUML) | [inventory-server/docs/REQUEST_LIFECYCLE.md](inventory-server/docs/REQUEST_LIFECYCLE.md) |
| Client technical reference | [docs/client/TECHNICAL.md](docs/client/TECHNICAL.md) |

## Prerequisites

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | 17+ | Build (targets Java 8 bytecode) |
| JRE | 8+ | Run packaged JARs on deployment hosts |
| Maven | 3.9+ | Build |
| MySQL | 8.0+ | Database |

## Quick Start

### 1. Build

```bash
cd inventory-server && mvn clean package -DskipTests
cd ../inventory-client && mvn clean package -DskipTests
```

See [docs/BUILD.md](docs/BUILD.md).

### 2. Configure and start the server

```bash
cd inventory-server
cp .env.example .env
# Edit .env, or run interactive setup:
java -jar target/inventory-server-1.0.0.jar setup
java -jar target/inventory-server-1.0.0.jar migrate
java -jar target/inventory-server-1.0.0.jar
```

Verify: [http://localhost:8080/api/health](http://localhost:8080/api/health)  
API explorer: [http://localhost:8080/api/docs](http://localhost:8080/api/docs)

### 3. Run the client

```bash
cd inventory-client
java -jar target/inventory-client-1.0.0.jar
# Optional: java -jar target/inventory-client-1.0.0.jar --server-url http://localhost:8080/api
```

- First install: complete the **setup wizard** if the server is not `INITIALIZED`
- Daily use: **guest screen** → **Login** → main application

See [docs/RUN.md](docs/RUN.md) and [docs/USER_GUIDE.md](docs/USER_GUIDE.md).

## Configuration Summary

| Layer | Where | What |
|-------|-------|------|
| Server infrastructure | `inventory-server/.env` | DB, port, JWT secret, paths (`G4IMS_SERVER_*`) |
| Application settings | MySQL (via API) | Company, tax, notifications, etc. |
| Client server URL | `client.properties` or `--server-url` | API base URL |

Copy template: `inventory-server/.env.example` → `.env` (never commit `.env`).

## Development

- **Code style:** Google Java Style (`fmt-maven-plugin` on compile)
- **Language level:** Java 8 source APIs only
- **Tests:** `cd inventory-server && mvn test`

Full IDE and platform setup: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Deployment

Production checklist, Windows service notes, security, and backups: [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md).

## License

BSD 2-Clause. See [LICENSE](LICENSE).
