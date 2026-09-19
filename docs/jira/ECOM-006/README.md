# ECOM-006 — Payment Service

**Project:** ShopSphere — Enterprise E-Commerce Platform
**Jira Ticket:** ECOM-006
**Service:** Payment Service
**Service Port:** 8085
**Database:** `shopsphere_payment`
**Branch:** `feature/ECOM-006-payment-service`
**Implementation Commit:** `fd01eae ECOM-006: implement payment service`
**Documentation Path:** `docs/jira/ECOM-006/README.md`

---

# 1. Business Requirement

ShopSphere requires a dedicated **Payment Service** to manage payment information associated with customer orders.

The Payment Service is responsible for:

* Creating payment records.
* Storing payment amount and payment method.
* Tracking payment status.
* Maintaining transaction IDs.
* Retrieving payments.
* Finding payments associated with an order.
* Updating payment status.
* Deleting payment records when required.

For this training implementation, payment processing is **simulated**.

No real payment gateway such as Stripe or Razorpay is integrated yet.

## Business Requirement

When a customer places an order:

```text
Customer
   |
   v
ShopSphere Application
   |
   v
Order Service
   |
   v
Payment Service
   |
   v
Payment Record
```

The Payment Service maintains payment information independently from the Order Service.

---

# 2. Why Business Needs It

Payment information is a critical part of an e-commerce platform.

A business needs a dedicated Payment Service because payment operations should not be tightly coupled with other business services.

## Key Reasons

### 2.1 Payment Ownership

The Payment Service owns payment-related data.

```text
Payment Service
      |
      +---- Payment ID
      +---- Order ID
      +---- User ID
      +---- Amount
      +---- Payment Method
      +---- Status
      +---- Transaction ID
      +---- Created Time
      +---- Updated Time
```

### 2.2 Independent Scaling

Payment traffic can increase independently from other services.

For example:

```text
Normal Traffic

User Service       2 instances
Product Service    2 instances
Cart Service       2 instances
Order Service      2 instances
Payment Service    2 instances
```

During a large sale:

```text
Payment Traffic
       |
       v
Payment Service
       |
       +---- Instance 1
       +---- Instance 2
       +---- Instance 3
       +---- Instance 4
```

The service can later be independently scaled on Kubernetes.

### 2.3 Failure Isolation

If payment processing has an issue, it should not directly corrupt product or cart data.

This follows the microservice principle:

> Each service owns its own business responsibility and data.

### 2.4 Auditability

Payments require transaction information.

The system needs to know:

```text
Which order?
Which user?
How much?
Which payment method?
Which transaction?
What status?
When created?
When updated?
```

---

# 3. Real-Time Enterprise Scenario

Consider a customer purchasing a laptop from ShopSphere.

```text
Customer
   |
   | 1. Select Product
   v
Product Service
   |
   | 2. Add to Cart
   v
Cart Service
   |
   | 3. Checkout
   v
Order Service
   |
   | 4. Create Order
   v
Payment Service
   |
   | 5. Create Payment
   v
Payment Database
```

Example:

```text
Order ID       : 1001
User ID        : 2001
Amount         : ₹49,999
Payment Method : CARD
Transaction ID : TXN-1001
Status         : PENDING
```

The Payment Service stores this information.

Later, the payment gateway or payment workflow can update:

```text
PENDING
   |
   v
SUCCESS
```

or:

```text
PENDING
   |
   v
FAILED
```

For the current training implementation, the status update is performed through the service API.

---

# 4. Architecture Diagram

## 4.1 Current ShopSphere Service Architecture

```text
                         CUSTOMER
                            |
                            v
                    +---------------+
                    |   Frontend    |
                    +---------------+
                            |
                            v
                    +---------------+
                    | API / Gateway |
                    +---------------+
                            |
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
   User Service      Product Service     Cart Service
      :8081               :8082              :8083
          |                 |                 |
          v                 v                 v
   User Database     Product Database     Cart Database


                            |
                            v
                     +-------------+
                     | Order       |
                     | Service     |
                     | :8084       |
                     +-------------+
                            |
                            | orderId
                            | userId
                            | amount
                            v
                     +-------------+
                     | Payment     |
                     | Service     |
                     | :8085       |
                     +-------------+
                            |
                            v
                  +----------------------+
                  | shopsphere_payment   |
                  | PostgreSQL Database  |
                  +----------------------+
```

## 4.2 Payment Service Internal Architecture

