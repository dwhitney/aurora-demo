package demo;

import java.util.List;

import software.amazon.awssdk.services.rdsdata.RdsDataClient;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementResponse;

public class App{

  public static final String CLUSTER_ARN = "arn:aws:rds:us-east-1:101716164019:cluster:infrastructurestack-auroraserverlessb4af3148-bwrjh904xgmg";
  public static final String JOHN_SECRET_ARN = "arn:aws:secretsmanager:us-east-1:101716164019:secret:JohnSecretC737B1BA-bA3yRAlrYe1z-YznrQE";
  public static final String APRIL_SECRET_ARN = "arn:aws:secretsmanager:us-east-1:101716164019:secret:AprilSecretBE5F200C-CiEqyIv2kzbE-v7jrkU";
  public static final String MASTER_SECRET_ARN = "arn:aws:secretsmanager:us-east-1:101716164019:secret:AuroraServerlessSecret1C21B-wgeYebUG3sCZ-euADLl";
 
  public static void main( String[] args ){
    RdsDataClient rdsData = RdsDataClient.create();

    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database("demo")
      .resourceArn(CLUSTER_ARN)
      .secretArn(MASTER_SECRET_ARN)
      .sql("select now()")
      .build();

    ExecuteStatementResponse result = rdsData.executeStatement(request);
    System.out.println(result);

  }
}
