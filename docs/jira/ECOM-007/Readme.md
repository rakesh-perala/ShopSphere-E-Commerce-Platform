# ECOM-007 — Docker 

# ShopSphere Enterprise E-Commerce Platform

> **Jira:** ECOM-007
> **Feature Branch:** `feature/ECOM-007-docker-containerization`
> **Implementation Commit:** `ecbed52 ECOM-007: implement Docker containerization`
> **Primary Service:** `user-service`
> **Application Type:** Spring Boot REST API
> **Application Port:** `8081`
> **Database:** PostgreSQL
> **Database Port:** `5432`
> **Docker Image:** `shopsphere/user-service:1.0.0`

---

# 1. Business Requirement

ShopSphere is a microservices-based e-commerce platform.

The application consists of independently deployable services such as:

```text
User Service
Product Service
Cart Service
Order Service
Payment Service
```

Each service needs a consistent runtime so that the application can move through environments without depending on manual server configuration.

The business requirement for ECOM-007 is to containerize the ShopSphere User Service.

The application should be able to move through the following lifecycle:

```text
Developer Code
      |
      v
Maven Build
      |
      v
Unit Tests
      |
      v
Spring Boot JAR
      |
      v
Docker Image
      |
      v
Docker Container
      |
      v
Runtime Validation
      |
      v
Future CI/CD
      |
      v
Amazon ECR
      |
      v
Amazon EKS
```

The implementation must also support a database-backed runtime.

Therefore the local Docker environment contains:

```text
User Service Container
        |
        | Docker Network
        |
        v
PostgreSQL Container
        |
        v
Named Docker Volume
```

---

## 1.1 ECOM-007 Scope

The ticket established:

```text
✅ User Service Dockerfile
✅ .dockerignore
✅ Docker-ready application.yml
✅ Maven JAR build
✅ Docker image build
✅ PostgreSQL container
✅ Docker network
✅ PostgreSQL persistent volume
✅ User Service container
✅ Database connectivity
✅ API validation
✅ Docker runtime troubleshooting
```

The following are later platform stages:

```text
⏭️ Jenkins CI pipeline
⏭️ SonarQube integration
⏭️ Trivy CI integration
⏭️ Nexus artifact repository
⏭️ Amazon ECR
⏭️ Kubernetes / EKS deployment
⏭️ RDS PostgreSQL
```

Those concepts are documented in this Docker Bible so the complete enterprise flow is understood.

---

# 2. Why Business Needs It

## 2.1 Problem With Manual Deployment

A traditional server deployment can look like:

```text
Engineer
   |
   +--> Create VM
   |
   +--> Install Java
   |
   +--> Install Maven
   |
   +--> Copy JAR
   |
   +--> Configure database
   |
   +--> Configure ports
   |
   +--> Configure system service
   |
   +--> Repeat in another environment
```

This can create:

```text
Environment A
    |
    +--> Java version X
    +--> Different OS configuration

Environment B
    |
    +--> Java version Y
    +--> Different OS configuration
```

The application may behave differently.

---

## 2.2 Docker Solution

Docker packages the application runtime into a repeatable unit.

```text
Application
    +
Runtime
    +
Startup Instructions
    |
    v
Docker Image
```

Then:

```text
Docker Image
      |
      v
Container
```

The same image can later be promoted through environments.

---

## 2.3 Enterprise Benefits

Containerization provides:

```text
Consistency
Repeatability
Portability
Isolation
Faster deployment
Simpler rollback
Better CI/CD integration
Environment standardization
```

---

# 3. Real-Time Enterprise Scenario

Imagine ShopSphere has 10 developers working on the User Service.

Developer completes a feature:

```text
Developer
    |
    v
Git Commit
    |
    v
Jenkins
    |
    +--> Maven Build
    +--> Unit Tests
    +--> SonarQube
    +--> Quality Gate
    +--> Trivy Filesystem Scan
    |
    v
JAR
    |
    v
Docker Build
    |
    v
Docker Image
    |
    +--> Trivy Image Scan
    |
    v
Amazon ECR
    |
    v
DEV
    |
    v
UAT
    |
    v
PROD
    |
    v
EKS
```

The key enterprise principle is:

> **Build once, promote the same validated image.**

That means production should not require rebuilding the image.

---

# 4. Architecture Diagram

## 4.1 Local Docker Architecture

```text
                         Docker Host
                              |
              +---------------+----------------+
              |                                |
              v                                v
   shopsphere-user-service            shopsphere-postgres
              |                                |
              |                                |
              +-------- shopsphere-network ----+
                               |
                               v
                      Docker Internal DNS
                               |
                               v
                    shopsphere-postgres
```

---

## 4.2 User Service Request Flow

```text
Client
  |
  | HTTP
  | :8081
  v
User Service Container
  |
  | JDBC
  | :5432
  v
PostgreSQL Container
  |
  v
Named Docker Volume
```

---

## 4.3 Image Lifecycle

```text
Source Code
    |
    v
Maven
    |
    v
user-service JAR
    |
    v
Docker Build
    |
    v
shopsphere/user-service:1.0.0
    |
    v
Container
```

---

## 4.4 Detailed Docker Runtime

```text
Docker Engine
│
├── Network
│   └── shopsphere-network
│
├── Container
│   └── shopsphere-user-service
│       ├── Port 8081
│       ├── Java 17 Runtime
│       └── Spring Boot JAR
│
├── Container
│   └── shopsphere-postgres
│       ├── Port 5432
│       └── PostgreSQL 17
│
└── Volume
    └── shopsphere-postgres-data
        └── PostgreSQL data
```

---

## 4.5 Future AWS Architecture

```text
                       Amazon Web Services
                              |
              +---------------+---------------+
              |                               |
              v                               v
             ECR                             EKS
              |                               |
              |                               v
              |                        Kubernetes Pod
              |                               |
              |                               v
              |                       User Service
              |                               |
              |                               v
              |                        Kubernetes Service
              |
              v
        Docker Image


Database:

User Service
     |
     v
RDS PostgreSQL
```

---

# 5. Repository Structure

```text
ShopSphere-E-Commerce-Platform/
│
├── README.md
│
├── application/
│   ├── frontend/
│   │
│   └── services/
│       ├── user-service/
│       │   ├── pom.xml
│       │   ├── Dockerfile
│       │   ├── .dockerignore
│       │   │
│       │   └── src/
│       │       ├── main/
│       │       │   ├── java/
│       │       │   └── resources/
│       │       │       └── application.yml
│       │       │
│       │       └── test/
│       │
│       ├── product-service/
│       ├── cart-service/
│       ├── order-service/
│       └── payment-service/
│
├── docker/
│
├── jenkins/
│
├── kubernetes/
│
├── helm/
│
├── monitoring/
│
├── logging/
│
├── terraform/
│
└── docs/
    └── jira/
        └── ECOM-007/
            └── README.md
```

---

## 5.1 Why Dockerfile Lives With the Service

The User Service owns its runtime definition.

Therefore:

```text
application/services/user-service/
        |
        +--> Source
        +--> pom.xml
        +--> Dockerfile
        +--> application.yml
```