```text
Client
  |
  v
PaymentController
  |
  v
PaymentRequest
  |
  v
PaymentService
  |
  v
PaymentRepository
  |
  v
Payment Entity
  |
  v
PostgreSQL
```

## 4.3 Payment Creation Flow

```text
POST /api/payments
        |
        v
PaymentController
        |
        | Validate request
        v
PaymentRequest
        |
        v
PaymentService
        |
        | Create Payment object
        v
Payment Entity
        |
        v
PaymentRepository
        |
        v
PostgreSQL
        |
        v
PaymentResponse
        |
        v
HTTP 201 CREATED
```

---

# 5. Repository Structure

The Payment Service follows the ShopSphere microservice structure.

```text
ShopSphere-E-Commerce-Platform/
│
├── application/
│   └── services/
│       └── payment-service/
│           │
│           ├── pom.xml
│           ├── .gitignore
│           │
│           └── src/
│               ├── main/
│               │   ├── java/
│               │   │   └── com/
│               │   │       └── shopsphere/
│               │   │           └── paymentservice/
│               │   │               │
│               │   │               ├── PaymentServiceApplication.java
│               │   │               │
│               │   │               ├── controller/
│               │   │               │   └── PaymentController.java
│               │   │               │
│               │   │               ├── dto/
│               │   │               │   ├── PaymentRequest.java
│               │   │               │   └── PaymentResponse.java
│               │   │               │
│               │   │               ├── entity/
│               │   │               │   └── Payment.java
│               │   │               │
│               │   │               ├── exception/
│               │   │               │   └── GlobalExceptionHandler.java
│               │   │               │
│               │   │               ├── repository/
│               │   │               │   └── PaymentRepository.java
│               │   │               │
│               │   │               └── service/
│               │   │                   └── PaymentService.java
│               │   │
│               │   └── resources/
│               │       └── application.yml
│               │
│               └── test/
│                   └── java/
│                       └── com/
│                           └── shopsphere/
│                               └── paymentservice/
│                                   ├── service/
│                                   │   └── PaymentServiceTest.java
│                                   │
│                                   └── controller/
│                                       └── PaymentControllerTest.java
│
└── docs/
    └── jira/
        └── ECOM-006/
            └── README.md
```

---

# 6. Files Created / Modified

## 6.1 File Summary

| File                             | Action  | Purpose                            |
| -------------------------------- | ------- | ---------------------------------- |
| `pom.xml`                        | Created | Maven dependencies/build           |
| `.gitignore`                     | Created | Ignore build artifacts             |
| `PaymentServiceApplication.java` | Created | Spring Boot entry point            |
| `Payment.java`                   | Created | Payment database entity            |
| `PaymentRepository.java`         | Created | Database access                    |
| `PaymentService.java`            | Created | Business logic                     |
| `PaymentRequest.java`            | Created | Request validation                 |
| `PaymentResponse.java`           | Created | API response model                 |
| `PaymentController.java`         | Created | REST API endpoints                 |
| `GlobalExceptionHandler.java`    | Created | Exception handling                 |
| `application.yml`                | Created | Application/database configuration |
| `PaymentServiceTest.java`        | Created | Service unit tests                 |
| `PaymentControllerTest.java`     | Created | Controller tests                   |

## 6.2 `.gitignore`

```gitignore
target/
*.log
```

Purpose:

```text
Developer Build
     |
     v
Maven creates target/
     |
     v
.gitignore
     |
     v
Git does NOT track target/
```

This prevents compiled artifacts from entering Git.

---

# 7. Deep Concept Explanation

# 7.1 Spring Boot Application

`PaymentServiceApplication.java` is the entry point.

```java
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
```

Spring Boot starts:

* Embedded web server.
* Spring dependency injection.
* REST controllers.
* JPA repositories.
* Application configuration.

---

# 7.2 Entity Layer

`Payment.java` represents payment data.

```text
Payment Entity
      |
      +-- id
      +-- orderId
      +-- userId
      +-- amount
      +-- paymentMethod
      +-- status
      +-- transactionId
      +-- createdAt
      +-- updatedAt
```

The entity is mapped to:

```text
payments
```

table.

---

# 7.3 Payment ID

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

The database generates the primary key.

Example:

```text
Payment 1
Payment 2
Payment 3
```

---

# 7.4 Order ID

```java
private Long orderId;
```

The Payment Service stores the order reference.

Important microservice principle:

```text
Order Service DB
       X
       |
       X   No direct database relationship
       |
Payment Service DB
```

Instead:

```text
Payment
   |
   +-- orderId = 1001
```

The Payment Service does not directly access the Order Service database.

---

# 7.5 User ID

```java
private Long userId;
```

This identifies the customer associated with the payment.

Again, this is an ID reference rather than a direct database relationship.

---

# 7.6 BigDecimal for Money

```java
private BigDecimal amount;
```

`BigDecimal` is preferred for monetary values because floating-point types can introduce precision problems.

Example:

```text
499.99
999.99
49999.00
```

---

# 7.7 Payment Status

Initial status:

```text
PENDING
```

Possible future lifecycle:

```text
PENDING
   |
   +----> SUCCESS
   |
   +----> FAILED
```

Future production implementation may support additional states such as:

```text
PENDING
AUTHORIZED
SUCCESS
FAILED
REFUNDED
CANCELLED
```

---

# 7.8 Transaction ID

```java
@Column(nullable = false, unique = true)
private String transactionId;
```

Transaction IDs identify payment transactions.

Example:

```text
TXN-1001
TXN-1002
TXN-1003
```

The database uniqueness constraint prevents duplicate transaction IDs.

---

# 7.9 Repository Layer

```java
public interface PaymentRepository
        extends JpaRepository<Payment, Long>
```

Spring Data JPA provides common operations automatically.

Examples:

```text
save()
findAll()
findById()
delete()
```

Custom query:

```java
List<Payment> findByOrderId(Long orderId);
```

Spring Data derives the query from the method name.

---

# 7.10 Service Layer

`PaymentService.java` contains business operations.

```text
Controller
    |
    v
Service
    |
    v
Repository
```

The controller should not directly manage database operations.

This separation makes the application easier to:

* Test.
* Maintain.
* Extend.
* Troubleshoot.

---

# 7.11 DTO Layer

## PaymentRequest

Used when clients send payment information.

```text
Client
   |
   v
PaymentRequest
   |
   v
Validation
   |
   v
PaymentService
```

## PaymentResponse

Used when the API sends payment information back.

```text
Payment Entity
      |
      v
PaymentResponse
      |
      v
Client
```

DTOs prevent the API contract from being tightly coupled to the database entity.

---

# 7.12 Validation

Example:

```java
@NotNull
@Positive
private Long orderId;
```

This prevents invalid order IDs.

Amount:

```java
@DecimalMin(value = "0.01")
private BigDecimal amount;
```

Payment method:

```java
@NotBlank
private String paymentMethod;
```

Transaction ID:

```java
@NotBlank
private String transactionId;
```

Invalid requests are rejected before business processing.

---

# 7.13 REST Controller

Base endpoint:

```text
/api/payments
```

Supported operations:

```text
POST    /api/payments
GET     /api/payments
GET     /api/payments/{id}
GET     /api/payments/order/{orderId}
PUT     /api/payments/{id}/status
DELETE  /api/payments/{id}
```

---

# 7.14 Exception Handling

The application uses:

```java
@RestControllerAdvice
```

This allows centralized exception handling.

Current training implementation maps `RuntimeException` to HTTP 404.

Production hardening can later distinguish:

```text
Validation Error       -> 400
Payment Not Found      -> 404
Duplicate Transaction  -> 409
Unexpected Error       -> 500
```

The current implementation intentionally follows the pattern used by the earlier ShopSphere services.

---

# 8. Every Command Explained

## 8.1 Navigate to Repository

```bash
cd ~/PROJECT/ShopSphere-E-Commerce-Platform
```

Moves into the ShopSphere repository.

---

## 8.2 Switch to Develop

```bash
git switch develop
```

Moves to the integration branch.

---

## 8.3 Synchronize Develop

```bash
git pull origin develop
```

Downloads and integrates the latest `develop` changes.

---

## 8.4 Create Feature Branch

```bash
git switch -c feature/ECOM-006-payment-service
```

Creates the Jira-specific feature branch.

Naming convention:

```text
feature/<JIRA-ID>-<short-description>
```

---

## 8.5 Check Git Status

```bash
git status
```

Used before and after implementation to understand repository state.

---

## 8.6 Check Differences

```bash
git diff
```

Shows unstaged changes.

For staged changes:

```bash
git diff --cached
```

---

## 8.7 Stage Files

```bash
git add .gitignore pom.xml src/
```

Stages only the intended Payment Service files.

This was important because Maven creates `target/` directories that should not be committed.

---

## 8.8 Review Staged Files

```bash
git status --short
```

Expected:

```text
A  .gitignore
A  pom.xml
A  src/...
```

No `target/` files should appear.

---

## 8.9 Check Ignored Files

```bash
git check-ignore -v target/target*
```

This verifies that Maven build artifacts are ignored.

---

## 8.10 Review Staged Statistics

```bash
git diff --cached --stat
```

Shows the number of files and lines staged.

ECOM-006 implementation staging resulted in:

```text
13 files changed
1006 insertions
```

---

## 8.11 Commit

```bash
git commit -m "ECOM-006: implement payment service"
```

Creates the implementation commit.

Commit convention:

```text
<JIRA-ID>: <short description>
```

---

## 8.12 Push Feature Branch

```bash
git push -u origin feature/ECOM-006-payment-service
```

Pushes the feature branch to GitHub and establishes upstream tracking.

---

## 8.13 Maven Clean Test

```bash
mvn clean test
```

This performs:

```text
clean
 |
 +-- Remove previous build output
 |
 v
compile
 |
 v
test
 |
 v
BUILD SUCCESS
```

ECOM-006 validation:

```text
PaymentServiceTest
7 tests passed

PaymentControllerTest
7 tests passed

Total
14 tests passed
```

---

# 9. Every YAML / JSON / Configuration Explained

## 9.1 `application.yml`

```yaml
spring:
  application:
    name: payment-service

  datasource:
    url: jdbc:postgresql://localhost:5432/shopsphere_payment
    username: shopsphere
    password: shopsphere

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8085
```

## Application Name

```yaml
spring:
  application:
    name: payment-service
```

Identifies the Spring Boot application.

This becomes useful later for:

* Service discovery.
* Logging.
* Monitoring.
* Metrics.
* Distributed tracing.

---

## Database URL

```yaml
url: jdbc:postgresql://localhost:5432/shopsphere_payment
```

Breakdown:

```text
jdbc
 |
 +-- PostgreSQL
       |
       +-- Host: localhost
       +-- Port: 5432
       +-- Database: shopsphere_payment
```

---

## Database Username

```yaml
username: shopsphere
```

Database user used by the application.

---

## Database Password

```yaml
password: shopsphere
```

Training-only configuration.

In production this should NOT be stored as plain text.

Production options include:

```text
AWS Secrets Manager
AWS Parameter Store
Kubernetes Secrets
External Secrets Operator
Vault
```

---

## Hibernate DDL

```yaml
ddl-auto: update
```

For this training environment Hibernate updates the database schema automatically.

Production environments normally require controlled database migrations using tools such as:

```text
Flyway
Liquibase
```

---

## SQL Logging

```yaml
show-sql: false
```

SQL statements are not printed during normal application execution.

This keeps logs cleaner.

---

## SQL Formatting

```yaml
format_sql: true
```

When SQL is generated/logged, formatting is easier to read.

---

## Server Port

```yaml
server:
  port: 8085
```

Payment Service listens on:

```text
8085
```

Current ShopSphere service ports:

```text
User Service       8081
Product Service    8082
Cart Service       8083
Order Service      8084
Payment Service    8085
```

---

# 10. README Purpose / Deployment / Validation / Rollback / Usage

## 10.1 README Purpose

This README documents the complete ECOM-006 implementation.

It provides:

* Business requirement.
* Architecture.
* Repository structure.
* Implementation details.
* Configuration.
* Commands.
* Testing.
* Troubleshooting.
* Rollback.
* Git flow.
* Interview preparation.

---

# Deployment

## Local Deployment

Prerequisites:

```text
Java 17
Maven
PostgreSQL
Git
```

Set Java 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Verify:

```bash
java -version
```

Expected:

```text
Java 17
```

---

## Start PostgreSQL

The training environment expects:

```text
Host     : localhost
Port     : 5432
Database : shopsphere_payment
User     : shopsphere
```

---

## Build

```bash
mvn clean package
```

---

## Run

```bash
mvn spring-boot:run
```

Application:

```text
http://localhost:8085
```

---

# API Usage

## Create Payment

```http
POST /api/payments
```

Example request:

```json
{
  "orderId": 100,
  "userId": 200,
  "amount": 499.99,
  "paymentMethod": "CARD",
  "transactionId": "TXN-1001"
}
```

Expected result:

```text
HTTP 201 CREATED
```

Example response:

```json
{
  "id": 1,
  "orderId": 100,
  "userId": 200,
  "amount": 499.99,
  "paymentMethod": "CARD",
  "status": "PENDING",
  "transactionId": "TXN-1001"
}
```

