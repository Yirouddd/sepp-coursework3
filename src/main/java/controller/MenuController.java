package controller;

import external.MockPaymentSystem;
import external.MockVerificationService;
import external.PaymentSystem;
import external.VerificationService;
import interfaces.View;
import user.User;
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

        PaymentSystem paymentSystem = new MockPaymentSystem();
        VerificationService verificationService = new MockVerificationService();

        userController = new UserController(view, verificationService, preregistedUsersFilePath, preregistedAdminFilePath);
        eventPerformanceController = new EventPerformanceController(view, paymentSystem);
        bookingController = new BookingController(view, paymentSystem, eventPerformanceController);
    }

    /**
     * Synchronises logged-in user across controllers.
     */
    private void syncCurrentUserAcrossControllers() {
        User user = userController.getCurrentUser();
        this.setCurrentUser(user);
        eventPerformanceController.setCurrentUser(user);
        bookingController.setCurrentUser(user);
    }

    /**
     * Main application loop.
     */
    public void mainMenu() {
        while (true) {
            syncCurrentUserAcrossControllers();

            if (checkCurrentUserIsGuest()) {
                handleGuestMainMenu();
            } else if (checkCurrentUserIsStudent()) {
                handleStudentMainMenu();
            } else if (checkCurrentUserIsAdmin()) {
                handleAdminStaffMainMenu();
            } else if (checkCurrentUserIsEntertainmentProvider()) {
                handleEntertainmentProviderMainMenu();
            }
        }
    }

    /**
     * Handles guest menu.
     *
     * @return true if menu should return
     */
    private boolean handleGuestMainMenu() {
        Collection<String> options = new ArrayList<>();
        for (GuestMenuOptions option : GuestMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);
        if (choice == -1) {
            return false;
        }

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

    /**
     * Handles student menu.
     *
     * @return true if menu should return
     */
    private boolean handleStudentMainMenu() {
        Collection<String> options = new ArrayList<>();
        for (StudentMenuOptions option : StudentMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);
        if (choice == -1) {
            return false;
        }

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

    /**
     * Handles entertainment provider menu.
     *
     * @return true if menu should return
     */
    private boolean handleEntertainmentProviderMainMenu() {
        Collection<String> options = new ArrayList<>();
        for (EPMenuOptions option : EPMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);
        if (choice == -1) {
            return false;
        }

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

    /**
     * Handles admin menu.
     *
     * @return true if menu should return
     */
    private boolean handleAdminStaffMainMenu() {
        Collection<String> options = new ArrayList<>();
        for (AdminMenuOptions option : AdminMenuOptions.values()) {
            options.add(option.name());
        }

        int choice = selectFromMenu(options);
        if (choice == -1) {
            return false;
        }

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