This keeps the service independently buildable.

---

# 6. Files Created / Modified

## 6.1 Dockerfile

**Path**

```text
application/services/user-service/Dockerfile
```

Contents:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 6.2 `.dockerignore`

**Path**

```text
application/services/user-service/.dockerignore
```

Contents:

```text
.git
.gitignore
src/
pom.xml
target/classes/
target/generated-sources/
target/generated-test-sources/
target/maven-status/
target/surefire-reports/
target/test-classes/
*.log
```

---

## 6.3 `application.yml`

**Path**

```text
application/services/user-service/src/main/resources/application.yml
```

Contents:

```yaml
spring:
  application:
    name: user-service

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:shopsphere_user}
    username: ${DB_USERNAME:shopsphere}
    password: ${DB_PASSWORD:shopsphere}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8081
```

---

## 6.4 PostgreSQL Container

Image:

```text
postgres:17
```

No custom PostgreSQL Dockerfile was required.

---

## 6.5 Docker Network

```text
shopsphere-network
```

---

## 6.6 Docker Volume

```text
shopsphere-postgres-data
```

---

# 7. Deep Concept Explanation

# 7.1 What Is Docker?

Docker is a container platform used to package and run applications in isolated environments.

The major components are:

```text
Dockerfile
   |
   v
Docker Image
   |
   v
Docker Container
```

---

# 7.2 What Is a Dockerfile?

A Dockerfile is a text file containing instructions used to build a Docker image.

Example:

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

# 7.3 What Is a Docker Image?

A Docker image is a packaged, immutable template used to create containers.

Our image:

```text
shopsphere/user-service:1.0.0
```

It contains the application runtime definition and the application JAR.

---

# 7.4 What Is a Container?

A container is a running instance of a Docker image.

```text
Image
  |
  v
Container
```

Our running application container:

```text
shopsphere-user-service
```

---

# 7.5 JAR vs Image vs Container

This is one of the most important interview questions.

```text
JAR
 |
 +--> Java application artifact
 |
 v
Docker Image
 |
 +--> packaged application runtime
 |
 v
Docker Container
 |
 +--> running process
```

Example:

```text
target/user-service-0.0.1-SNAPSHOT.jar

        ↓

shopsphere/user-service:1.0.0

        ↓

shopsphere-user-service
```

---

# 7.6 Docker Build Context

When running:

```bash
docker build -t shopsphere/user-service:1.0.0 .
```

the final `.` means:

```text
Current Directory
       |
       v
Build Context
```

Docker can access files in the build context.

This is why `.dockerignore` is important.

---

# 7.7 Docker Image Layers

Docker images are built using layers.

Conceptually:

```text
Image
│
├── Base image layer
│
├── Application filesystem changes
│
├── JAR layer
│
└── Metadata/configuration
```

The base image:

```text
eclipse-temurin:17-jre
```

provides the runtime foundation.

The application layer contains the JAR.

Layered images help Docker reuse unchanged parts during builds.

---

# 7.8 Docker Build Cache

Suppose the Dockerfile is:

```text
FROM
WORKDIR
COPY JAR
EXPOSE
ENTRYPOINT
```

If earlier layers remain unchanged, Docker may reuse cached layers.

This helps speed up repeated builds.

Enterprise CI systems should structure Dockerfiles so that expensive stable operations are cache-friendly.

---

# 7.9 Why Runtime Image Uses JRE

The User Service JAR is already compiled.

Therefore the runtime container does not need the complete build toolchain.

The Dockerfile uses:

```text
eclipse-temurin:17-jre
```

The CI/build environment can use:

```text
JDK 17
Maven
```

Conceptually:

```text
Build Environment
    |
    +--> JDK
    +--> Maven
    |
    v
JAR

Runtime Environment
    |
    +--> JRE
    |
    v
Application
```

---

# 7.10 Dockerfile vs Image vs Container

| Component     | Purpose                        |
| ------------- | ------------------------------ |
| Dockerfile    | Instructions to build an image |
| Image         | Immutable package/template     |
| Container     | Running instance of an image   |
| Registry      | Stores/publishes images        |
| Docker Engine | Runs containers                |

---

# 7.11 Container Lifecycle

```text
Created
   |
   v
Running
   |
   +-----> Stopped
   |          |
   |          v
   |        Started
   |
   v
Removed
```

Typical commands:

```bash
docker create
docker start
docker stop
docker rm
docker run
```

`docker run` normally combines create and start behavior.

---

# 7.12 Restart vs Recreate

Restart:

```bash
docker restart shopsphere-user-service
```

starts the same container again.

Recreate:

```text
Remove Container
       |
       v
Create New Container
```

is useful when configuration or image version changes.

---

# 7.13 Container Ephemeral Nature

Containers are generally treated as disposable runtime instances.

Therefore:

```text
Application container
        |
        +--> Ephemeral
```

Persistent state should be stored separately.

For PostgreSQL:

```text
PostgreSQL container
        |
        v
Named volume
```

---

# 7.14 Docker Network

A Docker network provides communication between containers.

Our network:

```text
shopsphere-network
```

contains:

```text
shopsphere-user-service
shopsphere-postgres
```

---

# 7.15 Docker DNS

Docker provides DNS-based service discovery on user-defined networks.

Therefore:

```text
shopsphere-user-service
        |
        | DNS
        v
shopsphere-postgres
```

The application does not need to know the database container's IP.

---

# 7.16 Why Container IP Is Not Used

Bad:

```text
DB_HOST=172.18.0.2
```

Better:

```text
DB_HOST=shopsphere-postgres
```

Reason:

```text
Container IP
    |
    +--> Dynamic

Container name
    |
    +--> Stable service reference
```

---

# 7.17 Docker Port Mapping

This command:

```bash
-p 8081:8081
```

means:

```text
Host
8081
 |
 v
Container
8081
```

Likewise:

```bash
-p 5432:5432
```

means:

```text
Host
5432
 |
 v
PostgreSQL Container
5432
```

---

# 7.18 EXPOSE vs Port Publishing

This:

```dockerfile
EXPOSE 8081
```

does not publish the port.

It documents the application port.

Port publishing happens with:

```bash
-p 8081:8081
```

---

# 7.19 Environment Variables

The application configuration uses:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

This makes configuration environment-specific.

Example:

```text
Local Java process
DB_HOST=localhost
```

Docker:

```text
DB_HOST=shopsphere-postgres
```

Kubernetes:

```text
DB_HOST=<Kubernetes Service DNS>
```

Same image can therefore run in different environments.

---

# 7.20 Spring Placeholder Syntax

This:

```text
${DB_HOST:localhost}
```

means:

```text
Use DB_HOST
     |
     +--> if provided

Otherwise:
     |
     +--> localhost
```

This is useful for development and containerized execution.

---

# 7.21 Docker Volume

Our PostgreSQL volume:

```text
shopsphere-postgres-data
```

stores database data.

Flow:

```text
PostgreSQL
    |
    v
/var/lib/postgresql/data
    |
    v
shopsphere-postgres-data
```

---

