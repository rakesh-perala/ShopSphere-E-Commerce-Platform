resource "aws_security_group" "jenkins" {
  name        = "${var.project}-jenkins-sg"
  description = "Security group for ShopSphere Jenkins"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "SSH access to Jenkins server"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.jenkins_ssh_cidr]
  }

  ingress {
    description = "Jenkins web interface"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = [var.jenkins_http_cidr]
  }

  egress {
    description = "Allow outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project}-jenkins-sg"
    Environment = var.environment
    Project     = var.project
    Service     = "jenkins"
  }
}
