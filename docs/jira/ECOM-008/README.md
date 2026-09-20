# Terraform Enterprise Infrastructure 

# Terraform Project

**Repository:** `ShopSphere-E-Commererce-Platform`
**Primary Technology:** Terraform
**Infrastructure:** AWS
**Purpose:** Infrastructure as Code (IaC)
**Project Level:** Enterprise / DevOps / Interview Preparation

---

# 1. Project Overview

This repository contains Terraform code used to provision AWS infrastructure using **Infrastructure as Code (IaC)**.

Instead of manually creating AWS resources through the AWS Console, Terraform allows us to define infrastructure using `.tf` files.

Terraform reads the configuration, compares the desired infrastructure with the current infrastructure, creates an execution plan, and applies the required changes.

The project is structured using a reusable **VPC child module** and root-level Terraform configuration.

---

# 2. Business Requirement

In an enterprise DevOps environment, infrastructure should not depend on manual AWS Console operations.

The organization needs:

* Repeatable infrastructure
* Version-controlled infrastructure
* Reusable modules
* Consistent environments
* Reviewable infrastructure changes
* Infrastructure drift detection
* Automated deployments
* Easy rollback
* CI/CD integration
* Separate development and production environments

Terraform provides these capabilities.

---

# 3. Why Business Needs Terraform

Imagine an organization needs the following infrastructure:

```text
VPC
 ├── Public Subnet
 ├── Private Subnet
 ├── Internet Gateway
 ├── Route Tables
 ├── Security Groups
 └── EC2 Instances
```

Creating everything manually through AWS Console is time-consuming and error-prone.

With Terraform:

```text
Terraform Code
      |
      v
terraform plan
      |
      v
Review Changes
      |
      v
terraform apply
      |
      v
AWS Infrastructure
```

The same Terraform code can be used to create consistent infrastructure across environments.

---

# 4. Real-Time Enterprise Scenario

Suppose a company has three environments:

```text
Development
     |
     v
QA
     |
     v
Production
```

Each environment requires:

* VPC
* Subnets
* Security Groups
* EC2
* Load Balancer
* Database
* Monitoring

Without IaC:

```text
Engineer manually creates infrastructure
             |
             v
Different configuration
             |
             v
Environment inconsistency
             |
             v
Production problems
```

With Terraform:

```text
Git Repository
      |
      v
Terraform Code
      |
      v
terraform plan
      |
      v
Code Review
      |
      v
terraform apply
      |
      v
AWS
```

Infrastructure becomes repeatable and auditable.

---

# 5. Infrastructure Architecture

```text
                    Git Repository
                          |
                          v
                  Terraform Configuration
                          |
              +-----------+-----------+
              |                       |
              v                       v
        Root Terraform           VPC Module
        Configuration             Child Module
              |                       |
              +-----------+-----------+
                          |
                          v
                    AWS Provider
                          |
                          v
                       AWS
                          |
              +-----------+-----------+
              |                       |
              v                       v
             VPC                    Jenkins
              |                     EC2
       +------+------+                |
       |             |                |
   Subnets       Networking           |
                                      |
                                      v
                              Jenkins CI Server
```

---

# 6. Terraform Execution Flow

The enterprise Terraform workflow is:

```text
Developer
   |
   v
Modify Terraform Code
   |
   v
git diff
   |
   v
terraform fmt
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Code Review
   |
   v
terraform apply
   |
   v
AWS Infrastructure
```

---

# 7. Repository Structure

Current project structure:

```text
terraform-project/
│
└── terraform/
    │
    ├── .gitignore
    ├── .terraform.lock.hcl
    │
    ├── main.tf
    ├── providers.tf
    ├── versions.tf
    ├── variables.tf
    │
    ├── jenkins-ec2.tf
    ├── jenkins-security-group.tf
    ├── jenkins-variables.tf
    │
    ├── tfplan
    │
    └── modules/
        │
        └── vpc/
            ├── main.tf
            ├── variables.tf
            └── outputs.tf
```

---

# 8. Root Module vs Child Module

This is one of the most important Terraform interview concepts.

## Root Module

The directory from which Terraform commands are executed is the **root module**.

In this project:

```text
terraform/
```

is the root module.

It contains:

```text
main.tf
providers.tf
versions.tf
variables.tf
jenkins-ec2.tf
jenkins-security-group.tf
```

---

# 9. Child Module

The reusable VPC module is:

```text
terraform/modules/vpc/
```

It contains:

```text
main.tf
variables.tf
outputs.tf
```

The root module calls this child module.

Conceptually:

```text
Root Module
     |
     | calls
     v
VPC Child Module
     |
     v
Creates VPC Infrastructure
```

---

# 10. Parent and Child Module Relationship

Terraform terminology can be confusing.

In this project:

```text
terraform/
    |
    | module "vpc"
    v
modules/vpc/
```

The root module is the **calling module**.

The VPC module is the **called child module**.

You can explain it in an interview like this:

> "The root module contains the environment-level Terraform configuration, and it calls reusable child modules such as the VPC module. The child module contains the reusable resource definitions, variables, and outputs."

---

# 11. `versions.tf`

Purpose:

```text
Terraform and provider version requirements
```

Typical configuration:

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}
```

The purpose is to control Terraform and provider compatibility.

---

# 12. Why Provider Version Matters

Suppose today:

```text
AWS Provider = 6.x
```

Tomorrow a new provider version introduces a breaking change.

Without version constraints, the infrastructure build may behave differently.

Therefore enterprise Terraform projects pin or constrain provider versions.

---

# 13. `.terraform.lock.hcl`

This file records the selected provider version and checksums.

Example:

```text
terraform-provider-aws
```

Terraform uses the lock file to ensure consistent provider installation.

Best practice:

```text
Commit .terraform.lock.hcl
```

Do not normally delete it from Git.

---

# 14. `providers.tf`

The AWS provider tells Terraform how to communicate with AWS.

Example:

```hcl
provider "aws" {
  region = var.aws_region
}
```

Flow:

```text
Terraform
    |
    v
AWS Provider
    |
    v
AWS APIs
    |
    v
AWS Resources
```

---

# 15. Authentication

Terraform should not normally contain AWS access keys directly inside `.tf` files.

Preferred approaches include:

```text
AWS CLI Profile
Environment Variables
IAM Role
EC2 Instance Profile
OIDC
CI/CD Identity
```

For Jenkins:

```text
Jenkins
   |
   v
IAM Role / OIDC
   |
   v
AWS APIs
```

Never commit:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

into Git.

---

# 16. `variables.tf`

Variables make Terraform configuration reusable.

Example:

```hcl
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}
```

Instead of hardcoding:

```hcl
region = "ap-south-1"
```

we use:

```hcl
region = var.aws_region
```

---

# 17. Why Variables Are Important

Without variables:

```text
Development
    |
    | hardcoded values
    v
Production
    |
    | copy/paste
    v
Different configuration
```

With variables:

```text
Same Terraform Code
       |
       +---- dev values
       |
       +---- QA values
       |
       +---- prod values
```

This improves reusability.

---

# 18. Variable Flow

```text
variables.tf
     |
     v
Variable Definition
     |
     v
Variable Value
     |
     v
Terraform Resource
```

Possible value sources:

```text
Default
terraform.tfvars
*.auto.tfvars
-var
Environment Variable
CI/CD Pipeline
```

---

# 19. `main.tf`

`main.tf` generally contains the main infrastructure configuration or module calls.

For example:

```hcl
module "vpc" {
  source = "./modules/vpc"

  ...
}
```

This means:

```text
Root main.tf
     |
     v
modules/vpc/main.tf
```

---

# 20. VPC Module

The VPC module is responsible for networking infrastructure.

Conceptually:

```text
VPC Module
    |
    +-- VPC
    |
    +-- Subnets
    |
    +-- Internet Gateway
    |
    +-- Route Tables
    |
    +-- Associations
```

The main benefit is reuse.

---

# 21. VPC Module `main.tf`

A VPC resource typically looks like:

```hcl
resource "aws_vpc" "this" {
  cidr_block = var.vpc_cidr

  tags = {
    Name = var.vpc_name
  }
}
```

Terraform understands:

```text
resource "aws_vpc" "this"
```

as an AWS VPC resource managed by Terraform.

---

# 22. VPC Module `variables.tf`

The module receives values from the root module.

Example:

```hcl
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
}
```

This creates a contract between:

```text
Root Module
     |
     | input
     v
Child Module
```

---

# 23. VPC Module `outputs.tf`

Outputs expose values from a child module.

Example:

```hcl
output "vpc_id" {
  value = aws_vpc.this.id
}
```

Flow:

```text
VPC Resource
     |
     v
Module Output
     |
     v
Root Module
```

Outputs are especially useful when one module depends on another.

---

# 24. Module Input and Output

Think of a module like a function.

```text
Input
  |
  v
+----------------+
|   VPC MODULE   |
+----------------+
  |
  v
Output
```

Example:

```text
Input:
vpc_cidr = 10.0.0.0/16

Output:
vpc_id
```

---

# 25. Jenkins EC2 Infrastructure

This project also contains Terraform configuration for Jenkins infrastructure.

Typical flow:

```text
Terraform
    |
    +---- Security Group
    |
    +---- EC2 Instance
    |
    +---- Jenkins Server
```

The Jenkins EC2 configuration is separated into:

```text
jenkins-ec2.tf
jenkins-security-group.tf
jenkins-variables.tf
```

This improves readability.

---

# 26. Why Split Terraform Files?

Terraform does not require one resource per file.

Terraform loads all `.tf` files in the same directory.

Therefore:

```text
main.tf
jenkins-ec2.tf
jenkins-security-group.tf
variables.tf
```

are logically one root module.

The split is for human organization.

Terraform sees:

```text
All .tf files
      |
      v
One Root Module
```

---

# 27. Important Interview Question

### Does Terraform execute `main.tf` first?

**No.**

Terraform loads all `.tf` files in the working directory.

The filenames are primarily for organization.

For example:

```text
main.tf
network.tf
ec2.tf
security.tf
outputs.tf
```

are all part of the same module.

Terraform builds a dependency graph from the configuration.

---

# 28. Terraform Dependency Graph

Suppose:

```text
VPC
 |
 v
Subnet
 |
 v
EC2
 |
 v
Application
```

Terraform understands dependencies.

Example:

```text
VPC
 |
 +--> Subnet
       |
       +--> EC2
```

Terraform creates resources in dependency order when dependencies are known.

---

# 29. Explicit Dependency

Sometimes we use:

```hcl
depends_on = [
  aws_vpc.this
]
```

This explicitly tells Terraform:

```text
Create A before B
```

However, implicit dependencies are preferred when Terraform can infer them naturally.

---

# 30. Terraform State

Terraform state is one of the most important concepts.

Terraform needs to remember:

```text
What Terraform manages
What resources exist
Resource IDs
Resource attributes
Dependencies
```

The default local state file is:

```text
terraform.tfstate
```

---

# 31. Desired vs Actual vs State

There are three important things:

```text
Terraform Code
     |
     | desired state
     v

Terraform State
     |
     | Terraform's recorded knowledge
     v

AWS Infrastructure
     |
     | actual state
     v
```

Terraform compares these to determine required actions.

---

# 32. Terraform State Example

Suppose Terraform created:

```text
EC2 Instance
```

AWS gives it:

```text
i-0123456789
```

Terraform stores information about the managed resource in state.

Conceptually:

```text
Terraform
    |
    v
terraform.tfstate
    |
    v
aws_instance.jenkins
    |
    v
i-0123456789
```

---

# 33. Why State Is Critical

If state is lost:

```text
Terraform
    |
    X
Cannot accurately map configuration
to existing infrastructure
```

Therefore enterprise teams protect state carefully.

---

# 34. Local State vs Remote State

Local:

```text
terraform.tfstate
```

Enterprise:

```text
Terraform
    |
    v
Remote Backend
    |
    v
S3
    +
Locking mechanism
```

Remote state allows teams and CI/CD systems to work with shared infrastructure state.

---

# 35. State Locking

Imagine:

```text
Developer A
    |
terraform apply
    |
    v
State locked

Developer B
    |
terraform apply
    |
    X
```

Locking prevents concurrent state modifications.

Without locking:

```text
Engineer A ----+
               |
               v
            State
               ^
               |
Engineer B ----+
```

This can cause corruption or conflicting changes.

---

# 36. Terraform Backend

A common AWS enterprise architecture is:

```text
Terraform
    |
    v
S3 Backend
    |
    +-- State Storage
    |
    +-- Versioning
    |
    +-- Encryption
