ECOM-007 — Docker Containerization
ShopSphere Enterprise E-Commerce Platform
> **Jira:** ECOM-007  
> **Feature Branch:** `feature/ECOM-007-docker-containerization`  
> **Implementation Commit:** `ecbed52 ECOM-007: implement Docker containerization`  
> **Service:** `user-service`
---
1. Business Requirement
ShopSphere needs a consistent way to package and run its Java microservices across developer laptops, CI, Docker, and later Kubernetes environments.
For ECOM-007, the User Service was containerized and validated with PostgreSQL running in a separate Docker container.
Target flow:
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
Local API Validation
    |
    v
Future ECR
    |
    v
Future EKS
```
---
2. Why Business Needs It
Containerization reduces differences between environments caused by Java/runtime versions, operating-system packages, manual configuration, and network/database setup.
The important artifact distinction is:
```text
JAR
 |
 +--> packaged Java application
 |
v
Docker Image
 |
 +--> JAR + runtime definition
 |
v
Container
 |
 +--> running image instance
```
Docker therefore gives the team a repeatable runtime package that can later be promoted through CI/CD.
---
3. Real-Time Enterprise Scenario
A developer completes a User Service change. The enterprise CI flow can become:
```text
Developer
   |
   v
GitHub
   |
   v
Jenkins
   |
   +--> Maven Compile
   +--> Unit Tests
   +--> SonarQube
   +--> Quality Gate
   +--> Trivy filesystem scan
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
ECOM-007 establishes the Docker packaging and runtime foundation for this later pipeline.
---
4. Architecture Diagram
4.1 Local Docker Architecture
```text
                         Docker Host
                              |
               +--------------+--------------+
               |                             |
               v                             v
   shopsphere-user-service          shopsphere-postgres
               |                             |
               +-------- shopsphere-network-+
                              |
                              v
                     Docker internal DNS
```
The User Service connects to PostgreSQL using:
```text
shopsphere-postgres
```
not a hard-coded container IP.
4.2 Request / Data Flow
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
shopsphere-postgres-data
```
4.3 AWS Mapping
```text
Local Docker                       AWS / Kubernetes
-----------                         -----------------
Docker image          ----------->  ECR image
Container             ----------->  EKS Pod
Container DNS         ----------->  Kubernetes Service DNS
PostgreSQL container  ----------->  RDS / Aurora
Named volume          ----------->  Managed persistent storage
```
The mapping is conceptual; the production architecture is managed differently from local Docker.
---
5. Repository Structure
```text
ShopSphere-E-Commerce-Platform/
├── application/
│   └── services/
│       └── user-service/
│           ├── pom.xml
│           ├── Dockerfile
│           ├── .dockerignore
│           └── src/
│               └── main/
│                   └── resources/
│                       └── application.yml
│
├── docker/
├── kubernetes/
├── helm/
└── docs/
    └── jira/
        └── ECOM-007/
            └── README.md
