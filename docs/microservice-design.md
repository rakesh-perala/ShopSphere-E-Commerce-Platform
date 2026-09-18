# ShopSphere — Enterprise E-Commerce Platform

## Microservice Design

---

## 1. Purpose

ShopSphere is designed as a **microservices-based e-commerce application**.

Instead of building one large backend application, we divide the business into independent services.

```text
                         SHOPSPHERE
                             |
              +--------------+--------------+
              |              |              |
              v              v              v
        User Service   Product Service   Cart Service
              |              |              |
              v              v              v
           User DB        Product DB       Cart DB
                             |
                             v
                       Order Service
                             |
                             v
                       Payment Service
                             |
                             v
                        Payment DB
```

Each service owns a specific business capability.

---

# 2. Why Microservices?

A traditional monolithic application could look like:

```text
                    E-Commerce Application
                           |
       +-------------------+-------------------+
       |          |          |        |        |
      User     Product      Cart    Order   Payment
       |          |          |        |        |
       +----------+----------+--------+--------+
                           |
                       One Database
```

If one part has a problem, the entire application can potentially be affected.

With microservices:

```text
User Service       Product Service       Order Service
     |                   |                    |
   User DB            Product DB            Order DB
```

Each service can be:

* developed independently
* tested independently
* deployed independently
* scaled independently
* monitored independently
* rolled back independently

### Enterprise Example

During a festival sale, product traffic may increase significantly.

We may need:

```text
Product Service
Replicas: 2 -> 10
```

while User Service may remain:

```text
User Service
Replicas: 2
```

We don't need to scale every service just because Product Service has increased traffic.

---

# 3. ShopSphere Services

We will initially implement five backend microservices.

| Service         | Business Responsibility            | Database   |
| --------------- | ---------------------------------- | ---------- |
| User Service    | Users, registration, login/profile | User DB    |
| Product Service | Products, categories, inventory    | Product DB |
| Cart Service    | Shopping cart                      | Cart DB    |
| Order Service   | Orders and order history           | Order DB   |
| Payment Service | Payment processing/status          | Payment DB |

Later we can introduce:

```text
Notification Service
```

for email/SMS/order notifications.

---

# 4. Service Ownership Principle

The most important microservice rule is:

> **Each service owns its business data.**

For example:

```text
Product Service
      |
      v
Product DB
      |
      X
Order Service cannot directly modify Product DB
```

If Order Service needs product information:

```text
Order Service
      |
      | REST API
      v
Product Service
      |
      v
Product DB
```

This keeps business ownership clear.

---

# 5. User Service

## Business Responsibility

User Service manages:

* user registration
* authentication-related operations
* user profile
* user information

### Database

```text
User Service
     |
     v
 PostgreSQL
     |
     v
 users
```

### User Table

```text
users
--------------------------------
id
name
email
password_hash
created_at
updated_at
```

Email should be unique.

---

## APIs

### Register User

```http
POST /api/users/register
```

Example request:

```json
{
  "name": "Rakesh",
  "email": "rakesh@example.com",
  "password": "password123"
}
```

Example response:

```json
{
  "id": 101,
  "name": "Rakesh",
  "email": "rakesh@example.com",
  "message": "User registered successfully"
}
```

### Get User

```http
GET /api/users/{id}
```

### Health

```http
GET /actuator/health
```

---

# 6. Product Service

## Business Responsibility

Product Service manages:

* products
* categories
* prices
* inventory
* stock quantity

### Database

```text
Product Service
      |
      v
  PostgreSQL
      |
      +---- products
      |
      +---- categories
```

### Product Table

```text
products
--------------------------------
id
name
description
price
stock_quantity
category_id
created_at
updated_at
```

### Category Table

```text
categories
--------------------------------
id
name
description
created_at
```

---

## APIs

### Get Products

```http
GET /api/products
```

### Get Product

```http
GET /api/products/{id}
```

### Create Product

```http
POST /api/products
```

Example:

```json
{
  "name": "Laptop",
  "description": "Business Laptop",
  "price": 75000,
  "stockQuantity": 25,
  "categoryId": 10
}
```

### Update Stock

Conceptually:

```http
PATCH /api/products/{id}/stock
```

The Product Service remains responsible for changing stock.

### Health

```http
GET /actuator/health
```

---

# 7. Cart Service

## Business Responsibility

Cart Service manages:

* shopping carts
* cart items
* quantity
* adding products
* removing products
* updating quantity

### Database

```text
Cart Service
     |
     v
 PostgreSQL
     |
     +---- carts
     |
     +---- cart_items
```

### Cart

```text
carts
--------------------------------
id
user_id
created_at
updated_at
```

### Cart Items

```text
cart_items
--------------------------------
id
cart_id
product_id
quantity
created_at
updated_at
```

Notice:

```text
cart_items.product_id
```

is a logical reference to the Product Service.

We do not create a cross-database foreign key.

---

## APIs

### Get Cart

```http
GET /api/carts/{userId}
```

### Add Product

```http
POST /api/carts/{userId}/items
```

Example:

```json
{
  "productId": 501,
  "quantity": 2
}
```

### Update Quantity

```http
PUT /api/carts/{userId}/items/{productId}
```

### Remove Item

```http
DELETE /api/carts/{userId}/items/{productId}
```

---

# 8. Order Service

## Business Responsibility

Order Service manages:

* order creation
* order items
* order totals
* order status
* order history

### Database

```text
Order Service
      |
      v
 PostgreSQL
      |
      +---- orders
      |
      +---- order_items
```

### Orders

```text
orders
--------------------------------
id
user_id
total_amount
status
created_at
updated_at
```

### Order Items

```text
order_items
--------------------------------
id
order_id
product_id
quantity
price
created_at
```

---

# 9. Why Store Price in order_items?

Suppose today:

```text
Laptop = ₹75,000
```

Customer purchases it.

Tomorrow:

```text
Laptop = ₹80,000
```

The customer's historical order must still show:

```text
Purchased Price = ₹75,000
```

Therefore:

```text
Product DB
Current Price = ₹80,000

Order DB
Historical Price = ₹75,000
```

This is an important real-world e-commerce design decision.

---

# 10. Order API

### Create Order

```http
POST /api/orders
```

Example:

```json
{
  "userId": 101,
  "items": [
    {
      "productId": 501,
      "quantity": 2
    }
  ]
}
```

Possible response:

```json
{
  "orderId": 9001,
  "status": "PENDING_PAYMENT",
  "totalAmount": 150000
}
```

---

# 11. Payment Service

## Business Responsibility

Payment Service manages:

* payment request
* payment validation
* payment processing
* payment status
* transaction reference

Initially we use a **simulated payment provider** for learning.

Later a real payment gateway integration can be added.

### Database

```text
Payment Service
      |
      v
 PostgreSQL
      |
      v
 payments
```

### Payment Table

```text
payments
--------------------------------
id
order_id
amount
status
transaction_reference
created_at
updated_at
```

---

# 12. Payment API

```http
POST /api/payments
```

Example:

```json
{
  "orderId": 9001,
  "amount": 150000
}
```

Successful response:

```json
{
  "paymentId": 7001,
  "orderId": 9001,
  "status": "SUCCESS",
  "transactionReference": "TXN-ABC123"
}
```

Failure:

```json
{
  "paymentId": 7001,
  "orderId": 9001,
  "status": "FAILED"
}
```

---

# 13. Complete Customer Flow

A customer places an order.

```text
Customer
   |
   v
Frontend
   |
   v
User Service
   |
   | authenticate
   v
Product Service
   |
   | product information
   v
Cart Service
   |
   | checkout
   v
Order Service
   |
   | validate product
   v
Product Service
   |
   | stock available
   v
Order Service
   |
   | payment request
   v
Payment Service
   |
   | SUCCESS
   v
Order Service
   |
   v
Order DB
```

---

# 14. Detailed Order Flow

