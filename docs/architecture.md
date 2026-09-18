# ShopSphere — Enterprise E-Commerce Platform

## Architecture Design

---

# 1. Document Information

| Item                   | Details                 |
| ---------------------- | ----------------------- |
| Project                | ShopSphere              |
| Architecture           | Microservices           |
| Cloud                  | AWS                     |
| Infrastructure as Code | Terraform               |
| Frontend               | Web Application         |
| Backend                | Microservices           |
| Database               | PostgreSQL              |
| Containers             | Docker                  |
| Orchestration          | Kubernetes / Amazon EKS |
| CI/CD                  | Jenkins                 |
| Container Registry     | Amazon ECR              |
| Monitoring             | Prometheus + Grafana    |
| Logging                | Loki                    |
| DNS                    | Amazon Route 53         |
| Load Balancing         | AWS Load Balancer       |
| Initial Environment    | Development             |
| Future Environment     | Production              |

---

# 2. Purpose of This Document

This document explains the complete technical architecture of ShopSphere.

The objective is to understand:

* What components exist
* Why each component exists
* How components communicate
* How users access the application
* How microservices communicate
* How databases are accessed
* How AWS networking works
* How Terraform provisions the infrastructure
* How Docker and Kubernetes are used
* How CI/CD deploys the application
* How monitoring and logging work
* How failures are handled

---

# 3. High-Level Architecture

The complete ShopSphere architecture looks like this:

```text
                              INTERNET
                                  |
                                  v
                            Route 53
                               DNS
                                  |
                                  v
                         Application Entry
                         Load Balancer
                                  |
                    +-------------+-------------+
                    |                           |
                    v                           v
               Frontend                  API / Ingress
                    |                           |
                    |             +-------------+-------------+
                    |             |             |             |
                    |             v             v             v
                    |          User          Product        Cart
                    |         Service         Service       Service
                    |             |             |             |
                    |             v             v             v
                    |          User DB       Product DB    Cart DB
                    |                                             |
                    |                                             v
                    |                                          Order
                    |                                         Service
                    |                                             |
                    |                                             v
                    |                                         Order DB
                    |                                             |
                    |                                             v
                    |                                         Payment
                    |                                         Service
                    |                                             |
                    |                                             v
                    |                                        Payment DB
                    |
                    +-------------------+
                                        |
                                        v
                                  AWS VPC
                                        |
                          +-------------+-------------+
                          |                           |
                          v                           v
                    Public Subnets              Private Subnets
                                                       |
                                                       v
                                               Application / DB
```

---

# 4. Simple Architecture

Before understanding the detailed architecture, remember this simple flow:

```text
User
  |
  v
Frontend
  |
  v
Load Balancer
  |
  v
Microservices
  |
  v
PostgreSQL
```

Everything else exists to make this system:

* Secure
* Scalable
* Highly available
* Observable
* Automated
* Maintainable

---

# 5. Real-Time Customer Request

Let's understand the architecture using a real business example.

### Scenario

A customer opens ShopSphere and searches for:

```text
"iPhone"
```

The request flow is:

```text
Customer
   |
   | HTTPS
   v
Route 53
   |
   v
Load Balancer
   |
   v
Frontend
   |
   | API Request
   v
Product Service
   |
   v
Product Database
   |
   v
Products
   |
   v
Frontend
   |
   v
Customer
```

The customer does not directly connect to PostgreSQL.

---

# 6. Component Overview

ShopSphere contains several architectural layers.

```text
+--------------------------------------------------+
|                    USERS                         |
+--------------------------------------------------+
                     |
                     v
+--------------------------------------------------+
|              DNS / ROUTE 53                     |
+--------------------------------------------------+
                     |
                     v
+--------------------------------------------------+
|             LOAD BALANCER                       |
+--------------------------------------------------+
                     |
          +----------+----------+
          |                     |
          v                     v
     FRONTEND                API LAYER
                                |
             +------------------+------------------+
             |        |         |        |         |
             v        v         v        v         v
           USER    PRODUCT     CART    ORDER    PAYMENT
          SERVICE  SERVICE    SERVICE  SERVICE   SERVICE
             |        |         |        |         |
             v        v         v        v         v
            DB       DB        DB       DB        DB
```

