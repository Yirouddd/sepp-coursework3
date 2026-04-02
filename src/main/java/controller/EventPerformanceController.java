package controller;

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

    public void cancelPerformance() {}

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