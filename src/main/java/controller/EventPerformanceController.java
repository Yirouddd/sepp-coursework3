package controller;

import enums.BookingStatus;
import enums.PerdformanceStatus;
import external.MockPaymentSystem;
import external.PaymentSystem;
import interfaces.TextUserInterface;
import interfaces.View;
import object.Booking;
import user.User;
import object.Event;
import object.Performance;

import interfaces.View;

import java.util.List;
import java.util.ArrayList;

public class EventPerformanceController extends Controller {

    private long nextEventID;
    private long nextPerformanceID;

    private List<Event> events;
    private List<Performance> performances;

    // Todo: the argument is wrong
    public EventPerformanceController(User currentUser, View view) {
        this.nextEventID = 1;
        this.nextPerformanceID = 1;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
    }

    public Event createEvent() {

        return null;
    }

    public void searchforPerformances() {

    }

    public void viewPerformance() {

    }

    public void cancelPerformance() {
        View view = new TextUserInterface();
        PaymentSystem paymentSystem = new MockPaymentSystem();

        Performance performance;
        String organiserMessage;

        while (true){

            String performanceInput = view.getInput("Enter performance ID to cancel: ");

            // performance == null
            if (performanceInput == null || performanceInput.trim().isEmpty()){
                view.displayError("Performance ID cannot be empty.");
                continue;
            }

            long performanceID;
            try {
                performanceID = Long.parseLong(performanceInput.trim());
            } catch (NumberFormatException e) {
                view.displayError("Invalid performance ID.");
                continue;
            }

            // sameEP == false
            performance = getPerformanceByID(performanceID);

            if (performance == null) {
                view.displayError("Performance with given number does not exist.");
                continue;
            }

            // sameEP == false
            if (performance.getEvent() == null ||
                performance.getEvent().getOrganiserName() == null ||
                !performance.getEvent().getOrganiser().equals(currentUser)) {
                view.displayError("The performance with given number does not belong to you.");
                continue;
            }

            if (performance.getStatus() == PerdformanceStatus.CANCELLED) {
                view.displayError("This performance has already been cancelled.");
                return;
            }

            if (!performance.checkHasNotHappenedYet()) {
                view.displayError("Performance can't be cancelled as it has already happened.");
                return;
            }

            break;
        }

        // message
        while (true) {
            organiserMessage = view.getInput("Provide a cancellation message for affected students: ");

            if (organiserMessage == null || organiserMessage.trim().isEmpty()) {
                view.displayError("Please provide a non-empty message for the students.");
                continue;
            }
            break;
        }



        if (performance.hasActiveBooking()) {

            for (Booking booking : performance.getBookings()){
                if (booking == null || (booking.getBookingStatus() != BookingStatus.ACTIVE)) {
                    continue;
                }

                boolean refundSuccess = paymentSystem.processRefund(
                        booking.getNumTickets(),
                        performance.getEventTitle(),
                        booking.getStudentEmail(),
                        booking.getStudentPhone(),
                        performance.getOrganiserEmail(),
                        booking.getTransactionAmount(),
                        organiserMessage
                );

                // 1c.2a: one refund failed -> whole cancellation fails
                if (!refundSuccess) {
                    view.displayError("The performance could not be cancelled because refund processing was unsuccessful.");
                    return;
                }
                booking.cancelByProvider();
            }
        }
        // 1 / 1c.3: all refunds successful or no bookings
        performance.cancel();
        view.displaySuccess("The performance has been cancelled and any refunds have been processed.");
    }

    private boolean checkIfSponsorshipPossible(Performance performance, int amount) {
        return false;
    }

    public void sponsorPerformance() {

    }

    private void addEvent(Event e) {

    }

    private void addPerformance(Performance p) {

    }

    private Event getEventByID(long eventID) {

        return null;
    }

    private Event getEventByTitle(String title) {

        return null;
    }

    private Performance getPerformanceByID(long performanceID) {

        return null;
    }
}