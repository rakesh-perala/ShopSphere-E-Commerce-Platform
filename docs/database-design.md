# ShopSphere — Database Design

## 1. Document Information

| Item                | Details                                     |
| ------------------- | ------------------------------------------- |
| Project             | ShopSphere — Enterprise E-Commerce Platform |
| Document            | Database Design                             |
| Database            | PostgreSQL                                  |
| Target AWS Database | Amazon RDS for PostgreSQL                   |
| Architecture        | Microservices                               |
| Database Ownership  | Service-owned data                          |
| Environment         | Development / QA / Production               |
| Status              | Design                                      |

---

# 2. Purpose

This document explains how ShopSphere stores and manages business data using PostgreSQL.

The main goal is to design the database so that:

* Each microservice owns its business data.
* Data is persistent.
* Services do not directly modify another service's database.
* PostgreSQL is used as the relational database.
* Production databases run privately inside AWS.
* Database credentials are not hard-coded.
* The design supports transactions, indexing, backups, scaling, and recovery.
* The database can later be provisioned using Terraform.

---

# 3. Business Requirement

ShopSphere is an e-commerce application.

Customers should be able to:

```text
Register
   |
   v
Login
   |
   v
Browse Products
   |
   v
Add Products to Cart
   |
   v
Checkout
   |
   v
Create Order
   |
   v
Process Payment
   |
   v
Confirm Order
```

All important information must survive application restarts.

For example:

If a customer creates an order today, that order should still exist tomorrow.

Therefore, ShopSphere requires a real persistent relational database.

---

# 4. Why PostgreSQL?

PostgreSQL is selected as the primary database for ShopSphere.

It provides:

* ACID transactions
* Primary keys
* Foreign keys
* Indexes
* Constraints
* Transactions
* Backup and recovery
* Strong relational data modeling
* Good support for cloud deployments
* Good compatibility with enterprise applications

Example:

An order contains multiple order items.

```text
orders
   |
   +---- order_items
   |
   +---- payment
```

A relational database is well suited for this type of business relationship.

---

# 5. Important Microservices Database Principle

ShopSphere follows:

> Database per service / service-owned data.

The important point is **data ownership**, not necessarily that every service must immediately have a completely separate physical RDS instance.

For our learning project, services will be designed so that each service owns its data and other services access that information through APIs.

Target logical architecture:

```text
                    ShopSphere
                        |
        +---------------+---------------+
        |               |               |
        v               v               v
   User Service   Product Service   Order Service
        |               |               |
        v               v               v
     User DB        Product DB        Order DB


        +---------------+---------------+
        |                               |
        v                               v
   Cart Service                  Payment Service
        |                               |
        v                               v
     Cart DB                       Payment DB
```

The databases may initially be implemented as separate PostgreSQL databases within a controlled lab environment.

In production, they can be separated further based on scalability, security, availability, and operational requirements.

---

# 6. Data Ownership

Each service owns specific business data.

| Service              | Owns                            |
| -------------------- | ------------------------------- |
| User Service         | Users                           |
| Product Service      | Products, Categories, Inventory |
| Cart Service         | Carts, Cart Items               |
| Order Service        | Orders, Order Items             |
| Payment Service      | Payments, Payment Status        |
| Notification Service | Notification records            |

Example:

```text
Product Service
       |
       +---- Product DB
       |
       +---- Product information
       +---- Category information
       +---- Stock information
```

Order Service should not directly update the Product DB.

Instead:

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

This preserves microservice ownership.

---

# 7. High-Level Database Architecture

```text
                         ShopSphere
                             |
             +---------------+---------------+
             |               |               |
             v               v               v
        User Service   Product Service   Cart Service
             |               |               |
             v               v               v
          User DB        Product DB        Cart DB
             |               |               |
             +---------------+---------------+
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
```

All databases are persistent.

---

# 8. Database Relationship at Business Level

Although services own their databases independently, they still exchange business identifiers.

For example:

```text
User Service
    |
    | user_id = 101
    v
Order Service
    |
    | order.user_id = 101
    v
Order DB
```

Here `101` represents the User Service's user ID.

The Order Service does not create a database-level foreign key into the User DB.

Instead, it treats the ID as a logical business reference.

This is important in microservices.

---

# 9. Service 1 — User Database

## Business Purpose

The User Service manages customer identity and profile information.

Example operations:

```text
Register User
Login
Get Profile
Update Profile
```

---

## User Table

```text
users
```

