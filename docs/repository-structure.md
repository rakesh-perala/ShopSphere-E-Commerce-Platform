# ShopSphere — Repository Structure

## 1. Document Information

| Item                   | Details                                     |
| ---------------------- | ------------------------------------------- |
| Project                | ShopSphere — Enterprise E-Commerce Platform |
| Document               | Repository Structure                        |
| Architecture           | Microservices                               |
| Infrastructure as Code | Terraform                                   |
| Containerization       | Docker                                      |
| Orchestration          | Kubernetes / Amazon EKS                     |
| CI/CD                  | Jenkins                                     |
| Database               | PostgreSQL / Amazon RDS                     |
| Monitoring             | Prometheus / Grafana                        |
| Logging                | Loki                                        |
| Source Control         | Git                                         |
| Status                 | Design                                      |

---

# 2. Purpose

This document explains the Git repository structure for the ShopSphere enterprise e-commerce platform.

The repository will contain:

* Application source code
* Microservices
* Frontend
* Docker configuration
* Kubernetes manifests
* Helm charts
* Jenkins pipelines
* Terraform infrastructure
* Database migrations
* Monitoring configuration
* Logging configuration
* Project documentation

The most important infrastructure rule is:

> **All AWS infrastructure must be created and managed through Terraform.**

We should not manually create production infrastructure from the AWS Console as part of the documented deployment process.

---

# 3. High-Level Repository

Target structure:

```text
shopsphere/
│
├── README.md
│
├── docs/
│   ├── business-requirements.md
│   ├── architecture.md
│   ├── database-design.md
│   ├── repository-structure.md
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
├── helm/
│
├── jenkins/
│
├── monitoring/
│
├── logging/
│
└── terraform/
    │
    ├── main.tf
    ├── provider.tf
    ├── versions.tf
    ├── variables.tf
    ├── outputs.tf
    ├── terraform.tfvars.example
    └── modules/
        ├── vpc/
        ├── security-groups/
        ├── iam/
        ├── ecr/
        ├── eks/
        ├── database/
        └── load-balancer/
```

This is the **target architecture**.

We will not create every folder and file immediately.

They will be introduced as the project progresses.

---

# 4. Why Repository Structure Matters

In an enterprise project, repository organization is important because multiple teams work on the same codebase.

For example:

```text
Developer
   |
   +--> Application
   |
   +--> Docker
   |
   +--> Kubernetes

DevOps Engineer
   |
   +--> Jenkins
   |
   +--> Terraform

SRE
   |
   +--> Monitoring
   |
   +--> Logging
```

A clear structure helps everyone understand where their responsibility starts and ends.

---

# 5. Repository Ownership

A simple ownership model:

```text
                    ShopSphere Repository
                            |
        +-------------------+-------------------+
        |                   |                   |
        v                   v                   v
   Application          Platform/DevOps      Operations
        |                   |                   |
        v                   v                   v
 Frontend/Services     Terraform/Jenkins   Monitoring/Logging
```

The actual organization can later be divided into separate repositories if the company requires it.

For this learning project, we intentionally keep everything in one repository so the complete enterprise workflow can be understood end-to-end.

---

# 6. Root README.md

File:

```text
README.md
```

## Purpose

The root README is the entry point for the project.

It should explain:

* What ShopSphere is
* Business purpose
* Architecture
* Technology stack
* Repository structure
* How to start development
* How infrastructure is provisioned
* How applications are deployed
* How monitoring works
* Important links and documentation

Example:

```text
README.md
    |
    +--> Business
    +--> Architecture
    +--> Application
    +--> Terraform
    +--> Kubernetes
    +--> Jenkins
    +--> Monitoring
```

---

# 7. docs/ Directory

The `docs/` directory contains project documentation.

```text
docs/
│
├── business-requirements.md
├── architecture.md
├── database-design.md
├── repository-structure.md
├── deployment-flow.md
└── troubleshooting.md
```

---

# 8. business-requirements.md

Purpose:

Defines what the business needs.

It answers:

```text
Why are we building ShopSphere?
What problem does it solve?
Who uses it?
What capabilities are required?
What are the business expectations?
```

