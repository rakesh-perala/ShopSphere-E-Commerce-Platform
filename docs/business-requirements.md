# ShopSphere — Enterprise E-Commerce Platform

## 1. Document Information

| Item                   | Details                         |
| ---------------------- | ------------------------------- |
| Project Name           | ShopSphere                      |
| Project Type           | Enterprise E-Commerce Platform  |
| Architecture           | Microservices                   |
| Cloud Platform         | AWS                             |
| Infrastructure as Code | Terraform                       |
| Database               | PostgreSQL                      |
| Containerization       | Docker                          |
| Container Platform     | Kubernetes / Amazon EKS         |
| CI/CD                  | Jenkins                         |
| Monitoring             | Prometheus + Grafana            |
| Logging                | Loki                            |
| Source Control         | Git / GitHub                    |
| Environment            | Dev initially, Production later |

---

# 2. Business Requirement

ShopSphere is an enterprise e-commerce platform that allows customers to browse products, manage their shopping cart, place orders, make payments, and track their orders.

The platform also provides administrative capabilities for managing products, inventory, customers, orders, and operational activities.

The application must be designed using a **microservices architecture** so that individual business capabilities can be developed, deployed, scaled, and maintained independently.

The platform will run on AWS and its infrastructure will be provisioned using Terraform.

---

# 3. Business Problem

A traditional monolithic e-commerce application can become difficult to scale and maintain as the business grows.

For example:

```text
                         Monolithic Application

                 +-------------------------------+
                 |                               |
                 |        E-Commerce App         |
                 |                               |
                 |  User                         |
                 |  Product                      |
                 |  Cart                         |
                 |  Order                        |
                 |  Payment                      |
                 |  Notification                 |
                 |                               |
                 +---------------+---------------+
                                 |
                                 v
                            Database
```

If the Order functionality receives heavy traffic, scaling the entire application may be required even though only the Order functionality needs additional capacity.

This can increase:

* Infrastructure cost
* Deployment time
* Application complexity
* Failure impact
* Development dependency between teams

ShopSphere will solve this by separating major business capabilities into independent services.

---

# 4. Business Objective

The primary objectives are:

1. Provide a reliable e-commerce platform.
2. Allow customers to browse and purchase products.
3. Maintain persistent customer, product, cart, and order information.
4. Separate business functionality into independent microservices.
5. Allow individual services to scale independently.
6. Deploy infrastructure consistently using Terraform.
7. Containerize application services using Docker.
8. Deploy services on Kubernetes/EKS.
9. Implement automated CI/CD using Jenkins.
10. Provide centralized monitoring and logging.
11. Support troubleshooting, rollback, and incident management.
12. Create an enterprise-level DevOps project suitable for real-world practice and interviews.

---

# 5. Target Users

ShopSphere will support two primary user types.

## 5.1 Customer

A customer can:

* Register
* Login
* Browse products
* View product details
* Search products
* Add products to cart
* Update cart quantity
* Remove products from cart
* Place orders
* Make a payment through a simulated payment flow
* View order history
* Track order status

---

## 5.2 Administrator

An administrator can:

* Login to the admin application
* Add products
* Update products
* Remove/deactivate products
* Manage categories
* Update inventory
* View customers
* View orders
* Update order status
* Monitor application activity

---

# 6. Core Business Services

ShopSphere will initially contain the following core microservices.

```text
                         ShopSphere
                              |
                    API Gateway / Ingress
                              |
        +----------+----------+----------+----------+
        |          |          |          |          |
        v          v          v          v          v
      User      Product      Cart       Order     Payment
     Service    Service     Service    Service    Service
```

## 6.1 User Service

Responsible for:

* Customer registration
* Authentication
* User profile
* Customer information

Example:

```text
POST /users/register
POST /users/login
GET  /users/{id}
```

---

## 6.2 Product Service

Responsible for:

* Product creation
* Product details
* Product search
* Categories
* Product availability
* Inventory information

Example:

```text
GET  /products
GET  /products/{id}
POST /products
PUT  /products/{id}
```