# 7.22 Bind Mount vs Named Volume

### Named Volume

```text
-v shopsphere-postgres-data:/var/lib/postgresql/data
```

Docker manages the storage location.

### Bind Mount

Conceptually:

```text
-v /host/path:/container/path
```

The host controls the source path.

For databases, named volumes are often simpler for local containerized development.

---

# 7.23 Why Application Data and Database Data Are Different

The User Service is stateless from the application's perspective:

```text
Container
   |
   +--> Can be replaced
```

The PostgreSQL data is stateful:

```text
Database
   |
   +--> Must persist
```

Therefore:

```text
Application Container
      |
      X
      |
      +--> Do not depend on container-local persistence

Database
      |
      v
Persistent Storage
```

---

# 7.24 Docker Secrets

For production, passwords should not be stored as plain source-controlled defaults.

Current training configuration contains:

```text
DB_PASSWORD
```

as a lab value.

Enterprise systems should use:

```text
Jenkins Credentials
AWS Secrets Manager
Kubernetes Secrets
External Secrets
Vault
```

depending on the platform.

---

# 7.25 Docker Image Tagging

Current image:

```text
shopsphere/user-service:1.0.0
```

Structure:

```text
repository/name:tag
```

Example:

```text
shopsphere/user-service:1.0.0
```

Avoid production dependence on:

```text
latest
```

because `latest` can point to different content over time.

Prefer:

```text
1.0.0
1.0.1
1.1.0
commit-SHA
```

---

# 7.26 Immutable Image

An immutable image means the image identified by a version should not be silently replaced with different application content.

Enterprise model:

```text
Build
  |
  v
Image v1.0.0
  |
  v
Scan
  |
  v
Registry
  |
  v
DEV
  |
  v
UAT
  |
  v
PROD
```

Same image.

---

# 7.27 Docker Registry

A registry stores images.

Examples:

```text
Docker Hub
Amazon ECR
GitHub Container Registry
Harbor
```

For ShopSphere AWS architecture:

```text
Nexus
 |
 +--> JAR / WAR artifacts

ECR
 |
 +--> Docker / OCI images
```

---

# 8. Every Command Explained

## 8.1 Maven Test

```bash
mvn clean test
```

### `clean`

Removes previous Maven build output.

### `test`

Runs tests.

This must pass before creating the image.

---

## 8.2 Maven Package

```bash
mvn clean package
```

Creates:

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

---

## 8.3 List JAR

```bash
ls -lh target/*.jar
```

Useful before Docker build.

---

## 8.4 Docker Build

```bash
docker build -t shopsphere/user-service:1.0.0 .
```

### `docker build`

Build an image.

### `-t`

Assign image name and tag.

### `.`

Use current directory as build context.

---

## 8.5 List Images

```bash
docker images
```

or:

```bash
docker image ls
```

---

## 8.6 Inspect Image

```bash
docker image inspect shopsphere/user-service:1.0.0
```

Useful for:

* entrypoint
* environment
* architecture
* layers
* metadata

---

## 8.7 Create Network

```bash
docker network create shopsphere-network
```

Creates the network.

---

## 8.8 List Networks

```bash
docker network ls
```

---

## 8.9 Inspect Network

```bash
docker network inspect shopsphere-network
```

Use this when troubleshooting service-to-service communication.

---

## 8.10 Create Volume

```bash
docker volume create shopsphere-postgres-data
```

---

## 8.11 List Volumes

```bash
docker volume ls
```

---

## 8.12 Inspect Volume

```bash
docker volume inspect shopsphere-postgres-data
```

---

## 8.13 Run PostgreSQL

```bash
docker run -d \
  --name shopsphere-postgres \
  --network shopsphere-network \
  -e POSTGRES_DB=shopsphere_user \
  -e POSTGRES_USER=shopsphere \
  -e POSTGRES_PASSWORD=shopsphere \
  -v shopsphere-postgres-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:17
```

---

## 8.14 Run User Service

```bash
docker run -d \
  --name shopsphere-user-service \
  --network shopsphere-network \
  -e DB_HOST=shopsphere-postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=shopsphere_user \
  -e DB_USERNAME=shopsphere \
  -e DB_PASSWORD=shopsphere \
  -p 8081:8081 \
  shopsphere/user-service:1.0.0
```

---

## 8.15 List Running Containers

```bash
docker ps
```

---

## 8.16 List All Containers

```bash
docker ps -a
```

---

## 8.17 Container Logs

```bash
docker logs shopsphere-user-service
```

---

## 8.18 Follow Logs

```bash
docker logs -f shopsphere-user-service
```

---

## 8.19 Last 100 Log Lines

```bash
docker logs --tail 100 shopsphere-user-service
```

---

## 8.20 Container Inspect

```bash
docker inspect shopsphere-user-service
```

---

## 8.21 Container Environment

```bash
docker inspect shopsphere-user-service \
  --format '{{range .Config.Env}}{{println .}}{{end}}'
```

---

## 8.22 Container IP

```bash
docker inspect shopsphere-postgres \
  --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}'
```

Useful for troubleshooting.

Do not use the result as permanent application configuration.

---

## 8.23 Container Ports

```bash
docker port shopsphere-user-service
```

---

## 8.24 Execute Command

```bash
docker exec -it shopsphere-user-service sh
```

---

## 8.25 Execute PostgreSQL Container Shell

Depending on the image:

```bash
docker exec -it shopsphere-postgres bash
```

or:

```bash
docker exec -it shopsphere-postgres sh
```

---

## 8.26 Restart Container

```bash
docker restart shopsphere-user-service
```

---

## 8.27 Stop Container

```bash
docker stop shopsphere-user-service
```

---

## 8.28 Start Container

```bash
docker start shopsphere-user-service
```

---

## 8.29 Remove Container

```bash
docker rm shopsphere-user-service
```

---

## 8.30 Force Remove Container

```bash
docker rm -f shopsphere-user-service
```

---

## 8.31 Remove Image

```bash
docker rmi shopsphere/user-service:1.0.0
```

Only do this when the image is no longer required.

---

## 8.32 Docker System Information

```bash
docker info
```

Useful for:

* Docker engine
* storage driver
* runtime
* resource information

---

## 8.33 Docker Version

```bash
docker version
```

---

## 8.34 Docker Disk Usage

```bash
docker system df
```

Useful when disk usage becomes a concern.

---

# 9. Every YAML / JSON / Configuration Explained

## 9.1 `application.yml`

Complete configuration:

```yaml
spring:
  application:
    name: user-service

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:shopsphere_user}
    username: ${DB_USERNAME:shopsphere}
    password: ${DB_PASSWORD:shopsphere}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8081
```

---

## 9.2 Spring Application Name

```yaml
spring:
  application:
    name: user-service
```

Identifies the application.

Useful in:

```text
Logs
Monitoring
Tracing
Service discovery
Operational dashboards
```

---

## 9.3 Datasource URL

```yaml
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:shopsphere_user}
```

This contains:

```text
jdbc
  |
  v
postgresql
  |
  v
DB_HOST
  |
  v
DB_PORT
  |
  v
DB_NAME
```

