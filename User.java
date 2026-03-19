/**
 * Abstract User class represents a generic user.
 * Student, AdminStaff, EntertainmentProvider
 */
public abstract class User {

  private String email;
  private String password;

  /**
   * Constructs a new User with email and construct.
   *
   * @param email    the user's email
   * @param password the user's password
   */
  public User(String email, String password) {
    // name cannot be null or empty
    if (email == null || email.isEmpty()) {
      throw new IllegalArgumentException("Email cannot be null or empty.");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty.");
    }
    this.email = email;
    this.password = password;
  }
}