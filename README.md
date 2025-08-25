# Arch Manager Back — Setup Guide

This README walks you through setting up the database, Docker, and dependencies to run the Spring Boot backend.

## Prerequisites

- **Java 17+**
- **Maven 3.9+** (or use the Maven Wrapper `mvnw`)
- **Docker Desktop**
  - Windows: default **named pipe** engine (no extra config needed)
  - macOS/Linux: default **unix socket**
- (Optional) **psql** client for PostgreSQL

---

## Quick Start (TL;DR)

```bash
# 1) Start PostgreSQL (via Docker)
docker run --name archmanager-postgres -e POSTGRES_PASSWORD=MonMotDePasse123   -p 5432:5432 -v arch_pgdata:/var/lib/postgresql/data -d postgres:16

# 2) (Optional) Pre-pull the Neo4j image to speed up first project creation
docker pull neo4j:5.26-community

# 3) Build & run the app
mvn -q -DskipTests clean package
mvn spring-boot:run

# 4) Open API docs
# http://localhost:8080/swagger-ui.html
```

> The app will auto-create/update the schema (`spring.jpa.hibernate.ddl-auto=update`).

---

## Configuration Overview

Your `application.yml` (already provided) sets these defaults:

### Spring & Database

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: MonMotDePasse123
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
```

- Connects to the **default `postgres`** database on `localhost:5432` with user `postgres`.
- If you prefer a dedicated DB, create it and change the URL, e.g.:
  ```
  jdbc:postgresql://localhost:5432/archmanager
  ```

### Docker & Neo4j

```yaml
app:
  docker:
    engine-path: "npipe:////./pipe/docker_engine" # Windows named pipe
    neo4j-image: "neo4j:5.26-community"
    memory-limit: 4294967296 # 4GB
    volume-path: "/data"
    host: "localhost:"
  neo4j:
    bolt-port: 7687
    http-port: 7474
    auth-url: "NEO4J_AUTH=neo4j/"
    bolt-prefix: "bolt://"
```

- **Windows** (default): `engine-path` points to the named pipe.
- **macOS/Linux**: set `engine-path` to `unix:///var/run/docker.sock`.
- The app provisions one **Neo4j container per project** (Community mode) and picks an **ephemeral host port** for Bolt automatically. Docker Desktop should be allotted **≥ 4 GB** RAM (see Docker Desktop settings).

### Project Runtime & Healthchecks

```yaml
app:
  project:
    slug-prefix: "proj-"
    volume-suffix: "_data"
    idle-threshold: 30 # minutes without activity
    idle-rate-ms: 600000 # check every 10 min
    chunk-size: 5000 # graph import batch size
  healthcheck:
    interval: 1s
    timeout: 60s
    start-period: 3s
    retries: 20
    cmd-shell: "CMD-SHELL"
    cmd-template: "wget -qO- http://localhost:%d || exit 1"
```

- Idle projects (no sessions, inactive beyond threshold) are **stopped automatically**.
- Graph imports run in chunks (default 5,000).

### Security & API Docs

```yaml
security:
  jwt:
    secret: "..." # demo secret, replace in production
    expiration-ms: 3600000
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

- Swagger UI at: **`/swagger-ui.html`**

---

## Database Setup

### Option A — Use Docker (recommended for local dev)

```bash
docker run --name archmanager-postgres   -e POSTGRES_PASSWORD=MonMotDePasse123   -p 5432:5432 -v arch_pgdata:/var/lib/postgresql/data   -d postgres:16
```

- Connects at `jdbc:postgresql://localhost:5432/postgres` with `postgres/MonMotDePasse123`.
- Schema is created/updated automatically on app startup.

### Option B — Use a local PostgreSQL installation

1. Ensure PostgreSQL is running on `localhost:5432`.
2. Create (optional) a dedicated database:
   ```sql
   CREATE DATABASE archmanager;
   ```
3. Update `spring.datasource.url` accordingly.

---

## Docker Engine Setup

- **Windows**: Docker Desktop uses the **named pipe** `//./pipe/docker_engine`, which matches
  `app.docker.engine-path = "npipe:////./pipe/docker_engine"`. Nothing more to do.
- **macOS/Linux**: change:
  ```yaml
  app:
    docker:
      engine-path: "unix:///var/run/docker.sock"
  ```
- Ensure Docker Desktop allocated memory ≥ **4 GB** (Settings → Resources).

> The app will automatically pull **`neo4j:5.26-community`** and create one container **per project**.

---

## Dependencies

This is a Maven project. Dependencies are defined in `pom.xml` and include:

- Spring Boot starters (Web, JPA, Validation, Security, Data Neo4j)
- PostgreSQL driver
- Docker Java client
- Lombok
- MapStruct
- JJWT
- springdoc OpenAPI

They are **downloaded automatically** on the first build.

> **IDE tips**
>
> - Enable **annotation processing** for **Lombok** and **MapStruct** in your IDE.
> - If you see mapper generation issues, reimport Maven and rebuild the project.

---

## Build & Run

```bash
# Clean, compile, download dependencies
mvn -q -DskipTests clean package

# Run the application
mvn spring-boot:run
# or
java -jar target/arch-manager-back-0.0.1-SNAPSHOT.jar
```

The app starts on **http://localhost:8080** by default.

Open API docs:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## Verifying Neo4j Provisioning

When you create/connect to a project, the app:

- provisions a **Neo4j container** (`neo4j:5.26-community`),
- waits until the container is **healthy**,
- assigns an **ephemeral host Bolt port**,
- and connects using `bolt://localhost:<ephemeralPort>`.

Logs will show the selected port and container ID.

---

## Production Notes

- Replace the **JWT secret** with a strong value via env var:
  ```bash
  export SECURITY_JWT_SECRET="your-strong-secret"
  ```
- Point `spring.datasource.url` to your managed PostgreSQL.
- Consider running Postgres and Neo4j on dedicated hosts/services.
- For Neo4j **Enterprise** (one DB per project on a cluster), the codebase already includes
  abstraction interfaces (`GraphAccessProvider`, `GraphProjectProvisioner`) so you can plug
  in Enterprise adapters without changing the application layer.

---
