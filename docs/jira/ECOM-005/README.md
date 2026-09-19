# ECOM-005 — ShopSphere Order Service

## Enterprise Implementation & Git Strategy Documentation

**Project:** ShopSphere — Enterprise E-Commerce Platform
**Jira Ticket:** ECOM-005
**Service:** Order Service
**Service Port:** 8084
**Database:** PostgreSQL — `shopsphere_order`
**Technology:** Java 17, Spring Boot 3.5.5, Maven, Spring Data JPA, PostgreSQL
**Git Branch:** `feature/ECOM-005-order-service`

---

# 1. Business Requirement

ShopSphere needs an **Order Service** to manage customer orders after a customer completes the shopping/cart process.

The Order Service is responsible for:

* Creating orders
* Retrieving an order by ID
* Retrieving all orders for a user
* Updating order status
* Deleting orders
* Maintaining order creation and update timestamps
* Persisting order information in its own database

The service must expose REST APIs so that other ShopSphere services can communicate with it.

---

# 2. Why the Business Needs It

In an e-commerce platform, order management is a core business capability.

A customer may:

1. Select products.
2. Add products to the cart.
3. Proceed to checkout.
4. Create an order.
5. Make or initiate payment.
6. Track the order status.

The Order Service provides the backend capability for steps related to order creation and lifecycle management.

Without a dedicated Order Service, order-related functionality would become tightly coupled with other services.

---

# 3. Real-Time Enterprise Scenario

Consider a customer purchasing a laptop and accessories.

The high-level business flow is:

```text
Customer
   |
   v
Frontend
   |
   v
Cart Service
   |
   | Checkout
   v
Order Service
   |
   | Create Order
   v
Order Database
   |
   v
Payment Service
   |
   v
Order Status Update
```

Example:

```text
User ID:       101
Total Amount:  85000.00
Initial Status: CREATED
```

The Order Service creates an order such as:

```text
Order ID: 5001
User ID: 101
Total Amount: 85000.00
Status: CREATED
Created At: <timestamp>
Updated At: <timestamp>
```

Later, the order status can change:

```text
CREATED
   |
   v
PAYMENT_PENDING
   |
   v
PAID
   |
   v
CONFIRMED
   |
   v
SHIPPED
   |
   v
DELIVERED
```

The current implementation keeps the status as a simple string so that the DevOps project remains focused on deployment, automation, infrastructure, monitoring, and troubleshooting.

---

# 4. Architecture

## 4.1 ShopSphere Service Architecture

```text
                         Internet
                            |
                            v
                     +-------------+
                     |  Frontend   |
                     +-------------+
                            |
                            v
                 +----------------------+
                 | API / Load Balancer  |
                 +----------------------+
                            |
          +-----------------+------------------+
          |                 |                  |
          v                 v                  v
   User Service      Product Service      Cart Service
      :8081               :8082              :8083
                                                |
                                                v
                                         Order Service
                                            :8084
                                                |
                                                v
                                         Payment Service
                                            :8085
```

---

# 5. Order Service Architecture

```text
                    Order Service
                       :8084
                         |
             +-----------+-----------+
             |                       |
             v                       v
       REST Controller          Service Layer
             |                       |
             +-----------+-----------+
                         |
                         v
                   Repository
                         |
                         v
                  PostgreSQL DB
                 shopsphere_order
```

---

# 6. Layered Architecture

The Order Service follows a simple Spring Boot layered architecture.

```text
HTTP Request
     |
     v
+----------------------+
| OrderController      |
+----------------------+
     |
     v
+----------------------+
| OrderService         |
+----------------------+
     |
     v
+----------------------+
| OrderRepository      |
+----------------------+
     |
     v
+----------------------+
| PostgreSQL            |
+----------------------+
```

## Responsibilities

### Controller

Handles:

* HTTP requests
* URL mappings
* Request parameters
* Path variables
* HTTP responses

### Service

Handles:

* Business operations
* Order creation
* Order retrieval
* Status updates
* Deletion

### Repository

Handles:

* Database access
* CRUD operations
* User-based order lookup

### Entity

Represents the database order record.

---

# 7. Repository Structure

```text
ShopSphere-E-Commerce-Platform/
│
├── application/
│   └── services/
│       └── order-service/
│           │
│           ├── pom.xml
│           ├── .gitignore
│           │
│           └── src/
│               ├── main/
│               │   ├── java/
│               │   │   └── com/
│               │   │       └── shopsphere/
│               │   │           └── orderservice/
│               │   │               │
│               │   │               ├── OrderServiceApplication.java
│               │   │               │
│               │   │               ├── controller/
│               │   │               │   └── OrderController.java
│               │   │               │
│               │   │               ├── entity/
│               │   │               │   └── Order.java
│               │   │               │
│               │   │               ├── exception/
│               │   │               │   └── GlobalExceptionHandler.java
│               │   │               │
│               │   │               ├── repository/
│               │   │               │   └── OrderRepository.java
│               │   │               │
│               │   │               └── service/
│               │   │                   └── OrderService.java
│               │   │
│               │   └── resources/
│               │       └── application.yml
│               │
│               └── test/
│                   └── java/
│                       └── com/
│                           └── shopsphere/
│                               └── orderservice/
│                                   ├── controller/
│                                   │   └── OrderControllerTest.java
│                                   │
│                                   └── service/
│                                       └── OrderServiceTest.java
│
└── docs/
    └── jira/
        └── ECOM-005/
            └── README.md
```

