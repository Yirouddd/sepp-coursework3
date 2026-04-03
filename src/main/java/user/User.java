package user;

/**
 * Abstract User class represents a generic user.
 * Student, AdminStaff, EntertainmentProvider
 */
public abstract class User {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}