```text
                Customer
                    |
                    v
              Place Order
                    |
                    v
              Order Service
                    |
                    v
             Validate User
                    |
              +-----+-----+
              |           |
           Invalid       Valid
              |           |
              v           v
            Reject    Check Product
                          |
                    +-----+-----+
                    |           |
                 No Stock     Available
                    |           |
                    v           v
                  Reject    Create Order
                                |
                                v
                         Create Order Items
                                |
                                v
                         Payment Service
                                |
                    +-----------+-----------+
                    |                       |
                  FAILED                 SUCCESS
                    |                       |
                    v                       v
             Payment Failed          Confirm Order
                                            |
                                            v
                                       Update Stock
                                            |
                                            v
                                       Order Created
```

---

# 15. Service-to-Service Communication

Initially ShopSphere will use **REST APIs**.

Example:

```text
Order Service
     |
     | GET /api/products/501
     v
Product Service
     |
     v
Product DB
```

Payment:

```text
Order Service
     |
     | POST /api/payments
     v
Payment Service
     |
     v
Payment DB
```

---

# 16. Synchronous Communication

Our initial architecture uses synchronous communication.

Example:

```text
Order Service
      |
      | HTTP request
      v
Payment Service
      |
      | HTTP response
      v
Order Service
```

Order Service waits for the Payment Service response.

This is simple and useful for our first implementation.

---

# 17. Future Asynchronous Architecture

As the project becomes more enterprise-level, we can introduce messaging.

For example:

```text
Order Service
      |
      | OrderCreated Event
      v
 Message Broker
      |
      +------------+-------------+
      |            |             |
      v            v             v
Payment       Notification   Analytics
Service        Service        Service
```

Possible future technologies:

* Kafka
* RabbitMQ
* AWS SQS/SNS

We will not introduce these on Day 1 unless there is a business reason.

---

# 18. Failure Scenario

Suppose Payment Service is down.

```text
Order Service
      |
      | POST /payments
      v
Payment Service
      X
     DOWN
```

The Order Service should not simply disappear or mark the order as successful.

Instead:

```text
Order Status
     |
     v
PENDING_PAYMENT
```

The customer can receive an appropriate response.

This gives us a real production troubleshooting scenario later.

---

# 19. Another Failure Scenario

Product Service is unavailable.

```text
Order Service
      |
      | Product validation
      v
Product Service
      X
     DOWN
```

Order Service should fail safely.

Possible result:

```text
Order Creation
      |
      v
Validation Failed
      |
      v
HTTP 503 Service Unavailable
```

Logs should contain:

```text
timestamp
service
request-id
product-id
error
downstream-service
response-time
```

---

# 20. Health and Readiness

Every microservice should expose health information.

Example:

```http
GET /actuator/health
```

Possible response:

```json
{
  "status": "UP"
}
```

In Kubernetes, we will later use:

```text
Liveness Probe
       |
       v
Is application alive?

Readiness Probe
       |
       v
Can application receive traffic?
```

Example:

```text
Kubernetes
     |
     +---- Liveness ----> Service
     |
     +---- Readiness ---> Service
```

This becomes important during EKS deployment.

---

# 21. Configuration

Application configuration should not be hard-coded.

Example environment variables:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
PRODUCT_SERVICE_URL
PAYMENT_SERVICE_URL
```

Example:

```text
Order Service
     |
     +---- DB_HOST
     |
     +---- PRODUCT_SERVICE_URL
     |
     +---- PAYMENT_SERVICE_URL
```

Later Kubernetes will provide configuration through:

```text
ConfigMap
Secret
External Secrets
```

depending on the requirement.

---

# 22. Secrets

Never commit:

```text
DB_PASSWORD=password123
```

into Git.

Instead:

```text
Secret Manager / Secrets System
            |
            v
       Kubernetes Secret
            |
            v
       Application
