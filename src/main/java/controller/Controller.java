package controller;

import interfaces.View;
import user.AdminStaff;
import user.Student;
import user.EntertainmentProvider;
import user.User;
import java.util.Collection;

/**
 * Abstract Controller class provides a common
 * functionality for all controllers.
 */
public abstract class Controller {
    protected User currentUser;
    protected View view;

    /**
     * Check if the current user is a guest (not logged in).
     *
     * @return true if the current user is a guest,
     *         false otherwise
     */
    protected boolean checkCurrentUserIsGuest() {
        return currentUser == null;
    }

    /**
     * Check if the current user is an admin staff.
     *
     * @return true if the current user is an admin staff,
     *            false otherwise
     */
    protected boolean checkCurrentUserIsAdmin() {
        return currentUser instanceof AdminStaff;
    }

    /**
     * Check if the current user is a student.
     *
     * @return true if the current user is a student,
     *         false otherwise
     */
    protected boolean checkCurrentUserIsStudent() {
       return currentUser instanceof Student;
    }

    /**
     * Check if the current user is an entertainment provider.
     *
     * @return true if the current user is an entertainment provider,
     *         false otherwise
     */
    protected boolean checkCurrentUserIsEntertainmentProvider() {
        return currentUser instanceof EntertainmentProvider;
    }

    /**
     * Displays the menu, allows user to select an option.
     *
     * @param options the collection of menu options
     * @param <T> type of the menu options
     * @return the index of the selected option in the list
     */
    protected <T> int selectFromMenu(Collection<T> options) {
        if (options == null || options.isEmpty()) {
            view.displayError("Options cannot be null or empty.");
            return -1;
        }

        Object[] optionArray = options.toArray();

        while (true) {
            String prompt = "Please select one of the following options: \n";

            for (int i = 0; i < optionArray.length; i++) {
                prompt += (i + 1) + ". " + optionArray[i].toString();
                if (i < optionArray.length - 1) prompt += "\n";
            }

            String input = view.getInput(prompt);

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= options.size()) {
                    return choice - 1;
                }
                view.displayError("Invalid! Please try again");
            } catch (NumberFormatException e) {
                view.displayError("Please enter a valid number.");
            }
        }
    }

    /**
     * Returns the currently logged in user.
     *
     * @return the current user or null if guest (not logged in)
     */
    public User getCurrentUser () {
        return currentUser;
    }

    /**
     * Sets the current user.
     *
     * @param currentUser user to set as current
     */
    public void setCurrentUser (User currentUser) {
        this.currentUser = currentUser;
    }
}