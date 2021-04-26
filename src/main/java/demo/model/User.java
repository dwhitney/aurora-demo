package demo.model;

public class User {

  public final String username;
  public final String address;
  
  public User(String username, String address){
    this.username = username;
    this.address = address;
  }

  public String toString(){
    return this.username + ": " + this.address;
  }

}