```

State should be protected because it may contain sensitive infrastructure information.

---

# 37. `terraform init`

First command after cloning a Terraform project:

```bash
terraform init
```

Purpose:

* Initialize Terraform
* Download providers
* Initialize modules
* Initialize backend
* Prepare working directory

Expected concept:

```text
Initializing provider plugins...
Terraform has been successfully initialized!
```

---

# 38. `.terraform` Directory

After:

```bash
terraform init
```

Terraform may create:

```text
.terraform/
```

This directory contains Terraform's local working data and downloaded provider/module components.

Normally it should not be committed to Git.

---

# 39. `.gitignore`

Typical entries:

```gitignore
.terraform/
*.tfstate
*.tfstate.*
*.tfplan
crash.log
*.tfvars
```

But be careful with:

```text
*.tfvars
```

because some teams intentionally commit non-secret environment variable files.

The important rule is:

```text
Never commit secrets.
```

---

# 40. Terraform Format

Run:

```bash
terraform fmt
```

Purpose:

```text
Standardize Terraform formatting
```

Example:

```text
Before:
resource "aws_instance" "jenkins"{ami=var.ami}

After:
resource "aws_instance" "jenkins" {
  ami = var.ami
}
```

---

# 41. Terraform Validate

Run:

```bash
terraform validate
```

Purpose:

```text
Check Terraform configuration syntax
and internal consistency
```

Expected:

```text
Success! The configuration is valid.
```

Important:

`terraform validate` does not mean the infrastructure will definitely work in AWS.

---

# 42. Terraform Plan

Run:

```bash
terraform plan
```

Terraform calculates what it wants to change.

Example:

```text
+ create
~ update
- destroy
-/+ replace
```

Example:

```text
Plan: 3 to add, 0 to change, 0 to destroy.
```

---

# 43. Meaning of Plan Symbols

```text
+       Create

~       Update in-place

-       Destroy

-/+     Destroy and recreate

<=      Read data
```

The `-/+` case is especially important.

It means Terraform cannot safely update the resource in place and must replace it.

---

# 44. Terraform Apply

Run:

```bash
terraform apply
```

Terraform asks for confirmation.

Example:

```text
Do you want to perform these actions?
Only 'yes' will be accepted to approve.
```

Then:

```text
yes
```

Terraform creates or modifies the infrastructure.

---

# 45. Plan File

A safer enterprise workflow is:

```bash
terraform plan -out=tfplan
```

Then:

```bash
terraform apply tfplan
```

Flow:

```text
terraform plan
      |
      v
Saved Plan
      |
      v
Review
      |
      v
terraform apply tfplan
```

This ensures the reviewed plan is the plan being applied.

---

# 46. Terraform Destroy

Command:

```bash
terraform destroy
```

This removes resources managed by Terraform.

Example:

```text
Plan: 0 to add, 0 to change, 5 to destroy.
```

Use extreme caution in production.

---

# 47. Terraform Refresh Concept

Modern Terraform workflows generally refresh state during planning operations.

You can inspect current infrastructure with:

```bash
terraform plan
```

If infrastructure was changed manually in AWS, Terraform may detect differences.

This is called:

```text
Drift
```

---

# 48. Terraform Drift

Suppose Terraform created:

```text
EC2
Instance Type = t3.micro
```

Someone manually changes it in AWS:

```text
t3.medium
```

Now:

```text
Terraform Code
     |
     | t3.micro
     v
AWS
     |
     | t3.medium
```

Terraform detects the difference.

That difference is infrastructure drift.

---

# 49. Drift Detection Workflow

```text
Terraform Code
      |
      v
terraform plan
      |
      v
Compare State / AWS
      |
      v
Drift Detected
      |
      v
Investigate
      |
      +------+
      |      |
      v      v
Code Fix   Manual Change
      |      |
      +------+
          |
          v
terraform plan
```

---

# 50. Terraform Import

Suppose an EC2 instance already exists in AWS:

```text
Created manually
```

But Terraform does not manage it.

You can import it.

Modern Terraform supports configuration-driven import, while the traditional command is:

```bash
terraform import aws_instance.example i-0123456789
```

Important:

Import brings the resource into Terraform state, but you still need matching Terraform configuration.

---

# 51. Import Flow

```text
Existing AWS Resource
        |
        v
terraform import
        |
        v
Terraform State
        |
        v
Write matching .tf configuration
        |
        v
terraform plan
```

---

# 52. Import Does Not Automatically Write Perfect Configuration

Interview answer:

> "Terraform import associates an existing infrastructure resource with a Terraform resource address in state. It does not by itself produce a complete, production-ready Terraform configuration. I still need to define the resource configuration and validate it with plan."

---

# 53. Terraform Resource Address

Example:

```text
aws_instance.jenkins
```

For a module:

```text
module.vpc.aws_vpc.this
```

The address identifies the Terraform-managed object.

---

# 54. Terraform State Commands

Useful commands:

```bash
terraform state list
```

Shows resources tracked in state.

```bash
terraform state show aws_instance.jenkins
```

Shows detailed state information.

```bash
terraform state pull
```

Retrieves the current state.

---

# 55. Terraform Outputs

Run:

```bash
terraform output
```

Specific output:

```bash
terraform output vpc_id
```

Useful for:

```text
VPC ID
Subnet ID
EC2 IP
Load Balancer DNS
```

---

# 56. Terraform Console

Command:

```bash
terraform console
```

Useful for testing expressions.

Example:

```text
var.aws_region
```

This helps debug Terraform expressions.

---

# 57. Terraform Data Sources

Resources create infrastructure.

Data sources read existing information.

Example concept:

```text
Resource:
Create EC2

Data Source:
Find AMI
```

Example:

```hcl
data "aws_ami" "ubuntu" {
  ...
}
```

Flow:

```text
AWS
 |
 v
Data Source
 |
 v
Terraform
 |
 v
Resource
```

---

# 58. Resource vs Data Source

### Resource

```text
Creates/manages infrastructure
```

### Data Source

```text
Reads existing information
```

Interview answer:

> "Resources manage infrastructure, while data sources allow Terraform to query existing information that can be used by resources or other configuration."

---

# 59. Terraform Modules — Enterprise Purpose

Modules prevent repeated infrastructure code.

Without modules:

```text
Dev VPC code
QA VPC code
Prod VPC code
```

Large duplication.

With module:

```text
Reusable VPC Module
       |
       +---- Dev
       |
       +---- QA
       |
       +---- Prod
```

---

# 60. Module Reusability

Example:

```hcl
module "vpc" {
  source = "./modules/vpc"

  vpc_cidr = "10.0.0.0/16"
}
```

Another environment:

```hcl
module "vpc" {
  source = "./modules/vpc"

  vpc_cidr = "10.1.0.0/16"
}
```

Same module.

Different inputs.

---

# 61. Terraform Module Design

A good module generally contains:

```text
module/
├── main.tf
├── variables.tf
└── outputs.tf
```

Optional:

```text
README.md
versions.tf
data.tf
locals.tf
```

---

# 62. Module Best Practices

Good modules should:

* Have clear inputs
* Have useful outputs
* Avoid unnecessary hardcoding
* Use meaningful names
* Have documentation
* Be reusable
* Have predictable behavior
* Avoid hidden dependencies

---

# 63. Terraform `count`

`count` creates multiple instances based on a number.

Example:

```hcl
resource "aws_instance" "server" {
  count = 2

  ...
}
```

Terraform addresses them as:

```text
aws_instance.server[0]
aws_instance.server[1]
```

---

# 64. Terraform `for_each`

`for_each` creates resources from a set or map.

Example:

```hcl
resource "aws_instance" "server" {
  for_each = {
    app1 = "t3.micro"
    app2 = "t3.small"
  }

  instance_type = each.value
}
```

Addresses:

```text
aws_instance.server["app1"]
aws_instance.server["app2"]
```

---

# 65. Count vs For Each

### count

Best when:

```text
Instances are identical
```

### for_each

Best when:

```text
Each instance has a meaningful key/value
```

Interview explanation:

> "I use count when the instances are essentially identical and number-based indexing is sufficient. I prefer for_each when resources have meaningful identities or different configuration values, because the map keys provide more stable addresses."

---

# 66. Terraform Locals

Locals allow reusable expressions.

Example:

```hcl
locals {
  common_tags = {
    Project     = "Terraform"
    Environment = "dev"
  }
}
```

Then:

```hcl
tags = local.common_tags
```

---

# 67. Terraform Outputs and Module Dependencies

Example:

```text
VPC Module
    |
    | vpc_id
    v
EC2 Module
```

The VPC module outputs:

```text
vpc_id
```

The EC2 configuration consumes:

```text
module.vpc.vpc_id
```

This creates a dependency.

---

# 68. EC2 in Different Availability Zones

Enterprise interview scenario:

```text
VPC
 |
 +--- AZ-a
 |     |
 |     +--- Subnet A
 |
 +--- AZ-b
       |
       +--- Subnet B
```

EC2 instances can be placed in different AZs for higher availability.

Example concept:

```text
EC2-1 -> subnet-a -> AZ-a
EC2-2 -> subnet-b -> AZ-b
```

---

# 69. Specific Subnet Requirement

Interview question:

> "How would you launch an EC2 instance in a specific subnet?"

Answer:

Use the subnet ID:

```hcl
subnet_id = aws_subnet.private_a.id
```

or:

```hcl
subnet_id = var.subnet_id
```

The subnet determines the Availability Zone.

---

# 70. Terraform Security Groups

Security groups should be defined as code.

Example concept:

```text
Security Group
 |
 +--- SSH 22
 |
 +--- HTTP 80
 |
 +--- HTTPS 443
```

Avoid:

```text
0.0.0.0/0
```

for sensitive ports unless there is a justified requirement.

---

# 71. Jenkins Security Group

For a Jenkins server, common ports may include:

```text
22     SSH
8080   Jenkins
```

Production access should preferably be restricted through:

```text
VPN
Bastion
SSM
Private networking
Trusted CIDR
Load balancer
```

rather than exposing administrative ports globally.

---

# 72. Infrastructure Tags

Enterprise infrastructure should use consistent tags.

Example:

```hcl
tags = {
  Project     = "ShopSphere"
  Environment = "dev"
  ManagedBy   = "Terraform"
  Owner       = "DevOps"
}
```

Tags help with:

* Cost tracking
* Ownership
* Automation
* Inventory
* Auditing

---

# 73. Terraform Naming Convention

Use meaningful resource names.

Good:

```text
aws_instance.jenkins
aws_security_group.jenkins
module.vpc
```

Avoid:

```text
aws_instance.test1
aws_instance.final
aws_instance.new
```

Infrastructure names should explain their purpose.

---

# 74. Terraform Workflow in Git

Enterprise workflow:

```text
Developer
   |
   v
Create Feature Branch
   |
   v
Modify Terraform
   |
   v
terraform fmt
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Commit
   |
   v
Pull Request
   |
   v
Review
   |
   v
CI Validation
   |
   v
Apply
```

---

# 75. Recommended Git Workflow

```bash
git switch develop
git pull origin develop

git switch -c feature/terraform-networking

terraform fmt
terraform validate
terraform plan

git status
git diff

git add terraform/
git commit -m "Add Terraform networking infrastructure"

git push -u origin feature/terraform-networking
```

---

# 76. Terraform CI/CD

A Jenkins pipeline can automate Terraform validation.

Typical pipeline:

```text
Git Push
   |
   v
Jenkins
   |
   +--> terraform fmt -check
   |
   +--> terraform init
   |
   +--> terraform validate
   |
   +--> terraform plan
   |
   v
Approval
   |
   v
terraform apply
```

---

# 77. Enterprise Terraform Pipeline

A stronger pipeline:

```text
Checkout
   |
   v
Terraform Format Check
   |
   v
Terraform Init
   |
   v
Terraform Validate
   |
   v
Security Scan
   |
   v
Terraform Plan
   |
   v
Manual Approval
   |
   v
Terraform Apply
   |
   v
Validation
```

Possible security tools:

```text
Trivy
Checkov
tfsec
TFLint
```

---

# 78. Terraform Plan in CI

CI should generate a plan.

Example:

```bash
terraform plan -out=tfplan
```

The plan should be reviewed before production deployment.

This provides infrastructure change visibility.

---

# 79. Terraform Apply in Production

A common enterprise model:

```text
Developer
    |
    v
Pull Request
    |
    v
Terraform Plan
    |
    v
Review
    |
    v
Approval
    |
    v
Terraform Apply
```

Production infrastructure should not normally be changed manually.

---

# 80. Infrastructure Drift Policy

If someone manually changes AWS:

```text
AWS Console
    |
    v
Manual Change
    |
    v
Drift
```

The team should decide whether:

```text
Option 1:
Revert the manual change
```

or:

```text
Option 2:
Update Terraform code
```

Terraform should remain the source of truth.

---

# 81. Terraform Rollback

Terraform rollback is different from application rollback.

Application:

```text
Version 2
   |
   v
Rollback
   |
   v
