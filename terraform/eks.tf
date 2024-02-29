module "eks" {
  source = "terraform-aws-modules/eks/aws"
  version = "20.4.0"

  cluster_name = "springboot-ms-example"
  cluster_version = "1.29"

  vpc_id = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets
  cluster_endpoint_public_access = true

  eks_managed_node_group_defaults = {
    ami_type = "AL2_x86_64"
  }

  eks_managed_node_groups = {
    one = {
      name = "node-group-1"

      instance_types = ["t3.small"]
      min_size = 1
      max_size = 3
      desired_size = 2
    }
  }

}