Docker example becomes:

```text
jdbc:postgresql://shopsphere-postgres:5432/shopsphere_user
```

---

## 9.4 Username

```yaml
username: ${DB_USERNAME:shopsphere}
```

Uses runtime variable when supplied.

---

## 9.5 Password

```yaml
password: ${DB_PASSWORD:shopsphere}
```

Uses runtime variable when supplied.

The hard-coded default is acceptable only for this training environment.

Do not carry training credentials into production.

---

## 9.6 Hibernate DDL

```yaml
ddl-auto: update
```

Used here for development/training convenience.

Production environments should generally use controlled migrations such as:

```text
Flyway
Liquibase
```

rather than allowing automatic schema mutation by application startup.

---

## 9.7 SQL Logging

```yaml
show-sql: false
```

Keeps normal logs cleaner.

---

## 9.8 SQL Formatting

```yaml
format_sql: true
```

Formats Hibernate-generated SQL when SQL output exists.

---

## 9.9 Server Port

```yaml
server:
  port: 8081
```

Spring Boot listens on port `8081`.

---

# 10. README Purpose / Deployment / Validation / Rollback / Usage

## 10.1 Purpose

This README is the canonical documentation for ECOM-007 Docker implementation.

It is intended for:

```text
Developers
DevOps Engineers
Platform Engineers
SRE Engineers
Interview Preparation
Troubleshooting
Future CI/CD implementation
```

---

# 10.2 Deployment Sequence

```text
1. Source Code
      |
      v
2. Maven Test
      |
      v
3. Maven Package
      |
      v
4. JAR
      |
      v
5. Docker Build
      |
      v
6. Docker Image
      |
      v
7. Docker Network
      |
      v
8. PostgreSQL Container
      |
      v
9. User Service Container
      |
      v
10. API Validation
```

---

# 10.3 Build Application

```bash
mvn clean package
```

Confirm the artifact:

```bash
ls -lh target/*.jar
```

---

# 10.4 Build Image

```bash
docker build \
  -t shopsphere/user-service:1.0.0 \
  .
```

Confirm:

```bash
docker images
```

---

# 10.5 Create Network

```bash
docker network create shopsphere-network
```

---

# 10.6 Create Database Volume

```bash
docker volume create shopsphere-postgres-data
```

---

# 10.7 Start PostgreSQL

```bash
docker run -d \
  --name shopsphere-postgres \
  --network shopsphere-network \
  -e POSTGRES_DB=shopsphere_user \
  -e POSTGRES_USER=shopsphere \
  -e POSTGRES_PASSWORD=shopsphere \
  -v shopsphere-postgres-data:/var/lib/postgresql/data \
  -p 5432:5432 \
  postgres:17
```

Check:

```bash
docker ps
```

---

# 10.8 Start User Service

```bash
docker run -d \
  --name shopsphere-user-service \
  --network shopsphere-network \
  -e DB_HOST=shopsphere-postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=shopsphere_user \
  -e DB_USERNAME=shopsphere \
  -e DB_PASSWORD=shopsphere \
  -p 8081:8081 \
  shopsphere/user-service:1.0.0
```

---

# 10.9 Validate Application

```bash
curl http://localhost:8081/api/users
```

Initial response:

```json
[]
```

---

# 10.10 Create Test User

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Rakesh",
    "lastName": "DevOps",
    "email": "rakesh.devops@example.com",
    "password": "DevOps123"
  }'
```

Response:

```json
{
  "id": 1,
  "firstName": "Rakesh",
  "lastName": "DevOps",
  "email": "rakesh.devops@example.com"
}
```

---

# 10.11 Get Users Again

```bash
curl http://localhost:8081/api/users
```

Expected:

```json
[
  {
    "id": 1,
    "firstName": "Rakesh",
    "lastName": "DevOps",
    "email": "rakesh.devops@example.com"
  }
]
```

This validates:

```text
HTTP
 |
 v
Spring Boot
 |
 v
Repository
 |
 v
PostgreSQL
 |
 v
Persistent Storage
```

---

# 10.12 Usage

Typical developer flow:

```bash
mvn clean package
docker build -t shopsphere/user-service:1.0.0 .
docker network create shopsphere-network
docker volume create shopsphere-postgres-data
docker run postgres
docker run user-service
curl http://localhost:8081/api/users
```

---

# 10.13 Rollback Usage

Suppose:

```text
Current image = 1.1.0
Previous image = 1.0.0
```

Rollback:

```text
1.1.0
  |
  X
  |
1.0.0
```

Run the previous image.

The database should be handled separately because application rollback and database schema rollback are different concerns.

---

# 11. Validation

## 11.1 Maven Validation

ECOM-007 User Service validation:

```text
15 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

---

## 11.2 Docker Image Validation

Image built successfully:

```text
shopsphere/user-service:1.0.0
```

---

## 11.3 PostgreSQL Validation

PostgreSQL container:

```text
shopsphere-postgres
```

using:

```text
postgres:17
```

---

## 11.4 Network Validation

Network:

```text
shopsphere-network
```

was created successfully.

---

## 11.5 Volume Validation

Volume:

```text
shopsphere-postgres-data
```

was created successfully.

---

## 11.6 User Service Container Validation

Container:

```text
shopsphere-user-service
```

was successfully started.

---

## 11.7 Database Connectivity Validation

The User Service successfully connected to PostgreSQL through:

```text
DB_HOST=shopsphere-postgres
```

---

## 11.8 API Validation

Successfully validated:

```text
GET /api/users
POST /api/users
```

The POST operation created the user in PostgreSQL and GET returned the stored record.

---

# 12. Troubleshooting & RCA

# 12.1 User Service Cannot Connect to Database

### Symptom

Application starts but database connection fails.

### First checks

```bash
docker ps
```

Then:

```bash
docker logs shopsphere-user-service
```

Then:

```bash
docker logs shopsphere-postgres
```

Then:

```bash
docker network inspect shopsphere-network
```

---

### Root Cause

Most common cause in this project:

```text
DB_HOST=localhost
```

inside the container.

Inside the User Service container:

```text
localhost
    |
    v
User Service Container
```

It does not point to PostgreSQL.

---

### Correct Configuration

```text
DB_HOST=shopsphere-postgres
```

---

# 12.2 PostgreSQL Container Is Not Running

Check:

```bash
docker ps -a
```

Look for:

```text
shopsphere-postgres
```

Then:

```bash
docker logs shopsphere-postgres
```

---

# 12.3 PostgreSQL Name Cannot Be Resolved

Check network:

```bash
docker network inspect shopsphere-network
```

Both containers should be attached.

Expected:

```text
shopsphere-user-service
shopsphere-postgres
```

---

# 12.4 Docker Build Cannot Find JAR

### Symptom

```text
COPY target/*.jar app.jar
```

fails.

### Root Cause

The JAR was not created.

### Resolution

```bash
mvn clean package
```

Then:

```bash
ls -lh target/*.jar
```

Then rebuild:

```bash
docker build -t shopsphere/user-service:1.0.0 .
```

---

# 12.5 `.dockerignore` Excludes Required Files

