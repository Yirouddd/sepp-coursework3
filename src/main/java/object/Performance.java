package object;

import enums.BookingStatus;
import enums.PerformanceStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Performance class represents a performance of an event.
 */
public class Performance {
    private long performanceId;
    private Event event;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Collection<String> performersNames;
    private String venueAddress;
    private int venueCapacity;
    private boolean venueIsOutdoor;
    private boolean venueIsAllowsSmoking;
    private int numTicketsTotal;
    private int numTicketsSold;
    private final double ticketPrice;
    private boolean isSponsored;
    private double sponsoredAmount;
    private Collection<Integer> reviewsRatings;
    private Collection<String> reviewsComments;
    private Collection<Booking> bookings;
    private PerformanceStatus performanceStatus;

    /**
     * Constructs a performance.
     */
    public Performance(long performanceId,
                       Event event,
                       LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       Collection<String> performersNames,
                       String venueAddress,
                       int venueCapacity,
                       boolean venueIsOutdoor,
                       boolean venueIsAllowsSmoking,
                       int numTicketsTotal,
                       double ticketPrice) {

        this.performanceId = performanceId;
        this.event = event;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.performersNames = performersNames == null ? new ArrayList<>() : new ArrayList<>(performersNames);
        this.venueAddress = venueAddress.trim();
        this.venueCapacity = venueCapacity;
        this.venueIsOutdoor = venueIsOutdoor;
        this.venueIsAllowsSmoking = venueIsAllowsSmoking;
        this.numTicketsTotal = numTicketsTotal;
        this.numTicketsSold = 0;
        this.ticketPrice = ticketPrice;
        this.isSponsored = false;
        this.sponsoredAmount = 0.0;
        this.reviewsRatings = new ArrayList<>();
        this.reviewsComments = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.performanceStatus = PerformanceStatus.ACTIVE;
    }

    /**
     * Cancels the performance.
     */
    public void cancel() {
        this.performanceStatus = PerformanceStatus.CANCELLED;
    }

    /**
     * Checks whether the event is ticketed.
     *
     * @return true if ticketed
     */
    public boolean checkIfEventIsTicketed() {
        return event != null && event.isTicketed();
    }

    /**
     * Checks if enough tickets remain.
     *
     * @param numTicketsToBuy requested tickets
     * @return true if enough remain
     */
    public boolean checkIfTicketsLeft(int numTicketsToBuy) {
        return (numTicketsTotal - numTicketsSold) >= numTicketsToBuy;
    }

    /**
     * Final ticket price after sponsorship.
     *
     * @return final price
     */
    public double getFinalTicketPrice() {
        double finalPrice = ticketPrice - sponsoredAmount;
        return Math.max(0.0, finalPrice);
    }

    /**
     * Returns organiser email.
     *
     * @return organiser email
     */
    public String getOrganiserEmail() {
        return event.getOrganiserEmail();
    }

    /**
     * Returns event id.
     *
     * @return event id
     */
    public long getEventId() {
        return event.getEventID();
    }

    /**
     * Returns event title.
     *
     * @return event title
     */
    public String getEventTitle() {
        return event.getEventTitle();
    }

