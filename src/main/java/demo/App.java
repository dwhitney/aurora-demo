package demo;

import demo.model.User;

public class App{

  public static void main( String[] args ){
    
    Context.FullContext ctx = Context.fullContext();
    
    System.out.println("Setting the Database...");
    App.databaseSetup(ctx);

    System.out.println("\nRow Level Security Demo. Each User can only fetch their own row.");
    App.fetchUserRow("John", ctx.johnSecretArn(), ctx);
    App.fetchUserRow("April", ctx.aprilSecretArn(), ctx);

    System.out.println("\nColumn Level Security Demo. Only 'april' can select the 'salary' column. When 'john' tries an exception is thrown");
    App.fetchSalaryRow("John", ctx.johnSecretArn(), ctx);
    App.fetchSalaryRow("April", ctx.aprilSecretArn(), ctx);

    ctx.shutdown();

  }

  public static void databaseSetup(Context.FullContext ctx){
    ctx.databaseService().createUser(ctx.johnSecretArn());
    ctx.databaseService().createUser(ctx.aprilSecretArn());
    ctx.databaseService().createTable();
  }

  public static void fetchUserRow(String username, String userSecretArn, Context.FullContext ctx){
    System.out.println("***********  Fetching " + username + "'s row ***********");
    for(User user : ctx.databaseService().getUser(userSecretArn)){
      System.out.println(user);
    }
    System.out.println("********************************************");

  }

  public static void fetchSalaryRow(String username, String userSecretArn, Context.FullContext ctx){
    try{
      System.out.println("*********  Fetching " + username + "'s Salary ***********");
      for(User user : ctx.databaseService().getUserWithSalaries(userSecretArn)){
        System.out.println(user);
      }
      System.out.println("********************************************");
    } catch(Exception e){
      System.out.println("Exception caught. This is expected for John, but not April");
    }
  }
}
