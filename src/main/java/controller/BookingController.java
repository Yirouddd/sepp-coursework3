package controller;

import object.Booking;
import object.Performance;
import object.Event;
import interface1.TextUserInterface;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * BookingController.java
 */
public class BookingController {
    //main.user.StudentPreferences studentPreferences;

    //added fields needed
    private TextUserInterface view;
    private Collection<Event> events;
    private long nextBookingNumber = 1;

    // Constructor
    public BookingController(TextUserInterface view, Collection<Event> events) {
        this.view = view;
        this.events = events;
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

        // ===== STEP 7: Display success =====
        view.displaySuccess("Review submitted successfully!");
    }


    public void cancelBooking() {
        // Implementation for cancelling a booking
    }

    private void addBooking(Booking b) {
        // Implementation for adding a booking
    }

    private Performance getPerformanceByID(long performanceID) {
        for (Event event : events) {
            for (Performance perf : event.getPerformances()) {
                if (perf.getID() == performanceID) {
                    return perf;
                }
            }
        }
        return null;
    }

    private boolean checkIfBookingPossible(Performance performance, int numTickets) {
        // Implementation for checking if a booking is possible
        return false; // Placeholder return value
    }

    private String findBookingsByEventID(long eventID) {
        // Implementation for finding a booking by its eventID
        return null; // Placeholder return value
    }

    private String getBookingByNumber(long bookingNumber) {
        // Implementation for finding a booking by its eventID
        return null; // Placeholder return value
    }



}