| Column        | Type               | Purpose         |
| ------------- | ------------------ | --------------- |
| id            | BIGSERIAL / BIGINT | Primary key     |
| name          | VARCHAR            | Customer name   |
| email         | VARCHAR            | Customer email  |
| password_hash | VARCHAR            | Hashed password |
| created_at    | TIMESTAMP          | Creation time   |
| updated_at    | TIMESTAMP          | Last update     |

Example:

```text
users
------------------------------------------------
id | name       | email              | created_at
------------------------------------------------
101| Rakesh     | rakesh@example.com | ...
102| Rahul      | rahul@example.com  | ...
```

---

# 10. User Database Constraints

Email should be unique.

Example:

```text
UNIQUE(email)
```

This prevents:

```text
customer1@example.com
customer1@example.com
```

from being registered twice.

The database should enforce important data integrity rules rather than depending only on application code.

---

# 11. User Database Index

Create an index for frequently searched columns.

Example:

```text
users.email
```

Why?

Login commonly searches:

```sql
SELECT *
FROM users
WHERE email = 'customer@example.com';
```

An index can make this lookup much faster as the table grows.

---

# 12. Service 2 — Product Database

The Product Service owns product information.

Business operations:

```text
Create Product
Update Product
Get Product
Search Products
Manage Categories
Manage Stock
```

---

# 13. Category Table

```text
categories
```

| Column      | Type      | Purpose              |
| ----------- | --------- | -------------------- |
| id          | BIGINT    | Primary key          |
| name        | VARCHAR   | Category name        |
| description | TEXT      | Category description |
| created_at  | TIMESTAMP | Creation time        |

Example:

```text
categories
--------------------------------
id | name
--------------------------------
1  | Electronics
2  | Mobiles
3  | Laptops
```

---

# 14. Product Table

```text
products
```

| Column         | Type      | Purpose               |
| -------------- | --------- | --------------------- |
| id             | BIGINT    | Primary key           |
| name           | VARCHAR   | Product name          |
| description    | TEXT      | Product description   |
| price          | NUMERIC   | Current product price |
| stock_quantity | INTEGER   | Available stock       |
| category_id    | BIGINT    | Category reference    |
| created_at     | TIMESTAMP | Creation time         |
| updated_at     | TIMESTAMP | Last update           |

Example:

```text
products
-------------------------------------------------------
id | name          | price | stock_quantity | category
-------------------------------------------------------
501| Laptop        | 65000 | 20             | 3
502| Mobile Phone  | 25000 | 50             | 2
```

---

# 15. Product Relationships

Inside Product Service:

```text
Category
   |
   | 1
   |
   | many
   v
Products
```

One category can contain many products.

Example:

```text
Electronics
   |
   +--- Laptop
   +--- Monitor
   +--- Keyboard
```

The Product DB can enforce this relationship with a foreign key.

---

# 16. Product Indexes

Potential indexes:

```text
products.category_id
products.name
products.created_at
```

Example:

```sql
SELECT *
FROM products
WHERE category_id = 3;
```

An index on `category_id` can improve this query as product volume grows.

---

# 17. Inventory Ownership

Product Service owns stock.

For example:

```text
Product Service
       |
       v
Product DB
       |
       v
stock_quantity = 20
```

Order Service should not directly execute:

```sql
UPDATE products
SET stock_quantity = stock_quantity - 1;
```

Instead:

```text
Order Service
      |
      | Request stock operation
      v
Product Service
      |
      v
Product DB
```

This prevents multiple services from directly modifying the same business data.

---

# 18. Service 3 — Cart Database

Cart Service owns shopping carts.

Main tables:

```text
carts
cart_items
```

---

# 19. Carts Table

```text
carts
```

| Column     | Type      | Purpose         |
| ---------- | --------- | --------------- |
| id         | BIGINT    | Primary key     |
| user_id    | BIGINT    | User Service ID |
| created_at | TIMESTAMP | Creation time   |
| updated_at | TIMESTAMP | Last update     |

Important:

`user_id` is a logical reference to User Service.

There is no cross-database foreign key.

---

# 20. Cart Items Table

```text
cart_items
```

| Column     | Type      | Purpose            |
| ---------- | --------- | ------------------ |
| id         | BIGINT    | Primary key        |
| cart_id    | BIGINT    | Cart reference     |
| product_id | BIGINT    | Product Service ID |
| quantity   | INTEGER   | Quantity           |
| created_at | TIMESTAMP | Creation time      |
| updated_at | TIMESTAMP | Last update        |

Relationship:

```text
Cart
 |
 +---- Cart Item
 |
 +---- Cart Item
 |
 +---- Cart Item
```

One cart can contain multiple products.

---

# 21. Cart Example