    /**
     * Checks whether performance has not happened yet.
     *
     * @return true if current time is before start
     */
    public boolean checkHasNotHappenedYet() {
        return startDateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if this performance belongs to an EP email.
     *
     * @param epEmail provider email
     * @return true if same provider
     */
    public boolean checkCreatedByEP(String epEmail) {
        return epEmail != null
                && event != null
                && event.getOrganiserEmail() != null
                && event.getOrganiserEmail().equalsIgnoreCase(epEmail);
    }

    /**
     * Checks if there are active bookings.
     *
     * @return true if active booking exists
     */
    public boolean hasActiveBooking() {
        for (Booking b : bookings) {
            if (b.getBookingStatus() == BookingStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds sponsorship.
     *
     * @param amount sponsorship amount
     */
    public void sponsor(double amount) {
        if (!checkIfEventIsTicketed()) {
            throw new IllegalArgumentException("Sponsorship cannot be applied to non-ticketed performances.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Sponsorship must be positive.");
        }

        this.isSponsored = true;
        this.sponsoredAmount += amount;
    }

    /**
     * Adds a review.
     *
     * @param rating rating from 1 to 5
     * @param comment comment text
     */
    public void review(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        reviewsRatings.add(rating);
        reviewsComments.add(comment == null ? "" : comment);
    }

    /**
     * Adds a booking to this performance.
     *
     * @param b booking
     */
    public void addBooking(Booking b) {
        if (b == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        if (!checkIfEventIsTicketed()) {
            throw new IllegalStateException("Cannot book a non-ticketed performance.");
        }
        if (!checkIfTicketsLeft(b.getNumTickets())) {
            throw new IllegalStateException("Not enough tickets left.");
        }

        bookings.add(b);
        numTicketsSold += b.getNumTickets();
    }

    /**
     * Removes a booking from this performance.
     *
     * @param b booking
     */
    public void removeBooking(Booking b) {
        if (b != null && bookings.remove(b)) {
            numTicketsSold -= b.getNumTickets();
            if (numTicketsSold < 0) {
                numTicketsSold = 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder details = new StringBuilder();

        details.append("---Performance Details---\n");
        details.append("ID: ").append(performanceId).append("\n");
        details.append("Event: ").append(getEventTitle()).append("\n");
        details.append("Start: ").append(startDateTime).append("\n");
        details.append("End: ").append(endDateTime).append("\n");

        details.append("Performers: ");
        if (performersNames.isEmpty()) {
            details.append("To be added\n");
        } else {
            details.append(String.join(", ", performersNames)).append("\n");
        }

        details.append("Venue: ").append(venueAddress).append("\n");
        details.append("Capacity: ").append(venueCapacity).append("\n");
        details.append("Venue type: ").append(venueIsOutdoor ? "Outdoor" : "Indoor").append("\n");
        details.append("Smoking allowed: ").append(venueIsAllowsSmoking ? "Yes" : "No").append("\n");
        details.append("Tickets left: ").append(getTicketsLeft()).append("\n");
        details.append("Price: £").append(getFinalTicketPrice()).append("\n");
        details.append("Status: ").append(performanceStatus).append("\n");

        if (isSponsored) {
            details.append("Sponsored by: £").append(sponsoredAmount).append("\n");
        }

        if (reviewsRatings.isEmpty()) {
            details.append("No ratings yet.\n");
        } else {
            details.append("Average rating: ").append(getAverageRating()).append("\n");
            details.append("Comments:\n");
            for (String r : reviewsComments) {
                details.append(" - ").append(r).append("\n");
            }
        }

        return details.toString();
    }

    public long getPerformanceId() {
        return performanceId;
    }

    public int getTicketsLeft() {
        return numTicketsTotal - numTicketsSold;
    }

    public boolean isSponsored() {
        return isSponsored;
    }

    public PerformanceStatus getStatus() {
        return performanceStatus;
    }

    public double getSponsoredAmount() {
        return sponsoredAmount;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setSponsored(boolean sponsored) {
        this.isSponsored = sponsored;
    }

    public void setSponsoredAmount(double sponsoredAmount) {
        this.sponsoredAmount = sponsoredAmount;
    }

    /**
     * Returns average rating for this performance.
     *
     * @return average rating
     */
    public double getAverageRating() {
        if (reviewsRatings.isEmpty()) {
            return 0.0;
        }

        int total = 0;
        for (int rating : reviewsRatings) {
            total += rating;
        }
        return (double) total / reviewsRatings.size();
    }

    public Collection<Integer> getReviewsRatings() {
        return new ArrayList<>(reviewsRatings);
    }

    public Collection<String> getReviewsComments() {
        return new ArrayList<>(reviewsComments);
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public Event getEvent() {
        return event;
    }

    public Collection<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    public String getVenueAddress() {
        return venueAddress;
    }
}