---

# 7. Layer 1 — User

The customer is outside AWS.

Example:

```text
Laptop
Mobile
Browser
   |
   v
ShopSphere
```

Users access the application through HTTPS.

---

# 8. Layer 2 — Route 53

Amazon Route 53 provides DNS.

Example:

```text
shopsphere.example.com
             |
             v
         Route 53
             |
             v
      Load Balancer
```

Instead of customers remembering an IP address, they use a domain name.

### Why do we need it?

Without DNS:

```text
Customer
   |
   v
IP Address
```

With DNS:

```text
Customer
   |
   v
shopsphere.example.com
   |
   v
Route 53
   |
   v
Load Balancer
```

This also gives us flexibility to change infrastructure without changing the public domain name.

---

# 9. Layer 3 — Load Balancer

The Load Balancer is the public entry point for application traffic.

```text
Internet
   |
   v
Load Balancer
   |
   +--------+--------+
   |                 |
   v                 v
Frontend Instance  Frontend Instance
```

For the Kubernetes implementation, the AWS Load Balancer will eventually route traffic toward Kubernetes services/ingress.

### Why?

It provides:

* Traffic distribution
* High availability
* Health checks
* TLS termination
* Routing

---

# 10. Layer 4 — Frontend

The frontend is the user-facing application.

Example:

```text
Browser
   |
   v
ShopSphere UI
```

The frontend provides screens such as:

```text
Login
Products
Product Details
Cart
Checkout
Orders
Admin
```

The frontend does not directly connect to PostgreSQL.

Instead:

```text
Frontend
   |
   v
Backend API
   |
   v
Database
```

This protects the database and keeps business logic inside backend services.

---

# 11. Layer 5 — API / Ingress Layer

The API layer routes requests to the correct microservice.

Example:

```text
/api/users/*      ---> User Service

/api/products/*  ---> Product Service

/api/cart/*      ---> Cart Service

/api/orders/*    ---> Order Service

/api/payments/*  ---> Payment Service
```

Architecture:

```text
                       API Entry
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
     /api/users       /api/products     /api/orders
          |                |                |
          v                v                v
      User Svc         Product Svc       Order Svc
```

In Kubernetes, this routing can be implemented using an Ingress/API gateway layer.

---

# 12. Layer 6 — Microservices

ShopSphere uses independent services.

```text
+-------------+
| User Service|
+-------------+

+----------------+
| Product Service|
+----------------+

+-------------+
| Cart Service|
+-------------+

+--------------+
| Order Service|
+--------------+

+----------------+
| Payment Service|
+----------------+
```

Each service owns a specific business responsibility.

---

# 13. User Service

The User Service manages:

* Registration
* Login
* User profile
* Authentication-related operations

Example:

```text
POST /users/register

POST /users/login

GET /users/{id}
```

Architecture:

```text
Frontend
   |
   v
User Service
   |
   v
User Database
```

---

# 14. Product Service

The Product Service manages:

* Products
* Categories
* Product details
* Inventory information

Example:

```text
GET /products

GET /products/{id}

POST /products

PUT /products/{id}
```

Architecture:

```text
Frontend
   |
   v
Product Service
   |
   v
Product Database
```

---

# 15. Cart Service

The Cart Service manages:

* Customer cart
* Cart items
* Quantity
* Remove item
* Update item

Architecture:

```text
Frontend
   |
   v
Cart Service
   |
   v
Cart Database
```

---

# 16. Order Service

The Order Service manages:

* Order creation
* Order items
* Order total
* Order status
* Order history

Architecture:

```text
Frontend
   |
   v
Order Service
   |
   v
Order Database
```

---

# 17. Payment Service

For this project, payment will initially be simulated.

Architecture:

```text
Order Service
     |
     | Payment Request
     v
Payment Service
     |
     v
Payment Database
```

Example:

```text
Payment
   |
   +---- SUCCESS
   |
   +---- FAILED
```

Later, we can simulate external payment-provider communication.

---

# 18. Microservice Communication

Microservices need to communicate with each other.

Example:

```text
Order Service
     |
     | Product information
     v
Product Service
```

Another example:

```text
Order Service
     |
     | Payment request
     v
Payment Service
```

