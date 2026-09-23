# ECOM-009 — Jenkins CI Pipeline

## 1. Document Information

| Item                  | Details                           |
| --------------------- | --------------------------------- |
| Project               | ShopSphere E-Commerce Platform    |
| Jira                  | ECOM-009                          |
| Module                | Jenkins CI                        |
| Branch                | `feature/ECOM-009-jenkins-ci`     |
| Implementation Commit | `34d796a`                         |
| Previous Checkpoint   | `408b5f5` — ECOM-007 Docker merge |
| Pipeline File         | `Jenkinsfile`                     |
| Application           | `user-service`                    |
| Build Tool            | Maven                             |
| Java Version          | JDK 17                            |
| Code Quality          | SonarQube                         |
| Security              | Trivy                             |
| Artifact              | Spring Boot JAR                   |
| Containerization      | Docker                            |
| Artifact Repository   | Nexus — release flow              |
| Image Registry        | Docker Hub — release flow         |

---

# 2. Business Requirement

ShopSphere requires an automated Continuous Integration pipeline that validates application changes whenever developers push code.

The pipeline must automatically:

1. Checkout source code.
2. Identify the application version.
3. Compile and test the service.
4. Publish test results.
5. Perform SonarQube static analysis.
6. Validate the SonarQube Quality Gate.
7. Perform filesystem security scanning with Trivy.
8. Identify the generated JAR artifact.
9. Build a Docker image for release branches.
10. Scan the Docker image.
11. Push the approved release image to Docker Hub.

The objective is to prevent defective, insecure, or unvalidated code from progressing toward release.

---

# 3. Why Jenkins CI?

Jenkins provides an automation engine that connects source control, build tools, quality tools, security scanners, artifact repositories, and container registries into a repeatable delivery process.

Without CI:

```text
Developer
   |
   v
Git Push
   |
   v
Manual Build
   |
   v
Manual Testing
   |
   v
Manual Security Check
   |
   v
Manual Docker Build
   |
   v
Manual Push
```

This creates opportunities for:

* Human error
* Missing tests
* Inconsistent builds
* Security issues
* Incorrect artifact versions
* Manual deployment mistakes
* Poor traceability

With Jenkins:

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
    +--> Version Resolution
    |
    +--> Maven Test
    |
    +--> SonarQube
    |
    +--> Quality Gate
    |
    +--> Trivy FS Scan
    |
    +--> Artifact Detection
    |
    +--> Release-only Docker Build
    |
    +--> Docker Image Scan
    |
    +--> Docker Hub Push
```

The same process is executed consistently for every build.

---

# 4. What We Added

The primary implementation for ECOM-009 is:

```text
Jenkinsfile
```

at the repository root.

Repository structure:

```text
ShopSphere-E-Commerce-Platform/
│
├── Jenkinsfile
│
├── application/
│   └── services/
│       └── user-service/
│           ├── pom.xml
│           ├── Dockerfile
│           └── src/
│
├── terraform/
│
├── jenkins/
│
└── docs/
```

The Jenkinsfile defines the complete CI workflow as code.

This follows the principle:

> **Pipeline configuration should be version controlled together with application source code.**

---

# 5. Why Pipeline as Code?

Instead of manually creating every Jenkins build step through the Jenkins UI, the pipeline is defined in the repository.

This provides:

* Version control
* Code review
* Change history
* Auditability
* Reproducibility
* Easy rollback
* Consistent CI behavior
* Disaster recovery

For example:

```text
Git
 |
 +-- Jenkinsfile
 |
 +-- pom.xml
 |
 +-- Dockerfile
 |
 +-- application source
```

Jenkins reads the `Jenkinsfile` and executes the defined pipeline.

If the pipeline changes, the change itself is reviewed through Git.

---

# 6. Jenkinsfile Architecture

The pipeline follows this structure:

```text
pipeline
│
├── agent
│
├── tools
│
├── options
│
├── environment
│
├── stages
│   ├── Git Checkout
│   ├── Resolve Application Version
│   ├── Maven Build & Test
│   ├── SonarQube Analysis
│   ├── SonarQube Quality Gate
│   ├── Trivy Filesystem Scan
│   ├── Identify Build Artifact
│   ├── Upload to Nexus
│   ├── Docker Build
│   ├── Trivy Docker Image Scan
│   └── Docker Hub Push
│
└── post
    ├── success
    ├── failure
    └── always