---

## 6.3 Cart Service

Responsible for:

* Creating a shopping cart
* Adding products
* Updating quantity
* Removing products
* Viewing cart

Example:

```text
POST   /cart/items
GET    /cart/{userId}
PUT    /cart/items/{id}
DELETE /cart/items/{id}
```

---

## 6.4 Order Service

Responsible for:

* Creating orders
* Maintaining order items
* Calculating order totals
* Managing order status
* Order history

Example:

```text
POST /orders
GET  /orders/{id}
GET  /users/{userId}/orders
```

---

## 6.5 Payment Service

The initial project will use a **simulated payment workflow** for learning purposes.

Responsible for:

* Payment request
* Payment validation
* Payment success/failure simulation
* Payment status

Example:

```text
POST /payments
GET  /payments/{id}
```

We are not connecting to a real banking/payment provider initially.

---

# 7. Future Service — Notification Service

Later we can introduce:

```text
Notification Service
```

It can handle:

* Order confirmation
* Payment confirmation
* Shipping notification
* Email notification
* SMS simulation

Example:

```text
Order Created
     |
     v
Notification Service
     |
     +---- Email
     |
     +---- SMS
```

This service will be added after the core platform is working.

---

# 8. Database Requirement

ShopSphere requires a **real persistent relational database**.

We will use:

**PostgreSQL**

The database is not going to be a temporary mock database.

The target AWS implementation is:

```text
                    AWS VPC
                       |
                Private Subnets
                       |
                       v
              Amazon RDS PostgreSQL
```

The database must not be directly exposed to the public internet.

---

# 9. Microservices Database Design

Because this is a microservices project, we will follow the principle:

> Each service owns its business data.

Conceptually:

```text
User Service
     |
     v
 User Database


Product Service
     |
     v
Product Database


Order Service
     |
     v
 Order Database
```

This prevents every service from directly modifying another service's database.

For the learning environment, we can implement these as separate PostgreSQL databases or separate logical databases/schemas initially, while preserving clear ownership boundaries.

---

# 10. Initial Data Model

### User Data

```text
users
-------------------------
id
name
email
password_hash
created_at
updated_at
```

### Product Data

```text
products
-------------------------
id
name
description
price
stock_quantity
category_id
created_at
updated_at
```

### Category Data

```text
categories
-------------------------
id
name
description
```

### Cart Data

```text
carts
-------------------------
id
user_id
created_at
updated_at
```

```text
cart_items
-------------------------
id
cart_id
product_id
quantity
```

### Order Data

```text
orders
-------------------------
id
user_id
total_amount
status
created_at
updated_at
```

```text
order_items
-------------------------
id
order_id
product_id
quantity
price
```

### Payment Data

```text
payments
-------------------------
id
order_id
amount
status
transaction_reference
created_at
```

---

# 11. Customer Order Flow

A real-time customer scenario:

> A customer wants to purchase a laptop.

The business flow will be:

```text
Customer
   |
   | Login
   v
User Service
   |
   | Browse products
   v
Product Service
   |
   | Add laptop to cart
   v
Cart Service
   |
   | Checkout
   v
Order Service
   |
   | Payment request
   v
Payment Service
   |
   | Payment successful
   v
Order Service
   |
   | Confirm order
   v
Order Database
```

---

# 12. Detailed Order Processing

When a customer clicks **Place Order**, the backend should perform business validations.

```text
                Place Order
                     |
                     v
              Validate User
                     |
              +------+------+
              |             |
           Invalid         Valid
              |             |
              v             v
           Reject       Check Product
                            |
                     +------+------+
                     |             |
                  No Stock       Available
                     |             |
                     v             v
                  Reject       Create Order
                                    |
                                    v
                              Create Order Items
                                    |
                                    v
                              Process Payment
                                    |
                         +----------+----------+
                         |                     |
                      Failed                Success
                         |                     |
                         v                     v
                  Payment Failed        Confirm Order
                                              |
                                              v
                                         Update Stock
                                              |
                                              v
                                         Order Created
```