Conceptually:

```text
User
 |
 v
Frontend
 |
 v
Order Service
 |
 +----> Product Service
 |
 +----> Payment Service
 |
 v
Order Database
```

We will initially use synchronous REST APIs for simplicity.

Later, we can introduce asynchronous communication using a messaging system if the project requires it.

---

# 19. Database Architecture

The database is a critical component.

The target architecture is:

```text
                         PostgreSQL
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
       User DB            Product DB           Order DB
          |                   |                   |
          v                   v                   v
      User Service        Product Service      Order Service
```

The important microservices principle is:

> A service owns its business data.

For example:

```text
User Service
     |
     +---- User Database

Product Service
     |
     +---- Product Database

Order Service
     |
     +---- Order Database
```

Another service should not directly update another service's tables.

---

# 20. AWS Database Architecture

The database should be private.

```text
                         INTERNET
                            |
                            X
                            |
                     No Direct Access
                            |
                            v
                     Private Subnet
                            |
                            v
                    PostgreSQL / RDS
```

Only authorized application components should be able to connect to the database.

---

# 21. AWS VPC Architecture

The entire environment will exist inside an AWS VPC.

```text
+------------------------------------------------------+
|                      AWS VPC                         |
|                                                      |
|   +-------------------+  +-------------------+       |
|   | Public Subnet     |  | Public Subnet     |       |
|   |                   |  |                   |       |
|   | Load Balancer     |  | Load Balancer     |       |
|   +-------------------+  +-------------------+       |
|                                                      |
|   +-------------------+  +-------------------+       |
|   | Private Subnet    |  | Private Subnet    |       |
|   |                   |  |                   |       |
|   | Applications      |  | Applications      |       |
|   |                   |  |                   |       |
|   +-------------------+  +-------------------+       |
|                                                      |
|   +----------------------------------------------+   |
|   | Private Database Subnets                     |   |
|   |                                              |   |
|   | PostgreSQL / RDS                             |   |
|   +----------------------------------------------+   |
|                                                      |
+------------------------------------------------------+
```

---

# 22. Public vs Private Subnets

### Public Subnet

Resources that need internet-facing access can be placed behind appropriate public networking.

Example:

```text
Internet
   |
   v
Public Subnet
   |
   v
Load Balancer
```

### Private Subnet

Internal application and database components should not be directly exposed.

```text
Private Subnet
     |
     +---- Microservices
     |
     +---- Database
```

---

# 23. Security Group Architecture

Security Groups control network traffic.

Example:

```text
Internet
   |
   | HTTPS :443
   v
Load Balancer SG
   |
   | Application traffic
   v
Application SG
   |
   | PostgreSQL :5432
   v
Database SG
```

Conceptually:

```text
Internet
   |
   | 443
   v
[LB-SG]
   |
   | Application Port
   v
[APP-SG]
   |
   | 5432
   v
[DB-SG]
```

The database Security Group should allow PostgreSQL traffic only from the appropriate application security group.

---

# 24. Docker Architecture

Every microservice will eventually become a container.

```text
User Service
      |
      v
Docker Image
      |
      v
Container

Product Service
      |
      v
Docker Image
      |
      v
Container
```

Example:

```text
user-service:v1
product-service:v1
cart-service:v1
order-service:v1
payment-service:v1
```

Images will eventually be stored in Amazon ECR.

---

# 25. Amazon ECR

Amazon ECR will act as the container image registry.

```text
Developer
    |
    v
Docker Build
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

Example:

```text
ECR
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

---

# 26. Kubernetes / EKS Architecture

Amazon EKS will eventually run the microservices.

```text
                         EKS Cluster
                              |
          +-------------------+-------------------+
          |                   |                   |
          v                   v                   v
      User Pods          Product Pods          Order Pods
          |                   |                   |
          v                   v                   v
      User Svc            Product Svc           Order Svc
```

Kubernetes provides:

* Scheduling
* Service discovery
* Self-healing
* Scaling
* Rolling deployment
* Resource management

---

# 27. Kubernetes Service Discovery

Inside Kubernetes, services can communicate using Kubernetes Service DNS.

Conceptually:

```text
Order Pod
   |
   | HTTP
   v
product-service
   |
   v
Product Pods
```

The Order Service does not need to know the individual Product Pod IP.

Kubernetes manages the service discovery.

---

# 28. Kubernetes Scaling

Suppose Product traffic increases.

```text
Normal:

Product Service
     |
     +---- Pod 1
     +---- Pod 2
```

During high traffic:

```text
Product Service
     |
     +---- Pod 1
     +---- Pod 2
     +---- Pod 3
     +---- Pod 4
     +---- Pod 5
```

The service can scale independently.

---

# 29. Terraform Architecture

Terraform manages the AWS infrastructure.

```text
                    Terraform
                        |
       +----------------+----------------+
       |                |                |
       v                v                v
      VPC           Security Groups      IAM
       |
       +---- Subnets
       |
       +---- Routing
       |
       +---- Load Balancer
       |
       +---- EKS
       |
       +---- RDS
       |
       +---- Supporting Infrastructure
```

---

# 30. Single Terraform Apply Requirement

Our project has an important requirement:

> From the Terraform root directory, the complete environment should be provisionable through one Terraform workflow.

Example:

```bash
cd terraform

terraform init

terraform plan

terraform apply
```

Terraform should understand the resource dependency graph.

Example:

```text
VPC
 |
 +---- Subnets
 |
 +---- Security Groups
 |
 +---- EKS
 |
 +---- RDS
 |
 +---- Load Balancer
```

We don't manually run Terraform inside every module.

---

# 31. Terraform Module Architecture

Internally we will keep Terraform modular.

```text
terraform/
│
├── main.tf
├── provider.tf
├── variables.tf
├── outputs.tf
├── versions.tf
│
└── modules/
    │
    ├── vpc/
    │
    ├── security-groups/
    │
    ├── database/
    │
    ├── compute/
    │
    ├── load-balancer/
    │
    └── iam/
```

The root Terraform configuration calls the modules.

```text
Root Terraform
      |
      +---- VPC Module
      |
      +---- Security Module
      |
      +---- Database Module
      |
      +---- Compute Module
      |
      +---- Load Balancer Module
      |
      +---- IAM Module
```

---

# 32. CI/CD Architecture

Jenkins will automate application delivery.

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
    +---- Unit Tests
    |
    +---- SonarQube
    |
    +---- Security Scan
    |
    +---- Docker Build
    |
    +---- Push to ECR
    |
    v
Amazon EKS
```

Each microservice can have its own pipeline or a shared pipeline pattern.

---

# 33. Monitoring Architecture

Prometheus collects metrics.

```text
Microservices
     |
     v
Metrics
     |
     v
Prometheus
     |
     v
Grafana
```

We can monitor:

```text
CPU
Memory
Request Count
Request Latency
HTTP Errors
Pod Count
Database Metrics
```

---

# 34. Logging Architecture

Container logs will eventually be centralized.

```text
Microservices
     |
     v
Container Logs
     |
     v
Log Collector
     |
     v
Loki
     |
     v
Grafana
```

Example troubleshooting:

```text
Customer
   |
   | Order failed
   v
Order Service
   |
   v
Logs
   |
   v
Loki
   |
   v
Grafana
```

An engineer can search the logs to identify the failure.

---

# 35. Complete DevOps Architecture

The complete lifecycle becomes:

```text
                         Developer
                             |
                             v
                           GitHub
                             |
                             v
                          Jenkins
                             |
             +---------------+---------------+
             |               |               |
             v               v               v
           Build          Test           Security
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
                    +------------------------+----------------------+
                    |          |             |          |           |
                    v          v             v          v           v
                  User      Product         Cart       Order      Payment
                 Service    Service        Service    Service     Service
                    |          |             |          |           |
                    +----------+-------------+----------+-----------+
                                               |
                                               v
                                         PostgreSQL
                                               |
                            +------------------+------------------+
                            |                                     |
                            v                                     v
                       Prometheus                              Loki
                            |                                     |
                            v                                     v
                         Grafana                              Grafana
```

---

# 36. Infrastructure and Application Relationship

This is an important interview concept.

Terraform manages:

```text
Infrastructure
     |
     +---- VPC
     +---- Networking
     +---- Security
     +---- EKS
     +---- RDS
     +---- IAM
     +---- Load Balancer
