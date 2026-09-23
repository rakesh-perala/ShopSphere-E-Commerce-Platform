# Terraform Enterprise Checkpoints

## Why Enterprise Terraform Uses Plan → Review → Saved Plan → Apply

---

## 1. Purpose

Terraform is an Infrastructure as Code (IaC) tool used to define and manage infrastructure declaratively.

In a small personal lab, someone may execute:

```bash
terraform init
terraform validate
terraform apply
```

In an enterprise environment, infrastructure changes are normally controlled through explicit checkpoints.

A typical enterprise Terraform workflow is:

```text
Terraform Code
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
Review Proposed Infrastructure Changes
      |
      v
Save Approved Plan
      |
      v
terraform apply <saved-plan>
      |
      v
Validate Actual Infrastructure
```

The important principle is:

> **Validate the code first, understand the proposed changes second, and only then apply the reviewed infrastructure change.**

---

# 2. Why We Need Enterprise Checkpoints

Terraform controls real infrastructure.

For example, Terraform can create, modify, or destroy:

* VPCs
* Subnets
* Route tables
* Internet Gateways
* NAT Gateways
* Security Groups
* EC2 instances
* RDS databases
* Load Balancers
* IAM resources
* EKS clusters
* S3 buckets
* DNS records

A Terraform command is therefore not just a coding operation.

It can have:

* Cost impact
* Security impact
* Availability impact
* Networking impact
* Data-loss impact
* Application impact
* Compliance impact

Therefore, enterprise teams introduce checkpoints before infrastructure is changed.

---

# 3. The Four Important Terraform Commands

The four commands have different responsibilities.

```text
terraform fmt
       |
       |-- Code formatting
       v
terraform validate
       |
       |-- Configuration correctness
       v
terraform plan
       |
       |-- Proposed infrastructure changes
       v
terraform apply
       |
       |-- Actual infrastructure changes
```

But enterprise workflows introduce one more important concept:

```text
terraform plan -out=<plan-file>
```

This saves the exact proposed execution plan.

Then:

```bash
terraform apply <plan-file>
```

applies that saved plan.

---

# 4. What Does terraform fmt Do?

Command:

```bash
terraform fmt
```

or for CI-style validation:

```bash
terraform fmt -check
```

`terraform fmt` formats Terraform files according to Terraform's standard formatting rules.

For example, inconsistent formatting such as:

```hcl
resource "aws_instance" "jenkins" {
ami="abc"
instance_type="t3.medium"
}
```

can be formatted into:

```hcl
resource "aws_instance" "jenkins" {
  ami           = "abc"
  instance_type = "t3.medium"
}
```

## Why Enterprise Teams Care

Consistent formatting provides:

* Easier code review
* Cleaner pull requests
* Standardized repositories
* Fewer formatting-only changes
* Better readability

However:

> `terraform fmt` does NOT validate whether the infrastructure design is correct.

It only formats the code.

---

# 5. What Does terraform validate Do?

Command:

```bash
terraform validate
```

This checks whether the Terraform configuration is valid.

For example:

```bash
terraform validate
```

may return:

```text
Success! The configuration is valid.
```

This means Terraform can understand the configuration structure.

It helps detect problems such as:

* Invalid HCL syntax
* Invalid Terraform configuration
* Incorrect argument structure
* Invalid references
* Incorrect module configuration
* Some type-related configuration errors

---

# 6. Very Important: validate Does NOT Mean AWS Is Safe

This is one of the most important concepts.

Suppose we have:

```hcl
resource "aws_security_group" "jenkins" {
  name = "jenkins-sg"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
```

Terraform may say:

```text
Success! The configuration is valid.
```

But that does NOT mean:

```text
Security is correct.
```

It only means Terraform understands the configuration.

Terraform validate does not answer questions such as:

* Will this create resources?
* Will this destroy resources?
* How many resources will change?
* Will an existing database be replaced?
* Which security groups will change?
* Which subnet will be used?
* Which EC2 instance will be created?
* How much infrastructure will be affected?
* What will the AWS bill be?
* Is the proposed architecture approved?

Those questions are answered by:

```bash
terraform plan
```

---

# 7. validate vs plan vs apply

This distinction is extremely important for interviews.

| Command              | Main Purpose                   | Changes AWS? |
| -------------------- | ------------------------------ | -----------: |
| `terraform fmt`      | Format Terraform code          |           No |
| `terraform validate` | Validate configuration         |           No |
| `terraform plan`     | Calculate proposed changes     |           No |
| `terraform apply`    | Execute infrastructure changes |          Yes |

Therefore:

```text
validate != plan
plan != apply
```