---

# 8. Files Created / Modified

| File                           | Path                                            | Status  | Purpose                 | Enterprise Benefit         |
| ------------------------------ | ----------------------------------------------- | ------- | ----------------------- | -------------------------- |
| `pom.xml`                      | `application/services/order-service/pom.xml`    | Created | Maven configuration     | Standardized build         |
| `OrderServiceApplication.java` | `src/main/java/...`                             | Created | Spring Boot entry point | Starts service             |
| `application.yml`              | `src/main/resources/application.yml`            | Created | Runtime configuration   | Externalized configuration |
| `Order.java`                   | `entity/Order.java`                             | Created | Order entity            | Database model             |
| `OrderRepository.java`         | `repository/OrderRepository.java`               | Created | Database operations     | Persistence abstraction    |
| `OrderService.java`            | `service/OrderService.java`                     | Created | Business operations     | Separation of concerns     |
| `OrderController.java`         | `controller/OrderController.java`               | Created | REST APIs               | Service exposure           |
| `GlobalExceptionHandler.java`  | `exception/GlobalExceptionHandler.java`         | Created | Exception handling      | Consistent error response  |
| `OrderServiceTest.java`        | `test/.../service`                              | Created | Service tests           | Business validation        |
| `OrderControllerTest.java`     | `test/.../controller`                           | Created | API tests               | Controller validation      |
| `.gitignore`                   | `application/services/order-service/.gitignore` | Created | Ignore Maven artifacts  | Clean Git repository       |

---

# 9. Maven Configuration

## `pom.xml`

The service uses:

```text
Spring Boot       3.5.5
Java              17
Maven
Spring Web
Spring Data JPA
Spring Validation
PostgreSQL
Spring Boot Test
```

Important dependencies:

```xml
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-validation
postgresql
spring-boot-starter-test
```

## Why Maven?

Maven provides:

* Dependency management
* Compilation
* Testing
* Packaging
* Standard project lifecycle
* CI/CD integration

Enterprise CI/CD can execute:

```bash
mvn clean test
```

and later:

```bash
mvn clean package
```

---

# 10. Java Version

The project requires:

```text
Java 17
```

The environment was explicitly configured to use Java 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Verify:

```bash
java -version
mvn -version
```

Both should report Java 17.

---

# 11. Application Configuration

File:

```text
application/services/order-service/src/main/resources/application.yml
```

Configuration:

```yaml
spring:
  application:
    name: order-service

  datasource:
    url: jdbc:postgresql://localhost:5432/shopsphere_order
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
  port: 8084
```

---

# 12. YAML Explanation

## Application Name

```yaml
spring:
  application:
    name: order-service
```

This identifies the Spring Boot application.

It is useful for:

* Logs
* Monitoring
* Service identification
* Distributed systems

---

## Database URL

```yaml
url: jdbc:postgresql://localhost:5432/shopsphere_order
```

Breakdown:

```text
jdbc
 |
 +-- PostgreSQL
       |
       +-- localhost
       |
       +-- port 5432
       |
       +-- database shopsphere_order
```

In production, `localhost` would normally be replaced by an RDS endpoint or another managed database endpoint.

---

## Hibernate Configuration

```yaml
ddl-auto: update
```

For this development implementation, Hibernate can automatically update the schema.

For production, schema management should normally use controlled migration tooling such as Flyway or Liquibase rather than relying on automatic schema updates.

---

## Server Port

```yaml
server:
  port: 8084
```

The Order Service runs on:

```text
http://localhost:8084
```

---

# 13. Order Entity

File:

```text
entity/Order.java
```

The entity contains:

```text
id
userId
totalAmount
status
createdAt
updatedAt
```

Database mapping:

```text
Order Java Object
       |
       v
orders table
       |
       +-- id
       +-- user_id
       +-- total_amount
       +-- status
       +-- created_at
       +-- updated_at
```

---

# 14. Important Design Decision — User ID

The Order entity contains:

```java
private Long userId;
```

The Order Service does **not** directly maintain a foreign-key relationship to the User Service database.

This follows the microservice principle:

```text
User Service
    |
    | owns user data
    v
User Database


Order Service
    |
    | owns order data
    v
Order Database
```

The Order Service stores the user's identifier.

Cross-service validation can later happen through:

```text
Order Service
      |
      v
User Service API
```

rather than direct database access.

This prevents tight database coupling between services.

---

# 15. Order Repository

File:

```text
repository/OrderRepository.java
```

It extends:

```java
JpaRepository<Order, Long>
```

This automatically provides standard operations such as:

```text
save()
findById()
findAll()
delete()
deleteById()
existsById()
```

The project also defines:

```java
List<Order> findByUserId(Long userId);
```

Spring Data JPA derives the query from the method name.

Conceptually:

```text
findByUserId(101)
       |
       v
SELECT orders
WHERE user_id = 101
```

---

# 16. Order Service

File:

```text
service/OrderService.java
```

The service contains the core operations.

## Create Order