Customer adds:

```text
Laptop     x 1
Mouse      x 2
Keyboard   x 1
```

Database:

```text
carts
----------------
id | user_id
----------------
9001 | 101


cart_items
----------------------------------
id | cart_id | product_id | qty
----------------------------------
1  | 9001    | 501        | 1
2  | 9001    | 601        | 2
3  | 9001    | 701        | 1
```

---

# 22. Service 4 — Order Database

Order Service owns order information.

Main tables:

```text
orders
order_items
```

---

# 23. Orders Table

```text
orders
```

| Column       | Type      | Purpose         |
| ------------ | --------- | --------------- |
| id           | BIGINT    | Primary key     |
| user_id      | BIGINT    | User Service ID |
| total_amount | NUMERIC   | Order total     |
| status       | VARCHAR   | Order status    |
| created_at   | TIMESTAMP | Creation time   |
| updated_at   | TIMESTAMP | Last update     |

Possible statuses:

```text
PENDING_PAYMENT
PAYMENT_FAILED
CONFIRMED
CANCELLED
SHIPPED
DELIVERED
```

---

# 24. Order Items Table

```text
order_items
```

| Column     | Type      | Purpose                |
| ---------- | --------- | ---------------------- |
| id         | BIGINT    | Primary key            |
| order_id   | BIGINT    | Order reference        |
| product_id | BIGINT    | Product Service ID     |
| quantity   | INTEGER   | Purchased quantity     |
| price      | NUMERIC   | Price at purchase time |
| created_at | TIMESTAMP | Creation time          |

---

# 25. Why Store Price in order_items?

This is an important enterprise design decision.

Suppose:

```text
Today:

Laptop = ₹60,000
```

Customer purchases it.

Tomorrow:

```text
Laptop = ₹65,000
```

The customer's historical order should still show:

```text
Purchased Price = ₹60,000
```

Therefore:

```text
products.price
```

stores the current product price.

While:

```text
order_items.price
```

stores the price snapshot at the time of purchase.

This protects historical order accuracy.

---

# 26. Order Relationship

Inside Order Service:

```text
Order
 |
 +---- Order Item
 |
 +---- Order Item
 |
 +---- Order Item
```

Example:

```text
Order #10001
   |
   +--- Laptop    ₹60,000 x 1
   +--- Mouse      ₹1,000 x 2
   +--- Keyboard   ₹2,000 x 1
```

---

# 27. Order Indexes

Common indexes:

```text
orders.user_id
orders.status
orders.created_at
```

Example:

Customer opens order history:

```sql
SELECT *
FROM orders
WHERE user_id = 101
ORDER BY created_at DESC;
```

An index on `user_id` and potentially a composite index involving `created_at` can improve this access pattern.

---

# 28. Service 5 — Payment Database

Payment Service owns payment information.

Table:

```text
payments
```

---

# 29. Payments Table

| Column                | Type      | Purpose           |
| --------------------- | --------- | ----------------- |
| id                    | BIGINT    | Primary key       |
| order_id              | BIGINT    | Order Service ID  |
| amount                | NUMERIC   | Payment amount    |
| status                | VARCHAR   | Payment status    |
| transaction_reference | VARCHAR   | Payment reference |
| created_at            | TIMESTAMP | Creation time     |
| updated_at            | TIMESTAMP | Last update       |

Possible statuses:

```text
INITIATED
SUCCESS
FAILED
REFUNDED
```

---

# 30. Payment Relationship

```text
Order Service
     |
     | order_id
     v
Payment Service
     |
     v
Payment DB
```

Again, `order_id` is a logical reference.

Payment DB should not directly create a database foreign key into Order DB if the services use separate databases.

---

# 31. Payment Idempotency

Payment operations must consider duplicate requests.

Example:

```text
Customer clicks Pay
       |
       v
Payment request
       |
       v
Network timeout
       |
       v
Customer retries
```

Without idempotency, the system could potentially process the same payment twice.

Therefore, the Payment Service should use a unique transaction/reference mechanism.

Example concept:

```text
transaction_reference
        |
        v
UNIQUE
```

This helps prevent duplicate processing.

---

# 32. Complete Logical Data Model

```text
USER SERVICE
------------

users
  |
  | user_id
  |
  +--------------------+
                       |
                       v
ORDER SERVICE       CART SERVICE
-------------       ------------
orders              carts
  |                   |
  v                   v
order_items        cart_items
  |
  | product_id
  v
PRODUCT SERVICE
---------------
products
   |
   v
categories


ORDER SERVICE
      |
      | order_id
      v
PAYMENT SERVICE
---------------
payments
```