Each stage has a different responsibility.

---

# 8. Why Didn't We Run terraform apply Immediately After validate?

This was intentional.

Our workflow was:

```bash
terraform fmt -check
terraform validate
terraform plan
terraform plan -out=jenkins-bootstrap.tfplan
terraform show jenkins-bootstrap.tfplan
terraform apply jenkins-bootstrap.tfplan
```

We did NOT do:

```bash
terraform validate
terraform apply
```

because `validate` only tells us:

> "Terraform understands this configuration."

It does not tell us:

> "These are exactly the AWS changes we want to make."

That second question requires:

```bash
terraform plan
```

---

# 9. What Does terraform plan Do?

Command:

```bash
terraform plan
```

Terraform compares:

```text
Terraform Configuration
        +
Terraform State
        +
Current Infrastructure
        |
        v
Proposed Changes
```

It determines what Terraform intends to do.

Typical output:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

This is extremely valuable.

It tells us:

```text
16 resources will be created.
0 resources will be modified.
0 resources will be destroyed.
```

---

# 10. Why Plan Review Is Important

Imagine Terraform shows:

```text
Plan: 16 to add, 0 to change, 3 to destroy.
```

That immediately deserves investigation.

Why?

Because we may not have intended to destroy anything.

A production database could potentially be affected.

A route table could change.

A security group could change.

An EC2 instance could be replaced.

Therefore:

```text
terraform plan
```

acts as a safety checkpoint.

---

# 11. Our ShopSphere Terraform Example

For our ShopSphere Jenkins infrastructure, Terraform planned:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

The resources included:

```text
VPC
 |
 +-- Public Subnet 1
 |
 +-- Public Subnet 2
 |
 +-- Private Subnet 1
 |
 +-- Private Subnet 2
 |
 +-- Internet Gateway
 |
 +-- NAT Gateway
 |
 +-- NAT EIP
 |
 +-- Route Tables
 |
 +-- Route Associations
 |
 +-- Jenkins Security Group
 |
 +-- Jenkins EC2
```

The important observation was:

```text
16 to add
0 to change
0 to destroy
```

This matched the intended initial infrastructure deployment.

Therefore the plan could be reviewed before infrastructure creation.

---

# 12. Why We Saved the Plan

Instead of simply running:

```bash
terraform apply
```

we used:

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

This does two things:

1. Calculates the Terraform plan.
2. Saves that plan to a file.

Example:

```text
jenkins-bootstrap.tfplan
```

This gives us a concrete artifact representing the planned infrastructure change.

---

# 13. Why a Saved Plan Is Important

Think about the difference.

### Without saved plan

```text
terraform plan
       |
       v
Human reviews output
       |
       v
terraform apply
       |
       v
Terraform calculates/apply operation
```

### With saved plan

```text
terraform plan -out=jenkins-bootstrap.tfplan
       |
       v
Human reviews saved plan
       |
       v
Approved plan artifact
       |
       v
terraform apply jenkins-bootstrap.tfplan
```

The second workflow gives us a much stronger change-control process.

---

# 14. terraform plan -out

Our command:

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

produced:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

and:

```text
Saved the plan to: jenkins-bootstrap.tfplan
```

This was our **reviewed Terraform execution plan**.

---

# 15. Why Did We Run terraform show?

After saving the plan, we ran:

```bash
terraform show jenkins-bootstrap.tfplan
```

This allowed us to inspect the saved plan.

The workflow became:

```text
terraform plan -out=jenkins-bootstrap.tfplan
                |
                v
       Saved execution plan
                |
                v
terraform show jenkins-bootstrap.tfplan
                |
                v
         Human review
                |
                v
terraform apply jenkins-bootstrap.tfplan
```

This is much closer to enterprise change-management practices.

---

# 16. Why Apply the Saved Plan?

After review, we ran:

```bash
terraform apply jenkins-bootstrap.tfplan
```

Terraform then executed the saved plan.

The result was:

```text
Apply complete! Resources: 16 added, 0 changed, 0 destroyed.
```

This gave us:

```text
Planned:
16 add
0 change
0 destroy

Applied:
16 add
0 change
0 destroy
```

That is an important checkpoint.

---

# 17. Plan vs Saved Plan

There is an important distinction.

### Normal plan

```bash
terraform plan
```

Terraform calculates a plan and displays it.

### Saved plan

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

Terraform calculates and saves the plan.

Then:

```bash
terraform apply jenkins-bootstrap.tfplan
```

applies that saved plan.

This is useful when we want a controlled:

```text
Plan → Review → Approval → Apply
```

workflow.

