package controller;

import user.User;
import object.Event;
import object.Performance;

import interfaces.View;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

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

        while (performance == null) {
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