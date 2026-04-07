package object;

import enums.BookingStatus;
import enums.PerformanceStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a performance of an event with date and time, venue, bookings
 * and reviews.
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
     * Constructs a performance with the specifies details.
     *
     * @param performanceId unique performance ID
     * @param event associated event
     * @param startDateTime start date time
     * @param endDateTime end date time
     * @param performersNames list of performers
     * @param venueAddress venue address
     * @param venueCapacity venue capacity
     * @param venueIsOutdoor true if venue is outdoor
     * @param venueIsAllowsSmoking true if venue allows smoking
     * @param numTicketsTotal total tickets available for the performance
     * @param ticketPrice price per ticket
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
     * @return true if the event is ticketed
     */
    public boolean checkIfEventIsTicketed() {
        return event != null && event.isTicketed();
    }

    /**
     * Checks if enough tickets remain to purchase.
     *
     * @param numTicketsToBuy number of requested tickets
     * @return true if enough sufficient tickets remain
     */
    public boolean checkIfTicketsLeft(int numTicketsToBuy) {
        return (numTicketsTotal - numTicketsSold) >= numTicketsToBuy;
    }

    /**
     * Returns the final ticket price after applying sponsorship.
     *
     * @return final ticket price
     */
    public double getFinalTicketPrice() {
        double finalPrice = ticketPrice - sponsoredAmount;
        return Math.max(0.0, finalPrice);
    }

    /**
     * Returns the organiser's email.
     *
     * @return organiser email
     */
    public String getOrganiserEmail() {
        return event.getOrganiserEmail();
    }

    /**
     * Returns the associated event ID.
     *
     * @return event ID
     */
    public long getEventId() {
        return event.getEventID();
    }

    /**
     * Returns the associated event title.
     *
     * @return event title
     */
    public String getEventTitle() {
        return event.getEventTitle();
    }

    /**
     * Checks whether the performance has not happened yet.
     *
     * @return true if the start time is in the future
     */
    public boolean checkHasNotHappenedYet() {
        return startDateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks whether this performance was created by the given EP.
     *
     * @param epEmail provider email
     * @return true if created by the provider
     */
    public boolean checkCreatedByEP(String epEmail) {
        return epEmail != null
                && event != null
                && event.getOrganiserEmail() != null
                && event.getOrganiserEmail().equalsIgnoreCase(epEmail);
    }

    /**
     * Checks if there are any active bookings.
     *
     * @return true if at least one active booking exists
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
     * Adds sponsorship to the performance.
     *
     * @param amount sponsorship amount
     * @throws IllegalArgumentException if event is non-ticketed or amount is
     * non-positive
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
     * Adds a review for the performance.
     *
     * @param rating rating from 1 to 5
     * @param comment review comment (can be null)
     * @throws IllegalArgumentException if rating is less than 1 or is bigger
     * than 5
     */
    public void review(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        reviewsRatings.add(rating);
        reviewsComments.add(comment == null ? "" : comment);
    }

    /**
     * Adds a booking to the performance.
     *
     * @param b booking to add
     * @throws IllegalArgumentException if booking is null
     * @throws IllegalStateException if performance is non-ticketed or not
     * enough tickets left
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
     * Removes a booking from the performance.
     *
     * @param b booking to remove
     */
    public void removeBooking(Booking b) {
        if (b != null && bookings.remove(b)) {
            numTicketsSold -= b.getNumTickets();
            if (numTicketsSold < 0) {
                numTicketsSold = 0;
            }
        }
    }

    /**
     * Returns a string representation of the performance details.
     *
     * @return performance details string
     */
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

    /**
     * Returns the performance ID.
     *
     * @return performance ID
     */
    public long getPerformanceId() {
        return performanceId;
    }

    /**
     * Returns the number of tickets left.
     *
     * @return tickets left
     */
    public int getTicketsLeft() {
        return numTicketsTotal - numTicketsSold;
    }

    /**
     * Checks whether the performance is sponsored.
     *
     * @return true if sponsored
     */
    public boolean isSponsored() {
        return isSponsored;
    }

    /**
     * Returns the performance status.
     *
     * @return status
     */
    public PerformanceStatus getStatus() {
        return performanceStatus;
    }

    /**
     * Returns the sponsored amount applied.
     *
     * @return sponsored amount
     */
    public double getSponsoredAmount() {
        return sponsoredAmount;
    }

    /**
     * Returns the performance start time.
     *
     * @return start time
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Returns the performance end time.
     *
     * @return end time
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    /**
     * Sets whether the performance is sponsored.
     *
     * @param sponsored  true if sponsored
     */
    public void setSponsored(boolean sponsored) {
        this.isSponsored = sponsored;
    }

    /**
     * Sets the sponsored amount.
     *
     * @param sponsoredAmount amount to set
     */
    public void setSponsoredAmount(double sponsoredAmount) {
        this.sponsoredAmount = sponsoredAmount;
    }

    /**
     * Returns average rating for the performance.
     *
     * @return average rating or 0.0 if no ratings exist
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

    /**
     * Returns the list of ratings for the performance.
     *
     * @return copy of ratings
     */
    public Collection<Integer> getReviewsRatings() {
        return new ArrayList<>(reviewsRatings);
    }

    /**
     * Returns the list of review comments for the performance.
     *
     * @return copy of comments
     */
    public Collection<String> getReviewsComments() {
        return new ArrayList<>(reviewsComments);
    }

    /**
     * Returns the ticket price.
     *
     * @return ticket price
     */
    public double getTicketPrice() {
        return ticketPrice;
    }

    /**
     * Returns the associated event.
     *
     * @return event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the bookings of the performance.
     *
     * @return copy of bookings
     */
    public Collection<Booking> getBookings() {
        return new ArrayList<>(bookings);
    }

    /**
     * Returns the venue address.
     *
     * @return venue address
     */
    public String getVenueAddress() {
        return venueAddress;
    }
}