---

# 18. Enterprise Change-Control Model

A simplified enterprise workflow looks like:

```text
Developer / DevOps Engineer
          |
          v
Terraform Code Change
          |
          v
Pull Request
          |
          v
CI Validation
          |
          +---- terraform fmt -check
          |
          +---- terraform validate
          |
          +---- terraform plan
          |
          v
Terraform Plan Review
          |
          v
Approval / Change Control
          |
          v
Saved Terraform Plan
          |
          v
Terraform Apply
          |
          v
Infrastructure Validation
```

This separates:

```text
Writing infrastructure code
```

from:

```text
Changing infrastructure
```

---

# 19. Real-Time Enterprise Example

Imagine a production Terraform change.

A DevOps engineer changes:

```hcl
instance_type = "t3.medium"
```

to:

```hcl
instance_type = "m7i.large"
```

The code may be valid.

Therefore:

```bash
terraform validate
```

passes.

But we still need:

```bash
terraform plan
```

because the important question is:

```text
Will Terraform modify the existing instance?
```

The plan may reveal:

```text
~ aws_instance.app
    instance_type: "t3.medium" -> "m7i.large"
```

Now the team understands the infrastructure impact.

---

# 20. Another Critical Example: Accidental Destroy

Suppose someone changes a resource configuration incorrectly.

Terraform may still return:

```text
terraform validate

Success! The configuration is valid.
```

But:

```bash
terraform plan
```

could show:

```text
Plan: 2 to add, 1 to destroy.
```

That is a major warning.

The engineer can stop before:

```bash
terraform apply
```

and investigate the cause.

This is why:

> **`terraform validate` is not an approval to modify infrastructure.**

---

# 21. Why Enterprise Teams Don't Treat apply as a Testing Command

This is another important mindset.

Some beginners think:

```text
validate → apply → see whether it works
```

That is risky.

The enterprise mindset is:

```text
validate
   ↓
understand proposed changes
   ↓
review
   ↓
approve
   ↓
apply
```

Infrastructure should not be used as the place where we discover what Terraform intends to do.

The plan should provide that visibility first.

---

# 22. Terraform State Is Also Part of the Decision

Terraform calculates changes using:

```text
Configuration
       +
State
       +
Provider / Real Infrastructure
       |
       v
Plan
```

The Terraform state records resources Terraform manages.

For our project:

```bash
terraform state list
```

can show resources such as:

```text
aws_instance.jenkins
aws_security_group.jenkins
module.vpc.aws_vpc.main
module.vpc.aws_subnet.public[0]
...
```

Therefore, `terraform plan` is important because it considers the relationship between:

```text
desired state
```

and:

```text
known managed state
```

---

# 23. Why State Must Be Reviewed Carefully

Suppose Terraform state already contains:

```text
aws_db_instance.production
```

and someone changes a database-related configuration.

A plan could potentially show:

```text
-/+ aws_db_instance.production
```

The:

```text
-/+
```

notation indicates replacement.

That could be much more serious than:

```text
~ change
```

Therefore, enterprise engineers inspect the plan carefully rather than blindly running apply.

---

# 24. The Meaning of Terraform Plan Symbols

Common Terraform plan symbols:

```text
+ resource
```

means:

```text
Create
```

Example:

```text
+ aws_instance.jenkins
```

---

```text
~ resource
```

means:

```text
Modify
```

Example:

```text
~ aws_security_group.jenkins
```

---

```text
- resource
```

means:

```text
Destroy
```

Example:

```text
- aws_instance.old
```

---

```text
-/+ resource
```

means:

```text
Destroy and recreate
```

This is commonly called:

```text
Replacement
```

---

# 25. Why Replacement Requires Extra Attention

Suppose the plan says:

```text
-/+ aws_instance.jenkins
```

That means Terraform may destroy the existing EC2 and create another one.

That could cause:

* Downtime
* New public IP
* Lost local configuration
* Service interruption
* Dependency problems

Therefore:

```text
-/+
```

should immediately trigger investigation.

---

# 26. Our Actual Enterprise Checkpoint

Our ShopSphere workflow was:

### Step 1 — Format

```bash
terraform fmt -check
```

Result:

```text
Passed
```

---

### Step 2 — Validate

```bash
terraform validate
```

Result:

```text
Success! The configuration is valid.
```

---

### Step 3 — Generate and save plan

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

Result:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

Saved:

```text
jenkins-bootstrap.tfplan
```

---

### Step 4 — Review saved plan

```bash
terraform show jenkins-bootstrap.tfplan
```

We confirmed the proposed infrastructure.

---

### Step 5 — Apply reviewed plan

