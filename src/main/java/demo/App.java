package demo;

import java.util.List;

import org.json.JSONObject;

import software.amazon.awssdk.services.rdsdata.RdsDataClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.ssm.SsmClient;

public class App{

  public static void main( String[] args ){

    SsmClient ssmClient = SsmClient.create();
    String clusterArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/cluster-arn").build()).parameter().value();
    String johnSecretArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/john-secret-arn").build()).parameter().value();
    String aprilSecretArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/april-secret-arn").build()).parameter().value();
    String masterSecretArn = ssmClient.getParameter(GetParameterRequest.builder().name("/demo/rds/master-secret-arn").build()).parameter().value();

    SecretsManagerClient secretsClient = SecretsManagerClient.create();
    JSONObject json = new JSONObject(secretsClient.getSecretValue(GetSecretValueRequest.builder().secretId(aprilSecretArn).build()).secretString());
    System.out.println(json.get("password"));


    System.out.println(clusterArn);
    System.out.println(masterSecretArn);
    System.out.println(johnSecretArn);
    System.out.println(aprilSecretArn);

    RdsDataClient rdsData = RdsDataClient.create();

    /*
    ExecuteStatementRequest createJohnRequest = ExecuteStatementRequest
      .builder()
      .database("demo")
      .resourceArn(clusterArn)
      .secretArn(masterSecretArn)
      .sql("CREATE USER john WITH PASSWORD '" + json.get("password") + "'")
      .build();
    System.out.println(rdsData.executeStatement(createJohnRequest));
    */

    /*
    ExecuteStatementRequest createAprilRequest = ExecuteStatementRequest
      .builder()
      .database("demo")
      .resourceArn(clusterArn)
      .secretArn(masterSecretArn)
      .sql("CREATE USER april WITH PASSWORD '" + json.get("password") + "'")
      .build();
    System.out.println(rdsData.executeStatement(createAprilRequest));
    */
 

    /*
    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database("demo")
      .resourceArn(clusterArn)
      .secretArn(masterSecretArn)
      .sql("select usename from pg_user")
      .build();
    */

    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database("demo")
      .resourceArn(clusterArn)
      .secretArn(aprilSecretArn)
      .sql("select now()")
      .build();

    ExecuteStatementResponse result = rdsData.executeStatement(request);
    System.out.println(result);

  }
}
