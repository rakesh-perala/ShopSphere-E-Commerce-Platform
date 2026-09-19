# ECOM-003 — Product Service

## Enterprise E-Commerce Platform — ShopSphere

**Jira Ticket:** ECOM-003
**Feature:** Product Service
**Repository:** `ShopSphere-E-Commerce-Platform`
**Branch:** `feature/ECOM-003-product-service`
**Implementation Commit:** `ad720c8`
**Commit Message:** `ECOM-003: implement product service`
**Technology:** Java 17, Spring Boot 3.5.5, Maven, Spring Data JPA, PostgreSQL
**Service Port:** `8082`

---

# 1. Business Requirement

ShopSphere requires a dedicated **Product Service** to manage the products available on the e-commerce platform.

The Product Service is responsible for:

* Creating products
* Viewing all products
* Viewing a product by ID
* Searching products by category
* Updating products
* Deleting products
* Validating product input
* Persisting product data
* Providing REST APIs for other services or clients

### Product information

Each product contains:

| Field           | Description               |
| --------------- | ------------------------- |
| `id`            | Unique product identifier |
| `name`          | Product name              |
| `description`   | Product description       |
| `price`         | Product price             |
| `category`      | Product category          |
| `stockQuantity` | Available stock quantity  |

---

# 2. Why the Business Needs It

In an enterprise e-commerce platform, product management should not be tightly coupled with the User, Cart, Order, or Payment services.

The Product Service provides a dedicated ownership boundary.

For example:

```text
Customer
   |
   v
Frontend
   |
   v
API / Load Balancer
   |
   v
Product Service
   |
   v
Product Database
```

The Product Service owns product information.

Other services should not directly modify the Product Service database.

For example:

```text
Order Service
     |
     | Product ID
     v
Product Service
     |
     v
Product Data
```

This follows the **service-owned data principle**.

---

# 3. Real-Time Enterprise Scenario

Consider a customer searching for a laptop.

The frontend sends:

```http
GET /api/products/category/Electronics
```

The request reaches the Product Service.

The flow is:

```text
Customer
   |
   v
Frontend
   |
   v
Load Balancer
   |
   v
Product Service
   |
   v
ProductRepository
   |
   v
PostgreSQL
   |
   v
Product Response
   |
   v
Customer
```

For creating a product:

```text
Admin / Product Management System
              |
              v
       POST /api/products
              |
              v
       ProductController
              |
              v
        ProductService
              |
              v
       ProductRepository
              |
              v
         PostgreSQL
```

---

# 4. Architecture Diagram

```text
                     ShopSphere
                         |
                         |
                 +-------v-------+
                 |    Frontend   |
                 +-------+-------+
                         |
                         |
                   HTTPS Request
                         |
                         v
              +----------------------+
              | Load Balancer / API  |
              +----------+-----------+
                         |
                         v
              +----------------------+
              |   Product Service    |
              |     Port 8082        |
              +----------+-----------+
                         |
              +----------+----------+
              |          |          |
              v          v          v
        Controller    Service    Validation
              |
              v
        ProductRepository
              |
              v
       Spring Data JPA
              |
              v
        PostgreSQL DB
```

---

# 5. Application Layer Architecture

The Product Service follows a simple layered architecture.

```text
HTTP Request
     |
     v
+-------------------+
| ProductController |
+---------+---------+
          |
          v
+-------------------+
|  ProductService   |
+---------+---------+
          |
          v
+-------------------+
| ProductRepository |
+---------+---------+
          |
          v
+-------------------+
|    PostgreSQL     |
+-------------------+
```

Supporting components:

```text
ProductRequest
      |
      v
Validation
      |
      v
ProductController

Product
      |
      v
JPA Entity

ProductResponse
      |
      v
API Response

GlobalExceptionHandler
      |
      v
Error Response
```

---

# 6. Repository Structure

The implemented service has the following structure:

```text
application/
└── services/
    └── product-service/
        ├── .gitignore
        ├── pom.xml
        │
        └── src/
            ├── main/
            │   ├── java/
            │   │   └── com/
            │       └── shopsphere/
            │           └── productservice/
            │               ├── ProductServiceApplication.java
            │               │
            │               ├── controller/
            │               │   └── ProductController.java
            │               │
            │               ├── dto/
            │               │   ├── ProductRequest.java
            │               │   └── ProductResponse.java
            │               │
            │               ├── entity/
            │               │   └── Product.java
            │               │
            │               ├── exception/
            │               │   └── GlobalExceptionHandler.java
            │               │
            │               ├── repository/
            │               │   └── ProductRepository.java
            │               │
            │               └── service/
            │                   └── ProductService.java
            │
            └── resources/
                └── application.yml

            └── test/
                └── java/
                    └── com/
                        └── shopsphere/
                            └── productservice/
                                ├── controller/
                                │   └── ProductControllerTest.java
                                │
                                └── service/
                                    └── ProductServiceTest.java
```

---

# 7. Files Created

ECOM-003 created 13 files.

| File                             | Path                                    | Purpose                                    |
| -------------------------------- | --------------------------------------- | ------------------------------------------ |
| `.gitignore`                     | `application/services/product-service/` | Ignores Maven build output and logs        |
| `pom.xml`                        | `application/services/product-service/` | Maven dependencies and build configuration |
| `ProductServiceApplication.java` | `src/main/java/.../`                    | Spring Boot entry point                    |
| `ProductController.java`         | `controller/`                           | REST API endpoints                         |
| `ProductRequest.java`            | `dto/`                                  | Incoming API request model                 |
| `ProductResponse.java`           | `dto/`                                  | API response model                         |
| `Product.java`                   | `entity/`                               | Product database entity                    |
| `GlobalExceptionHandler.java`    | `exception/`                            | Central runtime exception handling         |
| `ProductRepository.java`         | `repository/`                           | Database access layer                      |
| `ProductService.java`            | `service/`                              | Product business/service operations        |
| `application.yml`                | `src/main/resources/`                   | Application and database configuration     |
| `ProductControllerTest.java`     | `src/test/.../controller/`              | Controller/API tests                       |
| `ProductServiceTest.java`        | `src/test/.../service/`                 | Service-layer tests                        |

---

# 8. Deep Concept Explanation

## 8.1 Spring Boot Application

`ProductServiceApplication.java` is the entry point.

```java
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

`@SpringBootApplication` enables:

* Component scanning
* Auto configuration
* Spring Boot configuration

When the application starts, Spring Boot creates the application context and initializes the required components.

---

# 9. Maven Configuration

File:

```text
application/services/product-service/pom.xml
```

Important dependencies:

### Spring Web

```xml
<artifactId>spring-boot-starter-web</artifactId>
```

Provides REST API capabilities.

Used for:

* `@RestController`
* `@GetMapping`
* `@PostMapping`
* `@PutMapping`
* `@DeleteMapping`

---

### Spring Data JPA

```xml
<artifactId>spring-boot-starter-data-jpa</artifactId>
```

Provides:

* JPA
* Hibernate
* Repository abstraction
* Database persistence

---

### Validation

```xml
<artifactId>spring-boot-starter-validation</artifactId>
```

Used for:

```java
@NotBlank
@NotNull
@Min
@DecimalMin
```

---

### PostgreSQL Driver

```xml
<groupId>org.postgresql</groupId>
<artifactId>postgresql</artifactId>
```

Allows the application to communicate with PostgreSQL.

---

### Testing

```xml
<artifactId>spring-boot-starter-test</artifactId>
```

Provides testing support including:

* JUnit
* Mockito
* Spring Test
* MockMvc

---

# 10. Java Version

The project uses:

```xml
<java.version>17</java.version>
```

During ECOM-003 validation, Maven initially encountered a Java version mismatch because Maven was using Java 25 while the project required Java 17.

The active environment was corrected using:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Validation:

```bash
java -version
javac -version
mvn -version
```

Maven was then running with Java 17.

Important lesson:

```text
Project Java Version
        |
        v