```

Application deployment manages:

```text
Application
     |
     +---- Docker Images
     +---- Kubernetes Deployments
     +---- Kubernetes Services
     +---- Configurations
     +---- Secrets
```

Therefore:

```text
Terraform
   |
   v
Infrastructure

CI/CD + Kubernetes
   |
   v
Application
```

We should keep these responsibilities clear.

---

# 37. End-to-End Customer Order Flow

Let's follow one complete business transaction.

Customer:

```text
"I want to buy a laptop."
```

Flow:

```text
Customer
   |
   v
Route 53
   |
   v
Load Balancer
   |
   v
Frontend
   |
   v
Product Service
   |
   v
Product Database
```

Customer adds the laptop to cart:

```text
Frontend
   |
   v
Cart Service
   |
   v
Cart Database
```

Customer clicks Place Order:

```text
Frontend
   |
   v
Order Service
   |
   +---- Check Product
   |
   +---- Check Stock
   |
   +---- Create Order
   |
   +---- Create Order Items
   |
   +---- Request Payment
   |
   v
Payment Service
   |
   v
Payment Database
```

Successful payment:

```text
Payment Service
      |
      v
SUCCESS
      |
      v
Order Service
      |
      v
Order Confirmed
```

---

# 38. Failure Handling Example

Suppose Payment Service is down.

```text
Order Service
      |
      v
Payment Service
      X
     DOWN
```

The Order Service should not silently report a successful payment.

Possible flow:

```text
Payment Request
      |
      v
Payment Service
      X
      |
      v
Payment Failed / Timeout
      |
      v
Order Status
"PENDING_PAYMENT"
```

This gives us a realistic incident scenario for later troubleshooting.

---

# 39. High Availability Design

Critical components should avoid single points of failure where practical.

Example:

```text
                Load Balancer
                 /         \
                v           v
           Application  Application
             Pod 1         Pod 2
                \           /
                 \         /
                  Database
```

For AWS managed services such as RDS, we can later configure appropriate high-availability options such as Multi-AZ based on the environment and cost requirements.

---

# 40. Security Architecture

The security model is:

```text
                    INTERNET
                       |
                       | HTTPS
                       v
                  Load Balancer
                       |
                       v
                 Application
                       |
                       | Private Network
                       v
                    Database
```

Security principles:

* Public traffic enters through controlled entry points.
* Internal services communicate over private networking.
* Database is not publicly exposed.
* Security Groups restrict traffic.
* IAM follows least privilege.
* Secrets are not stored in Git.
* HTTPS protects external communication.
* Authentication protects user/admin operations.

---

# 41. Why Microservices?

The business reason is independent scalability and ownership.

Example:

```text
Product traffic = HIGH
Order traffic   = NORMAL
Payment traffic = LOW
```

With a monolith:

```text
Entire Application
       |
       v
Scale Everything
```

With microservices:

```text
Product Service
       |
       v
Scale Product Service

Order Service
       |
       v
Keep normal capacity

Payment Service
       |
       v
Keep normal capacity
```

Microservices also allow teams to deploy individual business capabilities independently, although they introduce additional operational complexity.

---

# 42. What Happens If We Don't Have This Architecture?

### Without Load Balancer

Traffic management and high availability become more difficult.

### Without Private Networking

Internal resources may be unnecessarily exposed.

### Without Microservices

Individual business components become more tightly coupled.

### Without Database Persistence

Orders and customer information could be lost when application instances are replaced.

### Without Terraform

Infrastructure creation becomes heavily dependent on manual processes.

### Without Docker

Application environments can become inconsistent.

### Without Kubernetes

Running and scaling many microservices becomes harder to manage.

### Without CI/CD

Deployments require more manual work.

### Without Monitoring

Engineers may not know when the system is degrading.

### Without Centralized Logging

Troubleshooting distributed failures becomes much harder.

---

# 43. Architecture File-to-File Relationship

As we build the project, the important relationship will look approximately like this:

```text
Business Requirements
        |
        v
Architecture
        |
        v
Database Design
        |
        v
Application Design
        |
        v
Docker Configuration
        |
        v