```

---

# 7. Jenkins Agent

The pipeline uses:

```groovy
agent any
```

This means Jenkins can execute the pipeline on any available compatible Jenkins agent.

The agent must have the required tooling available.

For the current ShopSphere Jenkins server, Terraform provisioning installed the required build tools including:

* Java
* Maven
* Docker
* Trivy
* Jenkins

---

# 8. Jenkins Tool Configuration

The pipeline references configured Jenkins tools:

```groovy
tools {
    jdk 'jdk17'
    maven 'maven3'
}
```

This means the pipeline does not hardcode local executable paths for Java and Maven.

Jenkins manages the configured tool installations.

Conceptually:

```text
Jenkins Global Tools
        |
        +-- jdk17
        |
        +-- maven3
        |
        +-- sonar-scanner
```

The pipeline references those logical names.

This makes the Jenkinsfile portable across Jenkins agents.

---

# 9. Pipeline Options

The pipeline includes:

```groovy
options {
    disableConcurrentBuilds()
    timestamps()

    buildDiscarder(
        logRotator(
            numToKeepStr: '20'
        )
    )
}
```

## 9.1 disableConcurrentBuilds

```groovy
disableConcurrentBuilds()
```

Prevents multiple builds of the same pipeline from executing simultaneously.

This is useful when builds share:

* Workspace
* Docker resources
* Temporary files
* Build artifacts

It reduces race-condition risks.

---

## 9.2 timestamps

```groovy
timestamps()
```

Adds timestamps to Jenkins console logs.

Example:

```text
12:30:01 [Pipeline] Start
12:30:05 Running Maven tests
12:30:42 SonarQube analysis completed
```

This is valuable for:

* Troubleshooting
* Performance analysis
* Incident investigation
* Auditability

---

## 9.3 Build Retention

```groovy
buildDiscarder(
    logRotator(
        numToKeepStr: '20'
    )
)
```

Only the most recent 20 builds are retained.

This prevents Jenkins from accumulating unlimited build history and consuming disk space.

---

# 10. Environment Variables

The pipeline defines:

```groovy
environment {

    SERVICE_NAME = 'user-service'

    SERVICE_DIR = 'application/services/user-service'

    DOCKER_IMAGE = 'dockerperala/shopsphere-user-service'

    SCANNER_HOME = tool 'sonar-scanner'
}
```

## SERVICE_NAME

```text
user-service
```

Identifies the ShopSphere microservice being built.

## SERVICE_DIR

```text
application/services/user-service
```

Allows the same Jenkinsfile to execute commands from the correct microservice directory.

## DOCKER_IMAGE

```text
dockerperala/shopsphere-user-service
```

Defines the Docker image repository.

## SCANNER_HOME

```groovy
tool 'sonar-scanner'
```

Resolves the configured Jenkins Sonar Scanner installation.

---

# 11. Stage 1 — Git Checkout

```groovy
stage('Git Checkout') {
    steps {
        checkout scm
    }
}
```

Jenkins retrieves the source code from the SCM configuration.

Flow:

```text
Git Repository
      |
      v
    Jenkins
      |
      v
   Workspace
```

Why?

The pipeline must always build the exact source revision associated with the Jenkins job.

---

# 12. Stage 2 — Resolve Application Version

The pipeline executes:

```bash
mvn help:evaluate \
  -Dexpression=project.version \
  -q \
  -DforceStdout
```

The version comes from:

```text
application/services/user-service/pom.xml
```

Example:

```xml
<version>1.0.0</version>
```

Jenkins stores the result in:

```text
APP_VERSION
```

For example:

```text
APP_VERSION=1.0.0
```

---

# 13. Why Maven POM Is the Version Source of Truth

We intentionally do not generate arbitrary Docker versions inside Jenkins.

The application version is defined by Maven.

Therefore:

```text
pom.xml
    |
    v
1.0.0
    |
    +----> Nexus artifact
    |
    +----> Docker image
    |
    +----> Release metadata
```

This keeps application artifacts traceable.

Example:

```text
user-service:1.0.0
```

can be traced back to:

```text
Maven version = 1.0.0
```

---

# 14. Stage 3 — Maven Build & Test

The pipeline executes:

```bash
mvn clean test
```

This performs:

```text
Clean
  |
  v