Version 1
```

Terraform:

```text
Current Configuration
       |
       v
Previous Git Commit
       |
       v
terraform plan
       |
       v
terraform apply
```

The desired infrastructure configuration is changed back.

---

# 82. Terraform Rollback Example

Suppose:

```text
Before:
instance_type = "t3.micro"
```

Changed to:

```text
instance_type = "t3.medium"
```

To roll back:

```text
Git checkout previous version
       |
       v
terraform plan
       |
       v
Review
       |
       v
terraform apply
```

---

# 83. Important Rollback Warning

Do not blindly run:

```bash
terraform destroy
```

as a rollback mechanism.

Terraform rollback normally means:

```text
Restore desired configuration
```

then:

```text
terraform plan
terraform apply
```

---

# 84. Terraform State Rollback

State rollback is a different operation.

If the state becomes corrupted or an incorrect state change occurs, restore from a protected backend/versioned state backup according to the organization's recovery process.

Do not manually edit:

```text
terraform.tfstate
```

unless you fully understand the implications.

---

# 85. Terraform State Commands — Dangerous Operations

Commands such as:

```bash
terraform state rm
terraform state mv
terraform state replace-provider
```

can change Terraform's state relationships.

Use them carefully.

They do not automatically change AWS infrastructure.

---

# 86. `terraform state rm`

Example:

```bash
terraform state rm aws_instance.jenkins
```

This removes the resource from Terraform state.

It does **not** normally destroy the AWS instance.

After this:

```text
AWS Instance
    |
    v
Still exists

Terraform
    |
    X
No longer managing it
```

---

# 87. Terraform Destroy vs State RM

Very important interview comparison:

```text
terraform destroy
```

means:

```text
Destroy managed infrastructure
```

Whereas:

```text
terraform state rm
```

means:

```text
Remove resource from Terraform state
```

The AWS resource may continue to exist after `state rm`.

---

# 88. Terraform Taint / Replacement Concept

If a resource needs replacement, Terraform may show:

```text
-/+ destroy and create replacement
```

Modern Terraform workflows generally use:

```bash
terraform apply -replace="aws_instance.jenkins"
```

rather than older taint-based workflows.

---

# 89. Terraform Troubleshooting Method

Use this enterprise RCA flow:

```text
Problem
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Terraform logs
   |
   v
AWS resource status
   |
   v
IAM permissions
   |
   v
Networking
   |
   v
State
   |
   v
Provider
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Plan
   |
   v
Apply
   |
   v
Validate
```

---

# 90. Common Problem — Provider Error

Symptom:

```text
Failed to load plugin
```

Check:

```bash
terraform version
terraform providers
terraform init
```

Possible causes:

* Provider version mismatch
* Corrupted provider download
* Lock file issue
* Network issue

---

# 91. Common Problem — Authentication Failure

Example:

```text
UnauthorizedOperation
AccessDenied
```

Check:

```bash
aws sts get-caller-identity
```

Then verify:

```text
AWS identity
IAM permissions
AWS region
Credentials
Role
```

---

# 92. Common Problem — Wrong Region

Terraform:

```text
ap-south-1
```

But the resource was expected in:

```text
us-east-1
```

Check:

```hcl
provider "aws" {
  region = var.aws_region
}
```

Then:

```bash
terraform plan
```

---

# 93. Common Problem — Resource Already Exists

Terraform may return:

```text
already exists
```

Possible reason:

```text
Resource was created manually
```

Solution:

```text
Option 1:
Delete it if it is not required

Option 2:
Import it into Terraform
```

Do not create duplicate resources blindly.

---

# 94. Common Problem — Dependency Error

Example:

```text
InvalidSubnetID.NotFound
```

Possible reason:

```text
Wrong subnet ID
Wrong region
Resource does not exist
```

Check:

```bash
terraform state list
terraform output
aws ec2 describe-subnets
```

---

# 95. Common Problem — Terraform Wants to Destroy Unexpectedly

Never immediately run:

```bash
terraform apply
```

First inspect:

```bash
terraform plan
```

Understand why Terraform wants:

```text
destroy
```

or:

```text
-/+ replace
```

Possible reasons:

* Configuration change
* Immutable attribute
* Resource renamed
* State mismatch
* Provider behavior
* Drift

---

# 96. `terraform plan` Is Your Safety Net

Before applying:

```bash
terraform plan
```

Always understand:

```text
What will be created?
What will change?
What will be destroyed?
```

Enterprise principle:

```text
Never blindly apply infrastructure changes.
```

---

# 97. Sensitive Variables

Never hardcode:

```hcl
password = "MyPassword123"
```

Prefer:

```hcl
variable "db_password" {
  sensitive = true
}
```

And provide the value through a secure mechanism.

---

# 98. Secret Management

Terraform should integrate with enterprise secret management such as:

```text
AWS Secrets Manager
AWS SSM Parameter Store
Vault
CI/CD secret store
```

Terraform state can still contain sensitive values depending on the resource/provider, so protecting state is essential.

---

# 99. Terraform Security Best Practices

Follow:

```text
Least privilege IAM
No hardcoded credentials
Encrypted state
Remote backend
State locking
Provider pinning
Module versioning
Security scanning
PR review
Sensitive variables
Restricted security groups
Consistent tagging
```

---

# 100. Terraform Performance

Large Terraform projects can become slow.

Improve performance through:

* Modular architecture
* Appropriate resource grouping
* Remote state
* Avoid unnecessary data sources
* Avoid excessive dependencies
* Separate independent stacks where appropriate

Do not create one massive Terraform state for every infrastructure component without considering operational boundaries.

---

# 101. Terraform Workspace

Terraform workspaces allow multiple state instances for the same configuration.

Concept:

```text
Same Code
   |
   +---- workspace dev
   |
   +---- workspace qa
   |
   +---- workspace prod
```

However, many enterprises prefer separate root configurations/directories or separate state boundaries for major environments because isolation and operational clarity can be stronger.

---

# 102. Terraform Environment Strategy

Example:

```text
terraform/
├── modules/
│   ├── vpc/
│   └── ec2/
│
└── environments/
    ├── dev/
    ├── qa/
    └── prod/
```

Each environment can have its own:

```text
Backend
Variables
State
Provider configuration
```

---

# 103. Terraform Enterprise Architecture

A mature structure may look like:

```text
terraform/
│
├── modules/
│   ├── vpc/
│   ├── security-group/
│   ├── ec2/
│   ├── alb/
│   └── rds/
│
└── environments/
    ├── dev/
    ├── qa/
    └── prod/
```

Flow:

```text
Reusable Modules
       |
       v
Environment Configuration
       |
       v
Remote State
       |
       v
AWS
```

---

# 104. Build Once, Reuse Many

Terraform modules should follow a similar principle to application engineering:

```text
Reusable Module
      |
      +---- Dev
      |
      +---- QA
      |
      +---- Production
```

Only environment-specific values should change where possible.

---

# 105. Terraform and GitOps

Terraform infrastructure can be managed using GitOps principles.

```text
Git
 |
 v
Terraform Configuration
 |
 v
Pull Request
 |
 v
Plan
 |
 v
Approval
 |
 v
Apply
 |
 v
AWS
```

Git becomes the auditable source of desired infrastructure configuration.

---

# 106. Terraform + Jenkins

Example enterprise flow:

```text
Developer
   |
   v
Git Push
   |
   v
Jenkins
   |
   +--> Checkout
   |
   +--> terraform fmt
   |
   +--> terraform init
   |
   +--> terraform validate
   |
   +--> Security Scan
   |
   +--> terraform plan
   |
   v
Manual Approval
   |
   v
terraform apply
   |
   v
AWS
```

---

# 107. Terraform + AWS Architecture

Typical DevOps infrastructure:

```text
                    Terraform
                        |
              +---------+---------+
              |                   |
              v                   v
             VPC                 IAM
              |
       +------+------+
       |             |
       v             v
    Public        Private
    Subnets       Subnets
       |             |
       v             v
      ALB          EC2/EKS
                     |
                     v
                    RDS
```

---

# 108. Terraform + EKS

In a larger enterprise:

```text
Terraform
   |
   +--> VPC
   |
   +--> Subnets
   |
   +--> IAM
   |
   +--> EKS
   |
   +--> Node Groups
```

Then:

```text
Argo CD / Jenkins
       |
       v
EKS
```

Terraform creates the infrastructure.

Kubernetes tools manage application workloads.

---

# 109. Terraform vs Kubernetes

Important distinction:

### Terraform

Manages infrastructure:

```text
VPC
EC2
EKS
RDS
IAM
Security Groups
```

### Kubernetes

Manages container workloads:

```text
Deployment
Service
ConfigMap
Secret
Ingress
HPA
```

A common enterprise architecture uses both.

---

# 110. Terraform vs Ansible

### Terraform

Primarily:

```text
Infrastructure provisioning
```

### Ansible

Primarily:

```text
Configuration management
Application/server automation
```

Example:

```text
Terraform
   |
   v
Create EC2

Ansible
   |
   v
Configure EC2
```

---

# 111. Terraform vs CloudFormation

Both are Infrastructure as Code technologies.

Terraform:

```text
Multi-cloud
Reusable modules
Large provider ecosystem
```

CloudFormation:

```text
AWS-native
Deep AWS integration
```

The appropriate choice depends on organizational requirements.

---

# 112. Terraform Idempotency

Terraform aims for predictable desired-state management.

If the infrastructure already matches the configuration:

```bash
terraform plan
```

may show:

```text
No changes.
Your infrastructure matches the configuration.
```

This is an important IaC principle.

---

# 113. Immutable Infrastructure Concept

Instead of manually modifying servers:

```text
Existing Server
     |
     | manual changes
     v
Configuration drift
```

IaC encourages:

```text
Code
 |
 v
Desired Infrastructure
```

For some resources, replacement is preferable to manual mutation.

---

# 114. Enterprise Review Checklist

Before Terraform PR approval:

```text
[ ] terraform fmt
[ ] terraform validate
[ ] terraform plan reviewed
[ ] No hardcoded credentials
[ ] IAM permissions reviewed
[ ] Security groups reviewed
[ ] CIDRs reviewed
[ ] Tags present
[ ] Variables documented
[ ] Outputs documented
[ ] Module reusable
[ ] State strategy verified
[ ] Provider version constrained
[ ] Security scan passed
[ ] Destroy/replacement changes understood
```

---

# 115. Production Checklist

Before production apply:

```text
[ ] Correct AWS account
[ ] Correct region
[ ] Correct workspace/environment
[ ] Correct backend
[ ] Correct state
[ ] Terraform plan reviewed
[ ] Destructive changes checked
[ ] IAM permissions verified
[ ] Backup/recovery understood
[ ] Approval received
```

---

# 116. Important Terraform Commands

Initialization:

```bash
terraform init
```

Formatting:

```bash
terraform fmt
```

Validation:

```bash
terraform validate
```

Planning:

```bash
terraform plan
```

Save plan:

```bash
terraform plan -out=tfplan
```

Apply:

```bash
terraform apply
```

Apply saved plan:

```bash
terraform apply tfplan
```

Destroy:

```bash
terraform destroy
```

Show outputs:

```bash
terraform output
```

List state:

```bash
terraform state list
```

Show resource:

```bash
terraform state show RESOURCE
```

---

# 117. Practical Project Validation

From the Terraform directory:

```bash
cd terraform
```

Check:

```bash
terraform version
```

Initialize:

```bash
terraform init
```

Format:

```bash
terraform fmt -recursive
```

Validate:

```bash
terraform validate
```

Plan:

```bash
terraform plan
```

Review:

```bash
git diff
```

Apply:

```bash
terraform apply
```

---

# 118. AWS Validation

After apply:

```bash
aws sts get-caller-identity
```

Check EC2:

```bash
aws ec2 describe-instances
```

Check VPC:

```bash
aws ec2 describe-vpcs
```

Check subnets:

```bash
aws ec2 describe-subnets
```

Check security groups:

```bash
aws ec2 describe-security-groups
```

---

# 119. Terraform Validation Flow

```text
terraform fmt
       |
       v
terraform validate
       |
       v
terraform plan
       |
       v
AWS Validation
       |
       v
terraform apply
       |
       v
AWS Resource Validation
```

---

# 120. Troubleshooting RCA Template

For every Terraform incident document:

```text
Incident:
Terraform Apply Failure

Symptom:
EC2 creation failed

Error:
AccessDenied

Investigation:
1. Checked Terraform plan
2. Checked AWS identity
3. Checked IAM permissions
4. Checked region
5. Checked provider

Root Cause:
IAM role did not have required permission

Fix:
Updated IAM policy

Validation:
terraform plan
terraform apply
AWS CLI validation

Prevention:
Add IAM validation to CI/CD
```

---

# 121. Enterprise Terraform Incident Flow

```text
Incident
   |
   v