Relationship:

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
Implementation
```

---

# 9. architecture.md

Purpose:

Defines the technical architecture.

It explains:

* Frontend
* Microservices
* Database
* AWS
* Terraform
* EKS
* Load Balancer
* Networking
* CI/CD
* Monitoring
* Logging

Relationship:

```text
business-requirements.md
          |
          v
architecture.md
          |
          +----> application
          +----> terraform
          +----> kubernetes
          +----> jenkins
```

---

# 10. database-design.md

Purpose:

Defines PostgreSQL database architecture.

It explains:

* Service ownership
* Tables
* Columns
* Primary keys
* Foreign keys
* Indexes
* Transactions
* RDS architecture
* Security
* Backup
* Recovery
* Database migrations

Relationship:

```text
database-design.md
       |
       +----------------+
       |                |
       v                v
Application        Terraform
       |                |
       v                v
DB connection       RDS
       |                |
       +-------+--------+
               |
               v
          PostgreSQL
```

---

# 11. repository-structure.md

This document.

Purpose:

Explains:

```text
Where does each file belong?
Why does the file exist?
Who uses it?
What does it connect to?
What happens if it is missing?
```

It acts as the map of the project.

---

# 12. deployment-flow.md

This document will later explain the complete deployment process.

Expected flow:

```text
Developer
   |
   v
Git Push
   |
   v
Jenkins
   |
   v
Build
   |
   v
Test
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
ShopSphere
```

Infrastructure provisioning remains separate:

```text
Terraform
   |
   v
AWS Infrastructure
   |
   +--> VPC
   +--> EKS
   +--> RDS
   +--> ECR
   +--> IAM
   +--> Networking
```

---

# 13. troubleshooting.md

This will contain production-style troubleshooting scenarios.

Examples:

```text
Application unavailable
Database connection failure
Pod CrashLoopBackOff
High CPU
High memory
EKS node failure
RDS connectivity failure
Terraform failure
Jenkins failure
Docker image failure
Ingress failure
```

It will include:

```text
Symptom
   |
   v
Investigation
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Validation
   |
   v
Prevention
```

---

# 14. application/ Directory

The `application/` directory contains application source code.

```text
application/
│
├── frontend/
│
└── services/
    ├── user-service/
    ├── product-service/
    ├── cart-service/
    ├── order-service/
    └── payment-service/
```

---

# 15. Frontend

Location:

```text
application/frontend/
```

Purpose:

Provides the customer-facing web application.

Example:

```text
Customer
   |
   v
Frontend
   |
   v
Backend APIs
```

Frontend responsibilities:

* Login
* Product browsing
* Product search
* Cart
* Checkout
* Order history
* Payment UI
* User profile

The frontend does not directly connect to PostgreSQL.

Instead:

```text
Frontend
   |
   v
API
   |
   v
Microservice
   |
   v
Database
```

---

# 16. Microservices Directory

Location:

```text
application/services/
```

Contains:

```text
user-service/
product-service/
cart-service/
order-service/
payment-service/
```

Each service represents a business capability.

---

# 17. User Service

```text
application/services/user-service/
```

Responsibilities:

```text
Register
Login
Profile
User information
```

Database:

```text
User Service
     |
     v
User PostgreSQL DB
```

---

# 18. Product Service

```text
application/services/product-service/
```

Responsibilities:

```text
Products
Categories
Pricing
Inventory
```

Database:

```text
Product Service
      |
      v
Product PostgreSQL DB
```

---

# 19. Cart Service

```text
application/services/cart-service/
```

Responsibilities:

```text
Create cart
Add item
Remove item
Update quantity
View cart
```

Database:

```text
Cart Service
     |
     v
Cart PostgreSQL DB
```

---

# 20. Order Service

```text
application/services/order-service/
```

Responsibilities:

```text
Create order
Order items
Order status
Order history
```

Database:

```text
Order Service
      |
      v
Order PostgreSQL DB
```

---

# 21. Payment Service

```text
application/services/payment-service/
```

Responsibilities:

```text
Payment request
Payment validation
Payment status
Transaction reference
```

Database:

```text
Payment Service
       |
       v
