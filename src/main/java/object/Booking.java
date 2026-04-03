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
<<<<<<< feature/cancel-booking
    BookingStatus status;
    private Performance performance;
=======
    private BookingStatus status;
    private Student student;
    private BookingStatus bookingStatus;

>>>>>>> Main

    /**
     * Constructs a new Booking with the specified details.
     *
     * @param bookingNumber the bookingNumber of the booking
     * @param numTickets the number of tickets of the booking
     * @param amountPaid the amount of money the user has paid
     * @param bookingDateTime the date and time the booking was made
     * @param status the status of the booking
     */
<<<<<<< feature/cancel-booking

    private Student student;
    //^added (refer solution cw1) corresponds to a booking that does not belong to the student

    public Booking(long bookingNumber, int numTickets, double amountPaid,
                   LocalDateTime bookingDateTime, BookingStatus status, Student student, Performance performance) {
=======
    public Booking(Student student, long bookingNumber, int numTickets, double amountPaid, LocalDateTime bookingDateTime, BookingStatus status) {
>>>>>>> Main
        this.bookingNumber = bookingNumber;
        this.numTickets = numTickets;
        this.amountPaid = amountPaid;
        this.bookingDateTime = bookingDateTime;
        this.status = status;
        this.student = student;
<<<<<<< feature/cancel-booking
        this.performance = performance;
    }

    //added this in correspondence to line 29 being added.
    public Student getStudent() {
        return student;
    }

    public Performance getPerformance() {
        return performance;
=======
        bookingStatus = BookingStatus.ACTIVE;
>>>>>>> Main
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

<<<<<<< feature/cancel-booking
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

=======

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

    public BookingStatus getBookingStatus () {
        return bookingStatus;
    }

>>>>>>> Main

}