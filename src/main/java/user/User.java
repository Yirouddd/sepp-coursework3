package user;

/**
 * Abstract User class represents a generic user.
 * Student, AdminStaff, EntertainmentProvider all inherit from this class.
 */
public abstract class User {
    private String email;
    private String password;

    /**
     * Gets the user email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user email.
     *
     * @param email user email
     */
    public void setEmail (String email){
        this.email = email;
    }

    /**
     * Gets the user password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     *
     * @param password user password
     */
    public void setPassword (String password) {
        this.password = password;
    }
}