Compile
  |
  v
Test
```

The purpose is to detect:

* Compilation errors
* Unit test failures
* Dependency problems
* Application regressions

If tests fail:

```text
Maven Test
    |
    X
FAIL
    |
    v
Pipeline stops
```

The pipeline should not proceed toward release activities.

---

# 15. JUnit Test Reporting

The pipeline publishes:

```groovy
junit(
    testResults: '**/target/surefire-reports/*.xml',
    allowEmptyResults: true
)
```

Maven Surefire generates XML test reports.

Jenkins consumes those reports and provides test visibility in the Jenkins UI.

This allows teams to see:

* Total tests
* Passed tests
* Failed tests
* Test trends
* Failed test cases

---

# 16. Stage 4 — SonarQube Analysis

The pipeline uses:

```groovy
withSonarQubeEnv('SonarQube')
```

and executes Sonar Scanner.

The analysis is configured with:

```text
Project Name:
shopsphere-user-service

Project Key:
shopsphere-user-service
```

Source directory:

```text
src/main/java
```

Compiled classes:

```text
target/classes
```

---

# 17. Why SonarQube?

Maven tests answer:

> "Does the application behavior pass the automated tests?"

SonarQube provides additional code-quality analysis.

It can identify areas such as:

* Bugs
* Vulnerabilities
* Code smells
* Duplicated code
* Maintainability issues
* Coverage-related quality information

The objective is to detect quality issues before release.

---

# 18. Stage 5 — SonarQube Quality Gate

After analysis:

```groovy
waitForQualityGate(
    abortPipeline: true
)
```

Jenkins waits for SonarQube to return the Quality Gate result.

Conceptually:

```text
Code
 |
 v
SonarQube Analysis
 |
 v
Quality Gate
 |
 +---- PASS ---> Continue
 |
 +---- FAIL ---> Pipeline stops
```

The important enterprise principle is:

> **Static analysis should not be informational only when the organization requires quality enforcement.**

A failed Quality Gate blocks further pipeline progression.

---

# 19. Stage 6 — Trivy Filesystem Scan

The pipeline executes:

```bash
trivy fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  --no-progress \
  .
```

Trivy scans the service workspace.

The scan includes:

```text
vuln
secret
misconfig
```

Only:

```text
HIGH
CRITICAL
```

severity findings are considered blocking.

---

# 20. Why Trivy Filesystem Scan?

Security scanning should happen before packaging and release.

The filesystem scan can identify problems such as:

* Vulnerable dependencies
* Exposed secrets
* Configuration problems
* Infrastructure/configuration security issues

Pipeline behavior:

```text
Trivy FS Scan
      |
      +---- No blocking findings ---> Continue
      |
      +---- HIGH/CRITICAL ----------> Fail
```

This shifts security left into the development lifecycle.

---

# 21. Stage 7 — Identify Build Artifact

The pipeline searches:

```bash
find target \
  -maxdepth 1 \
  -type f \
  -name "*.jar" \
  ! -name "*.jar.original" \
  | head -n 1
```

The generated application JAR is stored as:

```text
JAR_FILE
```

Example:

```text
target/user-service-1.0.0.jar
```

---

# 22. Why We Explicitly Identify the JAR

ShopSphere `user-service` is a Spring Boot application.

Its deployment artifact is:

```text
JAR
```

It is **not a WAR application**.

Therefore, this pipeline does not deploy the service to Tomcat.

The architecture is:

```text
Spring Boot
     |
     v
Executable JAR
     |
     v
Docker Image
     |
     v
Container Platform
```

This is different from traditional Java application architecture:

```text
WAR
 |
 v