---

## Get All Payments

```http
GET /api/payments
```

---

## Get Payment by ID

```http
GET /api/payments/1
```

---

## Get Payments by Order

```http
GET /api/payments/order/100
```

---

## Update Payment Status

```http
PUT /api/payments/1/status?status=SUCCESS
```

---

## Delete Payment

```http
DELETE /api/payments/1
```

Expected:

```text
HTTP 204 NO CONTENT
```

---

# 11. Validation

## 11.1 Maven Validation

Command:

```bash
mvn clean test
```

Result:

```text
PaymentServiceTest
7 tests
7 passed

PaymentControllerTest
7 tests
7 passed

Total
14 tests
14 passed
```

Result:

```text
BUILD SUCCESS
```

---

## 11.2 Validation Coverage

### Service Tests

Covered:

```text
Create Payment
Get All Payments
Get Payment By ID
Payment Not Found
Get Payments By Order ID
Update Payment Status
Delete Payment
```

### Controller Tests

Covered:

```text
Create Payment
Get All Payments
Get Payment By ID
Get Payments By Order ID
Update Payment Status
Delete Payment
Invalid Request
```

---

## 11.3 Git Validation

Final feature branch:

```text
feature/ECOM-006-payment-service
```

Implementation commit:

```text
fd01eae ECOM-006: implement payment service
```

Remote:

```text
origin/feature/ECOM-006-payment-service
```

Working tree:

```text
nothing to commit, working tree clean
```

---

# 12. Troubleshooting & RCA

## Issue 1 — Maven Uses Wrong Java Version

### Symptom

Maven may use a different installed Java version.

### Check

```bash
java -version
mvn -version
```

### Resolution

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Then:

```bash
mvn -version
```

Verify Maven uses Java 17.

### RCA

The project is configured for:

```text
Java 17
```

but the environment may contain another Java version.

---

# Issue 2 — `target/` Appears in Git

### Symptom

```bash
git status
```

shows Maven build artifacts.

### Check

```bash
git check-ignore -v target/target*
```

### Resolution

Ensure `.gitignore` contains:

```gitignore
target/
*.log
```

### RCA

Maven automatically creates compiled output under:

```text
target/
```

Build artifacts should not be version controlled.

---

# Issue 3 — Database Connection Failure

### Symptom

Application cannot connect to PostgreSQL.

### Check

```bash
psql -h localhost -p 5432 -U shopsphere -d shopsphere_payment
```

Verify:

```text
PostgreSQL running
Database exists
Username exists
Password correct
Port 5432 available
```

### RCA

Possible causes:

```text
PostgreSQL stopped
Wrong database name
Wrong credentials
Wrong port
Database unavailable
```

---

# Issue 4 — Port 8085 Already in Use

### Check

```bash
sudo lsof -i :8085
```

or:

```bash
ss -ltnp | grep 8085
```

### RCA

Another process is already listening on port 8085.

---

# Issue 5 — Invalid Payment Request

Example:

```json
{
  "userId": 200,
  "amount": 499.99,
  "paymentMethod": "CARD",
  "transactionId": "TXN-1001"
}
```

Missing:

```text
orderId
```

The validation layer rejects the request.

Expected:

```text
HTTP 400 BAD REQUEST
```

---

# Issue 6 — Duplicate Transaction ID

The entity contains:

```java
unique = true
```

for:

```text
transactionId
```

Therefore the database should reject duplicate transaction IDs.

Production implementation should handle this explicitly and return:

```text
HTTP 409 CONFLICT
```

rather than exposing a generic database exception.

---

# 13. Rollback Procedure

Rollback depends on where the issue is discovered.

## 13.1 Rollback Local Code

If the implementation is not yet merged:

```bash
git switch feature/ECOM-006-payment-service
git log --oneline
```

Identify the implementation commit:

```text
fd01eae ECOM-006: implement payment service
```

Then rollback carefully if required.

---

## 13.2 Revert Implementation Commit

For a shared branch where history must remain intact:

```bash
git revert fd01eae
```

Then:

```bash
git push origin feature/ECOM-006-payment-service
```

`git revert` creates a new commit that reverses the previous commit.

This is generally safer than rewriting shared history.

---

## 13.3 Rollback After Merge

If ECOM-006 has already been merged into `develop`, identify the merge commit:

```bash
git log --oneline --graph
```

Then use a controlled revert procedure appropriate to the merge.

Do not use:

