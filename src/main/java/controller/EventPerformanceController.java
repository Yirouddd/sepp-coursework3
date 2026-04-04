package controller;

import enums.BookingStatus;
import enums.PerformanceStatus;
import external.MockPaymentSystem;
import external.PaymentSystem;
import interfaces.TextUserInterface;
import interfaces.View;
import object.Booking;
import user.Student;
import user.StudentPreferences;
import user.User;
import object.Event;
import object.Performance;

import interfaces.View;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class EventPerformanceController extends Controller {

    private long nextEventID;
    private long nextPerformanceID;

    private Collection<Event> events;
    private Collection<Performance> performances;

    private View view;
    Performance performance;
    PaymentSystem paymentSystem;

    // Constructor for testing
    public EventPerformanceController(View view) {
        this.nextEventID = 1;
        this.nextPerformanceID = 1;

        this.view = view;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
        paymentSystem = new MockPaymentSystem();
    }

    public Event createEvent() {

        return null;
    }

    public void searchForPerformances() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate targetDate = null;

        // 1a: keep asking until the date format is correct
        while (targetDate == null) {
            String targetDateRow = view.getInput("Please enter a date (yyyy-MM-dd): ");

            try {
                targetDate = LocalDate.parse(targetDateRow, inputFormatter);
            } catch (DateTimeParseException e) {
                view.displayError("Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        // Step 1: find all performances on the target date
        List<Performance> specificPerformance = new ArrayList<>();

        for (Performance performance : performances) {
            if (performance == null || performance.getStatus() != PerformanceStatus.ACTIVE) {
                continue;
            }
            LocalDate startDate = performance.getStartDateTime().toLocalDate();
            LocalDate endDate = performance.getEndDateTime().toLocalDate();

            // performance is on the target date if the target date falls within [startDate, endDate]
            if (!startDate.isAfter(targetDate) && !endDate.isBefore(targetDate)) {
                specificPerformance.add(performance);
            }
        }

        // 1b: no performances on the provided date
        if (specificPerformance.isEmpty()) {
            view.displayError("There are no performances on " + targetDate + ".");
            return;
        }

        // 1c: if current user is a student with preferences, matching events go first
        User user = getCurrentUser();
        if (user instanceof Student) {
            Student student = (Student) user;
            StudentPreferences studentPreferences = student.getStudentPreferences();

            if (studentPreferences != null) {
                specificPerformance.sort(
                        Comparator
                                .comparingInt((Performance p) -> {
                                    Event event = getEventByID(p.getEventId());
                                    return (event != null &&
                                            studentPreferences.matchesStudentPreference(event.getEventType())) ? 0 : 1;
                                })
                                .thenComparing(Performance::getStartDateTime)
                );
            } else {
                specificPerformance.sort(Comparator.comparing(Performance::getStartDateTime));
            }
        } else {
            specificPerformance.sort(Comparator.comparing(Performance::getStartDateTime));
        }

        // display all performances
        view.displaySuccess("Performances on " + targetDate + ":");

        for (Performance performance : specificPerformance) {
            Event event = getEventByID(performance.getEventId());

            String organiserName = "Unknown organiser";
            double eventAverageRating = 0.0;

            if (event != null) {
                organiserName = event.getOrganiserName();
                eventAverageRating = event.getAverageRatingOfPerformances();
            }

            String result =
                    "Performance ID: " + performance.getPerformanceId()
                            + " Event: " + performance.getEventTitle()
                            + " Time: " + performance.getStartDateTime().toLocalTime().format(timeFormatter)
                            + " - " + performance.getEndDateTime().toLocalTime().format(timeFormatter)
                            + " Venue: " + performance.getVenueAddress()
                            + " EP: " + organiserName
                            + " Event average rating: " + String.format("%.2f", eventAverageRating);

            view.displaySuccess(result);
        }
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
        if (!performance.checkIfEventIsTicketed()) {
            view.displayError("Sponsorship cannot be applied to non-ticketed " +
                    "performances.");
            return false;
        }
        if (amount <= 0) {
            view.displayError("Sponsorship must be positive.");
            return false;
        }
        return true;
    }

    public void sponsorPerformance() {
        if (performances.isEmpty()) {
            view.displayError("No performances available to sponsor.");
            return;
        }

        Performance performance = null;

        while (performance == null) {
            try {
                String input = view.getInput("Enter performance ID to " +
                        "sponsor: ");
                long performanceID = Long.parseLong(input);
                performance = getPerformanceByID(performanceID);
                if (performance == null) {
                    view.displayError("Performance with given ID does not " +
                            "exist");
                }
            }
            catch (NumberFormatException e) {
                view.displayError("Invalid input. Please reenter a " +
                        "performance ID.");
            }
            catch (NoSuchElementException e) {
                view.displayError("No input provided, cancelling sponsorship.");
                return;
            }
        }

        // check if the event is ticketed
        if (!performance.checkIfEventIsTicketed()) {
            view.displayError("The requested performance event is not " +
                    "ticketed. It cannot be sponsored.");
            return;
        }

        // get valid sponsorship amount
        double amount = -1;
        while (amount <= 0 || amount > performance.getTicketPrice()) {
            try {
                String input =
                        view.getInput("Enter sponsorship amount: £" + performance.getTicketPrice());
                amount = Double.parseDouble(input);
                if (amount <= 0 || amount > performance.getTicketPrice()) {
                    view.displayError("Invalid amount. It cannot be less than" +
                            " 0 or bigger than ticket price.");
                }
            }
            catch (NumberFormatException e) {
                view.displayError("Invalid input. Please enter a number.");
            }
            catch (NoSuchElementException e) {
                view.displayError("No input provided, cancelling sponsorship.");
                return;
            }
        }

        performance.sponsor(amount);

        view.displaySuccess("Sponsorship successful! You sponsored a " +
                "performance with ID " + performance.getPerformanceId() +
                "with £" + amount);
    }

    public void addEvent(Event e) {
        if (e != null) {
            events.add(e);
        }
    }

    public void addPerformance(Performance p) {
        if (p != null) {
            performances.add(p);
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
            if (e.getEventTitle() == title) {
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