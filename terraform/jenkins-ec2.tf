data "aws_ami" "ubuntu" {
  most_recent = true
  owners      = ["099720109477"]

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

resource "aws_instance" "jenkins" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.jenkins_instance_type
  subnet_id              = module.vpc.public_subnet_ids[0]
  vpc_security_group_ids = [aws_security_group.jenkins.id]
  key_name               = var.jenkins_key_name

  associate_public_ip_address = true
  user_data_replace_on_change = true

  root_block_device {
    volume_size = var.jenkins_root_volume_size
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = <<-EOF
    #!/bin/bash

    set -euxo pipefail

    exec > >(tee -a /var/log/shopsphere-user-data.log | logger -t shopsphere-user-data -s 2>/dev/console) 2>&1

    export DEBIAN_FRONTEND=noninteractive

    echo "===== ShopSphere Jenkins bootstrap started ====="

    # ============================================================
    # BASE PACKAGE INSTALLATION
    # ============================================================

    apt-get update

    echo "===== Installing base packages ====="

    apt-get install -y \
      ca-certificates \
      curl \
      wget \
      gnupg \
      git \
      unzip \
      fontconfig \
      openjdk-21-jre \
      openjdk-17-jdk \
      maven

    # ============================================================
    # JAVA CONFIGURATION
    # ============================================================

    echo "===== Configuring Java ====="

    JAVA17_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
    JAVA21_HOME="/usr/lib/jvm/java-21-openjdk-amd64"

    # Verify both Java installations exist
    test -x "$JAVA17_HOME/bin/java"
    test -x "$JAVA17_HOME/bin/javac"
    test -x "$JAVA21_HOME/bin/java"

    # Keep Java 21 as the system default.
    # Jenkins controller will run using Java 21.
    update-alternatives --install /usr/bin/java java "$JAVA21_HOME/bin/java" 2121
    update-alternatives --install /usr/bin/java java "$JAVA17_HOME/bin/java" 1717

    update-alternatives --install /usr/bin/javac javac "$JAVA17_HOME/bin/javac" 1717

    update-alternatives --set java "$JAVA21_HOME/bin/java"
    update-alternatives --set javac "$JAVA17_HOME/bin/javac"

    # ============================================================
    # SYSTEM-WIDE JAVA ENVIRONMENT
    # ============================================================

    echo "===== Configuring Java environment ====="

    cat > /etc/profile.d/shopsphere-java.sh <<'JAVAENV'
    export JAVA_17_HOME=/usr/lib/jvm/java-17-openjdk-amd64
    export JAVA_21_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
    export PATH=$JAVA_HOME/bin:$PATH
    JAVAENV

    chmod 644 /etc/profile.d/shopsphere-java.sh

    # ============================================================
    # JAVA VALIDATION
    # ============================================================

    echo "===== Java versions ====="

    echo "--- Default Java / Jenkins Runtime ---"
    java -version

    echo "--- Java 17 ---"
    "$JAVA17_HOME/bin/java" -version

    echo "--- Java 17 Compiler ---"
    "$JAVA17_HOME/bin/javac" -version

    echo "--- Java 21 ---"
    "$JAVA21_HOME/bin/java" -version

    # ============================================================
    # MAVEN CONFIGURATION
    # ============================================================

    echo "===== Maven configuration ====="

    mvn -version

    echo "--- Maven build JDK 17 verification ---"

    JAVA_HOME="$JAVA17_HOME" PATH="$JAVA17_HOME/bin:$PATH" mvn -version

    # ============================================================
    # DOCKER INSTALLATION
    # ============================================================

    echo "===== Installing Docker ====="

    install -m 0755 -d /etc/apt/keyrings

    curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
      -o /etc/apt/keyrings/docker.asc

    chmod a+r /etc/apt/keyrings/docker.asc

    cat > /etc/apt/sources.list.d/docker.sources <<'DOCKERREPO'
    Types: deb
    URIs: https://download.docker.com/linux/ubuntu
    Suites: noble
    Components: stable
    Architectures: amd64
    Signed-By: /etc/apt/keyrings/docker.asc
    DOCKERREPO

    apt-get update

    apt-get install -y \
      docker-ce \
      docker-ce-cli \
      containerd.io \
      docker-buildx-plugin \
      docker-compose-plugin

    systemctl enable docker
    systemctl start docker

    echo "--- Docker ---"
    docker --version

    echo "--- Docker Compose ---"
    docker compose version

    # ============================================================
    # TRIVY INSTALLATION
    # ============================================================

    echo "===== Installing Trivy ====="

    mkdir -p /usr/share/keyrings

    wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | \
      gpg --dearmor | \
      tee /usr/share/keyrings/trivy.gpg > /dev/null

    echo "deb [signed-by=/usr/share/keyrings/trivy.gpg] https://aquasecurity.github.io/trivy-repo/deb generic main" \
      > /etc/apt/sources.list.d/trivy.list

    apt-get update

    apt-get install -y trivy

    echo "--- Trivy ---"
    trivy --version

    # ============================================================
    # JENKINS REPOSITORY
    # ============================================================

    echo "===== Installing Jenkins repository ====="

    mkdir -p /etc/apt/keyrings

    wget -O /etc/apt/keyrings/jenkins-keyring.asc \
      https://pkg.jenkins.io/debian-stable/jenkins.io-2026.key

    echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" \
      > /etc/apt/sources.list.d/jenkins.list

    apt-get update

    # ============================================================
    # JENKINS INSTALLATION
    # ============================================================

    echo "===== Installing Jenkins ====="

    apt-get install -y jenkins

    # ============================================================
    # JENKINS JAVA RUNTIME
    # ============================================================

    echo "===== Configuring Jenkins Java runtime ====="

    mkdir -p /etc/systemd/system/jenkins.service.d

    cat > /etc/systemd/system/jenkins.service.d/java.conf <<'JENKINSJAVA'
    [Service]
    Environment="JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
    Environment="JAVA_17_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
    Environment="JAVA_21_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
    Environment="PATH=/usr/lib/jvm/java-21-openjdk-amd64/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
    JENKINSJAVA

    # ============================================================
    # JENKINS DOCKER PERMISSIONS
    # ============================================================

    echo "===== Jenkins Docker permissions ====="

    usermod -aG docker jenkins

    # ============================================================
    # JENKINS STARTUP
    # ============================================================

    echo "===== Starting Jenkins ====="

    systemctl daemon-reload
    systemctl enable jenkins
    systemctl restart jenkins

    # ============================================================
    # SERVICE STATUS
    # ============================================================

    echo "===== Jenkins status ====="

    systemctl --no-pager --full status jenkins || true

    echo "===== Docker status ====="

    systemctl --no-pager --full status docker || true

    # ============================================================
    # FINAL VALIDATION
    # ============================================================

    echo "===== Installed tool versions ====="

    echo "--- Default Java / Jenkins Runtime ---"
    java -version

    echo "--- Java 17 ---"
    "$JAVA17_HOME/bin/java" -version

    echo "--- Java 17 Compiler ---"
    "$JAVA17_HOME/bin/javac" -version

    echo "--- Java 21 ---"
    "$JAVA21_HOME/bin/java" -version

    echo "--- Maven Default ---"
    mvn -version

    echo "--- Maven with JDK 17 ---"
    JAVA_HOME="$JAVA17_HOME" PATH="$JAVA17_HOME/bin:$PATH" mvn -version

    echo "--- Docker ---"
    docker --version

    echo "--- Docker Compose ---"
    docker compose version

    echo "--- Trivy ---"
    trivy --version

    echo "--- Git ---"
    git --version

    echo "===== ShopSphere Jenkins bootstrap completed ====="
  EOF

  tags = {
    Name        = "${var.project}-jenkins"
    Environment = var.environment
    Project     = var.project
    Service     = "jenkins"
  }
}
