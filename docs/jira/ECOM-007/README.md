# ECOM-007 — Docker Containerization

# ShopSphere Enterprise E-Commerce Platform

> **Jira:** ECOM-007
> **Feature Branch:** `feature/ECOM-007-docker-containerization`
> **Implementation Commit:** `ecbed52 ECOM-007: implement Docker containerization`
> **Service:** `user-service`
> **Scope:** Docker image, Docker runtime, PostgreSQL container, network, volume, and Docker-ready application configuration

---

# 1. Business Requirement

ShopSphere microservices must be packaged consistently so that the same application can run across developer, CI, Docker, and later Kubernetes environments.

For ECOM-007, the **User Service** was containerized first.

The implementation introduced:

```text
application/services/user-service/
├── Dockerfile
├── .dockerignore
└── src/main/resources/application.yml
```

A PostgreSQL container was also created for runtime validation.

The target delivery flow is:

```text
Java Source
    |
    v
Maven
    |
    +--> Compile
    +--> Unit Test
    +--> Package
    |
    v
Spring Boot JAR
    |
    v
Docker Build
    |
    v
Docker Image
    |
    v
Docker Container
    |
    +--> PostgreSQL Container
    |
    v
Runtime Validation
    |
    v
Future Amazon ECR
    |
    v
Future Amazon EKS
```

---

# 2. Why Business Needs It

Without containerization, application behavior can differ between environments because of:

* Java/runtime differences
* Operating-system differences
* Missing dependencies
* Manual server configuration
* Database connection differences
* Inconsistent environment setup

Docker gives ShopSphere a consistent application runtime.

The important enterprise distinction is:

```text
JAR
 |
 +--> Application Artifact
 |
 v
Docker Image
 |
 +--> Containerized Delivery Artifact
 |
 v
Container
 |
 +--> Running Application Instance
```

Docker therefore provides a repeatable packaging and deployment model.

---

# 3. Real-Time Enterprise Scenario

Assume the ShopSphere User Service has completed development.

The enterprise CI/CD flow can later be:

```text
Developer
    |
    v
GitHub
    |
    v
Jenkins
    |
    +--> Checkout
    |
    +--> Maven Compile
    |
    +--> Unit Tests
    |
    +--> SonarQube
    |
    +--> Quality Gate
    |
    +--> Trivy Filesystem Scan
    |
    +--> Maven Package
    |
    v
User Service JAR
    |
    v
Docker Build
    |
    v
Docker Image
    |
    v
Trivy Image Scan
    |
    v
Amazon ECR
    |
    v
Amazon EKS
    |
    v
Kubernetes Pod
```

ECOM-007 establishes the Docker foundation required by that future pipeline.

---

# 4. Architecture Diagram

## 4.1 Local Docker Architecture

```text
                           Docker Host
                                |
               +----------------+----------------+
               |                                 |
               v                                 v
   shopsphere-user-service              shopsphere-postgres
               |                                 |
               |                                 |
               +-------- shopsphere-network -----+
                              |
                              v
                    Docker Internal DNS
                              |
                              v
                    shopsphere-postgres
```

The User Service communicates with PostgreSQL through:

```text
shopsphere-postgres
```

instead of a hard-coded container IP.

---

## 4.2 Application Request Flow

```text
Client
   |
   | HTTP :8081
   v
User Service Container
   |
   | JDBC :5432
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
target/user-service-0.0.1-SNAPSHOT.jar
    |
    v
docker build
    |
    v
shopsphere/user-service:1.0.0
    |
    v
shopsphere-user-service
```

---

## 4.4 Future AWS Mapping

```text
Local Docker
----------------
Docker Image
Docker Network
Docker Container
Docker Volume
PostgreSQL Container

          |
          v

AWS / Kubernetes
----------------
ECR
EKS
Kubernetes Service
Kubernetes Secret
RDS PostgreSQL / Aurora
EBS / managed storage
```