The services communicate through APIs.

They do not directly query each other's databases.

---

# 33. Primary Key

A primary key uniquely identifies a record.

Example:

```text
users.id
products.id
orders.id
payments.id
```

Example:

```text
User ID = 101
```

No two users should have the same primary key.

---

# 34. Foreign Key

A foreign key creates a database-enforced relationship between tables within the same database.

Example inside Product DB:

```text
categories.id
      |
      v
products.category_id
```

This can be enforced using:

```text
FOREIGN KEY (category_id)
REFERENCES categories(id)
```

---

# 35. Cross-Service Reference

For microservices:

```text
orders.user_id
```

does not need to be a database-level foreign key to:

```text
users.id
```

because User DB and Order DB are independently owned.

Instead:

```text
Order Service
     |
     | user_id
     v
User Service API
     |
     v
User DB
```

This is an important microservices interview concept.

---

# 36. Database Normalization

ShopSphere should avoid unnecessary duplication of business data.

For example, instead of storing:

```text
order_id
product_name
category_name
category_description
```

repeatedly in every order record, the design separates business entities appropriately.

However, controlled duplication is sometimes intentional.

Example:

```text
order_items.price
```

is duplicated from the product's current price intentionally.

Why?

Because it represents the historical transaction price.

So normalization is not about eliminating every duplicate value.

It is about designing data correctly for the business requirement.

---

# 37. Transactions

A transaction groups multiple database operations into one logical operation.

Example:

```text
Create Order
     |
     +--- Insert Order
     |
     +--- Insert Order Items
     |
     +--- Commit
```

If something fails:

```text
Insert Order
     |
Insert Order Items
     |
     X
Failure
     |
     v
ROLLBACK
```

The database should avoid leaving an incomplete local transaction.

---

# 38. Microservices and Distributed Transactions

One important limitation:

We should not assume that one PostgreSQL transaction can safely cover:

```text
Order DB
   +
Payment DB
   +
Product DB
```

because they are independently owned data stores.

Instead:

```text
Order Service
      |
      v
Payment Service
      |
      v
Payment DB
```

Each service manages its own local transaction.

For more advanced versions of ShopSphere, we can introduce:

* Saga pattern
* Outbox pattern
* Events
* Message broker
* Retry handling
* Idempotency

For the initial implementation, synchronous REST orchestration with explicit order states is sufficient for learning.

---

# 39. Order and Payment Flow

```text
Customer
   |
   v
Order Service
   |
   | Create Order
   v
Order DB
   |
   | PENDING_PAYMENT
   v
Payment Service
   |
   v
Payment DB
   |
   +----------+
   |          |
Success      Failure
   |          |
   v          v
CONFIRMED   PAYMENT_FAILED
```

If Payment Service is unavailable:

```text
Order Service
      |
      v
Payment Service
      X
     DOWN
      |
      v
Timeout
      |
      v
Order = PENDING_PAYMENT
```

The order should not incorrectly become `CONFIRMED`.

---

# 40. Database Security

The production database should be private.

Target architecture:

```text
Internet
    |
    v
Load Balancer
    |
    v
EKS Applications
    |
    v
Private RDS PostgreSQL
```

The database should not be directly accessible from the public internet.

---

# 41. AWS RDS PostgreSQL Architecture

Target:

```text
                     AWS VPC
                        |
        +---------------+---------------+
        |                               |
        v                               v
 Public Subnets                  Private Subnets
        |                               |
        v                               v
 Load Balancer                    EKS Applications
                                        |
                                        |
                                        v
                                  RDS PostgreSQL
```

RDS should be placed in private subnets.

---

# 42. RDS Security Group

Conceptual security flow:

```text
Internet
    |
    | HTTPS :443
    v
Load Balancer
    |
    | Application traffic
    v
EKS Application
    |
    | PostgreSQL :5432
    v
RDS PostgreSQL
```

Database security group:

```text
Inbound
Port: 5432
Source: Application Security Group
```

Not:

```text
0.0.0.0/0
```

for production database access.

---

# 43. Database Credentials

Credentials must not be hard-coded in source code.

Bad:

```text
DB_PASSWORD=password123
```

inside application code or Git.

Preferred architecture:

```text
Secrets Manager / Kubernetes Secret
              |
              v
        Application
              |
              v
        RDS PostgreSQL
```

Later, Terraform can provision the infrastructure and the application deployment can consume secrets through the appropriate secret-management mechanism.

---

# 44. Database Configuration

