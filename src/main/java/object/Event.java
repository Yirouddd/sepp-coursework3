package object;

import enums.EventType;
import user.EntertainmentProvider;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents an event with a title, type, performances, and an entertainment
 * provider.
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
     * @param isTicketed true if event is ticketed
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
     * @param performanceID performance ID
     * @param startDateTime start time
     * @param endDateTime end time
     * @param performerNames names of performers
     * @param venueAddress venue address
     * @param venueCapacity venue capacity
     * @param venueIsOutdoors true if venue is outdoors
     * @param venueIsSmoking true if smoking is allowed
     * @param numTicketsTotal total tickets
     * @param ticketPrice ticket price
     * @return the created performance
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
     * Returns a performance by its id.
     *
     * @param performanceID performance id
     * @return the performance or null if not found
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
     * Returns string information of performances on a specific date.
     *
     * @param searchDate date/time to search by date
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

    /**
     * Returns the organiser's name.
     *
     * @return organiser name or "Unknown organiser" if not set
     */
    public String getOrganiserName() {
        return entertainmentProvider == null ? "Unknown organiser" : entertainmentProvider.getOrgName();
    }

    /**
     * Returns the organiser's email.
     *
     * @return organiser name or "Unknown email" if not set
     */
    public String getOrganiserEmail() {
        return entertainmentProvider == null ? "Unknown email" : entertainmentProvider.getEmail();
    }

    /**
     * Computes average rating across all performances that have reviews.
     *
     * @return average rating or 0.0 if no reviews exist
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
     * Returns all reviews from all performances.
     *
     * @return collection of review strings
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
     * Checks if any performance overlaps with the given times.
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
     * @param p performance to add
     */
    public void addPerformance(Performance p) {
        if (p != null && !performances.contains(p)) {
            performances.add(p);
        }
    }

    /**
     * Removes a performance from the event.
     *
     * @param p performance to remove
     */
    public void removePerformance(Performance p) {
        performances.remove(p);
    }

    /**
     * Returns a string representation of the event.
     *
     * @return event details
     */
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

    /**
     * Returns the event ID.
     *
     * @return event ID
     */
    public long getEventID() {
        return eventID;
    }

    /**
     * Returns whether the event is ticketed.
     *
     * @return true if ticketed
     */
    public boolean isTicketed() {
        return isTicketed;
    }

    /**
     * Returns the event title.
     *
     * @return event title
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Returns the event type.
     *
     * @return event type
     */
    public EventType getEventType() {
        return type;
    }

    /**
     * Sets the entertainment provider for this event.
     *
     * @param entertainmentProvider provider to set
     */
    public void setOrganizer(EntertainmentProvider entertainmentProvider) {
        this.entertainmentProvider = entertainmentProvider;
    }

    /**
     * Returns all performances of the event.
     *
     * @return list of performances
     */
    public List<Performance> getPerformances() {
        return new ArrayList<>(performances);
    }
}