Payment PostgreSQL DB
```

Initially, payment will be simulated for learning purposes.

A real external payment gateway can be introduced later.

---

# 22. Service Internal Structure

Each microservice should eventually have its own internal structure.

Example:

```text
user-service/
│
├── src/
├── tests/
├── migrations/
├── Dockerfile
├── README.md
└── configuration/
```

The exact files depend on the programming language and framework selected.

For example:

```text
src/
   |
   +-- controller
   +-- service
   +-- repository
   +-- model
   +-- configuration
```

The exact implementation will be decided during the application-development phase.

---

# 23. Database Migration Location

Each service should own its database migrations.

Example:

```text
user-service/
    |
    +-- migrations/
         |
         +-- V1__create_users.sql
```

Product:

```text
product-service/
    |
    +-- migrations/
         |
         +-- V1__create_categories.sql
         +-- V2__create_products.sql
```

Order:

```text
order-service/
    |
    +-- migrations/
         |
         +-- V1__create_orders.sql
         +-- V2__create_order_items.sql
```

This maintains service ownership.

---

# 24. docker/ Directory

Location:

```text
docker/
```

Purpose:

Contains shared Docker-related configuration where needed.

However, each microservice will generally have its own Dockerfile close to its application code.

Example:

```text
application/services/user-service/Dockerfile
application/services/product-service/Dockerfile
application/services/cart-service/Dockerfile
application/services/order-service/Dockerfile
application/services/payment-service/Dockerfile
```

Why?

Because each service may have:

* Different dependencies
* Different build process
* Different runtime requirements
* Different versioning

---

# 25. Docker Flow

```text
Source Code
    |
    v
Dockerfile
    |
    v
docker build
    |
    v
Container Image
    |
    v
ECR
    |
    v
EKS
```

Example:

```bash
docker build -t shopsphere-user-service:1.0 .
```

---

# 26. Kubernetes Directory

Location:

```text
kubernetes/
```

Purpose:

Contains Kubernetes resources required to deploy the application.

Potential structure:

```text
kubernetes/
│
├── namespace/
├── configmaps/
├── secrets/
├── deployments/
├── services/
├── ingress/
├── hpa/
└── serviceaccounts/
```

These resources deploy the application **onto infrastructure that Terraform created**.

---

# 27. Terraform and Kubernetes Relationship

This distinction is very important.

Terraform:

```text
Creates infrastructure
```

Kubernetes:

```text
Runs applications on that infrastructure
```

Example:

```text
Terraform
    |
    +--> VPC
    +--> EKS
    +--> Node Groups
    +--> IAM
    +--> Networking
    +--> RDS
    +--> ECR
    |
    v
Infrastructure Ready
    |
    v
Kubernetes
    |
    +--> Deployment
    +--> Service
    +--> Ingress
    +--> HPA
```

---

# 28. helm/ Directory

Helm packages Kubernetes application configuration.

Target:

```text
helm/
│
└── shopsphere/
    ├── Chart.yaml
    ├── values.yaml
    └── templates/
```

Helm can manage repeated Kubernetes configuration.

Example:

```text
values-dev.yaml
values-qa.yaml
values-prod.yaml
```

Helm does not replace Terraform.

```text
Terraform
    |
    v
AWS / EKS Infrastructure

Helm
    |
    v
Application Kubernetes Resources
```

---

# 29. jenkins/ Directory

Location:

```text
jenkins/
```

Purpose:

Stores Jenkins pipeline definitions and CI/CD-related configuration.

Possible structure:

```text
jenkins/
│
├── Jenkinsfile
├── pipelines/
└── scripts/
```

The Jenkins pipeline will eventually perform tasks such as:

```text
Checkout
   |
Build
   |
Unit Test
   |
Security Scan
   |
Docker Build
   |
Push Image
   |
Deploy
   |
Validate
```

---

# 30. Jenkins and Terraform

Jenkins can execute Terraform as part of infrastructure automation.

Example:

```text
Jenkins
   |
   v