```java
public Order createOrder(Long userId, BigDecimal totalAmount)
```

Flow:

```text
Request
   |
   v
Controller
   |
   v
OrderService
   |
   v
new Order()
   |
   v
Repository.save()
   |
   v
Database
```

---

## Get Order

```java
public Order getOrder(Long orderId)
```

It retrieves an order by ID.

If the order does not exist:

```text
Order not found
```

is raised.

---

## Get Orders by User

```java
public List<Order> getOrdersByUser(Long userId)
```

This allows the application to retrieve all orders belonging to a user.

Example:

```text
GET /api/orders/user/101
```

---

## Update Status

```java
public Order updateOrderStatus(Long orderId, String status)
```

Flow:

```text
Order ID
   |
   v
Find Order
   |
   v
Update Status
   |
   v
Update Timestamp
   |
   v
Save
```

---

## Delete Order

```java
public void deleteOrder(Long orderId)
```

The service first retrieves the order and then deletes it.

---

# 17. REST APIs

Base path:

```text
/api/orders
```

## Create Order

```http
POST /api/orders?userId=101&totalAmount=85000
```

Example:

```bash
curl -X POST \
"http://localhost:8084/api/orders?userId=101&totalAmount=85000"
```

---

## Get Order

```http
GET /api/orders/{orderId}
```

Example:

```bash
curl http://localhost:8084/api/orders/1
```

---

## Get User Orders

```http
GET /api/orders/user/{userId}
```

Example:

```bash
curl http://localhost:8084/api/orders/user/101
```

---

## Update Order Status

```http
PUT /api/orders/{orderId}/status?status=PAID
```

Example:

```bash
curl -X PUT \
"http://localhost:8084/api/orders/1/status?status=PAID"
```

---

## Delete Order

```http
DELETE /api/orders/{orderId}
```

Example:

```bash
curl -X DELETE \
http://localhost:8084/api/orders/1
```

---

# 18. REST API Flow

```text
Client
  |
  | HTTP Request
  v
OrderController
  |
  | method call
  v
OrderService
  |
  | repository call
  v
OrderRepository
  |
  | SQL
  v
PostgreSQL
```

Response travels back:

```text
PostgreSQL
   |
   v
Repository
   |
   v
Service
   |
   v
Controller
   |
   v
HTTP Response
```

---

# 19. Exception Handling

File:

```text
exception/GlobalExceptionHandler.java
```

The application uses:

```java
@RestControllerAdvice
```

This provides centralized exception handling.

Current implementation handles:

```java
RuntimeException.class
```

and returns:

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found: 10"
}
```

This is intentionally simple for the current project scope.

A production implementation would normally distinguish errors such as:

```text
400 Bad Request
404 Not Found
409 Conflict
422 Unprocessable Entity
500 Internal Server Error
```

---

# 20. Testing Strategy

The Order Service contains two test layers.

```text
                 Tests
                   |
          +--------+--------+
          |                 |
          v                 v
   Service Tests      Controller Tests
       6 tests             5 tests
          |                 |
          +--------+--------+
                   |
                   v
                11 tests
```

---

# 21. Service Tests

File:

```text
OrderServiceTest.java
```

Six tests validate:

1. Order creation
2. Order retrieval
3. Exception when order does not exist
4. Orders retrieved by user
5. Order status update
6. Order deletion

Validation result:

```text
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0
```

---

# 22. Controller Tests

File:

```text
OrderControllerTest.java
```

Five tests validate:

1. Create order API
2. Get order API
3. Get user orders API
4. Update order status API
5. Delete order API

Validation result:

```text
Tests run: 5
Failures: 0
Errors: 0
Skipped: 0
```

---

# 23. Final Maven Validation

The final command was executed from:

```text
application/services/order-service
```

Command:

```bash
mvn clean test
```

Result:

```text
Tests run: 11
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Total validation:

```text
Service tests       6
Controller tests    5
---------------------
Total              11
Passed              11
Failed               0
Errors               0
Skipped              0
```

---

# 24. Git Strategy for ECOM-005

## 24.1 Why Git Strategy Matters

In an enterprise environment, developers and DevOps engineers normally do not make changes directly on `main`.

A controlled Git workflow provides:

* Jira traceability
* Code isolation
* Parallel development
* Reviewability
* Controlled integration
* Rollback capability
* Release management

---

# 25. ShopSphere Branching Strategy

The project uses:

```text
main
  |
  v
develop
  |
  +-------------------------------+
  |               |               |
  v               v               v
ECOM-003       ECOM-004        ECOM-005
feature        feature         feature
```

More specifically:

```text
main
  |
  └── develop
        |
        ├── feature/ECOM-003-product-service
        |
        ├── feature/ECOM-004-cart-service
        |
        └── feature/ECOM-005-order-service
```

---

# 26. `main` Branch

`main` represents the stable/release branch.

Typical enterprise purpose:

```text
main
 |
 +-- Production-ready code
 +-- Release baseline
 +-- Stable version
```

Feature development should not normally happen directly on `main`.

---

# 27. `develop` Branch

`develop` is the integration branch for ongoing development.

The current ShopSphere strategy is:

```text
main
  |
  v
develop
  |
  +-- feature branches
```

Developers start their Jira feature branches from the latest `develop`.

