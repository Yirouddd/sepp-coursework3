package systemTest;

import controller.BookingController;
import object.Booking;
import object.Performance;
import enums.BookingStatus;
import user.Student;
import user.User;
import external.PaymentSystem;
import external.MockPaymentSystem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import systemTest.MockView;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CancelBookingSystemTest {

    @Test
    public void testCancelBookingSuccess() {
        // Setup
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();
        PaymentSystem paymentSystem = new MockPaymentSystem();

        // Create future performance (>24 hours away)
        Performance perf = new Performance(
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().plusHours(48),       // startDateTime
                LocalDateTime.now().plusHours(50),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        // Create booking
        Booking booking = new Booking(
                (Student) student,           // student FIRST
                1L,                          // bookingNumber
                2,                           // numTickets
                50.0,                        // amountPaid
                LocalDateTime.now(),         // bookingDateTime
                BookingStatus.ACTIVE,         // status
                perf
        );

        controller.addBooking(booking);

        // Simulate user inputs
        mockView.addInput("1");

        // Execute
        controller.cancelBooking();

        // Assert
        String output = mockView.getOutput();
        assertTrue(output.contains("cancelled successfully") || output.contains("Refund"));
    }

    @Test
    public void testCancelBookingWithin24Hours() {
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();
        PaymentSystem paymentSystem = new MockPaymentSystem();

        Performance perf = new Performance(
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().plusHours(12),       // startDateTime
                LocalDateTime.now().plusHours(14),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        Booking booking = new Booking(
                (Student) student,           // student FIRST
                1L,                          // bookingNumber
                2,                           // numTickets
                50.0,                        // amountPaid
                LocalDateTime.now(),         // bookingDateTime
                BookingStatus.ACTIVE,         // status
                perf
        );


        controller.addBooking(booking);
        mockView.addInput("1");
        controller.cancelBooking();

        String output = mockView.getOutput();
        assertTrue(output.contains("24 hours") || output.contains("cannot cancel"));
    }

    @Test
    public void testCancelBookingNotFound() {
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();
        PaymentSystem paymentSystem = new MockPaymentSystem();

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        mockView.addInput("999");
        controller.cancelBooking();

        String output = mockView.getOutput();
        assertTrue(output.contains("not found") || output.contains("error"));
    }

    @Test
    public void testCancelBookingAlreadyCancelled() {
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();
        PaymentSystem paymentSystem = new MockPaymentSystem();

        Performance perf = new Performance(
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().plusHours(48),       // startDateTime
                LocalDateTime.now().plusHours(50),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        Booking booking = new Booking(
                (Student) student,           // student FIRST
                1L,                          // bookingNumber
                2,                           // numTickets
                50.0,                        // amountPaid
                LocalDateTime.now(),         // bookingDateTime
                BookingStatus.CANCELLEDBYSTUDENT,         // status
                perf
        );

        controller.addBooking(booking);
        mockView.addInput("1");
        controller.cancelBooking();

        String output = mockView.getOutput();
        assertTrue(output.contains("already cancelled"));
    }
}