```bash
git reset --hard
git push --force
```

on a shared enterprise branch unless explicitly approved.

---

## 13.4 Application Rollback

In future Kubernetes deployment:

```text
Current Version
      |
      v
Payment Service v2
      |
      | Problem
      v
Rollback
      |
      v
Payment Service v1
```

Helm can later support:

```bash
helm history payment-service
helm rollback payment-service <revision>
```

The exact release name and namespace will be defined when Kubernetes deployment is implemented.

---

# 14. Production Best Practices

The current implementation is intentionally simple for DevOps training.

For production, improve the following areas.

## 14.1 Never Store Passwords in Git

Current:

```yaml
password: shopsphere
```

Production:

```text
AWS Secrets Manager
       |
       v
Application
```

---

## 14.2 Use Database Migration

Instead of:

```yaml
ddl-auto: update
```

Production should use:

```text
Flyway / Liquibase
```

with controlled migrations.

---

## 14.3 Payment Gateway Integration

Current:

```text
Payment Service
      |
      v
Simulated Payment
```

Future:

```text
Payment Service
      |
      v
Payment Gateway
      |
      v
Bank / Card / UPI
```

---

## 14.4 Idempotency

Payment systems must protect against duplicate requests.

Example:

```text
Request 1
TXN-1001
   |
   v
Payment Created

Request 2
TXN-1001
   |
   v
Detect Existing Transaction
   |
   v
Do Not Create Duplicate Payment
```

---

## 14.5 Secure Transaction Handling

Use:

* HTTPS.
* Authentication.
* Authorization.
* Secret management.
* Encryption.
* Audit logging.
* Sensitive-data masking.

---

## 14.6 Observability

Production Payment Service should expose:

```text
Metrics
Logs
Traces
Health Checks
```

Future ShopSphere stack:

```text
Payment Service
      |
      +---- Prometheus
      |
      +---- Grafana
      |
      +---- Loki
      |
      +---- Alerting
```

---

## 14.7 Kubernetes Readiness

Later the service will be containerized:

```text
Payment Service
      |
      v
Docker Image
      |
      v
Amazon ECR
      |
      v
Amazon EKS
```

---

## 14.8 CI/CD

Future pipeline:

```text
Developer
   |
   v
Git
   |
   v
Jenkins
   |
   +--> Maven Test
   |
   +--> Security Scan
   |
   +--> Docker Build
   |
   +--> Image Scan
   |
   +--> Push ECR
   |
   v
Deployment
```

---

# 15. Interview Questions & Answers

## Q1. What is the responsibility of the Payment Service?

**Answer:**

The Payment Service is responsible for managing payment-related information such as order ID, user ID, amount, payment method, transaction ID and payment status. In our ShopSphere implementation, payment processing is simulated, while the service is designed so that a real payment gateway can be integrated later.

---

## Q2. Why did you create a separate Payment Service?

**Answer:**

We follow a microservice architecture where each service owns a specific business capability. Payment processing has different security, scalability and operational requirements, so we isolated it into a dedicated service with its own data ownership.

---

## Q3. Does Payment Service directly access Order Service's database?

**Answer:**

No. Payment Service stores the `orderId` and `userId` as references. It does not directly access another service's database. If additional order information is required, the services communicate through APIs or future asynchronous events.

---

## Q4. Why did you use BigDecimal for payment amount?

**Answer:**

We use `BigDecimal` because financial calculations require precise decimal arithmetic. Floating-point types such as `double` can introduce precision issues.

---

## Q5. Why do you use DTOs?

**Answer:**

DTOs separate the external API contract from the persistence entity. This prevents the database model from being directly exposed and gives us flexibility to evolve the API independently.

---

## Q6. What is the purpose of `PaymentRepository`?

**Answer:**

`PaymentRepository` provides the persistence layer. It extends Spring Data JPA's `JpaRepository`, which gives us CRUD operations and allows us to define custom queries such as finding payments by order ID.

---

## Q7. How do you validate payment requests?

**Answer:**

We use Jakarta Bean Validation annotations such as `@NotNull`, `@Positive`, `@DecimalMin` and `@NotBlank`. The controller uses `@Valid`, so invalid requests are rejected before reaching the business layer.

---

## Q8. What happens when a payment is created?

**Answer:**

The client sends a payment request to the controller. The controller validates it and passes the data to the service layer. The service creates a Payment entity with an initial `PENDING` status and saves it through the repository into PostgreSQL. Finally, the entity is converted into a PaymentResponse and returned to the client.