```
---
6. Files Created / Modified
6.1 `application/services/user-service/Dockerfile`
```dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```
Purpose: defines the User Service runtime image.
6.2 `.dockerignore`
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
The final JAR is intentionally not ignored because the Dockerfile copies `target/*.jar`.
6.3 `application.yml`
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
6.4 PostgreSQL
Official image used:
```text
postgres:17
```
No custom PostgreSQL Dockerfile was required.
6.5 Docker Network
```text
shopsphere-network
```
6.6 Docker Volume
```text
shopsphere-postgres-data
```
mounted at:
```text
/var/lib/postgresql/data
```
---
7. Deep Concept Explanation
Dockerfile
`FROM` selects the Java runtime, `WORKDIR` sets the working directory, `COPY` adds the JAR, `EXPOSE` documents the application port, and `ENTRYPOINT` starts the Spring Boot process.
JAR vs Image vs Container
```text
source code
   |
   v
Maven
   |
   v
user-service-0.0.1-SNAPSHOT.jar
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
Why JRE?
The runtime container only needs Java to execute the application. The full build toolchain is handled outside the runtime image.
Docker Network
Both containers were attached to `shopsphere-network`. Docker's internal DNS resolved `shopsphere-postgres` to the database container.
Docker Volume
The PostgreSQL data directory is stored in a named volume so database data is separate from the container lifecycle.
Environment-driven Configuration
The same image can use different database endpoints:
```text
Local Java process -> localhost
Docker             -> shopsphere-postgres
Kubernetes         -> Kubernetes Service DNS
```
---
8. Every Command Explained
Maven
```bash
mvn clean test
```
Removes previous build output and runs tests.
```bash
mvn clean package
```
Builds and packages the application as a JAR.
Docker image
```bash
docker build -t shopsphere/user-service:1.0.0 .
```
Creates the image, assigns the repository/tag, and uses the current directory as the build context.
Network
```bash
docker network create shopsphere-network
```
Creates the shared user-defined network.
Volume
```bash
docker volume create shopsphere-postgres-data
```
Creates named persistent storage.
PostgreSQL
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
User Service
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
Diagnostics
```bash
docker ps
docker ps -a
docker logs shopsphere-user-service
docker logs -f shopsphere-user-service
docker images
docker network inspect shopsphere-network
docker volume inspect shopsphere-postgres-data
docker port shopsphere-user-service
```
Shell access
```bash
docker exec -it shopsphere-user-service sh
```
Cleanup
```bash
docker rm -f shopsphere-user-service
docker rm -f shopsphere-postgres
```
Remove the database volume only when resetting test data intentionally:
```bash
docker volume rm shopsphere-postgres-data
```
---
9. Every YAML / JSON / Configuration Explained
The main runtime configuration is Spring YAML.
```yaml
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:shopsphere_user}
```
`${VARIABLE:default}` means the environment variable is used when present; otherwise the default value is used.
For Docker:
```text
DB_HOST=shopsphere-postgres
DB_PORT=5432
DB_NAME=shopsphere_user
```
For local execution, the defaults can point to localhost.
Port mappings
```text
-p 8081:8081
```
means host port `8081` maps to container port `8081`.
```text
-p 5432:5432
```
means host port `5432` maps to PostgreSQL container port `5432`.
---
10. README Purpose / Deployment / Validation / Rollback / Usage
Purpose
This README is the Docker reference for ECOM-007 and covers the container image, runtime networking, PostgreSQL storage, configuration, troubleshooting, and enterprise delivery mapping.
Deployment sequence
```text
Maven Package
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
     +---- PostgreSQL container
     |
     v
User Service container
     |
     v
API Validation
```
Validation
```bash
curl http://localhost:8081/api/users
```
Initial response:
```json
[]
```
Test user creation:
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
Observed response:
```json
{"id":1,"firstName":"Rakesh","lastName":"DevOps","email":"rakesh.devops@example.com"}
```
GET validation returned the persisted user.
Rollback
Remove the current application container and run a previous immutable image tag:
```bash
docker rm -f shopsphere-user-service
docker run ... shopsphere/user-service:<previous-tag>
```
Do not delete the PostgreSQL volume unless database reset is intended.
---
11. Validation
Maven
Final User Service validation:
```text
15 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```
Docker
Image successfully built:
```text
shopsphere/user-service:1.0.0
```
PostgreSQL
Container successfully created:
```text
shopsphere-postgres
```
Networking
Both containers communicated over:
```text
shopsphere-network
```
Persistence
PostgreSQL used:
```text
shopsphere-postgres-data
```
API
Validated:
```text
GET /api/users
POST /api/users
```
The POST created a user and GET returned the stored record.
---
12. Troubleshooting & RCA
Issue 1 — PostgreSQL connection failed using localhost
Root Cause: inside a container, `localhost` refers to that same container.
Resolution: use:
```text
DB_HOST=shopsphere-postgres
```
and place both containers on the same Docker network.
Issue 2 — Hard-coded database container IP
Root Cause: container IPs can change.
Resolution: use the Docker DNS/service name:
```text
shopsphere-postgres
```
Issue 3 — Docker cannot find JAR
Root Cause: the JAR was not built before Docker build, or the final JAR was excluded from the build context.
Resolution:
```bash
mvn clean package
docker build -t shopsphere/user-service:1.0.0 .
```
Keep the final JAR available to the Docker build context.
Issue 4 — Database data does not survive container recreation
Root Cause: data was stored only in the container writable layer.
Resolution: use the named volume:
```text
shopsphere-postgres-data
```
Issue 5 — Container starts but API is unavailable
Check:
```bash
docker ps
docker logs shopsphere-user-service
docker port shopsphere-user-service
curl http://localhost:8081/api/users
```
Issue 6 — Containers cannot communicate
Check:
```bash
docker network inspect shopsphere-network
```
Both containers should be members of the same network.
---
13. Rollback Procedure
Application rollback
```bash
docker rm -f shopsphere-user-service
docker run ... shopsphere/user-service:<previous-tag>
```
Image rollback
Use the last known-good immutable image tag:
```text
shopsphere/user-service:0.9.0
```
instead of rebuilding during rollback.
Database rollback
Preserve the volume unless test data must be reset.
```bash
docker volume rm shopsphere-postgres-data
```
is destructive and should be intentional.
Kubernetes rollback concept
```text
Current image
     |
     v
Deployment
     |
     X
Problem
     |
     v
Previous immutable image
     |
     v
Rollback
```
---
14. Production Best Practices
Immutable image tags
Prefer:
```text
1.0.0
1.0.1
<git-sha>
```
rather than relying on mutable `latest`.
Build once, promote many
```text
Source
  |
  v
JAR
  |
  v
Image
  |
  v
ECR
  |
  +--> DEV
  +--> UAT
  +--> PROD
```
Security
Scan both the repository/filesystem and the final image with Trivy.
Runtime image
Use a minimal Java runtime image and keep build tooling outside the runtime image.
Secrets
Do not bake database passwords into images. Use Jenkins Credentials, AWS Secrets Manager, Kubernetes Secrets, or External Secrets according to environment design.
Container user
For production, run the application as a non-root user where practical.
Registry
```text
Nexus -> JAR/WAR artifacts
ECR   -> Docker/OCI images
```
Kubernetes
Later:
```text
ECR
 |
 v
Deployment
 |
 v
Pod
 |
 v
Service
```
---
15. Interview Questions & Answers
Q1. What is Docker?
Answer: Docker packages an application and its runtime dependencies into an image that can run as a container.
Q2. Image vs container?
Answer: An image is the immutable package used to create containers. A container is a running instance of that image.
Q3. JAR vs Docker image?
Answer: The JAR is the Java application artifact. The Docker image contains that artifact plus the runtime definition needed to execute it.
Q4. Why use a JRE base image?
Answer: The runtime only needs Java execution capabilities. The compiler/build toolchain is handled during the build stage or CI environment.
Q5. Why is localhost wrong for PostgreSQL?
Answer: Inside the User Service container, localhost refers to the User Service container itself. PostgreSQL runs in another container.
Q6. How did containers communicate?
Answer: Both containers were attached to a user-defined Docker network and PostgreSQL was resolved by the DNS name `shopsphere-postgres`.
Q7. Why not hard-code the PostgreSQL IP?
Answer: Container IPs are dynamic. Service/container names are more stable for discovery.
Q8. What is `.dockerignore`?
Answer: It controls which files are excluded from the Docker build context and reduces unnecessary data sent to the Docker daemon.
Q9. Why is the final JAR not ignored?
Answer: The Dockerfile copies `target/*.jar`, so the built artifact must remain in the build context.
Q10. Why use a named PostgreSQL volume?
Answer: It separates database storage from the PostgreSQL container lifecycle and allows data to survive container recreation when the volume is retained.
Q11. What does `-p 8081:8081` mean?
Answer: It maps host port 8081 to container port 8081.
Q12. How do you troubleshoot a Docker application?
Answer: I check container status, application logs, port mappings, network membership, environment variables, and application/database connectivity in that order.
Q13. Why avoid `latest` in production?
Answer: `latest` is mutable and weakens traceability. Immutable version or commit-based tags improve rollback and auditability.
Q14. What is the enterprise CI flow?
Answer: Checkout, Maven build/test, quality and security checks, package the JAR, build and scan the image, push to ECR, and deploy through Kubernetes.
---
16. Review Questions
What is the difference between a Dockerfile, image, and container?
Why does Maven run before Docker build?
Why use `eclipse-temurin:17-jre`?
What does `WORKDIR /app` do?
What does `COPY target/*.jar app.jar` do?
What does `EXPOSE 8081` mean?
What does `ENTRYPOINT` do?
Why is `localhost` wrong for PostgreSQL in this design?
Why is the Docker network required?
Why reference PostgreSQL by name?
Why use a named volume?
What happens to container-local data when the container is deleted?
What does `-p 8081:8081` mean?
Why is the final JAR not excluded by `.dockerignore`?
What does Trivy scan?
Why should image tags be immutable?
Where should application secrets be stored?
How does local Docker map to ECR/EKS/RDS?
---
17. Enterprise Markdown Documentation
Jira
```text
ECOM-007 — Docker Containerization
```
Feature branch
```text
feature/ECOM-007-docker-containerization
```
Implementation commit
```text
ecbed52 ECOM-007: implement Docker containerization
```
Implementation flow
```text
Jira
 |
 v
Feature Branch
 |
 v
Dockerfile + .dockerignore + application.yml
 |
 v
Maven Package
 |
 v
Docker Image
 |
 +---- PostgreSQL Container
 |          |
 |          +---- Named Volume
 |
 +---- Docker Network
 |
 v
User Service Container
 |
 v
API Validation
```
Enterprise traceability
```text
Jira
  |
  v
Git Branch
  |
  v
Commit
  |
  v
Jenkins Build
  |
  v
Docker Image
  |
  v
Security Scan
  |
  v
ECR
  |
  v
EKS
```
---
18. Git Flow — Enterprise Level
Branching strategy
```text
main
  |
  v
develop
  |
  +------------------------------+
  |                              |
  v                              v
feature/ECOM-007-            feature/ECOM-008-
docker-containerization      jenkins-cicd
  |                              |
  v                              v
Implementation               Implementation
  |                              |
  v                              v
Validation                    Validation
  |                              |
  v                              v
Commit                        Commit
  |                              |
  v                              v
Push                          Push
  |                              |
  +--------------+---------------+
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
Standard workflow
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
Maven Test
    |
    v
Docker Build
    |
    v
Docker Runtime Validation
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
```
Commands
```bash
git switch develop
git pull origin develop
git switch -c feature/ECOM-007-docker-containerization
```
After implementation:
```bash
git status
git diff
git add application/services/user-service
git commit -m "ECOM-007: implement Docker containerization"
git push -u origin feature/ECOM-007-docker-containerization
```
Documentation:
```bash
git add docs/jira/ECOM-007/README.md
git commit -m "ECOM-007: add Docker documentation"
git push
```
In a real enterprise environment, PR/code-review and CI gates are used at the appropriate integration or release boundary. A separate PR is not required for every training ticket.
---
Final ECOM-007 Checkpoint
```text
ECOM-007 — Docker Containerization
|
+-- Dockerfile                     ✅
+-- .dockerignore                  ✅
+-- Docker-ready application.yml   ✅
+-- Maven validation               ✅ 15/15
+-- Docker image                   ✅
+-- Docker network                 ✅
+-- PostgreSQL container           ✅
+-- PostgreSQL volume              ✅
+-- User Service container         ✅
+-- POST /api/users                 ✅
+-- GET /api/users                  ✅
+-- Git commit pushed               ✅
|
v
Docker foundation complete
```