Capture Error
   |
   v
terraform plan
   |
   v
Check Provider
   |
   v
Check Credentials
   |
   v
Check IAM
   |
   v
Check Region
   |
   v
Check State
   |
   v
Check AWS Resource
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Plan
   |
   v
Apply
   |
   v
Validate
   |
   v
Document RCA
```

---

# 122. Common Terraform Mistakes

Avoid:

```text
Hardcoded AWS credentials
Hardcoded environment values
Committing state
Committing secrets
Blind terraform apply
Blind terraform destroy
Ignoring plan output
Manual production changes
Uncontrolled provider upgrades
Huge monolithic modules
Overusing depends_on
```

---

# 123. Enterprise Best Practices

## Code

```text
Use modules
Use variables
Use outputs
Use locals
Use meaningful names
Format code
```

## Security

```text
Least privilege
Secret management
Encrypted state
Restricted network access
Security scanning
```

## Git

```text
Feature branches
Pull requests
Code review
Protected main branch
Commit traceability
```

## Operations

```text
Remote state
State locking
Plan before apply
Approval for production
Drift detection
RCA documentation
```

---

# 124. Terraform Interview Questions

## Q1. What is Terraform?

**Answer:**

> "Terraform is an Infrastructure as Code tool used to define, provision, and manage infrastructure using declarative configuration. I can use Terraform to manage AWS resources such as VPCs, EC2, IAM, security groups, EKS and RDS."

---

## Q2. What is Infrastructure as Code?

**Answer:**

> "Infrastructure as Code means defining infrastructure in code instead of creating it manually. This makes infrastructure repeatable, version-controlled, reviewable and automatable."

---

## Q3. What is a Terraform provider?

**Answer:**

> "A provider is the Terraform plugin that allows Terraform to interact with an external platform such as AWS. The AWS provider communicates with AWS APIs to create and manage resources."

---

## Q4. What is Terraform state?

**Answer:**

> "Terraform state records Terraform's knowledge about managed infrastructure, including resource identities and attributes. Terraform uses state to map configuration to real infrastructure and determine what changes are required."

---

## Q5. Why use remote state?

**Answer:**

> "Remote state provides centralized and controlled state storage for teams and CI/CD systems. It also supports collaboration and, depending on the backend, state locking and recovery capabilities."

---

## Q6. What is Terraform drift?

**Answer:**

> "Drift occurs when infrastructure changes outside Terraform, for example through the AWS Console, causing the actual infrastructure to differ from the Terraform configuration or recorded state."

---

## Q7. How do you detect drift?

**Answer:**

> "I run terraform plan and review the differences between the desired configuration and the current infrastructure state."

---

## Q8. What is terraform plan?

**Answer:**

> "Terraform plan previews the infrastructure changes Terraform intends to make without applying them. I use it as a safety and review step before apply."

---

## Q9. What is terraform apply?

**Answer:**

> "Terraform apply executes the planned infrastructure changes against the target platform."

---

## Q10. What is terraform init?

**Answer:**

> "Terraform init initializes the working directory, downloads required providers and modules, and configures the backend."

---

## Q11. What is a Terraform module?

**Answer:**

> "A module is a reusable collection of Terraform configuration. In this project, I use a VPC child module so networking resources can be reused with different input values."

---

## Q12. Root module vs child module?

**Answer:**

> "The root module is the directory from which Terraform is executed. A child module is a reusable module called by the root or another module. In this project, the root Terraform configuration calls the VPC child module."

---

## Q13. Resource vs data source?

**Answer:**

> "A resource manages infrastructure, while a data source reads existing information from a provider."

---

## Q14. Count vs for_each?

**Answer:**

> "Count is useful when I need a number of similar resources. For_each is useful when resources have meaningful keys or different values because the keys provide stable resource identities."

---

## Q15. What happens if someone manually changes an EC2 instance?

**Answer:**

> "That can create drift. I would run terraform plan, determine the difference, and then either update the Terraform configuration if the change is intended or revert the infrastructure to match the desired configuration."

---

## Q16. What happens if Terraform wants to destroy a production resource?

**Answer:**

> "I would not blindly apply. I would inspect the plan, identify why Terraform wants to destroy or replace the resource, verify the state and configuration, and obtain the required approval before applying."

---

## Q17. Does terraform state rm destroy infrastructure?

**Answer:**

> "No. terraform state rm removes the resource from Terraform state. It does not normally destroy the actual infrastructure."

---

## Q18. How do you import an existing resource?

**Answer:**

> "I define the Terraform resource configuration and associate the existing resource with that Terraform resource address using Terraform's import capability. Then I run terraform plan to reconcile the configuration with the imported infrastructure."

---

## Q19. How do you handle secrets?

**Answer:**

> "I avoid hardcoding secrets in Terraform files. I use secure mechanisms such as AWS Secrets Manager, SSM Parameter Store, CI/CD secret stores or workload identity, and I protect Terraform state because sensitive values can still be represented in state."

---

## Q20. How do you integrate Terraform with Jenkins?

**Answer:**

> "Jenkins checks out the Terraform repository, initializes Terraform, validates and formats the configuration, runs security checks, generates a plan, publishes the plan for review, and after approval runs terraform apply using an appropriate AWS identity."

---

# 125. 7-Year-Level Interview Scenario

### Interviewer:

> "You have an existing production VPC created manually. How would you bring it under Terraform?"

### Answer:

> "First I would inventory the existing VPC resources and understand the current architecture. I would create matching Terraform configuration, import the existing resources into the appropriate Terraform addresses, and then run terraform plan. I would continue reconciling the configuration until the plan represents the intended state without unexpected destructive changes. Once validated and reviewed, I would manage future changes through Terraform and Git rather than making manual changes."

---

# 126. 7-Year-Level Scenario — Production Drift

### Interviewer:

> "Someone changed a production EC2 instance manually. What will you do?"

### Answer:

> "I would first identify exactly what changed using terraform plan and AWS information. Then I would determine whether the manual change was intentional. If it is intended, I would update the Terraform configuration and commit the change through the normal review process. If it was unauthorized or accidental, I would restore the infrastructure to the Terraform-defined configuration. I would also document the incident and address the process that allowed the manual change."

---

# 127. 7-Year-Level Scenario — Terraform Wants Replacement

### Interviewer:

> "Terraform wants to destroy and recreate a production EC2 instance. What do you do?"

### Answer:

> "I would not immediately apply. I would inspect the plan and identify the exact attribute causing replacement. I would check whether the change is expected, whether downtime is acceptable, and whether a safer migration strategy is required. If the replacement is valid, I would follow the production approval process and execute it during an appropriate window."

---

# 128. 7-Year-Level Scenario — State Lock

### Interviewer:

> "Terraform says the state is locked. What do you do?"

### Answer:

> "First I would verify whether another Terraform operation is actually running. I would never force-unlock blindly. If the lock is stale and I have confirmed no active Terraform operation exists, I would follow the organization's state recovery process and use force-unlock only when appropriate."

---

# 129. 7-Year-Level Scenario — Two Engineers Apply

### Interviewer:

> "Two engineers run terraform apply at the same time. What protects the infrastructure?"

### Answer:

> "A properly configured remote backend with state locking helps prevent concurrent state modifications. The second operation should wait or fail according to the backend behavior rather than modifying the same state concurrently."

---

# 130. 7-Year-Level Scenario — AWS Console Change

### Interviewer:

> "Should DevOps engineers never use the AWS Console?"

### Answer:

> "The console can still be useful for investigation, monitoring and emergency troubleshooting. However, infrastructure changes should normally be performed through Terraform in an IaC-managed environment so that the desired state remains version-controlled and auditable."

---

# 131. Project-Level Enterprise Flow

This project can evolve into:

```text
GitHub
   |
   v
Jenkins
   |
   +--> Terraform Init
   |
   +--> Terraform Validate
   |
   +--> Security Scan
   |
   +--> Terraform Plan
   |
   v
Approval
   |
   v
Terraform Apply
   |
   v
AWS
   |
   +--> VPC
   |
   +--> Security Groups
   |
   +--> Jenkins EC2
   |
   +--> Future Infrastructure
```

---

# 132. Complete Terraform Lifecycle

```text
Write Code
    |
    v
terraform fmt
    |
    v
terraform init
    |
    v
terraform validate
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
AWS Infrastructure
    |
    v
Monitor
    |
    v
Detect Drift
    |
    v
Modify Code
    |
    v
Plan
    |
    v
Apply
```

---

# 133. Enterprise Traceability

Infrastructure changes should be traceable:

```text
Jira Ticket
     |
     v
Git Branch
     |
     v
Terraform Commit
     |
     v
Pull Request
     |
     v
Terraform Plan
     |
     v
Approval
     |
     v
Terraform Apply
     |
     v
AWS Infrastructure
```

This provides auditability.

---

# 134. Final Project Summary

This Terraform project demonstrates:

```text
Infrastructure as Code
Terraform Providers
Terraform Variables
Terraform Outputs
Terraform Modules
VPC Module
EC2 Provisioning
Security Groups
Terraform State
Terraform Plan
Terraform Apply
Terraform Import
Terraform Drift
Terraform Dependencies
Terraform Git Workflow
Terraform CI/CD
Terraform Security
Terraform Troubleshooting
Terraform Rollback
Enterprise Best Practices
7-Year-Level Interview Preparation
```

---

# 135. Final Interview Answer

If an interviewer asks:

> "Explain your Terraform project."

A strong answer is:

> "In my project, I use Terraform to provision and manage AWS infrastructure as Infrastructure as Code. The Terraform root module contains the environment-level configuration and calls a reusable VPC child module. The VPC module encapsulates networking resources and exposes required values through outputs. I also manage Jenkins infrastructure using separate Terraform configuration files for the EC2 instance, security group and variables.
>
> I follow the standard Terraform lifecycle of init, fmt, validate, plan and apply. Before applying changes, I review the Terraform plan to understand creates, updates and replacements. I also understand Terraform state, remote backend concepts, state locking, drift detection and importing existing AWS resources.
>
> For enterprise usage, I would store state in a protected remote backend, use least-privilege AWS identity, keep provider versions controlled, avoid hardcoded secrets, run Terraform validation and security scanning through Jenkins, generate a plan during CI, and require approval before production apply.
>
> If infrastructure changes outside Terraform, I use terraform plan to identify drift and then either update the Terraform code if the change is intentional or reconcile the infrastructure back to the desired state. For rollback, I normally restore the previous Terraform configuration through Git and apply the resulting plan rather than treating terraform destroy as a rollback mechanism."

---

# 136. Quick Revision Sheet

Remember this flow:

```text
CODE
 |
 v
INIT
 |
 v
FMT
 |
 v
VALIDATE
 |
 v
PLAN
 |
 v
REVIEW
 |
 v
APPLY
 |
 v
AWS
```

Remember Terraform architecture:

```text
ROOT MODULE
     |
     v
CHILD MODULE
     |
     v
RESOURCE
     |
     v
AWS
```

Remember state:

```text
Terraform Code
      |
      v
Terraform State
      |
      v
AWS Infrastructure
```

Remember drift:

```text
Terraform Code
      |
      X
AWS changed manually
      |
      v
DRIFT
      |
      v
terraform plan
```

Remember CI/CD:

```text
Git
 |
 v
Jenkins
 |
 +--> fmt
 +--> init
 +--> validate
 +--> security scan
 +--> plan
 |
 v
Approval
 |
 v
apply
 |
 v
AWS
```

---

# 137. Final Terraform Best-Practice Rule

The most important principle is:

```text
DO NOT THINK:

"I created AWS infrastructure."

THINK:

"I defined the desired infrastructure as code,
stored it in Git,
validated it,
planned the change,
reviewed it,
applied it,
and can reproduce or recover it."
```

That is the difference between simply knowing Terraform commands and working with Terraform at an enterprise DevOps level.

---

# 138. Project Completion Checklist

```text
[x] Terraform project initialized
[x] AWS provider configured
[x] Provider version controlled
[x] Root module configured
[x] VPC child module configured
[x] Module variables configured
[x] Module outputs configured
[x] Jenkins EC2 configuration
[x] Jenkins security group
[x] Terraform variables
[x] Terraform plan
[x] Terraform state concepts
[x] Terraform import concepts
[x] Terraform drift concepts
[x] Terraform Git workflow
[x] Terraform CI/CD architecture
[x] Terraform security
[x] Terraform troubleshooting
[x] Terraform RCA
[x] Terraform rollback
[x] Enterprise best practices
[x] Interview preparation
```

---

# 139. One-Line Interview Summary

> **"Terraform allows me to define AWS infrastructure as version-controlled, reusable, reviewable and repeatable code, while Terraform state maintains the relationship between that configuration and the real infrastructure."**

---

# END OF TERRAFORM ENTERPRISE BIBLE
`
**Primary Technology:** Terraform
**Infrastructure:** AWS
**Purpose:** Infrastructure as Code (IaC)
**Project Level:** Enterprise / DevOps / Interview Preparation