If the JAR is excluded, Docker cannot copy it.

Check:

```text
target/
```

Do not blindly exclude the entire `target/` directory if the final image build requires:

```text
target/*.jar
```

Our `.dockerignore` specifically excludes Maven internals while allowing the JAR.

---

# 12.6 Port 8081 Already in Use

Check:

```bash
ss -ltnp | grep 8081
```

Or:

```bash
docker ps
```

Then:

```bash
docker port shopsphere-user-service
```

Possible fix:

```bash
docker rm -f shopsphere-user-service
```

Then start the container again.

---

# 12.7 Port 5432 Already in Use

Check:

```bash
ss -ltnp | grep 5432
```

Possible cause:

```text
Local PostgreSQL service
```

is already using port 5432.

The container can use another host port if needed:

```text
-p 15432:5432
```

but the application would then need the appropriate host/runtime configuration when connecting externally.

---

# 12.8 Container Starts Then Stops

Run:

```bash
docker ps -a
```

Then:

```bash
docker logs shopsphere-user-service
```

Usually inspect:

```text
Application exception
Database connection
Invalid configuration
Missing environment variable
Port configuration
Java startup
```

---

# 12.9 Docker Permission Denied

If:

```bash
docker ps
```

returns permission errors for the Linux user:

Check:

```bash
id
```

The user may need Docker group membership.

Lab command:

```bash
sudo usermod -aG docker $USER
```

Then start a new login session or refresh the group.

This is a privileged capability and should be handled carefully in production.

---

# 12.10 PostgreSQL Data Missing

Check:

```bash
docker volume inspect shopsphere-postgres-data
```

Then:

```bash
docker inspect shopsphere-postgres
```

Verify the mount:

```text
/var/lib/postgresql/data
```

---

# 12.11 User Service Logs Show Database Timeout

Check:

```bash
docker network inspect shopsphere-network
```

Then verify:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

Inspect configuration:

```bash
docker inspect shopsphere-user-service
```

---

# 12.12 Docker DNS Troubleshooting

The application should use:

```text
shopsphere-postgres
```

not:

```text
172.18.0.2
```

If resolution fails:

1. Confirm both containers are on the same user-defined network.
2. Confirm PostgreSQL container name.
3. Confirm the PostgreSQL container is running.
4. Inspect network configuration.

---

# 12.13 Image Is Old

Sometimes developers rebuild but accidentally run an old container.

Check:

```bash
docker images
```

Check container:

```bash
docker inspect shopsphere-user-service
```

Check image reference:

```bash
docker inspect shopsphere-user-service \
  --format '{{.Config.Image}}'
```

Use explicit immutable tags.

---

# 12.14 Wrong Configuration Inside Container

Check:

```bash
docker inspect shopsphere-user-service
```

Look for:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

---

# 12.15 RCA Method

Use this incident workflow:

```text
Symptom
   |
   v
Container State
   |
   v
Application Logs
   |
   v
Environment Variables
   |
   v
Network
   |
   v
Port Mapping
   |
   v
Database
   |
   v
Storage
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Validation
```

---

# 13. Rollback Procedure

# 13.1 Application Image Rollback

Suppose:

```text
Current:
shopsphere/user-service:1.1.0

Previous:
shopsphere/user-service:1.0.0
```

Stop and remove the current container:

```bash
docker rm -f shopsphere-user-service
```

Run the previous image:

```bash
docker run ... shopsphere/user-service:1.0.0
```

---

# 13.2 Why Roll Back the Image Instead of Rebuilding?

Production rollback should be fast.

Preferred:

```text
Known-good image
      |
      v
Run previous version
```

Not:

```text
Source
  |
  v
Rebuild
  |
  v
Hope it matches
```

---

# 13.3 Database Rollback

Database rollback is separate.

Example:

```text
Application Version 1.1
      |
      v
Schema Version 2
```

Rolling back only the application may not be sufficient.

Enterprise systems should use controlled database migrations.

---

# 13.4 Volume Protection

Do not remove:

```text
shopsphere-postgres-data
```

unless database reset is intentional.

Reset command:

```bash
docker volume rm shopsphere-postgres-data
```

This permanently deletes the local training database data.

---

# 13.5 Full Local Environment Cleanup

Stop/remove containers:

```bash
docker rm -f shopsphere-user-service
docker rm -f shopsphere-postgres
```

Keep the volume when data should remain.

Remove the volume only when intentionally resetting:

```bash
docker volume rm shopsphere-postgres-data
```

Remove network:

```bash
docker network rm shopsphere-network
```

---

# 14. Production Best Practices

# 14.1 Immutable Tags

Do not rely on:

```text
latest
```

Prefer:

```text
1.0.0
1.0.1
1.1.0
<git-sha>
```

---

# 14.2 Build Once, Promote Many

```text
Source
  |
  v
Build
  |
  v
JAR
  |
  v
Docker Image
  |
  v
Trivy Scan
  |
  v
ECR
  |
  +--> DEV
  |
  +--> UAT
  |
  +--> PROD
```

Same image.

---

# 14.3 Multi-Stage Builds

The current Dockerfile is intentionally simple.

Enterprise Dockerfiles often use multi-stage builds:

```text
Stage 1
Build
 |
 +--> JDK
 +--> Maven
 |
 v
JAR

Stage 2
Runtime
 |
 +--> JRE
 +--> JAR
 |
 v
Final Image
```

Benefits:

```text
Smaller image
Fewer unnecessary packages
Reduced attack surface
Cleaner separation of build/runtime
```

For this ticket, Maven already runs outside the Dockerfile, so the single-stage runtime Dockerfile is appropriate.

---

# 14.4 Run as Non-Root

Production containers should ideally run as a non-root user.

Conceptually:

```dockerfile
USER appuser
```

This reduces risk if the application is compromised.

---

# 14.5 Minimal Base Image

Prefer a runtime image containing only what is needed.

For example:

```text
JRE runtime
```

instead of a full development environment.

---

# 14.6 Vulnerability Scanning

Use Trivy for:

```text
Filesystem
+
Image
```

Typical CI:

```bash
trivy fs .
```

Then:

```bash
trivy image shopsphere/user-service:1.0.0
```

Severity thresholds should be defined by enterprise security policy.

---

# 14.7 Secret Management

Never place production passwords directly into:

```text
Dockerfile
Git
Jenkinsfile
application.yml
```

Preferred:

```text
AWS Secrets Manager
Kubernetes Secrets
External Secrets
Jenkins Credentials
Vault
```

---

# 14.8 Health Checks

Applications should expose operational health.

For Spring Boot, a common enterprise pattern is:

```text
Actuator
   |
   v
/actuator/health
```

Later Kubernetes can use:

```text
livenessProbe
readinessProbe
startupProbe
```

---

# 14.9 Resource Limits

Docker/Kubernetes workloads should have appropriate:

```text
CPU
Memory
```

limits and requests.

Kubernetes is the preferred place in the ShopSphere platform for workload resource policy.

---

# 14.10 Logging

Containers should write application logs to standard output where possible:

```text
stdout
stderr
```