Terraform
   |
   v
terraform plan
   |
   v
Approval
   |
   v
terraform apply
```

For production, infrastructure changes should normally use controlled approval and state-management practices.

---

# 31. monitoring/ Directory

Purpose:

Contains monitoring configuration.

Potential components:

```text
monitoring/
│
├── prometheus/
├── grafana/
└── alerts/
```

Monitoring flow:

```text
Applications
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

---

# 32. logging/ Directory

Purpose:

Contains logging configuration.

Target architecture:

```text
Application Pods
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

Logs help engineers troubleshoot production incidents.

---

# 33. Terraform Directory

This is one of the most important directories in ShopSphere.

```text
terraform/
│
├── main.tf
├── provider.tf
├── versions.tf
├── variables.tf
├── outputs.tf
├── terraform.tfvars.example
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

---

# 34. Terraform Source of Truth

Our project rule:

> **AWS infrastructure must be represented in Terraform code.**

Example:

```text
terraform/
      |
      v
AWS Infrastructure
```

If someone manually changes infrastructure in the AWS Console:

```text
AWS Console
     |
     v
Manual Change
     |
     v
Terraform State / Configuration Drift
```

The change should be reviewed and reconciled through Terraform.

---

# 35. terraform/main.tf

Purpose:

The root Terraform configuration coordinates the infrastructure modules.

Conceptually:

```text
main.tf
   |
   +--> VPC module
   +--> Security Group module
   +--> IAM module
   +--> ECR module
   +--> EKS module
   +--> Database module
   +--> Load Balancer module
```

The root module does not need to contain every resource definition itself.

It calls reusable child modules.

---

# 36. terraform/provider.tf

Defines the AWS provider configuration.

Conceptually:

```text
Terraform
   |
   v
AWS Provider
   |
   v
AWS Account / Region
```

The exact region and authentication method will be configured later according to the environment.

Credentials should not be committed to Git.

---

# 37. terraform/versions.tf

Defines:

* Terraform version constraints
* Provider version constraints

Example concept:

```text
Terraform
   |
   +--> AWS Provider
   |
   +--> Version constraints
```

This helps make infrastructure builds reproducible.

---

# 38. terraform/variables.tf

Defines inputs to the root module.

Potential variables:

```text
region
environment
project_name
vpc_cidr
availability_zones
instance_types
database_engine
database_storage
eks_version
```

Example:

```text
variable
    |
    v
Root Module
    |
    v
Child Module
```

---

# 39. terraform/outputs.tf

Defines important values returned by Terraform.

Examples:

```text
VPC ID
Private Subnet IDs
EKS Cluster Name
ECR Repository URLs
RDS Endpoint
Security Group IDs
```

Example:

```text
Terraform
   |
   v
RDS
   |
   v
RDS Endpoint
   |
   v
Output
```

Applications can then consume infrastructure information through controlled configuration mechanisms.

---

# 40. terraform.tfvars.example

This file demonstrates expected variable values.

Example:

```text
environment = "dev"
region      = "ap-south-1"
```

Sensitive values should not be committed.

We should use:

```text
terraform.tfvars
```

only when appropriate and ensure sensitive local files are excluded through `.gitignore`.

---

# 41. Terraform Modules

The modules directory contains reusable infrastructure components.

```text
modules/
│
├── vpc/
├── security-groups/
├── iam/
├── ecr/
├── eks/
├── database/
└── load-balancer/
```

Each module should have its own:

```text
main.tf
variables.tf
outputs.tf
```

when appropriate.

---

# 42. VPC Module

Location:

```text
terraform/modules/vpc/
```

Responsibilities may include:

```text
VPC
Public Subnets
Private Subnets
Internet Gateway
NAT Gateway
Route Tables
Routes
```

Architecture:

```text
VPC Module
    |
    +--> VPC
    +--> Public Subnets
    +--> Private Subnets
    +--> IGW
    +--> NAT
    +--> Routes
```

---

# 43. Security Groups Module

Location:

```text
terraform/modules/security-groups/
```

Potential security groups:

```text
Load Balancer SG
Application SG
Database SG
```

