package systemTest;

import enums.BookingStatus;
import enums.EventType;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.Student;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class BookingTest {

    private Booking booking;
    private Student student;
    private Performance performance;

    @BeforeEach
    void setup() {
        student = new Student("olivia@mail.com", "123", "Olivia", 123);

        Event event = new Event(
                1,
                "Test Event",
                EventType.Music,
                true
        );

        performance = new Performance(
                1,
                event,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new ArrayList<>(),
                "Address",
                100,
                false,
                false,
                50,
                10.0
        );

        booking = new Booking(
                student,
                performance,
                1,
                2,
                20.0,
                LocalDateTime.now()
        );
    }

    @Test
    void getBookingNumberCorrectValue() {
        assertEquals(1, booking.getBookingNumber());
    }

    @Test
    void getNumTicketsCorrectValue() {
        assertEquals(2, booking.getNumTickets());
    }

    @Test
    void getTransactionAmountCorrectValue() {
        assertEquals(20.0, booking.getTransactionAmount());
    }

    @Test
    void getBookingStatusActiveInitially() {
        assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus());
    }

    @Test
    void getStudentEmailCorrectEmail() {
        assertEquals("olivia@mail.com", booking.getStudentEmail());
    }

    @Test
    void getStudentPhoneNumberCorrectNumber() {
        assertEquals(123, booking.getStudentPhoneNumber());
    }

    @Test
    void getStudentCorrectStudent() {
        assertEquals(student, booking.getStudent());
    }

    @Test
    void getPerformanceCorrectPerformance() {
        assertEquals(performance, booking.getPerformance());
    }

    @Test
    void cancelByStudentUpdateStatus() {
        booking.cancelByStudent();
        assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus());
    }

    @Test
    void cancelPaymentFailedUpdateStatus() {
        booking.cancelPaymentFailed();
        assertEquals(BookingStatus.PAYMENTFAILED, booking.getBookingStatus());
    }

    @Test
    void cancelByProviderUpdateStatus() {
        booking.cancelByProvider();
        assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking.getBookingStatus());
    }

    @Test
    void checkBookedByStudentCorrectEmail() {
        assertTrue(booking.checkBookedByStudent("olivia@mail.com"));
    }

    @Test
    void checkBookedByStudentWrongEmail() {
        assertFalse(booking.checkBookedByStudent("wrongmeail@mail.com"));
    }

    @Test
    void generateBookingRecordContainsBookingNumber() {
        assertTrue(booking.generateBookingRecord().contains("Booking #1"));
    }
}