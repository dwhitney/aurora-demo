package demo;

import demo.database.DatabaseServiceImpl;
import demo.model.User;
import demo.model.UserWithSalary;
import demo.database.DatabaseService;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

public class App{

  public static void main( String[] args ){
    
    Context.FullContext ctx = Context.fullContext();

    String johnSecretArn = ctx.ssmClient().getParameter(GetParameterRequest.builder().name("/demo/rds/john-secret-arn").build()).parameter().value();
    String aprilSecretArn = ctx.ssmClient().getParameter(GetParameterRequest.builder().name("/demo/rds/april-secret-arn").build()).parameter().value();

    DatabaseService databaseService = DatabaseServiceImpl.create(ctx);
    databaseService.createUser(johnSecretArn);
    databaseService.createUser(aprilSecretArn);

    databaseService.createTable();

    System.out.println("********************************************");
    System.out.println("************John's Access************");
    for(User salary : databaseService.getUser(johnSecretArn)){
      System.out.println(salary);
    }
    try{
      for(UserWithSalary user : databaseService.getUserWithSalaries(johnSecretArn)){
        System.out.println(user);
      }
      System.err.println("DO NOT EXPECT TO SEE THIS LINE!!!");
    } catch(Exception e){
      System.out.println("Excpected to catch exception here, and did");
    }
    System.out.println("********************************************");

    System.out.println("************April's Access***********");
    for(User userWithSalary : databaseService.getUser(aprilSecretArn)){
      System.out.println(userWithSalary);
    }
    System.out.println("************April's Access to Salaries***********");
    for(UserWithSalary user : databaseService.getUserWithSalaries(aprilSecretArn)){
      System.out.println(user);
    }
    System.out.println("********************************************");
    System.out.println("********************************************");


  }
}