Flow:

```text
Internet
   |
   | 443
   v
LB SG
   |
   | Application traffic
   v
Application
   |
   | 5432
   v
Database SG
```

The database should only accept PostgreSQL traffic from the appropriate application layer.

---

# 44. IAM Module

Location:

```text
terraform/modules/iam/
```

Responsibilities:

```text
IAM Roles
IAM Policies
EKS Roles
Node Roles
Service-related permissions
```

The module follows least-privilege principles.

---

# 45. ECR Module

Location:

```text
terraform/modules/ecr/
```

Purpose:

Creates Amazon ECR repositories for ShopSphere images.

Example:

```text
ECR
 |
 +-- shopsphere-user-service
 +-- shopsphere-product-service
 +-- shopsphere-cart-service
 +-- shopsphere-order-service
 +-- shopsphere-payment-service
```

This is infrastructure, so it is Terraform-managed.

---

# 46. EKS Module

Location:

```text
terraform/modules/eks/
```

Potential responsibilities:

```text
EKS Cluster
Node Groups
IAM integration
Cluster networking
Security integration
```

Conceptual flow:

```text
Terraform
   |
   v
EKS Module
   |
   +--> EKS Cluster
   +--> Node Groups
   +--> IAM
   +--> Networking
```

---

# 47. Database Module

Location:

```text
terraform/modules/database/
```

Potential responsibilities:

```text
RDS PostgreSQL
DB Subnet Group
Parameter Group
Encryption
Backup configuration
Multi-AZ configuration
Database Security Group integration
```

Architecture:

```text
Terraform
    |
    v
Database Module
    |
    v
Private RDS PostgreSQL
```

---

# 48. Load Balancer Module

Location:

```text
terraform/modules/load-balancer/
```

This module may manage AWS load-balancing infrastructure when it is appropriate to manage that layer explicitly through Terraform.

Kubernetes may also provision AWS load-balancer resources through controllers/operators.

The exact responsibility will be finalized when the EKS ingress architecture is implemented.

The principle remains:

> Infrastructure resources must have an explicit infrastructure-as-code ownership model.

---

# 49. Terraform Dependency Flow

The infrastructure will have dependencies.

Conceptually:

```text
VPC
 |
 +-------------------+
 |                   |
 v                   v
Subnets          Security Groups
 |                   |
 +---------+---------+
           |
           v
      EKS / RDS
           |
           v
    Application Layer
```

Terraform automatically builds a dependency graph based on resource references.

---

# 50. Single Root Terraform Workflow

Our target is:

```bash
cd terraform

terraform init
terraform plan
terraform apply
```

The root module calls the child modules.

```text
terraform apply
       |
       v
root main.tf
       |
       +---- VPC
       |
       +---- Security Groups
       |
       +---- IAM
       |
       +---- ECR
       |
       +---- EKS
       |
       +---- RDS
       |
       +---- Load Balancer infrastructure
```

This is the intended enterprise learning workflow.

---

# 51. Important Terraform Boundary

Terraform provisions infrastructure.

It does not become the tool for every application operation.

Example:

```text
Terraform
   |
   +--> VPC
   +--> EKS
   +--> RDS
   +--> ECR
   +--> IAM
   |
   v
Infrastructure Ready
```

Then:

```text
Jenkins
   |
   v
Build Application
   |
   v
Docker
   |
   v
ECR
   |
   v
Kubernetes / Helm
   |
   v
EKS
```

This separation makes responsibilities clear.

---

# 52. Complete Repository Flow

```text
Developer
    |
    v
Git Repository
    |
    +------------------------+
    |                        |
    v                        v
Application              Terraform
    |                        |
    v                        v
Docker                    AWS Infrastructure
    |                        |
    v                        +---- VPC
ECR                          +---- EKS
    |                        +---- RDS
    v                        +---- ECR
Kubernetes                   +---- IAM
    |                        +---- Networking
    v
ShopSphere
```

---

# 53. End-to-End Enterprise Flow

