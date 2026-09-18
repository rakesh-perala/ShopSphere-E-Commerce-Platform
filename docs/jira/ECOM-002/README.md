# ECOM-002 — ShopSphere User Service

## 1. Business Requirement

### Jira Ticket

**ECOM-002 — Implement User Service**

### Objective

Implement the foundation of the ShopSphere User Service as an independent Spring Boot microservice.

The service must provide:

* User creation
* User retrieval
* User retrieval by ID
* User retrieval by email
* User deletion
* Request validation
* PostgreSQL/JPA database integration
* Centralized validation error handling
* Unit tests
* Controller/API tests
* Externalized database configuration

### Business Requirement

ShopSphere requires a dedicated service responsible for managing customer/user information.

The User Service must be independently deployable and must expose REST APIs that other ShopSphere components can consume.

---

# 2. Why the Business Needs It

In an enterprise e-commerce platform, user functionality should not be tightly coupled with Product, Cart, Order, or Payment functionality.

A dedicated User Service provides:

* Independent ownership of user data
* Independent deployment
* Independent scaling
* Clear API boundaries
* Better maintainability
* Easier CI/CD
* Service-level monitoring
* Future authentication/authorization integration

Instead of building one large application:

```text
ShopSphere Monolith
       |
       ├── Users
       ├── Products
       ├── Cart
       ├── Orders
       └── Payments
```

ShopSphere follows a microservice model:

```text
                    ShopSphere
                         |
        +----------------+----------------+
        |        |        |        |       |
        ▼        ▼        ▼        ▼       ▼
      User    Product    Cart     Order  Payment
    Service   Service   Service  Service Service
```

ECOM-002 establishes the first application-level microservice foundation.

---

# 3. Real-Time Enterprise Scenario

Imagine a customer registering on ShopSphere.

The request could look like:

```http
POST /api/users
```

with:

```json
{
  "firstName": "Rakesh",
  "lastName": "Perala",
  "email": "rakesh@example.com",
  "password": "Password123"
}
```

The request flows through:

```text
Client
  |
  | HTTP POST
  ▼
UserController
  |
  | validated request
  ▼
UserService
  |
  | business operation
  ▼
UserRepository
  |
  | JPA
  ▼
PostgreSQL / Amazon RDS
```

The service returns a response containing user information without returning the password.

---

# 4. Architecture Diagram

## Application Architecture

```text
                  Client / Frontend
                         |
                         | HTTP/REST
                         ▼
              +-----------------------+
              |    UserController     |
              |   /api/users          |
              +-----------+-----------+
                          |
                          ▼
              +-----------------------+
              |      UserService      |
              |   Business Logic      |
              +-----------+-----------+
                          |
                          ▼
              +-----------------------+
              |    UserRepository     |
              | Spring Data JPA       |
              +-----------+-----------+
                          |
                          ▼
              +-----------------------+
              |       User Entity     |
              |       users table     |
              +-----------+-----------+
                          |
                          ▼
                 PostgreSQL / RDS
```

## Request Processing

```text
HTTP Request
     |
     ▼
@RequestBody
     |
     ▼
@Valid
     |
     ▼
UserCreateRequest
     |
     ▼
UserController
     |
     ▼
UserService
     |
     ▼
UserRepository
     |
     ▼
Database
     |
     ▼
UserResponse
     |
     ▼
HTTP Response
```

---

# 5. Repository Structure

Current ECOM-002 implementation:

```text
ShopSphere-E-Commerce-Platform/
│
├── README.md
│
├── docs/
│   ├── architecture.md
│   ├── business-requirements.md
│   ├── database-design.md
│   ├── microservice-design.md
│   └── repository-structure.md
│
└── application/
    └── services/
        └── user-service/
            │
            ├── .gitignore
            ├── pom.xml
            │
            └── src/
                ├── main/
                │   ├── java/
                │   │   └── com/
                │   │       └── shopsphere/
                │   │           └── userservice/
                │   │               │
                │   │               ├── UserServiceApplication.java
                │   │               │
                │   │               ├── controller/
                │   │               │   └── UserController.java
                │   │               │
                │   │               ├── dto/
                │   │               │   ├── UserCreateRequest.java
                │   │               │   └── UserResponse.java
                │   │               │
                │   │               ├── entity/
                │   │               │   └── User.java
                │   │               │
                │   │               ├── exception/
                │   │               │   └── GlobalExceptionHandler.java
                │   │               │
                │   │               ├── repository/
                │   │               │   └── UserRepository.java
                │   │               │
                │   │               └── service/
                │   │                   └── UserService.java
                │   │
                │   └── resources/
                │       └── application.yml
                │
                └── test/
                    └── java/
                        └── com/
                            └── shopsphere/
                                └── userservice/
                                    ├── controller/
                                    │   └── UserControllerTest.java
                                    │
                                    └── service/
                                        └── UserServiceTest.java
```

`target/` is intentionally excluded because it contains Maven-generated build artifacts.

---

# 6. Files Created / Modified

## 6.1 `.gitignore`

**Path**

```text
application/services/user-service/.gitignore
```

**Created / Modified**

Created.

**Purpose**

Prevents generated Maven files from entering Git.

```gitignore
target/
*.log
```

**Active / Example**

`target/` contains:

```text
.class files
test reports
Maven compiler metadata
build artifacts
```

**Enterprise Benefit**

Keeps source control clean and prevents generated artifacts from being versioned.

---

# 6.2 `pom.xml`

**Path**

```text
application/services/user-service/pom.xml
```

**Created / Modified**

Created.

**Purpose**

Defines the User Service Maven project and its dependencies.

Important dependencies:

```text
Spring Boot Web
Spring Data JPA
Spring Validation
PostgreSQL Driver
Spring Boot Test
```

### Dependency Purpose

| Dependency               | Purpose                          |
| ------------------------ | -------------------------------- |
| Spring Web               | REST APIs                        |
| Spring Data JPA          | Database persistence             |
| Validation               | Request validation               |
| PostgreSQL               | PostgreSQL database connectivity |
| Spring Boot Test         | Testing                          |
| Spring Boot Maven Plugin | Spring Boot packaging            |

Java version:

```xml
<java.version>17</java.version>
```

---

# 6.3 `UserServiceApplication.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/UserServiceApplication.java
```

**Purpose**

Spring Boot application entry point.

```java
@SpringBootApplication
public class UserServiceApplication
```

This annotation enables:

* Component scanning
* Auto configuration
* Spring Boot configuration

The application starts through:

```java
SpringApplication.run(
    UserServiceApplication.class,
    args
);
```

---

# 6.4 `User.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/entity/User.java
```

**Purpose**

Represents the User database entity.

Important fields:

```text
id
firstName
lastName
email
password
```

Database table:

```text
users
```

The entity uses:

```java
@Entity
@Table(name = "users")
```

### Validation

```text
@NotBlank
@Email
@Size
```

These ensure user data satisfies basic validation rules.

### Database constraints

The email is configured as:

```java
@Column(nullable = false, unique = true)
```

Therefore email is intended to be unique at the database level.

### Important Security Note

The current ECOM-002 foundation stores the password field directly.

Before production authentication/database deployment, password hashing such as BCrypt must be implemented.

This is intentionally a future security-hardening step rather than part of the current foundation ticket.

---

# 6.5 `UserCreateRequest.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/dto/UserCreateRequest.java
```

**Purpose**

Represents the incoming API request.

The DTO separates external API input from the persistence entity.

Example:

```json
{
  "firstName": "Rakesh",
  "lastName": "Perala",
  "email": "rakesh@example.com",
  "password": "Password123"
}
```

Validation is applied to the request before business processing.

---

# 6.6 `UserResponse.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/dto/UserResponse.java
```

**Purpose**

Defines the API response returned to clients.

The response contains:

```text
id
firstName
lastName
email
```

The password is intentionally not included in `UserResponse`.

Architecture:

```text
User Entity
    |
    | mapping
    ▼