---

# 1. Project Overview

This repository contains Terraform code used to provision AWS infrastructure using **Infrastructure as Code (IaC)**.

Instead of manually creating AWS resources through the AWS Console, Terraform allows us to define infrastructure using `.tf` files.

Terraform reads the configuration, compares the desired infrastructure with the current infrastructure, creates an execution plan, and applies the required changes.

The project is structured using a reusable **VPC child module** and root-level Terraform configuration.

---

# 2. Business Requirement

In an enterprise DevOps environment, infrastructure should not depend on manual AWS Console operations.

The organization needs:

* Repeatable infrastructure
* Version-controlled infrastructure
* Reusable modules
* Consistent environments
* Reviewable infrastructure changes
* Infrastructure drift detection
* Automated deployments
* Easy rollback
* CI/CD integration
* Separate development and production environments

Terraform provides these capabilities.

---

# 3. Why Business Needs Terraform

Imagine an organization needs the following infrastructure:

```text
VPC
 ├── Public Subnet
 ├── Private Subnet
 ├── Internet Gateway
 ├── Route Tables
 ├── Security Groups
 └── EC2 Instances
```

Creating everything manually through AWS Console is time-consuming and error-prone.

With Terraform:

```text
Terraform Code
      |
      v
terraform plan
      |
      v
Review Changes
      |
      v
terraform apply
      |
      v
AWS Infrastructure
```

The same Terraform code can be used to create consistent infrastructure across environments.

---

# 4. Real-Time Enterprise Scenario

Suppose a company has three environments:

```text
Development
     |
     v
QA
     |
     v
Production
```

Each environment requires:

* VPC
* Subnets
* Security Groups
* EC2
* Load Balancer
* Database
* Monitoring

Without IaC:

```text
Engineer manually creates infrastructure
             |
             v
Different configuration
             |
             v
Environment inconsistency
             |
             v
Production problems
```

With Terraform:

```text
Git Repository
      |
      v
Terraform Code
      |
      v
terraform plan
      |
      v
Code Review
      |
      v
terraform apply
      |
      v
AWS
```

Infrastructure becomes repeatable and auditable.

---

# 5. Infrastructure Architecture

```text
                    Git Repository
                          |
                          v
                  Terraform Configuration
                          |
              +-----------+-----------+
              |                       |
              v                       v
        Root Terraform           VPC Module
        Configuration             Child Module
              |                       |
              +-----------+-----------+
                          |
                          v
                    AWS Provider
                          |
                          v
                       AWS
                          |
              +-----------+-----------+
              |                       |
              v                       v
             VPC                    Jenkins
              |                     EC2
       +------+------+                |
       |             |                |
   Subnets       Networking           |
                                      |
                                      v
                              Jenkins CI Server
```

---

# 6. Terraform Execution Flow

The enterprise Terraform workflow is:

```text
Developer
   |
   v
Modify Terraform Code
   |
   v
git diff
   |
   v
terraform fmt
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Code Review
   |
   v
terraform apply
   |
   v
AWS Infrastructure
```

---

# 7. Repository Structure

Current project structure:

```text
terraform-project/
│
└── terraform/
    │
    ├── .gitignore
    ├── .terraform.lock.hcl
    │
    ├── main.tf
    ├── providers.tf
    ├── versions.tf
    ├── variables.tf
    │
    ├── jenkins-ec2.tf
    ├── jenkins-security-group.tf
    ├── jenkins-variables.tf
    │
    ├── tfplan
    │
    └── modules/
        │
        └── vpc/
            ├── main.tf
            ├── variables.tf
            └── outputs.tf
```

---

# 8. Root Module vs Child Module

This is one of the most important Terraform interview concepts.

## Root Module

The directory from which Terraform commands are executed is the **root module**.

In this project:

```text
terraform/
```

is the root module.

It contains:

```text
main.tf
providers.tf
versions.tf
variables.tf
jenkins-ec2.tf
jenkins-security-group.tf
```

---

# 9. Child Module

The reusable VPC module is:

```text
terraform/modules/vpc/
```

It contains:

```text
main.tf
variables.tf
outputs.tf
```

The root module calls this child module.

Conceptually:

```text
Root Module
     |
     | calls
     v
VPC Child Module
     |
     v
Creates VPC Infrastructure
```

---

# 10. Parent and Child Module Relationship

Terraform terminology can be confusing.

In this project:

```text
terraform/
    |
    | module "vpc"
    v
modules/vpc/
```

The root module is the **calling module**.

The VPC module is the **called child module**.

You can explain it in an interview like this:

> "The root module contains the environment-level Terraform configuration, and it calls reusable child modules such as the VPC module. The child module contains the reusable resource definitions, variables, and outputs."

---

# 11. `versions.tf`

Purpose:

```text
Terraform and provider version requirements
```

Typical configuration:

```hcl
terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}
```

The purpose is to control Terraform and provider compatibility.

---

# 12. Why Provider Version Matters

Suppose today:

```text
AWS Provider = 6.x
```

Tomorrow a new provider version introduces a breaking change.

Without version constraints, the infrastructure build may behave differently.

Therefore enterprise Terraform projects pin or constrain provider versions.

---

# 13. `.terraform.lock.hcl`

This file records the selected provider version and checksums.

Example:

```text
terraform-provider-aws
```

Terraform uses the lock file to ensure consistent provider installation.

Best practice:

```text
Commit .terraform.lock.hcl
```

Do not normally delete it from Git.

---

# 14. `providers.tf`

The AWS provider tells Terraform how to communicate with AWS.

Example:

```hcl
provider "aws" {
  region = var.aws_region
}
```

Flow:

```text
Terraform
    |
    v
AWS Provider
    |
    v
AWS APIs
    |
    v
AWS Resources
```

---

# 15. Authentication

Terraform should not normally contain AWS access keys directly inside `.tf` files.

Preferred approaches include:

```text
AWS CLI Profile
Environment Variables
IAM Role
EC2 Instance Profile
OIDC
CI/CD Identity
```

For Jenkins:

```text
Jenkins
   |
   v
IAM Role / OIDC
   |
   v
AWS APIs
```

Never commit:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
```

into Git.

---

# 16. `variables.tf`

Variables make Terraform configuration reusable.

Example:

```hcl
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-south-1"
}
```

Instead of hardcoding:

```hcl
region = "ap-south-1"
```

we use:

```hcl
region = var.aws_region
```

---

# 17. Why Variables Are Important

Without variables:

```text
Development
    |
    | hardcoded values
    v
Production
    |
    | copy/paste
    v
Different configuration
```

With variables:

```text
Same Terraform Code
       |
       +---- dev values
       |
       +---- QA values
       |
       +---- prod values
```

This improves reusability.

---

# 18. Variable Flow

```text
variables.tf
     |
     v
Variable Definition
     |
     v
Variable Value
     |
     v
Terraform Resource
```

Possible value sources:

```text
Default
terraform.tfvars
*.auto.tfvars
-var
Environment Variable
CI/CD Pipeline
```

---

# 19. `main.tf`

`main.tf` generally contains the main infrastructure configuration or module calls.

For example:

```hcl
module "vpc" {
  source = "./modules/vpc"

  ...
}
```

This means:

```text
Root main.tf
     |
     v
modules/vpc/main.tf
```

---

# 20. VPC Module

The VPC module is responsible for networking infrastructure.

Conceptually:

```text
VPC Module
    |
    +-- VPC
    |
    +-- Subnets
    |
    +-- Internet Gateway
    |
    +-- Route Tables
    |
    +-- Associations
```

The main benefit is reuse.

---

# 21. VPC Module `main.tf`

A VPC resource typically looks like:

```hcl
resource "aws_vpc" "this" {
  cidr_block = var.vpc_cidr

  tags = {
    Name = var.vpc_name
  }
}
```

Terraform understands:

```text
resource "aws_vpc" "this"
```

as an AWS VPC resource managed by Terraform.

---

# 22. VPC Module `variables.tf`

The module receives values from the root module.

Example:

```hcl
variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
}
```

This creates a contract between:

```text
Root Module
     |
     | input
     v
Child Module
```

---

# 23. VPC Module `outputs.tf`

Outputs expose values from a child module.

Example:

```hcl
output "vpc_id" {
  value = aws_vpc.this.id
}
```

Flow:

```text
VPC Resource
     |
     v
Module Output
     |
     v
Root Module
```

Outputs are especially useful when one module depends on another.

---

# 24. Module Input and Output

Think of a module like a function.

```text
Input
  |
  v
+----------------+
|   VPC MODULE   |
+----------------+
  |
  v
Output
```

Example:

```text
Input:
vpc_cidr = 10.0.0.0/16

Output:
vpc_id
```

---

# 25. Jenkins EC2 Infrastructure

This project also contains Terraform configuration for Jenkins infrastructure.

Typical flow:

```text
Terraform
    |
    +---- Security Group
    |
    +---- EC2 Instance
    |
    +---- Jenkins Server
```

The Jenkins EC2 configuration is separated into:

```text
jenkins-ec2.tf
jenkins-security-group.tf
jenkins-variables.tf
```

This improves readability.

---

# 26. Why Split Terraform Files?

Terraform does not require one resource per file.

Terraform loads all `.tf` files in the same directory.

Therefore:

```text
main.tf
jenkins-ec2.tf
jenkins-security-group.tf
variables.tf
```

are logically one root module.

The split is for human organization.

Terraform sees:

```text
All .tf files
      |
      v
One Root Module
```

---

# 27. Important Interview Question

### Does Terraform execute `main.tf` first?

**No.**

Terraform loads all `.tf` files in the working directory.

The filenames are primarily for organization.

For example:

```text
main.tf
network.tf
ec2.tf
security.tf
outputs.tf
```

are all part of the same module.

Terraform builds a dependency graph from the configuration.

---

# 28. Terraform Dependency Graph

Suppose:

```text
VPC
 |
 v
Subnet
 |
 v
EC2
 |
 v
Application
```

Terraform understands dependencies.

Example:

```text
VPC
 |
 +--> Subnet
       |
       +--> EC2
```

Terraform creates resources in dependency order when dependencies are known.

---

# 29. Explicit Dependency

Sometimes we use:

```hcl
depends_on = [
  aws_vpc.this
]
```

This explicitly tells Terraform:

```text
Create A before B
```

However, implicit dependencies are preferred when Terraform can infer them naturally.

---

# 30. Terraform State

Terraform state is one of the most important concepts.

Terraform needs to remember:

```text
What Terraform manages
What resources exist
Resource IDs
Resource attributes
Dependencies
```

The default local state file is:

```text
terraform.tfstate
```

---

# 31. Desired vs Actual vs State

There are three important things:

```text
Terraform Code
     |
     | desired state
     v

Terraform State
     |
     | Terraform's recorded knowledge
     v

AWS Infrastructure
     |
     | actual state
     v
```

Terraform compares these to determine required actions.

---

# 32. Terraform State Example

Suppose Terraform created:

```text
EC2 Instance
```

AWS gives it:

```text
i-0123456789
```

Terraform stores information about the managed resource in state.

Conceptually:

```text
Terraform
    |
    v
terraform.tfstate
    |
    v
aws_instance.jenkins
    |
    v
i-0123456789
```

---

# 33. Why State Is Critical

If state is lost:

```text
Terraform
    |
    X
Cannot accurately map configuration
to existing infrastructure
```

Therefore enterprise teams protect state carefully.

---

# 34. Local State vs Remote State

Local:

```text
terraform.tfstate
```

Enterprise:

```text
Terraform
    |
    v
Remote Backend
    |
    v
S3
    +
Locking mechanism
```

Remote state allows teams and CI/CD systems to work with shared infrastructure state.

---

# 35. State Locking

Imagine:

```text
Developer A
    |
terraform apply
    |
    v
State locked

Developer B
    |
terraform apply
    |
    X
```

Locking prevents concurrent state modifications.

Without locking:

```text
Engineer A ----+
               |
               v
            State
               ^
               |
Engineer B ----+
```

This can cause corruption or conflicting changes.

---

# 36. Terraform Backend

A common AWS enterprise architecture is:

```text
Terraform
    |
    v
S3 Backend
    |
    +-- State Storage
    |
    +-- Versioning
    |
    +-- Encryption