```text
                    DEVELOPER
                        |
                        v
                       GIT
                        |
             +----------+----------+
             |                     |
             v                     v
       APPLICATION             TERRAFORM
             |                     |
             v                     v
          DOCKER                AWS INFRA
             |                     |
             v              +------+------+
            ECR              |             |
             |               v             v
             |              EKS           RDS
             |               |
             +-------+-------+
                     |
                     v
                KUBERNETES
                     |
                     v
               SHOPSPHERE
                     |
          +----------+----------+
          |                     |
          v                     v
     PROMETHEUS               LOKI
          |                     |
          +----------+----------+
                     |
                     v
                  GRAFANA
```

---

# 54. Infrastructure vs Application Ownership

This distinction is important for the team.

| Area                  | Primary Tool      |
| --------------------- | ----------------- |
| AWS VPC               | Terraform         |
| AWS Subnets           | Terraform         |
| AWS Security Groups   | Terraform         |
| AWS IAM               | Terraform         |
| AWS EKS               | Terraform         |
| AWS Node Groups       | Terraform         |
| AWS RDS               | Terraform         |
| AWS ECR               | Terraform         |
| Application source    | Git               |
| Container build       | Docker            |
| CI/CD                 | Jenkins           |
| Kubernetes Deployment | Kubernetes / Helm |
| Application scaling   | Kubernetes HPA    |
| Database schema       | Migration tooling |
| Metrics               | Prometheus        |
| Dashboards            | Grafana           |
| Logs                  | Loki              |

Some Kubernetes-created AWS resources may be reconciled by Kubernetes controllers. Where that happens, the project must explicitly document the ownership boundary rather than allowing duplicate Terraform and Kubernetes ownership of the same resource.

---

# 55. What Happens If Terraform Is Missing?

Imagine manually creating infrastructure:

```text
Engineer
   |
   +--> AWS Console
   +--> Create VPC
   +--> Create EKS
   +--> Create RDS
   +--> Create Security Groups
```

Problems:

* No reliable infrastructure history
* Difficult reproduction
* Configuration drift
* Manual mistakes
* Harder disaster recovery
* Difficult environment duplication
* Poor auditability

With Terraform:

```text
Git
 |
 v
Terraform Code
 |
 v
terraform plan
 |
 v
Review
 |
 v
terraform apply
 |
 v
AWS
```

Infrastructure becomes repeatable and reviewable.

---

# 56. What Happens If a Terraform Module Is Missing?

Example:

```text
No VPC module
     |
     v
No defined network infrastructure
     |
     v
EKS/RDS dependencies cannot be created correctly
```

Or:

```text
No database module
     |
     v
No Terraform-managed RDS
     |
     v
Application has no managed persistent database
```

Each module exists because it owns a specific infrastructure responsibility.

---

# 57. Git Branching Strategy

For the learning project, we can use:

```text
main
 |
 +-- develop
      |
      +-- feature/SHOP-001-user-service
      +-- feature/SHOP-002-product-service
      +-- feature/SHOP-003-cart-service
      +-- feature/SHOP-004-order-service
      +-- feature/SHOP-005-terraform-vpc
      +-- feature/SHOP-006-terraform-rds
      +-- feature/SHOP-007-terraform-eks
```

Example:

```text
Developer
   |
   v
feature/SHOP-005-terraform-vpc
   |
   v
Pull Request
   |
   v
Review
   |
   v
develop
```

Production changes should eventually go through an approved release process.

---

# 58. Infrastructure Change Example

Suppose we need to change the VPC CIDR.

Developer changes:

```text
terraform/modules/vpc/
```

Then:

```bash
terraform fmt
terraform validate
terraform plan
```

Review the plan.

Only after approval:

```bash
terraform apply
```

This gives us an auditable infrastructure change.

---

# 59. Terraform Validation

Before committing Terraform changes:

```bash
terraform fmt -check
```

Validate configuration:

```bash
terraform validate
```

Review infrastructure changes:

```bash
terraform plan
```

Then, according to the environment's approval process:

```bash
terraform apply
```

---

# 60. Terraform State

Terraform state is critical.

Conceptually:

```text
Terraform Configuration
        |
        v
Terraform State
        |
        v
Real AWS Infrastructure
```

