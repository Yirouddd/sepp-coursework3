package controller;

import enums.BookingStatus;
import enums.PerformanceStatus;
import external.MockPaymentSystem;
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
 * Handles booking-related use cases.
 */
public class BookingController extends Controller {
    private long nextBookingNumber;
    private static Collection<Booking> bookings;
    private View view;
    private PaymentSystem paymentSystem;
    private EventPerformanceController eventPerformanceController;

    /**
     * Constructs BookingController.
     *
     * @param view UI view
     * @param eventPerformanceController shared event/performance controller
     */
    public BookingController(View view, EventPerformanceController eventPerformanceController) {
        this.nextBookingNumber = 1;
        this.view = view;
        this.bookings = new ArrayList<>();
        this.paymentSystem = new MockPaymentSystem();
        this.eventPerformanceController = eventPerformanceController;

    }

    /**
     * Shared helper to ensure current user is a student.
     *
     * @return true if current user is student
     */
    private boolean ensureStudent() {
        View view = new TextUserInterface();
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can perform this action.");
            return false;
        }
        return true;
    }

    /**
     * Books a performance.
     */
    public void bookPerformance() {
        if (!ensureStudent()){
            return;
        }

        Student student = (Student) currentUser;
        Performance performance = null;

        while (performance == null) {
            try {
                long performanceID = Long.parseLong(view.getInput("Enter performance ID: "));
                performance = getPerformanceByID(performanceID);

                if (performance == null) {
                    view.displayError("Invalid performance ID.");
                    continue;
                }

                if (!performance.checkIfEventIsTicketed()) {
                    view.displayError("This performance is not ticketed, so no booking is needed.");
                    return;
                }
            } catch (NumberFormatException e) {
                view.displayError("Please enter a valid numeric performance ID.");
            }
        }

        int numTickets;
        try {
            numTickets = Integer.parseInt(view.getInput("Enter number of tickets: "));
        } catch (NumberFormatException e) {
            view.displayError("Number of tickets must be a valid integer.");
            return;
        }

        if (!checkIfBookingPossible(performance, numTickets)) {
            view.displayError("Not enough tickets available.");
            return;
        }

        double totalCost = performance.getFinalTicketPrice() * numTickets;

        boolean paymentSuccessful = paymentSystem.processPayment(
                numTickets,
                performance.getEventTitle(),
                student.getEmail(),
                student.getPhoneNumber(),
                performance.getOrganiserEmail(),
                totalCost
        );

        if (!paymentSuccessful) {
            view.displayError("Payment was unsuccessful therefore booking unsuccessful.");
            return;
        }

        Booking booking = new Booking(
                student,
                performance,
                nextBookingNumber++,
                numTickets,
                totalCost,
                LocalDateTime.now()
        );

        addBooking(booking);
        performance.addBooking(booking);
        student.addBooking(booking);

        view.displaySuccess("Booking successful.");
        view.displayBookingRecord(booking.generateBookingRecord());
    }

    public void reviewPerformance() {
        // Implementation for reviewing performance
    }

    public void cancelBooking() {
        // Implementation for cancelling a booking
    }

    /**
     * Adds booking to system store.
     *
     * @param b booking
     */
    private void addBooking(Booking b) {
        if (b != null) {
            bookings.add(b);
        }
    }

    /**
     * Removes booking from shared booking store.
     *
     * @param booking booking to remove
     */
    public static void removeBookingFromSystem(Booking booking) {
        bookings.remove(booking);
    }

    /**
     * Looks up performance by id using shared performance controller.
     *
     * @param performanceID performance id
     * @return performance or null
     */
    private Performance getPerformanceByID(long performanceID) {
        return eventPerformanceController.findPerformanceById(performanceID);
    }

    /**
     * Checks whether booking can proceed.
     *
     * @param performance performance
     * @param numTickets requested tickets
     * @return true if booking is allowed
     */
    private boolean checkIfBookingPossible(Performance performance, int numTickets) {
        if (performance == null) {
            view.displayError("Performance does not exist.");
            return false;
        }

        if (performance.getStatus() != PerformanceStatus.ACTIVE) {
            view.displayError("Cancelled performances cannot be booked.");
            return false;
        }

        if (!performance.checkHasNotHappenedYet()) {
            view.displayError("Cannot book a performance that has already started or ended.");
            return false;
        }

        if (numTickets <= 0) {
            view.displayError("Number of tickets must be positive.");
            return false;
        }

        if (!performance.checkIfEventIsTicketed()) {
            view.displayError("This performance is not ticketed.");
            return false;
        }

        if (!performance.checkIfTicketsLeft(numTickets)) {
            view.displayError("Not enough tickets left.");
            return false;
        }

        return true;
    }

    /**
     * Finds bookings by event id.
     *
     * @param eventID event id
     * @return matching bookings
     */
    private Collection<Booking> findBookingsByEventID(long eventID) {
        Collection<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getPerformance().getEventId() == eventID) {
                result.add(booking);
            }
        }
        return result;
    }

    /**
     * Gets booking by booking number.
     *
     * @param bookingNumber booking number
     * @return booking or null
     */
    private Booking getBookingByNumber(long bookingNumber) {
        for (Booking booking : bookings) {
            if (booking.getBookingNumber() == bookingNumber) {
                return booking;
            }
        }
        return null;
    }


}