```bash
terraform apply jenkins-bootstrap.tfplan
```

Result:

```text
Apply complete! Resources: 16 added, 0 changed, 0 destroyed.
```

---

# 27. Why This Is Better Than terraform apply Immediately

Compare these two workflows.

## Basic workflow

```text
terraform fmt
      ↓
terraform validate
      ↓
terraform apply
```

Problem:

```text
No explicit infrastructure-impact review
```

---

## Enterprise-style workflow

```text
terraform fmt
      ↓
terraform validate
      ↓
terraform plan
      ↓
Review
      ↓
Save plan
      ↓
Approval
      ↓
Apply saved plan
      ↓
Validate infrastructure
```

This provides a clear separation between:

```text
Code validation
```

and:

```text
Infrastructure execution
```

---

# 28. CI/CD Example

In a Jenkins-based enterprise pipeline, we could separate stages like:

```text
Stage 1
Checkout
      ↓
Stage 2
Terraform Format Check
      ↓
Stage 3
Terraform Validate
      ↓
Stage 4
Terraform Plan
      ↓
Stage 5
Publish Plan Artifact
      ↓
Stage 6
Manual Approval
      ↓
Stage 7
Terraform Apply
      ↓
Stage 8
Infrastructure Validation
```

For example:

```groovy
stage('Terraform Validate') {
    steps {
        sh 'terraform fmt -check'
        sh 'terraform validate'
    }
}

stage('Terraform Plan') {
    steps {
        sh 'terraform plan -out=tfplan'
    }
}

stage('Approval') {
    steps {
        input message: 'Approve Terraform infrastructure change?'
    }
}

stage('Terraform Apply') {
    steps {
        sh 'terraform apply tfplan'
    }
}
```

The exact implementation varies between organizations.

---

# 29. Pull Request Workflow

A mature Terraform repository commonly uses:

```text
feature branch
      |
      v
Pull Request
      |
      v
Terraform fmt
      |
      v
Terraform validate
      |
      v
Terraform plan
      |
      v
Plan output
      |
      v
Code review
      |
      v
Approval
      |
      v
Merge
      |
      v
Apply
```

This gives infrastructure changes the same discipline as application code.

---

# 30. Terraform Plan Is Similar to a Deployment Preview

For application deployments, engineers often want to know:

```text
What will be deployed?
```

Before deployment.

Terraform provides a similar capability:

```bash
terraform plan
```

It answers:

```text
What infrastructure changes will Terraform make?
```

before execution.

Therefore:

```text
terraform plan
```

is effectively an infrastructure change preview.

---

# 31. Why We Used a Named Plan File

We used:

```text
jenkins-bootstrap.tfplan
```

instead of simply:

```text
tfplan
```

because the name communicates purpose.

```text
jenkins-bootstrap.tfplan
```

means:

```text
Terraform plan for Jenkins bootstrap infrastructure
```

In larger projects, descriptive plan artifacts make CI/CD pipelines and troubleshooting easier.

---

# 32. Important Security Note About Plan Files

Terraform plan files can contain sensitive information depending on the configuration and provider behavior.

Therefore:

```text
*.tfplan
```

should generally be protected from accidental publication.

Our `.gitignore` includes:

```gitignore
*.tfplan
```

Therefore:

```text
jenkins-bootstrap.tfplan
```

should NOT be committed to Git.

The plan is a temporary execution artifact, not application source code.

---

# 33. Why We Don't Commit Terraform State Either

Our `.gitignore` also contains:

```gitignore
*.tfstate
*.tfstate.*
```

Terraform state can contain sensitive infrastructure information.

Enterprise environments commonly use a remote backend such as:

```text
Amazon S3
      +
State locking / coordination mechanism
```

instead of storing local state in Git.

For our current lab, local state is being used to understand Terraform behavior.

---

# 34. Important Difference: Plan Is Not a Permanent Approval

A saved plan is not the same thing as:

```text
Permanent infrastructure approval
```

It represents a calculated set of changes based on the Terraform configuration, state, provider information, and environment at the time the plan was created.

Therefore, in real environments, teams should control:

* Who generated the plan
* Which commit generated it
* Which environment it targets
* Whether the plan is still appropriate
* Whether state changed
* Whether another deployment occurred
* Whether approval is still valid

---

# 35. Why We Should Not Blindly Reuse Old Plan Files

For example:

```text
Monday:
terraform plan -out=tfplan
```

Then someone changes Terraform code on Tuesday.

Applying the old plan is not the same as planning Tuesday's code.

Therefore:

```text
Code change
     ↓
New plan
     ↓
Review
     ↓
Apply
```

