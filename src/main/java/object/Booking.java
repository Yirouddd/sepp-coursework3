package object;

import enums.BookingStatus;
import user.Student;

import java.time.LocalDateTime;

/**
 * Booking class represents a student with a bookingNumber, numTickets, amountPaid, bookingDateTime and status;
 */
public class Booking {
    private long bookingNumber;
    private int numTickets;
    private double amountPaid;
    private BookingStatus status;
    private LocalDateTime bookingDateTime;
    private Student student;
    private Performance performance;


    /**
     * Constructs a booking.
     *
     * @param student student who made the booking
     * @param performance performance booked
     * @param numTickets number of tickets
     * @param amountPaid total amount paid
     */
    public Booking(Student student, Performance performance, long bookingNumber, int numTickets, double amountPaid, LocalDateTime bookingDateTime) {
        this.student = student;
        this.performance = performance;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingNumber = bookingNumber;
        status = BookingStatus.ACTIVE;
        this.bookingDateTime = bookingDateTime;
    }

    /**
     * Marks booking as canceled by student.
     */
    public void cancelByStudent() {
        status = BookingStatus.CANCELLEDBYSTUDENT;
    }

    /**
     * Marks booking as payment failed.
     */
    public void cancelPaymentFailed() {
        status = BookingStatus.PAYMENTFAILED;
    }

    /**
     * Marks booking as cancelled by provider.
     */
    public void cancelByProvider() {
        status = BookingStatus.CANCELLEDBYPROVIDER;
    }


    /**
     * Checks whether the booking belongs to the given student email.
     *
     * @param email student email
     * @return true if booking belongs to the student
     */
    public boolean checkBookedByStudent(String email) {
        return student != null && student.getEmail().equalsIgnoreCase(email);
    }

    /**
     * Returns a string containing student details.
     *
     * @return student details string
     */
    public String getStudentDetails() {
        return student.getName() + " | " + student.getEmail() + " | " + student.getPhoneNumber();
    }

    /**
     * Generates a booking record string.
     *
     * @return booking record
     */
    public String generateBookingRecord() {
        return "Booking #" + bookingNumber
                + " Performance ID: " + performance.getPerformanceId()
                + " Event: " + performance.getEventTitle()
                + " Tickets: " + numTickets
                + " Amount paid: £" + amountPaid
                + " Student: " + getStudentDetails()
                + " Status: " + status;
    }

    /**
     * Gets the unique booking number.
     *
     * @return the booking number
     */
    public long getBookingNumber() {
        return bookingNumber;
    }

    /**
     * Gets the number of tickets in this booking.
     *
     * @return the number of booked tickets
     */
    public int getNumTickets() {
        return numTickets;
    }

    /**
     * Gets the email address of the student who made the booking.
     *
     * @return the student's email address
     */
    public String getStudentEmail() {
        return student.getEmail();
    }

    /**
     * Gets the total transaction amount paid for this booking.
     *
     * @return the amount paid
     */
    public double getTransactionAmount() {
        return amountPaid;
    }

    /**
     * Gets the current status of the booking.
     *
     * @return the booking status
     */
    public BookingStatus getBookingStatus() {
        return status;
    }

    /**
     * Gets the date and time when the booking was made.
     *
     * @return the booking date and time
     */
    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    /**
     * Gets the Student's phone number
     *
     * @return Phone number of student
     */
    public int getStudentPhoneNumber() {
        return student.getPhoneNumber();
    }

    /**
     * Gets the Student who made this booking
     *
     * @return student
     */
    public Student getStudent() {
        return student;
    }

    public Performance getPerformance() {
        return performance;
    }

    public int getStudentPhone() {
        return student.getPhoneNumber();
    }
}