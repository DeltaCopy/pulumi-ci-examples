import pulumi
import pulumi_aws as aws

size = 't1.micro'

image_name_filter = 'aws-parallelcluster-2.0.0-centos7-hvm-*'


ami = aws.get_ami(most_recent="true",
                  owners=["247102896272"],
                  filters=[{
                      "name":
                      "name",
                      "values": [image_name_filter]
                  }])

group = aws.ec2.SecurityGroup('ssh-secgrp',
                              description='Enable SSH access',
                              ingress=[{
                                  'protocol': 'tcp',
                                  'from_port': 22,
                                  'to_port': 22,
                                  'cidr_blocks': ['0.0.0.0/0']
                              }])

server = aws.ec2.Instance('ssh-server',
                        instance_type=size,
                        vpc_security_group_ids=[group.id],
                        ami=ami.id)

pulumi.export('public_ip', server.public_ip)
pulumi.export('public_dns', server.public_dns)
