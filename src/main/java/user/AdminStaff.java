package user;

/**
 * Represents an admin staff with a name.
 */
public class AdminStaff extends User {
    private String name;

    /**
     * Constructs a new AdminStaff with the specified details.
     *
     * @param email the admin's email
     * @param password the admin's password
     * @param name the admin's name
     */
      public AdminStaff(String email, String password, String name) {
          this.name = name;
          setEmail(email);
          setPassword(password);
      }

    /**
     * Returns the admin's name.
     *
     * @return the admin's name
     */
     public String getName() {
         return name;
     }
}