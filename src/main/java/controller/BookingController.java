package controller;

import interfaces.TextUserInterface;
import object.Booking;
import object.Performance;
import object.Event;
import user.User;
import interfaces.View;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * BookingController handles actions related to bookings.
 */
public class BookingController extends Controller {
    // main.user.StudentPreferences studentPreferences;

    private long nextBookingNumber;
    private List<Booking> bookings;
    private List<Performance> performances;

    public BookingController(User currentUser, View view, List<Performance> performances) {
        super(currentUser, view);
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
        // Implementation for reviewing performance
        // ask user which performance to review
        String perfIDInput = view.getInput("Enter performance ID to review: ");
        long performanceID;

        //convert string to long
        try {
            performanceID = Long.parseLong(perfIDInput);
        } catch (NumberFormatException e) {
            //show error and exit if parsing fails (non-numeric input)
            view.displayError("Performance ID must be a number.");
            return;
        }

        //2. Find the performance
        // Search through all events to find this performance by its ID
        Performance performance = getPerformanceByID(performanceID);

        if (performance == null) {
            view.displayError("Performance not found.");
            return;
        }

        //3. Check if performance has already happened, edge case
        // Get the current time
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performanceStart = performance.getStartDateTime();

        //reject the review if performance hasn't happened
        if (now.isBefore(performanceStart)) {
            view.displayError("You can only review performances that have already occurred.");
            return;
        }

        // Rating must be between 1 and 5
        String ratingInput = view.getInput("Enter rating (1-5 stars): ");
        int rating;

        try {
            rating = Integer.parseInt(ratingInput);
        } catch (NumberFormatException e) {
            view.displayError("Rating must be a number.");
            return;
        }

        // Validate rating is 1-5
        if (rating < 1 || rating > 5) {
            view.displayError("Rating must be between 1 and 5.");
            return;
        }

        // Comment is optional (can be null or empty)
        String comment = view.getInput("Enter review comment (press Enter to skip): ");

        // If user pressed Enter without typing anything, treat as no comment
        if (comment != null && comment.trim().isEmpty()) {
            comment = null;
        }

        //6. Add the review to the performance =====
        // TODO: Create a Review object with rating and comment
        // TODO: Add review to the Performance
        // The review should be associated with this specific performance

        performance.review(rating, comment);

        //7. Display success
        view.displaySuccess("Review submitted successfully!");
    }


    public void cancelBooking() {
        // Implementation for cancelling a booking
    }

    private void addBooking(Booking b) {
        // Implementation for adding a booking
    }

    private Performance getPerformanceByID(long performanceID) {
        for (Performance perf : performances) {
            if (perf.getID() == performanceID) {
                return perf;
            }
        }
        return null;
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