```

State should be protected because it may contain sensitive infrastructure information.

---

# 37. `terraform init`

First command after cloning a Terraform project:

```bash
terraform init
```

Purpose:

* Initialize Terraform
* Download providers
* Initialize modules
* Initialize backend
* Prepare working directory

Expected concept:

```text
Initializing provider plugins...
Terraform has been successfully initialized!
```

---

# 38. `.terraform` Directory

After:

```bash
terraform init
```

Terraform may create:

```text
.terraform/
```

This directory contains Terraform's local working data and downloaded provider/module components.

Normally it should not be committed to Git.

---

# 39. `.gitignore`

Typical entries:

```gitignore
.terraform/
*.tfstate
*.tfstate.*
*.tfplan
crash.log
*.tfvars
```

But be careful with:

```text
*.tfvars
```

because some teams intentionally commit non-secret environment variable files.

The important rule is:

```text
Never commit secrets.
```

---

# 40. Terraform Format

Run:

```bash
terraform fmt
```

Purpose:

```text
Standardize Terraform formatting
```

Example:

```text
Before:
resource "aws_instance" "jenkins"{ami=var.ami}

After:
resource "aws_instance" "jenkins" {
  ami = var.ami
}
```

---

# 41. Terraform Validate

Run:

```bash
terraform validate
```

Purpose:

```text
Check Terraform configuration syntax
and internal consistency
```

Expected:

```text
Success! The configuration is valid.
```

Important:

`terraform validate` does not mean the infrastructure will definitely work in AWS.

---

# 42. Terraform Plan

Run:

```bash
terraform plan
```

Terraform calculates what it wants to change.

Example:

```text
+ create
~ update
- destroy
-/+ replace
```

Example:

```text
Plan: 3 to add, 0 to change, 0 to destroy.
```

---

# 43. Meaning of Plan Symbols

```text
+       Create

~       Update in-place

-       Destroy

-/+     Destroy and recreate

<=      Read data
```

The `-/+` case is especially important.

It means Terraform cannot safely update the resource in place and must replace it.

---

# 44. Terraform Apply

Run:

```bash
terraform apply
```

Terraform asks for confirmation.

Example:

```text
Do you want to perform these actions?
Only 'yes' will be accepted to approve.
```

Then:

```text
yes
```

Terraform creates or modifies the infrastructure.

---

# 45. Plan File

A safer enterprise workflow is:

```bash
terraform plan -out=tfplan
```

Then:

```bash
terraform apply tfplan
```

Flow:

```text
terraform plan
      |
      v
Saved Plan
      |
      v
Review
      |
      v
terraform apply tfplan
```

This ensures the reviewed plan is the plan being applied.

---

# 46. Terraform Destroy

Command:

```bash
terraform destroy
```

This removes resources managed by Terraform.

Example:

```text
Plan: 0 to add, 0 to change, 5 to destroy.
```

Use extreme caution in production.

---

# 47. Terraform Refresh Concept

Modern Terraform workflows generally refresh state during planning operations.

You can inspect current infrastructure with:

```bash
terraform plan
```

If infrastructure was changed manually in AWS, Terraform may detect differences.

This is called:

```text
Drift
```

---

# 48. Terraform Drift

Suppose Terraform created:

```text
EC2
Instance Type = t3.micro
```

Someone manually changes it in AWS:

```text
t3.medium
```

Now:

```text
Terraform Code
     |
     | t3.micro
     v
AWS
     |
     | t3.medium
```

Terraform detects the difference.

That difference is infrastructure drift.

---

# 49. Drift Detection Workflow

```text
Terraform Code
      |
      v
terraform plan
      |
      v
Compare State / AWS
      |
      v
Drift Detected
      |
      v
Investigate
      |
      +------+
      |      |
      v      v
Code Fix   Manual Change
      |      |
      +------+
          |
          v
terraform plan
```

---

# 50. Terraform Import

Suppose an EC2 instance already exists in AWS:

```text
Created manually
```

But Terraform does not manage it.

You can import it.

Modern Terraform supports configuration-driven import, while the traditional command is:

```bash
terraform import aws_instance.example i-0123456789
```

Important:

Import brings the resource into Terraform state, but you still need matching Terraform configuration.

---

# 51. Import Flow

```text
Existing AWS Resource
        |
        v
terraform import
        |
        v
Terraform State
        |
        v
Write matching .tf configuration
        |
        v
terraform plan
```

---

# 52. Import Does Not Automatically Write Perfect Configuration

Interview answer:

> "Terraform import associates an existing infrastructure resource with a Terraform resource address in state. It does not by itself produce a complete, production-ready Terraform configuration. I still need to define the resource configuration and validate it with plan."

---

# 53. Terraform Resource Address

Example:

```text
aws_instance.jenkins
```

For a module:

```text
module.vpc.aws_vpc.this
```

The address identifies the Terraform-managed object.

---

# 54. Terraform State Commands

Useful commands:

```bash
terraform state list
```

Shows resources tracked in state.

```bash
terraform state show aws_instance.jenkins
```

Shows detailed state information.

```bash
terraform state pull
```

Retrieves the current state.

---

# 55. Terraform Outputs

Run:

```bash
terraform output
```

Specific output:

```bash
terraform output vpc_id
```

Useful for:

```text
VPC ID
Subnet ID
EC2 IP
Load Balancer DNS
```

---

# 56. Terraform Console

Command:

```bash
terraform console
```

Useful for testing expressions.

Example:

```text
var.aws_region
```

This helps debug Terraform expressions.

---

# 57. Terraform Data Sources

Resources create infrastructure.

Data sources read existing information.

Example concept:

```text
Resource:
Create EC2

Data Source:
Find AMI
```

Example:

```hcl
data "aws_ami" "ubuntu" {
  ...
}
```

Flow:

```text
AWS
 |
 v
Data Source
 |
 v
Terraform
 |
 v
Resource
```

---

# 58. Resource vs Data Source

### Resource

```text
Creates/manages infrastructure
```

### Data Source

```text
Reads existing information
```

Interview answer:

> "Resources manage infrastructure, while data sources allow Terraform to query existing information that can be used by resources or other configuration."

---

# 59. Terraform Modules — Enterprise Purpose

Modules prevent repeated infrastructure code.

Without modules:

```text
Dev VPC code
QA VPC code
Prod VPC code
```

Large duplication.

With module:

```text
Reusable VPC Module
       |
       +---- Dev
       |
       +---- QA
       |
       +---- Prod
```

---

# 60. Module Reusability

Example:

```hcl
module "vpc" {
  source = "./modules/vpc"

  vpc_cidr = "10.0.0.0/16"
}
```

Another environment:

```hcl
module "vpc" {
  source = "./modules/vpc"

  vpc_cidr = "10.1.0.0/16"
}
```

Same module.

Different inputs.

---

# 61. Terraform Module Design

A good module generally contains:

```text
module/
├── main.tf
├── variables.tf
└── outputs.tf
```

Optional:

```text
README.md
versions.tf
data.tf
locals.tf
```

---

# 62. Module Best Practices

Good modules should:

* Have clear inputs
* Have useful outputs
* Avoid unnecessary hardcoding
* Use meaningful names
* Have documentation
* Be reusable
* Have predictable behavior
* Avoid hidden dependencies

---

# 63. Terraform `count`

`count` creates multiple instances based on a number.

Example:

```hcl
resource "aws_instance" "server" {
  count = 2

  ...
}
```

Terraform addresses them as:

```text
aws_instance.server[0]
aws_instance.server[1]
```

---

# 64. Terraform `for_each`

`for_each` creates resources from a set or map.

Example:

```hcl
resource "aws_instance" "server" {
  for_each = {
    app1 = "t3.micro"
    app2 = "t3.small"
  }

  instance_type = each.value
}
```

Addresses:

```text
aws_instance.server["app1"]
aws_instance.server["app2"]
```

---

# 65. Count vs For Each

### count

Best when:

```text
Instances are identical
```

### for_each

Best when:

```text
Each instance has a meaningful key/value
```

Interview explanation:

> "I use count when the instances are essentially identical and number-based indexing is sufficient. I prefer for_each when resources have meaningful identities or different configuration values, because the map keys provide more stable addresses."

---

# 66. Terraform Locals

Locals allow reusable expressions.

Example:

```hcl
locals {
  common_tags = {
    Project     = "Terraform"
    Environment = "dev"
  }
}
```

Then:

```hcl
tags = local.common_tags
```

---

# 67. Terraform Outputs and Module Dependencies

Example:

```text
VPC Module
    |
    | vpc_id
    v
EC2 Module
```

The VPC module outputs:

```text
vpc_id
```

The EC2 configuration consumes:

```text
module.vpc.vpc_id
```

This creates a dependency.

---

# 68. EC2 in Different Availability Zones

Enterprise interview scenario:

```text
VPC
 |
 +--- AZ-a
 |     |
 |     +--- Subnet A
 |
 +--- AZ-b
       |
       +--- Subnet B
```

EC2 instances can be placed in different AZs for higher availability.

Example concept:

```text
EC2-1 -> subnet-a -> AZ-a
EC2-2 -> subnet-b -> AZ-b
```

---

# 69. Specific Subnet Requirement

Interview question:

> "How would you launch an EC2 instance in a specific subnet?"

Answer:

Use the subnet ID:

```hcl
subnet_id = aws_subnet.private_a.id
```

or:

```hcl
subnet_id = var.subnet_id
```

The subnet determines the Availability Zone.

---

# 70. Terraform Security Groups

Security groups should be defined as code.

Example concept:

```text
Security Group
 |
 +--- SSH 22
 |
 +--- HTTP 80
 |
 +--- HTTPS 443
```

Avoid:

```text
0.0.0.0/0
```

for sensitive ports unless there is a justified requirement.

---

# 71. Jenkins Security Group

For a Jenkins server, common ports may include:

```text
22     SSH
8080   Jenkins
```

Production access should preferably be restricted through:

```text
VPN
Bastion
SSM
Private networking
Trusted CIDR
Load balancer
```

rather than exposing administrative ports globally.

---

# 72. Infrastructure Tags

Enterprise infrastructure should use consistent tags.

Example:

```hcl
tags = {
  Project     = "ShopSphere"
  Environment = "dev"
  ManagedBy   = "Terraform"
  Owner       = "DevOps"
}
```

Tags help with:

* Cost tracking
* Ownership
* Automation
* Inventory
* Auditing

---

# 73. Terraform Naming Convention

Use meaningful resource names.

Good:

```text
aws_instance.jenkins
aws_security_group.jenkins
module.vpc
```

Avoid:

```text
aws_instance.test1
aws_instance.final
aws_instance.new
```

Infrastructure names should explain their purpose.

---

# 74. Terraform Workflow in Git

Enterprise workflow:

```text
Developer
   |
   v
Create Feature Branch
   |
   v
Modify Terraform
   |
   v
terraform fmt
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Commit
   |
   v
Pull Request
   |
   v
Review
   |
   v
CI Validation
   |
   v
Apply
```

---

# 75. Recommended Git Workflow

```bash
git switch develop
git pull origin develop

git switch -c feature/terraform-networking

terraform fmt
terraform validate
terraform plan

git status
git diff

git add terraform/
git commit -m "Add Terraform networking infrastructure"

git push -u origin feature/terraform-networking
```

---

# 76. Terraform CI/CD

A Jenkins pipeline can automate Terraform validation.

Typical pipeline:

```text
Git Push
   |
   v
Jenkins
   |
   +--> terraform fmt -check
   |
   +--> terraform init
   |
   +--> terraform validate
   |
   +--> terraform plan
   |
   v
Approval
   |
   v
terraform apply
```

---

# 77. Enterprise Terraform Pipeline

A stronger pipeline:

```text
Checkout
   |
   v
Terraform Format Check
   |
   v
Terraform Init
   |
   v
Terraform Validate
   |
   v
Security Scan
   |
   v
Terraform Plan
   |
   v
Manual Approval
   |
   v
Terraform Apply
   |
   v
Validation
```

Possible security tools:

```text
Trivy
Checkov
tfsec
TFLint
```

---

# 78. Terraform Plan in CI

CI should generate a plan.

Example:

```bash
terraform plan -out=tfplan
```

The plan should be reviewed before production deployment.

This provides infrastructure change visibility.

---

# 79. Terraform Apply in Production

A common enterprise model:

```text
Developer
    |
    v
Pull Request
    |
    v
Terraform Plan
    |
    v
Review
    |
    v
Approval
    |
    v
Terraform Apply
```

Production infrastructure should not normally be changed manually.

---

# 80. Infrastructure Drift Policy

If someone manually changes AWS:

```text
AWS Console
    |
    v
Manual Change
    |
    v
Drift
```

The team should decide whether:

```text
Option 1:
Revert the manual change
```

or:

```text
Option 2:
Update Terraform code
```

Terraform should remain the source of truth.

---

# 81. Terraform Rollback

Terraform rollback is different from application rollback.

Application:

```text
Version 2
   |
   v
Rollback
   |
   v
Version 1
```

Terraform:

```text
Current Configuration
       |
       v
Previous Git Commit
       |
       v
terraform plan
       |
       v
