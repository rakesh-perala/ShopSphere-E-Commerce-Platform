# ECOM-009 — ShopSphere User Service Jenkins CI Pipeline

## 1. Document Information

| Field                | Details                                         |
| -------------------- | ----------------------------------------------- |
| Project              | ShopSphere E-Commerce Platform                  |
| Jira Ticket          | ECOM-009                                        |
| Component            | User Service                                    |
| CI Tool              | Jenkins                                         |
| Build Tool           | Maven                                           |
| Programming Language | Java 17                                         |
| Code Quality         | SonarQube                                       |
| Security Scanner     | Trivy                                           |
| Artifact             | Executable JAR                                  |
| Pipeline Type        | Continuous Integration                          |
| Jenkinsfile          | `application/services/user-service/Jenkinsfile` |
| CI Job               | `ShopSphere-User-Service-CI`                    |
| Branch               | `feature/ECOM-009-jenkins-ci`                   |

---

# 2. Business Requirement

ShopSphere is a microservices-based e-commerce application.

Developers continuously make changes to services such as:

* User Service
* Product Service
* Cart Service
* Order Service
* Payment Service
* Notification Service

Every code change must be automatically validated before it is allowed to move toward release.

The CI pipeline provides an automated quality and security validation process.

Instead of developers manually performing:

```text
git pull
mvn clean package
mvn test
SonarQube scan
security scan
check generated JAR
```

Jenkins performs these activities consistently.

The objective is:

> Every successful CI build must produce a validated application artifact that can safely be consumed by the Release pipeline.

---

# 3. Why CI Is Required

Without CI:

```text
Developer
   |
   | writes code
   ↓
Git
   |
   ↓
Developer manually builds
   |
   ↓
Developer manually tests
   |
   ↓
Developer manually checks quality
   |
   ↓
Developer manually checks security
   |
   ↓
Artifact
```

This creates several problems:

* Manual mistakes
* Inconsistent environments
* Tests may be skipped
* Security scans may be forgotten
* Quality problems may reach later environments
* Developers may build different artifacts
* No standardized audit trail

With Jenkins CI:

```text
Developer
   |
   ↓
Git
   |
   ↓
Jenkins
   |
   ├── Checkout
   ├── Build
   ├── Test
   ├── SonarQube
   ├── Quality Gate
   ├── Trivy
   └── Artifact
```

The process becomes repeatable and auditable.

---

# 4. CI vs Release vs CD

This distinction is extremely important in an enterprise DevOps environment.

## CI

CI answers:

> "Is this source code healthy enough to become a release artifact?"

Our ECOM-009 CI pipeline:

```text
Checkout
   ↓
Maven Build
   ↓
Unit Tests
   ↓
SonarQube
   ↓
Quality Gate
   ↓
Trivy Filesystem Scan
   ↓
Identify Artifact
   ↓
Archive Artifact
```

---

## Release

Release answers:

> "How do we promote the validated artifact and package it for distribution?"

ECOM-010 will handle:

```text
Validated CI Artifact
   ↓
Release Version
   ↓
Nexus
   ↓
Docker Build
   ↓
Trivy Image Scan
   ↓
ECR
```

---

## CD

CD answers:

> "How do we deploy the released artifact?"

Later:

```text
ECR
   ↓
GitOps Repository
   ↓
Helm
   ↓
Argo CD
   ↓
EKS
```

Therefore:

```text
CI ≠ Release ≠ CD
```

Keeping these responsibilities separate makes the platform easier to operate and troubleshoot.

---

# 5. ECOM-009 Architecture

```text
                    Developer
                        |
                        | git push
                        ↓
               GitHub Repository
                        |
                        ↓
             Jenkins CI Pipeline
                        |
             ┌──────────┴──────────┐
             │                     │
             ↓                     ↓
        Maven Build/Test      Source Checkout
             |
             ↓
        SonarQube Analysis
             |
             ↓
       SonarQube Quality Gate
             |
             ↓
       Trivy Filesystem Scan
             |
             ↓
       Executable JAR
             |
             ↓
       Jenkins Artifact Store
             |
             ↓
       Release Pipeline
            ECOM-010
```

---

# 6. Repository Structure

Relevant structure:

```text
ShopSphere-E-Commerce-Platform/
│
├── application/
│   └── services/
│       └── user-service/
│           │
│           ├── src/
│           │   ├── main/
│           │   └── test/
│           │
│           ├── pom.xml
│           ├── Dockerfile
│           └── Jenkinsfile
│
├── jenkins/
│   └── release/
│       └── user-service/
│           └── Jenkinsfile
│
└── docs/
    └── jira/
        └── ECOM-009/
            └── README.md
```

Important:

```text
application/services/user-service/Jenkinsfile
```

is the CI pipeline.

Later:

```text
jenkins/release/user-service/Jenkinsfile
```

will be the Release pipeline.

This prevents Jenkins from confusing CI and Release responsibilities.

---

# 7. Jenkins CI Pipeline

The current pipeline is:

```text
1. Git Checkout
        ↓
2. Resolve Application Version
        ↓
3. Maven Build & Test
        ↓
4. SonarQube Analysis
        ↓
5. SonarQube Quality Gate
        ↓
6. Trivy Filesystem Scan
        ↓
7. Identify Build Artifact
        ↓
8. Archive Artifact
```

---

# 8. Stage 1 — Git Checkout

```groovy
stage('Git Checkout') {
    steps {
        checkout scm
    }
}
```

## What does it do?

Jenkins obtains the source code from Git.

The Jenkins job knows:

```text
Repository
Branch
Credentials
SCM configuration
```

Jenkins checks out the exact revision associated with the build.

Example:

```text
feature/ECOM-009-jenkins-ci
        |
        ↓
ae030b4...
```

## Why do we need this stage?

The Jenkins agent needs the source code before Maven, SonarQube, or Trivy can execute.

Without checkout:

```text
Jenkins Workspace
        |
        └── no application source
```

Therefore all later stages would fail.

---

# 9. Stage 2 — Resolve Application Version

```groovy
stage('Resolve Application Version') {
    steps {
        dir("${SERVICE_DIR}") {
            script {
                env.APP_VERSION = sh(
                    script: '''
                        mvn help:evaluate \
                          -Dexpression=project.version \
                          -q \
                          -DforceStdout
                    ''',
                    returnStdout: true
                ).trim()
            }
        }
    }
}
```

## What does it do?

It reads the application version from:

```text
pom.xml
```

Current version:

```text
0.0.1-SNAPSHOT
```

Maven command:

```bash
mvn help:evaluate \
  -Dexpression=project.version \
  -q \
  -DforceStdout
```

returns:

```text
0.0.1-SNAPSHOT
```

## Why do we need it?

The pipeline should not manually guess the application version.

The source repository is the source of truth.

This becomes important later when Release uses:

```text
1.0.0
1.0.1
1.1.0
```

etc.

---

# 10. Stage 3 — Maven Build & Test

```groovy
stage('Maven Build & Test') {
```

The important command is:

```bash
mvn clean package -DskipTests=false
```

## What happens?

Maven performs:

```text
clean
  ↓
compile
  ↓
test
  ↓
package
```

The resulting artifact is:

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

---

# 11. Why `clean`?

```bash
mvn clean
```

removes the previous build output.

Usually:

```text
target/
```

is removed.

This prevents stale build files from affecting the new build.

Example:

```text
Previous build
target/
   old classes
   old JAR
   old generated files
```

After:

```bash
mvn clean
```

the old output is removed.

Then the application is rebuilt.

---

# 12. Why `package`?

Maven's `package` lifecycle creates the deployable artifact.

For this Spring Boot service:

```text
source code
     ↓
compile
     ↓
test
     ↓
package
     ↓
JAR
```

Result:

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

---

# 13. Why `-DskipTests=false`?

We explicitly tell Maven:

```text
Do NOT skip tests.
```

This makes the CI intention obvious.

We want:

```text
Build + Test
```

not:

```text
Build only
```

The pipeline output confirms:

```text
Tests run: 15
Failures: 0
Errors: 0
Skipped: 0
```

Therefore:

```text
15 / 15 tests passed
```

---

# 14. Why JUnit Results Are Published?

The pipeline uses:

```groovy
junit(
    testResults: '**/target/surefire-reports/*.xml',
    allowEmptyResults: true
)
```

