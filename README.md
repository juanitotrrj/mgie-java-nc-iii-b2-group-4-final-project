# Inventory Management System - Group 4

A full-stack inventory management system built with a Java Swing desktop client and a native Java SE 8 REST API backend.

## Team Members

- Baillo, Abigail
- Cabatac, Alma
- De Guzman, Nicko G.
- Dumpit, Luzviminda
- Tarroja, Juanito III S.

## Architecture

| Component | Technology |
|-----------|-----------|
| Client | Java Swing (desktop) |
| Server | Java SE 8 REST API (`com.sun.net.httpserver`) |
| Database | MySQL 8 |
| Build | Maven |

## Project Structure

```
├── inventory-server/    # Backend REST API
├── inventory-client/    # Java Swing desktop client (TBD)
├── LICENSE
└── README.md
```

## Installation Guide

### Prerequisites

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | 17+ | Building the project (bytecode targets Java 8) |
| Maven | 3.9+ | Dependency management and build |
| MySQL | 8.0+ | Database |
| Git | 2.x+ | Source control |

### Windows

#### 1. Install JDK 17

Download and install from [Adoptium](https://adoptium.net/temurin/releases/?version=17) (select Windows x64 `.msi`).

After installing, verify:

```powershell
java -version
```

If `java` is not recognized, add it to PATH:

```powershell
# Find where Java was installed, then:
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot", "User")
[Environment]::SetEnvironmentVariable("Path", "$env:Path;%JAVA_HOME%\bin", "User")
```

#### 2. Install Maven

Download the binary zip from [maven.apache.org](https://maven.apache.org/download.cgi). Extract to `C:\Program Files\Maven`.

```powershell
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Maven\apache-maven-3.9.6", "User")
[Environment]::SetEnvironmentVariable("Path", "$env:Path;%MAVEN_HOME%\bin", "User")
```

Verify:

```powershell
mvn -version
```

#### 3. Install MySQL

Download and install [MySQL Community Server 8.0](https://dev.mysql.com/downloads/mysql/) (select Windows x64 MSI Installer). During setup, note the root password you set.

Create the database:

```powershell
mysql -u root -p -e "CREATE DATABASE inventory_system_group4 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### 4. Register Environment Variables

```powershell
cd inventory-server\scripts
powershell -ExecutionPolicy Bypass -File .\register-server-env-windows.ps1
```

Edit the sensitive values afterward:

```powershell
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_DB_PASSWORD", "your_actual_password", "User")
[Environment]::SetEnvironmentVariable("G4IMS_SERVER_AUTH_TOKEN_SECRET", "a_random_string_at_least_32_characters_long", "User")
```

Restart PowerShell after registering.

#### 5. Build and Run

```powershell
cd inventory-server
mvn clean package -DskipTests
java -jar target\inventory-server-1.0.0.jar
```

Verify: open a browser to `http://localhost:8080/api/health`

---

### macOS

#### 1. Install Homebrew (if not already installed)

```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. Install JDK 17, Maven, and MySQL

```bash
brew install openjdk@17 maven mysql
```

Link JDK:

```bash
sudo ln -sfn $(brew --prefix openjdk@17)/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Verify:

```bash
java -version
mvn -version
```

#### 3. Start MySQL and Create Database

```bash
brew services start mysql
mysql -u root -e "CREATE DATABASE inventory_system_group4 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -e "CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'your_password';"
mysql -u root -e "GRANT ALL PRIVILEGES ON inventory_system_group4.* TO 'inventory_user'@'localhost';"
```

#### 4. Register Environment Variables

```bash
cd inventory-server/scripts
bash register-server-env-linux.sh
```

Edit `~/.config/g4ims/g4ims-server.env` and set your actual database password and token secret, then reload:

```bash
source ~/.config/g4ims/g4ims-server.env
```

#### 5. Build and Run

```bash
cd inventory-server
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar
```

Verify:

```bash
curl http://localhost:8080/api/health
```

---

### Linux (Ubuntu/Debian)

#### 1. Install JDK 17 and Maven

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven
```

Verify:

```bash
java -version
mvn -version
```

#### 2. Install MySQL

```bash
sudo apt install -y mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

Secure the installation and set the root password:

```bash
sudo mysql_secure_installation
```

Create the database and user:

```bash
sudo mysql -e "CREATE DATABASE inventory_system_group4 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
sudo mysql -e "CREATE USER 'inventory_user'@'localhost' IDENTIFIED BY 'your_password';"
sudo mysql -e "GRANT ALL PRIVILEGES ON inventory_system_group4.* TO 'inventory_user'@'localhost';"
sudo mysql -e "FLUSH PRIVILEGES;"
```

#### 3. Register Environment Variables

```bash
cd inventory-server/scripts
bash register-server-env-linux.sh
```

Edit `~/.config/g4ims/g4ims-server.env` and replace the default passwords/secrets:

```bash
nano ~/.config/g4ims/g4ims-server.env
```

Reload:

```bash
source ~/.config/g4ims/g4ims-server.env
```

#### 4. Build and Run

```bash
cd inventory-server
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar
```

Verify:

```bash
curl http://localhost:8080/api/health
```

---

## Running Migrations

```bash
# Run pending migrations
java -jar target/inventory-server-1.0.0.jar migrate

# Check migration status
java -jar target/inventory-server-1.0.0.jar migrate:status

# Generate a new migration
java -jar target/inventory-server-1.0.0.jar migrate:generate create_users_table

# Rollback last migration
java -jar target/inventory-server-1.0.0.jar migrate:rollback
```

## Environment Configuration

All configuration is driven by environment variables prefixed with `G4IMS_SERVER_*` (server) and `G4IMS_CLIENT_*` (client). See `inventory-server/scripts/g4ims-server.env.example` for the full list of 116 server variables.

Key variables to set before first run:

| Variable | Purpose |
|----------|---------|
| `G4IMS_SERVER_DB_PASSWORD` | MySQL password |
| `G4IMS_SERVER_DB_PORT` | MySQL port (default: 3306) |
| `G4IMS_SERVER_AUTH_TOKEN_SECRET` | JWT signing key (32+ chars) |
| `G4IMS_SERVER_SERVER_PORT` | HTTP server port (default: 8080) |

## Development Environment Setup

### Windows

#### IDE: IntelliJ IDEA Community Edition (Recommended)

1. Download from [jetbrains.com/idea](https://www.jetbrains.com/idea/download/?section=windows) (Community is free).
2. On first launch, go to **File > Project Structure > SDKs**, add your JDK 17 installation.
3. Open the `inventory-server` folder as a Maven project (IntelliJ auto-detects `pom.xml`).
4. Set Project SDK to 17 and language level to 8 under **File > Project Structure > Project**.

#### IDE Alternative: VS Code

1. Install [VS Code](https://code.visualstudio.com/).
2. Install the **Extension Pack for Java** from the marketplace.
3. Open the monorepo root folder. VS Code will detect the Maven project automatically.

#### Git

Download and install from [git-scm.com](https://git-scm.com/download/win). Use default settings. Verify:

```powershell
git --version
```

#### Running in Development

```powershell
cd inventory-server
mvn clean compile exec:java -Dexec.mainClass="com.group4.inventoryserver.Main"
```

Or build and run the JAR:

```powershell
mvn clean package -DskipTests
java -jar target\inventory-server-1.0.0.jar
```

#### Hot Reload (Manual)

There is no automatic hot-reload for `com.sun.net.httpserver`. Stop the server (Ctrl+C), rebuild, and restart. For faster iteration during development:

```powershell
mvn compile exec:java -Dexec.mainClass="com.group4.inventoryserver.Main"
```

#### MySQL Workbench (Optional)

Download from [dev.mysql.com/downloads/workbench](https://dev.mysql.com/downloads/workbench/) for a GUI to inspect the database.

---

### macOS

#### IDE: IntelliJ IDEA Community Edition

```bash
brew install --cask intellij-idea-ce
```

Alternatively, VS Code:

```bash
brew install --cask visual-studio-code
```

Then install the **Extension Pack for Java**.

#### Git

macOS ships with Git via Xcode Command Line Tools:

```bash
xcode-select --install
git --version
```

#### Development Workflow

```bash
cd inventory-server

# Compile and run directly (no JAR packaging needed)
mvn compile exec:java -Dexec.mainClass="com.group4.inventoryserver.Main"

# Or full build
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar
```

#### Database GUI (Optional)

```bash
brew install --cask sequel-ace
```

Or use MySQL Workbench:

```bash
brew install --cask mysqlworkbench
```

---

### Linux (Ubuntu/Debian)

#### IDE: IntelliJ IDEA Community Edition

```bash
sudo snap install intellij-idea-community --classic
```

Alternatively, VS Code:

```bash
sudo snap install code --classic
```

Then install the **Extension Pack for Java** from the Extensions panel.

#### Git

```bash
sudo apt install -y git
git --version
```

#### Development Workflow

```bash
cd inventory-server

# Compile and run directly
mvn compile exec:java -Dexec.mainClass="com.group4.inventoryserver.Main"

# Or full build
mvn clean package -DskipTests
java -jar target/inventory-server-1.0.0.jar
```

#### Database GUI (Optional)

```bash
sudo snap install dbeaver-ce
```

---

### Code Style

This project uses **Google Java Style Guide** enforced by the `fmt-maven-plugin`. The formatter runs automatically during `mvn compile`. To format manually:

```bash
mvn fmt:format
```

Configure your IDE to use 2-space indentation, no tabs, and 100-character line width to match.

### Project Conventions

- Java source level: **8** (no Java 9+ APIs)
- Build JDK: **17**
- All configuration via environment variables (`G4IMS_SERVER_*`)
- No `application.properties` committed — use `scripts/g4ims-server.env.example` as reference
- Sensitive values (passwords, secrets) must never be committed to git

### Useful Maven Commands

| Command | Purpose |
|---------|---------|
| `mvn clean compile` | Compile sources (also formats code) |
| `mvn clean package -DskipTests` | Build fat JAR |
| `mvn fmt:format` | Format code to Google Style |
| `mvn fmt:check` | Check formatting without modifying |
| `mvn test` | Run unit tests |

## License

BSD 2-Clause. See [LICENSE](LICENSE).
