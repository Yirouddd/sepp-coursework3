package object;

import enums.EventType;
import user.EntertainmentProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Event class.
 */
public class Event {
    private long eventID;
    private String eventTitle;
    private EventType type;
    private boolean isTicketed;
    private List<Performance> performances;
    private EntertainmentProvider entertainmentProvider;

    /**
     * Constructs an event.
     *
     * @param eventID event id
     * @param eventTitle event title
     * @param type event type
     * @param isTicketed whether event is ticketed
     */
    public Event(long eventID, String eventTitle, EventType type, boolean isTicketed) {
        this.eventID = eventID;
        this.eventTitle = eventTitle.trim();
        this.type = type;
        this.isTicketed = isTicketed;
        this.performances = new ArrayList<>();
    }

    /**
     * Creates and adds a performance to this event.
     *
     * @return newly created performance
     */
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
                performanceID,
                this,
                startDateTime,
                endDateTime,
                performerNames,
                venueAddress,
                venueCapacity,
                venueIsOutdoors,
                venueIsSmoking,
                numTicketsTotal,
                ticketPrice
        );
        addPerformance(p);
        return p;
    }

    /**
     * Gets a performance by id.
     *
     * @param performanceID performance id
     * @return performance or null
     */
    public Performance getPerformanceByID(long performanceID) {
        for (Performance p : performances) {
            if (p.getPerformanceId() == performanceID) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns info strings for performances on a specific date.
     *
     * @param searchDate date/time to search by date component
     * @return collection of performance info strings
     */
    public Collection<String> getInfoOfPerformancesOnDate(LocalDateTime searchDate) {
        List<String> result = new ArrayList<>();
        if (searchDate == null) {
            return result;
        }

        for (Performance p : performances) {
            if (p.getStartDateTime().toLocalDate().equals(searchDate.toLocalDate())) {
                result.add(p.toString());
            }
        }
        return result;
    }

    public String getOrganiserName() {
        return entertainmentProvider == null ? "Unknown organiser" : entertainmentProvider.getOrgName();
    }

    public String getOrganiserEmail() {
        return entertainmentProvider == null ? "Unknown email" : entertainmentProvider.getEmail();
    }

    /**
     * Computes average rating across performances that have reviews.
     *
     * @return average rating
     */
    public double getAverageRatingOfPerformances() {
        if (performances.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        int count = 0;

        for (Performance p : performances) {
            double avg = p.getAverageRating();
            if (avg > 0) {
                total += avg;
                count++;
            }
        }

        return count == 0 ? 0.0 : total / count;
    }

    /**
     * Gets all reviews from all performances.
     *
     * @return collection of review lines
     */
    public Collection<String> getAllPerformanceReviews() {
        List<String> reviews = new ArrayList<>();

        for (Performance p : performances) {
            List<Integer> ratings = new ArrayList<>(p.getReviewsRatings());
            List<String> comments = new ArrayList<>(p.getReviewsComments());

            for (int i = 0; i < ratings.size(); i++) {
                String comment = "";
                if (i < comments.size()) {
                    comment = comments.get(i);
                }

                reviews.add("Performance " + p.getPerformanceId()
                        + " | Rating: " + ratings.get(i)
                        + " | Comment: " + comment);
            }
        }

        return reviews;
    }

    /**
     * Checks if this event already has a performance overlapping the given time.
     *
     * @param startDateTime start time
     * @param endDateTime end time
     * @return true if overlap exists
     */
    public boolean hasPerformanceAtSameTimes(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        for (Performance p : performances) {
            boolean overlaps = startDateTime.isBefore(p.getEndDateTime())
                    && endDateTime.isAfter(p.getStartDateTime());
            if (overlaps) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a performance to the event.
     *
     * @param p performance
     */
    public void addPerformance(Performance p) {
        if (p != null && !performances.contains(p)) {
            performances.add(p);
        }
    }

    /**
     * Removes a performance from the event.
     *
     * @param p performance
     */
    public void removePerformance(Performance p) {
        performances.remove(p);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("---Event Details---\n");
        result.append("Event ID: ").append(eventID).append("\n");
        result.append("Title: ").append(eventTitle).append("\n");
        result.append("Type: ").append(type).append("\n");
        result.append("Ticketed: ").append(isTicketed).append("\n");
        result.append("Organiser: ").append(getOrganiserName())
                .append(" - ").append(getOrganiserEmail()).append("\n");
        result.append("Number of performances: ").append(performances.size()).append("\n");
        result.append("Performances:\n");

        for (Performance p : performances) {
            result.append(" - ").append(p.getPerformanceId()).append("\n");
        }

        return result.toString();
    }

    public long getEventID() {
        return eventID;
    }

    public boolean isTicketed() {
        return isTicketed;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public EventType getEventType() {
        return type;
    }

    public void setOrganizer(EntertainmentProvider entertainmentProvider) {
        this.entertainmentProvider = entertainmentProvider;
    }

    public List<Performance> getPerformances() {
        return new ArrayList<>(performances);
    }
}