Tomcat
```

The ShopSphere pipeline follows the Spring Boot containerized model.

---

# 23. Release Branch Strategy

Not every branch should automatically publish production-style artifacts.

The pipeline therefore uses:

```groovy
when {
    expression {
        return env.BRANCH_NAME?.startsWith('release/')
    }
}
```

Release activities execute only for:

```text
release/*
```

Example:

```text
release/v1.0.0
```

---

# 24. Feature/Develop CI Flow

For feature or develop branches:

```text
Git Checkout
     |
     v
Resolve Version
     |
     v
Maven Test
     |
     v
SonarQube
     |
     v
Quality Gate
     |
     v
Trivy FS
     |
     v
Identify JAR
     |
     v
Success
```

The purpose is continuous validation.

No release Docker image is pushed.

---

# 25. Release CI Flow

For:

```text
release/*
```

the pipeline additionally executes:

```text
Nexus
  |
  v
Docker Build
  |
  v
Trivy Image Scan
  |
  v
Docker Hub Push
```

Complete flow:

```text
Git
 |
 v
Checkout
 |
 v
Version
 |
 v
Maven Test
 |
 v
SonarQube
 |
 v
Quality Gate
 |
 v
Trivy FS
 |
 v
JAR
 |
 v
Nexus
 |
 v
Docker Build
 |
 v
Trivy Image
 |
 v
Docker Hub
```

---

# 26. Stage 8 — Nexus

The current Jenkinsfile contains:

```groovy
stage('Upload to Nexus') {
    when {
        expression {
            return env.BRANCH_NAME?.startsWith('release/')
        }
    }

    steps {
        echo "Nexus upload will be configured for release builds."
    }
}
```

This stage is intentionally a placeholder.

Why?

The ShopSphere Nexus repository details have not yet been finalized.

We should not hardcode another project's Nexus URL or repository configuration.

The intended future artifact flow is:

```text
Maven Build
     |
     v
JAR
     |
     v
Nexus
```

Example conceptual artifact:

```text
com.shopsphere:user-service:1.0.0
```

The actual Nexus repository and server configuration will be added when that infrastructure is established.

---

# 27. Stage 9 — Docker Build

For release branches:

```bash
docker build \
  -t ${DOCKER_IMAGE}:${APP_VERSION} \
  .
```

Example:

```text
dockerperala/shopsphere-user-service:1.0.0
```

The Docker tag is derived from the Maven application version.

---

# 28. Why We Do Not Use `latest`

We intentionally use immutable version tags.

Example:

```text
1.0.0
1.0.1
1.1.0
```

instead of:

```text
latest
```

Why?

With `latest`, the same tag can point to different application builds over time.

With immutable version tags:

```text
1.0.0
```

always identifies a specific release artifact.

This improves:

* Traceability
* Rollback
* Auditing
* Reproducibility
* Deployment safety

---

# 29. Stage 10 — Trivy Docker Image Scan

After building the image, Jenkins runs:

```bash
trivy image \
  --severity HIGH,CRITICAL \
  --no-progress \
  ${DOCKER_IMAGE}:${APP_VERSION}
```

The container image is scanned before publishing.

Flow:

```text
Docker Build
     |
     v
Image
     |
     v
Trivy Image Scan
     |
     +---- Pass ---> Push
     |
     +---- Fail ---> Stop
```

This prevents an image with blocking vulnerabilities from automatically reaching the registry.

---

# 30. Stage 11 — Docker Hub Push

The pipeline uses Jenkins credentials:

```text
docker-cred
```

Credentials are injected through:

```groovy
withCredentials([
    usernamePassword(
        credentialsId: 'docker-cred',
        usernameVariable: 'DOCKER_USERNAME',
        passwordVariable: 'DOCKER_PASSWORD'
    )
])
```

The pipeline logs in using:

```bash
docker login --password-stdin
```

and pushes:

```bash
docker push \
  ${DOCKER_IMAGE}:${APP_VERSION}
```

Then:

```bash
docker logout
```

---

# 31. Why Jenkins Credentials?

Credentials must not be hardcoded into:

```text
Jenkinsfile
Git
Dockerfile
Shell scripts
```

Bad practice:

```text
DOCKER_PASSWORD=my-secret-password
```

Correct enterprise pattern:

```text
Jenkins Credentials
        |
        v
Pipeline
        |
        v
Environment variables
        |
        v
Docker login
```

Secrets remain managed by Jenkins rather than source control.

---

# 32. Pipeline Post Actions

The pipeline contains:

```groovy
post {

    success {
        echo "ShopSphere ${SERVICE_NAME} CI pipeline completed successfully."
    }

    failure {
        echo "ShopSphere ${SERVICE_NAME} CI pipeline failed."
    }

    always {
        archiveArtifacts(
            artifacts: "${SERVICE_DIR}/target/*.jar",
            allowEmptyArchive: true
        )
    }
}
```

---

# 33. Success Handling

On success:

```text
ShopSphere user-service CI pipeline completed successfully.
```

This provides clear build status in the Jenkins console.

---

# 34. Failure Handling

If a stage fails:

```text
Pipeline
   |
   X
Failure
   |
   v
Failure message
```

The failed stage becomes visible in Jenkins.

This helps developers and DevOps engineers identify where the pipeline stopped.

---

# 35. Artifact Archiving

Jenkins archives:

```text
application/services/user-service/target/*.jar
```

This provides build-level artifact traceability.

For example:

```text
Build #25
 |
 +-- user-service-1.0.0.jar
```

This is useful during troubleshooting and historical build investigation.

---

# 36. Complete Enterprise CI Architecture

```text
                         +----------------+
                         |     Developer  |
                         +-------+--------+
                                 |
                                 | git push
                                 v
                         +----------------+
                         |      Git       |
                         +-------+--------+
                                 |
                                 v
                         +----------------+
                         |    Jenkins     |
                         +-------+--------+
                                 |
             +-------------------+-------------------+
             |                   |                   |
             v                   v                   v
        Maven Build         SonarQube             Trivy
             |                   |                   |
             v                   v                   v
          Tests             Quality Gate        Security Scan
             |                   |                   |
             +-------------------+-------------------+
                                 |
                                 v
                           Identify JAR
                                 |
                         release/* only
                                 |
                    +------------+------------+
                    |                         |
                    v                         v
                  Nexus                  Docker Build
                                              |
                                              v
                                         Trivy Image
                                              |
                                              v
                                         Docker Hub
```

---

# 37. Branch-Based Behavior

| Branch      |                 Maven | SonarQube | Quality Gate | Trivy FS |        Nexus | Docker Build |  Image Scan |    Docker Push |
| ----------- | --------------------: | --------: | -----------: | -------: | -----------: | -----------: | ----------: | -------------: |
| `feature/*` |                   Yes |       Yes |          Yes |      Yes |           No |           No |          No |             No |
| `develop`   |                   Yes |       Yes |          Yes |      Yes |           No |           No |          No |             No |
| `release/*` |                   Yes |       Yes |          Yes |      Yes |      Planned |          Yes |         Yes |            Yes |
| `main`      | Future promotion flow |    Future |       Future |   Future | Release flow |  GitOps flow | GitOps flow | Existing image |

The exact `main` production behavior will be implemented through the later release/GitOps architecture rather than mixing production deployment into ECOM-009.

---

# 38. Release Example

Suppose the application version is:

```text
1.0.0
```

The release branch is:

```text
release/v1.0.0
```

The expected artifact chain becomes:

```text
Git
 |
 +-- release/v1.0.0
 |
 v
Maven
 |
 +-- version = 1.0.0
 |
 v
JAR
 |
 +-- user-service-1.0.0.jar
 |
 +--> Nexus
 |      |
 |      +-- com.shopsphere:user-service:1.0.0
 |
 +--> Docker Build
        |
        +-- dockerperala/shopsphere-user-service:1.0.0
                |
                v
             Trivy
                |
                v
            Docker Hub
```

This provides artifact traceability from source code to container image.

---

# 39. What Happens When a Developer Pushes Code?

Example:

```bash
git checkout feature/ECOM-009-some-change

git add .

git commit -m "ECOM-009: update user service"

git push origin feature/ECOM-009-some-change
```

Jenkins detects the SCM change according to the configured job/webhook mechanism.

Pipeline starts:

```text
Checkout
   ↓
Version
   ↓
Maven
   ↓
Tests
   ↓
SonarQube
   ↓
Quality Gate
   ↓
Trivy
   ↓
JAR
```

If any blocking stage fails:

```text
Pipeline = FAILED
```

The developer fixes the problem and pushes another commit.

---

# 40. Example Failure Scenarios

## Scenario 1 — Unit Test Failure

```text
Maven Test
    |
    X
Test failed
```

Result:

```text
Pipeline FAILED
```

Docker release stages do not execute.

---

## Scenario 2 — SonarQube Quality Gate Failure

```text
SonarQube
    |
    v
Quality Gate
    |
    X
FAILED
```

Result:

```text
Pipeline FAILED
```

Release processing stops.

---

## Scenario 3 — Trivy Filesystem Finding

```text
Trivy FS
   |
   X
HIGH/CRITICAL finding
```

Because:

```text
--exit-code 1
```

the pipeline fails.

---

## Scenario 4 — Docker Image Vulnerability

```text
Docker Build
     |
     v
Trivy Image
     |
     X
Blocking vulnerability
```

Docker Hub push is not reached.

This creates a security checkpoint before publishing the image.

---

# 41. Why We Added Multiple Quality Gates

The pipeline uses multiple validation layers.

```text
Layer 1
Maven
   |
   +--> Does the application build and test successfully?

Layer 2
SonarQube
   |
   +--> Is the code quality acceptable?

Layer 3
Trivy Filesystem
   |
   +--> Are there blocking security issues in the source/build context?

Layer 4
Trivy Image
   |
   +--> Is the final container image acceptable?
```

Each layer addresses a different risk.

---

# 42. Shift-Left Security

Security is introduced before deployment.

Traditional approach:

```text
Develop
   ↓
Build
   ↓
Deploy
   ↓
Security review
```

Shift-left approach:

```text
Develop
   ↓
Build
   ↓
Test
   ↓
Code Quality
   ↓
Security Scan
   ↓
Package
   ↓
Release
```

The ShopSphere pipeline follows the second approach.

---

# 43. Traceability

The pipeline establishes relationships between:

```text
Git Commit
     |
     v
Branch
     |
     v
Jenkins Build
     |
     v
Maven Version
     |
     v
JAR
     |
     v
Docker Image
     |
     v
Release
```

Example:

```text
Git commit
abc123
   |
   v
Jenkins Build #42
   |
   v
Version 1.0.0
   |
   v
user-service-1.0.0.jar
   |
   v
shopsphere-user-service:1.0.0
```

This is essential during production incidents and rollback investigations.

---

# 44. What We Intentionally Did NOT Add

ECOM-009 is CI, not the complete production delivery platform.

We intentionally did not add:

### Tomcat deployment

ShopSphere user-service produces a Spring Boot JAR.

Therefore:

```text
No WAR
No Tomcat deployment
```

---

### Production deployment

This pipeline does not deploy directly to production.

Future architecture:

```text
Jenkins
   |
   v
Build / Validate / Publish
   |
   v
GitOps
   |
   v
Kubernetes
   |
   v
Production
```

---

### Nexus implementation

The Nexus stage is currently a placeholder because the ShopSphere Nexus server/repository configuration has not yet been finalized.

---

### Kubernetes deployment

Kubernetes deployment belongs to the later Kubernetes/Helm/GitOps stages.

---

### Production promotion

Production promotion will follow the enterprise release flow rather than allowing every CI build to deploy directly.

---

# 45. Enterprise Release Flow

The target ShopSphere lifecycle is:

```text
feature/ECOM-xxx
        |
        v
     develop
        |
        v
 release/vX.Y.Z
        |
        v
       main
        |
        v
   Production
```

CI validates code.

Release automation packages and publishes artifacts.

GitOps controls production deployment.

---

# 46. Why CI and CD Are Separated

CI answers:

> "Is this code buildable, testable, secure, and releasable?"

CD answers:

> "Should this approved artifact be deployed to an environment?"

ShopSphere separates these responsibilities.

```text
CI
 |
 +-- Build
 +-- Test
 +-- Quality
 +-- Security
 +-- Package
 +-- Publish
 |
 v
Artifact

        ↓

CD / GitOps
 |
 +-- Deployment configuration
 +-- Environment promotion
 +-- Kubernetes
 +-- Rollout
 +-- Rollback
```

This separation improves control and auditability.

---

# 47. Enterprise Principles Implemented

ECOM-009 introduces several important DevOps principles.

### 1. Pipeline as Code

Jenkinsfile is version controlled.

### 2. Automated Testing

Maven tests execute automatically.

### 3. Automated Code Quality

SonarQube analysis executes automatically.

### 4. Quality Gate Enforcement

Failed quality gates stop progression.

### 5. Shift-Left Security

Trivy scans execute during CI.

### 6. Immutable Versioning

Release Docker images use application versions.

### 7. Credential Management

Docker credentials are stored in Jenkins Credentials.

### 8. Build Traceability

JAR artifacts are archived.

### 9. Branch-Aware Automation

Release-only activities are controlled using branch conditions.

### 10. CI/CD Separation

Production deployment is not coupled directly to CI.

---

# 48. Validation Checklist

Before considering ECOM-009 complete, validate:

```text
[ ] Jenkinsfile committed
[ ] Jenkinsfile pushed
[ ] Jenkins job can checkout repository
[ ] JDK 17 resolved
[ ] Maven resolved
[ ] Maven tests pass
[ ] JUnit results published
[ ] SonarQube analysis completes
[ ] SonarQube Quality Gate passes
[ ] Trivy filesystem scan passes
[ ] JAR detected
[ ] Release branch condition works
[ ] Docker build works on release branch
[ ] Trivy image scan works
[ ] Docker credentials resolve
[ ] Docker image push works
[ ] JAR artifact archived
[ ] Jenkins console logs verified
```

---

# 49. Troubleshooting Matrix

| Problem                 | Likely Cause                        | Investigation                           |
| ----------------------- | ----------------------------------- | --------------------------------------- |
| JDK not found           | Jenkins tool configuration          | Check `jdk17`                           |
| Maven not found         | Tool configuration                  | Check `maven3`                          |
| Tests fail              | Application/test issue              | Read Maven output                       |
| JUnit not visible       | Surefire XML missing                | Check `target/surefire-reports`         |
| SonarQube unavailable   | Server/configuration issue          | Check SonarQube Jenkins configuration   |
| Quality Gate timeout    | Webhook/connectivity issue          | Check SonarQube webhook                 |
| Trivy fails             | Vulnerability/secret/config finding | Review Trivy output                     |
| JAR missing             | Maven build/package issue           | Inspect `target/`                       |
| Docker build fails      | Dockerfile/build context            | Run Docker build manually               |
| Docker scan fails       | Image vulnerability                 | Review Trivy image report               |
| Docker login fails      | Jenkins credential issue            | Verify `docker-cred`                    |
| Docker push fails       | Registry/repository permission      | Check Docker Hub credentials/repository |
| Nexus stage unavailable | Nexus not configured                | Configure ShopSphere Nexus later        |

---

# 50. Security Considerations

The current lab environment is designed for hands-on enterprise practice.

For production, additional controls should be applied.

## Jenkins access

Current lab security group allows:

```text
SSH 22
Jenkins 8080
```

from broad CIDR ranges.

Production should restrict access through mechanisms such as:

* Trusted corporate IP ranges
* VPN
* Bastion
* Private networking
* Controlled ingress

---

## Credentials

Never store:

```text
Passwords
Tokens
API keys
Private keys
```

inside the Git repository.

Use:

```text
Jenkins Credentials
AWS IAM roles
Secret management systems
```

as appropriate.

---

# 51. Current Implementation Boundary

ECOM-009 currently establishes the CI foundation.

```text
                ECOM-009
                   |
                   v
        +----------------------+
        |     Jenkins CI       |
        +----------------------+
        | Git Checkout         |
        | Maven Test           |
        | SonarQube            |
        | Quality Gate         |
        | Trivy FS             |
        | JAR Identification   |
        +----------------------+
                   |
             release/*
                   |
        +----------+----------+
        |                     |
        v                     v
      Nexus             Docker Build
                              |
                              v
                         Trivy Image
                              |
                              v
                         Docker Hub
```

Future milestones extend this foundation into:

```text
CI
 |
 v
Artifact
 |
 v
GitOps
 |
 v
Kubernetes
 |
 v
Deployment
 |
 v
Monitoring
 |
 v
Observability
```

---

# 52. Git Checkpoint

Implementation commit:

```text
34d796a ECOM-009: add Jenkins CI pipeline
```

Branch:

```text
feature/ECOM-009-jenkins-ci
```

The implementation has been pushed to:

```text
origin/feature/ECOM-009-jenkins-ci
```

Important:

> ECOM-009 should not be merged into `develop` until the Jenkins pipeline has been executed and its real build evidence has been captured.

---

# 53. Evidence to Capture

Once Jenkins executes the pipeline successfully, capture:

```text
1. Jenkins job name
2. Build number
3. Git commit
4. Branch
5. Maven version
6. Maven test result
7. JUnit result
8. SonarQube result
9. Quality Gate result
10. Trivy filesystem result
11. JAR artifact
12. Release branch Docker build result
13. Trivy image result
14. Docker Hub push result
15. Jenkins final status
```

These should be added to the final ECOM-009 implementation documentation.

---

# 54. Interview Explanation

### Question:

**How did you implement Jenkins CI for ShopSphere?**

### Answer:

I implemented Jenkins Pipeline as Code using a root-level `Jenkinsfile`.

The pipeline starts by checking out the source code and resolving the application version directly from the Maven POM. It then executes Maven clean test and publishes JUnit results.

After successful testing, the pipeline performs SonarQube static analysis and waits for the Quality Gate. We also integrated Trivy for filesystem security scanning.

The generated Spring Boot JAR is identified and archived by Jenkins.

For release branches, additional stages are enabled to publish the artifact to Nexus, build the Docker image using the Maven application version, scan the image with Trivy, and push the immutable versioned image to Docker Hub using Jenkins-managed credentials.

We deliberately separated CI from production deployment. Jenkins validates and publishes artifacts, while the later GitOps workflow will control Kubernetes deployment and production promotion.

---

# 55. Senior-Level Interview Question

### Why don't you build and push Docker images for every feature branch?

Because CI validation and release artifact publication have different responsibilities.

Feature branches need fast feedback:

```text
Build
Test
Quality
Security
```

Release branches represent an intentional release candidate and therefore perform:

```text
Artifact publication
Docker build
Image security scan
Registry push
```

This reduces unnecessary registry artifacts and gives the release process a controlled boundary.

---

# 56. Senior-Level Interview Question

### Why is the Maven POM used as the version source?

The Maven POM is the application's build metadata and version source.

Using it consistently allows the same version to identify:

```text
Application
   |
   +-- JAR
   |
   +-- Nexus artifact
   |
   +-- Docker image
```

This avoids version drift between application artifacts and container images.

---

# 57. Senior-Level Interview Question

### What happens if SonarQube passes but Trivy fails?

The pipeline still stops.

Quality and security are separate gates.

```text
SonarQube
   |
   +-- PASS
   |
   v
Trivy
   |
   +-- FAIL
   |
   v
Pipeline FAILED
```

Passing one gate does not override another.

---

# 58. Senior-Level Interview Question

### Why use both filesystem and image scanning?

They scan different points in the lifecycle.

Filesystem scanning:

```text
Source / dependencies / configuration
```

Image scanning:

```text
Final container image
```

The final Docker image can contain additional OS-level packages or layers, so validating both stages provides broader coverage.

---

# 59. Senior-Level Interview Question

### Why use Jenkins Credentials instead of putting Docker credentials in the Jenkinsfile?

Credentials are sensitive information.

Hardcoding them creates the risk of exposing secrets through:

* Git history
* Pull requests
* Jenkinsfile
* Logs

Jenkins Credentials provides controlled secret injection without storing the secret directly in source code.

---

# 60. Final Architecture Decision

The ECOM-009 implementation establishes this ShopSphere CI model:

```text
Developer
    |
    v
Git
    |
    v
Jenkins
    |
    +--> Maven Build/Test
    |
    +--> JUnit Reports
    |
    +--> SonarQube
    |
    +--> Quality Gate
    |
    +--> Trivy Filesystem
    |
    +--> JAR Artifact
    |
    +--> release/* only
             |
             +--> Nexus
             |
             +--> Docker Build
             |
             +--> Trivy Image
             |
             +--> Docker Hub
```

The design intentionally keeps:

```text
CI
≠
Production Deployment
```

Production deployment will be handled by the later GitOps/Kubernetes architecture.

---

# 61. ECOM-009 Completion Criteria

ECOM-009 is considered technically complete only after:

```text
Jenkinsfile
      ↓
Jenkins Job
      ↓
Successful CI Execution
      ↓
Evidence Captured
      ↓
ECOM-009 README Updated
      ↓
Documentation Commit
      ↓
Push
      ↓
Merge feature → develop
      ↓
Validate develop
```

The implementation commit alone is **not** the final milestone checkpoint.

The real Jenkins execution evidence completes the milestone.

---

# 62. Enterprise Takeaway

The main purpose of ECOM-009 is not simply:

> "We created a Jenkinsfile."

The actual engineering outcome is:

> **We established a repeatable, version-controlled CI quality and security pipeline for the ShopSphere user-service, with branch-aware release artifact handling, immutable Docker versioning, credential isolation, build traceability, and a clear separation between CI and future GitOps-based deployment.**

That is the enterprise-level objective of ECOM-009.