Then the platform collects them.

Future ShopSphere architecture:

```text
Container
   |
   v
stdout/stderr
   |
   v
Log Collector
   |
   v
Centralized Logging
```

---

# 14.11 Observability

Production containers should provide:

```text
Metrics
Logs
Traces
Health
```

Future architecture:

```text
Application
   |
   +--> Metrics
   +--> Logs
   +--> Traces
   |
   v
Observability Platform
```

---

# 14.12 Image Signing / Provenance

Enterprise organizations may use:

```text
Image signing
SBOM
Provenance
Admission policies
```

to ensure only approved images run in production.

---

# 14.13 Registry Policies

ECR should use:

```text
Lifecycle policies
Image scanning
Access control
Encryption
Repository policies
```

---

# 14.14 Database

Local:

```text
PostgreSQL Container
```

AWS production:

```text
Amazon RDS PostgreSQL
```

Do not treat a production database container as automatically equivalent to a managed database service.

---

# 14.15 Container Orchestration

Docker is useful for packaging and local runtime.

For many production microservices:

```text
Docker Image
     |
     v
Kubernetes
     |
     v
Pods
```

---

# 14.16 No `latest` in Production

Bad:

```text
deployment:
  image: shopsphere/user-service:latest
```

Better:

```text
deployment:
  image: <registry>/shopsphere/user-service:1.0.0
```

Best practice is to use immutable references and a promotion model.

---

# 14.17 Separate Build and Runtime Responsibilities

```text
Build
 |
 +--> Maven
 +--> JDK
 +--> Tests
 |
 v
Artifact

Runtime
 |
 +--> JRE
 +--> JAR
 |
 v
Container
```

---

# 14.18 Docker Socket Security

The Jenkins CI environment may need Docker access.

However:

```text
docker.sock
```

is highly privileged.

A production organization should carefully consider:

```text
Docker-in-Docker
Rootless BuildKit
Kaniko
Buildah
Remote builders
Ephemeral CI agents
```

depending on architecture and security requirements.

---

# 15. Interview Questions & Answers

## Q1. What did you implement in ECOM-007?

**Answer**

I containerized the ShopSphere User Service using a Java 17 JRE base image. Maven first creates the Spring Boot JAR, then Docker packages that JAR into `shopsphere/user-service:1.0.0`. I also created a PostgreSQL container, a user-defined Docker network, and a named volume for database persistence, then validated the API end to end.

---

## Q2. What is the difference between a JAR, image and container?

**Answer**

The JAR is the Java application artifact. The Docker image packages that artifact with the runtime definition. The container is the running instance of that image.

---

## Q3. Why did you use a JRE base image?

**Answer**

The application is already built by Maven, so the runtime container only needs Java to execute the JAR. This avoids unnecessary build tooling in the runtime image.

---

## Q4. Why is `localhost` wrong for your database?

**Answer**

Inside a User Service container, `localhost` refers to the User Service container itself. PostgreSQL is running in another container, so the application must use the PostgreSQL container's DNS name.

---

## Q5. How did the User Service connect to PostgreSQL?

**Answer**

Both containers were attached to `shopsphere-network`, and the application used `shopsphere-postgres` as the database host. Docker's internal DNS resolves that name.

---

## Q6. Why didn't you use the PostgreSQL container IP?

**Answer**

Container IP addresses are dynamic. A container name on a user-defined network provides a stable service reference.

---

## Q7. What is `.dockerignore`?

**Answer**

`.dockerignore` controls which files are excluded from the Docker build context. It reduces unnecessary context and prevents unrelated files from being sent into the build.

---

## Q8. Why is the JAR allowed through `.dockerignore`?

**Answer**

Because the Dockerfile contains `COPY target/*.jar app.jar`. The final application artifact must be available during the image build.

---

## Q9. What does `EXPOSE` do?

**Answer**

`EXPOSE` documents the port the application uses inside the container. It does not publish the port to the host. Publishing is done with `-p`.

---

## Q10. What is the difference between `EXPOSE 8081` and `-p 8081:8081`?

**Answer**

`EXPOSE` documents the container port. `-p` maps a host port to a container port.

---

## Q11. Why use a Docker network?

**Answer**

A user-defined network provides container-to-container communication and DNS-based service discovery.

---

## Q12. Why use a Docker volume?

**Answer**

Database data should survive container recreation. The named volume separates persistent database storage from the PostgreSQL container lifecycle.

---

## Q13. What happens if you remove the PostgreSQL container?

**Answer**

The container can be removed while the named volume remains. The database data can then be reused by another PostgreSQL container that mounts the same volume.

---

## Q14. What is the difference between a named volume and a bind mount?

**Answer**

Docker manages the storage location of a named volume, while a bind mount maps an explicit host filesystem path into the container.

---

## Q15. Why use environment variables?

**Answer**

They keep environment-specific configuration outside the application image. The same image can connect to different databases in different environments.

---

## Q16. Should database passwords be stored in `application.yml`?

**Answer**

Real production passwords should not be committed to source control. They should come from a secret-management mechanism.

---

## Q17. What is a Docker image layer?

**Answer**

Docker builds images using layers. When unchanged layers can be reused, Docker can use its build cache to reduce build time and storage duplication.

---

## Q18. What is Docker build context?

**Answer**

The build context is the set of files Docker can access during `docker build`. In our command, `.` represents the current directory.

---

## Q19. How do you troubleshoot a container?

**Answer**

I start with `docker ps -a`, then inspect `docker logs`, environment variables, port mapping, network membership, dependent services and storage.

---

## Q20. How do you troubleshoot database connectivity?

**Answer**

I verify that PostgreSQL is running, both containers share a network, the DB host is `shopsphere-postgres`, port 5432 is correct, credentials are correct, and application logs show no JDBC errors.

---

## Q21. Why shouldn't we use `latest` in production?

**Answer**

`latest` is mutable and weakens traceability. Immutable version or commit-based tags make promotion and rollback predictable.

---

## Q22. What is build once, promote many?

**Answer**

We build and validate one image and then promote the same image through DEV, UAT and PROD instead of rebuilding it for every environment.

---

## Q23. What is the difference between Docker Hub, Nexus and ECR?

**Answer**

Docker Hub and ECR are container registries. Nexus can store build artifacts such as JARs and can also support other repository types. In our intended AWS architecture, Nexus stores Java artifacts and ECR stores container images.

---

## Q24. How does Docker fit into Jenkins?

**Answer**

Jenkins performs the CI workflow, Maven builds the application, Docker packages the resulting JAR into an image, Trivy scans it, and the image is pushed to the container registry.

---

## Q25. What is Trivy used for?

**Answer**

Trivy can scan filesystems, container images and other supported artifacts for security issues. In the ShopSphere CI flow, we use it for filesystem and image security scanning.

---

## Q26. Why scan both filesystem and image?

**Answer**

A filesystem scan can detect vulnerable dependencies, secrets or configuration issues in the source/build context. An image scan checks the final container image, including runtime components.

---

## Q27. Why isn't PostgreSQL packaged into the User Service image?

**Answer**

