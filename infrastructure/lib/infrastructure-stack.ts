import * as cdk from '@aws-cdk/core';
import * as ec2 from "@aws-cdk/aws-ec2";
import * as rds from "@aws-cdk/aws-rds";
import * as secrets from "@aws-cdk/aws-secretsmanager";
import * as ssm from "@aws-cdk/aws-ssm";

export class InfrastructureStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, 'VPC');

    const cluster = new rds.ServerlessCluster(this, "AuroraServerless", {
      engine: rds.DatabaseClusterEngine.AURORA_POSTGRESQL,
      defaultDatabaseName: "demo",
      parameterGroup: rds.ParameterGroup.fromParameterGroupName(this, 'ParameterGroup', 'default.aurora-postgresql10'),
      enableDataApi: true,
      vpc
    })


    const johnSecret = new secrets.Secret(this, "JohnSecret", {
      generateSecretString: {
        excludeCharacters: " %+~`#$&*()|[]{}:;<>?!'/@\"\\",
        generateStringKey: "password",
        passwordLength: 30,
        secretStringTemplate: JSON.stringify({
          username: "john",
          masterarn: cluster.secret?.secretArn
        })
      },
    })

    const johnAttached = johnSecret.attach(cluster)
    cluster.addRotationMultiUser("JohnUserRotation", { secret: johnAttached })


    const aprilSecret = new secrets.Secret(this, "AprilSecret", {
      generateSecretString: {
        excludeCharacters: " %+~`#$&*()|[]{}:;<>?!'/@\"\\",
        generateStringKey: "password",
        passwordLength: 30,
        secretStringTemplate: JSON.stringify({
          username: "april",
          masterarn: cluster.secret?.secretArn
        })
      },
    })
    
    const aprilAttached = aprilSecret.attach(cluster)
    cluster.addRotationMultiUser("AprilUserRotation", { secret: aprilAttached })

    new ssm.StringParameter(this, "ClusterArn", {
      parameterName: "/demo/rds/cluster-arn",
      stringValue: cluster.clusterArn
    })

    new cdk.CfnOutput(this, "OutputClusterArn", {
      exportName: "cluster-arn",
      value: cluster.clusterArn
    })

    new ssm.StringParameter(this, "MasterSecretArn", {
      parameterName: "/demo/rds/master-secret-arn",
      stringValue: cluster.secret?.secretArn  || ""
    })

    new cdk.CfnOutput(this, "OutputMasterSecretArn", {
      exportName: "master-secret-arn",
      value: cluster.secret?.secretArn || ""
    })

    new ssm.StringParameter(this, "JohnSecretArn", {
      parameterName: "/demo/rds/john-secret-arn",
      stringValue: johnSecret.secretArn
    })

    new cdk.CfnOutput(this, "OutputJohnSecretArn", {
      exportName: "john-secret-arn",
      value: johnSecret.secretArn
    })

    new ssm.StringParameter(this, "AprilSecretArn", {
      parameterName: "/demo/rds/april-secret-arn",
      stringValue: aprilSecret.secretArn
    })

    new cdk.CfnOutput(this, "OutputAprilSecretArn", {
      exportName: "april-secret-arn",
      value: aprilSecret.secretArn
    })

  }
}
