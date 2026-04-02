package controller;

import user.User;
import object.Event;
import object.Performance;
import interfaces.View;

import java.util.List;
import java.util.ArrayList;

public class EventPerformanceController extends Controller {

    private long nextEventID;
    private long nextPerformanceID;

    private View view;

    private List<Event> events;
    private List<Performance> performances;

    public EventPerformanceController(User currentUser, View view) {

        super(currentUser, view);
        this.view = view;
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

        // show performance
        view.displaySpecificPerformance(performance.toString());

        // collect all performances of a particular event
        String eventTitle = performance.getEventTitle();
        double averageEventRating = getAverageRatingForEvent(eventTitle);
        view.displaySuccess("Event average rating: " + averageEventRating);

        List<String> allReviews = getAllReviewsForEvent(eventTitle);

        if (allReviews.isEmpty()) {
            view.displaySuccess("No reviews were added for this event yet.");
        }
        else {
            view.displaySuccess("All reviews for event: ");
            for (String review : allReviews) {
                view.displaySuccess(review);
            }
        }
    }

    public double getAverageRatingForEvent(String eventTitle) {
        int totalRating = 0;
        int ratingCount = 0;

        for (Performance p : performances) {
            if (p.getEventTitle().equals(eventTitle)) {
                for (int r : p.getReviewsRatings()) {
                    totalRating += r;
                    ratingCount++;
                }
            }
        }
        if (ratingCount != 0) {
            return (double) (totalRating / ratingCount);
        }
        else {
            return 0.0;
        }
    }

    public List<String> getAllReviewsForEvent(String eventTitle) {
        List<String> allReviews = new ArrayList<>();
        for (Performance p : performances) {
            if (p.getEventTitle().equals(eventTitle)) {
                List<Integer> ratings = new ArrayList<>(p.getReviewsRatings());
                List<String> comments = new ArrayList<>(p.getReviewsComments());

                for (int i = 0; i < ratings.size(); i++) {
                    allReviews.add("Performance " + p.getPerformanceId() + " - " +
                            ratings.get(i) + " - " + comments.get(i));
                }
            }
        }
        return allReviews;
    }

    public void cancelPerformance() {

    }

    private boolean checkIfSponsorshipPossible(Performance performance, int amount) {

        return false;
    }

    public void sponsorPerformance() {

    }

    private void addEvent(Event e) {

    }

    public void addPerformance(Performance p) {
        performances.add(p);
    }

    private Event getEventByID(long eventID) {

        return null;
    }

    private Event getEventByTitle(String title) {

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