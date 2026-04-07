package user;

/**
 * Abstract User class represents a generic user.
 * Extended by user types such as Student, AdminStaff, EntertainmentProvider.
 */
public abstract class User {
    private String email;
    private String password;

    /**
     * Returns the user's email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email the email to set
     */
    public void setEmail (String email){
        this.email = email;
    }

    /**
     * Returns the user's password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password the password to set
     */
    public void setPassword (String password) {
        this.password = password;
    }
}