Maven Surefire generates XML test reports.

Jenkins reads those reports.

This gives us:

```text
Total Tests
Passed
Failed
Skipped
Error
```

inside Jenkins.

This is much better than reading raw console output.

---

# 15. Stage 4 — SonarQube Analysis

```groovy
stage('SonarQube Analysis') {
```

The pipeline uses:

```groovy
withSonarQubeEnv('SonarQube')
```

and:

```bash
sonar-scanner
```

## What does SonarQube do?

SonarQube analyzes source code for maintainability and code-quality issues.

It can identify things such as:

* Bugs
* Code smells
* Security issues
* Duplicated code
* Maintainability problems
* Reliability issues

It is a static analysis tool.

---

# 16. Why SonarQube Is After Maven Build?

The pipeline performs:

```text
Maven Build/Test
       ↓
SonarQube
```

The build produces:

```text
target/classes
```

SonarQube receives:

```text
-Dsonar.java.binaries=target/classes
```

This allows SonarQube to analyze Java source together with compiled bytecode.

---

# 17. SonarQube Project Configuration

Current configuration:

```text
Project Name:
shopsphere-user-service

Project Key:
shopsphere-user-service

Sources:
src/main/java

Java Binaries:
target/classes
```

The project key uniquely identifies the service in SonarQube.

For another service:

```text
shopsphere-product-service
shopsphere-cart-service
shopsphere-order-service
```

we can use separate SonarQube projects.

---

# 18. Stage 5 — SonarQube Quality Gate

```groovy
stage('SonarQube Quality Gate') {
```

The key command is:

```groovy
waitForQualityGate(
    abortPipeline: true
)
```

## What is a Quality Gate?

SonarQube performs analysis.

The Quality Gate determines whether the configured quality conditions are satisfied.

Conceptually:

```text
SonarQube Analysis
        ↓
Quality Gate
        |
        ├── OK → continue
        |
        └── Failed → stop pipeline
```

---

# 19. Why `waitForQualityGate`?

SonarQube analysis is asynchronous.

Jenkins sends the analysis:

```text
Jenkins
   ↓
SonarQube
```

SonarQube processes it.

Jenkins then waits for the result:

```text
PENDING
   ↓
SUCCESS
   ↓
QUALITY GATE = OK
```

The Jenkins–SonarQube webhook is therefore important.

Current ECOM-009 validation confirmed:

```text
SonarQube Analysis: SUCCESS
Quality Gate: OK
```

---

# 20. Why `abortPipeline: true`?

```groovy
abortPipeline: true
```

means:

```text
Quality Gate failed
       ↓
STOP CI
```

We don't want:

```text
Bad quality
   ↓
Trivy
   ↓
Artifact
   ↓
Release
```

Instead:

```text
Bad quality
   ↓
Pipeline STOP
```

This creates a quality-control boundary.

---

# 21. Stage 6 — Trivy Filesystem Scan

```groovy
stage('Trivy Filesystem Scan') {
```

The pipeline executes:

```bash
trivy fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  --exit-code 1 \
  --no-progress \
  .
```

---

# 22. Why Trivy Filesystem Scanning?

At this stage we are scanning the source/build directory.

Trivy checks for:

```text
Vulnerabilities
Secrets
Misconfigurations
```

The pipeline explicitly scans:

```text
vuln
secret
misconfig
```

---

# 23. Why HIGH and CRITICAL?

The CI policy currently focuses on:

```text
HIGH
CRITICAL
```

This prevents the pipeline from being blocked by every informational or low-severity finding.

The policy can be tightened later based on organizational security standards.

---

# 24. Why `--exit-code 1`?

This is critical.

```bash
--exit-code 1
```

means:

```text
Finding detected at configured severity
             ↓
Trivy returns exit code 1
             ↓
Jenkins stage fails
             ↓
Pipeline stops
```

Without this, Trivy could simply print vulnerabilities while Jenkins still reports:

```text
SUCCESS
```

That would make the security scan informational instead of enforcing policy.

---

# 25. Why `--no-progress`?

Jenkins console logs should remain clean.

Therefore:

```bash
--no-progress
```

prevents unnecessary progress-bar output.

This makes CI logs easier to read.

---

# 26. Current Trivy Result

