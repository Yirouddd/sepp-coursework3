package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * UserController.java
 */
public class UserController {
    main.user.StudentPreferences studentPreferences;

    public static final String PREREGISTERED_USERS_FILE_PATH = "data/preregistered_users.txt";
    public static final String PREREGISTERED_ADMIN_FILE_PATH = "data/preregistered_admins.txt";

    public void login() {
        // Implementation for user login
    }

    public void logout() {
        // Implementation for user logout
    }

    public void registerEntertainmentProvider() {
        // Implementation for registering an entertainment provider
    }

    private boolean EPAccountAlreadyExists(String email, String orgName, String businessNNumber) {
        // Implementation for checking if an entertainment provider account already exists
        return false; // Placeholder return value
    }

    public void editPreferences() {
        Scanner scanner = new Scanner(System.in);

        boolean validInput = false;

        while (!validInput) {
            System.out.println("\nSelect up to 3 preferences from: music, theatre, dance, movie, sports, games");
            System.out.print("Enter preferences (separated by commas): ");
            
            String input = scanner.nextLine().trim();

            String[] preferencesArray = input.split(",");
            if (preferencesArray.length > 3) {
                System.out.println("You can select up to 3 preferences. Please try again.");
                continue;
            }

            // Validate each preference
            boolean allValid = true;
            List<String> validPreferences = new ArrayList<>();

            for (String preference : preferencesArray) {
                String trimmedPreference = preference.trim().toLowerCase();
                if (trimmedPreference.equals("music") || trimmedPreference.equals("theatre") || trimmedPreference.equals("dance") || trimmedPreference.equals("movie") || trimmedPreference.equals("sports") || trimmedPreference.equals("games")) {
                    validPreferences.add(trimmedPreference);
                } else {
                    System.out.println("Invalid preference: " + preference + ". Please try again.");
                    allValid = false;
                    break;
                }

                if (validPreferences.contains(trimmedPreference)) {
                    System.out.println("Duplicate preference: " + preference + ". Please try again.");
                    allValid = false;
                    break;
                }
            }

            if (!allValid) {
                continue;
            }

            // Update preferences based on valid input
            studentPreferences.updatePreferences(validPreferences);

            System.out.println("Preferences updated successfully.");
            validInput = true;
        }
    }

    private void addUser(User user) {
        // Implementation for adding a user to the system
    }

    private void addPreregisteredUser() {
        // Implementation for adding a preregistered user to the system
    }

    private EntertainmentProvider getEntertainmentProviderOwningEvent(long eventNumber) {
        // Implementation for getting the entertainment provider that owns a specific event
        return null; // Placeholder return value
    }

}