package object;

import enums.BookingStatus;
import user.Student;
import object.Performance;
import java.time.LocalDateTime;

/**
 * Booking class represents a student with a bookingNumber, numTickets, amountPaid, bookingDateTime and status;
 */
public class Booking {
    private long bookingNumber;
    private int numTickets;
    private double amountPaid;
    private LocalDateTime bookingDateTime;
    private BookingStatus status;
    private Student student;
    private BookingStatus bookingStatus;
    private Performance performance;

    /**
     * Constructs a new Booking with the specified details.
     *
     * @param bookingNumber   the bookingNumber of the booking
     * @param numTickets      the number of tickets of the booking
     * @param amountPaid      the amount of money the user has paid
     * @param bookingDateTime the date and time the booking was made
     * @param status          the status of the booking
     */
    public Booking(Student student, long bookingNumber, int numTickets, double amountPaid, LocalDateTime bookingDateTime, BookingStatus status, Performance performance) {
        this.bookingNumber = bookingNumber;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.student = student;
        this.performance = performance;
        this.bookingStatus = BookingStatus.ACTIVE;
    }
    public void cancelByStudent() {
        bookingStatus = BookingStatus.CANCELLEDBYSTUDENT;
    }

    public void cancelPaymentFailed() {
        bookingStatus = BookingStatus.PAYMENTFAILED;
    }

    public void cancelByProvider() {
        bookingStatus = BookingStatus.CANCELLEDBYPROVIDER;
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


    // getter
    public int getNumTickets() {
        return numTickets;
    }

    public String getStudentEmail() {
        return student.getEmail();
    }

    public int getStudentPhone() {
        return student.getPhoneNumber();
    }

    public double getTransactionAmount() {
        return amountPaid;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public Student getStudent() {
        return student;
    }

    public BookingStatus getStatus() {
        return status; //not the same as bookingStatus
    }

    public void setStatus(BookingStatus newStatus) {
        this.status = newStatus;
    }

    public Performance getPerformance() {
        return performance;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public long getBookingNumber() {
        return bookingNumber;
    }
}
