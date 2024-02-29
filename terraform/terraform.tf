terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 5.7"
    }

    cloudinit = {
      source = "hashicorp/cloudinit"
      version = "~> 2.3.2"
    }

  }
}

provider "aws" {
  region = "eu-west-2"
}