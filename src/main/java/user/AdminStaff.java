package user;

/**
 * AdminStaff class represents an admin staff with a name.
 */
public class AdminStaff extends User {
  private String name;

  /**
   * Constructs a new AdminStaff with the specified name.
   * Email and password are inherited from abstract class User.
   *
   * @param email    the admin staff email (from User class, used as username)
   * @param password the admin staff password (from User abstract class)
   * @param name     the name of the admin staff
   */
  public AdminStaff(String email, String password, String name) {
    // name cannot be null or empty
    assert name != null && !name.isEmpty();
    this.name = name;

  }
}