---

## Q9. How would you make this production-ready?

**Answer:**

I would integrate a real payment gateway, introduce idempotency, secure secrets using AWS Secrets Manager, use database migrations such as Flyway, add authentication and authorization, implement structured error handling, expose metrics and health checks, containerize the service, deploy it to EKS and monitor it through Prometheus, Grafana and centralized logging.

---

## Q10. How would you troubleshoot a payment service failure?

**Answer:**

I would first check the service health and application logs, then verify database connectivity, recent deployments, resource utilization and dependency failures. I would check Prometheus and Grafana metrics for error rate and latency, inspect centralized logs, identify the root cause and then either fix forward or roll back the deployment depending on the incident.

---

## Q11. How would you prevent duplicate payments?

**Answer:**

I would implement idempotency using a unique transaction or idempotency key. Before creating a payment, the service would check whether that key has already been processed. If it has, the existing payment result would be returned instead of creating another payment.

---

## Q12. What is your Git workflow for ECOM-006?

**Answer:**

I started from the latest `develop` branch and created `feature/ECOM-006-payment-service`. I implemented the service file by file, validated it using Maven tests, reviewed the Git changes, committed using the Jira ID, and pushed the feature branch. After documentation and final validation, the feature branch is merged into `develop`.

---

# 16. Review Questions

Use these questions to test your understanding.

1. Why does ShopSphere need a Payment Service?
2. What responsibilities belong to Payment Service?
3. Why is payment data service-owned?
4. Why does Payment Service store `orderId` instead of directly accessing Order DB?
5. Why is `BigDecimal` used?
6. What is the purpose of `PaymentRequest`?
7. What is the purpose of `PaymentResponse`?
8. What does `PaymentRepository` do?
9. How does `findByOrderId()` work?
10. Why is `transactionId` unique?
11. Why is the initial status `PENDING`?
12. What does `@Valid` do?
13. What does `@NotBlank` do?
14. What does `@DecimalMin("0.01")` do?
15. Why is exception handling centralized?
16. Why should secrets not be stored in Git?
17. Why should production use database migrations?
18. What is payment idempotency?
19. How would you monitor Payment Service?
20. How would you deploy Payment Service to EKS?
21. How would Jenkins build and deploy it?
22. How would you troubleshoot database connectivity?
23. How would you troubleshoot high payment latency?
24. How would you roll back a failed deployment?
25. Explain the complete ECOM-006 Git workflow.

---

# 17. Enterprise Markdown Documentation

This section represents the documentation artifact that belongs in:

```text
docs/jira/ECOM-006/README.md
```

The document records the complete implementation lifecycle:

```text
Requirement
    |
    v
Architecture
    |
    v
Payment Service Design
    |
    v
Implementation
    |
    v
Unit Tests
    |
    v
Controller Tests
    |
    v
Maven Validation
    |
    v
Git Review
    |
    v
Jira Commit
    |
    v
Feature Branch Push
    |
    v
Documentation
    |
    v
Develop Integration
```

## Enterprise Responsibility Boundary

```text
+-------------------+--------------------------+
| Component         | Responsibility            |
+-------------------+--------------------------+
| User Service      | User management           |
| Product Service   | Products/inventory        |
| Cart Service      | Shopping cart             |
| Order Service     | Orders                    |
| Payment Service   | Payments                  |
+-------------------+--------------------------+
```

Payment Service owns:

```text
payments
```

and does not directly modify:

```text
users
products
carts
orders
```

This preserves service ownership.

---

# 18. Git Flow — Enterprise Level

## 18.1 Enterprise Branching Strategy

ShopSphere uses:

```text
main
  |
  v
develop
  |
  +-----------------------------+
  |                             |
  v                             v
feature/ECOM-006-payment-service feature/ECOM-007-...
  |                             |
  v                             v
Implementation                Implementation
  |                             |
  v                             v
Validation                    Validation
  |                             |
  v                             v
Commit                        Commit
  |                             |
  v                             v
Push                          Push
  |                             |
  +-------------+---------------+
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

## 18.2 Why `develop`?

`develop` is the integration branch.

New feature work should normally start from the latest:

```text
develop
```

not from an old/stale feature branch.

---

## 18.3 ECOM-006 Branch

```text
feature/ECOM-006-payment-service
```

The branch identifies:

```text
feature
   +
Jira ticket
   +
