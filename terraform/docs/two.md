# Terraform Enterprise Checkpoints

## Plan → Review → Approve → Apply

## 1. Purpose

Terraform manages real infrastructure such as:

* VPCs
* Subnets
* Security Groups
* EC2 instances
* NAT Gateways
* Databases
* Load Balancers
* IAM resources

Because Terraform can create, modify, replace, or destroy infrastructure, enterprise teams do not treat `terraform apply` as a simple testing command.

The goal is to separate:

* **Code validation**
* **Infrastructure impact analysis**
* **Change review**
* **Infrastructure execution**

---

## 2. Standard Enterprise Workflow

For ShopSphere, our Terraform checkpoint process is:

```text
terraform fmt -check
        ↓
terraform validate
        ↓
terraform plan -out=<plan>.tfplan
        ↓
terraform show <plan>.tfplan
        ↓
Review / Approval
        ↓
terraform apply <plan>.tfplan
        ↓
Infrastructure Validation
```

---

## 3. Why We Did Not Run `terraform apply` After `terraform validate`

This is the key concept.

`terraform validate` answers:

> **"Is my Terraform configuration valid?"**

It does **not** answer:

> **"What will Terraform change in AWS?"**

For example, Terraform can successfully return:

```text
Success! The configuration is valid.
```

while the actual plan could contain:

```text
Plan: 5 to add, 2 to change, 1 to destroy.
```

The configuration is valid, but the infrastructure impact may not be what we intended.

Therefore, we run:

```bash
terraform plan
```

before applying infrastructure changes.

---

## 4. Responsibility of Each Command

| Command                      | Purpose                              | Changes AWS? |
| ---------------------------- | ------------------------------------ | -----------: |
| `terraform fmt -check`       | Check Terraform formatting           |           No |
| `terraform validate`         | Validate Terraform configuration     |           No |
| `terraform plan`             | Show proposed infrastructure changes |           No |
| `terraform plan -out=tfplan` | Generate and save execution plan     |           No |
| `terraform show tfplan`      | Review saved plan                    |           No |
| `terraform apply tfplan`     | Execute the saved plan               |      **Yes** |

The important distinction is:

```text
validate != plan != apply
```

---

## 5. Why `terraform plan` Is Required

Terraform determines the proposed changes by considering:

* Terraform configuration
* Terraform state
* Current infrastructure
* Provider information

The plan can show:

```text
+   Create
~   Modify
-   Destroy
-/+ Replace
```

This allows engineers to identify unintended changes before infrastructure is modified.

### Example

If the plan shows:

```text
-/+ aws_instance.jenkins
```

Terraform may destroy and recreate the Jenkins EC2 instance.

That could result in:

* Downtime
* New public IP
* Loss of instance-local configuration
* Service interruption

This is exactly the type of impact that should be identified before `apply`.

---

## 6. Why We Saved the Plan

For our ShopSphere infrastructure, we used:

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

Terraform reported:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

It also created:

```text
jenkins-bootstrap.tfplan
```

The saved plan provides a concrete execution artifact representing the infrastructure changes calculated by Terraform.

---

## 7. Why We Used `terraform show`

We then reviewed the saved plan:

```bash
terraform show jenkins-bootstrap.tfplan
```

This allowed us to inspect the planned infrastructure before execution.

The checkpoint was therefore:

```text
Generate Plan
      ↓
Save Plan
      ↓
Review Plan
      ↓
Apply Reviewed Plan
```

---

## 8. Why We Applied the Saved Plan

After reviewing the plan, we executed:

```bash
terraform apply jenkins-bootstrap.tfplan
```

Terraform completed:

```text
Apply complete! Resources: 16 added, 0 changed, 0 destroyed.
```

This gave us consistency between the reviewed plan and the executed infrastructure change.

---

## 9. Why This Is Better Than Direct Apply

A basic workflow could be:

```bash
terraform validate
terraform apply
```

