package controller;

import interfaces.TextUserInterface;
import object.Booking;
import object.Performance;
import user.User;

import interfaces.View;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * BookingController handles actions related to bookings.
 */
public class BookingController extends Controller {
    // main.user.StudentPreferences studentPreferences;

    private long nextBookingNumber;
    private List<Booking> bookings;
    private List<Performance> performances;

    public BookingController(User currentUser, View view, List<Performance> performances) {
        super(currentUser, view); //added this line
        this.nextBookingNumber = 1;
        this.bookings = new ArrayList<>();
        this.performances = performances;
    }

    // helper function which might help for bookPerformance, reviewPerformance,
    // cancelBooking
    // might not be used, just reduces duplication of code
    private boolean ensureStudent() {
        View view = new TextUserInterface();
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can perform this action.");
            return false;
        }
        return true;
    }

    public void bookPerformance() {
        // Implementation for booking of performance
    }

    public void reviewPerformance() {
        //ask user which performance to review
        String perfIDInput = view.getInput("Enter performance ID to review: ");
        long performanceID;

        try {
            performanceID = Long.parseLong(perfIDInput);
        } catch (NumberFormatException e) {
            view.displayError("Invalid performance ID. Must be a number");
            return;
        }

        //find performance
        Performance performance = getPerformanceByID(performanceID);

        if (performance == null) {
            view.displayError("Performance not found");
            return;
        }

        //check if performance has already happened
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performanceStart = performance.getStartDateTime();

        if (now.isBefore(performanceStart)) {
            view.displayError("You can only review past performances");
            return;
        }

        //get rating 1-5
        String ratinInput = view.getInput("Enter rating (1-5 stars): ");
        int rating;

        try {
            rating = Integer.parseInt(ratinInput);
        } catch (NumberFormatException e) {
            view.displayError("Invalid rating value. Must be a number");
            return;
        }

        if (rating < 1 || rating > 5) {
            view.displayError("Rating must be between 1 and 5.");
            return;
        }

        //Get optional: comment
        String comment = view.getInput("Enter review comment (press Enter to skip): ");

        if (comment != null && comment.trim().isEmpty()) {
            comment = null;
        }

        //Add review to performance
        performance.review(rating, comment);

        //display success
        view.displaySuccess("Review submitted successfully");
    }

    public void cancelBooking() {
        // Implementation for cancelling a booking
    }

    private void addBooking(Booking b) {
        // Implementation for adding a booking
    }

    private Performance getPerformanceByID(long performanceID) {
        // Implementation for getting performanceID
        for (Performance perf : performances) {
            if (perf.getID() == performanceID) {
                return perf;
            }
        }
        return null; // Placeholder return value
    }

    private boolean checkIfBookingPossible(Performance performance, int numTickets) {
        // Implementation for checking if a booking is possible
        return false; // Placeholder return value
    }

    private Collection<Booking> findBookingsByEventID(long eventID) {
        // Implementation for finding a booking by its eventID
        return null; // Placeholder return value
    }

    private Booking getBookingByNumber(long bookingNumber) {
        // Implementation for finding a booking by its eventID
        return null; // Placeholder return value
    }

}