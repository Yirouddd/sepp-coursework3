package object;

import enums.BookingStatus;
import user.Student;

import java.time.LocalDateTime;
import object.Performance;
/**
 * Booking class represents a student with a bookingNumber, numTickets, amountPaid, bookingDateTime and status;
 */
public class Booking {
    private long bookingNumber;
    private int numTickets;
    private double amountPaid;
    private LocalDateTime bookingDateTime;
    BookingStatus status;
    private Performance performance;

    /**
     * Constructs a new Booking with the specified details.
     *
     * @param bookingNumber the bookingNumber of the booking
     * @param numTickets the number of tickets of the booking
     * @param amountPaid the amount of money the user has paid
     * @param bookingDateTime the date and time the booking was made
     * @param status the status of the booking
     */

    private Student student;
    //^added (refer solution cw1) corresponds to a booking that does not belong to the student

    public Booking(long bookingNumber, int numTickets, double amountPaid,
                   LocalDateTime bookingDateTime, BookingStatus status, Student student, Performance performance) {
        this.bookingNumber = bookingNumber;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.student = student;
        this.performance = performance;
    }

    //added this in correspondence to line 29 being added.
    public Student getStudent() {
        return student;
    }

    public Performance getPerformance() {
        return performance;
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

    public long getBookingNumber() {
        return bookingNumber;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus newStatus) {
        this.status = newStatus;
    }

    public int getNumTickets() {
        return numTickets;
    }

    public double getAmountPaid() {
        return amountPaid;
    }


}