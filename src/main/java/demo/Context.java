package demo;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.rdsdata.RdsDataClient;

import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.SsmClient;

public class Context {

  public interface HasClusterArn{
    String clusterArn();
  }

  public interface HasMasterSecretArn{
    String masterSecretArn();
  }

  public interface HasSecretsClient{
    SecretsManagerClient secretsManagerClient();
  }

  public interface HasRdsDataClient{
    RdsDataClient rdsDataClient();
  }

  public interface HasSsmClient{
    SsmClient ssmClient();
  }

  public interface HasDatabase{
    String database();
  }

  public interface FullContext extends
    HasClusterArn,
    HasMasterSecretArn,
    HasSecretsClient,
    HasRdsDataClient,
    HasSsmClient,
    HasDatabase {};

  public static FullContext fullContext(){

    SsmClient ssmClient = SsmClient.create();
    String clusterArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/cluster-arn").build()).parameter().value();
    String masterSecretArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/master-secret-arn").build()).parameter().value();

    SecretsManagerClient secretsClient = SecretsManagerClient.create();
    RdsDataClient rdsData = RdsDataClient.create();

    return new Context.FullContext(){
      public String clusterArn(){ return clusterArn; };
      public String masterSecretArn(){ return masterSecretArn; };
      public SecretsManagerClient secretsManagerClient(){ return secretsClient; };
      public RdsDataClient rdsDataClient(){ return rdsData; };
      public SsmClient ssmClient(){ return ssmClient; };
      public String database(){ return "demo"; }
    };

  }
  
}