---

# 28. Feature Branch

For ECOM-005:

```text
feature/ECOM-005-order-service
```

The branch name directly maps the Git work to Jira.

This gives immediate traceability:

```text
Jira
 |
 | ECOM-005
 v
Git Branch
 |
 | feature/ECOM-005-order-service
 v
Code
```

---

# 29. Creating the ECOM-005 Branch

The recommended enterprise workflow is:

```bash
git switch develop
git pull origin develop
git switch -c feature/ECOM-005-order-service
```

Meaning:

### `git switch develop`

Move to the integration branch.

### `git pull origin develop`

Get the latest remote changes.

### `git switch -c`

Create a new feature branch.

---

# 30. Why Create From `develop`?

Suppose:

```text
develop
   |
   +-- ECOM-003
   |
   +-- ECOM-004
```

ECOM-005 should start from the latest integration point.

```text
latest develop
      |
      v
feature/ECOM-005-order-service
```

This reduces the possibility of developing against stale code.

---

# 31. File-by-File Implementation Strategy

For ECOM-005, implementation was intentionally performed incrementally.

Typical order:

```text
1. pom.xml
      |
2. application.yml
      |
3. Main Application
      |
4. Entity
      |
5. Repository
      |
6. Service
      |
7. Controller
      |
8. Exception Handler
      |
9. Service Tests
      |
10. Controller Tests
      |
11. .gitignore
      |
12. Validation
```

This approach makes troubleshooting easier because each layer can be understood independently.

---

# 32. Jira → Git → Validation Flow

The complete enterprise flow is:

```text
Jira ECOM-005
      |
      v
Create feature branch
      |
      v
Implement Order Service
      |
      v
Run tests
      |
      v
Review Git changes
      |
      v
Commit
      |
      v
Push feature branch
      |
      v
Documentation
      |
      v
Final validation
      |
      v
Merge into develop
```

---

# 33. ECOM-005 Actual Git History

The implementation commit:

```text
231076f ECOM-005: implement order service
```

During validation, Maven-generated `target/` artifacts were discovered in the Git history.

These artifacts should not be committed.

A cleanup was therefore performed.

Cleanup commit:

```text
da605bb ECOM-005: remove build artifacts
```

Final feature history:

```text
da605bb ECOM-005: remove build artifacts
231076f ECOM-005: implement order service
```

This is an important real-world Git lesson:

> Always inspect what is being committed, especially after running build tools.

---

# 34. Why `target/` Must Not Be Committed

Maven creates:

```text
target/
```

containing generated files such as:

```text
.class
.xml
.txt
createdFiles.lst
inputFiles.lst
```

These are build outputs.

They should be generated by the build system, not stored in source control.

Correct:

```text
Git
 |
 +-- source code
 +-- pom.xml
 +-- configuration
 +-- tests
 +-- documentation
```

Incorrect:

```text
Git
 |
 +-- source code
 +-- pom.xml
 +-- target/
       |
       +-- .class
       +-- test reports
       +-- generated files
```

---

# 35. `.gitignore`

File:

```text
application/services/order-service/.gitignore
```

Contents:

```gitignore
target/
*.log
```

This prevents:

```text
target/
*.log
```

from being accidentally added to Git.

---

# 36. Real-Time Git Incident — Build Artifacts

During ECOM-005 implementation, the build artifacts had initially been added to Git.

The issue was identified after reviewing the repository state.

The cleanup process was:

```bash
git rm -r --cached application/services/order-service/target
```

Important:

```text
--cached
```

means:

```text
Remove from Git index
but keep local files
```

This was important because Maven can continue generating the files locally.

Then `.gitignore` was added:

```gitignore
target/
*.log
```

Then:

```bash
git add application/services/order-service/.gitignore
```

The cleanup was committed:

```bash
git commit -m "ECOM-005: remove build artifacts"
```

---

# 37. Why We Did Not Delete `target/` From the Local Machine

The Maven `target/` directory is useful locally because it contains build output.

The requirement was:

```text
Remove target/ from Git
```

not:

```text
Remove target/ from the local development environment
```

Therefore:

```text
Git repository       → target removed
Developer machine    → target can remain
```

---

# 38. Verifying Git Is No Longer Tracking `target/`

Command:

```bash
git ls-files application/services/order-service/target
```

Expected result:

```text
<no output>
```

Actual validation produced no output.

Therefore:

```text
target/
```

is no longer tracked by Git.

---

# 39. Git Working Tree Validation

Command:

```bash
git status
```

Final result:

```text
On branch feature/ECOM-005-order-service
Your branch is up to date with
'origin/feature/ECOM-005-order-service'.

nothing to commit, working tree clean
```

This confirms:

```text
Working tree      CLEAN
Local branch      synchronized
Remote branch     synchronized
Uncommitted work  NONE
```

---

# 40. Commit Strategy

The project uses:

```text
<JIRA-ID>: <short description>
```

Examples:

```text
ECOM-005: implement order service
ECOM-005: remove build artifacts
```

Benefits:

* Easy Jira traceability
* Easy Git history search
* Easy troubleshooting
* Clear change ownership
* Better release notes

---

# 41. Useful Git Commands

## Check branch

```bash
git branch --show-current
```

Expected:

```text
feature/ECOM-005-order-service
```

---

## Check status

```bash
git status
```

Used to identify:

* Modified files
* Untracked files
* Staged files
* Current branch
* Remote synchronization

---

## Review changes

```bash
git diff
```

Shows unstaged changes.

---

## Review staged changes

```bash
git diff --cached
```

Shows what will be committed.

---

## Review commit history

```bash
git log --oneline
```

Example:

```text
da605bb ECOM-005: remove build artifacts
231076f ECOM-005: implement order service
```

---

## Push branch

```bash
git push
```

Because the upstream branch is already configured, Git pushes the current branch to:

```text
origin/feature/ECOM-005-order-service
```

---

# 42. Why We Do Not Create a New `develop` Branch for Every Jira Ticket

Incorrect approach:

```text
ECOM-003 → develop-003
ECOM-004 → develop-004
ECOM-005 → develop-005
```

This creates unnecessary branch management.

Recommended project strategy:

```text
main
 |
 v
develop
 |
 +-- feature/ECOM-003
 +-- feature/ECOM-004
 +-- feature/ECOM-005
```

`develop` is a permanent integration branch.

---

# 43. Merge Strategy

After:

```text
Implementation
+
Testing
+
Git cleanup
+
Documentation
```

the feature branch can be merged into `develop`.

Recommended merge:

```bash
git switch develop
git pull origin develop
git merge --no-ff feature/ECOM-005-order-service \
  -m "Merge ECOM-005 order service into develop"
```

Then:

```bash
git push origin develop
```

The merge should only happen after the feature branch is validated.

---

# 44. Why `--no-ff`?

The project uses a merge commit for feature integration.

Example:

```text
                 feature/ECOM-005
                       |
                       v
develop ---------------M
```

The merge commit preserves the fact that a Jira feature was integrated.

This makes project history easier to understand.

---

# 45. Pre-Merge Checklist

Before merging ECOM-005:

```text
[ ] Jira requirement completed
[ ] Feature branch correct
[ ] Implementation completed
[ ] Unit tests passed
[ ] Controller tests passed
[ ] Maven build successful
[ ] target/ not tracked
[ ] .gitignore exists
[ ] Git working tree clean
[ ] Feature branch pushed
[ ] Documentation completed
```

Current implementation status:

```text
Jira requirement        ✅
Feature branch          ✅
Implementation          ✅
Unit tests              ✅
Controller tests        ✅
Maven build             ✅
target cleanup          ✅
.gitignore              ✅
Feature branch pushed   ✅
Working tree clean      ✅
Documentation           This document
Merge                   Next integration step
```

---

# 46. CI/CD Integration

Later, Jenkins can automatically validate the feature branch.

Example pipeline:

```text
Developer Push
      |
      v
GitHub
      |
      v
Jenkins
      |
      +--> Checkout
      |
      +--> Maven Test
      |
      +--> Security Scan
      |
      +--> Docker Build
      |
      +--> Image Scan
      |
      +--> Push Image to ECR
      |
      +--> Deploy to EKS
      |
      v
Validation
```

For the current implementation, the critical build stage is:

```bash
mvn clean test
```

---

# 47. Future Containerization

The current ticket focuses on the Order Service application.

Later, Docker can package it:

```text
Order Service
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
Amazon ECR
```

Example future image:

```text
<aws-account>.dkr.ecr.<region>.amazonaws.com/shopsphere/order-service:<version>
```

---

# 48. Future Kubernetes Deployment

The future deployment model is:

```text
Developer
   |
   v
GitHub
   |
   v
Jenkins
   |
   v
Amazon ECR
   |
   v
Amazon EKS
   |
   v
Order Service Pods
   |
   v
PostgreSQL / Amazon RDS
```

The service port remains:

```text
8084
```

Kubernetes Service and Deployment configuration will be handled in a later DevOps implementation ticket.

---

# 49. Terraform Responsibility

Terraform will eventually manage infrastructure such as:

```text
VPC
 |
 +-- Public Subnets
 +-- Private Subnets
 +-- Route Tables
 +-- Internet Gateway
 +-- NAT
 |
 +-- Security Groups
 |
 +-- IAM
 |
 +-- ECR
 |
 +-- EKS
 |
 +-- RDS
```

Application source code should not create AWS infrastructure directly.

---

# 50. Database Ownership

ShopSphere follows service ownership.

```text
User Service
    |
    +-- User data

Product Service
    |
    +-- Product data

Cart Service
    |
    +-- Cart data

Order Service
    |
    +-- Order data

Payment Service
    |
    +-- Payment data
```

The Order Service owns:

```text
shopsphere_order
```

Other services should not directly modify the Order Service database.

---

# 51. Development vs Production Configuration

Current development:

```text
localhost:5432
shopsphere_order
username: shopsphere
password: shopsphere
```

Production should not hard-code credentials.

A production architecture should use mechanisms such as:

```text
AWS Secrets Manager
        |
        v
Kubernetes Secret / External Secret
        |
        v
Order Service
        |
        v
Amazon RDS PostgreSQL
```

---

# 52. Troubleshooting & RCA

## Issue 1 — Maven Says No POM

Error:

```text
The goal you specified requires a project to execute
but there is no POM in this directory
```