Application configuration will eventually contain values such as:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USER
DB_PASSWORD
```

Example:

```text
DB_HOST=shopsphere-db.xxxxx.region.rds.amazonaws.com
DB_PORT=5432
DB_NAME=orders
```

The password should come from a secret mechanism rather than being committed to Git.

---

# 45. Database Migrations

Application database structure should be managed through versioned migrations.

Example:

```text
V1__create_users.sql
V2__create_products.sql
V3__create_orders.sql
V4__add_payment_reference.sql
```

The exact migration framework will be selected when the application technology is finalized.

Possible approaches include:

* Flyway
* Liquibase
* Framework-native migration tools

The important enterprise principle is:

> Database changes must be version-controlled and repeatable.

---

# 46. Repository Relationship

The database design will later connect with application code and Terraform.

Target structure:

```text
shopsphere/
|
+-- docs/
|    |
|    +-- database-design.md
|
+-- application/
|    |
|    +-- services/
|         |
|         +-- user-service/
|         +-- product-service/
|         +-- cart-service/
|         +-- order-service/
|         +-- payment-service/
|
+-- terraform/
     |
     +-- main.tf
     +-- modules/
          |
          +-- database/
```

Relationship:

```text
database-design.md
        |
        +-------------------+
        |                   |
        v                   v
Application           Terraform
        |                   |
        v                   v
DB Configuration      RDS Infrastructure
        |                   |
        +---------+---------+
                  |
                  v
           PostgreSQL
```

---

# 47. Terraform Database Responsibility

Terraform will eventually create the database infrastructure.

For example:

```text
terraform/
|
+-- main.tf
|
+-- variables.tf
|
+-- outputs.tf
|
+-- modules/
     |
     +-- database/
          |
          +-- main.tf
          +-- variables.tf
          +-- outputs.tf
```

The database module may manage:

* RDS PostgreSQL
* DB subnet group
* Parameter group
* Security group integration
* Encryption configuration
* Backup configuration
* Multi-AZ configuration
* Storage
* Monitoring-related settings

Terraform manages the infrastructure.

The application manages its database schema through migrations.

This separation is important.

---

# 48. Terraform vs Database Migration

A common interview question is:

> Does Terraform create application tables?

Normally, Terraform should manage infrastructure rather than application schema.

Example:

```text
Terraform
   |
   v
Create RDS PostgreSQL
   |
   v
Database available
   |
   v
Application Migration
   |
   v
Create tables
```

Therefore:

```text
Terraform = Infrastructure
Migration Tool = Database Schema
Application = Business Logic
```

---

# 49. Backup and Recovery

Production PostgreSQL requires backup and recovery planning.

Important considerations:

* Automated backups
* Backup retention
* Point-in-time recovery
* Snapshot strategy
* Multi-AZ where required
* Recovery testing

Example incident:

```text
Application
    |
    v
Database
    X
Data corruption
    |
    v
Recovery process
    |
    v
Backup / Snapshot
    |
    v
Restored database
```

A backup is useful only if recovery has been tested.

---

# 50. High Availability

For production workloads, RDS can be configured for Multi-AZ.

Conceptually:

```text
             RDS
              |
       +------+------+
       |             |
       v             v
   Primary        Standby
    AZ-1           AZ-2
```

Application traffic normally uses the RDS endpoint rather than hard-coding a specific database server address.

If AWS performs a managed failover, the application can continue using the database endpoint.

---

# 51. Database Monitoring

Database monitoring should include:

```text
CPU
Connections
Storage
Latency
Read/Write activity
Database errors
Slow queries
Connection saturation
```

Later, ShopSphere monitoring can connect database metrics with:

```text
Prometheus
     |
     v
Grafana
```

and AWS database metrics can be monitored through:

```text
CloudWatch
```

---

# 52. Important Database Alerts

Potential production alerts:

```text
High Database CPU
High Database Connections
Low Free Storage
High Query Latency
Connection Failures
Database Unavailable
```

Example:

```text
DB Connections
      |
      v
90% capacity
      |
      v
Alert
      |
      v
DevOps / SRE
      |
      v
Investigate application connection pool
```

---

# 53. Real-Time Enterprise Example

Suppose a customer buys a laptop.

Product:

```text
Laptop
Price = ₹60,000
Stock = 20
```

Customer adds one laptop to the cart.

```text
Cart Service
     |
     v
cart_items
quantity = 1
```

Customer clicks checkout.

```text
Order Service
     |
     v
Create Order
status = PENDING_PAYMENT
```

Order item:

```text
product_id = 501
quantity = 1
price = 60000
```

Payment:

```text
Payment Service
     |
     v
