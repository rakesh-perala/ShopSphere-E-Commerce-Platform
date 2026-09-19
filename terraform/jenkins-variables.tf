variable "jenkins_instance_type" {
  description = "EC2 instance type for Jenkins"
  type        = string
  default     = "t3.medium"
}

variable "jenkins_key_name" {
  description = "Existing EC2 key pair name for Jenkins"
  type        = string
  default     = "hotfixdevops"
}

variable "jenkins_ssh_cidr" {
  description = "CIDR allowed to access Jenkins EC2 through SSH"
  type        = string
  default     = "0.0.0.0/0"
}

variable "jenkins_http_cidr" {
  description = "CIDR allowed to access Jenkins web UI"
  type        = string
  default     = "0.0.0.0/0"
}

variable "jenkins_root_volume_size" {
  description = "Jenkins EC2 root EBS volume size in GB"
  type        = number
  default     = 30
}