Application and database have different lifecycles and responsibilities. They should remain separate services so they can be scaled and managed independently.

---

## Q28. How would this architecture change in AWS?

**Answer**

The User Service image would be pushed to ECR, deployed to EKS, and the database would normally move from the local PostgreSQL container to a managed service such as RDS PostgreSQL.

---

## Q29. How would you roll back the application?

**Answer**

Deploy the previous known-good immutable image tag. We should not rebuild the application during a normal image rollback.

---

## Q30. What is the biggest difference between local Docker and EKS?

**Answer**

Docker provides the container runtime and packaging model. Kubernetes adds orchestration features such as scheduling, replicas, service discovery, self-healing and declarative deployment management.

---

## Q31. Why should containers be treated as ephemeral?

**Answer**

Containers should be replaceable. Persistent data should live outside the application container, which improves reliability and simplifies deployment.

---

## Q32. Why is Docker socket access sensitive?

**Answer**

Access to the Docker daemon can provide extensive control over the host. CI systems should therefore restrict Docker access and use hardened build architectures.

---

## Q33. What is a multi-stage Docker build?

**Answer**

It uses one stage for building the application and another smaller stage for runtime. The final image contains only the runtime components and built artifact.

---

## Q34. Why did you not use a multi-stage build here?

**Answer**

Because Maven already builds the JAR outside Docker in the current ShopSphere workflow. The Dockerfile is intentionally focused only on the runtime image.

---

## Q35. How would you improve this Dockerfile for production?

**Answer**

I would evaluate a smaller/hardened runtime image, run as a non-root user, add health-related support, validate the base image lifecycle, scan the image, pin important dependencies appropriately, and add stronger metadata and provenance controls.

---

# 16. Review Questions

## Docker Fundamentals

1. What is Docker?
2. What is a Docker image?
3. What is a Docker container?
4. What is Docker Engine?
5. What is a registry?
6. What is a Dockerfile?
7. What is a build context?
8. What is a Docker image layer?
9. What is Docker build cache?
10. What is the container lifecycle?

---

## Dockerfile

11. What does `FROM` do?
12. What does `WORKDIR` do?
13. What does `COPY` do?
14. What does `EXPOSE` do?
15. What does `ENTRYPOINT` do?
16. Why is the JRE used?
17. Why must the JAR exist before Docker build?
18. What would happen if `.dockerignore` excluded the JAR?

---

## Networking

19. Why use a user-defined network?
20. What is Docker DNS?
21. Why use `shopsphere-postgres` instead of its IP?
22. What does `-p 8081:8081` mean?
23. What is the difference between container and host ports?
24. How do you inspect a Docker network?

---

## Storage

25. Why use a volume for PostgreSQL?
26. What is the difference between a volume and bind mount?
27. What happens to a volume when the container is deleted?
28. When should a database volume be deleted?

---

## Configuration

29. Why use environment variables?
30. What does `${DB_HOST:localhost}` mean?
31. Why should production passwords not be stored in Git?
32. How would you inject secrets into Kubernetes?

---

## Security

33. What is Trivy?
34. Why scan both filesystem and image?
35. Why avoid `latest`?
36. Why run containers as non-root?
37. Why is Docker socket access sensitive?
38. What is an SBOM?
39. What is image signing?

---

## CI/CD

40. Where does Maven fit?
41. Where does Docker fit?
42. Where does Nexus fit?
43. Where does ECR fit?
44. Where does Jenkins fit?
45. What does build once, promote many mean?

---

## AWS

46. How does Docker map to ECR?
47. How does ECR map to EKS?
48. How does PostgreSQL container map to RDS?
49. Why is a managed database preferred for production?
50. What changes when moving from Docker to Kubernetes?

---

# 17. Enterprise Markdown Documentation

## 17.1 Jira Ticket

```text
ECOM-007 — Docker Containerization
```

---

## 17.2 Feature Branch

```text
feature/ECOM-007-docker-containerization
```

---

## 17.3 Implementation Commit

```text
ecbed52 ECOM-007: implement Docker containerization
```

---

## 17.4 Implementation Summary

```text
Jira
  |
  v
Feature Branch
  |
  v
User Service
  |
  +--> Dockerfile
  +--> .dockerignore
  +--> application.yml
  |
  v
Maven Package
  |
  v
JAR
  |
  v
Docker Build
  |
  v
shopsphere/user-service:1.0.0
  |
  +--------------------------+
  |                          |
  v                          v
User Service             PostgreSQL
Container                 Container
  |                          |
  +-------- Network ---------+
             |
             v
      Persistent Volume
```

---

## 17.5 Actual Validation Result

```text
Maven Tests
    |
    +--> 15/15 ✅

Docker Image
    |
    +--> Built ✅

PostgreSQL
    |
    +--> Running ✅

Docker Network
    |
    +--> Created ✅

Docker Volume
    |
    +--> Created ✅

User Service
    |
    +--> Running ✅

API
    |
    +--> GET /api/users ✅
    +--> POST /api/users ✅

Database
    |
    +--> Connectivity ✅
    +--> Persistence ✅
```

---

## 17.6 Enterprise Traceability

```text
Business Requirement
        |
        v
Jira ECOM-007
        |
        v
Git Feature Branch
        |
        v
Source Code
        |
        v
Maven Build
        |
        v
JAR
        |
        v
Docker Image
        |
        v
Security Scan
        |
        v
Container Registry
        |
        v
Kubernetes
        |
        v
Production
```

---

## 17.7 Artifact Flow

```text
Source
  |
  v
Maven
  |
  v
JAR
  |
  v
Nexus
```

and:

```text
JAR
  |
  v
Docker Build
  |
  v
Docker Image
  |
  v
ECR
```

---

## 17.8 Application / Platform Responsibility

```text
Application Team
     |
     +--> Java Code
     +--> Tests
     +--> Dockerfile
     +--> application configuration

DevOps / Platform
     |
     +--> Jenkins
     +--> Security scanning
     +--> Registry
     +--> Kubernetes
     +--> Infrastructure

Security
     |
     +--> Vulnerability policy
     +--> Image scanning
     +--> Secret controls
     +--> Supply-chain controls
```

---

## 17.9 Enterprise Delivery Model

```text
Developer
   |
   v
Git
   |
   v
Jenkins
   |
   +--> Maven
   +--> Tests
   +--> SonarQube
   +--> Quality Gate
   +--> Trivy
   |
   v
JAR
   |
   v
Nexus
   |
   v
Docker Build
   |
   v
Trivy Image Scan
   |
   v
ECR
   |
   v
EKS
```

---

# 18. Git Flow — Enterprise Level

## 18.1 Branching Strategy

```text
main
  |
  v
develop
  |
  +-------------------------------+
  |                               |
  v                               v
feature/ECOM-007-docker-      feature/ECOM-008-
containerization              jenkins-cicd
  |                               |
  v                               v
Implementation                 Implementation
  |                               |
  v                               v
Validation                    Validation
  |                               |
  v                               v
Commit                        Commit
  |                               |
  v                               v
Push                          Push
  |                               |
  +---------------+---------------+
                  |
                  v
               develop
                  |
                  v
          Integration Testing
                  |
                  v
                 main
```