is the safer workflow.

---

# 36. Enterprise Mental Model

Remember this simple model:

```text
terraform fmt
       |
       | "Is my code formatted?"
       v
terraform validate
       |
       | "Does Terraform understand my configuration?"
       v
terraform plan
       |
       | "What will Terraform change?"
       v
Plan Review
       |
       | "Is that change intended?"
       v
terraform apply
       |
       | "Execute the approved infrastructure change."
       v
Infrastructure
```

This is the core concept.

---

# 37. Interview Answer

### Question:

> Why don't you run `terraform apply` immediately after `terraform validate`?

### Strong answer:

`terraform validate` only verifies that the Terraform configuration is syntactically and structurally valid. It does not show the actual infrastructure changes Terraform intends to make.

In an enterprise workflow, I first run `terraform plan` to understand the impact, especially resources being created, modified, replaced, or destroyed. For controlled deployments, I save the plan using `terraform plan -out=tfplan`, review it, and then apply the reviewed plan using `terraform apply tfplan`.

This gives us a controlled:

```text
Validate → Plan → Review → Approve → Apply
```

workflow and reduces the risk of unintended infrastructure changes.

---

# 38. 7-Year-Level Interview Explanation

A more senior answer:

> I treat Terraform plan as an infrastructure change-management checkpoint rather than treating apply as a testing mechanism. `terraform validate` confirms configuration correctness, but it doesn't provide an impact analysis. Before applying, I generate a plan and inspect resource additions, modifications, replacements, and destructions. For controlled environments, I persist the execution plan with `terraform plan -out`, review it through the CI/CD or change-management process, and apply the approved plan artifact. This gives us traceability and separates configuration validation from infrastructure execution.

---

# 39. Our ShopSphere Enterprise Flow

For ShopSphere, the current architecture follows:

```text
Git
 |
 | Terraform source
 v
Feature Branch
 |
 v
terraform fmt -check
 |
 v
terraform validate
 |
 v
terraform plan
 |
 v
jenkins-bootstrap.tfplan
 |
 v
terraform show
 |
 v
Plan Review
 |
 v
terraform apply jenkins-bootstrap.tfplan
 |
 v
AWS Infrastructure
```

Our first infrastructure deployment produced:

```text
16 resources created
0 changed
0 destroyed
```

The infrastructure included:

```text
VPC
├── Public Subnets
├── Private Subnets
├── Internet Gateway
├── NAT Gateway
├── NAT EIP
├── Route Tables
├── Route Associations
├── Jenkins Security Group
└── Jenkins EC2
```

---

# 40. Final Rule to Remember

The most important rule is:

```text
terraform validate
```

means:

> **"Terraform understands my configuration."**

Not:

> **"It is safe to change AWS."**

Then:

```text
terraform plan
```

means:

> **"Here is what Terraform intends to change."**

Then:

```text
terraform plan -out=tfplan
```

means:

> **"Save this proposed execution plan."**

Then:

```text
terraform show tfplan
```

means:

> **"Let me inspect the saved plan."**

Finally:

```text
terraform apply tfplan
```

means:

> **"Execute the reviewed plan."**

Therefore the enterprise mindset is:

```text
                    ENTERPRISE TERRAFORM
                           |
                           v
                    Format the Code
                           |
                           v
                    Validate the Code
                           |
                           v
                  Generate Infrastructure Plan
                           |
                           v
                       Review Plan
                           |
                           v
                    Approve the Change
                           |
                           v
                    Apply Saved Plan
                           |
                           v
                  Validate Infrastructure
```

## One-Line Interview Memory

> **Validate tells me the configuration is valid; plan tells me what will change; review tells me whether the change is intended; apply executes the reviewed infrastructure change.**

---

# 41. ShopSphere Checkpoint Command Sequence

For future Terraform infrastructure changes, our standard checkpoint should be:

```bash
terraform fmt -check
```

```bash
terraform validate
```

```bash
terraform plan -out=<descriptive-plan-name>.tfplan
```

```bash
terraform show <descriptive-plan-name>.tfplan
```

After review/approval:

```bash
terraform apply <descriptive-plan-name>.tfplan
```

Then verify:

```bash
terraform state list
```

and validate the actual AWS resources.

---

# 42. Final Enterprise Principle

Terraform should be treated as:

```text
Infrastructure Code
+
Change Management
+
Review
+
Controlled Execution
```

not simply:

```text
Write .tf files
+
terraform apply
```

That distinction is what makes the ShopSphere Terraform workflow an **enterprise-style DevOps project** rather than just a Terraform lab.
