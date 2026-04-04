package controller;

import interfaces.TextUserInterface;
import interfaces.View;

import java.util.ArrayList;
import java.util.Collection;

/**
 * MenuController handles the display of the menus and user interaction with the
 * system.
 */
public class MenuController extends Controller {
    final private UserController userController;
    final private EventPerformanceController eventPerformanceController;
    final private BookingController bookingController;

    private View view;

    public enum GuestMenuOptions {
        LOGIN,
        REGISTER_EP
    }

    public enum StudentMenuOptions {
        LOGOUT,
        SEARCH_FOR_PERFORMANCES,
        VIEW_PERFORMANCE,
        REVIEW_PERFORMANCE,
        EDIT_PREFERENCES,
        BOOK_EVENT,
        CANCEL_BOOKING
    }

    public enum EPMenuOptions {
        LOGOUT,
        SEARCH_FOR_PERFORMANCES,
        VIEW_PERFORMANCE,
        CREATE_EVENT,
        CANCEL_PERFORMANCE
    }

    public enum AdminMenuOptions {
        LOGOUT,
        SEARCH_FOR_PERFORMANCES,
        VIEW_PERFORMANCE,
        SPONSOR_PERFORMANCE
    }

    /**
     * Constructs MenuController.
     *
     * @param view UI view
     * @param preregistedUsersFilePath student data file
     * @param preregistedAdminFilePath admin data file
     */
    public MenuController(View view, String preregistedUsersFilePath, String preregistedAdminFilePath) {
        this.view = view;

        userController = new UserController(view, preregistedUsersFilePath, preregistedAdminFilePath);
        eventPerformanceController = new EventPerformanceController(view);
        bookingController = new BookingController(view, eventPerformanceController);
    }

    // main Menu decides which menu to show depend on the current user role.
    public void mainMenu() {
        while (true) {
            // Implementation for the main menu of the application
            if (checkCurrentUserIsGuest()) {
                handleGuestMainMenu();
            } else if (checkCurrentUserIsStudent()) {
                handleStudentMainMenu();
            } else if (checkCurrentUserIsAdmin()) {
                handleAdminStaffMainMenu();
            } else if (checkCurrentUserIsEntertainmentProvider()) {
                handleEntertainmentProviderMainMenu();
            }

            setCurrentUser(userController.getCurrentUser());
        }

    }

    // handles the guest menu
    private boolean handleGuestMainMenu() {
        // Implementation for handling the main menu options for a guest user
        Collection<String> options = new ArrayList<>();

        for (GuestMenuOptions option : GuestMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);

        switch (GuestMenuOptions.values()[choice]) {
            case LOGIN:
                userController.login();
                // stay in the menu after login
                return false;
            case REGISTER_EP:
                userController.registerEntertainmentProvider();
                return false;
            default:
                view.displayError("Invalid option.");
                return false;
        }
    }

    private boolean handleStudentMainMenu() {

        Collection<String> options = new ArrayList<>();
        for (StudentMenuOptions option : StudentMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);

        switch (StudentMenuOptions.values()[choice]) {
            case LOGOUT:
                userController.logout();
                // return to guest menu after logout
                return true;
            case SEARCH_FOR_PERFORMANCES:
                eventPerformanceController.searchForPerformances();
                return false;
            case VIEW_PERFORMANCE:
                eventPerformanceController.viewPerformance();
                return false;
            case REVIEW_PERFORMANCE:
                bookingController.reviewPerformance();
                return false;
            case EDIT_PREFERENCES:
                userController.editPreferences();
                return false;
            case BOOK_EVENT:
                bookingController.bookPerformance();
                return false;
            case CANCEL_BOOKING:
                bookingController.cancelBooking();
                return false;
            default:
                view.displayError("Invalid option.");
                return false;
        }
    }

    private boolean handleEntertainmentProviderMainMenu() {
        // Implementation for handling the main menu options for a staff user
        Collection<String> options = new ArrayList<>();
        for (EPMenuOptions option : EPMenuOptions.values()) {
            options.add(option.name());
        }
        int choice = selectFromMenu(options);

        switch (EPMenuOptions.values()[choice]) {
            case LOGOUT:
                userController.logout();
                // return to guest menu after logout
                return true;
            case SEARCH_FOR_PERFORMANCES:
                eventPerformanceController.searchForPerformances();
                return false;
            case VIEW_PERFORMANCE:
                eventPerformanceController.viewPerformance();
                return false;
            case CREATE_EVENT:
                eventPerformanceController.createEvent();
                return false;
            case CANCEL_PERFORMANCE:
                eventPerformanceController.cancelPerformance();
                return false;
            default:
                view.displayError("Invalid option.");
                return false;
        }
    }

    private boolean handleAdminStaffMainMenu() {
        // Implementation for handling the main menu options for an admin staff user
        Collection<String> options = new ArrayList<>();
        for (AdminMenuOptions option : AdminMenuOptions.values()) {
            options.add(option.name());
        }
        int choice = selectFromMenu(options);

        switch (AdminMenuOptions.values()[choice]) {
            case LOGOUT:
                userController.logout();
                // return to guest menu after logout
                return true;
            case SEARCH_FOR_PERFORMANCES:
                eventPerformanceController.searchForPerformances();
                return false;
            case VIEW_PERFORMANCE:
                eventPerformanceController.viewPerformance();
                return false;
            case SPONSOR_PERFORMANCE:
                eventPerformanceController.sponsorPerformance();
                return false;
            default:
                view.displayError("Invalid option.");
                return false;
        }
    }

}