package object;

import enums.EventType;
import user.EntertainmentProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ArrayList;

import java.util.List;

/**
 * Event class
 */


public class Event{

    private long eventID;
    private String eventTitle;
    private EventType type;
    private boolean isTicketed;
    private List<Performance> performances;
    private EntertainmentProvider entertainmentProvider;

    public Event(long eventID, String eventTitle, EventType type, boolean isTicketed) {
        this.eventID = eventID;
        this.eventTitle = eventTitle;
        this.type = type;
        this.isTicketed = isTicketed;
        this.performances = new ArrayList<>();
    }
    public Performance createPerformance(long performanceID,
                                         LocalDateTime startDateTime,
                                         LocalDateTime endDateTime,
                                         Collection<String> performerNames,
                                         String venueAddress,
                                         int venueCapacity,
                                         boolean venueIsOutdoors,
                                         boolean venueIsSmoking,
                                         int numTicketsTotal,
                                         double ticketPrice) {
        Performance p = new Performance(
                performanceID, this, startDateTime,
                endDateTime, performerNames, venueAddress, venueCapacity,
                venueIsOutdoors, venueIsSmoking, numTicketsTotal, ticketPrice
        );
        addPerformance(p);
        return p;
    }

    public Performance getPerformanceByID(long performanceID) {
        for (Performance p : performances) {
            if (p.getPerformanceId() == performanceID) {
                return p;
            }
        }
        return null;
    }

    public Collection<String> getInfoOfPerformancesOnDate(LocalDateTime searchDate) {
        // TODO
        return null;
    }

    public String getOrganiserName() {
        return entertainmentProvider.getOrgName();
    }

    public String getOrganiserEmail() {
        return entertainmentProvider.getEmail();
    }

    public double getAverageRatingOfPerformances() {
        if (performances.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int count = 0;

        for (Performance p : performances) {
            double average = p.getAverageRating();

            if (average > 0) {
                total += average;
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return (double) total / count;
    }

    public Collection<String> getAllPerformanceReviews() {
        List<String> reviews = new ArrayList<>();
        for (Performance p : performances) {
            for (String comment : p.getReviewsComments()) {
                reviews.add("Performance " + p.getPerformanceId() + ": " + comment);
            }
        }
        return reviews;
    }

    public boolean hasPerformanceAtSameTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        for (Performance p : performances) {
            if (startDateTime.isBefore(p.getEndDateTime()) && endDateTime.isAfter(p.getStartDateTime())) {
                return true;
            }
        }
        return false;
    }

    public void addPerformance(Performance p) {
        if (p != null && !performances.contains(p)) {
            performances.add(p);
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("---Event Details---\n");
        result.append("Event ID: ").append(eventID).append("\n");
        result.append("Title: ").append(eventTitle).append("\n");
        result.append("Type: ").append(type).append("\n");
        result.append("Ticketed: ").append(isTicketed).append("\n");

        if (entertainmentProvider != null) {
            result.append("Organiser: ").append(getOrganiserName()).append(" - ").append(getOrganiserEmail()).append("\n");
        } else {
            result.append("Organiser: Not assigned\n");
        }

        result.append("Number of performances: ").append(performances.size()).append("\n");
        result.append("Performances: \n");
        for (Performance p : performances) {
            result.append(" - ").append(p.getPerformanceId()).append("\n");
        }
        return result.toString();
    }

    //getters
    public long getEventID() {
        return eventID;
    }

    public boolean isTicketed() {
        return isTicketed;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public EventType getEventType () {
        return type;
    }

    public void setOrganizer(EntertainmentProvider entertainmentProvider) {
        this.entertainmentProvider = entertainmentProvider;
    }

}