The state tells Terraform what infrastructure it manages.

For an enterprise implementation, remote state should eventually be used with appropriate locking and access controls.

A future design may use:

```text
S3
 |
 v
Terraform State
```

with a suitable locking mechanism supported by the selected Terraform/backend design.

State files must not be casually committed to Git.

---

# 61. .gitignore

A root `.gitignore` should eventually exclude files such as:

```text
.terraform/
*.tfstate
*.tfstate.*
*.tfvars
*.tfvars.json
.env
.env.*
```

Sensitive or generated files should not be committed.

A carefully reviewed exception can be made for safe example files such as:

```text
terraform.tfvars.example
```

---

# 62. Secrets

Secrets should never be stored directly in:

```text
Git
Terraform source
Dockerfile
Kubernetes manifest
Jenkinsfile
```

Preferred architecture:

```text
Secret Management
       |
       v
Application
       |
       v
Database
```

Terraform can provision the infrastructure needed for secret management while avoiding exposing secret values unnecessarily in source control or Terraform state.

---

# 63. Environment Structure

We will eventually support:

```text
DEV
QA
PROD
```

Conceptually:

```text
ShopSphere
    |
    +---- DEV
    |
    +---- QA
    |
    +---- PROD
```

Infrastructure differences should be controlled through Terraform variables/modules rather than manual AWS changes.

For example:

```text
DEV
small infrastructure

QA
medium infrastructure

PROD
high-availability infrastructure
```

---

# 64. Real-Time Enterprise Example

Imagine the company asks:

> "Create a new ShopSphere environment for QA."

Without Terraform:

```text
Engineer
   |
   +--> Manually create VPC
   +--> Manually create subnets
   +--> Manually create security groups
   +--> Manually create EKS
   +--> Manually create RDS
   +--> Manually create ECR
```

This can take significant time and introduces configuration differences.

With Terraform:

```text
Git
 |
 v
Terraform
 |
 v
QA Variables
 |
 v
terraform plan
 |
 v
terraform apply
 |
 v
QA Infrastructure
```

The same infrastructure design can be reproduced consistently.

---

# 65. File-to-File Relationship

The important relationship is:

```text
business-requirements.md
          |
          v
architecture.md
          |
          v
database-design.md
          |
          v
repository-structure.md
          |
          +--------------------+
          |                    |
          v                    v
application/              terraform/
          |                    |
          v                    v
      Docker              AWS Infrastructure
          |                    |
          v                    v
         ECR                  EKS/RDS
          |                    |
          +----------+---------+
                     |
                     v
                Kubernetes
                     |
                     v
                ShopSphere
```

---

# 66. Day-by-Day Project Creation Strategy

We will **not create everything on Day 1**.

Recommended progression:

```text
Phase 1
Business Requirements
        |
        v
Architecture
        |
        v
Database Design
        |
        v
Repository Structure
```

Then:

```text
Phase 2
Application
   |
   +--> User Service
   +--> Product Service
   +--> Cart Service
   +--> Order Service
   +--> Payment Service
```

Then:

```text
Phase 3
Docker
   |
   v
Container Images
```

Then:

```text
Phase 4
Terraform
   |
   +--> VPC
   +--> Security Groups
   +--> IAM
   +--> ECR
   +--> RDS
   +--> EKS
```

Then:

```text
Phase 5
Kubernetes / Helm
```

Then:

```text
Phase 6
Jenkins CI/CD
```

Then:

```text
Phase 7
Monitoring + Logging
```

Finally:

```text
Phase 8
Incidents + RCA + Rollback
```

---

# 67. Current Project Status

Completed:

```text
Business Requirements       DONE
Architecture Design         DONE
Database Design             DONE
Repository Structure        DONE
```

Next phase:

```text
Application Design / Microservice Development
```

Before writing application code, we will define the exact technology stack and service contracts.

---

# 68. Interview Explanation

### Interviewer:

**Explain your ShopSphere repository structure.**

### Answer:

> "I designed ShopSphere as a monorepo for our enterprise learning project so that the complete application and DevOps lifecycle can be understood from one repository.
>
> The application directory contains the frontend and individual microservices such as User, Product, Cart, Order, and Payment.
>
> The Terraform directory contains the complete AWS infrastructure code. Terraform is our infrastructure source of truth. The root Terraform configuration calls reusable modules for networking, security groups, IAM, ECR, EKS, RDS, and other infrastructure components.
>
> The Kubernetes and Helm directories contain the application deployment configuration, while Jenkins handles CI/CD.
>
> Documentation is maintained under the docs directory so business requirements, architecture, database design, deployment flow, and troubleshooting are version-controlled along with the project.
>
> The important separation is that Terraform provisions infrastructure, while Kubernetes manages application workloads running on that infrastructure. Jenkins automates the delivery process between source code, container images, and Kubernetes.
>
> Our target infrastructure workflow is a single root Terraform workflow using init, plan, and apply. The root module passes variables and dependencies to child modules, allowing us to provision the complete AWS environment consistently rather than manually creating infrastructure through the console."

---

# 69. Key Enterprise Principles

ShopSphere repository follows these principles:

```text
1. Clear ownership
2. Service-oriented application structure
3. Infrastructure as Code
4. Terraform as infrastructure source of truth
5. Reusable Terraform modules
6. Version-controlled infrastructure
7. No manual production infrastructure workflow
8. Secure secret handling
9. Independent service database ownership
10. Containerized applications
11. Kubernetes-based deployment
12. Jenkins-based CI/CD
13. Monitoring and logging
14. Environment separation
15. Documentation as code
16. Reviewable changes
17. Reproducible infrastructure
```

---

# 70. Final Repository Architecture

```text
                         SHOPSPHERE
                             |
                             v
                         Git Repo
                             |
       +---------------------+----------------------+
       |                     |                      |
       v                     v                      v
   application/          terraform/              docs/
       |                     |                      |
       |                     v                      |
       |               AWS Infrastructure           |
       |                     |                      |
       |          +----------+----------+           |
       |          |          |          |           |
       |          v          v          v           |
       |         VPC        EKS        RDS          |
       |                     |                      |
       v                     v                      |
     Docker             Kubernetes                 |
       |                     |                      |
       v                     v                      |
      ECR              ShopSphere App              |
       |                                            |
       +--------------------+-----------------------+
                            |
                            v
                       Jenkins CI/CD
                            |
                  +---------+---------+
                  |                   |
                  v                   v
             Prometheus            Loki
                  |                   |
                  +---------+---------+
                            |
                            v
                         Grafana
```

---

# 71. Final Takeaway

The repository is not just a collection of folders.

Each directory has a responsibility:

```text
application/  -> Business application
docker/       -> Containerization
terraform/    -> AWS infrastructure
kubernetes/   -> Application deployment
helm/         -> Kubernetes packaging
jenkins/      -> CI/CD
monitoring/   -> Metrics and alerts
logging/      -> Centralized logs
docs/         -> Project knowledge
```

Most importantly:

```text
                    TERRAFORM
                        |
                        v
              INFRASTRUCTURE SOURCE
                     OF TRUTH
                        |
        +---------------+---------------+
        |               |               |
        v               v               v
       VPC             EKS             RDS
        |               |               |
        +---------------+---------------+
                        |
                        v
                  Infrastructure
                     Ready
                        |
                        v
               Kubernetes / Helm
                        |
                        v
                  Application
                        |
                        v
                  Jenkins CI/CD
```

For ShopSphere, **we will not manually build the AWS infrastructure and then write Terraform afterward**.

We will design the infrastructure requirements first and implement the infrastructure through Terraform modules.

That gives us the real enterprise workflow:

```text
Requirement
    ↓
Architecture
    ↓
Terraform Design
    ↓
terraform plan
    ↓
Review
    ↓
terraform apply
    ↓
AWS Infrastructure
    ↓
Application Deployment
```

**Next phase: Application / Microservice Design**

We should next define each microservice's **API, business responsibility, database ownership, request/response flow, folder structure, and how the services communicate** before writing the actual application code.
