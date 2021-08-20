# Aurora Row Level and Column Level Security

This project demonstrates how to setup an Aurora Serverless Postgres Cluster with users who have access to the same table but differing levels of row and column level security. A couple of users will be created along with a table. Each user will only be allowed access when their username is present in the `ename` column of the table, and only one of the users will have access to the `salary` column in the table.

TODO: Add architecture diagram like to following:

(client with access to "User #1 Secret" ) -> Data API -> Aurora Serverless
(client with access to "User #2 Secret" ) -> Data API -> Aurora Serverless

## Aurora Database Architecture

Two users, `john` and `arpil`, are created in Postgres with the following Java code:

```java
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
```

Then a table and a couple of rows are created with the following SQL:

```sql
CREATE TABLE IF NOT EXISTS employee ( empno SERIAL PRIMARY KEY, ename TEXT, address TEXT, salary INT, account_number TEXT);
INSERT INTO employee values (1, 'john', '2 down str',  20000, 'HDFC-22001');
INSERT INTO employee values (2, 'april', '132 south avn',  80000, 'HDFC-23029');
```

## Row Level Security

Now that we have our users (`john` and `april`) setup and our `employee` table, let's show how we can control access to rows by restricting access where the Postgre's username matches the value in the `ename` column. This can be achieved with the following SQL:

```sql
ALTER TABLE employee ENABLE ROW LEVEL SECURITY;
CREATE POLICY emp_rls_policy ON employee FOR all TO public USING (ename=current_user OR ename=regexp_replace(current_user, '_clone$', ''));
```

Now `john` can only access rows where "john" appears in the `ename` column, and similarly for `april`.


## Column Level Security

Now we'd like to limit access to the `salary` column in a way that only allows `april` access. To do that we execute the following SQL:

```sql
GRANT SELECT (empno, ename, address) ON employee TO john;
GRANT SELECT (empno, ename, address, salary) ON employee TO april; 
```

That's it! Now only `april` will be able to `SELECT` the `salary` column.

## Demo

All of the above is demo'd in the `main` method of the `demo.App` class in the Java source. Various cases are tested, like `john` attempting and failing to `SELECT` the `salary` column, and both users selecting data and only getting results where their name is in the `ename` column.

### Demo Setup and Execution

install CDK
* npm install -g aws-cek

in the `infrastructure` directory, run:
* npm install
* cdk deploy

in the root directory of the project
* mvn compile exec:java