Terraform
        |
        +---- VPC
        +---- Security
        +---- EKS
        +---- RDS
        |
        v
Kubernetes Manifests
        |
        v
CI/CD
        |
        v
Monitoring
        |
        v
Logging
```

Each document and configuration file will have a specific responsibility.

---

# 44. Future Repository Architecture

Target repository:

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
│   │
│   ├── frontend/
│   │
│   └── services/
│       ├── user-service/
│       ├── product-service/
│       ├── cart-service/
│       ├── order-service/
│       └── payment-service/
│
├── docker/
│
├── kubernetes/
│
├── jenkins/
│
└── terraform/
    │
    ├── main.tf
    ├── provider.tf
    ├── variables.tf
    ├── outputs.tf
    ├── versions.tf
    │
    └── modules/
        ├── vpc/
        ├── security-groups/
        ├── database/
        ├── compute/
        ├── load-balancer/
        └── iam/
```

This is our target structure, not something we need to create all at once.

---

# 45. Architecture Validation

Before moving to implementation, we should be able to answer:

### Application

* Can a customer access the frontend?
* Can the frontend call the API?
* Can the API reach the correct microservice?
* Can microservices communicate correctly?

### Database

* Can the application connect to PostgreSQL?
* Is the database private?
* Are database credentials protected?

### AWS

* Is the VPC correctly configured?
* Are public and private subnets correctly separated?
* Are Security Groups correctly configured?
* Can internal resources communicate?

### Terraform

```bash
terraform fmt
terraform validate
terraform plan
```

The plan should show the expected infrastructure changes.

### Kubernetes

Later we will validate:

```bash
kubectl get nodes
kubectl get pods
kubectl get svc
kubectl get ingress
```

---

# 46. Common Architecture Troubleshooting

## Problem 1 — Frontend Cannot Reach Backend

Check:

```text
Frontend
   |
   X
Backend
```

Investigate:

* DNS
* Load Balancer
* Ingress
* Security Groups
* Service configuration
* Network policies
* Application logs

---

## Problem 2 — Backend Cannot Reach Database

Flow:

```text
Backend
   |
   X
PostgreSQL
```

Check:

```text
Database endpoint
Port 5432
Security Group
Subnet routing
Credentials
Application configuration
```

---

## Problem 3 — Pod Is Running But Application Is Not Accessible

A running pod does not automatically mean the application is reachable.

Check:

```bash
kubectl get pods
kubectl get svc
kubectl get ingress
kubectl describe pod <pod-name>
kubectl logs <pod-name>
```

---

## Problem 4 — Terraform Creates Unexpected Resources

Check:

```bash
terraform plan
terraform state list
terraform state show <resource>
```

Then identify the module/resource responsible.

---

# 47. Rollback Strategy

Rollback depends on the layer.

### Application Rollback

Use Kubernetes deployment rollback.

```bash
kubectl rollout history deployment/<deployment-name>
kubectl rollout undo deployment/<deployment-name>
```

### Terraform Rollback

Terraform does not work like a traditional application deployment rollback.

We should:

1. Review the Terraform plan.
2. Correct the configuration.
3. Apply the corrected configuration.
4. Use version control to restore known-good infrastructure code when appropriate.

Example:

```text
Git
 |
 v
Known Good Terraform Version
 |
 v
terraform plan
 |
 v
terraform apply
```

### Database Rollback

Database changes require additional care.

We will later implement migration and backup strategies rather than treating database rollback as a simple Terraform rollback.

---

# 48. Enterprise Interview Explanation

### Interview Question

**"Explain the architecture of your e-commerce application."**

### Natural Answer

> "I worked on an e-commerce platform called ShopSphere based on a microservices architecture. The frontend is exposed through a controlled public entry point, while backend services run inside the AWS environment. We separated business capabilities such as User, Product, Cart, Order, and Payment into independent services.
>
> The services use PostgreSQL for persistent business data, with clear data ownership between services. The databases are kept private and application traffic is controlled using AWS networking and Security Groups.
>
> We containerize the services using Docker and deploy them on Kubernetes, with Amazon EKS as the target AWS platform. Terraform manages the underlying AWS infrastructure such as VPC, networking, security, EKS, databases, IAM, and load-balancing components.
>
> Jenkins handles CI/CD by building, testing, scanning, creating container images, pushing them to ECR, and deploying them to EKS. Prometheus and Grafana are used for monitoring, while Loki is used for centralized logging.
>
> The overall design allows us to scale individual services, deploy them independently, and troubleshoot failures at the service, infrastructure, database, and application levels."

