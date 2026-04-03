package object;

import enums.BookingStatus;
import enums.PerformanceStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Performance class represents a performance of an event.
 * Contains all relevant details including id, time, performers, venue
 * tickets quantity and price, sponsorship and reviews
 */

public class Performance {
    private long performanceId;
    private long eventId;
    private String eventTitle;
    private String organiserEmail;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Collection<String> performersNames;
    private String venueAddress;
    private int venueCapacity;
    private boolean venueIsOutdoor;
    private boolean venueIsAllowsSmoking;
    private int numTicketsTotal;
    private int numTicketsSold;
    private double ticketPrice;
    private boolean isSponsored;
    private double sponsoredAmount;
    private Collection<Integer> reviewsRatings;
    private Collection<String> reviewsComments;
    private Collection<Booking> bookings;
    PerformanceStatus performanceStatus;
    private Event event;

    public Performance(long performanceId, long eventId,
                       String eventTitle,LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       Collection<String> performersNames,
                       String venueAddress, int venueCapacity,
                       boolean venueIsOutdoor, boolean venueIsAllowsSmoking,
                       int numTicketsTotal, double ticketPrice) {
        // checks
        assert performanceId > 0: "Performance ID must be positive";
        assert eventId > 0: "Event ID must be positive";
        assert eventTitle != null && !eventTitle.isEmpty(): "Event title " +
                "cannot be null or empty";
        assert (startDateTime != null && endDateTime != null): "Start or end" +
                " time cannot be null";
        assert !endDateTime.isBefore(startDateTime): "End date time must be " +
                "after the start time";
        assert venueCapacity > 0: "Capacity must be positive";
        assert numTicketsTotal >= 0: "tickets cannot be negative";
        assert ticketPrice >= 0: "Price cannot be negative";

        this.performanceId = performanceId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.performersNames = performersNames;
        this.venueAddress = venueAddress;
        this.venueCapacity = venueCapacity;
        this.venueIsOutdoor = venueIsOutdoor;
        this.venueIsAllowsSmoking = venueIsAllowsSmoking;
        this.numTicketsTotal = numTicketsTotal;
        this.numTicketsSold = 0;
        this.ticketPrice = ticketPrice;
        this.isSponsored = false;
        this.sponsoredAmount = 0;
        this.reviewsRatings = new ArrayList<>();
        this.reviewsComments = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.performanceStatus = PerformanceStatus.ACTIVE;
    }

    /**
     * Cancels performance
     */
    public void cancel() {
        this.performanceStatus = PerformanceStatus.CANCELLED;    }

    /**
     * Checks if event is ticketed
     * @return true if event is ticketed
     */
    public boolean checkIfEventIsTicketed() {
        // Implementation for checking if the event is ticketed
        return numTicketsTotal > 0;
    }

    /**
     * Checks if tickets are still left for purchase
     * @param numTicketsToBuy number of tickets user wants to buy
     * @return true if enough tickets remain
     */
    public boolean checkIfTicketsLeft(int numTicketsToBuy) {
        // Implementation for checking if there are tickets left
        return (numTicketsTotal - numTicketsSold) >= numTicketsToBuy;
    }

    /**
     * Returns the final ticket price
     * Sponsorship is applied if necessary
     * @return ticket price (after reductions if any)
     */
    public double getFinalTicketPrice() {
        // Implementation for calculating the final ticket price
        if (isSponsored) {
            return ticketPrice - sponsoredAmount;
        }
        return ticketPrice;
    }

    /**
     * Gets email of the EP (organiser)
     * @return email of the organiser
     */
    public String getOrganiserEmail() {
        // Implementation for getting the organizer's email
        return organiserEmail;
    }

    /**
     * Gets event ID of the corresponding event to the performance
     * @return event ID
     */
    public long getEventId() {
        return eventId;
    }
    /**
     * Gets event title of the corresponding event to the performance
     * @return event title
     */
    public String getEventTitle() {
        // Implementation for getting the event title
        return eventTitle;
    }