```

For AWS production architecture, secret-management infrastructure can be Terraform-managed, while actual secret values should be handled securely rather than committed to Git.

---

# 23. Microservice Folder Structure

Each service will follow a consistent structure.

Example:

```text
application/
└── services/
    └── user-service/
        ├── src/
        ├── pom.xml
        ├── Dockerfile
        ├── README.md
        └── .gitignore
```

Later, depending on the selected application framework:

```text
src/
├── main/
│   ├── java/
│   └── resources/
└── test/
```

We will establish the exact technology before creating these files.

---

# 24. Application Layer Flow

Inside a typical backend service:

```text
HTTP Request
     |
     v
Controller
     |
     v
Service Layer
     |
     v
Repository
     |
     v
PostgreSQL
```

Example:

```text
POST /api/orders
       |
       v
OrderController
       |
       v
OrderService
       |
       v
OrderRepository
       |
       v
PostgreSQL
```

This keeps responsibilities separated.

---

# 25. Docker Integration

Each microservice will eventually have its own container image.

```text
user-service
     |
     v
Docker Image

product-service
     |
     v
Docker Image

cart-service
     |
     v
Docker Image

order-service
     |
     v
Docker Image

payment-service
     |
     v
Docker Image
```

Images will later be stored in:

```text
Amazon ECR
```

---

# 26. Kubernetes Integration

Each service will eventually run as a Kubernetes workload.

```text
EKS
 |
 +---- user-service
 |
 +---- product-service
 |
 +---- cart-service
 |
 +---- order-service
 |
 +---- payment-service
```

Kubernetes will manage:

* Pods
* Deployments
* Services
* ConfigMaps
* Secrets
* Probes
* Resource requests/limits
* Autoscaling

---

# 27. Jenkins CI/CD Integration

The eventual pipeline will look like:

```text
Developer
    |
    v
Git
    |
    v
Jenkins
    |
    +---- Build
    |
    +---- Unit Test
    |
    +---- Code Quality
    |
    +---- Security Scan
    |
    +---- Docker Build
    |
    +---- Push Image
    |
    v
Container Registry
    |
    v
Kubernetes / EKS
```

Each service can have its own build and deployment process.

---

# 28. Terraform Integration

Terraform belongs to the **infrastructure layer**.

Terraform will provision:

```text
AWS
 |
 +---- VPC
 |
 +---- Subnets
 |
 +---- Route Tables
 |
 +---- Security Groups
 |
 +---- IAM
 |
 +---- ECR
 |
 +---- EKS
 |
 +---- RDS PostgreSQL
 |
 +---- Load Balancer Infrastructure
```

The root workflow remains:

```bash
cd terraform

terraform init

terraform plan

terraform apply
```

Root Terraform calls modules:

```text
terraform/
│
├── main.tf
├── provider.tf
├── variables.tf
├── outputs.tf
│
└── modules/
    ├── vpc/
    ├── security-groups/
    ├── iam/
    ├── ecr/
    ├── eks/
    ├── database/
    └── load-balancer/
```

The important ownership boundary is:

```text
Terraform
    |
    v
AWS Infrastructure

Kubernetes / Helm
    |
    v
Application Workloads

Jenkins
    |
    v
CI/CD Automation
```

We should avoid having two different systems manage the same resource unintentionally.

---

# 29. Database Architecture

Conceptually:

```text
                   PostgreSQL
                       |
       +---------------+---------------+
       |               |               |
       v               v               v
    User DB        Product DB        Cart DB

                       |
             +---------+---------+
             |                   |
             v                   v
          Order DB           Payment DB
```

For the learning project, we will maintain clear database ownership even if the initial AWS implementation uses a practical PostgreSQL deployment strategy.

The key rule remains:

```text
User Service     -> User Data
Product Service  -> Product Data
Cart Service     -> Cart Data
Order Service    -> Order Data
Payment Service  -> Payment Data
```

---

# 30. Security Boundary

Target AWS architecture:

```text
                         INTERNET
                            |
                           HTTPS
                            |
                            v
                    Load Balancer
                            |
                            v
                     Application Layer
                            |
                            | 5432
                            v
                     RDS PostgreSQL