This gives us a realistic enterprise workflow to troubleshoot later.

---

# 13. AWS Business Requirement

The application must run inside an AWS network.

High-level architecture:

```text
                         Internet
                            |
                            v
                       Route 53
                            |
                            v
                    Load Balancer
                            |
                            v
                     Application Layer
                            |
                +-----------+-----------+
                |                       |
                v                       v
          Frontend Layer          Microservices
                                      |
             +------------------------+----------------+
             |          |          |        |           |
             v          v          v        v           v
           User     Product      Cart     Order      Payment
             |          |          |        |           |
             +----------+----------+--------+-----------+
                                      |
                                      v
                              Private Database
                                      |
                                      v
                              PostgreSQL / RDS
```

---

# 14. Network Requirement

The AWS environment will use a VPC.

Initial design:

```text
                         VPC
                          |
          +---------------+---------------+
          |                               |
          v                               v
    Public Subnets                 Private Subnets
          |                               |
          v                               |
   Load Balancer                        |
                                          |
                         +----------------+----------------+
                         |                |                |
                         v                v                v
                    User Service    Product Service   Order Service
                         |                |                |
                         +----------------+----------------+
                                          |
                                          v
                                   PostgreSQL / RDS
```

The public internet should not directly access the database.

---

# 15. Security Requirements

The platform must follow basic enterprise security practices.

Requirements:

* Database should remain private.
* Security Groups should restrict traffic.
* IAM should follow least privilege.
* Secrets must not be hardcoded into source code.
* Database credentials must not be committed to Git.
* HTTPS should be used for public application traffic.
* Application containers should run with appropriate permissions.
* Administrative endpoints should require authentication.
* Sensitive configuration should be managed through appropriate secret-management mechanisms.

---

# 16. Infrastructure as Code Requirement

All AWS infrastructure should be managed through Terraform.

Target:

```text
terraform apply
       |
       +---- VPC
       +---- Subnets
       +---- Internet Gateway
       +---- Route Tables
       +---- Security Groups
       +---- IAM
       +---- Compute
       +---- Load Balancer
       +---- PostgreSQL
       +---- Monitoring infrastructure
       |
       v
ShopSphere Environment
```

The user should be able to provision the environment from the Terraform root directory instead of manually creating every AWS resource from the console.

---

# 17. Container Requirement

Each microservice should eventually have its own container image.

```text
User Service
     |
     v
Docker Image

Product Service
     |
     v
Docker Image

Cart Service
     |
     v
Docker Image

Order Service
     |
     v
Docker Image

Payment Service
     |
     v
Docker Image
```

Images will later be stored in Amazon ECR.

---

# 18. Kubernetes Requirement

The target production-style deployment platform will be Amazon EKS.

```text
                       Amazon EKS
                           |
        +------------------+------------------+
        |          |          |        |       |
        v          v          v        v       v
      User      Product      Cart     Order   Payment
      Pod        Pods        Pods      Pods     Pods
```

Kubernetes will provide:

* Service discovery
* Pod management
* Scaling
* Rolling deployments
* Self-healing
* Resource management
* Service networking

---

# 19. CI/CD Requirement

Jenkins will automate the application delivery process.

Target flow:

```text
Developer
    |
    v
GitHub
    |
    v
Jenkins
    |
    +---- Checkout
    |
    +---- Build
    |
    +---- Unit Test
    |
    +---- SonarQube
    |
    +---- Security Scan
    |
    +---- Docker Build
    |
    +---- Push Image to ECR
    |
    v
Kubernetes / EKS
```

Each microservice should be independently buildable and deployable.

---

# 20. Monitoring Requirement

The platform must provide application and infrastructure monitoring.

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

We should eventually monitor:

* CPU
* Memory
* Pod count
* Request rate
* Request latency
* Error rate
* Application health
* Database health
* Kubernetes resources

---

# 21. Logging Requirement

Application logs should eventually be centralized.

Target:

```text
Microservices
     |
     v
Container Logs
     |
     v
Loki
     |
     v
Grafana
```