Maven Compiler
        |
        v
JDK Used by Maven
        |
        v
Must be compatible
```

---

# 11. Application Configuration

File:

```text
src/main/resources/application.yml
```

Configuration:

```yaml
spring:
  application:
    name: product-service

  datasource:
    url: jdbc:postgresql://localhost:5432/shopsphere_product
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
  port: 8082
```

## Application Name

```yaml
spring:
  application:
    name: product-service
```

Used to identify the service inside the Spring Boot environment.

---

## Database URL

```yaml
jdbc:postgresql://localhost:5432/shopsphere_product
```

Meaning:

```text
PostgreSQL
   |
   +-- Host: localhost
   |
   +-- Port: 5432
   |
   +-- Database: shopsphere_product
```

---

## JPA Configuration

```yaml
ddl-auto: update
```

For the current development implementation, Hibernate can update the database schema based on entity changes.

For production, schema management should normally be handled using a controlled migration strategy such as Flyway or Liquibase rather than relying on automatic schema updates.

---

# 12. Product Entity

File:

```text
entity/Product.java
```

The entity represents the database record.

```java
@Entity
@Table(name = "products")
public class Product {
```

This tells JPA that `Product` is a persistent entity mapped to the `products` table.

Fields:

```text
id
name
description
price
category
stockQuantity
```

The ID uses:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

This allows the database to generate the identifier.

---

# 13. Product Repository

File:

```text
repository/ProductRepository.java
```

The repository extends:

```java
JpaRepository<Product, Long>
```

This provides standard operations such as:

```text
save()
findAll()
findById()
delete()
```

The custom method:

```java
List<Product> findByCategoryIgnoreCase(String category);
```

allows category searches without case sensitivity.

Example:

```text
electronics
Electronics
ELECTRONICS
```

can all be handled as the same category search.

---

# 14. Product Service Layer

File:

```text
service/ProductService.java
```

The service layer contains the application's product operations.

## Create

```java
createProduct()
```

Calls:

```text
ProductService
      |
      v
ProductRepository.save()
```

---

## Get All

```java
getAllProducts()
```

Calls:

```text
ProductRepository.findAll()
```

---

## Get By ID

```java
getProductById()
```

Uses:

```java
findById()
```

If the product doesn't exist, the service throws:

```text
Product not found with id: <id>
```

---

## Category Search

```java
getProductsByCategory()
```

Calls:

```java
findByCategoryIgnoreCase()
```

---

## Update

The service first retrieves the existing product.

```text
Request
   |
   v
Find existing product
   |
   v
Update fields
   |
   v
Save existing entity
```

This avoids blindly creating a new product record.

---

## Delete

The service:

1. Finds the product
2. Confirms it exists
3. Deletes it

```text
DELETE request
      |
      v
findById()
      |
      v
Product exists?
      |
      v
delete()
```

---

# 15. DTO Layer

The service uses DTOs to separate API contracts from persistence entities.

## ProductRequest

File:

```text
dto/ProductRequest.java
```

Used for incoming requests.

Validation:

```java
@NotBlank
@NotNull
@DecimalMin
@Min
```

Example:

```json
{
  "name": "Laptop",
  "description": "Enterprise laptop",
  "price": 75000.00,
  "category": "Electronics",
  "stockQuantity": 10
}
```

---

## ProductResponse

File:

```text
dto/ProductResponse.java
```

Used for API responses.

The method:

```java
ProductResponse.fromEntity(product)
```

converts the JPA entity into an API response object.

This keeps the external API model separate from the database entity.

---

# 16. REST API Endpoints

Base path:

```text
/api/products
```

## Create Product

```http
POST /api/products
```

Response:

```text
201 Created
```

---

## Get All Products

```http
GET /api/products
```

Response:

```text
200 OK
```

---

## Get Product

```http
GET /api/products/{id}
```

Example:

```http
GET /api/products/1
```

---

## Get By Category

```http
GET /api/products/category/{category}
```

Example:

```http
GET /api/products/category/Electronics
```

---

## Update Product

```http
PUT /api/products/{id}
```

Example:

```http
PUT /api/products/1
```

---

## Delete Product

```http
DELETE /api/products/{id}
```

Response:

```text
204 No Content
```

---

# 17. REST API Flow

Create request:

```text
POST /api/products
        |
        v
ProductController
        |
        v
@Valid ProductRequest
        |
        v
Product Entity
        |
        v
ProductService
        |
        v
ProductRepository
        |
        v
PostgreSQL
        |
        v
ProductResponse
        |
        v
HTTP 201
```

---

# 18. Validation

The request model validates important business input.

For example:

```java
@NotBlank(message = "Product name is required")
private String name;
```

This prevents an empty product name.

Price:

```java
@NotNull
@DecimalMin(value = "0.0")
```

prevents a missing or negative price.

Stock:

```java
@NotNull
@Min(value = 0)
```

prevents negative stock quantities.

Category:

```java
@NotBlank
```

requires a category.

---

# 19. Exception Handling

File:

```text
exception/GlobalExceptionHandler.java
```

Uses:

```java
@RestControllerAdvice
```

This provides centralized handling for runtime exceptions.

Example:

```text
GET /api/products/999
```

If the product doesn't exist, the service throws:

```text
Product not found with id: 999
```

The exception handler converts this into an HTTP error response.

### Current implementation note

The current ECOM-003 implementation handles `RuntimeException` as a `404 Not Found`.

This is intentionally simple for the DevOps-focused practice project.

A future production hardening ticket can introduce:

```text
ProductNotFoundException
ValidationException
BadRequestException
ErrorResponse
```

with more precise HTTP status handling.

---

# 20. Testing Strategy

Two test classes were created.

```text
ProductServiceTest
ProductControllerTest
```

Total:

```text
14 tests
```

---

# 21. Service Layer Tests

File:

```text
src/test/java/com/shopsphere/productservice/service/ProductServiceTest.java
```

Tests include:

1. Create product
2. Get all products
3. Get product by ID
4. Product not found
5. Search by category
6. Update product
7. Delete product

Result:

```text
Tests run: 7
Failures: 0
Errors: 0
Skipped: 0
```

---

# 22. Controller Tests

File:

```text
src/test/java/com/shopsphere/productservice/controller/ProductControllerTest.java
```

Tests include:

1. Create product
2. Get all products
3. Get product by ID
4. Get products by category
5. Update product
6. Delete product
7. Reject invalid product request

Result:

```text
Tests run: 7
Failures: 0
Errors: 0
Skipped: 0
```

---

# 23. Final Validation

The final command executed was:

```bash
mvn clean test
```

Result:

```text
Tests run: 14
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

Final result:

```text
Product Service
      |
      +-- Compilation       PASS
      |
      +-- Service tests     7/7 PASS
      |
      +-- Controller tests  7/7 PASS
      |
      +-- Total             14/14 PASS
      |
      +-- Maven Build       SUCCESS
```

---

# 24. Git Validation

Before committing:

```bash
git diff --cached --check
```

Result:

```text
No output
```

This means there were no Git whitespace errors detected in the staged changes.

Git status before commit showed 13 staged files.

---

# 25. Git Commit

Commit created:

```bash
git commit -m "ECOM-003: implement product service"
```

Commit:

```text
ad720c8 ECOM-003: implement product service
```

Commit summary:

```text
13 files changed
912 insertions(+)
```

---

# 26. Git Branch Strategy

ECOM-003 follows the ShopSphere enterprise branching model.

```text
main
  |
  v
develop
  |
  +-----------------------------+
  |                             |
  v                             v
ECOM-002                    ECOM-003
                              |
                              v
             feature/ECOM-003-product-service
```

The feature branch was created from `develop`.

This keeps `develop` as the integration branch.

---

# 27. Git Push

The feature branch was pushed using:

```bash
git push -u origin feature/ECOM-003-product-service
```

Remote branch:

```text
origin/feature/ECOM-003-product-service
```

The local branch tracks the remote branch.

Validation:

```bash
git status
```

Result:

```text
On branch feature/ECOM-003-product-service
Your branch is up to date with
'origin/feature/ECOM-003-product-service'.

nothing to commit, working tree clean
```

---

# 28. Final Git State

```text
main
  |
  +-- develop
        |
        +-- feature/ECOM-003-product-service
                    |
                    +-- ad720c8
                        ECOM-003: implement product service
```

Current feature branch:

```text
feature/ECOM-003-product-service
```

Current commit:

```text
ad720c8
```

Working tree:

```text
clean
```

Remote tracking:

```text
origin/feature/ECOM-003-product-service
```

---

# 29. Every Important Command Explained

## Validate Maven project

```bash
mvn validate
```

Checks whether the Maven project configuration is valid.

---

## Run tests

```bash
mvn test
```

Compiles the application and executes tests.

---

## Clean and test

```bash
mvn clean test
```

First removes previous build output and then performs a fresh build and test execution.

This was the final validation command.

---

## Check Git status

```bash
git status
```

Shows:

* Current branch
* Staged changes
* Unstaged changes
* Untracked files
* Working-tree state

---

## Check staged files

```bash
git status --short
```

Useful before committing.

---

## Check staged whitespace problems

```bash
git diff --cached --check
```

Detects common whitespace errors in staged changes.

---

## Commit

```bash
git commit -m "ECOM-003: implement product service"
```

Creates the Jira-linked implementation commit.

---

## Push feature branch

```bash
git push -u origin feature/ECOM-003-product-service
```

Pushes the feature branch and configures upstream tracking.

---

## Check recent commit

```bash
git log -1 --oneline
```

Shows the latest commit.

---

## Check branch tracking

```bash
git branch -vv
```

Shows local branches and their remote tracking branches.

---

# 30. Deployment Model

ECOM-003 currently provides the application layer.

The future DevOps deployment flow is:

```text
Developer
    |
    v
GitHub
    |
    v
Jenkins
    |
    +--> Maven Build
    |
    +--> Unit Tests
    |
    +--> Security Scan
    |
    +--> Docker Build
    |
    v
Amazon ECR
    |
    v
Amazon EKS
    |
    v
Product Service Pod
    |
    v
PostgreSQL / Amazon RDS
```

The current ticket focuses on the Product Service application.

Infrastructure and Kubernetes deployment will be handled through later Jira tickets.

---

# 31. Docker Integration — Future Flow

The Product Service will eventually be packaged as a Docker image.

Example future flow:

```text
Product Service Source
        |
        v
     Maven
        |
        v
     JAR File
        |
        v
   Docker Build
        |
        v
Docker Image
        |
        v
Amazon ECR
```

The application port is:

```text
8082
```

---

# 32. Kubernetes Integration — Future Flow

Future Kubernetes deployment:

```text
Amazon EKS
    |
    v
Product Service Deployment
    |
    +-- Pod
    |
    +-- Pod
    |
    v
Product Service
    |
    v
PostgreSQL
```

The number of replicas will be controlled through Kubernetes configuration.

---

# 33. CI/CD Integration — Future Flow

Jenkins will eventually execute:

```text
Git Push
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
   +--> Docker Image Scan
   |
   +--> Push Image to ECR
   |
   v
Deployment
```

---

# 34. Troubleshooting & RCA

## Issue 1 — Maven Java Version Mismatch

### Error

```text
Fatal error compiling:
error: release version 17 not supported
```

### Root Cause

The project required Java 17, but Maven was running with Java 25.

The system had:

```text
java      -> Java 25
javac     -> Java 17
```

This created an inconsistent build environment.

### Resolution

Set Java 17 for the current shell:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Then verify:

```bash
java -version
javac -version
mvn -version
```

All should use Java 17.

### Lesson

Always verify the JDK used by Maven, not just the system `javac`.

---

# 35. Issue 2 — Invalid Product Request

During the controller test for invalid input, Spring logged:

```text
MethodArgumentNotValidException
```

This was expected.

The test intentionally submitted an empty request.

Validation detected missing:

```text
name
price
category
stockQuantity
```

The API returned:

```text
400 Bad Request
```

Therefore the test passed.

### Lesson

A warning/error-looking log line during a negative test does not necessarily mean the build failed.

Always check:

```text
Tests run
Failures
Errors
BUILD SUCCESS
```

---

# 36. Issue 3 — Deprecated MockBean Warning

The test compilation reported:

```text
MockBean has been deprecated and marked for removal
```

This is a warning, not a test failure.

Current ECOM-003 validation:

```text
14/14 tests passed
BUILD SUCCESS
```

The warning can be addressed in a future maintenance ticket without expanding the current implementation scope.

---

# 37. Rollback Procedure

## Application Git Rollback

The ECOM-003 implementation commit is:

```text
ad720c8
```

If the feature needs to be removed before integration, the feature branch can be reset or deleted according to the team's approved Git workflow.

Because the implementation is isolated in:

```text
feature/ECOM-003-product-service
```

it does not automatically affect `develop`.

---

## Commit Revert

If the commit has already been integrated and needs to be safely reversed, prefer:

```bash
git revert ad720c8
```

This creates a new commit that reverses the changes while preserving Git history.

---

# 38. Production Best Practices

The current implementation is intentionally simple for the DevOps practice project.

For production hardening, consider:

### 1. Database migrations

Use:

```text
Flyway
```

or:

```text
Liquibase
```

instead of:

```yaml
ddl-auto: update
```

---

### 2. Secrets management

Do not store:

```yaml
username: shopsphere
password: shopsphere
```

in production configuration.

Use:

```text
AWS Secrets Manager
```

or:

```text
Kubernetes Secrets
```

with appropriate secret-management practices.

---

### 3. Environment-specific configuration

Use separate configuration for:

```text
DEV
QA
STAGE
PROD
```

---

### 4. Dedicated exception types

Instead of handling every `RuntimeException` as 404, introduce:

```text
ProductNotFoundException
```

and specific error responses.

---

### 5. API documentation

Introduce:

```text
OpenAPI / Swagger
```

for API documentation.

---

### 6. Observability

Add:

```text
Prometheus
Grafana
Loki
```

for:

* Metrics
* Dashboards
* Logs
* Troubleshooting

---

### 7. Container security

Before deployment:

```text
Docker image scan
Dependency scan
Secret scan
SAST
```

---

### 8. CI/CD quality gates

Jenkins should prevent deployment when:

```text
Unit tests fail
      OR
Security scan fails
      OR
Image scan fails
      OR
Quality gate fails
```

---

# 39. Enterprise DevOps Flow

The complete future lifecycle is:

```text
Jira
  |
  v
Developer
  |
  v
Feature Branch
  |
  v
Code
  |
  v
Git Commit
  |
  v
GitHub
  |
  v
Jenkins
  |
  +-------------------+
  |                   |
  v                   v
Maven Test       Security Scan
  |                   |
  +---------+---------+
            |
            v
       Docker Build
            |
            v
          ECR
            |
            v
          EKS
            |
            v
     Product Service
            |
            v
       PostgreSQL
            |
            v
 Prometheus / Grafana / Loki
```

---

# 40. Interview Explanation

## How would you explain ECOM-003 in an interview?

> "In the ShopSphere e-commerce platform, I implemented the Product Service as an independent Spring Boot microservice. The service exposes REST APIs for creating, retrieving, updating and deleting products, along with category-based product searches.
>
> I used Spring Data JPA for persistence and PostgreSQL as the database. I separated the API request and response models using DTOs and added request validation using Jakarta Bean Validation.
>
> From the DevOps perspective, I kept the service Maven-based and testable so it could later move through our CI/CD pipeline. I added service-layer and controller-layer tests, and the final Maven validation passed all 14 tests.
>
> The implementation was delivered through a Jira-based Git workflow using the `feature/ECOM-003-product-service` branch. After validation, I committed it as `ad720c8` with the message `ECOM-003: implement product service` and pushed the feature branch to GitHub.
>
> The next stages of the DevOps lifecycle are containerization, security scanning, ECR image publishing, Kubernetes deployment on EKS, monitoring, logging and production troubleshooting."

---

# 41. Interview Questions & Answers

## Q1. Why did you create a separate Product Service?

**Answer:**

> "Product management is a separate business capability, so we isolate it into its own service. This gives us independent ownership of product data and allows the service to scale and deploy independently."

---

## Q2. Why did you use DTOs?

**Answer:**

> "I used DTOs to separate the external API contract from the JPA entity. This prevents the database model from becoming tightly coupled to the API contract and gives us more flexibility when the API evolves."

---

## Q3. Why use a service layer?

**Answer:**

> "The service layer keeps application operations separate from HTTP handling and database access. The controller handles the API request, the service handles the operation, and the repository handles persistence."

---

## Q4. Why use Spring Data JPA?

**Answer:**

> "Spring Data JPA reduces boilerplate database code and provides repository methods such as save, findById, findAll and delete. It also integrates naturally with Hibernate and PostgreSQL."

---

## Q5. Why validate requests?

**Answer:**

> "Validation prevents invalid data from entering the application. For example, product name and category cannot be blank, price cannot be negative, and stock quantity cannot be negative."

---

## Q6. How did you test the service?

**Answer:**

> "I created unit tests using JUnit and Mockito for the service layer. I tested create, read, category search, update, delete and not-found scenarios."

---

## Q7. How did you test REST APIs?

**Answer:**

> "I used Spring MockMvc with `@WebMvcTest` to test the controller layer without requiring a real database. I tested successful API requests and validation failure scenarios."

---

## Q8. How many tests passed?

**Answer:**

> "There were 14 tests in total: 7 service-layer tests and 7 controller-layer tests. The final `mvn clean test` completed with 14 tests passed, zero failures and zero errors."

---

## Q9. What happened when Maven initially failed?

**Answer:**

> "The project required Java 17, but Maven was running with Java 25. I verified the Java and Maven versions, set `JAVA_HOME` to the Java 17 installation, updated the PATH for the shell, and then Maven successfully compiled the project using release 17."

---

## Q10. How would you deploy this service?

**Answer:**

> "The intended DevOps lifecycle is Maven build and test, Docker image creation, security scanning, publishing the image to Amazon ECR, and deploying the container to Amazon EKS using Kubernetes or Helm."

---

## Q11. How would you manage the database password in production?

**Answer:**

> "I would not keep the password in Git. I would use a managed secret solution such as AWS Secrets Manager and inject the secret securely into the application environment."

---

## Q12. What would you improve before production?

**Answer:**

> "I would add database migrations, production-grade exception handling, secret management, environment-specific configuration, API documentation, observability, security scanning and stronger CI/CD quality gates."

---

## Q13. How did you manage Git for this ticket?

**Answer:**

> "I created `feature/ECOM-003-product-service` from the `develop` branch, implemented the service, validated it, committed it with the Jira ticket ID, and pushed the feature branch to GitHub."

---

## Q14. Why didn't you directly commit to develop?

**Answer:**

> "We use feature branches so that individual Jira changes remain isolated. `develop` acts as the integration branch, while feature branches contain individual pieces of work."

---

# 42. Review Questions

Use these questions to verify your understanding.

1. Why is Product Service separated from User Service?
2. What is the responsibility of `ProductController`?
3. What is the responsibility of `ProductService`?
4. What is the responsibility of `ProductRepository`?
5. Why do we use `ProductRequest`?
6. Why do we use `ProductResponse`?
7. What does `@Entity` do?
8. What does `@GeneratedValue` do?
9. Why is `JpaRepository` useful?
10. What does `@Valid` do?
11. What happens when validation fails?
12. Why do we use `@RestControllerAdvice`?
13. What is the difference between unit testing and controller testing?
14. Why did we use Mockito?
15. Why did Maven initially fail?
16. How did you fix the Java version mismatch?
17. Why is `target/` ignored by Git?
18. Why did we use a feature branch?
19. Why is the Jira ID included in the commit message?
20. What is the next step after pushing the feature branch?
21. How would Jenkins build this service?
22. How would you containerize it?
23. How would you publish the image?
24. How would you deploy it to EKS?
25. How would you monitor it in production?

---

# 43. Enterprise Markdown Documentation Summary

## Ticket

```text
ECOM-003
```

## Feature

```text
Product Service
```

## Branch

```text
feature/ECOM-003-product-service
```

## Commit

```text
ad720c8
```

## Commit Message

```text
ECOM-003: implement product service
```

## Technology

```text
Java 17
Spring Boot 3.5.5
Spring Web
Spring Data JPA
PostgreSQL
Maven
JUnit
Mockito
MockMvc
```

## APIs

```text
POST   /api/products
GET    /api/products
GET    /api/products/{id}
GET    /api/products/category/{category}
PUT    /api/products/{id}
DELETE /api/products/{id}
```

## Tests

```text
Service tests:     7/7 PASS
Controller tests:  7/7 PASS
Total:            14/14 PASS
```

## Build

```text
BUILD SUCCESS
```

## Git

```text
Feature branch pushed successfully
Working tree clean
Remote tracking configured
```

---

# 44. ECOM-003 Completion Checklist

```text
[x] Jira ticket defined
[x] Feature branch created from develop
[x] Maven project created
[x] Spring Boot application created
[x] PostgreSQL configuration added
[x] Product entity created
[x] Product repository created
[x] Product service created
[x] Product request DTO created
[x] Product response DTO created
[x] Product controller created
[x] Validation added
[x] Exception handling added
[x] Service tests added
[x] Controller tests added
[x] Maven clean test passed
[x] 14/14 tests passed
[x] Git whitespace validation passed
[x] Implementation committed
[x] Implementation pushed to GitHub
[x] Working tree clean
```

---

# 45. Current Project State

ECOM-003 implementation is complete.

```text
ECOM-003
   |
   v
Product Service
   |
   v
Implementation Complete
   |
   v
14/14 Tests Passed
   |
   v
Commit ad720c8
   |
   v
Feature Branch Pushed
   |
   v
Working Tree Clean
```

The next DevOps lifecycle stages are intentionally separate from this ticket:

```text
ECOM-003
   |
   +--> Documentation
   |
   v
Integration into develop
   |
   v
Future CI/CD
   |
   v
Docker
   |
   v
Security Scanning
   |
   v
Amazon ECR
   |
   v
Terraform / AWS
   |
   v
Amazon EKS
   |
   v
Kubernetes / Helm
   |
   v
Monitoring / Logging
```

---

# 46. Final Enterprise Takeaway

The important point of ECOM-003 is not simply creating Java classes.

The ticket demonstrates an enterprise development and DevOps workflow:

```text
Business Requirement
        |
        v
Jira Ticket
        |
        v
Feature Branch
        |
        v
Implementation
        |
        v
Automated Tests
        |
        v
Build Validation
        |
        v
Git Quality Check
        |
        v
Jira-Based Commit
        |
        v
GitHub Feature Branch
        |
        v
Future CI/CD Integration
```

The Product Service is intentionally simple enough to understand while being realistic enough to become part of the larger ShopSphere DevOps lifecycle.

**ECOM-003 implementation status: COMPLETE.**
