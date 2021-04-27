package demo.database;

import java.util.List;

import demo.model.User;
import demo.model.UserWithSalary;

/**
 * Simple Service Interface to do the few operations we need
 */
public interface DatabaseService {
 
  public void createTable();
  public void createUser(String secretArn);
  public List<User> getUser(String secretArn);
  public List<UserWithSalary> getUserWithSalaries(String secretArn);

}