---

## 18.2 Jira-to-Git Workflow

```text
Jira Ticket
    |
    v
develop
    |
    v
feature/ECOM-007-docker-containerization
    |
    v
Docker Implementation
    |
    v
Maven Validation
    |
    v
Docker Build
    |
    v
Runtime Validation
    |
    v
Clean Git Review
    |
    v
Jira Commit
    |
    v
Push
    |
    v
Documentation
    |
    v
Documentation Commit
    |
    v
Push
    |
    v
Merge into develop
```

---

## 18.3 Branch Creation

```bash
git switch develop
git pull origin develop
git switch -c feature/ECOM-007-docker-containerization
```

---

## 18.4 Check Changes

```bash
git status
```

```bash
git diff
```

---

## 18.5 Add Implementation

```bash
git add application/services/user-service
```

---

## 18.6 Commit

```bash
git commit -m "ECOM-007: implement Docker containerization"
```

---

## 18.7 Push

```bash
git push -u origin feature/ECOM-007-docker-containerization
```

---

## 18.8 Documentation Commit

```bash
git add docs/jira/ECOM-007/README.md
```

Then:

```bash
git commit -m "ECOM-007: add Docker documentation"
```

Then:

```bash
git push
```

---

## 18.9 Why Jira Commit IDs Matter

This:

```text
ECOM-007: implement Docker containerization
```

makes the Git history traceable back to the Jira requirement.

Enterprise traceability becomes:

```text
Jira
  |
  v
Commit
  |
  v
Build
  |
  v
Image
  |
  v
Deployment
```

---

# Docker Bible — Practical Command Cheat Sheet

## Build

```bash
mvn clean package
```

```bash
docker build -t shopsphere/user-service:1.0.0 .
```

---

## Images

```bash
docker images
docker image ls
docker image inspect shopsphere/user-service:1.0.0
```

---

## Containers

```bash
docker ps
docker ps -a
docker logs shopsphere-user-service
docker logs -f shopsphere-user-service
docker inspect shopsphere-user-service
```

---

## Network

```bash
docker network ls
docker network inspect shopsphere-network
```

---

## Volume

```bash
docker volume ls
docker volume inspect shopsphere-postgres-data
```

---

## Application

```bash
curl http://localhost:8081/api/users
```

---

## Create User

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Rakesh",
    "lastName": "DevOps",
    "email": "rakesh.devops@example.com",
    "password": "DevOps123"
  }'
```

---

## Cleanup

```bash
docker rm -f shopsphere-user-service
docker rm -f shopsphere-postgres
```

Optional data reset:

```bash
docker volume rm shopsphere-postgres-data
```

Optional network removal:

```bash
docker network rm shopsphere-network
```

---

# Docker Interview Mental Model

Whenever an interviewer asks:

> “Explain your Docker implementation.”

Think:

```text
Source Code
    |
    v
Maven
    |
    v
JAR
    |
    v
Dockerfile
    |
    v
Image
    |
    v
Container
    |
    +------ Network ------+
    |                     |
    v                     v
User Service          PostgreSQL
                            |
                            v
                         Volume
```

Then explain:

```text
JAR
  |
  +--> Application artifact

Image
  |
  +--> Packaged delivery artifact

Container
  |
  +--> Running instance

Network
  |
  +--> Service communication

Volume
  |
  +--> Persistent database storage
```

---

# Production Docker Mental Model

```text
Developer
   |
   v
Git
   |
   v
Jenkins
   |
   +--> Build
   +--> Test
   +--> SonarQube
   +--> Quality Gate
   +--> Trivy
   |
   v
JAR
   |
   v
Docker Build
   |
   v
Image
   |
   v
Trivy Image Scan
   |
   v
ECR
   |
   v
EKS
   |
   v
Pod
   |
   v
Service
   |
   v
Production
```

---

# Final ECOM-007 Checkpoint

```text
ECOM-007 — Docker Containerization
|
+-- User Service Dockerfile          ✅
+-- Java 17 Runtime Image            ✅
+-- .dockerignore                    ✅
+-- Docker-ready application.yml     ✅
+-- Maven Build                      ✅
+-- Maven Tests                      ✅ 15/15
+-- Spring Boot JAR                  ✅
+-- Docker Image                     ✅
+-- PostgreSQL 17 Container          ✅
+-- Docker Network                   ✅
+-- Docker DNS                       ✅
+-- PostgreSQL Named Volume          ✅
+-- User Service Container            ✅
+-- Database Connectivity             ✅
+-- GET /api/users                    ✅
+-- POST /api/users                   ✅
+-- Data Persistence                 ✅
+-- Implementation Commit            ✅
+-- Feature Branch Push              ✅
|
v
Docker Foundation Complete
```

---

# Final Interview Answer

> “In ShopSphere ECOM-007, I containerized the User Service using a Java 17 JRE base image. The application was first built and tested with Maven, which generated the Spring Boot JAR. I then used a Dockerfile to package that JAR into `shopsphere/user-service:1.0.0`.
>
> For runtime validation, I created a user-defined Docker network named `shopsphere-network` and ran PostgreSQL 17 as a separate container. The User Service connected to PostgreSQL using the Docker DNS name `shopsphere-postgres` rather than a hard-coded container IP. PostgreSQL data was persisted using the named volume `shopsphere-postgres-data`.
>
> I also changed the Spring Boot datasource configuration to use environment variables for the database host, port, name, username and password. This allows the same application image to run in different environments without rebuilding it.
>
> I validated the implementation with 15 passing Maven tests and then verified the Docker runtime through the User Service APIs. In the enterprise CI/CD flow, the same image can be scanned with Trivy, pushed to Amazon ECR, and deployed to Amazon EKS, while the database can move from the local PostgreSQL container to Amazon RDS.”

---

# Complete ECOM-007 Enterprise Flow

```text
                         SHOPSPHERE
                            |
                            v
                      Jira ECOM-007
                            |
                            v
               feature/ECOM-007-docker-containerization
                            |
                            v
                      User Service
                            |
                            v
                       Maven Build
                            |
                            v
                           JAR
                            |
                            v
                       Dockerfile
                            |
                            v
                    Docker Image
                  shopsphere/user-service
                            |
               +------------+------------+
               |                         |
               v                         v
       User Service                 PostgreSQL
         Container                   Container
               |                         |
               +---- Docker Network ------+
                             |
                             v
                    Named Docker Volume
                             |
                             v
                       API Validation
                             |
                             v
                         Git Push
                             |
                             v
                     Future Jenkins CI
                             |
                             v
                     Trivy Image Scan
                             |
                             v
                            ECR
                             |
                             v
                            EKS
                             |
                             v
                           PROD
```

# ECOM-007 Status

```text
IMPLEMENTATION : COMPLETE ✅
VALIDATION     : COMPLETE ✅
DOCUMENTATION  : COMPLETE ✅
DOCKER BIBLE   : COMPLETE ✅

Next Platform Stage:
Jenkins CI / ECOM-008
```