The local Docker design is a learning/runtime foundation. It is not intended to be copied literally into production.

---

# 5. Repository Structure

The relevant ShopSphere structure is:

```text
ShopSphere-E-Commerce-Platform/
│
├── application/
│   └── services/
│       └── user-service/
│           ├── pom.xml
│           ├── Dockerfile
│           ├── .dockerignore
│           └── src/
│               └── main/
│                   ├── java/
│                   └── resources/
│                       └── application.yml
│
├── docker/
│
├── kubernetes/
│
├── helm/
│
└── docs/
    └── jira/
        └── ECOM-007/
            └── README.md
```

---

# 6. Files Created / Modified

## 6.1 Dockerfile

**Path**

```text
application/services/user-service/Dockerfile
```

Current implementation:

```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Purpose

This Dockerfile defines the runtime image for the ShopSphere User Service.

---

## 6.2 `.dockerignore`

**Path**

```text
application/services/user-service/.dockerignore
```

Current contents:

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

### Important

The final JAR is intentionally not excluded.

The Dockerfile needs:

```text
target/*.jar
```

to be available during `docker build`.

---

## 6.3 `application.yml`

**Path**

```text
application/services/user-service/src/main/resources/application.yml
```

Docker-ready configuration:

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

### Why this configuration matters

The original Docker execution exposed an important container concept:

```text
localhost inside container
        !=
PostgreSQL container
```

Therefore the database host became environment-driven.

---

## 6.4 PostgreSQL Container

The database used:

```text
postgres:17
```

The official PostgreSQL image was used.

No custom PostgreSQL Dockerfile was required.

---

## 6.5 Docker Network

Created:

```text
shopsphere-network
```

Purpose:

```text
User Service
      |
      v
shopsphere-network
      |
      v
PostgreSQL
```

This provides container-to-container communication and Docker DNS resolution.

---

## 6.6 Docker Volume

Created:

```text
shopsphere-postgres-data
```

Mounted into:

```text
/var/lib/postgresql/data
```

Purpose:

Separate PostgreSQL data storage from the container lifecycle.

---

# 7. Deep Concept Explanation

## 7.1 JAR vs Docker Image vs Docker Container

These are different objects.

### JAR

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

The JAR is the packaged Spring Boot application.

### Docker Image

```text
shopsphere/user-service:1.0.0
```

The image packages the application artifact with its runtime instructions.

### Docker Container

```text
shopsphere-user-service
```

The container is a running instance of the image.

Full flow:

```text
Source Code
    |
    v
Maven Build
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
Container
```

---

## 7.2 Why Use a JRE Runtime Image?

The application has already been compiled by Maven.

The runtime container only needs a Java runtime.

Therefore:

```dockerfile
FROM eclipse-temurin:17-jre
```

is appropriate for this learning implementation.

The full JDK/build toolchain is not required inside the runtime container.

---

## 7.3 Dockerfile Explanation

### `FROM`

```dockerfile
FROM eclipse-temurin:17-jre
```

Defines the base image.

It gives the container a Java 17 runtime.

---

### `WORKDIR`

```dockerfile
WORKDIR /app
```

Sets the container working directory.

The application will operate from:

```text
/app
```

---

### `COPY`

```dockerfile
COPY target/*.jar app.jar
```

Copies the Maven-built JAR into the image.

This means:

```text
Maven package
      |
      v
target/*.jar
      |
      v
Docker build
```

Maven must therefore run before the Docker build.

---

### `EXPOSE`

```dockerfile
EXPOSE 8081
```

Documents the application port.

It does not itself publish the port to the Docker host.

Port publishing is done with:

```bash
-p 8081:8081
```

---

### `ENTRYPOINT`

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Defines the process started when the container launches.

---

# 7.4 Docker Network

A user-defined network was created:

```text
shopsphere-network
```

Both containers were connected to it.

```text
shopsphere-user-service
          |
          v
shopsphere-network
          |
          v
shopsphere-postgres
```

Docker internal DNS allows:

```text
shopsphere-postgres
```

to resolve to the PostgreSQL container.

---

# 7.5 Why Container IP Should Not Be Hard-Coded

A container IP can change when a container is recreated.

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

Container name / DNS
    |
    +--> Stable service reference
```

---

# 7.6 Docker Volume

PostgreSQL was started with:

```text
-v shopsphere-postgres-data:/var/lib/postgresql/data
```

The storage flow is:

```text
PostgreSQL
    |
    v
/var/lib/postgresql/data
    |
    v
shopsphere-postgres-data
```

This separates database storage from the PostgreSQL container.

---

# 7.7 Why PostgreSQL Needs Persistent Storage

Without a volume, database data can be coupled to the container lifecycle.

With a named volume:

```text
PostgreSQL Container
       |
       v
Named Volume
       |
       v
Container recreated
       |
       v
Volume can remain
```

The volume still needs proper backup and protection in a real production environment.

---

# 7.8 Environment Variables

The application uses:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

Docker runtime values were:

```text
DB_HOST=shopsphere-postgres
DB_PORT=5432
DB_NAME=shopsphere_user
DB_USERNAME=shopsphere
DB_PASSWORD=shopsphere
```

This allows the same image to run in different environments without rebuilding it.

---

# 7.9 Port Mapping

Application:

```text
-p 8081:8081
```

means:

```text
Host port 8081
      |
      v
Container port 8081
```

PostgreSQL:

```text
-p 5432:5432
```

means:

```text
Host port 5432
      |
      v
PostgreSQL container port 5432
```

---

# 8. Every Command Explained

## 8.1 Maven Test

```bash
mvn clean test
```

Purpose:

```text
clean
 |
 +--> Remove previous build output

test
 |
 +--> Compile + run unit tests
```

---

## 8.2 Maven Package

```bash
mvn clean package
```

Purpose:

Create the application artifact.

Expected artifact:

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

---

## 8.3 Docker Build

```bash
docker build -t shopsphere/user-service:1.0.0 .
```

Explanation:

```text
docker build
    |
    +--> Build image

-t
    |
    +--> Assign repository/tag

shopsphere/user-service
    |
    +--> Image name

:1.0.0
    |
    +--> Image tag

.
    |
    +--> Current directory as build context
```

---

## 8.4 Create Docker Network

```bash
docker network create shopsphere-network
```

Creates a user-defined bridge network.

---

## 8.5 Create Docker Volume

```bash
docker volume create shopsphere-postgres-data
```

Creates named persistent storage.

---

## 8.6 Run PostgreSQL

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

### `-d`

Detached mode.

### `--name`

Container name and DNS name.

### `--network`

Connect the container to the shared Docker network.

### `-e`

Set environment variables.

### `-v`

Attach the named volume.

### `-p`

Publish PostgreSQL to the Docker host.

### `postgres:17`

Official PostgreSQL image.

---

## 8.7 Run User Service

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

The critical setting is:

```text
DB_HOST=shopsphere-postgres
```

---

## 8.8 List Running Containers

```bash
docker ps
```

Use this first when checking whether the containers are running.

---

## 8.9 List All Containers

```bash
docker ps -a
```

Shows running and stopped containers.

---

## 8.10 Check Images

```bash
docker images
```

Shows locally available images.

---

## 8.11 View Application Logs

```bash
docker logs shopsphere-user-service
```

Useful for application startup errors and database connectivity issues.

---

## 8.12 Follow Logs

```bash
docker logs -f shopsphere-user-service
```

Streams live logs.

---

## 8.13 Inspect Network

```bash
docker network inspect shopsphere-network
```

Useful for verifying whether both containers are connected.

---

## 8.14 Inspect Volume

```bash
docker volume inspect shopsphere-postgres-data
```

Shows volume metadata and mount details.

---

## 8.15 Inspect Container

```bash
docker inspect shopsphere-user-service
```

Useful for:

* environment variables
* network configuration
* mounts
* container state
* port mappings

---

## 8.16 Execute Shell in Container

```bash
docker exec -it shopsphere-user-service sh
```

Opens an interactive shell when the image contains a compatible shell.

---

## 8.17 Stop User Service

```bash
docker stop shopsphere-user-service
```

---

## 8.18 Remove User Service

```bash
docker rm shopsphere-user-service
```

Force removal:

```bash
docker rm -f shopsphere-user-service
```

---

# 9. Every YAML / JSON / Configuration Explained

ECOM-007 mainly uses:

```text
Dockerfile
YAML
Docker CLI configuration
Environment variables
```

---

## 9.1 `application.yml`

```yaml
spring:
  application:
    name: user-service
```

Defines the Spring application name.

---

### Database URL

```yaml
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:shopsphere_user}
```

This uses Spring placeholder syntax:

```text
${VARIABLE:default}
```

Meaning:

```text
Use VARIABLE if provided
otherwise use default
```

Example:

```text
DB_HOST=shopsphere-postgres
```

results in:

```text
jdbc:postgresql://shopsphere-postgres:5432/shopsphere_user
```

---

### Database Username

```yaml
username: ${DB_USERNAME:shopsphere}
```

Uses the runtime variable if provided.

---

### Database Password

```yaml
password: ${DB_PASSWORD:shopsphere}
```

Uses the runtime variable if provided.

Production systems should not keep real credentials in source-control defaults.

---

### Hibernate DDL

```yaml
ddl-auto: update
```

Used for this development/training environment.

Production environments should use controlled database migration practices rather than relying on automatic schema updates.

---

### SQL Logging

```yaml
show-sql: false
```

Avoids verbose SQL output.

---

### SQL Formatting

```yaml
format_sql: true
```

Formats Hibernate SQL when SQL output is generated.

---

### Server Port

```yaml
server:
  port: 8081
```

The User Service listens on port 8081.

---

# 10. README Purpose / Deployment / Validation / Rollback / Usage

## 10.1 README Purpose

This README documents:

* Docker architecture
* Dockerfile design
* JAR-to-image workflow
* Docker network
* PostgreSQL container
* PostgreSQL volume
* application configuration
* commands
* validation
* troubleshooting
* rollback
* enterprise CI/CD mapping
* interview preparation

---

## 10.2 Deployment Sequence

```text
1. Maven build
       |
       v
2. JAR generated
       |
       v
3. Docker network
       |
       v
4. PostgreSQL container
       |
       v
5. Docker image
       |
       v
6. User Service container
       |
       v
7. API validation
```

Commands:

```bash
mvn clean package
```

```bash
docker network create shopsphere-network
```

```bash
docker volume create shopsphere-postgres-data
```

Then run PostgreSQL and the User Service using the documented commands.

---

## 10.3 Validation

Check containers:

```bash
docker ps
```

Check logs:

```bash
docker logs shopsphere-user-service
```

Check API:

```bash
curl http://localhost:8081/api/users
```

Initial response:

```json
[]
```

---

## 10.4 Create a User

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

## 10.5 GET Users

```bash
curl http://localhost:8081/api/users
```

Expected result includes the created user.

This confirmed:

```text
Client
  |
  v
User Service Container
  |
  v
PostgreSQL Container
  |
  v
Persistent Volume
```

---

## 10.6 Rollback

Application rollback:

```bash
docker rm -f shopsphere-user-service
```

Then run a previous image:

```bash
docker run ... shopsphere/user-service:<previous-tag>
```

Do not delete the database volume unless intentionally resetting test data.

---

# 11. Validation

## 11.1 Maven

The User Service completed:

```text
15 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

---

## 11.2 Docker

Successfully built:

```text
shopsphere/user-service:1.0.0
```

---

## 11.3 PostgreSQL

Successfully started:

```text
shopsphere-postgres
```

---

## 11.4 Network

Successfully created:

```text
shopsphere-network
```

---

## 11.5 Volume

Successfully created:

```text
shopsphere-postgres-data
```

---

## 11.6 User Service

Successfully started:

```text
shopsphere-user-service
```

---

## 11.7 API

Successfully validated:

```text
GET /api/users
POST /api/users
```

The POST created a database-backed User Service record.

GET then returned the persisted record.

---

# 12. Troubleshooting & RCA

## Issue 1 — User Service could not connect to PostgreSQL

### Symptom

Application container failed to connect to the database.

### Root Cause

The initial application configuration used:

```text
localhost
```

Inside the User Service container, `localhost` means the User Service container itself.

PostgreSQL was running in a different container.

### Resolution

Use:

```text
DB_HOST=shopsphere-postgres
```

and attach both containers to:

```text
shopsphere-network
```

---

## Issue 2 — Database container IP confusion

### Symptom

The PostgreSQL container had an internal address such as:

```text
172.18.0.2
```

### Root Cause

Docker container IP addresses are dynamic.

### Resolution

Use:

```text
shopsphere-postgres
```

through Docker DNS.

---

## Issue 3 — Docker build could not find JAR

### Symptom

Docker failed at:

```dockerfile
COPY target/*.jar app.jar
```

### Root Cause

The Maven JAR did not exist before the Docker build.

### Resolution

Run:

```bash
mvn clean package
```

before:

```bash
docker build ...
```

---

## Issue 4 — `.dockerignore` could exclude the JAR

### Symptom

Docker could not find `target/*.jar`.

### Root Cause

The final artifact may have been excluded from the build context.

### Resolution

Exclude unnecessary Maven build internals, but keep:

```text
target/*.jar
```

available.

---

## Issue 5 — PostgreSQL data persistence confusion

### Symptom

Concern about data surviving container replacement.

### Root Cause

Container writable storage is not the desired persistence layer for database data.

### Resolution

Use:

```text
shopsphere-postgres-data
```

as a named volume.

---

## Issue 6 — API unavailable

### Troubleshooting sequence

First:

```bash
docker ps
```

Then:

```bash
docker logs shopsphere-user-service
```

Then:

```bash
docker port shopsphere-user-service
```

Then:

```bash
curl http://localhost:8081/api/users
```

If database connectivity is suspected:

```bash
docker network inspect shopsphere-network
```

---

## Issue 7 — Containers are not on the same network

Check:

```bash
docker network inspect shopsphere-network
```

Both:

```text
shopsphere-user-service
shopsphere-postgres
```

should be attached to the network.

---

# 13. Rollback Procedure

## 13.1 Application Rollback

Remove the current container:

```bash
docker rm -f shopsphere-user-service
```

Run a known-good previous image:

```bash
docker run ... shopsphere/user-service:<previous-tag>
```

---

## 13.2 Image Rollback

Example:

```text
shopsphere/user-service:1.0.0
shopsphere/user-service:0.9.0
```

Rollback means:

```text
Current image
     |
     X
Previous known-good image
     |
     v
Container
```

The application should not need to be rebuilt during a normal image rollback.

---

## 13.3 Database Rollback

Do not remove:

```text
shopsphere-postgres-data
```

unless resetting the database is intentional.

For a complete training reset:

```bash
docker rm -f shopsphere-postgres
docker volume rm shopsphere-postgres-data
```

This permanently removes the local database data stored in the volume.

---

# 14. Production Best Practices

## 14.1 Immutable Image Tags

Avoid production deployment based on:

```text
latest
```

Use immutable tags such as:

```text
1.0.0
1.0.1
1.1.0
<git-sha>
```

---

## 14.2 Build Once, Promote Many

Recommended enterprise flow:

```text
Source
  |
  v
JAR
  |
  v
Docker Image
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

The same image should be promoted between environments.

---

## 14.3 Image Security

Use Trivy to scan:

```text
Filesystem
+
Docker Image
```

Recommended CI flow:

```text
Build
  |
  v
Test
  |
  v
Trivy
  |
  v
Docker Build
  |
  v
Trivy Image Scan
  |
  v
ECR
```

---

## 14.4 Configuration

Never place real production passwords in:

```text
Dockerfile
application.yml
Git
Jenkinsfile
```

Use:

```text
AWS Secrets Manager
Kubernetes Secrets
External Secrets
Jenkins Credentials
```

depending on the architecture.

---

## 14.5 Runtime Security

For production:

* Prefer non-root containers.
* Use minimal runtime images.
* Keep base images patched.
* Scan images continuously.
* Avoid unnecessary Linux packages.
* Use immutable image tags.
* Apply resource limits in Kubernetes.

---

## 14.6 Registry Strategy

For ShopSphere:

```text
Nexus
 |
 +--> JAR / WAR artifacts

Amazon ECR
 |
 +--> Docker / OCI images
```

---

## 14.7 Kubernetes Mapping

Later:

```text
Docker Image
     |
     v
ECR
     |
     v
Kubernetes Deployment
     |
     v
Pod
     |
     v
Kubernetes Service
```

---

## 14.8 Database Architecture

Local development:

```text
PostgreSQL Container
```

AWS production:

```text
RDS PostgreSQL
```

The application image does not need to change because database connection details can come from environment-specific configuration.

---

# 15. Interview Questions & Answers

## Q1. What is Docker?

**Answer**

Docker is a containerization platform used to package applications and their runtime dependencies into images that can run consistently across environments.

---

## Q2. What is the difference between an image and a container?

**Answer**

A Docker image is an immutable package used to create containers. A container is a running instance of that image.

---

## Q3. What is the difference between a JAR and a Docker image?

**Answer**

The JAR is the packaged Java application artifact. The Docker image packages that JAR with the runtime instructions required to execute it.

---

## Q4. Why did you use a JRE image?

**Answer**

The application is already compiled by Maven. The runtime container only needs Java to execute the application, so a JRE image avoids unnecessary build tooling in the runtime environment.

---

## Q5. Why did you use `localhost` initially and why did it fail?

**Answer**

`localhost` inside a container refers to that container. PostgreSQL was running in another container, so the User Service could not find the database there.

---

## Q6. How did you solve the database connectivity issue?

**Answer**

I put both containers on a user-defined Docker network and changed the database host to the Docker DNS name `shopsphere-postgres`.

---

## Q7. Why not use the PostgreSQL container IP?

**Answer**

Container IP addresses are dynamic. Docker service/container names provide a more stable DNS-based service reference.

---

## Q8. What is the purpose of `.dockerignore`?

**Answer**

It controls which files are excluded from the Docker build context. It reduces unnecessary content and prevents unwanted source/build files from being sent into the build context.

---

## Q9. Why did you not exclude the final JAR?

**Answer**

Because the Dockerfile copies `target/*.jar` into the image. The artifact must therefore remain available in the build context.

---

## Q10. Why do you need a Docker network?

**Answer**

It allows containers to communicate with each other using predictable service names and Docker DNS.

---

## Q11. Why do you need a Docker volume for PostgreSQL?

**Answer**

The volume separates database storage from the PostgreSQL container lifecycle so the data can remain when the container is recreated.

---

## Q12. What does `-p 8081:8081` mean?

**Answer**

It maps host port 8081 to container port 8081.

---

## Q13. What does `EXPOSE 8081` do?

**Answer**

It documents that the application uses port 8081 inside the image. It does not publish the port externally.

---

## Q14. What does `ENTRYPOINT` do?

**Answer**

It defines the default process executed when the container starts.

---

## Q15. How do you troubleshoot a container that keeps stopping?

**Answer**

I first check `docker ps -a`, then `docker logs`, then inspect the image command, environment variables, ports, network, and dependent services.

---

## Q16. How do you troubleshoot database connectivity?

**Answer**

I verify that both containers are running, confirm they share the same network, check the DB host and port, inspect application logs, and validate PostgreSQL availability.

---

## Q17. Why should production avoid `latest`?

**Answer**

`latest` is mutable and weakens deployment traceability. Immutable version or commit-based tags provide predictable promotion and rollback.

---

## Q18. What is the Docker enterprise CI flow?

**Answer**

Checkout the source, run Maven tests, package the JAR, build the image, scan the image, push it to ECR, and deploy the same image to Kubernetes environments.

---

## Q19. Where does Nexus fit?

**Answer**

Nexus can store Java artifacts such as JAR or WAR files. ECR is used for Docker/OCI container images.

---

## Q20. How does local Docker map to Kubernetes?

**Answer**

The Docker image is pushed to ECR and Kubernetes pulls that image into Pods managed by Deployments and exposed through Kubernetes Services.

---

# 16. Review Questions

1. What is Docker?
2. What is the difference between an image and a container?
3. What is the difference between a JAR and an image?
4. Why was `eclipse-temurin:17-jre` selected?
5. What does `WORKDIR /app` do?
6. What does `COPY target/*.jar app.jar` do?
7. Why must Maven package run before Docker build?
8. What does `EXPOSE 8081` mean?
9. What does `ENTRYPOINT` do?
10. What is `.dockerignore`?
11. Why is the final JAR not excluded?
12. Why is `localhost` wrong for a database in another container?
13. Why is a user-defined Docker network required?
14. Why should the PostgreSQL container be referenced by name?
15. Why should a database use persistent storage?
16. What is the purpose of `shopsphere-postgres-data`?
17. What does `-p 8081:8081` do?
18. What should you check first when a container stops?
19. How would you troubleshoot a database connection issue?
20. What should be scanned by Trivy?
21. Why are immutable image tags important?
22. Where should secrets be stored?
23. Why is Nexus different from ECR?
24. How does Docker map to Kubernetes?
25. How would you roll back a Docker deployment?

---

# 17. Enterprise Markdown Documentation

## Jira Ticket

```text
ECOM-007 — Docker Containerization
```

## Feature Branch

```text
feature/ECOM-007-docker-containerization
```

## Implementation Commit

```text
ecbed52 ECOM-007: implement Docker containerization
```

## Implementation Summary

```text
Jira
  |
  v
Feature Branch
  |
  v
User Service Dockerfile
  |
  v
.dockerignore
  |
  v
Docker-ready application.yml
  |
  v
Maven Package
  |
  v
Spring Boot JAR
  |
  v
Docker Image
  |
  +----------------------+
  |                      |
  v                      v
User Service         PostgreSQL
Container             Container
  |                      |
  +---- Docker Network --+
             |
             v
      Named PostgreSQL Volume
             |
             v
        Runtime Validation
```

---

## Validation Evidence

```text
Maven Tests                  ✅ 15/15
Dockerfile                   ✅
.dockerignore                ✅
Docker Image                 ✅
PostgreSQL Container         ✅
Docker Network               ✅
Docker Volume                ✅
User Service Container       ✅
GET /api/users               ✅
POST /api/users              ✅
Git Commit                   ✅
Feature Branch Push          ✅
```

---

## Enterprise Traceability

```text
Jira Ticket
     |
     v
Git Feature Branch
     |
     v
Commit
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
Registry
     |
     v
Deployment
```

---

## Application Artifact vs Delivery Artifact

```text
Spring Boot JAR
       |
       +--> Java Application Artifact
       |
       v
Docker Image
       |
       +--> Container Delivery Artifact
       |
       v
Container
       |
       +--> Runtime Instance
```

---

## AWS Enterprise Mapping

```text
Development
-----------
Docker
  |
  v
Docker Image
  |
  v
ECR
  |
  v
EKS
  |
  v
Pod


Database
--------
Local PostgreSQL Container
          |
          v
Production RDS PostgreSQL
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
  +--------------------------+
  |                          |
  v                          v
feature/ECOM-007-       feature/ECOM-008-
docker-containerization jenkins-cicd
  |                          |
  v                          v
Implementation            Implementation
  |                          |
  v                          v
Validation                 Validation
  |                          |
  v                          v
Commit                     Commit
  |                          |
  v                          v
Push                       Push
  |                          |
  +------------+-------------+
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

## 18.2 Standard Enterprise Ticket Workflow

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
Implementation
    |
    v
File-by-file Validation
    |
    v
Maven Tests
    |
    v
Docker Build
    |
    v
Docker Runtime Validation
    |
    v
Clean Git Review
    |
    v
Jira Commit
    |
    v
Push Feature Branch
    |
    v
Documentation
    |
    v
Documentation Commit
    |
    v
Merge into develop
    |
    v
Integration Validation
```

---

## 18.3 Git Commands

Create the feature branch:

```bash
git switch develop
git pull origin develop
git switch -c feature/ECOM-007-docker-containerization
```

Check the work:

```bash
git status
git diff
```

Add implementation:

```bash
git add application/services/user-service
```

Commit:

```bash
git commit -m "ECOM-007: implement Docker containerization"
```

Push:

```bash
git push -u origin feature/ECOM-007-docker-containerization
```

Add documentation:

```bash
git add docs/jira/ECOM-007/README.md
```

Documentation commit:

```bash
git commit -m "ECOM-007: add Docker documentation"
```

Push:

```bash
git push
```

---

# Final ECOM-007 Checkpoint

```text
ECOM-007 — Docker Containerization
|
+-- User Service Dockerfile          ✅
+-- .dockerignore                    ✅
+-- Docker-ready application.yml     ✅
+-- Maven validation                 ✅ 15/15
+-- Spring Boot JAR                  ✅
+-- Docker image                     ✅
+-- Docker network                   ✅
+-- PostgreSQL container             ✅
+-- PostgreSQL named volume          ✅
+-- User Service container           ✅
+-- Database connectivity             ✅
+-- GET /api/users                    ✅
+-- POST /api/users                   ✅
+-- Git commit                        ✅
+-- Feature branch pushed             ✅
|
v
Docker foundation complete
```

---

# Complete ECOM-007 Enterprise Flow

```text
                         SHOPSPHERE ECOM-007
                                 |
                                 v
                         Jira Requirement
                                 |
                                 v
                         Git Feature Branch
                                 |
                                 v
                         User Service Code
                                 |
                                 v
                           Maven Build
                                 |
                                 v
                                JAR
                                 |
                                 v
                         Dockerfile Build
                                 |
                                 v
                         Docker Image
                                 |
                +----------------+----------------+
                |                                 |
                v                                 v
       User Service Container              PostgreSQL Container
                |                                 |
                +-------- shopsphere-network -----+
                                  |
                                  v
                       PostgreSQL Named Volume
                                  |
                                  v
                            API Validation
                                  |
                                  v
                           Git Commit / Push
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
```

## Final Interview Statement

> “For ECOM-007, I containerized the ShopSphere User Service using a Java 17 JRE base image. Maven first produced the Spring Boot JAR, then Docker packaged that artifact into an immutable application image. I also configured the application to use environment-driven PostgreSQL connection details because `localhost` inside a container refers to that container itself. For local validation, I ran PostgreSQL and the User Service on a user-defined Docker network and persisted PostgreSQL data using a named Docker volume. I validated the container, database connectivity, and User Service APIs. In the enterprise CI/CD flow, the image would then be scanned with Trivy, pushed to ECR, and deployed to EKS.”