Cause:

Maven was executed from:

```text
ShopSphere-E-Commerce-Platform/
```

but the Maven project is located at:

```text
application/services/order-service/
```

Correct:

```bash
cd application/services/order-service
mvn clean test
```

---

# 53. Issue 2 — Java Version Mismatch

Potential error:

```text
release version 17 not supported
```

Cause:

Maven may be using a different Java version.

Check:

```bash
java -version
mvn -version
```

Set Java 17:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Then:

```bash
mvn clean test
```

---

# 54. Issue 3 — Build Artifacts Tracked by Git

Symptoms:

```text
target/classes/
target/test-classes/
target/surefire-reports/
```

appear in Git.

Cause:

Maven-generated files were staged before `.gitignore` was configured.

Resolution:

```bash
git rm -r --cached application/services/order-service/target
```

Create:

```gitignore
target/
*.log
```

Then commit the cleanup.

---

# 55. Issue 4 — PostgreSQL Connection Failure

Possible error:

```text
Connection refused
```

Check PostgreSQL:

```bash
systemctl status postgresql
```

or if using Docker:

```bash
docker ps
```

Check port:

```bash
ss -lntp | grep 5432
```

Check database:

```text
shopsphere_order
```

Check application configuration:

```text
host
port
database
username
password
```

---

# 56. Issue 5 — Port 8084 Already in Use

Check:

```bash
ss -lntp | grep 8084
```

or:

```bash
lsof -i :8084
```

Identify the process before stopping anything.

Do not blindly kill processes in a shared environment.

---

# 57. RCA Approach

Enterprise troubleshooting should follow:

```text
Problem
   |
   v
Collect Evidence
   |
   v
Check Logs
   |
   v
Check Configuration
   |
   v
Check Dependencies
   |
   v
Identify Root Cause
   |
   v
Fix
   |
   v
Validate
   |
   v
Document RCA
```

Avoid immediately changing multiple things because that makes root-cause analysis difficult.

---

# 58. Rollback Procedure

## Git Rollback

If ECOM-005 must be removed before integration:

```bash
git switch develop
git pull origin develop
```

If the feature has not been merged, simply keep the feature branch isolated.

If already merged, use the team's approved revert process rather than rewriting shared history.

---

# 59. Why `git revert` Is Preferred for Shared History

For a shared branch:

```text
develop
```

avoid rewriting published history with commands such as:

```bash
git reset --hard
git push --force
```

unless explicitly approved.

A safer approach is:

```bash
git revert <commit>
```

This creates a new commit that reverses the previous change.

Conceptually:

```text
Original
   |
   v
ECOM-005 change
   |
   v
Revert commit
   |
   v
Previous behavior restored
```

---

# 60. Production Best Practices

For a production-grade Order Service, improve the current implementation with:

### Application

* DTO-based request/response handling
* Proper validation
* Typed exception classes
* Proper HTTP status codes
* Structured logging
* Correlation IDs
* API documentation
* Authentication/authorization

### Database

* Flyway/Liquibase migrations
* Connection pooling
* Indexing
* Backup strategy
* Encryption
* RDS Multi-AZ where required

### Kubernetes

* Readiness probe
* Liveness probe
* Startup probe
* Resource requests/limits
* HPA
* Pod disruption strategy
* SecurityContext
* Network policies

### DevOps

* Jenkins CI/CD
* SonarQube
* Trivy
* Docker
* Amazon ECR
* Amazon EKS
* Terraform
* GitOps
* Prometheus
* Grafana
* Loki

---

# 61. Security Best Practices

Do not commit:

```text
Passwords
AWS access keys
API keys
Private keys
Database credentials
Tokens
```

The current development configuration contains credentials only for the local learning environment.

Production should use:

```text
AWS Secrets Manager
        |
        v
External Secrets / Secret mechanism
        |
        v
Kubernetes
        |
        v
Order Service
```

---

# 62. Observability

Future monitoring should provide:

```text
                    Order Service
                         |
             +-----------+-----------+
             |           |           |
             v           v           v
          Metrics       Logs       Traces
             |           |           |
             v           v           v
        Prometheus      Loki      Tracing
             |
             v
          Grafana
```

Important metrics:

```text
HTTP request count
HTTP error count
HTTP latency
JVM memory
JVM CPU
Database connection pool
Pod CPU
Pod memory
```

---

# 63. Enterprise Deployment Flow

Future complete flow:

```text
Developer
    |
    v
Git Feature Branch
    |
    v
Pull Request / Approved Integration
    |
    v
develop
    |
    v
Jenkins
    |
    +---- Maven Test
    |
    +---- Code Quality
    |
    +---- Security Scan
    |
    +---- Docker Build
    |
    +---- Image Scan
    |
    +---- Push ECR
    |
    v
Deployment
    |
    v
Amazon EKS
    |
    v
Order Service Pods
    |
    v
Amazon RDS
```

---

# 64. Interview Explanation — 7-Year Experience Level

## Question

**How did you implement the Order Service in your ShopSphere project?**

### Answer

I implemented the Order Service as an independent Spring Boot microservice.

The service runs on port 8084 and owns its order data in a separate logical PostgreSQL database.

I followed a layered architecture with Controller, Service, Repository, and Entity layers.

