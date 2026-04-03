package object;

import enums.PerdformanceStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Performance.java
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
    private double ticketPrice;
    private boolean isSponsored;
    private double sponsoredAmount;
    private Collection<Integer> reviewsRatings;
    private Collection<String> reviewsComments;
    private PerdformanceStatus status;

    private Collection<Booking> bookings;

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
                       int numTicketsSold,
                       double ticketPrice
                       ) {
        this.performanceId = performanceId;
        this.event = event;
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
        this.bookings = new ArrayList<>();
    }

    public void cancel() {
        this.status = PerdformanceStatus.CANCELLED;
    }

    public boolean checkIfEventIsTicked() {

        return false;
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
        return event.getTitle();
    }

    public boolean checkHasNotHappenedYet() {
        return startDateTime.isAfter(LocalDateTime.now());
    }

    public boolean checkCreatedByEP(String epEmail) {
        // Implementation for checking if the performance was created by a specific entertainment provider
        return false; // Placeholder return value
    }

    public boolean hasActiveBooking() {
        if (numTicketsSold > 0){
            return true;
        }
        return false;
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

    // helper functions
    public Event getEvent() {
        return event;
    }

    public PerdformanceStatus getStatus() {
        return status;
    }



    public int getNumTicketsSold() {
        return numTicketsSold;
    }

    public Collection<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Collection<Booking> bookings) {
        this.bookings = bookings;
    }
    
}