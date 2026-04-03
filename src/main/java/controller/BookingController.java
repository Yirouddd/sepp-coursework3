package controller;

import enums.BookingStatus;
import interfaces.TextUserInterface;
import object.Booking;
import object.Performance;
import user.Student;
import user.User;
import external.PaymentSystem;

import interfaces.View;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;

/**
 * BookingController handles actions related to bookings.
 */
public class BookingController extends Controller {
    // main.user.StudentPreferences studentPreferences;

    private long nextBookingNumber;
    private List<Booking> bookings;
    private List<Performance> performances;
    private View view;

    public BookingController(User currentUser, View view, List<Performance> performances) {
        this.nextBookingNumber = 1;
        this.bookings = new ArrayList<>();
        this.performances = performances;
        this.view = view;
        this.currentUser = currentUser;
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
        View view = this.view;

        if (!ensureStudent()){return;}
        Student student = (Student) currentUser;

        Performance performance = null;
        int numTickets;

        while (performance == null) {
            String input = view.getInput("Enter performance ID:");

            long performanceID;
            try {
                performanceID = Long.parseLong(input.trim());
            } catch (NumberFormatException e) {
                view.displayError("Invalid performance ID. Please provide a correct performance ID.");
                continue;
            }

            Performance perf = getPerformanceByID(performanceID);

            if (perf == null) {
                view.displayError("Invalid performance ID. Please provide a correct performance ID.");
                continue;
            }

            if (!perf.checkIfEventIsTicketed()) {
                view.displaySuccess("This performance is non-ticketed and free to attend. No booking required.");
                return;
            }

            performance = perf;
        }

        try {
            String input = view.getInput("Enter number of tickets:");
            numTickets = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            view.displayError("Invalid number of tickets entered.");
            return;
        }

        if (!checkIfBookingPossible(performance, numTickets)) {
            view.displayError("Not enough tickets available.");
            return;
        }

        double totalCost = performance.getFinalTicketPrice() * numTickets;
        boolean paymentSuccessful = PaymentSystem.processPayment(
                numTickets,
                performance.getEventTitle(),
                student.getEmail(),
                student.getPhoneNumber(),
                "",
                totalCost
        );

        if (!paymentSuccessful) {
            view.displayError("Payment was unsuccessful therefore booking unsuccessful.");
            return;
        }

        long bookingNumber = nextBookingNumber;

        Booking booking = new Booking(
                student,
                bookingNumber,
                numTickets,
                totalCost,
                LocalDateTime.now(),
                BookingStatus.ACTIVE
        );

        addBooking(booking);
        performance.addBooking(booking);
        student.addBooking(booking);

        view.displayBookingRecord(
                "Booking confirmed!\n" +
                "Booking Number: " + bookingNumber + "\n" +
                "Event: " + performance.getEventTitle() + "\n" +
                "Tickets: " + numTickets + "\n" +
                "Total Paid: £" + totalCost
        );
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
        for (Performance p : performances) {
            if (p.getPerformanceId() == performanceID) {
                return p;
            }
        }
        return null;
    }

    private boolean checkIfBookingPossible(Performance performance, int numTickets) {
        return performance.checkIfTicketsLeft(numTickets);
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