UserResponse
    |
    ▼
API Client
```

This prevents exposing the entity directly as the API contract.

---

# 6.7 `UserRepository.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/repository/UserRepository.java
```

**Purpose**

Provides database access through Spring Data JPA.

```java
public interface UserRepository
        extends JpaRepository<User, Long>
```

This provides standard operations such as:

```text
save()
findAll()
findById()
deleteById()
```

Custom query method:

```java
Optional<User> findByEmail(String email);
```

Spring Data derives the query from the method name.

---

# 6.8 `UserService.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/service/UserService.java
```

**Purpose**

Contains the User Service business logic.

Responsibilities:

```text
Create User
Get All Users
Get User by ID
Get User by Email
Delete User
Map Entity → Response
```

Architecture:

```text
Controller
    |
    ▼
UserService
    |
    ▼
UserRepository
```

The controller does not directly communicate with the repository.

This separation keeps the application maintainable.

---

# 6.9 `UserController.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/controller/UserController.java
```

**Purpose**

Exposes REST APIs.

Base URL:

```text
/api/users
```

### APIs

| HTTP   | Endpoint                   | Purpose           |
| ------ | -------------------------- | ----------------- |
| POST   | `/api/users`               | Create user       |
| GET    | `/api/users`               | Get all users     |
| GET    | `/api/users/{id}`          | Get user by ID    |
| GET    | `/api/users/email/{email}` | Get user by email |
| DELETE | `/api/users/{id}`          | Delete user       |

Request validation is enabled using:

```java
@Valid
```

---

# 6.10 `GlobalExceptionHandler.java`

**Path**

```text
src/main/java/com/shopsphere/userservice/exception/GlobalExceptionHandler.java
```

**Purpose**

Centralizes API exception handling.

It handles:

```text
MethodArgumentNotValidException
Exception
```

Validation errors return:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": {
    "email": "Email must be valid"
  }
}
```

This provides a consistent API error structure.

---

# 6.11 `application.yml`

**Path**

```text
src/main/resources/application.yml
```

**Purpose**

Provides application and database configuration.

Application name:

```yaml
spring:
  application:
    name: user-service
```

Database configuration uses environment variables:

```yaml
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example structure:

```text
Environment
     |
     ├── DB_URL
     ├── DB_USERNAME
     └── DB_PASSWORD
             |
             ▼
      application.yml
             |
             ▼
        Spring Boot
```

This avoids hardcoding production database credentials.

Current default database configuration is for development/local configuration.

Actual AWS RDS connectivity will be introduced through the future infrastructure/deployment milestones.

---

# 6.12 `UserServiceTest.java`

**Path**

```text
src/test/java/com/shopsphere/userservice/service/UserServiceTest.java
```

**Created / Modified**

Created.

**Purpose**

Tests service-layer behavior using Mockito.

Covered scenarios:

```text
Create user
Get all users
Get user by ID
User by ID not found
Get user by email
User by email not found
Delete user
```

Result:

```text
7 tests
7 passed
0 failed
```

---

# 6.13 `UserControllerTest.java`

**Path**

```text
src/test/java/com/shopsphere/userservice/controller/UserControllerTest.java
```

**Created / Modified**

Created.

**Purpose**

Tests REST controller behavior using MockMvc.

Covered scenarios:

```text
Create user
Invalid create request
Get all users
Get user by ID
User ID not found
Get user by email
User email not found
Delete user
```

Result:

```text
8 tests
8 passed
0 failed
```

---

# 7. Deep Concept Explanation

## Controller

Controller is the API entry point.

```text
HTTP Request
     |
     ▼
Controller
```

It should handle:

* HTTP requests
* Request validation
* HTTP response status
* Request/response DTOs

It should not contain heavy business logic.

---

## Service

Service contains business logic.

```text
Controller
    |
    ▼
Service
```

This makes the application easier to test and maintain.

---

## Repository

Repository communicates with the database.

```text
Service
   |
   ▼
Repository
   |
   ▼