This will allow an engineer to investigate incidents across multiple services.

---

# 22. Availability Requirement

The platform should be designed so that failure of one application instance does not automatically cause complete application downtime.

For example:

```text
Order Service

       +------------+
       | Load Balancer|
       +------+------+
              |
       +------+------+
       |             |
       v             v
    Order Pod 1   Order Pod 2
       |             |
       +------+------+
              |
              v
          Order DB
```

If one pod fails, Kubernetes should be able to replace it.

---

# 23. Scalability Requirement

Different services may experience different traffic levels.

Example:

```text
Normal traffic:

Product Service
     |
     +---- 2 replicas


Festival Sale:

Product Service
     |
     +---- 5 replicas
     |
     +---- 8 replicas
     |
     +---- 10 replicas
```

The goal is to allow individual services to scale independently.

---

# 24. Failure Scenarios We Will Practice

This project should not only demonstrate successful deployment.

We will intentionally create and troubleshoot incidents.

Examples:

### Scenario 1 — Product Service Down

```text
Frontend
   |
   v
Product Service
   X
  DOWN
```

We investigate:

```text
kubectl get pods
kubectl describe pod
kubectl logs
kubectl get events
```

---

### Scenario 2 — Database Connection Failure

```text
Order Service
     |
     X
     |
PostgreSQL
```

We investigate:

* Security Group
* Database endpoint
* Port
* Credentials
* Application configuration
* Network connectivity

---

### Scenario 3 — High CPU

```text
Order Service
     |
     v
CPU = 95%
     |
     v
HPA
     |
     v
More Pods
```

---

### Scenario 4 — Bad Deployment

```text
Version 1
    |
    v
Version 2
    |
    X
Application errors
```

We will practice:

```text
Rollback
   |
   v
Version 1
```

---

# 25. Environment Requirement

Initially:

```text
Dev
```

Later:

```text
Dev
 |
 v
QA
 |
 v
Stage
 |
 v
Production
```

The same Terraform architecture should be reusable across environments with different configuration values.

---

# 26. Repository Requirement

Target repository structure:

```text
shopsphere/
│
├── README.md
│
├── docs/
│   ├── business-requirements.md
│   ├── architecture.md
│   ├── database-design.md
│   ├── deployment-flow.md
│   └── troubleshooting.md
│
├── application/
│   ├── frontend/
│   │
│   └── services/
│       ├── user-service/
│       ├── product-service/
│       ├── cart-service/
│       ├── order-service/
│       └── payment-service/
│
└── terraform/
    ├── main.tf
    ├── provider.tf
    ├── variables.tf
    ├── terraform.tfvars
    ├── outputs.tf
    ├── versions.tf
    │
    └── modules/
        ├── vpc/
        ├── security-groups/
        ├── compute/
        ├── database/
        ├── load-balancer/
        └── iam/
```

---

# 27. Business Success Criteria

ShopSphere will be considered successful when:

* Customers can register and login.
* Customers can browse products.
* Customers can add products to a cart.
* Customers can place orders.
* Payment workflow can be simulated.
* Orders are persisted in PostgreSQL.
* Product inventory is maintained.
* Microservices can run independently.
* AWS infrastructure is provisioned through Terraform.
* Application components can be containerized.
* Services can be deployed on Kubernetes/EKS.
* CI/CD can automatically build and deploy services.
* Monitoring is available.
* Logs can be centrally investigated.
* Application failures can be diagnosed.
* Deployments can be rolled back safely.

---

# 28. Real-Time Enterprise Example

Imagine ShopSphere is running during a major online sale.

Thousands of customers start browsing products.

```text
Customers
   |
   v
Load Balancer
   |
   +----------------------+
   |                      |
   v                      v
Product Pod 1          Product Pod 2
   |                      |
   +----------+-----------+
              |
              v
        Product Database
```

Product traffic increases significantly.

Kubernetes can scale the Product Service independently.

At the same time, the Order Service may receive a different traffic pattern.