```

Security Groups:

```text
Internet
   |
   | 443
   v
[Load Balancer SG]
   |
   | Application Traffic
   v
[Application SG]
   |
   | 5432
   v
[Database SG]
```

Database should remain private.

The DB security group should allow PostgreSQL traffic only from the required application security boundary.

---

# 31. Observability

Every service should eventually produce:

### Logs

```text
INFO
WARN
ERROR
```

Example:

```text
2026-09-18 10:20:30
service=order-service
requestId=abc123
message=Payment request failed
```

### Metrics

Examples:

```text
HTTP request count
HTTP error count
HTTP latency
JVM metrics
CPU
Memory
Database connection count
```

### Monitoring

Target:

```text
Applications
     |
     v
Prometheus
     |
     v
Grafana
```

### Logging

```text
Applications
     |
     v
Loki
     |
     v
Grafana
```

---

# 32. Real-Time Enterprise Scenario

Imagine ShopSphere is running a large festival sale.

Customer:

```text
User
 |
 | Login
 v
User Service
 |
 | Browse laptop
 v
Product Service
 |
 | Add to cart
 v
Cart Service
 |
 | Checkout
 v
Order Service
 |
 | Payment
 v
Payment Service
 |
 | Success
 v
Order Service
 |
 v
Order Confirmed
```

At the same time:

```text
Product Service
Replicas: 2 -> 8
```

because product browsing traffic increased.

Order Service may remain:

```text
Replicas: 2 -> 4
```

depending on actual traffic.

This is one of the operational advantages of microservices.

---

# 33. Service Dependency Map

```text
Frontend
   |
   +------------------+
   |                  |
   v                  v
User Service      Product Service
                       |
                       v
                  Cart Service
                       |
                       v
                  Order Service
                       |
                       v
                 Payment Service
```

More accurately, services should communicate only when business logic requires it.

We should avoid creating a dependency chain where every request must pass through every service.

---

# 34. What Happens If a Service Is Down?

| Service         | Example Impact                             |
| --------------- | ------------------------------------------ |
| User Service    | Login/registration unavailable             |
| Product Service | Product browsing/order validation affected |
| Cart Service    | Cart operations unavailable                |
| Order Service   | Checkout/order creation unavailable        |
| Payment Service | Orders may remain `PENDING_PAYMENT`        |

The application should fail gracefully instead of producing incorrect business transactions.

---

# 35. Rollback Strategy

Application rollback:

```text
Current Image
     |
     v
Version 1.2.0
     |
     X
   Issue
     |
     v
Rollback
     |
     v
Version 1.1.0
```

Kubernetes/Helm will eventually manage application rollout and rollback.

Infrastructure rollback is different:

```text
Terraform
     |
     v
Infrastructure Change
     |
     X
Problem
     |
     v
terraform plan
     |
     v
Controlled remediation
```

We should not blindly run:

```bash
terraform destroy
```

in a production environment.

---

# 36. Development Order

We will implement services in this order:

```text
1. User Service
       |
       v
2. Product Service
       |
       v
3. Cart Service
       |
       v
4. Order Service
       |
       v
5. Payment Service
```

Why this order?

Because the business flow naturally develops as:

```text
User
 |
 v
Product
 |
 v
Cart
 |
 v
Order
 |
 v
Payment
```

---

# 37. Development Strategy

For every service, we will follow the same process.

```text
Business Requirement
        |
        v
API Design
        |
        v
Database Design
        |
        v
Application Code
        |
        v
Unit Tests
        |
        v
Docker
        |
        v
Kubernetes
        |
        v
Jenkins CI/CD
        |
        v
Monitoring
        |
        v
