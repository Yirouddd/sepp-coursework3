package object;

import enums.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Event class
 */


public class Event{

    private long eventID;
    private String title;
    private EventType type;
    private boolean isTicketed;
    private Collection<Performance> performances;

    public Event() {
        // TODO
        this.performances = new ArrayList<>();
    }
    public Performance createPerformance(long performanceID, LocalDateTime startDateTime, LocalDateTime endDateTime, Collection<String> performerNames, String venueAddress, int venueCapacity, boolean venueIsOutdoors, boolean venueIsSmoking, double ticketPrice) {
        // TODO
        return null;
    }

    public Performance getPerformanceByID(long performanceID) {
        // TODO
        return null;
    }

    public Collection<String> getInfoOfPerformancesOnDate(LocalDateTime searchDate) {
        // TODO
        return null;
    }

    public String getOrganiserName() {
        // TODO
        return null;
    }

    public String getOrganiserEmail() {
        // TODO
        return null;
    }

    public double getAverageRatingOfPerformances() {
        // TODO
        return 0.0;
    }

    public Collection<String> getAllPerformanceReviews() {
        // TODO
        return null;
    }

    public boolean hasPerformanceAtSameTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // TODO
        return false;
    }

    public void addPerformance(Performance p) {
        // TODO
    }

    public String toString() {
        // TODO
        return null;
    }

    public long getEventID() {
        return eventID;
    }

    public String getTitle() {
        return title;
    }

    public Collection<Performance> getPerformances() {
        return performances;
    }

    public void setEventID(long id) {
        this.eventID = id;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public void setType(EventType t) {
        this.type = t;
    }

    public void setTicketed(boolean t) {
        this.isTicketed = t;
    }


}