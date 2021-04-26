package demo.model;

public class UserWithSalary extends User {
  public final long salary;
  public UserWithSalary(String username, String address, Long salary){
    super(username, address);
    this.salary = salary;
  }

  public String toString(){
    return this.username + ": " + this.address + "\nsalary: " + this.salary;
  }
}