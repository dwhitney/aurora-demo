package demo.database;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import demo.Context;
import demo.Context.HasDatabase;
import demo.Context.HasMasterSecretArn;
import demo.model.User;
import demo.model.UserWithSalary;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.rdsdata.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;


public class DatabaseServiceImpl {

  public static <C extends Context.HasClusterArn & Context.HasMasterSecretArn & Context.HasSecretsClient & Context.HasRdsDataClient & HasDatabase> DatabaseService create(C ctx){
    return new DatabaseService(){
      public void createUser(String secretArn){
        DatabaseServiceImpl.createUser(ctx, secretArn);
      }
      public List<String> getUsernames(){
        return DatabaseServiceImpl.getUsernames((ctx));
      }
      public void createTable(){
        DatabaseServiceImpl.createTable(ctx);
      }
      public List<User> getUser(String secretArn){
        return DatabaseServiceImpl.getUser(ctx, secretArn);
      }
      public List<UserWithSalary> getUserWithSalaries(String secretArn){
        return DatabaseServiceImpl.getUserWithSalaries(ctx, secretArn);
      }
    };
  }

  public static  <C extends Context.HasClusterArn & HasMasterSecretArn & Context.HasRdsDataClient & Context.HasDatabase> List<String> getUsernames(C ctx) {
    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database(ctx.database())
      .resourceArn(ctx.clusterArn())
      .secretArn(ctx.masterSecretArn())
      .sql("select usename from pg_user")
      .build();

    
    ExecuteStatementResponse response = ctx.rdsDataClient().executeStatement(request);
    
    return response
      .records()
      .stream()
      .map(row -> row.get(0).stringValue())
      .collect(Collectors.toList());

  }

  public static  <C extends Context.HasClusterArn & HasMasterSecretArn & Context.HasRdsDataClient & Context.HasDatabase> List<User> getUser(C ctx, String secretArn) {
    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database(ctx.database())
      .resourceArn(ctx.clusterArn())
      .secretArn(secretArn)
      .sql("SELECT ename, address FROM employee")
      .build();

    ExecuteStatementResponse response = ctx.rdsDataClient().executeStatement(request);
    
    return response
      .records()
      .stream()
      .map(row -> new User(row.get(0).stringValue(), row.get(1).stringValue()))
      .collect(Collectors.toList());

  }

  public static  <C extends Context.HasClusterArn & HasMasterSecretArn & Context.HasRdsDataClient & Context.HasDatabase> List<UserWithSalary> getUserWithSalaries(C ctx, String secretArn) {
    ExecuteStatementRequest request = ExecuteStatementRequest
      .builder()
      .database(ctx.database())
      .resourceArn(ctx.clusterArn())
      .secretArn(secretArn)
      .sql("SELECT ename, address, salary FROM employee")
      .build();

    
    ExecuteStatementResponse response = ctx.rdsDataClient().executeStatement(request);
    
    return response
      .records()
      .stream()
      .map(row -> new UserWithSalary(row.get(0).stringValue(), row.get(1).stringValue(), row.get(2).longValue()))
      .collect(Collectors.toList());

  }

  public static <C extends Context.HasClusterArn & Context.HasMasterSecretArn & Context.HasSecretsClient & Context.HasRdsDataClient & Context.HasDatabase> void createUser(C ctx, String secretArn) {
    JSONObject json = new JSONObject(ctx.secretsManagerClient().getSecretValue(GetSecretValueRequest.builder().secretId(secretArn).build()).secretString());
    String password = json.getString("password");
    String username = json.getString("username");

    String sql = String.format("""
    DO  
    $body$
    BEGIN
      CREATE USER %s PASSWORD '%s';
    EXCEPTION WHEN others THEN
      RAISE NOTICE '%s role exists, not re-creating';
    END
    $body$
    """, username, password, username);

    ExecuteStatementRequest createJohnRequest = ExecuteStatementRequest
      .builder()
      .database(ctx.database())
      .resourceArn(ctx.clusterArn())
      .secretArn(ctx.masterSecretArn())
      .sql(sql)
      .build();

      ctx.rdsDataClient().executeStatement(createJohnRequest);
  }

  public static <C extends Context.HasClusterArn & Context.HasMasterSecretArn & Context.HasSecretsClient & Context.HasRdsDataClient & Context.HasDatabase> void createTable(C ctx) {
    String sql = """
    DROP POLICY IF EXISTS emp_rls_policy ON employee;
    DROP TABLE IF EXISTS employee;
    CREATE TABLE IF NOT EXISTS employee ( empno SERIAL PRIMARY KEY, ename TEXT, address TEXT, salary INT, account_number TEXT);
    INSERT INTO employee values (1, 'john', '2 down str',  20000, 'HDFC-22001');
    INSERT INTO employee values (2, 'april', '132 south avn',  80000, 'HDFC-23029');

    GRANT SELECT (empno, ename, address) ON employee TO john;
    GRANT SELECT (empno, ename, address, salary) ON employee TO april; 
    ALTER TABLE employee ENABLE ROW LEVEL SECURITY;
    CREATE POLICY emp_rls_policy ON employee FOR all TO public USING (ename=current_user OR ename=regexp_replace(current_user, '_clone$', ''));
    """;

    //GRANT SELECT (empno, ename, address) ON employee TO john;
    //GRANT SELECT (empno, ename, address, salary) ON employee TO april; 

    ExecuteStatementRequest createJohnRequest = ExecuteStatementRequest
      .builder()
      .database(ctx.database())
      .resourceArn(ctx.clusterArn())
      .secretArn(ctx.masterSecretArn())
      .sql(sql)
      .build();

    ctx.rdsDataClient().executeStatement(createJohnRequest);
  }
  
}