amount = 60000
status = SUCCESS
```

Order becomes:

```text
CONFIRMED
```

Product Service then performs the stock operation.

```text
Stock:
20
 |
 | purchase 1
 v
19
```

The customer sees:

```text
Order Confirmed
Order ID: 10001
Amount: ₹60,000
```

---

# 54. What Happens If Payment Fails?

```text
Customer
   |
   v
Order Service
   |
   v
Create Order
   |
   v
PENDING_PAYMENT
   |
   v
Payment Service
   |
   X
Payment Failed
   |
   v
PAYMENT_FAILED
```

The order should not be marked as successfully completed.

This gives the application an explicit state that can be handled by retry or cancellation logic.

---

# 55. What Happens If Database Is Down?

Example:

```text
Product Service
      |
      v
Product DB
      X
Connection refused
```

Application logs:

```text
Database connection failed
```

Possible causes:

```text
1. RDS unavailable
2. Wrong endpoint
3. Wrong credentials
4. Security group issue
5. Private subnet/network issue
6. Connection pool exhaustion
7. Database restart
```

---

# 56. Troubleshooting — Connection Refused

Check:

```text
Application
    |
    v
DB hostname
    |
    v
Network route
    |
    v
Security Group
    |
    v
Port 5432
    |
    v
RDS
```

Validation:

```bash
nc -vz <db-host> 5432
```

If connectivity fails, investigate networking and security groups before changing application code.

---

# 57. Troubleshooting — Authentication Failed

Possible causes:

```text
Wrong username
Wrong password
Wrong database
Expired/rotated secret
Incorrect environment variable
```

Check application configuration and secret source.

Do not immediately change the database manually.

First identify the source of truth.

---

# 58. Troubleshooting — Too Many Connections

Symptoms:

```text
Connection refused
remaining connection slots are reserved
```

Possible causes:

```text
Too many application pods
Connection pool too large
Connections not released
Traffic spike
Database connection limit reached
```

Investigation:

```text
EKS Pods
   |
   +--- Pod 1 -> DB connections
   +--- Pod 2 -> DB connections
   +--- Pod 3 -> DB connections
   +--- Pod 4 -> DB connections
```

If every pod creates too many connections, the database can become saturated.

---

# 59. Troubleshooting — Slow Query

Symptoms:

```text
API response time increases
        |
        v
Database latency increases
```

Investigation:

```text
Application Metrics
        |
        v
Slow API
        |
        v
Database Query
        |
        v
EXPLAIN / EXPLAIN ANALYZE
```

Possible solutions:

* Add appropriate indexes
* Optimize query
* Reduce unnecessary data retrieval
* Review joins
* Review connection pool
* Scale database if required

Do not blindly add indexes because indexes also consume storage and can increase write overhead.

---

# 60. Troubleshooting — Wrong RDS Endpoint

Example:

```text
Development application
        |
        v
Production DB endpoint
```

This is a serious configuration problem.

Environment-specific configuration should be separated.

Example:

```text
DEV
DB_HOST=dev-db

QA
DB_HOST=qa-db

PROD
DB_HOST=prod-db
```

Secrets and endpoints should be managed safely.

---

# 61. Database Validation

Once PostgreSQL is available:

Check connectivity:

```bash
psql -h <host> -U <user> -d <database>
```

Check PostgreSQL version:

```sql
SELECT version();
```

Check tables:

```sql
\dt
```

Check users:

```sql
SELECT * FROM users;
```

Check products:

```sql
SELECT * FROM products;
```

Check orders:

```sql
SELECT * FROM orders;
```

---

# 62. Example Validation Flow

```text
Terraform
   |
   v
RDS Created
   |
   v
Database Connectivity
   |
   v
Migration
   |
   v
Tables Created
   |
   v
Insert Test Data
   |
   v
Application
   |
   v
API Test
```

Only after each stage succeeds should we move to the next stage.

---

# 63. What Happens If database-design.md Is Missing?

Without this document:

```text
Developers
    |
    +--- Create different table designs
    |
    +--- Duplicate data
    |
    +--- Unclear ownership
    |
    +--- Incorrect relationships
    |
    +--- Difficult migrations
    |
    +--- Production data problems
```

With this document:

```text
Business Requirement
        |
        v
Database Design
        |
        v
Application Design
        |
        v
Migration
        |
        v