Database
```

Spring Data JPA removes the need to write basic CRUD SQL manually.

---

## Entity

Entity represents persistent data.

```text
User Java Object
       |
       ▼
users database table
```

---

## DTO

DTO represents API data.

We intentionally separate:

```text
API Model
    ≠
Database Entity
```

This is important because database structure and API contracts may evolve independently.

---

# 8. Every Command Explained

## Maven Test

```bash
mvn clean test
```

### `clean`

Deletes:

```text
target/
```

### `test`

Performs:

```text
compile
test compile
run tests
```

Final result:

```text
Tests run: 15
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

---

## Git Status

```bash
git status
```

Shows:

* current branch
* staged changes
* unstaged changes
* untracked files

---

## Stage Files

```bash
git add application/services/user-service
```

Stages only the ECOM-002 User Service.

---

## Staged Validation

```bash
git diff --cached --check
```

Checks staged changes for whitespace/errors.

No output means no detected whitespace errors.

---

## Staged Statistics

```bash
git diff --cached --stat
```

ECOM-002 result:

```text
13 files changed
874 insertions
```

---

## Commit

```bash
git commit -m "ECOM-002: implement user service"
```

Created commit:

```text
bf555da ECOM-002: implement user service
```

---

## Push

```bash
git push -u origin feature/ECOM-002-user-service
```

This created the remote feature branch and configured upstream tracking.

---

## Branch Verification

```bash
git branch -vv
```

Confirmed:

```text
feature/ECOM-002-user-service
    bf555da
    [origin/feature/ECOM-002-user-service]
```

---

# 9. YAML Explained

Current configuration:

```yaml
spring:
  application:
    name: user-service

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/shopsphere}
    username: ${DB_USERNAME:shopsphere}
    password: ${DB_PASSWORD:shopsphere}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

server:
  port: 8081
```

## Environment Variable Syntax

Example:

```text
${DB_URL:default-value}
```

Means:

```text
Use DB_URL if available.
Otherwise use the default value.
```

Enterprise deployment can inject:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

through Kubernetes Secrets or another secure configuration mechanism.

---

# 10. README.md

For this Jira ticket, the service-level README can be introduced later as part of the application/deployment documentation milestone.

The repository-level README already exists from ECOM-001.

The current ECOM-002 source of truth is:

```text
application/services/user-service/
```

---

# 11. Deployment

ECOM-002 establishes the application foundation but does not yet deploy the service to AWS.

Future deployment architecture:

```text
Developer
    |
    ▼
GitHub
    |
    ▼
Jenkins
    |
    ├── Build
    ├── Test
    ├── Security Scan
    ├── Docker Build
    └── Push Image
             |
             ▼
            ECR
             |
             ▼
          EKS
             |
             ▼
      User Service Pod
             |
             ▼
        Amazon RDS
```

Those infrastructure and deployment responsibilities belong to later Jira milestones.

---

# 12. Validation

## Maven Validation

Command:

```bash
mvn clean test
```

Result:

```text
Tests run: 15
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

## Service Tests

```text
UserServiceTest
7/7 passed
```

## Controller Tests

```text
UserControllerTest
8/8 passed
```

## Git Validation

Working tree:

```text
nothing to commit, working tree clean
```

Remote branch:

```text
origin/feature/ECOM-002-user-service
```

Commit:

```text
bf555da
```

---

# 13. Troubleshooting & RCA

## Issue 1 — Maven Application Could Not Connect to Database

The service uses PostgreSQL through Spring Data JPA.

Initially, local PostgreSQL was not installed.

Instead of installing a local database unnecessarily, the architecture keeps the application configuration externalized and prepares the service for the future AWS RDS PostgreSQL environment.

### RCA

```text
Spring Boot
    |
    ▼
JPA
    |
    ▼
PostgreSQL connection required
    |
    ▼
