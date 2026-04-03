package controller;

import enums.BookingStatus;
import enums.PerformanceStatus;
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

    private View view;

    public EventPerformanceController(User currentUser, View view) {
        this.nextEventID = 1;
        this.nextPerformanceID = 1;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
        this.view = view;
    }

    public Event createEvent() {

        return null;
    }

    public void searchforPerformances() {

    }

    public void viewPerformance() {
        if (performances.isEmpty()) {
            view.displayError("No performances available.");
            return;
        }

        Performance performance = null;
        int attempts = 0;

        while (performance == null && attempts < 7) {
            attempts++;
            try {
                String input = view.getInput("Enter performance ID: ");
                long performanceID = Long.parseLong(input);
                performance = getPerformanceByID(performanceID);
                if (performance == null) {
                    view.displayError("Invalid ID. Please try again.");
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid input. Please enter a number.");
            }
        }

        if (performance == null) {
            view.displayError("Too many unsuccessful attempts were made");
            return;
        }

        // show performance details
        view.displaySpecificPerformance(performance.toString());

        //get the event by eventID
        Event event = getEventByID(performance.getEventId());

        if (event == null) {
            view.displayError("Associated event not found");
        }

        // show event details
        view.displaySuccess("\n---Event Details---");
        assert event != null;
        view.displaySuccess(event.toString());

        // show average rating for the event
        double averageRating = event.getAverageRatingOfPerformances();
        view.displaySuccess("Event average rating: " + averageRating);

        // show all reviews for the event
        List<String> allReviews =
                new ArrayList<>(event.getAllPerformanceReviews());

        if (allReviews.isEmpty()) {
            view.displaySuccess("No reviews were added to this event yet");
        }
        else {
            view.displaySuccess("All reviews for the event: ");
            for (String review : allReviews) {
                view.displaySuccess(" - " + review);
            }
        }
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
                !performance.getEvent().getOrganiserName().equals(currentUser)) {
                view.displayError("The performance with given number does not belong to you.");
                continue;
            }

            if (performance.getStatus() == PerformanceStatus.CANCELLED) {
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

    public void addEvent(Event e) {
        if (e != null) {
            events.add(e);
        }
    }

    public void addPerformance(Performance p) {
        if (p != null) {
            performances.add(p);

            Event e = getEventByID(p.getEventId());
            if (e != null) {
                e.addPerformance(p);
            }
        }
    }

    private Event getEventByID(long eventID) {
        for (Event e : events) {
            if (e.getEventID() == eventID) {
                return e;
            }
        }
        return null;
    }

    private Event getEventByTitle(String title) {
        for (Event e : events) {
            if (e.getTitle() == title) {
                return e;
            }
        }
        return null;
    }

    private Performance getPerformanceByID(long performanceID) {
        for (Performance p : performances) {
            if (p.getPerformanceId() == performanceID) {
                return p;
            }
        }
        return null;
    }
}