Incident Testing
```

We will not create the entire infrastructure on Day 1.

We will build the application progressively and connect each layer at the correct stage.

---

# 38. Interview Explanation

### Question:

**How did you design the ShopSphere application using microservices?**

### Interview Answer:

> "I designed ShopSphere as a microservices-based e-commerce platform. I separated the application into User, Product, Cart, Order, and Payment services based on business responsibilities. Each service owns its own data and exposes APIs for other services when communication is required. Initially I use synchronous REST communication because it keeps the architecture simple, and I can introduce asynchronous messaging later for use cases such as notifications and event-driven processing. The services are containerized with Docker and deployed on Kubernetes/EKS. Jenkins handles CI/CD, while Terraform manages the AWS infrastructure including VPC, networking, IAM, ECR, EKS, security groups, and private PostgreSQL infrastructure. Prometheus/Grafana and Loki provide observability."

---

# 39. Important 7-Year-Level Interview Points

### Q1. Why doesn't Order Service directly update Product DB?

Because Product Service owns product and inventory data.

```text
Order Service
     |
     X
Product DB
```

Instead:

```text
Order Service
     |
     v
Product Service
     |
     v
Product DB
```

---

### Q2. Why don't you use foreign keys between services?

Because each service owns its own database boundary.

For example:

```text
orders.user_id
```

can identify a user logically, but Order DB should not create a foreign key directly into User DB.

---

### Q3. What happens if Payment Service fails?

The order should not be incorrectly marked as successful.

A controlled state such as:

```text
PENDING_PAYMENT
```

can be maintained, with retry/reconciliation mechanisms introduced as the architecture evolves.

---

### Q4. Why use REST initially?

Because it is straightforward to implement, test, troubleshoot, and understand.

Later, high-volume or event-driven workflows can use:

```text
Kafka
SQS
SNS
RabbitMQ
```

where appropriate.

---

### Q5. Where does Terraform fit?

Terraform manages infrastructure.

```text
Terraform
   |
   v
AWS Infrastructure
```

It does not replace:

```text
Docker       -> containerization
Kubernetes   -> workload orchestration
Jenkins      -> CI/CD
Application  -> business logic
```

---

# 40. Final Architecture

```text
                         CUSTOMER
                            |
                            v
                       HTTPS / DNS
                            |
                            v
                     Load Balancer
                            |
                            v
                       FRONTEND
                            |
                            v
                         APIs
                            |
          +-----------------+------------------+
          |                 |                  |
          v                 v                  v
    User Service      Product Service      Cart Service
          |                 |                  |
          v                 v                  v
       User DB          Product DB           Cart DB
                              |
                              |
                              v
                        Order Service
                              |
                              v
                           Order DB
                              |
                              v
                       Payment Service
                              |
                              v
                         Payment DB


                     AWS PRIVATE NETWORK
                            |
                            v
                           VPC
                            |
              +-------------+-------------+
              |                           |
              v                           v
        Private Subnets              Public Subnets
              |                           |
              v                           v
             EKS                     Load Balancer
              |
              v
        Application Pods
              |
              v
        Private PostgreSQL


Infrastructure:
Terraform
    |
    +--> VPC
    +--> Networking
    +--> Security Groups
    +--> IAM
    +--> ECR
    +--> EKS
    +--> PostgreSQL
    +--> AWS infrastructure


Application Delivery:
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
    +--> Scan
    +--> Docker Build
    +--> Push Image
    |
    v
   ECR
    |
    v
   EKS


Observability:

Applications
     |
     +----> Prometheus ----> Grafana
     |
     +----> Loki ----------> Grafana
```

---

# 41. Key Principle

The ShopSphere architecture follows this separation:

```text
BUSINESS LOGIC
      |
      v
MICROSERVICES

APPLICATION PACKAGING
      |
      v
DOCKER

APPLICATION ORCHESTRATION
      |
      v
KUBERNETES / EKS

CI/CD
      |
      v
JENKINS

INFRASTRUCTURE
      |
      v
TERRAFORM

DATABASE
      |
      v
POSTGRESQL / RDS

OBSERVABILITY
      |
      +---- PROMETHEUS
      +---- GRAFANA
      +---- LOKI
```

This separation will make the project easier to operate, troubleshoot, scale, and explain in a real DevOps interview.