The service exposes REST APIs for creating orders, retrieving orders, retrieving orders by user, updating order status, and deleting orders.

I also added service-level and controller-level tests. The final Maven validation completed with 11 tests and zero failures.

From the DevOps perspective, I maintained the implementation in a Jira-based feature branch named `feature/ECOM-005-order-service`, used Jira-based commit messages, validated the build, removed generated Maven artifacts from Git, and added a `.gitignore`.

The feature branch was pushed to GitHub and is ready for integration into `develop`.

---

# 65. Interview Question — Git Strategy

## Question

**What Git branching strategy did you use?**

### Answer

We use `main` as the stable branch and `develop` as the integration branch.

For each Jira ticket, we create a feature branch from the latest `develop`.

For example:

```text
feature/ECOM-005-order-service
```

All implementation work is done on that feature branch.

After testing, documentation, and validation are completed, the feature is integrated into `develop`.

This gives us Jira traceability, isolated development, controlled integration, and a clean release path toward `main`.

---

# 66. Interview Question — Why Feature Branches?

### Answer

Feature branches isolate changes related to a specific Jira requirement.

For example:

```text
ECOM-005
   |
   v
feature/ECOM-005-order-service
```

This prevents unfinished work from directly affecting the integration or stable branch.

It also makes troubleshooting and code review easier because the branch has a clear business purpose.

---

# 67. Interview Question — Why Was `target/` Removed?

### Answer

`target/` contains Maven-generated build artifacts.

These files are environment-generated and should not be stored in source control.

During ECOM-005, the generated artifacts were identified in Git and removed using:

```bash
git rm -r --cached application/services/order-service/target
```

I then added:

```gitignore
target/
*.log
```

and committed the cleanup separately.

This keeps the repository clean while allowing Maven to regenerate build artifacts during CI/CD.

---

# 68. Interview Question — `git rm --cached` vs `rm`

### Answer

`git rm --cached` removes a file from Git tracking but keeps the file locally.

For example:

```bash
git rm -r --cached target
```

means:

```text
Git tracking → removed
Local files  → retained
```

A normal filesystem deletion would remove the files from the local machine as well.

---

# 69. Interview Question — How Do You Validate Before Merge?

### Answer

I use multiple validation levels.

First, application tests:

```bash
mvn clean test
```

Then Git validation:

```bash
git status
git diff
git log --oneline
```

Then I verify generated artifacts are not tracked:

```bash
git ls-files application/services/order-service/target
```

Finally, I confirm the feature branch is synchronized with its remote branch.

Only after those checks do I proceed with integration.

---

# 70. Interview Question — How Do You Handle a Failed Build?

### Answer

I don't immediately change multiple things.

I first identify:

```text
Which stage failed?
What is the exact error?
Which environment is being used?
Which Java/Maven version is active?
Are dependencies available?
Is the database available?
Is the configuration correct?
```

Then I make the smallest required change, rerun the relevant validation, and finally run the complete build again.

---

# 71. Interview Question — How Do Microservices Own Data?

### Answer

Each microservice owns its business data.

For example:

```text
User Service  → User data
Product       → Product data
Cart          → Cart data
Order         → Order data
Payment       → Payment data
```

Services communicate through APIs or messaging rather than directly modifying another service's database.

This reduces coupling and allows services to evolve independently.

---

# 72. Interview Question — What Would You Improve for Production?

### Answer

The current implementation is intentionally simple for the DevOps project scope.

For production, I would add stronger request/response DTOs, validation, typed exceptions, database migration tooling, authentication and authorization, structured logging, distributed tracing, health probes, resource limits, autoscaling, centralized secrets management, and stronger observability.

On the DevOps side, I would integrate Jenkins, security scanning, Docker, ECR, EKS, Terraform, GitOps, Prometheus, Grafana, and centralized logging.

---

# 73. Review Questions

1. Why is Order Service separated from Cart Service?
2. What is the responsibility of `OrderController`?
3. Why do we need `OrderService`?
4. What does `JpaRepository` provide?
5. Why is `userId` stored instead of directly referencing User Service's database?
6. Why is port 8084 used?
7. Why should `target/` not be committed?
8. What does `.gitignore` do?
9. What does `git rm --cached` do?
10. Why use Jira IDs in branch names?
11. Why use Jira IDs in commit messages?
12. Why is `develop` used as an integration branch?
13. Why should feature branches start from the latest `develop`?
14. Why should we validate before merging?
15. What is the difference between `git revert` and `git reset`?
16. How would you troubleshoot a PostgreSQL connection failure?
17. How would you troubleshoot port 8084?
18. How would Jenkins validate the Order Service?
19. How would you deploy the service to EKS?
20. How would you manage production database credentials?

---

# 74. ECOM-005 Final Status

```text
==================================================
              ECOM-005 STATUS
==================================================

Jira                         ECOM-005
Service                      Order Service
Port                         8084
Database                     shopsphere_order

Implementation               COMPLETE
Service Tests                6/6 PASS
Controller Tests             5/5 PASS
Total Tests                  11/11 PASS
Maven Build                  SUCCESS

Feature Branch               feature/ECOM-005-order-service

Implementation Commit        231076f
Cleanup Commit               da605bb

Build Artifacts              REMOVED
.gitignore                   ADDED
Remote Feature Branch        PUSHED
Working Tree                 CLEAN

Documentation                COMPLETE
Integration into develop     NEXT STEP
==================================================
```

