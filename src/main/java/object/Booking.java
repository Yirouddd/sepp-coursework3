package object;

import enums.BookingStatus;

import java.time.LocalDateTime;

/**
 * Booking class represents a student with a bookingNumber, numTickets, amountPaid, bookingDateTime and status;
 */
public class Booking {
    private long bookingNumber;
    private int numTickets;
    private double amountPaid;
    private LocalDateTime bookingDateTime;
    BookingStatus status;


    /**
     * Constructs a new Booking with the specified details.
     *
     * @param bookingNumber the bookingNumber of the booking
     * @param numTickets the number of tickets of the booking
     * @param amountPaid the amount of money the user has paid
     * @param bookingDateTime the date and time the booking was made
     * @param status the status of the booking
     */
    public Booking(long bookingNumber, int numTickets, double amountPaid, LocalDateTime bookingDateTime, BookingStatus status) {
        this.bookingNumber = bookingNumber;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
    }

    public void cancelByStudent() {
        // Implementation for student cancelling a booking
    }

    public void cancelPaymentFailed() {
        // Implementation for booking being cancelled due to failed payment
    }

    public void cancelByProvider() {
        // Implementation for EP cancelling a booking
    }

    public boolean checkBookedByStudent(String email) {
        // Implementation for checking if booked by student
        return false; // Placeholder return value
    }

    public String getStudentDetails() {
        // Implementation for checking if booked by student
        return null; // Placeholder return value
    }

    public String generateBookingRecord() {
        // Implementation for checking if booked by student
        return null; // Placeholder return value
    }

}