No local PostgreSQL
```

### Resolution

Do not create infrastructure just to test the application foundation.

AWS RDS will be provisioned later through Terraform.

---

## Issue 2 — Maven `target/` Appeared in Git

After running:

```bash
mvn clean test
```

Maven generated:

```text
target/
```

Git initially detected the generated files.

### RCA

Maven build artifacts were not ignored.

### Resolution

Created:

```text
application/services/user-service/.gitignore
```

with:

```gitignore
target/
*.log
```

After that, generated build files disappeared from Git's untracked list.

---

## Issue 3 — `git diff --stat` Was Empty

Before staging, Git showed:

```text
?? application/
```

but:

```bash
git diff --stat
```

showed nothing.

### RCA

`git diff` normally shows changes to tracked files.

The ECOM-002 files were new/untracked.

### Resolution

Used:

```bash
git diff --cached --stat
```

after staging.

Result:

```text
13 files changed
874 insertions(+)
```

---

## Issue 4 — `@MockBean` Deprecation Warning

Maven displayed:

```text
MockBean ... has been deprecated and marked for removal
```

### Impact

No test failure occurred.

```text
15/15 tests passed
BUILD SUCCESS
```

### Future Action

The testing approach can be modernized in a future maintenance/hardening ticket.

It does not block ECOM-002.

---

# 14. Rollback Procedure

If ECOM-002 must be removed before merging to the main branch:

### Local branch rollback

The commit is:

```text
bf555da
```

Before rewriting history, coordinate with the team if the branch is shared.

For a local-only rollback:

```bash
git reset --hard HEAD~1
```

Because the branch has already been pushed, do not force-push without team coordination.

A safer enterprise approach is normally to create a new corrective commit or revert the existing commit when the branch is already shared.

---

# 15. Production Best Practices

The current ECOM-002 implementation is a foundation. Before production deployment, additional hardening should be introduced.

## Security

Password hashing should be implemented.

```text
Raw Password
     |
     ▼
BCrypt / secure hashing
     |
     ▼
