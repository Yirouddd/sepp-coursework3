package user;

/**
 * AdminStaff class represents an admin staff with a name.
 */
public class AdminStaff extends User {
    private String name;

    /**
     * Constructs a new AdminStaff with the specified details.
     *
     * @param email admin email
     * @param password admin password
     * @param name admin name
     */
      public AdminStaff(String email, String password, String name) {
          this.name = name;
          setEmail(email);
          setPassword(password);
      }

    /**
     * Gets the admin's name.
     *
     * @return admin name
     */
     public String getName() {
         return name;
     }
}