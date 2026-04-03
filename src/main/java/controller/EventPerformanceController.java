package controller;

import enums.EventType;
import interfaces.TextUserInterface;
import interfaces.View;
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
        super(currentUser, view); //made changes and added super call
        this.nextEventID = 1;
        this.nextPerformanceID = 1;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
    }

    public Event createEvent() {

        //1. check if current user is EntertainmentProvider
        if (!checkCurrentUserIsEntertainmentProvider()) {
            view.displayError("Only Entertainment Providers can create events.");
            return null;
        }

        //2. get event title
        String title = view.getInput("Enter event title: ");
        while (title == null || title.trim().isEmpty()) {
            view.displayError("Title cannot be empty.");
            title = view.getInput("Enter event title: ");
        }

        //3. check for duplicate title
        if (getEventByTitle(title) != null) {
            view.displayError("Event with this title already exists.");
            return null;
        }

        //4. get event type
        String typeInput = view.getInput("Enter event type (MUSIC/THEATRE/DANCE/MOVIE/SPORTS): ");
        EventType type;
        try {
            type = EventType.valueOf(typeInput.toUpperCase());
        } catch (IllegalArgumentException e) {
            view.displayError("Invalid event type.");
            return null;
        }

        //5. get ticketed flag
        String ticketedInput = view.getInput("Is this event ticketed? (yes/no): ");
        boolean isTicketed = ticketedInput.equalsIgnoreCase("yes") || ticketedInput.equalsIgnoreCase("y");

        //6. create event
        long eventID = nextEventID++;
        Event event = new Event();
        //TODO: set event fields (title, type, isTicketed, eventID)

        //7. get performances
        int performanceCount = 0;
        String addMore = view.getInput("Add a performance? (yes/no): ");

        while (addMore.equalsIgnoreCase("yes") || addMore.equalsIgnoreCase("y")) {
            //get performance details
            String startInput = view.getInput("Enter start date/time (yyyy-MM-dd HH:mm): ");
            // parse and validate all performance fields
            performanceCount++;
            addMore = view.getInput("Add another performance? (yes/no): ");
        }

        if (performanceCount == 0) {
            view.displayError("Event must have at least 1 performance.");
            return null;
        }

        addEvent(event);
        view.displaySuccess("Event created successfully!");
        return event;

    }

    public void searchforPerformances() {

    }

    public void viewPerformance() {

    }

    public void cancelPerformance() {}

    private boolean checkIfSponsorshipPossible(Performance performance, int amount) {
        return false;
    }

    public void sponsorPerformance() {

    }

    private void addEvent(Event e) {
        if (e != null) {
            events.add(e);
        }
    }

    private void addPerformance(Performance p) {

    }

    private Event getEventByID(long eventID) {

        return null;
    }

    private Event getEventByTitle(String title) {
        if (title == null) return null;
        for (Event e : events ) {
            if (e.getTitle().equalsIgnoreCase(title)) {
                return e;
            }
        }
        return null;
    }

    private Performance getPerformanceByID(long performanceID) {

        return null;
    }
}