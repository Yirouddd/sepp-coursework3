package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import enums.EventType;
import external.PaymentSystem;
import interfaces.View;
import object.Performance;
import object.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.Student;
import user.EntertainmentProvider;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * System tests for the book performance use case.
 * Covers successful booking, invalid inputs, non-student users,
 * payment failure, and capacity limits.
 */
public class bookPerformanceSystemTest {

    private View mockView;
    private PaymentSystem paymentSystem;
    private EventPerformanceController eventPerformanceController;
    private BookingController bookingController;
    private Student student;
    private Performance performance;

    /**
     * Sets up a fresh controller, mock view, payment system, student, and a
     * registered future ticketed performance before each test.
     *
     * @throws Exception if the performances field cannot be accessed via reflection
     */
    @BeforeEach
    void setup() throws Exception {
        mockView = mock(View.class);
        paymentSystem = mock(PaymentSystem.class);
        when(paymentSystem.processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble()))
                .thenReturn(true);

        eventPerformanceController = new EventPerformanceController(mockView, paymentSystem);

        Event event = new Event(1, "Concert", EventType.Music, true);
        performance = event.createPerformance(
                1,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0);

        // Register the performance
        Field performancesField = EventPerformanceController.class.getDeclaredField("performances");
        performancesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Performance> performances = (Collection<Performance>) performancesField.get(eventPerformanceController);
        performances.add(performance);

        student = new Student("olivia@mail.com", "123", "Olivia", 123);
        bookingController = new BookingController(mockView, paymentSystem, eventPerformanceController);
        bookingController.setCurrentUser(student);
    }

    /**
     * Helper to stub the view's getInput to return a sequence of values.
     *
     * @param inputs the sequence of values to return
     */
    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    /**
     * Tests that a student can successfully book a ticketed performance.
     * Verifies that a booking record is displayed after a valid booking.
     */
    @Test
    void bookPerformanceSuccess_displaysBookingRecord() {
        stubInputs("1", "2"); // performanceId = 1, 2 tickets

        bookingController.bookPerformance();

        verify(mockView, description("A booking record should be displayed after a successful booking"))
                .displayBookingRecord(argThat(msg -> msg.contains("Booking #")));
    }

    /**
     * Tests that a successful booking displays a success message.
     */
    @Test
    void bookPerformanceSuccess_displaysSuccessMessage() {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A success message should be shown after booking")).displaySuccess("Booking successful.");
    }

    /**
     * Tests that an EP cannot book a performance.
     */
    @Test
    void bookPerformanceEP_displaysError() {
        EntertainmentProvider ep = new EntertainmentProvider("ep@mail.com", "password", "Org", "BN1", "Name", "Desc");
        bookingController.setCurrentUser(ep);

        bookingController.bookPerformance();

        verify(mockView, description("Only students should be able to book performances"))
                .displayError("Only students can perform this action.");
    }

    /**
     * Tests that entering an invalid performance ID displays an error.
     * A valid ID is provided as a retry so the loop can exit.
     */
    @Test
    void bookPerformance_invalidPerformanceId_displaysError() {
        stubInputs("abc", "1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("An invalid performance ID should show an error"))
                .displayError("Please enter a valid numeric performance ID.");
    }

    /**
     * Tests that entering a performance ID that doesn't exist displays an error.
     * A valid ID is provided as a retry so the loop can exit.
     */
    @Test
    void nonExistentPerformanceId_displaysError() {
        stubInputs("999", "1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("An unknown performance ID should show an error"))
                .displayError("Invalid performance ID.");
    }

    /**
     * Tests that requesting 0 tickets displays an error and doesn't book.
     */
    @Test
    void noTickets_displaysError() {
        stubInputs("1", "0");

        bookingController.bookPerformance();

        verify(mockView, description("Requesting zero tickets should show an error"))
                .displayError("Number of tickets must be positive.");
    }

    /**
     * Tests that requesting more tickets than available displays an error.
     */
    @Test
    void tooManyTickets_displaysError() {
        stubInputs("1", "999"); // performance only has 50 tickets

        bookingController.bookPerformance();

        verify(mockView, description("Requesting more tickets than available should show an error"))
                .displayError("Not enough tickets left.");
    }

    /**
     * Tests that a failed payment prevents the booking from completing.
     */
    @Test
    void paymentFails_displaysError() {
        when(paymentSystem.processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble()))
                .thenReturn(false);
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A failed payment should show an error and not confirm the booking"))
                .displayError("Payment was unsuccessful therefore booking unsuccessful.");
    }

    /**
     * Tests that a past performance cannot be booked.
     * Verifies an error is shown when the performance has already started.
     *
     * @throws Exception if the performances field cannot be accessed via reflection
     */
    @Test
    void bookPerformance_pastPerformance_displaysError() throws Exception {
        Event pastEvent = new Event(2, "Old Gig", EventType.Music, true);
        Performance pastPerformance = pastEvent.createPerformance(
                2,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                List.of("Band B"),
                "Old Hall",
                100,
                false,
                false,
                50,
                10.0);

        Field performancesField = EventPerformanceController.class.getDeclaredField("performances");
        performancesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Performance> performances = (Collection<Performance>) performancesField.get(eventPerformanceController);
        performances.add(pastPerformance);

        stubInputs("2", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A past performance should not be bookable"))
                .displayError("Cannot book a performance that has already started or ended.");
    }

    /**
     * Tests that a non-ticketed performance cannot be booked.
     * The system should notify the student that no booking is needed.
     */
    @Test
    void bookPerformance_nonTicketedEvent_displaysError() throws Exception {
        Event freeEvent = new Event(3, "Free Concert", EventType.Music, false);
        Performance freePerformance = freeEvent.createPerformance(
                3,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band C"),
                "Park",
                200,
                true,
                false,
                0,
                0.0);

        Field performancesField = EventPerformanceController.class.getDeclaredField("performances");
        performancesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Performance> performances =
                (Collection<Performance>) performancesField.get(eventPerformanceController);
        performances.add(freePerformance);

        stubInputs("3");

        bookingController.bookPerformance();

        verify(mockView, description("Non-ticketed performances should not require booking"))
                .displayError("This performance is not ticketed, so no booking is needed.");
    }

    /**
     * Tests that the booking record contains the student's name and email and ohone number.
     */
    @Test
    void bookPerformanceSuccess_bookingRecordContainsStudentDetails() {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("Booking record should contain student name, email and phone number"))
                .displayBookingRecord(argThat(msg ->
                        msg.contains("Olivia") && msg.contains("olivia@mail.com") && msg.contains("123")));
    }
}