Production Database
```

The document becomes the reference point for database decisions.

---

# 64. File Creation Plan

At this stage:

```text
docs/database-design.md
```

is being created.

Later files will be added.

Example:

```text
application/services/user-service/
application/services/product-service/
application/services/cart-service/
application/services/order-service/
application/services/payment-service/
```

Database migration files will be created inside the appropriate service according to the selected application technology.

---

# 65. Database Design → Application Connection

Later:

```text
User Service
    |
    +--- Controller / API
    |
    +--- Service Layer
    |
    +--- Repository / Data Access
    |
    +--- DB Configuration
    |
    v
User PostgreSQL Database
```

For Product Service:

```text
Product API
    |
    v
Product Service
    |
    v
Product Repository
    |
    v
Product PostgreSQL Database
```

Each service should know only what it needs to know about its own data.

---

# 66. Database Design → Terraform Connection

```text
database-design.md
       |
       v
Database Requirements
       |
       v
terraform/modules/database
       |
       v
RDS PostgreSQL
```

The design document defines what is required.

Terraform implements the infrastructure.

---

# 67. Database Design → Kubernetes Connection

Later, Kubernetes deployments will provide application configuration.

Conceptually:

```text
Kubernetes Deployment
        |
        +--- DB_HOST
        +--- DB_PORT
        +--- DB_NAME
        +--- DB_USER
        +--- DB_SECRET
        |
        v
Application Pod
        |
        v
Private RDS PostgreSQL
```

Sensitive values should be retrieved through an appropriate secret-management mechanism.

---

# 68. Database Design → CI/CD Connection

Later, Jenkins will build and deploy application services.

A simplified flow:

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
Deploy
   |
   v
Database Migration
   |
   v
Application
```

Database migrations must be controlled carefully in CI/CD.

A bad migration can affect production data even if the application image itself is correct.

---

# 69. Database Rollback Considerations

Application rollback and database rollback are not always the same.

Example:

```text
Application v2
      |
      v
Migration v2
      |
      v
Production
```

If application deployment fails:

```text
Application
    |
    v
Rollback to v1
```

But the database may already contain schema changes from v2.

Therefore database migrations should be designed with backward compatibility where practical.

A safe enterprise approach is often:

```text
Expand
  |
  v
Deploy compatible application
  |
  v
Migrate data
  |
  v
Remove old structure later
```

This reduces the risk of application/database version mismatch.

---

# 70. Enterprise Best Practices

## Database

* Use PostgreSQL.
* Use persistent storage.
* Use service-owned data.
* Use primary keys.
* Use appropriate foreign keys within service boundaries.
* Use unique constraints.
* Use indexes based on real query patterns.
* Use transactions for local business operations.
* Use migrations.
* Never hard-code passwords.
* Keep production DB private.
* Use encryption.
* Enable backups.
* Test recovery.
* Monitor database health.

## Microservices

* Avoid shared database ownership.
* Avoid direct cross-service database queries.
* Use service APIs.
* Treat cross-service IDs as logical references.
* Use idempotency for retryable operations.
* Use explicit order/payment states.
* Introduce Saga/Outbox/event patterns as system complexity grows.

---

# 71. Interview Explanation — 7-Year Level

### Interviewer:

**How did you design the database for ShopSphere?**

### Answer:

> "ShopSphere is a microservices-based e-commerce application, so I designed the database around service ownership rather than creating one shared database schema for all services.
>
> User Service owns user data, Product Service owns product and inventory data, Cart Service owns cart data, Order Service owns orders, and Payment Service owns payment records.
>
> PostgreSQL is used as the relational database because we need transactions, constraints, relationships, indexing, and persistent storage.
>
> For cross-service relationships, we store logical IDs such as user_id, product_id, and order_id rather than creating foreign keys across independent databases.
>
> For example, Order Service stores the user ID but does not directly query the User database. If it needs user information, it communicates with User Service through an API.
>
> For orders, I also store the product price inside order_items as a transaction-time price snapshot, because the current product price can change later while historical orders must remain accurate.
>
> In AWS, the production PostgreSQL database is designed to run privately using RDS, with security groups allowing PostgreSQL access only from the application layer.
>
> Terraform provisions the database infrastructure, while version-controlled database migrations manage the application schema.
>
> For distributed operations such as order and payment, I don't depend on a single distributed database transaction. I use explicit business states and, as the system evolves, patterns such as Saga or Outbox can be introduced."

---

# 72. Important Interview Questions

## Q1. Why PostgreSQL?

Because ShopSphere needs relational data, transactions, constraints, indexes, and persistent storage. PostgreSQL provides these capabilities and is well suited for transactional e-commerce workloads.

---

## Q2. Why not one database for all microservices?

A shared database creates tight coupling between services.

If every service directly modifies the same tables:

```text
User Service
     |
Product Service
     |
Order Service
     |
Payment Service
     |
     v
Shared DB
```

then database changes can affect multiple services.

Service-owned data reduces this coupling.

---

## Q3. Does every microservice need a separate physical RDS instance?

Not necessarily.

The important architectural principle is clear ownership of data.

For a learning or smaller deployment, multiple logical PostgreSQL databases can be hosted in a controlled environment.

For production, services can be separated further depending on:

* Scale
* Security
* Availability
* Performance
* Compliance
* Operational requirements

---

## Q4. Why does order_items contain price?

To preserve the price that was actually charged at the time of purchase.

The current product price can change later.

---

## Q5. Can Order Service directly query Product DB?

No, not as the normal microservice design.

Order Service should communicate with Product Service.

```text
Order Service
      |
      v
Product API
      |
      v
Product DB
```

---

## Q6. What is a primary key?

A primary key uniquely identifies a record.

Example:

```text
orders.id
```

---

## Q7. What is a foreign key?

A foreign key establishes a database-enforced relationship between tables, normally within the same service-owned database.

Example:

```text
order_items.order_id
        |
        v
orders.id
```

---

## Q8. How do you handle distributed transactions?

I avoid assuming one ACID transaction can span independent service databases.

Instead, services manage local transactions and coordinate through APIs/events.

For more advanced workflows, I would use patterns such as:

```text
Saga
Outbox
Event-driven processing
Idempotency
Retries
```

---

## Q9. How do you protect the production database?

I would:

```text
Private Subnets
+
Security Groups
+
Encryption
+
Secrets Management
+
Least Privilege
+
Backups
+
Monitoring
```

The database should not be publicly accessible.

---

## Q10. Does Terraform create database tables?

Normally Terraform provisions infrastructure.

```text
Terraform
   |
   v
RDS
```

Database migrations create application tables:

```text
Migration
   |
   v
PostgreSQL Schema
```

---

# 73. Final Database Architecture

```text
                           USERS
                             |
                             v
                        ShopSphere
                             |
       +---------------------+----------------------+
       |                     |                      |
       v                     v                      v
 User Service         Product Service          Cart Service
       |                     |                      |
       v                     v                      v
    User DB             Product DB               Cart DB
                             |
                             |
                       Product / Stock
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
                     RDS PostgreSQL
                             |
                +------------+------------+
                |            |            |
                v            v            v
             Backup       Security     Monitoring
```

---

# 74. End-to-End Data Flow

```text
Customer
   |
   v
Frontend
   |
   v
API / Ingress
   |
   +-------------------+
   |                   |
   v                   v
User Service      Product Service
   |                   |
   v                   v
User DB            Product DB
                       |
                       v
                  Cart Service
                       |
                       v
                    Cart DB
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
```

---

# 75. Database Design Principles

ShopSphere follows these principles:

```text
1. Persistent relational storage
2. PostgreSQL
3. Service-owned data
4. Clear database boundaries
5. No direct cross-service DB access
6. Strong local transactions
7. Historical order accuracy
8. Proper indexing
9. Version-controlled migrations
10. Private production database
11. Secure credentials
12. Backup and recovery
13. Monitoring
14. Explicit failure states
15. Infrastructure managed through Terraform
```

---

# 76. Current Project Status

Completed:

```text
1. Business Requirements       DONE
2. Architecture Design         DONE
3. Database Design             DONE
```

Next:

```text
4. Repository Structure
```

Target document:

```text
docs/repository-structure.md
```

That document will explain:

* Complete Git repository structure
* Why each folder exists
* Why each file will be created
* Frontend structure
* Microservice structure
* Docker structure
* Kubernetes structure
* Jenkins structure
* Terraform structure
* Documentation structure
* How all folders connect
* Created vs modified files
* Real-time enterprise workflow
* What happens if a folder/file is missing
* Git branching strategy
* Developer → Git → Jenkins → Docker → ECR → EKS → RDS flow
* Interview explanation
* Validation and troubleshooting

---

# 77. Final Takeaway

The most important database design decision in ShopSphere is:

```text
                    SERVICE OWNERSHIP
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
      User Data       Product Data      Order Data
          |                |                |
          v                v                v
       User DB         Product DB        Order DB
```

The application does **not** treat PostgreSQL as one giant shared database where every microservice can access every table.

Instead:

```text
Service
   |
   v
Own Business Data
   |
   v
Own Database Boundary
```

This keeps the architecture aligned with microservices while still giving ShopSphere the reliability and transactional capabilities of PostgreSQL.

**Next document: `docs/repository-structure.md`**
