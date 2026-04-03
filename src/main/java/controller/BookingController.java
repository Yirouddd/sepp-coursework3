package controller;

import enums.BookingStatus;
import external.PaymentSystem;
import interfaces.TextUserInterface;
import object.Booking;
import object.Performance;
import object.Event;
import user.User;
import user.Student;

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
    private PaymentSystem paymentSystem;

    public BookingController(User currentUser, View view, List<Performance> performances, PaymentSystem paymentSystem) {
        super(currentUser, view);
        this.nextBookingNumber = 1;
        this.bookings = new ArrayList<>();
        this.performances = performances;
        this.paymentSystem = paymentSystem;
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
        //1. check if current user is a Student
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can cancel bookings.");
            return;
        }

        //2. get booking number from user
        String bookingNumInput = view.getInput("Enter booking number: ");
        long bookingNumber;

        try {
            bookingNumber = Long.parseLong(bookingNumInput);
        } catch (NumberFormatException e) {
            view.displayError("Invalid booking number. Must be a number");
            return;
        }

        //3. find the booking
        Booking booking = getBookingByNumber(bookingNumber);

        if (booking == null) {
            view.displayError("Booking not found");
            return;
        }

        //verify booking belongs to current student
        if (!booking.getStudent().getEmail().equals(currentUser.getEmail())) {
            view.displayError("You are not allowed to review this booking.");
            return;
        }

        //4. check if booking is already cancelled
        if (booking.getStatus() == BookingStatus.CANCELLEDBYSTUDENT ||
                booking.getStatus() == BookingStatus.CANCELLEDBYPROVIDER) {
            view.displayError("Booking is already cancelled.");
            return;
        }

        // 5. get performance details
        Performance performance = getPerformanceByID(booking.getPerformance().getID());

        if (performance == null) {
            view.displayError("Performance not found");
            return;
        }

        //6. check if >24 hours before performance
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime performanceStart = performance.getStartDateTime();
        long hoursUntilPerformance = java.time.temporal.ChronoUnit.HOURS.between(now, performanceStart);

        if (hoursUntilPerformance <= 24) {
            view.displayError("Cannot cancel within 24 hours of performance start.");
            return;
        }

        //7. process refuned via PaymentSystem
        boolean refundSucess = paymentSystem.processRefund(
                booking.getNumTickets(),
                performance.getEventTitle(),
                currentUser.getEmail(),
                ((Student) currentUser).getPhoneNumber(),
                performance.getOrganiserEmail(),
                booking.getAmountPaid(),
                "" //no organiser message for student cancellation
        );

        if (!refundSucess) {
            view.displayError("Refund failed. Booking not cancelled.");
            return;
        }

        //update booking status and display success message
        booking.setStatus(BookingStatus.CANCELLEDBYSTUDENT);
        view.displaySuccess("Booking cancelled successfully. Refund of GBP" +
                            booking.getAmountPaid() + "has been processed.");
    }
    public void addBooking(Booking b) {
        // Implementation for adding a booking
        if (b != null) {
            bookings.add(b);
        }
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
        for (Booking b : bookings) {
            if (b.getBookingNumber() == bookingNumber) {
                return b;
            }
        }
        return null;
    }

}