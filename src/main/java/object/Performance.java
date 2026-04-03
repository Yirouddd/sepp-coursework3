package object;

import enums.PerdformanceStatus;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Performance.java
 */

public class Performance {
    private long performanceId;
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
    PerdformanceStatus status;

    public Performance(long performanceId, LocalDateTime startDateTime, LocalDateTime endDateTime, Collection<String> performersNames, String venueAddress, int duration) {
        this.performanceId = performanceId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.performersNames = performersNames;
        this.venueAddress = venueAddress;
        this.venueCapacity = venueCapacity;
        this.venueIsOutdoor = venueIsOutdoor;
        this.venueIsAllowsSmoking = venueIsAllowsSmoking;
        this.numTicketsTotal = numTicketsTotal;
        this.numTicketsSold = numTicketsSold;
        this.ticketPrice = ticketPrice;
        this.isSponsored = isSponsored;
        this.sponsoredAmount = sponsoredAmount;
        this.reviewsRatings = reviewsRatings;
        this.reviewsComments = reviewsComments;
        this.status = status;
    }

    public void cancel() {
        // Implementation for canceling the performance
    }

    public boolean checkIfEventIsTicked() {
        // Implementation for checking if the event is ticketed
        return false; // Placeholder return value
    }
    
    public boolean checkIfTicketsLeft(int numTicketsToBuy) {
        // Implementation for checking if there are tickets left
        return false; // Placeholder return value
    }

    public double getFinalTicketPrice() {
        // Implementation for calculating the final ticket price
        return 0.0; // Placeholder return value
    }

    public String getOrganiserEmail() {
        // Implementation for getting the organizer's email
        return ""; // Placeholder return value
    }

    public String getEventTitle() {
        // Implementation for getting the event title
        return ""; // Placeholder return value
    }

    public boolean checkHasNotHappenedYet() {
        // Implementation for checking if the performance has not happened yet
        return false; // Placeholder return value
    }

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

    public void sponsor(double amount) {
        // Implementation for sponsoring the performance
    }

    public void review(int rating, String comment) {
        // Implementation for adding a review to the performance
    }

    public void addBooking(Booking b) {
        // Implementation for adding a booking to the performance
    }

    public String toString() {
        // Implementation for converting the performance details to a string representation
        return ""; // Placeholder return value
    }

    public long getID() {
        return performanceId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
}