---

# 49. Key Architecture Principles

The ShopSphere architecture follows these principles:

1. **Separation of concerns**
2. **Microservice ownership**
3. **Private database access**
4. **Infrastructure as Code**
5. **Immutable container images**
6. **Automated deployment**
7. **Independent service scaling**
8. **Least-privilege security**
9. **Observability**
10. **Failure isolation**
11. **Version-controlled infrastructure**
12. **Repeatable deployments**

---

# 50. Final Architecture

The architecture we are targeting is:

```text
                              USERS
                                |
                                v
                         HTTPS / DOMAIN
                                |
                                v
                           Route 53
                                |
                                v
                      AWS Load Balancer
                                |
                  +-------------+-------------+
                  |                           |
                  v                           v
              Frontend                API / Ingress
                                              |
               +------------------------------+-----------------------------+
               |              |               |              |              |
               v              v               v              v              v
          User Service   Product Service   Cart Service   Order Service   Payment Service
               |              |               |              |              |
               v              v               v              v              v
            User DB       Product DB        Cart DB        Order DB       Payment DB
               |              |               |              |              |
               +--------------+---------------+--------------+--------------+
                                              |
                                              v
                                      Private AWS Network
                                              |
                                              v
                                            VPC
                                              |
                              +---------------+---------------+
                              |                               |
                              v                               v
                       Public Subnets                  Private Subnets
                              |                               |
                              v                               v
                       Load Balancer                  EKS / Applications
                                                              |
                                                              v
                                                         PostgreSQL
                                                              |
                                                              v
                                                         Observability
                                                     /               \
                                                    v                 v
                                               Prometheus           Loki
                                                    |                 |
                                                    +-------+---------+
                                                            |
                                                            v
                                                         Grafana


Terraform
    |
    +---- VPC
    +---- Subnets
    +---- Routing
    +---- Security Groups
    +---- IAM
    +---- EKS
    +---- RDS
    +---- Load Balancer
    +---- Supporting AWS Infrastructure


Jenkins
    |
    +---- GitHub
    +---- Build
    +---- Test
    +---- SonarQube
    +---- Security Scan
    +---- Docker Build
    +---- ECR
    +---- EKS
```

---

# 51. Architecture Decision Summary

| Area               | Decision                            |
| ------------------ | ----------------------------------- |
| Application style  | Microservices                       |
| Frontend           | Web application                     |
| Backend            | Independent business services       |
| Core services      | User, Product, Cart, Order, Payment |
| Database           | PostgreSQL                          |
| Database strategy  | Service-owned business data         |
| Cloud              | AWS                                 |
| Network            | VPC with public/private separation  |
| Public entry       | Load Balancer                       |
| Containers         | Docker                              |
| Registry           | Amazon ECR                          |
| Orchestration      | Amazon EKS                          |
| Infrastructure     | Terraform                           |
| CI/CD              | Jenkins                             |
| Monitoring         | Prometheus + Grafana                |
| Logging            | Loki + Grafana                      |
| DNS                | Route 53                            |
| Initial deployment | Development environment             |
| Provisioning goal  | Single root Terraform workflow      |

---

# 52. Next Document

The next major document is:

```text
docs/database-design.md
```

Before writing Terraform, we will understand the database properly:

```text
User Service
     |
     v
User Data

Product Service
     |
     v
Product Data

Cart Service
     |
     v
Cart Data

Order Service
     |
     v
Order Data

Payment Service
     |
     v
Payment Data
```

We will document:

* Database requirements
* PostgreSQL architecture
* Tables
* Columns
* Primary keys
* Foreign keys
* Relationships
* Indexes
* Transactions
* Service-to-database ownership
* Order transaction flow
* Database security
* RDS architecture
* Terraform database requirements
* Validation
* Troubleshooting
* Backup/recovery
* Interview questions
