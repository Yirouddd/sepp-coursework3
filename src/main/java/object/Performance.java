package object;

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
    private String eventTitle;
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
    private PerformanceStatus status;

    /**
     * Constructs performance object
     *
     * @param performanceId is a unique identified
     * @param eventTitle event title associated with this performance
     * @param startDateTime start date time
     * @param endDateTime end date time
     * @param performersNames collection of performer names
     * @param venueAddress venue address
     * @param venueCapacity capacity of the venue
     * @param numTicketsTotal total tickets available
     * @param ticketPrice ticket price
     */
    public Performance(long performanceId,
                       String eventTitle,LocalDateTime startDateTime,
                       LocalDateTime endDateTime,
                       Collection<String> performersNames,
                       String venueAddress, int venueCapacity,
                       int numTicketsTotal, double ticketPrice) {
        // checks
        assert performanceId > 0: "Performance ID must be positive";
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
        this.eventTitle = eventTitle;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.performersNames = performersNames;
        this.venueAddress = venueAddress;
        this.venueCapacity = venueCapacity;
        this.venueIsOutdoor = false; // default
        this.venueIsAllowsSmoking = false; // default
        this.numTicketsTotal = numTicketsTotal;
        this.numTicketsSold = 0;
        this.ticketPrice = ticketPrice;
        this.isSponsored = false;
        this.sponsoredAmount = 0;
        this.reviewsRatings = new ArrayList<>();
        this.reviewsComments = new ArrayList<>();
        this.status = PerformanceStatus.ACTIVE;
    }

    /**
     * Cancels performance
     */
    public void cancel() {
        this.status = PerformanceStatus.CANCELLED;
    }

    /**
     * Checks if event is ticketed
     * @return true if event is ticketed
     */
    public boolean checkIfEventIsTicketed() {
        return numTicketsTotal > 0;
    }

    /**
     * Checks if tickets are still left for purchase
     * @param numTicketsToBuy number of tickets user wants to buy
     * @return true if enough tickets remain
     */
    public boolean checkIfTicketsLeft(int numTicketsToBuy) {
        return (numTicketsTotal - numTicketsSold) >= numTicketsToBuy;
    }

    /**
     * Returns the final ticket price
     * Sponsorship is applied if necessary
     * @return ticket price (after reductions if any)
     */
    public double getFinalTicketPrice() {
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
        return ""; // Placeholder return value
    }

    /**
     * Gets event title of the corresponding event to the performance
     * @return event title
     */
    public String getEventTitle() {
        return eventTitle;
    }

    /**
     * Checks if the performance has not started yet
     * @return true if current time is before startDateTime
     */
    public boolean checkHasNotHappenedYet() {
        return LocalDateTime.now().isBefore(startDateTime);
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

    public boolean hasActiveBooking() {
        // Implementation for checking if there are active bookings for the performance
        return false; // Placeholder return value
    }

    public String getBookingDetailsForRefund() {
        // Implementation for getting booking details for refund processing
        return ""; // Placeholder return value
    }

    /**
     * Adds a sponsorship to the performance
     * @param amount sponsorship amount
     */
    public void sponsor(double amount) {
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
        if (rating < 0 || rating > 5) {
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
        /*if (b == null) {
            return;
        }
        numTicketsSold += b.getNumTickets(); // method in Booking
        if (numTicketsSold > numTicketsTotal) {
            throw new IllegalStateException("Booking exceeds available " +
                    "tickets. Not enough tickets");
        }*/
        return;
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
        details.append("Status: ").append(status).append("\n");

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

    public Collection<String> getPerformersNames() {
        return performersNames;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public int getVenueCapacity() {
        return venueCapacity;
    }

    public boolean isVenueOutdoor() {
        return venueIsOutdoor;
    }

    public boolean isVenueIsAllowsSmoking() {
        return venueIsAllowsSmoking;
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
}