terraform apply
```

The desired infrastructure configuration is changed back.

---

# 82. Terraform Rollback Example

Suppose:

```text
Before:
instance_type = "t3.micro"
```

Changed to:

```text
instance_type = "t3.medium"
```

To roll back:

```text
Git checkout previous version
       |
       v
terraform plan
       |
       v
Review
       |
       v
terraform apply
```

---

# 83. Important Rollback Warning

Do not blindly run:

```bash
terraform destroy
```

as a rollback mechanism.

Terraform rollback normally means:

```text
Restore desired configuration
```

then:

```text
terraform plan
terraform apply
```

---

# 84. Terraform State Rollback

State rollback is a different operation.

If the state becomes corrupted or an incorrect state change occurs, restore from a protected backend/versioned state backup according to the organization's recovery process.

Do not manually edit:

```text
terraform.tfstate
```

unless you fully understand the implications.

---

# 85. Terraform State Commands — Dangerous Operations

Commands such as:

```bash
terraform state rm
terraform state mv
terraform state replace-provider
```

can change Terraform's state relationships.

Use them carefully.

They do not automatically change AWS infrastructure.

---

# 86. `terraform state rm`

Example:

```bash
terraform state rm aws_instance.jenkins
```

This removes the resource from Terraform state.

It does **not** normally destroy the AWS instance.

After this:

```text
AWS Instance
    |
    v
Still exists

Terraform
    |
    X
No longer managing it
```

---

# 87. Terraform Destroy vs State RM

Very important interview comparison:

```text
terraform destroy
```

means:

```text
Destroy managed infrastructure
```

Whereas:

```text
terraform state rm
```

means:

```text
Remove resource from Terraform state
```

The AWS resource may continue to exist after `state rm`.

---

# 88. Terraform Taint / Replacement Concept

If a resource needs replacement, Terraform may show:

```text
-/+ destroy and create replacement
```

Modern Terraform workflows generally use:

```bash
terraform apply -replace="aws_instance.jenkins"
```

rather than older taint-based workflows.

---

# 89. Terraform Troubleshooting Method

Use this enterprise RCA flow:

```text
Problem
   |
   v
terraform validate
   |
   v
terraform plan
   |
   v
Terraform logs
   |
   v
AWS resource status
   |
   v
IAM permissions
   |
   v
Networking
   |
   v
State
   |
   v
Provider
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Plan
   |
   v
Apply
   |
   v
Validate
```

---

# 90. Common Problem — Provider Error

Symptom:

```text
Failed to load plugin
```

Check:

```bash
terraform version
terraform providers
terraform init
```

Possible causes:

* Provider version mismatch
* Corrupted provider download
* Lock file issue
* Network issue

---

# 91. Common Problem — Authentication Failure

Example:

```text
UnauthorizedOperation
AccessDenied
```

Check:

```bash
aws sts get-caller-identity
```

Then verify:

```text
AWS identity
IAM permissions
AWS region
Credentials
Role
```

---

# 92. Common Problem — Wrong Region

Terraform:

```text
ap-south-1
```

But the resource was expected in:

```text
us-east-1
```

Check:

```hcl
provider "aws" {
  region = var.aws_region
}
```

Then:

```bash
terraform plan
```

---

# 93. Common Problem — Resource Already Exists

Terraform may return:

```text
already exists
```

Possible reason:

```text
Resource was created manually
```

Solution:

```text
Option 1:
Delete it if it is not required

Option 2:
Import it into Terraform
```

Do not create duplicate resources blindly.

---

# 94. Common Problem — Dependency Error

Example:

```text
InvalidSubnetID.NotFound
```

Possible reason:

```text
Wrong subnet ID
Wrong region
Resource does not exist
```

Check:

```bash
terraform state list
terraform output
aws ec2 describe-subnets
```

---

# 95. Common Problem — Terraform Wants to Destroy Unexpectedly

Never immediately run:

```bash
terraform apply
```

First inspect:

```bash
terraform plan
```

Understand why Terraform wants:

```text
destroy
```

or:

```text
-/+ replace
```

Possible reasons:

* Configuration change
* Immutable attribute
* Resource renamed
* State mismatch
* Provider behavior
* Drift

---

# 96. `terraform plan` Is Your Safety Net

Before applying:

```bash
terraform plan
```

Always understand:

```text
What will be created?
What will change?
What will be destroyed?
```

Enterprise principle:

```text
Never blindly apply infrastructure changes.
```

---

# 97. Sensitive Variables

Never hardcode:

```hcl
password = "MyPassword123"
```

Prefer:

```hcl
variable "db_password" {
  sensitive = true
}
```

And provide the value through a secure mechanism.

---

# 98. Secret Management

Terraform should integrate with enterprise secret management such as:

```text
AWS Secrets Manager
AWS SSM Parameter Store
Vault
CI/CD secret store
```

Terraform state can still contain sensitive values depending on the resource/provider, so protecting state is essential.

---

# 99. Terraform Security Best Practices

Follow:

```text
Least privilege IAM
No hardcoded credentials
Encrypted state
Remote backend
State locking
Provider pinning
Module versioning
Security scanning
PR review
Sensitive variables
Restricted security groups
Consistent tagging
```

---

# 100. Terraform Performance

Large Terraform projects can become slow.

Improve performance through:

* Modular architecture
* Appropriate resource grouping
* Remote state
* Avoid unnecessary data sources
* Avoid excessive dependencies
* Separate independent stacks where appropriate

Do not create one massive Terraform state for every infrastructure component without considering operational boundaries.

---

# 101. Terraform Workspace

Terraform workspaces allow multiple state instances for the same configuration.

Concept:

```text
Same Code
   |
   +---- workspace dev
   |
   +---- workspace qa
   |
   +---- workspace prod
```

However, many enterprises prefer separate root configurations/directories or separate state boundaries for major environments because isolation and operational clarity can be stronger.

---

# 102. Terraform Environment Strategy

Example:

```text
terraform/
├── modules/
│   ├── vpc/
│   └── ec2/
│
└── environments/
    ├── dev/
    ├── qa/
    └── prod/
```

Each environment can have its own:

```text
Backend
Variables
State
Provider configuration
```

---

# 103. Terraform Enterprise Architecture

A mature structure may look like:

```text
terraform/
│
├── modules/
│   ├── vpc/
│   ├── security-group/
│   ├── ec2/
│   ├── alb/
│   └── rds/
│
└── environments/
    ├── dev/
    ├── qa/
    └── prod/
```

Flow:

```text
Reusable Modules
       |
       v
Environment Configuration
       |
       v
Remote State
       |
       v
AWS
```

---

# 104. Build Once, Reuse Many

Terraform modules should follow a similar principle to application engineering:

```text
Reusable Module
      |
      +---- Dev
      |
      +---- QA
      |
      +---- Production
```

Only environment-specific values should change where possible.

---

# 105. Terraform and GitOps

Terraform infrastructure can be managed using GitOps principles.

```text
Git
 |
 v
Terraform Configuration
 |
 v
Pull Request
 |
 v
Plan
 |
 v
Approval
 |
 v
Apply
 |
 v
AWS
```

Git becomes the auditable source of desired infrastructure configuration.

---

# 106. Terraform + Jenkins

Example enterprise flow:

```text
Developer
   |
   v
Git Push
   |
   v
Jenkins
   |
   +--> Checkout
   |
   +--> terraform fmt
   |
   +--> terraform init
   |
   +--> terraform validate
   |
   +--> Security Scan
   |
   +--> terraform plan
   |
   v
Manual Approval
   |
   v
terraform apply
   |
   v
AWS
```

---

# 107. Terraform + AWS Architecture

Typical DevOps infrastructure:

```text
                    Terraform
                        |
              +---------+---------+
              |                   |
              v                   v
             VPC                 IAM
              |
       +------+------+
       |             |
       v             v
    Public        Private
    Subnets       Subnets
       |             |
       v             v
      ALB          EC2/EKS
                     |
                     v
                    RDS
```

---

# 108. Terraform + EKS

In a larger enterprise:

```text
Terraform
   |
   +--> VPC
   |
   +--> Subnets
   |
   +--> IAM
   |
   +--> EKS
   |
   +--> Node Groups
```

Then:

```text
Argo CD / Jenkins
       |
       v
EKS
```

Terraform creates the infrastructure.

Kubernetes tools manage application workloads.

---

# 109. Terraform vs Kubernetes

Important distinction:

### Terraform

Manages infrastructure:

```text
VPC
EC2
EKS
RDS
IAM
Security Groups
```

### Kubernetes

Manages container workloads:

```text
Deployment
Service
ConfigMap
Secret
Ingress
HPA
```

A common enterprise architecture uses both.

---

# 110. Terraform vs Ansible

### Terraform

Primarily:

```text
Infrastructure provisioning
```

### Ansible

Primarily:

```text
Configuration management
Application/server automation
```

Example:

```text
Terraform
   |
   v
Create EC2

Ansible
   |
   v
Configure EC2
```

---

# 111. Terraform vs CloudFormation

Both are Infrastructure as Code technologies.

Terraform:

```text
Multi-cloud
Reusable modules
Large provider ecosystem
```

CloudFormation:

```text
AWS-native
Deep AWS integration
```

The appropriate choice depends on organizational requirements.

---

# 112. Terraform Idempotency

Terraform aims for predictable desired-state management.

If the infrastructure already matches the configuration:

```bash
terraform plan
```

may show:

```text
No changes.
Your infrastructure matches the configuration.
```

This is an important IaC principle.

---

# 113. Immutable Infrastructure Concept

Instead of manually modifying servers:

```text
Existing Server
     |
     | manual changes
     v
Configuration drift
```

IaC encourages:

```text
Code
 |
 v
Desired Infrastructure
```

For some resources, replacement is preferable to manual mutation.

---

# 114. Enterprise Review Checklist

Before Terraform PR approval:

```text
[ ] terraform fmt
[ ] terraform validate
[ ] terraform plan reviewed
[ ] No hardcoded credentials
[ ] IAM permissions reviewed
[ ] Security groups reviewed
[ ] CIDRs reviewed
[ ] Tags present
[ ] Variables documented
[ ] Outputs documented
[ ] Module reusable
[ ] State strategy verified
[ ] Provider version constrained
[ ] Security scan passed
[ ] Destroy/replacement changes understood
```

---

# 115. Production Checklist

Before production apply:

```text
[ ] Correct AWS account
[ ] Correct region
[ ] Correct workspace/environment
[ ] Correct backend
[ ] Correct state
[ ] Terraform plan reviewed
[ ] Destructive changes checked
[ ] IAM permissions verified
[ ] Backup/recovery understood
[ ] Approval received
```

---

# 116. Important Terraform Commands

Initialization:

```bash
terraform init
```

Formatting:

```bash
terraform fmt
```

Validation:

```bash
terraform validate
```

Planning:

```bash
terraform plan
```

Save plan:

```bash
terraform plan -out=tfplan
```

Apply:

```bash
terraform apply
```

Apply saved plan:

```bash
terraform apply tfplan
```

Destroy:

```bash
terraform destroy
```

Show outputs:

```bash
terraform output
```

List state:

```bash
terraform state list
```

Show resource:

```bash
terraform state show RESOURCE
```

---

# 117. Practical Project Validation

From the Terraform directory:

```bash
cd terraform
```

Check:

```bash
terraform version
```

Initialize:

```bash
terraform init
```

Format:

```bash
terraform fmt -recursive
```

Validate:

```bash
terraform validate
```

Plan:

```bash
terraform plan
```

Review:

```bash
git diff
```

Apply:

```bash
terraform apply
```

---

# 118. AWS Validation

After apply:

```bash
aws sts get-caller-identity
```

Check EC2:

```bash
aws ec2 describe-instances
```

Check VPC:

```bash
aws ec2 describe-vpcs
```

Check subnets:

```bash
aws ec2 describe-subnets
```

Check security groups:

```bash
aws ec2 describe-security-groups
```

---

# 119. Terraform Validation Flow

```text
terraform fmt
       |
       v
terraform validate
       |
       v
terraform plan
       |
       v
AWS Validation
       |
       v
terraform apply
       |
       v
AWS Resource Validation
```

---

# 120. Troubleshooting RCA Template

For every Terraform incident document:

```text
Incident:
Terraform Apply Failure

Symptom:
EC2 creation failed

Error:
AccessDenied

Investigation:
1. Checked Terraform plan
2. Checked AWS identity
3. Checked IAM permissions
4. Checked region
5. Checked provider

Root Cause:
IAM role did not have required permission

Fix:
Updated IAM policy

Validation:
terraform plan
terraform apply
AWS CLI validation

Prevention:
Add IAM validation to CI/CD
```

---

# 121. Enterprise Terraform Incident Flow

```text
Incident
   |
   v
Capture Error
   |
   v
terraform plan
   |
   v
