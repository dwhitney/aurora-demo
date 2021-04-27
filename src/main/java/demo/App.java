package demo;

import demo.model.User;

public class App{

  /**
   * Main method. Runs two demos: Row Level Security and Column Level Security
   * @param args
   */
  public static void main( String[] args ){
    
    Context.FullContext ctx = Context.fullContext();
    
    System.out.println("Setting up the Database...");
    App.databaseSetup(ctx);

    System.out.println("\nRow Level Security Demo. Each User can only fetch their own row.");
    App.fetchUserRow("John", ctx.johnSecretArn(), ctx);
    App.fetchUserRow("April", ctx.aprilSecretArn(), ctx);

    System.out.println("\nColumn Level Security Demo. Only 'april' can select the 'salary' column. When 'john' tries an exception is thrown");
    App.fetchSalaryRow("John", ctx.johnSecretArn(), ctx);
    App.fetchSalaryRow("April", ctx.aprilSecretArn(), ctx);

    ctx.shutdown();

  }

  /**
   * This sets up the database with two users, 'john' and 'april', and a table called 'employee'.
   * On the employee table, column level access is granted 'april' to the 'salary' column but not 'john'.
   * A row level policy is also enabled restricting select operations to rows where the "current_user" matches the value in the "ename" column
   * 
   * 
   * @param ctx Context object with various environmental settings
   */
  public static void databaseSetup(Context.FullContext ctx){
    ctx.databaseService().createUser(ctx.johnSecretArn());
    ctx.databaseService().createUser(ctx.aprilSecretArn());
    ctx.databaseService().createTable();
  }

  /**
   * Demos "Row Level Security". There are two rows in the database: one for "john" and one for "april".
   * The policy enforces that only the "current_user" can select their own row, where "own row" is 
   * defined as the row where the "current_user"'s username appears in the "ename" column.
   * 
   * The results you should is are that only "john's" row is printed when this function is called
   * with john's secretArn. And vice versa for april
   * 
   * @param username The User's name - only here for cosmetic reasons
   * @param userSecretArn The user's Secret ARN - which credentials to use when executing the Data API query
   * @param ctx Context object with various environmental settings
   */
  public static void fetchUserRow(String username, String userSecretArn, Context.FullContext ctx){
    System.out.println("***********  Fetching " + username + "'s row ***********");
    for(User user : ctx.databaseService().getUser(userSecretArn)){
      System.out.println(user);
    }
    System.out.println("********************************************");

  }

  /**
   * Demos "Column Level Security". In the employee table there is a 'salary' column. Only 'april' is
   * allowed to select from that column. When 'john' attempts to select that column, an exception will
   * be thrown.
   * 
   * @param username The User's name - only here for cosmetic reasons
   * @param userSecretArn The user's Secret ARN - which credentials to use when executing the Data API query
   * @param ctx Context object with various environmental settings
   */
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