business capability
```

---

## 18.4 ECOM-006 Actual Git History

Implementation:

```text
fd01eae ECOM-006: implement payment service
```

Feature branch:

```text
feature/ECOM-006-payment-service
```

Remote:

```text
origin/feature/ECOM-006-payment-service
```

The implementation was pushed successfully.

---

## 18.5 ECOM-006 Git Workflow

```text
develop
   |
   v
git pull origin develop
   |
   v
feature/ECOM-006-payment-service
   |
   v
Create Payment Service
   |
   v
File-by-File Validation
   |
   v
mvn clean test
   |
   v
14/14 Tests Passed
   |
   v
git diff
   |
   v
git diff --cached
   |
   v
git commit
   |
   v
git push
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
   |
   v
git push origin develop
```

---

## 18.6 Standard Commands

Start:

```bash
git switch develop
git pull origin develop
```

Create feature branch:

```bash
git switch -c feature/ECOM-006-payment-service
```

Check:

```bash
git status
git diff
```

Stage:

```bash
git add .gitignore pom.xml src/
```

Review:

```bash
git status --short
git diff --cached
git diff --cached --stat
```

Commit:

```bash
git commit -m "ECOM-006: implement payment service"
```

Push:

```bash
git push -u origin feature/ECOM-006-payment-service
```

After documentation:

```bash
git add docs/jira/ECOM-006/README.md
git commit -m "ECOM-006: add payment service documentation"
git push
```

Merge:

```bash
git switch develop
git pull origin develop

git merge --no-ff feature/ECOM-006-payment-service \
  -m "Merge ECOM-006 payment service into develop"

git push origin develop
```

---

## 18.7 Why Jira-Based Commits?

Instead of:

```text
fix code
changes
payment work
final
```

we use:

```text
ECOM-006: implement payment service
```

This provides traceability:

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
Documentation
  |
  v
Deployment
```

A manager or engineer can trace the implementation back to the business requirement.

---

## 18.8 Why Review Before Commit?

Before committing:

```bash
git status
git diff
git diff --cached
```

We verify:

```text
Correct files
Correct changes
No secrets
No target/
No logs
No temporary files
No accidental modifications
```

This is especially important in enterprise repositories.

---

## 18.9 Why `--no-ff` for Merge?

```bash
git merge --no-ff feature/ECOM-006-payment-service
```

This preserves a visible merge commit.

The history clearly shows that a Jira feature was integrated:

```text
*   Merge ECOM-006 payment service into develop
|\
| * ECOM-006 documentation
| * ECOM-006 implementation
|/
*   Previous develop commit
```

This improves historical traceability.

---

## 18.10 PR Strategy

For this training workflow, we do not create an unnecessary PR for every individual Jira ticket.

The workflow is:

```text
Jira Ticket
    |
    v
Feature Branch
    |
    v
Implementation
    |
    v
Validation
    |
    v
Documentation
    |
    v
Merge
```

In a real enterprise environment, PRs may be required by branch protection and organizational policy.

For major milestones, security-sensitive changes, infrastructure changes, or production releases, formal review and approval should be used according to team policy.

---

# Final ECOM-006 Summary

```text
Jira
ECOM-006
   |
   v
Payment Service
   |
   +-- Port: 8085
   |
   +-- PostgreSQL: shopsphere_payment
   |
   +-- REST APIs
   |
   +-- DTO Validation
   |
   +-- JPA Repository
   |
   +-- Unit Tests
   |
   +-- Controller Tests
   |
   v
14/14 Tests Passed
   |
   v
BUILD SUCCESS
   |
   v
Git Commit
fd01eae
   |
   v
Feature Branch Pushed
feature/ECOM-006-payment-service
```

## Enterprise DevOps Journey After ECOM-006

```text
ECOM-002  User Service
     |
ECOM-003  Product Service
     |
ECOM-004  Cart Service
     |
ECOM-005  Order Service
     |
ECOM-006  Payment Service
     |
     v
Docker
     |
     v
Jenkins CI/CD
     |
     v
Security Scanning
     |
     v
Amazon ECR
     |
     v
Terraform
     |
     v
AWS Infrastructure
     |
     v
Amazon EKS
     |
     v
Kubernetes
     |
     v
Helm
     |
     v
GitOps
     |
     v
Prometheus + Grafana
     |
     v
Loki + Centralized Logging
     |
     v
Alerting
     |
     v
Incident Management
     |
     v
Production Troubleshooting
     |
     v
Rollback
```

**ECOM-006 Payment Service implementation is complete, tested, committed, and pushed.**
