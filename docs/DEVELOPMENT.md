# Development Environment Setup

Guide for developers working on `inventory-server` and `inventory-client` in this monorepo.

## Repository Layout

```
mgie-java-nc-iii-b2-group-4-final-project/
├── inventory-server/          # REST API, migrations, .env
├── inventory-client/          # Swing desktop app
├── docs/                      # Monorepo documentation (this folder)
├── README.md
└── LICENSE
```

## Required Software

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 17+ | Compile (bytecode target Java 8) |
| Maven | 3.9+ | Build both modules |
| MySQL | 8.0+ | Local database |
| Git | 2.x+ | Version control |

Optional: IntelliJ IDEA CE, VS Code + Extension Pack for Java, MySQL Workbench / DBeaver.

## Clone and Open

```bash
git clone <repository-url>
cd mgie-java-nc-iii-b2-group-4-final-project
```

**IntelliJ:** Open the repo root or `inventory-server` / `inventory-client` as Maven projects. Set Project SDK to **17**, language level **8**.

**VS Code:** Open root folder; Java extension imports Maven modules.

## Local MySQL

### Linux (Ubuntu/Debian)

```bash
sudo apt install -y mysql-server openjdk-17-jdk maven
sudo mysql -e "CREATE DATABASE g4ims_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'dev_password';"
sudo mysql -e "GRANT ALL ON g4ims_local.* TO 'inventory_user'@'localhost';"
```

### macOS (Homebrew)

```bash
brew install openjdk@17 maven mysql
brew services start mysql
mysql -u root -e "CREATE DATABASE g4ims_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Windows

Install [MySQL 8](https://dev.mysql.com/downloads/mysql/) and [Temurin JDK 17](https://adoptium.net/). Create database via MySQL Workbench or `mysql` CLI.

### Docker (optional)

```bash
docker run -d --name g4ims-mysql \
  -e MYSQL_ROOT_PASSWORD=changeme \
  -e MYSQL_DATABASE=g4ims_local \
  -p 3307:3306 \
  mysql:8.0
```

Use in `.env`: `G4IMS_SERVER_DB_HOST=127.0.0.1`, `G4IMS_SERVER_DB_PORT=3307`, root or app user credentials.

## Server Development Setup

```bash
cd inventory-server
cp .env.example .env
# Edit DB password and AUTH_TOKEN_SECRET, or:
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar setup
java -jar target/inventory-server-1.0.0.jar migrate
```

### Run server without packaging (faster iteration)

```bash
mvn compile exec:java -Dexec.mainClass="com.group4.inventoryserver.Main"
```

Or after package:

```bash
java -jar target/inventory-server-1.0.0.jar
```

There is **no hot reload**; stop (Ctrl+C), recompile, restart.

### Useful server commands during development

```bash
java -jar target/inventory-server-1.0.0.jar status
java -jar target/inventory-server-1.0.0.jar config:validate
java -jar target/inventory-server-1.0.0.jar migrate:status
java -jar target/inventory-server-1.0.0.jar migrate:generate add_new_column
```

See [server/CLI.md](server/CLI.md).

### API explorer while developing

Open `http://localhost:8080/api/docs` when `G4IMS_SERVER_SWAGGER_ENABLED=true`.

OpenAPI source: `inventory-server/src/main/resources/docs/openapi.yaml`.

## Client Development Setup

```bash
cd inventory-client
mvn clean package -DskipTests
java -jar target/inventory-client-1.0.0.jar
```

Point at local server:

```bash
java -jar target/inventory-client-1.0.0.jar --server-url http://localhost:8080/api
```

Or edit `src/main/resources/client.properties` (`server.url=...`) before rebuild.

### Client run from IDE

Main class: `com.group4.inventoryclient.Main`

Program arguments example: `--server-url http://localhost:8080/api`

The client requires a running server. If setup is not `INITIALIZED`, the **setup wizard** opens instead of the guest shell.

## Code Style and Conventions

| Rule | Detail |
|------|--------|
| Language level | Java 8 source (no `var`, modules, records in production code) |
| Style | Google Java Style via `fmt-maven-plugin` on `mvn compile` |
| Format manually | `mvn fmt:format` |
| Server config | `.env` with `G4IMS_SERVER_*` keys ([CONFIGURATION.md](server/CONFIGURATION.md)) |
| Secrets | Never commit `.env`, passwords, or tokens |
| Framework | No Spring; native `HttpServer` only |

## Running Tests

```bash
cd inventory-server
mvn test
```

The client module currently has no automated test suite.

## Development Workflow Summary

1. Start MySQL
2. Configure `inventory-server/.env`
3. `mvn package` + `migrate` + run server
4. Run client JAR or `Main` from IDE
5. Use Swagger or client UI to exercise endpoints
6. Add migrations under `inventory-server/src/main/resources/db/migrations/` and update `migrations.index`

## Platform-Specific IDE Notes

### Windows

- PowerShell for Maven; use `copy .env.example .env`
- Line endings: Git `autocrlf` may affect shell scripts; server is Java-only

### macOS / Linux

- Ensure `JAVA_HOME` points to JDK 17 for Maven
- `mysqldump` on PATH for backup features in Settings

## Related Docs

- [BUILD.md](BUILD.md) — Maven goals and artifacts
- [RUN.md](RUN.md) — Startup order and troubleshooting
- [server/TECHNICAL.md](server/TECHNICAL.md) — Server architecture
- [client/TECHNICAL.md](client/TECHNICAL.md) — Client architecture
