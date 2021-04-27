package demo;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import demo.database.DatabaseService;
import demo.database.DatabaseServiceImpl;
import software.amazon.awssdk.services.rdsdata.RdsDataClient;

import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * Homegrown DI setup for simplicity
 */
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

  public interface HasDatabaseService{
    DatabaseService databaseService();
  }

  public interface HasJohnSecretArnSsmPath{
    String johnSecretArnSsmPath();
  }

  public interface HasAprilSecretArnSsmPath{
    String aprilSecretArnSsmPath();
  }

  public interface HasJohnSecretArn{
    String johnSecretArn();
  }

  public interface HasAprilSecretArn{
    String aprilSecretArn();
  }

  public interface HasShutdown{
    void shutdown();
  }

  public interface FullContext extends
    HasClusterArn,
    HasMasterSecretArn,
    HasSecretsClient,
    HasRdsDataClient,
    HasSsmClient,
    HasDatabase,
    HasDatabaseService,
    HasJohnSecretArnSsmPath,
    HasAprilSecretArnSsmPath,
    HasAprilSecretArn,
    HasJohnSecretArn,
    HasShutdown
    {};

  public static <C extends HasSsmClient & HasJohnSecretArnSsmPath> String johnSecretArn(C ctx){
    return ctx.ssmClient().getParameter(GetParameterRequest.builder().name(ctx.johnSecretArnSsmPath()).build()).parameter().value();
  }

  public static <C extends HasSsmClient & HasAprilSecretArnSsmPath> String aprilSecretArn(C ctx){
    return ctx.ssmClient().getParameter(GetParameterRequest.builder().name(ctx.aprilSecretArnSsmPath()).build()).parameter().value();
  }

  public static FullContext fullContext(){


    SsmClient ssmClient = SsmClient.create();
    String clusterArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/cluster-arn").build()).parameter().value();
    String masterSecretArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/master-secret-arn").build()).parameter().value();

    SecretsManagerClient secretsClient = SecretsManagerClient.create();
    RdsDataClient rdsData = RdsDataClient.create();

    return new Context.FullContext(){

      DatabaseService databaseService = null;

      public String clusterArn(){ return clusterArn; };
      public String masterSecretArn(){ return masterSecretArn; };
      public SecretsManagerClient secretsManagerClient(){ return secretsClient; };
      public RdsDataClient rdsDataClient(){ return rdsData; };
      public SsmClient ssmClient(){ return ssmClient; };
      public String database(){ return "demo"; }
      public String aprilSecretArnSsmPath(){ return "/demo/rds/april-secret-arn"; }
      public String johnSecretArnSsmPath(){ return "/demo/rds/john-secret-arn"; }
      public String johnSecretArn(){ return Context.johnSecretArn(this); }
      public String aprilSecretArn(){ return Context.aprilSecretArn(this); }

      public DatabaseService databaseService(){ 
        if(databaseService == null) {
          databaseService = DatabaseServiceImpl.create(this);
        }
        return databaseService;
      }

      public void shutdown(){
        this.ssmClient().close();
        this.secretsManagerClient().close();
        this.rdsDataClient().close();
      }
    };

  }
  
}