```text
Product Service
     |
     +---- 10 replicas

Order Service
     |
     +---- 5 replicas

Payment Service
     |
     +---- 3 replicas
```

This is one of the major business reasons for adopting microservices.

---

# 29. Why Terraform?

Without Terraform:

```text
Engineer
   |
   +---- AWS Console
   +---- Create VPC
   +---- Create Subnets
   +---- Create SG
   +---- Create DB
   +---- Create Compute
   +---- Configure LB
   +---- Repeat manually
```

This creates risks of:

* Manual mistakes
* Configuration differences
* Difficult reproduction
* Slow environment creation

With Terraform:

```text
Terraform Code
      |
      v
terraform plan
      |
      v
terraform apply
      |
      v
AWS Environment
```

The infrastructure becomes:

* Repeatable
* Version controlled
* Reviewable
* Reproducible
* Automated

---

# 30. Documentation Standard for This Project

For every important file we create or modify, documentation will answer the following questions.

### 1. What is this file?

Explain its purpose in simple terms.

### 2. Why are we adding it?

Explain the technical reason.

### 3. Why does the business need it?

Connect the file to the business requirement.

### 4. Was it created or modified?

Clearly mention:

```text
Status: Created
```

or:

```text
Status: Modified
```

### 5. What does it contain?

Explain the important configuration/code.

### 6. How does it connect to other files?

Show the dependency.

```text
main.tf
   |
   v
module/vpc
   |
   v
outputs.tf
   |
   v
module/database
```

### 7. What happens if we don't have it?

Explain the practical impact.

### 8. Real-time enterprise example

Give one realistic office scenario.

### 9. Commands/configuration

Explain the important commands and configuration.

### 10. Validation

Explain how an engineer verifies the implementation.

### 11. Troubleshooting / RCA

Explain common failures and how to investigate them.

### 12. Rollback / Cleanup

Explain how to safely undo the change.

### 13. Interview Explanation

Provide a natural answer that an experienced DevOps engineer could give in an interview.

---

# 31. Project Development Principle

We will build the project incrementally.

We will **not** create hundreds of files on Day 1.

The development approach will be:

```text
Business Requirements
        |
        v
Microservices Design
        |
        v
Database Design
        |
        v
Repository Structure
        |
        v
Application Development
        |
        v
Docker
        |
        v
AWS Infrastructure
        |
        v
Terraform
        |
        v
Kubernetes / EKS
        |
        v
Jenkins CI/CD
        |
        v
Monitoring
        |
        v
Logging
        |
        v
Incidents / RCA / Rollback
```

Each layer will be introduced only when we understand why it is required.

---

# 32. Final Business Requirement

The final ShopSphere platform should provide an enterprise-style e-commerce application where:

```text
                         CUSTOMER
                            |
                            v
                       FRONTEND
                            |
                            v
                    API / LOAD BALANCER
                            |
          +-----------------+-----------------+
          |        |        |        |        |
          v        v        v        v        v
        USER    PRODUCT    CART    ORDER    PAYMENT
       SERVICE  SERVICE   SERVICE  SERVICE  SERVICE
          |        |        |        |        |
          v        v        v        v        v
       PostgreSQL Databases / Persistent Storage
                            |
                            v
                           AWS
                            |
                  Terraform Managed
                            |
                            v
                         EKS
                            |
                +-----------+-----------+
                |                       |
                v                       v
             CI/CD                 Observability
            Jenkins             Prometheus/Grafana
                                      +
                                    Loki
```

The ultimate objective is not simply to deploy an application.

The objective is to understand how an enterprise DevOps engineer designs, provisions, deploys, operates, monitors, troubleshoots, and maintains a real microservices platform.

---

# 33. Next Project Document

After completing this Business Requirements document, the next document will be:

```text
docs/architecture.md
```

It will define:

```text
Frontend
   ↓
Load Balancer / API Layer
   ↓
Microservices
   ↓
Service-to-Service Communication
   ↓
Databases
   ↓
AWS VPC
   ↓
Terraform
```

We will then design the **exact AWS architecture and explain why every component exists before writing Terraform code**.