---

# 75. Actual Git Flow for ECOM-005

```text
                         Jira
                          |
                          | ECOM-005
                          v
                       develop
                          |
                          | create feature
                          v
              feature/ECOM-005-order-service
                          |
              +-----------+-----------+
              |                       |
              v                       v
       Implementation             Testing
              |                       |
              +-----------+-----------+
                          |
                          v
                       Commit
                          |
                          v
              231076f ECOM-005:
              implement order service
                          |
                          v
                   Git validation
                          |
                          v
                 target/ discovered
                          |
                          v
                    .gitignore
                          |
                          v
              da605bb ECOM-005:
              remove build artifacts
                          |
                          v
                     GitHub
                          |
                          v
                    Final validation
                          |
                          v
                    develop
                          |
                          v
                       main
```

---

# 76. Enterprise Git Mental Model

Think about the workflow like this:

```text
Jira
 |
 | What needs to be done?
 v
Feature Branch
 |
 | Where do I develop it?
 v
Commits
 |
 | What changes did I make?
 v
GitHub
 |
 | Where is the source stored?
 v
CI/CD
 |
 | Does it build and pass tests?
 v
develop
 |
 | Is it ready for integration?
 v
main
 |
 | Is it release-ready?
 v
Production
```

---

# 77. Key Lessons From ECOM-005

The most important practical lessons from this ticket are:

### 1. Always check your working directory

Maven requires a `pom.xml`.

```bash
pwd
ls
```

before running the build.

### 2. Always validate Java and Maven versions

```bash
java -version
mvn -version
```

### 3. Never blindly commit everything

Before:

```bash
git commit
```

review:

```bash
git status
git diff --cached
```

### 4. Build outputs should not be committed

Use:

```gitignore
target/
```

### 5. Jira should map to Git

Use:

```text
ECOM-005
```

in:

```text
branch name
commit message
documentation
```

### 6. Validate before integration

```text
Build
+
Tests
+
Git cleanup
+
Documentation
+
Working tree validation
```

### 7. Keep `develop` as a permanent integration branch

Do not create a separate integration branch for every Jira ticket.

---

# 78. Final Enterprise Summary

ECOM-005 implemented the ShopSphere Order Service as an independent Spring Boot microservice.

The service provides REST APIs for order creation, retrieval, user-based order lookup, status updates, and deletion.

The implementation uses Java 17, Spring Boot, Spring Data JPA, Maven, and PostgreSQL.

The implementation was validated with:

```text
11 tests
0 failures
0 errors
0 skipped
BUILD SUCCESS
```

The implementation was developed using:

```text
feature/ECOM-005-order-service
```

and tracked through Jira-based Git commits:

```text
231076f ECOM-005: implement order service
da605bb ECOM-005: remove build artifacts
```

Generated Maven artifacts were removed from Git and protected through:

```gitignore
target/
*.log
```

The feature branch was pushed successfully and the working tree is clean.

The next integration activity is to merge the validated ECOM-005 feature into the permanent `develop` branch and validate the integrated branch.

---

# 79. One-Minute Interview Summary

> "For ECOM-005, I implemented the Order Service as an independent Spring Boot microservice running on port 8084. It has Controller, Service, Repository, and Entity layers and uses PostgreSQL for persistence. The service provides APIs for order creation, retrieval, user-based order lookup, status updates, and deletion.
>
> From the DevOps side, I followed a Jira-driven Git workflow. I created `feature/ECOM-005-order-service` from the latest `develop`, implemented the service incrementally, added service and controller tests, and validated the application using Maven. The final validation had 11 tests with zero failures.
>
> During Git validation, I identified Maven-generated `target` artifacts in source control. I removed them using `git rm --cached`, added the appropriate `.gitignore`, and created a separate cleanup commit. The feature branch was pushed to GitHub and the working tree is clean.
>
> The feature is now ready for controlled integration into `develop`."

---

# 80. ECOM-005 Completion Checklist

```text
[✓] Jira requirement understood
[✓] Feature branch created
[✓] Maven project created
[✓] Java 17 configured
[✓] Application configuration created
[✓] Order entity created
[✓] Repository created
[✓] Service layer created
[✓] Controller created
[✓] Exception handler created
[✓] Service tests created
[✓] Controller tests created
[✓] 11/11 tests passed
[✓] Maven BUILD SUCCESS
[✓] .gitignore created
[✓] target/ removed from Git tracking
[✓] Cleanup committed
[✓] Feature branch pushed
[✓] Working tree clean
[✓] Git strategy documented
[✓] Enterprise documentation completed
[ ] Merge feature branch into develop
[ ] Validate develop after merge
```

---

## Document Ownership

**Project:** ShopSphere — Enterprise E-Commerce Platform
**Jira:** ECOM-005
**Component:** Order Service
**Documentation Location:**

```text
docs/jira/ECOM-005/README.md
```

**Feature Branch:**

```text
feature/ECOM-005-order-service
```

**Status:** Implementation and validation complete; ready for integration into `develop`.