The problem is that there is no explicit infrastructure-impact review between validation and execution.

Our enterprise-style workflow is:

```bash
terraform validate
terraform plan -out=jenkins-bootstrap.tfplan
terraform show jenkins-bootstrap.tfplan
terraform apply jenkins-bootstrap.tfplan
```

This introduces a deliberate change-control checkpoint.

---

## 10. Terraform Plan as a Change-Control Checkpoint

The plan allows engineers or reviewers to answer:

* What resources will be created?
* What resources will be modified?
* What resources will be destroyed?
* Will any resource be replaced?
* Are the changes expected?
* Does the infrastructure change match the ticket or requirement?
* Are there unexpected security or networking changes?

Only after these questions are addressed should infrastructure be applied.

---

## 11. Enterprise CI/CD Usage

A Jenkins pipeline can separate validation, planning, approval, and execution.

Example:

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

The exact implementation depends on the organization's CI/CD and change-management process.

---

## 12. Important Point About Saved Plan Files

Terraform plan files should not normally be committed to Git.

Our Terraform `.gitignore` contains:

```gitignore
*.tfplan
```

Plan files can contain sensitive infrastructure information and are execution artifacts rather than source code.

Similarly, Terraform state files are not committed to Git:

```gitignore
*.tfstate
*.tfstate.*
```

Enterprise environments commonly use a secure remote Terraform backend for state management.

---

## 13. Important Point About Old Plans

A saved plan should not be treated as a permanent approval.

If Terraform code, state, or the target environment changes, the previous plan may no longer represent the intended infrastructure change.

The safer approach is:

```text
Code Change
    ↓
Generate New Plan
    ↓
Review
    ↓
Apply
```

---

# 14. ShopSphere Terraform Checkpoint

For our Jenkins infrastructure, we followed:

### Format Check

```bash
terraform fmt -check
```

Result:

```text
Passed
```

### Configuration Validation

```bash
terraform validate
```

Result:

```text
Success! The configuration is valid.
```

### Saved Plan

```bash
terraform plan -out=jenkins-bootstrap.tfplan
```

Result:

```text
Plan: 16 to add, 0 to change, 0 to destroy.
```

### Plan Review

```bash
terraform show jenkins-bootstrap.tfplan
```

### Controlled Apply

```bash
terraform apply jenkins-bootstrap.tfplan
```

Result:

```text
Apply complete! Resources: 16 added, 0 changed, 0 destroyed.
```

---

# 15. Interview Answer

### Question

**Why don't you run `terraform apply` immediately after `terraform validate`?**

### Answer

`terraform validate` only verifies that the Terraform configuration is syntactically and structurally valid. It does not show what Terraform will actually change in the infrastructure.

In an enterprise workflow, I first run `terraform plan` to understand the impact, including resources that will be created, modified, replaced, or destroyed. For controlled deployments, I save the plan using `terraform plan -out`, review it, and then apply the reviewed plan using `terraform apply <plan-file>`.

This provides a controlled:

```text
Validate → Plan → Review → Approve → Apply
```

process and reduces the risk of unintended infrastructure changes.

---

# 16. One-Line Memory Rule

> **Validate tells us the Terraform code is valid. Plan tells us what will change. Review confirms the change is intended. Apply executes the reviewed infrastructure change.**

---

# 17. Standard ShopSphere Command Sequence

For future Terraform changes, use:

```bash
terraform fmt -check
terraform validate
terraform plan -out=<descriptive-plan-name>.tfplan
terraform show <descriptive-plan-name>.tfplan
```

After review and approval:

```bash
terraform apply <descriptive-plan-name>.tfplan
```

Then validate the resulting infrastructure and Terraform state.

---

## 18. Enterprise Principle

Terraform should be treated as:

> **Infrastructure as Code + Change Management + Controlled Execution**

rather than simply:

> **Write Terraform → Run `terraform apply`**