The ECOM-009 CI validation showed:

```text
pom.xml
Vulnerabilities: 0

Dockerfile
Misconfigurations: 0
```

No HIGH/CRITICAL findings caused the pipeline to fail.

Therefore the stage completed successfully.

---

# 27. Stage 7 — Identify Build Artifact

The pipeline searches for:

```text
*.jar
```

while excluding:

```text
*.jar.original
```

Example:

```text
target/
├── user-service-0.0.1-SNAPSHOT.jar
└── user-service-0.0.1-SNAPSHOT.jar.original
```

The executable artifact is:

```text
user-service-0.0.1-SNAPSHOT.jar
```

The `.jar.original` file is excluded.

---

# 28. Why Explicit Artifact Identification?

We don't want the Release pipeline to guess which JAR should be released.

The CI pipeline explicitly identifies:

```text
JAR_FILE
```

Example:

```text
target/user-service-0.0.1-SNAPSHOT.jar
```

If no executable JAR exists:

```text
ERROR: No executable JAR found.
```

and the pipeline fails.

This is safer than blindly continuing.

---

# 29. Stage 8 — Archive Artifact

The pipeline archives:

```groovy
archiveArtifacts(
    artifacts: "${SERVICE_DIR}/target/*.jar",
    allowEmptyArchive: true
)
```

The JAR becomes available from the Jenkins build record.

Conceptually:

```text
CI Build #25
   |
   └── user-service-0.0.1-SNAPSHOT.jar
```

Later Release can consume the artifact from the successful CI build.

---

# 30. Why Archive the Artifact?

This is one of the most important enterprise CI concepts.

We want:

```text
Source
  ↓
Build
  ↓
Test
  ↓
Quality
  ↓
Security
  ↓
Artifact
```

The artifact produced by CI becomes the trusted output of that build.

The Release pipeline should consume that artifact rather than rebuilding source code.

---

# 31. Artifact Promotion Concept

Bad approach:

```text
CI
 ↓
Build JAR A

Release
 ↓
Build JAR B
```

Now:

```text
JAR A != JAR B
```

Potentially, the released artifact is not exactly what CI validated.

Preferred approach:

```text
CI
 ↓
Build JAR A
 ↓
Test
 ↓
SonarQube
 ↓
Quality Gate
 ↓
Trivy
 ↓
Archive JAR A
        |
        ↓
Release
        |
        ↓
Promote SAME JAR A
```

This provides artifact traceability.

---

# 32. Complete CI Pipeline

```text
                   Git Push
                      |
                      ↓
                Jenkins CI
                      |
                      ↓
               Git Checkout
                      |
                      ↓
          Resolve Application Version
                      |
                      ↓
             Maven Build & Test
                      |
              ┌───────┴───────┐
              │               │
           Build OK        Test Failed
              │               │
              ↓               X
          SonarQube
              |
              ↓
        Quality Gate
          /       \
        OK        FAILED
        |            |
        ↓            X
      Trivy
        |
        ↓
   Artifact Identify
        |
        ↓
   Archive JAR
        |
        ↓
   CI SUCCESS
```

---

# 33. Jenkins Tools

The Jenkins pipeline uses:

```groovy
tools {
    jdk 'jdk17'
    maven 'maven3'
}
```

Configured tools:

| Tool          | Jenkins Name        | Purpose                       |
| ------------- | ------------------- | ----------------------------- |
| JDK           | `jdk17`             | Compile/test Java application |
| Maven         | `maven3`            | Build/test application        |
| Sonar Scanner | `sonar-scanner`     | Static analysis               |
| Trivy         | System installation | Security scanning             |

The Jenkins controller runtime may use a different Java version.

That does not mean the application must use the same Java version.

For this project:

```text
Jenkins Runtime → Java 21
Application Build → Java 17
```

This separation is intentional.

---

# 34. Why Java 17?

The application's `pom.xml` specifies:

```xml
<java.version>17</java.version>
```

Therefore Jenkins uses the JDK 17 tool:

```text
jdk17
```

This prevents "works on my machine" differences between developer and CI environments.

---

# 35. Current Build Evidence

The successful ECOM-009 CI execution produced:

```text
Java:
17.0.19

Maven:
3.9.16

Application Version:
0.0.1-SNAPSHOT

Tests:
15

Failures:
0

Errors:
0

Skipped:
0

Artifact:
user-service-0.0.1-SNAPSHOT.jar
```

SonarQube:

```text
Analysis:
SUCCESS

Quality Gate:
OK
```

Trivy:

```text
HIGH:
0

CRITICAL:
0
```

Final result:

```text
Finished: SUCCESS
```

---

# 36. SonarQube Webhook

The Quality Gate stage depends on Jenkins receiving the SonarQube task result.

Architecture:

```text
Jenkins
   |
   | submit analysis
   ↓
SonarQube
   |
   | process analysis
   ↓
Quality Gate
   |
   | webhook
   ↓
Jenkins
   |
   ↓
waitForQualityGate()
```

During ECOM-009 validation:

```text
Task:
PENDING

Then:

Task:
SUCCESS

Quality Gate:
OK
```

Therefore the webhook integration is functioning.

---

# 37. Why Webhook Is Better Than Polling?

Polling means:

```text
Jenkins
   |
   | "Finished?"
   ↓
SonarQube

Jenkins
   |
   | "Finished?"
   ↓
SonarQube
```

repeatedly.

Webhook:

```text
Jenkins → SonarQube

SonarQube → Jenkins
             "Analysis completed"
```

This is more event-driven and efficient.

---

# 38. Environment Variables

The pipeline defines:

```groovy
environment {

    SERVICE_NAME = 'user-service'

    SERVICE_DIR = 'application/services/user-service'

    SCANNER_HOME = tool 'sonar-scanner'
}
```

## `SERVICE_NAME`

Identifies the microservice.

```text
user-service
```

Useful for:

* Logs
* Notifications
* Artifact naming
* Release automation

---

## `SERVICE_DIR`

Defines the service location:

```text
application/services/user-service
```

This allows the Jenkinsfile to remain readable.

Instead of repeatedly writing:

```text
application/services/user-service
```

we use:

```text
${SERVICE_DIR}
```

---

## `SCANNER_HOME`

Jenkins resolves the configured Sonar Scanner installation:

```groovy
SCANNER_HOME = tool 'sonar-scanner'
```

Then:

```bash
${SCANNER_HOME}/bin/sonar-scanner
```

runs the configured scanner.

---

# 39. Pipeline Options

The pipeline contains:

```groovy
options {
    disableConcurrentBuilds()
    timestamps()
    buildDiscarder(...)
}
```

## `disableConcurrentBuilds()`

Prevents two builds of the same job from running simultaneously.

Example:

```text
Build #20 → RUNNING

Developer pushes another change

Build #21 → WAITING
```

This avoids unnecessary workspace/build conflicts.

---

# 40. `timestamps()`

Adds timestamps to Jenkins logs.

Example:

```text
[2026-09-23 10:20:31] Maven Build
[2026-09-23 10:21:08] Tests completed
```

This is useful for:

* Incident investigation
* Performance analysis
* RCA
* Build duration analysis

---

# 41. Build Discarder

The pipeline keeps:

```text
20 builds
```

This prevents unlimited Jenkins storage growth.

Enterprise Jenkins installations should always have retention policies.

Otherwise:

```text
Build #1
Build #2
...
Build #50000
```

can consume significant disk space.

---

# 42. Post Actions

The pipeline has:

```groovy
post {

    success {
        ...
    }

    failure {
        ...
    }

    always {
        archiveArtifacts(...)
    }
}
```

## `success`

Executed when the complete pipeline succeeds.

---

## `failure`

Executed when a stage fails.

This provides a clear CI failure message.

---

## `always`

Runs regardless of success/failure.

Artifact archival is placed here so the build can preserve available artifacts for investigation where appropriate.

---

# 43. Failure Scenarios

## Scenario 1 — Maven Compilation Failure

```text
Maven
  ↓
Compilation error
  ↓
Stage FAILED
  ↓
Pipeline FAILED
```

No release should happen.

---

## Scenario 2 — Unit Test Failure

```text
15 tests
14 passed
1 failed
```

Maven returns failure.

Pipeline stops.

---

## Scenario 3 — SonarQube Analysis Failure

```text
Jenkins
 ↓
SonarQube
 ↓
Analysis failed
```