Check Provider
   |
   v
Check Credentials
   |
   v
Check IAM
   |
   v
Check Region
   |
   v
Check State
   |
   v
Check AWS Resource
   |
   v
Root Cause
   |
   v
Fix
   |
   v
Plan
   |
   v
Apply
   |
   v
Validate
   |
   v
Document RCA
```

---

# 122. Common Terraform Mistakes

Avoid:

```text
Hardcoded AWS credentials
Hardcoded environment values
Committing state
Committing secrets
Blind terraform apply
Blind terraform destroy
Ignoring plan output
Manual production changes
Uncontrolled provider upgrades
Huge monolithic modules
Overusing depends_on
```

---

# 123. Enterprise Best Practices

## Code

```text
Use modules
Use variables
Use outputs
Use locals
Use meaningful names
Format code
```

## Security

```text
Least privilege
Secret management
Encrypted state
Restricted network access
Security scanning
```

## Git

```text
Feature branches
Pull requests
Code review
Protected main branch
Commit traceability
```

## Operations

```text
Remote state
State locking
Plan before apply
Approval for production
Drift detection
RCA documentation
```

---

# 124. Terraform Interview Questions

## Q1. What is Terraform?

**Answer:**

> "Terraform is an Infrastructure as Code tool used to define, provision, and manage infrastructure using declarative configuration. I can use Terraform to manage AWS resources such as VPCs, EC2, IAM, security groups, EKS and RDS."

---

## Q2. What is Infrastructure as Code?

**Answer:**

> "Infrastructure as Code means defining infrastructure in code instead of creating it manually. This makes infrastructure repeatable, version-controlled, reviewable and automatable."

---

## Q3. What is a Terraform provider?

**Answer:**

> "A provider is the Terraform plugin that allows Terraform to interact with an external platform such as AWS. The AWS provider communicates with AWS APIs to create and manage resources."

---

## Q4. What is Terraform state?

**Answer:**

> "Terraform state records Terraform's knowledge about managed infrastructure, including resource identities and attributes. Terraform uses state to map configuration to real infrastructure and determine what changes are required."

---

## Q5. Why use remote state?

**Answer:**

> "Remote state provides centralized and controlled state storage for teams and CI/CD systems. It also supports collaboration and, depending on the backend, state locking and recovery capabilities."

---

## Q6. What is Terraform drift?

**Answer:**

> "Drift occurs when infrastructure changes outside Terraform, for example through the AWS Console, causing the actual infrastructure to differ from the Terraform configuration or recorded state."

---

## Q7. How do you detect drift?

**Answer:**

> "I run terraform plan and review the differences between the desired configuration and the current infrastructure state."

---

## Q8. What is terraform plan?

**Answer:**

> "Terraform plan previews the infrastructure changes Terraform intends to make without applying them. I use it as a safety and review step before apply."

---

## Q9. What is terraform apply?

**Answer:**

> "Terraform apply executes the planned infrastructure changes against the target platform."

---

## Q10. What is terraform init?

**Answer:**

> "Terraform init initializes the working directory, downloads required providers and modules, and configures the backend."

---

## Q11. What is a Terraform module?

**Answer:**

> "A module is a reusable collection of Terraform configuration. In this project, I use a VPC child module so networking resources can be reused with different input values."

---

## Q12. Root module vs child module?

**Answer:**

> "The root module is the directory from which Terraform is executed. A child module is a reusable module called by the root or another module. In this project, the root Terraform configuration calls the VPC child module."

---

## Q13. Resource vs data source?

**Answer:**

> "A resource manages infrastructure, while a data source reads existing information from a provider."

---

## Q14. Count vs for_each?

**Answer:**

> "Count is useful when I need a number of similar resources. For_each is useful when resources have meaningful keys or different values because the keys provide stable resource identities."

---

## Q15. What happens if someone manually changes an EC2 instance?

**Answer:**

> "That can create drift. I would run terraform plan, determine the difference, and then either update the Terraform configuration if the change is intended or revert the infrastructure to match the desired configuration."

---

## Q16. What happens if Terraform wants to destroy a production resource?

**Answer:**

> "I would not blindly apply. I would inspect the plan, identify why Terraform wants to destroy or replace the resource, verify the state and configuration, and obtain the required approval before applying."

---

## Q17. Does terraform state rm destroy infrastructure?

**Answer:**

> "No. terraform state rm removes the resource from Terraform state. It does not normally destroy the actual infrastructure."

---

## Q18. How do you import an existing resource?

**Answer:**

> "I define the Terraform resource configuration and associate the existing resource with that Terraform resource address using Terraform's import capability. Then I run terraform plan to reconcile the configuration with the imported infrastructure."

---

## Q19. How do you handle secrets?

**Answer:**

> "I avoid hardcoding secrets in Terraform files. I use secure mechanisms such as AWS Secrets Manager, SSM Parameter Store, CI/CD secret stores or workload identity, and I protect Terraform state because sensitive values can still be represented in state."

---

## Q20. How do you integrate Terraform with Jenkins?

**Answer:**

> "Jenkins checks out the Terraform repository, initializes Terraform, validates and formats the configuration, runs security checks, generates a plan, publishes the plan for review, and after approval runs terraform apply using an appropriate AWS identity."

---

# 125. 7-Year-Level Interview Scenario

### Interviewer:

> "You have an existing production VPC created manually. How would you bring it under Terraform?"

### Answer:

> "First I would inventory the existing VPC resources and understand the current architecture. I would create matching Terraform configuration, import the existing resources into the appropriate Terraform addresses, and then run terraform plan. I would continue reconciling the configuration until the plan represents the intended state without unexpected destructive changes. Once validated and reviewed, I would manage future changes through Terraform and Git rather than making manual changes."

---

# 126. 7-Year-Level Scenario — Production Drift

### Interviewer:

> "Someone changed a production EC2 instance manually. What will you do?"

### Answer:

> "I would first identify exactly what changed using terraform plan and AWS information. Then I would determine whether the manual change was intentional. If it is intended, I would update the Terraform configuration and commit the change through the normal review process. If it was unauthorized or accidental, I would restore the infrastructure to the Terraform-defined configuration. I would also document the incident and address the process that allowed the manual change."

---

# 127. 7-Year-Level Scenario — Terraform Wants Replacement

### Interviewer:

> "Terraform wants to destroy and recreate a production EC2 instance. What do you do?"

### Answer:

> "I would not immediately apply. I would inspect the plan and identify the exact attribute causing replacement. I would check whether the change is expected, whether downtime is acceptable, and whether a safer migration strategy is required. If the replacement is valid, I would follow the production approval process and execute it during an appropriate window."

---

# 128. 7-Year-Level Scenario — State Lock

### Interviewer:

> "Terraform says the state is locked. What do you do?"

### Answer:

> "First I would verify whether another Terraform operation is actually running. I would never force-unlock blindly. If the lock is stale and I have confirmed no active Terraform operation exists, I would follow the organization's state recovery process and use force-unlock only when appropriate."

---

# 129. 7-Year-Level Scenario — Two Engineers Apply

### Interviewer:

> "Two engineers run terraform apply at the same time. What protects the infrastructure?"

### Answer:

> "A properly configured remote backend with state locking helps prevent concurrent state modifications. The second operation should wait or fail according to the backend behavior rather than modifying the same state concurrently."

---

# 130. 7-Year-Level Scenario — AWS Console Change

### Interviewer:

> "Should DevOps engineers never use the AWS Console?"

### Answer:

> "The console can still be useful for investigation, monitoring and emergency troubleshooting. However, infrastructure changes should normally be performed through Terraform in an IaC-managed environment so that the desired state remains version-controlled and auditable."

---

# 131. Project-Level Enterprise Flow

This project can evolve into:

```text
GitHub
   |
   v
Jenkins
   |
   +--> Terraform Init
   |
   +--> Terraform Validate
   |
   +--> Security Scan
   |
   +--> Terraform Plan
   |
   v
Approval
   |
   v
Terraform Apply
   |
   v
AWS
   |
   +--> VPC
   |
   +--> Security Groups
   |
   +--> Jenkins EC2
   |
   +--> Future Infrastructure
```

---

# 132. Complete Terraform Lifecycle

```text
Write Code
    |
    v
terraform fmt
    |
    v
terraform init
    |
    v
terraform validate
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
AWS Infrastructure
    |
    v
Monitor
    |
    v
Detect Drift
    |
    v
Modify Code
    |
    v
Plan
    |
    v
Apply
```

---

# 133. Enterprise Traceability

Infrastructure changes should be traceable:

```text
Jira Ticket
     |
     v
Git Branch
     |
     v
Terraform Commit
     |
     v
Pull Request
     |
     v
Terraform Plan
     |
     v
Approval
     |
     v
Terraform Apply
     |
     v
AWS Infrastructure
```

This provides auditability.

---

# 134. Final Project Summary

This Terraform project demonstrates:

```text
Infrastructure as Code
Terraform Providers
Terraform Variables
Terraform Outputs
Terraform Modules
VPC Module
EC2 Provisioning
Security Groups
Terraform State
Terraform Plan
Terraform Apply
Terraform Import
Terraform Drift
Terraform Dependencies
Terraform Git Workflow
Terraform CI/CD
Terraform Security
Terraform Troubleshooting
Terraform Rollback
Enterprise Best Practices
7-Year-Level Interview Preparation
```

---

# 135. Final Interview Answer

If an interviewer asks:

> "Explain your Terraform project."

A strong answer is:

> "In my project, I use Terraform to provision and manage AWS infrastructure as Infrastructure as Code. The Terraform root module contains the environment-level configuration and calls a reusable VPC child module. The VPC module encapsulates networking resources and exposes required values through outputs. I also manage Jenkins infrastructure using separate Terraform configuration files for the EC2 instance, security group and variables.
>
> I follow the standard Terraform lifecycle of init, fmt, validate, plan and apply. Before applying changes, I review the Terraform plan to understand creates, updates and replacements. I also understand Terraform state, remote backend concepts, state locking, drift detection and importing existing AWS resources.
>
> For enterprise usage, I would store state in a protected remote backend, use least-privilege AWS identity, keep provider versions controlled, avoid hardcoded secrets, run Terraform validation and security scanning through Jenkins, generate a plan during CI, and require approval before production apply.
>
> If infrastructure changes outside Terraform, I use terraform plan to identify drift and then either update the Terraform code if the change is intentional or reconcile the infrastructure back to the desired state. For rollback, I normally restore the previous Terraform configuration through Git and apply the resulting plan rather than treating terraform destroy as a rollback mechanism."

---

# 136. Quick Revision Sheet

Remember this flow:

```text
CODE
 |
 v
INIT
 |
 v
FMT
 |
 v
VALIDATE
 |
 v
PLAN
 |
 v
REVIEW
 |
 v
APPLY
 |
 v
AWS
```

Remember Terraform architecture:

```text
ROOT MODULE
     |
     v
CHILD MODULE
     |
     v
RESOURCE
     |
     v
AWS
```

Remember state:

```text
Terraform Code
      |
      v
Terraform State
      |
      v
AWS Infrastructure
```

Remember drift:

```text
Terraform Code
      |
      X
AWS changed manually
      |
      v
DRIFT
      |
      v
terraform plan
```

Remember CI/CD:

```text
Git
 |
 v
Jenkins
 |
 +--> fmt
 +--> init
 +--> validate
 +--> security scan
 +--> plan
 |
 v
Approval
 |
 v
apply
 |
 v
AWS
```

---

# 137. Final Terraform Best-Practice Rule

The most important principle is:

```text
DO NOT THINK:

"I created AWS infrastructure."

THINK:

"I defined the desired infrastructure as code,
stored it in Git,
validated it,
planned the change,
reviewed it,
applied it,
and can reproduce or recover it."
```

That is the difference between simply knowing Terraform commands and working with Terraform at an enterprise DevOps level.

---

# 138. Project Completion Checklist

```text
[x] Terraform project initialized
[x] AWS provider configured
[x] Provider version controlled
[x] Root module configured
[x] VPC child module configured
[x] Module variables configured
[x] Module outputs configured
[x] Jenkins EC2 configuration
[x] Jenkins security group
[x] Terraform variables
[x] Terraform plan
[x] Terraform state concepts
[x] Terraform import concepts
[x] Terraform drift concepts
[x] Terraform Git workflow
[x] Terraform CI/CD architecture
[x] Terraform security
[x] Terraform troubleshooting
[x] Terraform RCA
[x] Terraform rollback
[x] Enterprise best practices
[x] Interview preparation
```

---

# 139. One-Line Interview Summary

> **"Terraform allows me to define AWS infrastructure as version-controlled, reusable, reviewable and repeatable code, while Terraform state maintains the relationship between that configuration and the real infrastructure."**

---

# END OF TERRAFORM ENTERPRISE BIBLE