Database
```

Never store production passwords as plaintext.

## Secrets

Do not commit:

```text
DB_PASSWORD
API keys
tokens
credentials
```

Use:

```text
Kubernetes Secrets
AWS Secrets Manager
External Secrets
```

according to the final platform design.

## Database

Production database infrastructure should be provisioned through Terraform.

## API

Consider returning:

```http
201 Created
```

for successful resource creation rather than `200 OK`.

## Error Handling

Future versions can add specific handling for:

* Duplicate email
* Database exceptions
* Resource not found
* Authentication errors

## Observability

Future milestones should add:

```text
Metrics
Logs
Tracing
Health checks
Alerts
```

---

# 16. Interview Questions & Answers

## Q1. Why did you create a separate User Service?

**Answer:**

> We use a microservice architecture, so user functionality is owned by a dedicated User Service. This gives us clear ownership of user data, independent deployment, independent scaling, and a clean API boundary from services such as Product, Cart, Order, and Payment.

---

## Q2. Why don't you expose the JPA entity directly?

**Answer:**

> We use DTOs to separate the API contract from the persistence model. This prevents database implementation details from leaking into the API and allows the database model and API contract to evolve independently.

---

## Q3. Why do you have Controller, Service, and Repository layers?

**Answer:**

> The controller handles HTTP concerns, the service handles business logic, and the repository handles persistence. This separation improves maintainability, testing, and code organization.

---

## Q4. How does Spring Data JPA create database queries?

**Answer:**

> Spring Data JPA can derive queries from repository method names. For example, `findByEmail(String email)` is interpreted by Spring Data and translated into the appropriate database query.

---

## Q5. Why use environment variables for database configuration?

**Answer:**

> We don't want environment-specific configuration or credentials hardcoded in the application. Environment variables allow the same application artifact to run in different environments while the deployment platform supplies the appropriate database configuration.

---

## Q6. How would you deploy this service in AWS?

**Answer:**

> I would provision the AWS infrastructure using Terraform, build the Spring Boot application with Jenkins, create a Docker image, push it to Amazon ECR, and deploy the service to Amazon EKS. The service would connect to PostgreSQL running on Amazon RDS through secure private networking.

---

## Q7. How did you test the service?

**Answer:**

> I implemented service-layer unit tests using Mockito and controller-layer tests using MockMvc. The final Maven validation executed 15 tests, with all 15 passing and zero failures or errors.

---

## Q8. Why is `target/` not committed?

**Answer:**

> `target/` contains Maven-generated build artifacts such as compiled classes and test reports. These are generated during the build and should not be stored in source control.

---

## Q9. What happens when an invalid request reaches the API?

**Answer:**

> The request is validated using Jakarta Bean Validation. If validation fails, `MethodArgumentNotValidException` is handled by the global exception handler and the API returns HTTP 400 with structured validation details.

---

## Q10. How would you secure the password?

**Answer:**

> I would never store the plaintext password. I would hash it using a strong password hashing algorithm such as BCrypt and store only the hash. Authentication and authorization would be handled through a dedicated security design.

---

# 17. Review Questions

### Architecture

1. Why is User Service separated from Product Service?
2. Why should services avoid direct database access to another service's data?
3. What responsibility belongs to the Controller?
4. What responsibility belongs to the Service?
5. What responsibility belongs to the Repository?

### Spring Boot

6. What does `@SpringBootApplication` do?
7. What does `@RestController` do?
8. What does `@RequestMapping` do?
9. What does `@Valid` do?
10. What does `@RestControllerAdvice` do?

### JPA

11. What is `@Entity`?
12. What is `@Id`?
13. What does `@GeneratedValue` do?
14. Why is email marked unique?
15. What is Spring Data JPA?

### Testing

16. Why use Mockito?
17. Why use MockMvc?
18. What is the difference between unit testing and controller testing?
19. How many tests were executed for ECOM-002?
20. What was the final result?

### DevOps

21. Why should `target/` not be committed?
22. Why are database credentials externalized?
23. How would Jenkins build this service?
24. How would Docker package it?
25. How would EKS deploy it?

---

# 18. Enterprise Git Record

## Jira

```text
ECOM-002
Implement User Service
```

## Branch

```text
feature/ECOM-002-user-service
```

## Commit

```text
bf555da
ECOM-002: implement user service
```

## Remote Branch

```text
origin/feature/ECOM-002-user-service
```

## Working Tree

```text
clean
```

## Tests

```text
15 passed
0 failed
0 errors
0 skipped
```

---

# 19. Enterprise Implementation Flow

```text
Jira ECOM-002
      |
      ▼
Create Feature Branch
      |
      ▼
Implement User Service
      |
      ├── Spring Boot
      ├── Controller
      ├── DTO
      ├── Entity
      ├── Service
      ├── Repository
      └── Exception Handler
      |
      ▼
Implement Tests
      |
      ├── Service Tests
      └── Controller Tests
      |
      ▼
mvn clean test
      |
      ▼
15/15 PASS
      |
      ▼
.gitignore target/
      |
      ▼
Git Review
      |
      ▼
Commit
      |
      ▼
ECOM-002: implement user service
      |
      ▼
Push Feature Branch
      |
      ▼
origin/feature/ECOM-002-user-service
```

---

# 20. ECOM-002 Final Status

```text
┌──────────────────────────────────────────┐
│        ECOM-002 — USER SERVICE           │
├──────────────────────────────────────────┤
│ Application Bootstrap       ✅            │
│ User Entity                ✅            │
│ DTOs                       ✅            │
│ Repository                 ✅            │
│ Service Layer              ✅            │
│ REST Controller            ✅            │
│ Exception Handling         ✅            │
│ PostgreSQL Configuration   ✅            │
│ Unit Tests                 ✅ 7/7        │
│ Controller Tests           ✅ 8/8        │
│ Full Maven Validation      ✅            │
│ Git Ignore                 ✅            │
│ Enterprise Commit          ✅            │
│ Feature Branch Push        ✅            │
│ Working Tree               ✅ Clean      │
└──────────────────────────────────────────┘
```

**ECOM-002 is complete and pushed successfully.**