Pipeline stops.

---

## Scenario 4 — Quality Gate Failure

```text
SonarQube
 ↓
Quality Gate FAILED
 ↓
waitForQualityGate()
 ↓
abortPipeline=true
 ↓
Jenkins FAILED
```

---

## Scenario 5 — Trivy HIGH/CRITICAL Finding

```text
Trivy
 ↓
HIGH vulnerability
 ↓
exit-code 1
 ↓
Jenkins FAILED
```

---

## Scenario 6 — JAR Missing

```text
Build succeeds
       ↓
target/*.jar missing
       ↓
Identify Artifact
       ↓
ERROR
       ↓
Pipeline FAILED
```

This protects downstream Release automation.

---

# 44. RCA Example — Jenkins Tool Configuration

Earlier Jenkins failure:

```text
Tool type "maven" does not have an install of "maven3" configured
```

## Root Cause

The Jenkinsfile requested:

```groovy
maven 'maven3'
```

but Jenkins did not have a Maven installation with that exact name.

## Resolution

Configure:

```text
Manage Jenkins
   ↓
Tools
   ↓
Maven installations
   ↓
Name: maven3
```

Then Jenkins successfully resolved:

```text
maven3
```

Current successful Maven:

```text
3.9.16
```

---

# 45. RCA Example — Jenkinsfile Syntax

Earlier failure:

```text
No such DSL method 'Jenkins'
```

## Root Cause

The Jenkinsfile had invalid pipeline syntax/content at the beginning of the file.

Jenkins interpreted:

```text
Jenkins
```

as a pipeline DSL method.

## Resolution

The Jenkinsfile was corrected to use valid Declarative Pipeline syntax:

```groovy
pipeline {

    agent any

    ...
}
```

---

# 46. RCA Example — Quality Gate Waiting

If the pipeline remains at:

```text
SonarQube Quality Gate
```

check:

```text
1. SonarQube analysis
2. SonarQube project
3. Jenkins SonarQube configuration
4. Webhook
5. Jenkins URL
6. SonarQube task status
```

The important relationship is:

```text
SonarQube
   |
   └── webhook
          |
          ↓
       Jenkins
```

---

# 47. Security Considerations

Never put secrets directly in the Jenkinsfile.

Bad:

```groovy
NEXUS_PASSWORD = 'mypassword'
```

Bad:

```groovy
AWS_SECRET_ACCESS_KEY = 'xxxxxxxx'
```

Use Jenkins Credentials.

Example:

```groovy
withCredentials(...)
```

Credentials should be referenced by ID.

---

# 48. Immutable Artifact Principle

CI should produce a specific artifact.

Example:

```text
user-service-0.0.1-SNAPSHOT.jar
```

Later Release should promote that exact artifact.

Do not:

```text
CI → build
Release → rebuild
```

Prefer:

```text
CI → build once
CI → validate
Release → promote
```

This provides:

* Traceability
* Reproducibility
* Auditability
* Easier rollback

---

# 49. Why We Don't Push Docker Image in ECOM-009

Docker publishing is intentionally outside this CI pipeline.

ECOM-009 is responsible for:

```text
Source Validation
```

not:

```text
Release Distribution
```

Docker image publication belongs to ECOM-010.

Therefore ECOM-009 does not contain:

```text
docker push
ECR
Nexus publishing
release tagging
deployment
```

---

# 50. Why We Don't Deploy in CI

CI should not directly deploy production workloads.

The separation is:

```text
CI
 ↓
Validated Artifact
 ↓
Release
 ↓
Container Registry
 ↓
GitOps
 ↓
Deployment
```

This allows organizations to introduce:

```text
approval
change management
release windows
environment promotion
rollback
```

without modifying source validation.

---

# 51. Enterprise Branch Strategy

ShopSphere follows:

```text
feature/ECOM-xxx
        ↓
develop
        ↓
release/vX.Y.Z
        ↓
main
        ↓
production
```

Example:

```text
feature/ECOM-009-jenkins-ci
        ↓
develop
        ↓
release/v1.0.0
        ↓
main
```

CI validates changes early.

Release handles controlled promotion.

---

# 52. Jenkins Job Separation

Recommended Jenkins jobs:

```text
ShopSphere-User-Service-CI
        |
        | Jenkinsfile:
        | application/services/user-service/Jenkinsfile
        |
        ↓
ShopSphere-User-Service-Release
        |
        | Jenkinsfile:
        | jenkins/release/user-service/Jenkinsfile
```

This makes the responsibility of each job obvious.

---

# 53. CI Output Contract

The ECOM-009 pipeline produces:

```text
CI Build
   |
   ├── Git revision
   ├── Test results
   ├── SonarQube analysis
   ├── Quality Gate result
   ├── Trivy result
   └── Executable JAR
```

The most important output is:

```text
Validated JAR
```

The Release pipeline will consume this output.

---

# 54. Current CI Contract

For `user-service`:

```text
Source:
application/services/user-service

Build:
Maven

Java:
17

Artifact:
target/user-service-0.0.1-SNAPSHOT.jar

Quality:
SonarQube

Security:
Trivy

Artifact Storage:
Jenkins archive

Release Consumer:
ShopSphere-User-Service-Release
```

---

# 55. Production Improvements — Future

The current pipeline is intentionally focused on the ECOM-009 scope.

Future improvements can include:

### Code coverage

Add JaCoCo:

```text
Maven
 ↓
JaCoCo
 ↓
SonarQube
```

This will provide actual test coverage metrics.

---

### Dependency scanning

Add dependency vulnerability analysis.

---

### SBOM

Generate:

```text
Software Bill of Materials
```

using tools such as:

```text
Syft
Trivy
```

---

### Artifact repository integration

Release will publish the validated artifact to:

```text
Nexus
```

---

### Container security

Release will scan the final Docker image:

```text
Docker Image
     ↓
Trivy Image Scan
```

This is different from the ECOM-009 filesystem scan.

---

# 56. Files Modified for ECOM-009

Primary CI file:

```text
application/services/user-service/Jenkinsfile
```

Documentation:

```text
docs/jira/ECOM-009/README.md
```

The CI Jenkinsfile should remain dedicated to CI responsibilities.

---

# 57. Validation Checklist

Before declaring ECOM-009 complete:

```text
[✓] Jenkinsfile syntax valid
[✓] Git checkout successful
[✓] JDK 17 configured
[✓] Maven configured
[✓] Maven build successful
[✓] Unit tests successful
[✓] JUnit reports published
[✓] SonarQube analysis successful
[✓] SonarQube Quality Gate successful
[✓] SonarQube webhook working
[✓] Trivy filesystem scan successful
[✓] Executable JAR identified
[✓] Artifact archived
[✓] Jenkins build SUCCESS
```

Current validation:

```text
Maven Tests:
15/15 PASS

SonarQube:
ANALYSIS SUCCESSFUL

Quality Gate:
OK

Trivy:
No HIGH/CRITICAL findings causing failure

Artifact:
user-service-0.0.1-SNAPSHOT.jar

Pipeline:
SUCCESS
```

---

# 58. ECOM-009 Completion Criteria

ECOM-009 is complete when:

```text
Developer Push
      ↓
Jenkins
      ↓
Build
      ↓
Test
      ↓
SonarQube
      ↓
Quality Gate
      ↓
Trivy
      ↓
Artifact
      ↓
SUCCESS
```

The successful artifact becomes the input for:

```text
ECOM-010
Jenkins Release Pipeline
```

---

# 59. Interview Questions

## Q1. What is CI?

Continuous Integration is the practice of automatically building and validating code changes whenever developers integrate changes into a shared repository.

---

## Q2. Why did you use Jenkins?

Jenkins automates the CI workflow and provides a centralized, repeatable execution environment for build, testing, code-quality validation, security scanning, and artifact management.

---

## Q3. Why Maven?

Maven manages:

```text
Dependencies
Build lifecycle
Compilation
Testing
Packaging
```

for the Java application.

---

## Q4. Why do you run tests before SonarQube?

The application must first compile and produce the required build outputs.

The successful build also provides compiled classes that SonarQube can use during Java analysis.

---

## Q5. What is a SonarQube Quality Gate?

It is a set of configured quality conditions that determines whether analyzed source code satisfies the organization's quality policy.

---

## Q6. Why use `waitForQualityGate()`?

