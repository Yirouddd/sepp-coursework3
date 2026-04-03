package test;

import controller.BookingController;
import interfaces.View;
import object.Booking;
import object.Performance;
import enums.BookingStatus;
import user.Student;
import user.User;
import external.PaymentSystem;
import external.MockPaymentSystem;
import test.MockView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                1L,
                LocalDateTime.now().plusHours(48),
                LocalDateTime.now().plusHours(50),
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        // Create booking
        Booking booking = new Booking(
                1L,
                2,
                50.0,
                LocalDateTime.now(),
                BookingStatus.ACTIVE,
                (Student) student,
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
                1L,
                LocalDateTime.now().plusHours(12),
                LocalDateTime.now().plusHours(14),
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        Booking booking = new Booking(
                1L,
                2,
                50.0,
                LocalDateTime.now(),
                BookingStatus.ACTIVE,
                (Student) student,
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
                1L,
                LocalDateTime.now().plusHours(48),
                LocalDateTime.now().plusHours(50),
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, paymentSystem);

        Booking booking = new Booking(
                1L,
                2,
                50.0,
                LocalDateTime.now(),
                BookingStatus.CANCELLEDBYSTUDENT,
                (Student) student,
                perf
        );

        controller.addBooking(booking);
        mockView.addInput("1");
        controller.cancelBooking();

        String output = mockView.getOutput();
        assertTrue(output.contains("already cancelled"));
    }
}