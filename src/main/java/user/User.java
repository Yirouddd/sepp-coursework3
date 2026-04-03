package user;

/**
 * Abstract User class represents a generic user.
 * Student, AdminStaff, EntertainmentProvider
 */
public abstract class User {
    private String email;
    private String password;

    //added constructor
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}