package object;

import enums.BookingStatus;
import user.Student;

import java.time.LocalDateTime;

/**
 * Represents a booking made by a student for a performance.
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
     * @param student         the student making the booking
     * @param performance     the performance being booked
     * @param bookingNumber   unique booking ID
     * @param numTickets      number of tickets booked
     * @param amountPaid      total amount paid
     * @param bookingDateTime date and time of booking
     */
    public Booking(Student student, Performance performance, long bookingNumber, int numTickets, double amountPaid, LocalDateTime bookingDateTime) {
        this.student = student;
        this.performance = performance;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingNumber = bookingNumber;
        this.bookingDateTime = bookingDateTime;
        status = BookingStatus.ACTIVE;
    }

    /**
     * Marks the booking as canceled by the student.
     */
    public void cancelByStudent() {
        status = BookingStatus.CANCELLEDBYSTUDENT;
    }

    /**
     * Marks the booking as failed due to payment failure.
     */
    public void cancelPaymentFailed() {
        status = BookingStatus.PAYMENTFAILED;
    }

    /**
     * Marks the booking as cancelled by the provider.
     */
    public void cancelByProvider() {
        status = BookingStatus.CANCELLEDBYPROVIDER;
    }


    /**
     * Checks whether the booking belongs to a student with given student email.
     *
     * @param email student email
     * @return true if the booking belongs to the student
     */
    public boolean checkBookedByStudent(String email) {
        return student != null && student.getEmail().equalsIgnoreCase(email);
    }

    /**
     * Returns a string containing student details.
     *
     * @return student details as "Name | Email | PhoneNumber"
     */
    public String getStudentDetails() {
        return student.getName() + " | " + student.getEmail() + " | " + student.getPhoneNumber();
    }

    /**
     * Generates a full booking record string with performance ID, event,
     * number of tickets purchased, amount paid, student and booking status.
     *
     * @return booking record string
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
     * Returns the unique booking number.
     *
     * @return booking number
     */
    public long getBookingNumber() {
        return bookingNumber;
    }

    /**
     * Returns the number of tickets in this booking.
     *
     * @return the number of booked tickets
     */
    public int getNumTickets() {
        return numTickets;
    }

    /**
     * Returns the email address of the student who made the booking.
     *
     * @return the student's email address
     */
    public String getStudentEmail() {
        return student.getEmail();
    }

    /**
     * Returns the total amount paid for this booking.
     *
     * @return amount paid
     */
    public double getTransactionAmount() {
        return amountPaid;
    }

    /**
     * Returns the current status of the booking.
     *
     * @return booking status
     */
    public BookingStatus getBookingStatus() {
        return status;
    }

    /**
     * Returns the date and time when the booking was made.
     *
     * @return booking date and time
     */
    public LocalDateTime getBookingDateTime() {
        return bookingDateTime;
    }

    /**
     * Returns the phone number of the student who made the booking.
     *
     * @return phone number of student
     */
    public int getStudentPhoneNumber() {
        return student.getPhoneNumber();
    }

    /**
     * Returns the student who made this booking.
     *
     * @return student
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Returns the performance associated with this booking.
     *
     * @return performance
     */
    public Performance getPerformance() {
        return performance;
    }

    /**
     * Returns the phone number of the student who made the booking.
     *
     * @return phone number of student
     */
    public int getStudentPhone() {
        return student.getPhoneNumber();
    }
}