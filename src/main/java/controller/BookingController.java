package controller;

import interfaces.TextUserInterface;
import object.Booking;
import object.Performance;
import user.User;

import interfaces.View;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * BookingController handles actions related to bookings.
 */
public class BookingController extends Controller {

    private long nextBookingNumber;
    private Collection<Booking> bookings;
    private Collection<Performance> performances;

    private View view;

    public BookingController(View view) {
        this.nextBookingNumber = 1;

        this.view = view;
        bookings = new ArrayList<>();
        performances = new ArrayList<>();
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
    }

    public void cancelBooking() {
        // Implementation for cancelling a booking
    }

    private void addBooking(Booking b) {
        // Implementation for adding a booking
    }

    private Performance getPerformanceByID(long performanceID) {
        // Implementation for getting performanceID
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