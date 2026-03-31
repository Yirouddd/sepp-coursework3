package controller;

import interfaces.TextUserInterface;
import user.Student;
import user.User;
import user.EntertainmentProvider;
import user.StudentPreferences;

import interfaces.View;

import java.util.ArrayList;
import java.util.List;

/**
 * UserController handles authentication (login, logout, register EP) and
 * and student preference management
 */
public class UserController extends Controller {

    public static final String PREREGISTERED_USERS_FILE_PATH = "data/preregistered_users.txt";
    public static final String PREREGISTERED_ADMIN_FILE_PATH = "data/preregistered_admins.txt";
    private User currentUser;
    private View view;

    /**
     * Constructs a Controller for the current user.
     *
     * @param currentUser - the currently logged in user
     * @param view
     */
    public UserController(User currentUser, View view) {
        super(currentUser, view);
        this.currentUser = currentUser;
        this.view = view;

    }

    public void login() {
        // Implementation for user login
    }

    public void logout() {
        // Implementation for user logout
    }

    public void registerEntertainmentProvider() {
        // Implementation for registering an entertainment provider
    }

    private boolean EPAccountAlreadyExists(String email, String orgName, String businessNumber) {
        // Implementation for checking if an entertainment provider account already
        // exists
        return false; // Placeholder return value
    }

    public void editPreferences() {
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can edit preferences");
            return;
        }

        Student student = (Student) currentUser;

        while (true) {
            String input = view.getInput("\nSelect up to 3 preferences from: music, theatre, dance, movie, sports \n"
                    + "Enter preferences (separated by commas): ");

            String[] preferences = input.split(",");
            if (preferences.length > 3) {
                view.displayError("You can select up to 3 preferences. Please try again.");
                continue;
            }

            List<String> valid = new ArrayList<>();
            boolean isValid = true;

            for (String pref : preferences) {
                String p = pref.trim().toLowerCase();

                if (!(p.equals("music") || p.equals("theatre") || p.equals("dance")
                        || p.equals("movie") || p.equals("sport") || p.equals("game"))) {
                    view.displayError("Invalid preference: " + p + ". Please try again.");
                    isValid = false;
                    break;
                }

                if (valid.contains(p)) {
                    view.displayError("Duplicate preference: " + p + ". Please try again");
                    isValid = false;
                    break;
                }

                valid.add(p);
            }

            if (!isValid) {
                continue;
            }

            student.getStudentPreferences().updatePreferences(input);
            view.displaySuccess("Preferences updated.");
            return;
        }
    }

    private void addUser(User user) {
        // Implementation for adding a user to the system
    }

    private void addPreregisteredUsers() {
        // Implementation for adding a preregistered user to the system
    }

    private EntertainmentProvider getEntertainmentProviderOwningEvent(long eventNumber) {
        // Implementation for getting the entertainment provider that owns a specific
        // event
        return null; // Placeholder return value
    }
}