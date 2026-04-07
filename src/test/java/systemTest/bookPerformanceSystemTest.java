package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import enums.EventType;
import external.PaymentSystem;
import interfaces.View;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.EntertainmentProvider;
import user.Student;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class bookPerformanceSystemTest {

    private View mockView;
    private PaymentSystem paymentSystem;
    private EventPerformanceController eventPerformanceController;
    private BookingController bookingController;
    private Student student;
    private Performance performance;

    // Set up one future ticketed performance and one student before each test.
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
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0);

        addPerformanceToController(performance);

        student = new Student("olivia@mail.com", "123", "Olivia", 123);
        bookingController = new BookingController(mockView, paymentSystem, eventPerformanceController);
        bookingController.setCurrentUser(student);
    }

    // Stub a sequence of user inputs for the mocked view.
    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    // Register a performance in the shared event controller store.
    private void addPerformanceToController(Performance performanceToAdd) throws Exception {
        Field performancesField = EventPerformanceController.class.getDeclaredField("performances");
        performancesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Performance> performances = (Collection<Performance>) performancesField.get(eventPerformanceController);
        performances.add(performanceToAdd);
    }

    // Read the shared booking store from BookingController for stronger state checks.
    private Collection<Booking> getSystemBookings() throws Exception {
        Field bookingsField = BookingController.class.getDeclaredField("bookings");
        bookingsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Booking> bookings = (Collection<Booking>) bookingsField.get(bookingController);
        return bookings;
    }

    @Test
    void bookPerformanceSuccess_displaysBookingRecord() {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A booking record should be displayed after a successful booking"))
                .displayBookingRecord(argThat(msg -> msg.contains("Booking #")));
    }

    @Test
    void bookPerformanceSuccess_displaysSuccessMessage() {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A success message should be shown after booking"))
                .displaySuccess("Booking successful.");
    }

    @Test
    void bookPerformanceSuccess_updatesSystemStateAndPaymentDetails() throws Exception {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        assertAll(
                () -> assertEquals(48, performance.getTicketsLeft(),
                        "Booking 2 tickets should reduce remaining tickets from 50 to 48."),
                () -> assertEquals(1, performance.getBookings().size(),
                        "Successful booking should be stored on the performance."),
                () -> assertEquals(1, getSystemBookings().size(),
                        "Successful booking should also be stored in the controller booking store.")
        );

        verify(paymentSystem, description("Payment should be requested with the booked ticket count and total price"))
                .processPayment(2, "Concert", "olivia@mail.com", 123, "Unknown email", 30.0);
    }

    @Test
    void bookPerformanceSuccess_bookingRecordContainsStudentDetails() {
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("Booking record should contain student name, email and phone number"))
                .displayBookingRecord(argThat(msg ->
                        msg.contains("Olivia") && msg.contains("olivia@mail.com") && msg.contains("123")));
    }

    @Test
    void bookPerformanceSuccess_allRemainingTicketsCanBeBooked() {
        stubInputs("1", "50");

        bookingController.bookPerformance();

        assertEquals(0, performance.getTicketsLeft(),
                "Booking exactly the remaining number of tickets should be allowed.");
        verify(mockView).displaySuccess("Booking successful.");
    }

    @Test
    void bookPerformanceEP_displaysError() {
        EntertainmentProvider ep = new EntertainmentProvider("ep@mail.com", "password", "Org", "BN1", "Name", "Desc");
        bookingController.setCurrentUser(ep);

        bookingController.bookPerformance();

        verify(mockView, description("Only students should be able to book performances"))
                .displayError("Only students can perform this action.");
        verifyNoInteractions(paymentSystem);
    }

    @Test
    void bookPerformance_invalidPerformanceId_displaysError() {
        stubInputs("abc", "1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("An invalid performance ID should show an error"))
                .displayError("Please enter a valid numeric performance ID.");
        verify(mockView).displaySuccess("Booking successful.");
    }

    @Test
    void nonExistentPerformanceId_displaysError() {
        stubInputs("999", "1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("An unknown performance ID should show an error"))
                .displayError("Invalid performance ID.");
        verify(mockView).displaySuccess("Booking successful.");
    }

    @Test
    void invalidTicketCountFormat_displaysError() throws Exception {
        stubInputs("1", "two");

        bookingController.bookPerformance();

        verify(mockView, description("A non-numeric ticket count should show an error"))
                .displayError("Number of tickets must be a valid integer.");
        verify(paymentSystem, never()).processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble());
        assertTrue(getSystemBookings().isEmpty(),
                "No booking should be stored when the ticket count is not a valid integer.");
    }

    @Test
    void noTickets_displaysError() throws Exception {
        stubInputs("1", "0");

        bookingController.bookPerformance();

        verify(mockView, description("Requesting zero tickets should show an error"))
                .displayError("Number of tickets must be positive.");
        verify(paymentSystem, never()).processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble());
        assertTrue(getSystemBookings().isEmpty(),
                "No booking should be stored when zero tickets are requested.");
    }

    @Test
    void negativeTickets_displaysError() throws Exception {
        stubInputs("1", "-2");

        bookingController.bookPerformance();

        verify(mockView, description("Requesting a negative number of tickets should show an error"))
                .displayError("Number of tickets must be positive.");
        verify(paymentSystem, never()).processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble());
        assertTrue(getSystemBookings().isEmpty(),
                "No booking should be stored when a negative number of tickets is requested.");
    }

    @Test
    void tooManyTickets_displaysError() throws Exception {
        stubInputs("1", "999");

        bookingController.bookPerformance();

        verify(mockView, description("Requesting more tickets than available should show an error"))
                .displayError("Not enough tickets left.");
        verify(paymentSystem, never()).processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble());
        assertTrue(getSystemBookings().isEmpty(),
                "No booking should be stored when requested tickets exceed availability.");
    }

    @Test
    void paymentFails_displaysError() throws Exception {
        when(paymentSystem.processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble()))
                .thenReturn(false);
        stubInputs("1", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A failed payment should show an error and not confirm the booking"))
                .displayError("Payment was unsuccessful therefore booking unsuccessful.");
        verify(mockView, never()).displaySuccess("Booking successful.");
        verify(mockView, never()).displayBookingRecord(anyString());
        assertTrue(getSystemBookings().isEmpty(),
                "No booking should be stored when payment fails.");
        assertTrue(performance.getBookings().isEmpty(),
                "Performance should not contain a booking when payment fails.");
    }

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

        addPerformanceToController(pastPerformance);
        stubInputs("2", "2");

        bookingController.bookPerformance();

        verify(mockView, description("A past performance should not be bookable"))
                .displayError("Cannot book a performance that has already started or ended.");
        verify(paymentSystem, never()).processPayment(eq(2), eq("Old Gig"), anyString(), anyInt(), anyString(), anyDouble());
    }

    @Test
    void bookPerformance_cancelledPerformance_displaysError() throws Exception {
        Event cancelledEvent = new Event(4, "Cancelled Concert", EventType.Music, true);
        Performance cancelledPerformance = cancelledEvent.createPerformance(
                4,
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(4).plusHours(2),
                List.of("Band D"),
                "Arena",
                100,
                false,
                false,
                50,
                20.0);
        cancelledPerformance.cancel();

        addPerformanceToController(cancelledPerformance);
        stubInputs("4", "2");

        bookingController.bookPerformance();

        verify(mockView, description("Cancelled performances should not be bookable"))
                .displayError("Cancelled performances cannot be booked.");
        verify(paymentSystem, never()).processPayment(eq(2), eq("Cancelled Concert"), anyString(), anyInt(), anyString(), anyDouble());
    }

    @Test
    void bookPerformance_nonTicketedEvent_displaysError() throws Exception {
        Event freeEvent = new Event(3, "Free Concert", EventType.Music, false);
        Performance freePerformance = freeEvent.createPerformance(
                3,
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(5).plusHours(2),
                List.of("Band C"),
                "Park",
                200,
                true,
                false,
                0,
                0.0);

        addPerformanceToController(freePerformance);
        stubInputs("3");

        bookingController.bookPerformance();

        verify(mockView, description("Non-ticketed performances should not require booking"))
                .displayError("This performance is not ticketed, so no booking is needed.");
        verify(mockView, never()).displaySuccess("Booking successful.");
        verify(paymentSystem, never()).processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble());
    }
}