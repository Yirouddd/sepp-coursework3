package object;

import enums.EventType;
import user.EntertainmentProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Event class
 */


public class Event{

    private long eventID;
    private String eventTitle;
    private EventType type;
    private boolean isTicketed;
    private String organiserName;
    private String organiserEmail;
    private List<Performance> performances;

    public Event(long eventID, String eventTitle, EventType type,
                 boolean isTicketed, String organiserName, String organiserEmail) {
        assert eventID > 0: "Event ID must be positive";
        assert eventTitle != null && !eventTitle.isEmpty(): "Title cannot be " +
                "null or " +
                "empty";
        assert organiserEmail != null && !organiserEmail.isEmpty():
                "Organiser email is required";

        this.eventID = eventID;
        this.eventTitle = eventTitle;
        this.type = type;
        this.isTicketed = isTicketed;
        this.organiserName = organiserName;
        this.organiserEmail = organiserEmail;
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
                performanceID, this.eventID, this.eventTitle, startDateTime,
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
        return organiserEmail;
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
        if (p != null) {
            performances.add(p);
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("---Event Details---\n");
        result.append("Event ID: ").append(eventID).append("\n");
        result.append("Title: ").append(title).append("\n");
        result.append("Type: ").append(type).append("\n");
        result.append("Ticketed: ").append(isTicketed).append("\n");
        result.append("Organiser: ").append(organiserName).append(" - ").append(organiserEmail).append("\n");
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

    public String getTitle() {
        return title;
    }
}