    /**
     * Checks if the performance has not started yet
     * @return true if current time is before startDateTime
     */
    public boolean checkHasNotHappenedYet() {
        return startDateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the performance was created by the provided EP
     * @param epEmail email of the corresponding entertainment provider
     * @return true if the performance was created by a specific EP
     */
    public boolean checkCreatedByEP(String epEmail) {
        // Implementation for checking if the performance was created by a specific entertainment provider
        return false; // Placeholder return value
    }

    /**
     * Checks if there are active bookings
     * @return true if there are active bookings
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
     * Returns booking details for refund processing
     */
    public String getBookingDetailsForRefund() {
        // Implementation for getting booking details for refund processing
        return ""; // Placeholder return value

    }

    /**
     * Adds a sponsorship to the performance
     * @param amount sponsorship amount
     */
    public void sponsor(double amount) {
        // Implementation for sponsoring the performance
        if (!checkIfEventIsTicketed()) {
            throw new IllegalArgumentException("Sponsorship cannot be applied" +
                    " to non-ticketed performances");
        }
        if (amount <= 0){
            throw new IllegalArgumentException("Sponsorship must be positive");
        }
        this.isSponsored = true;
        this.sponsoredAmount += amount;
    }

    /**
     * Adds a review to the performance
     * @param rating rating score
     * @param comment review comment
     */
    public void review(int rating, String comment) {
        // Implementation for adding a review to the performance
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and " +
                    "5");
        }
        reviewsRatings.add(rating);
        reviewsComments.add(comment != null ? comment : "");
    }

    /**
     * Adds a booking to the performance
     * @param b booking
     */
    public void addBooking(Booking b) {
        // Implementation for adding a booking to the performance
        /*
        if (b == null) {
            return;
        }
        int ticketsToBook = b.getNumTickets; // method in Booking
        if (ticketsToBook <= 0) {
            throw new IllegalArgumentException("Booking must have at least one ticket");
        }
        if ((numTicketsSold + ticketsToBook) > numTicketsTotal) {
            throw new IllegalStateException("Booking exceeds available " +
                    "tickets. Not enough tickets");
        }

        numTicketsSold += ticketsToBook;
        bookings.add(b);
        */
    }

    /**
     * Converts the performance details to a string representation
     * @return detailed performance info
     */
    public String toString() {
        StringBuilder details = new StringBuilder();

        details.append("---Performance Details---\n");
        details.append("ID: ").append(performanceId).append("\n");
        details.append("Start: ").append(startDateTime).append("\n");
        details.append("End: ").append(endDateTime).append("\n");

        details.append("Performers: ");
        if (performersNames.isEmpty()) {
            details.append("To be added\n");
        }
        else {
            details.append(String.join(", ", performersNames)).append("\n");
        }

        details.append("Venue: ").append(venueAddress).append("\n");
        details.append("Capacity: ").append(venueCapacity).append("\n");
        if (venueIsOutdoor) {
            details.append("Outdoor");
        }
        else {
            details.append("Indoor");
        }
        if (venueIsAllowsSmoking) {
            details.append("Smoking is allowed");
        }
        else {
            details.append("Smoking is NOT allowed");
        }

        details.append("Tickets left: ").append(getTicketsLeft()).append("\n");
        details.append("Price: £").append(getFinalTicketPrice()).append("\n");
        details.append("Status: ").append(performanceStatus).append("\n");

        if (isSponsored) {
            details.append("Sponsored: £").append(sponsoredAmount).append("\n");
        }

        if (reviewsRatings.isEmpty()) {
            details.append("No ratings yet.\n");
        }
        else {
            details.append("Average rating: ").append(getAverageRating()).append(
                    "\n");
            details.append("Comments:\n");
            for (String r : reviewsComments) {
                details.append(" - ").append(r).append("\n");
            }
        }

        return details.toString();
    }

    // getters
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
        return status;
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
     * Returns average rating for a performance
     * @return average rating for the performance
     */
    public double getAverageRating() {
        if (reviewsRatings.isEmpty()) {
            return 0.0;
        }
        int total = 0;
        for (int r : reviewsRatings) {
            total += r;
        }
        return (double) total / reviewsRatings.size();
    }

    /**
     * Returns all ratings for the performance
     * @return collection of ratings
     */
    public Collection<Integer> getReviewsRatings() {
        return new ArrayList<>(reviewsRatings);
    }

    /**
     * Returns all comments for the performance
     * @return collection of comments
     */
    public Collection<String> getReviewsComments() {
        return new ArrayList<>(reviewsComments);
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public Event getEvent() {
        return event;
    }

    public Collection<Booking> getBookings () {
        return bookings;
    }
}