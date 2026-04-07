package unitTest;

import enums.BookingStatus;
import enums.EventType;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.EntertainmentProvider;
import user.Student;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

    private Student student;
    private Performance performance;
    private Booking booking;

    @BeforeEach
    void setUp() {
        student = new Student("student1@test.com", "pass1", "name1", 123456);
        performance = createPerformance(true);
        booking = new Booking(student, performance, 99L, 2, 20.0, LocalDateTime.of(2026, 4, 7, 12, 0));
    }

    private Performance createPerformance(boolean ticketed) {
        EntertainmentProvider provider = new EntertainmentProvider(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );
        Event event = new Event(1L, "event1", EventType.Music, ticketed);
        event.setOrganizer(provider);
        return event.createPerformance(
                1L,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(5).plusHours(2),
                List.of("performer1"),
                "venue1",
                100,
                false,
                false,
                50,
                10.0
        );
    }

    // New bookings should start as ACTIVE.
    @Test
    void constructor_setsStatusToActive() {
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus(),
                "A newly created booking should start with ACTIVE status.");
    }

    // Student cancellation should change the status.
    @Test
    void cancelByStudent_setsStatusToCancelledByStudent() {
        booking.cancelByStudent();

        assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus(),
                "cancelByStudent should set the status to CANCELLEDBYSTUDENT.");
    }

    // Payment failure cancellation should change the status.
    @Test
    void cancelPaymentFailed_setsStatusToPaymentFailed() {
        booking.cancelPaymentFailed();

        assertEquals(BookingStatus.PAYMENTFAILED, booking.getBookingStatus(),
                "cancelPaymentFailed should set the status to PAYMENTFAILED.");
    }

    // Provider cancellation should change the status.
    @Test
    void cancelByProvider_setsStatusToCancelledByProvider() {
        booking.cancelByProvider();

        assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking.getBookingStatus(),
                "cancelByProvider should set the status to CANCELLEDBYPROVIDER.");
    }

    // Email check should ignore case for the same student.
    @Test
    void checkBookedByStudent_returnsTrue_whenEmailMatchesIgnoringCase() {
        assertTrue(booking.checkBookedByStudent("STUDENT1@test.com"),
                "Booking ownership check should ignore email case.");
    }

    // Different email should return false.
    @Test
    void checkBookedByStudent_returnsFalse_whenEmailDoesNotMatch() {
        assertFalse(booking.checkBookedByStudent("student2@test.com"),
                "Booking ownership check should return false for a different email.");
    }

    // Null student should return false instead of throwing from the ownership check.
    @Test
    void checkBookedByStudent_returnsFalse_whenStudentIsNull() {
        Booking bookingWithNullStudent = new Booking(null, performance, 100L, 1, 10.0, LocalDateTime.now());

        assertFalse(bookingWithNullStudent.checkBookedByStudent("student1@test.com"),
                "Booking ownership check should return false when the booking has no student.");
    }

    // Student details string should contain name, email and phone in the expected format.
    @Test
    void getStudentDetails_returnsFormattedStudentDetails() {
        assertEquals("name1 | student1@test.com | 123456", booking.getStudentDetails(),
                "getStudentDetails should return the expected student details format.");
    }

    // Booking record should include booking number, performance id, event title, amount, student details and status.
    @Test
    void generateBookingRecord_returnsExpectedRecordText() {
        assertEquals(
                "Booking #99 Performance ID: 1 Event: event1 Tickets: 2 Amount paid: £20.0 Student: name1 | student1@test.com | 123456 Status: ACTIVE",
                booking.generateBookingRecord(),
                "generateBookingRecord should include the expected booking information."
        );
    }

    // Booking number getter should return the value passed to the constructor.
    @Test
    void getBookingNumber_returnsConstructorBookingNumber() {
        assertEquals(99L, booking.getBookingNumber(),
                "getBookingNumber should return the booking number set in the constructor.");
    }

    // Ticket count getter should return the value passed to the constructor.
    @Test
    void getNumTickets_returnsConstructorTicketCount() {
        assertEquals(2, booking.getNumTickets(),
                "getNumTickets should return the number of tickets set in the constructor.");
    }

    // Student email getter should return the student's email.
    @Test
    void getStudentEmail_returnsStudentEmail() {
        assertEquals("student1@test.com", booking.getStudentEmail(),
                "getStudentEmail should return the booked student's email.");
    }

    // Transaction amount getter should return the paid amount.
    @Test
    void getTransactionAmount_returnsAmountPaid() {
        assertEquals(20.0, booking.getTransactionAmount(),
                "getTransactionAmount should return the amount paid for the booking.");
    }

    // Student phone number getter should return the student's phone number.
    @Test
    void getStudentPhoneNumber_returnsStudentPhoneNumber() {
        assertEquals(123456, booking.getStudentPhoneNumber(),
                "getStudentPhoneNumber should return the booked student's phone number.");
    }

    // getStudent should return the same student object.
    @Test
    void getStudent_returnsBookedStudent() {
        assertSame(student, booking.getStudent(),
                "getStudent should return the same student object used to create the booking.");
    }

    // getPerformance should return the same performance object.
    @Test
    void getPerformance_returnsBookedPerformance() {
        assertSame(performance, booking.getPerformance(),
                "getPerformance should return the same performance object used to create the booking.");
    }

    // getStudentPhone should return the same phone number as the student.
    @Test
    void getStudentPhone_returnsStudentPhoneNumber() {
        assertEquals(123456, booking.getStudentPhone(),
                "getStudentPhone should return the booked student's phone number.");
    }
}