SonarQube analysis is asynchronous.

Jenkins waits for the final Quality Gate result rather than assuming the analysis succeeded.

---

## Q7. Why is a SonarQube webhook required?

The webhook allows SonarQube to notify Jenkins when analysis processing is complete.

---

## Q8. Why use Trivy?

Trivy provides security scanning for vulnerabilities, secrets, and configuration issues.

---

## Q9. Why use `--exit-code 1`?

It converts configured security findings into a pipeline failure.

---

## Q10. Why archive the JAR?

The archive provides a concrete artifact produced by the CI build that can later be promoted by the Release pipeline.

---

## Q11. Why shouldn't Release rebuild the application?

Rebuilding could produce a different artifact.

The preferred enterprise pattern is:

```text
Build once
Test once
Validate once
Promote the same artifact
```

---

## Q12. Why separate CI and Release?

CI validates source code.

Release promotes and publishes the validated artifact.

Separating responsibilities improves:

* Traceability
* Security
* Change control
* Troubleshooting
* Rollback
* Pipeline maintainability

---

## Q13. What happens if SonarQube Quality Gate fails?

Because:

```groovy
abortPipeline: true
```

the pipeline stops and the artifact must not proceed toward release.

---

## Q14. What happens if Trivy finds a HIGH vulnerability?

With:

```bash
--severity HIGH,CRITICAL
--exit-code 1
```

Trivy returns a non-zero exit code and Jenkins marks the stage as failed.

---

## Q15. What artifact did your pipeline produce?

```text
user-service-0.0.1-SNAPSHOT.jar
```

The successful CI build archived this artifact.

---

# 60. Senior-Level Interview Explanation

A strong interview explanation would be:

> "For ShopSphere, I separated CI from Release responsibilities. The CI pipeline checks out the service source, resolves the Maven version, performs a clean Maven build and unit tests, publishes JUnit results, runs SonarQube static analysis, waits for the SonarQube Quality Gate through the Jenkins webhook, performs Trivy filesystem scanning for high and critical vulnerabilities, identifies the executable JAR, and archives the validated artifact. The important design principle is that CI produces a trusted artifact. The Release pipeline should consume that exact artifact instead of rebuilding the application. This gives us traceability, reproducibility, and a clean separation between source validation and artifact promotion."

---

# 61. Final Enterprise Flow

```text
                    DEVELOPER
                        |
                        |
                     Git Push
                        |
                        ↓
              ┌──────────────────┐
              │     JENKINS      │
              │       CI         │
              └──────────────────┘
                        |
                        ↓
                  Git Checkout
                        |
                        ↓
              Resolve App Version
                        |
                        ↓
              Maven Build + Test
                        |
                        ↓
                  JUnit Results
                        |
                        ↓
                SonarQube Analysis
                        |
                        ↓
                 Quality Gate
                   /       \
                 OK         FAIL
                 |            |
                 ↓            X
              Trivy           STOP
             FS Scan
                 |
                 ↓
           Identify JAR
                 |
                 ↓
          Archive Artifact
                 |
                 ↓
             CI SUCCESS
                 |
                 |
                 ↓
        ┌─────────────────────┐
        │       ECOM-010      │
        │   RELEASE PIPELINE  │
        └─────────────────────┘
                 |
                 ↓
          Promote SAME JAR
                 |
                 ↓
               Nexus
                 |
                 ↓
            Docker Image
                 |
                 ↓
           Trivy Image Scan
                 |
                 ↓
                ECR
                 |
                 ↓
              GitOps/CD
```

---

# 62. ECOM-009 Status

```text
ECOM-009 — Jenkins CI
======================

Implementation : COMPLETE
Jenkinsfile    : COMPLETE
Maven Build    : PASS
Unit Tests     : 15/15 PASS
SonarQube      : PASS
Quality Gate   : OK
Webhook        : WORKING
Trivy FS       : PASS
Artifact       : ARCHIVED
Pipeline       : SUCCESS
```

**ECOM-009 CI is now a stable baseline.**

Do not add Nexus, Docker push, ECR, deployment, or Release logic to this Jenkinsfile.

Next:

```text
ECOM-010
Jenkins Release Pipeline
```

will consume the validated CI artifact.
