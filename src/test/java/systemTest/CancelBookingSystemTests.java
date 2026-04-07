package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import enums.BookingStatus;
import enums.EventType;
import external.PaymentSystem;
import interfaces.View;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * System tests for cancel booking use case.
 */
public class CancelBookingSystemTests {

    private View mockView;
    private PaymentSystem mockPaymentSystem;
    private EventPerformanceController eventPerformanceController;
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        mockView = Mockito.mock(View.class);
        mockPaymentSystem = Mockito.mock(PaymentSystem.class);
        eventPerformanceController = new EventPerformanceController(mockView, mockPaymentSystem);
        bookingController = new BookingController(mockView, mockPaymentSystem, eventPerformanceController);
    }

    private Student createStudent(String email, String name, int phoneNumber) {
        return new Student(email, "pass", name, phoneNumber);
    }

    private EntertainmentProvider createProvider(String email) {
        return new EntertainmentProvider(email, "pass", "org", "bn", "name", "desc");
    }

    private AdminStaff createAdmin() {
        return new AdminStaff("admin@test.com", "pass", "name");
    }

    private Performance createTicketedPerformance(long eventId,
                                                  long performanceId,
                                                  String eventTitle,
                                                  LocalDateTime start,
                                                  LocalDateTime end) {
        EntertainmentProvider provider = createProvider("ep@test.com");
        Event event = new Event(eventId, eventTitle, EventType.Music, true);
        event.setOrganizer(provider);
        provider.addEvent(event);

        Performance performance = event.createPerformance(
                performanceId,
                start,
                end,
                List.of("performer1"),
                "venue1",
                100,
                false,
                false,
                50,
                20.0
        );

        eventPerformanceController.addEvent(event);
        eventPerformanceController.addPerformance(performance);
        return performance;
    }

    /*
     * Use the real booking use case to create one more active booking in the shared controller store.
     * This helper must work even when the same performance already has bookings from earlier setup.
     */
    private Booking createActiveBookingThroughUseCase(Student student, Performance performance, int numTickets) {
        int previousCount = performance.getBookings().size();
        List<Booking> bookingsBefore = new ArrayList<>(performance.getBookings());

        bookingController.setCurrentUser(student);
        when(mockPaymentSystem.processPayment(
                eq(numTickets),
                eq(performance.getEventTitle()),
                eq(student.getEmail()),
                eq(student.getPhoneNumber()),
                eq(performance.getOrganiserEmail()),
                eq(performance.getFinalTicketPrice() * numTickets)
        )).thenReturn(true);
        when(mockView.getInput(anyString())).thenReturn(
                String.valueOf(performance.getPerformanceId()),
                String.valueOf(numTickets)
        );

        bookingController.bookPerformance();

        assertEquals(previousCount + 1, performance.getBookings().size(),
                "Test setup should add exactly one new booking for the requested performance.");

        Booking createdBooking = null;
        for (Booking booking : performance.getBookings()) {
            if (!bookingsBefore.contains(booking)) {
                createdBooking = booking;
                break;
            }
        }

        assertNotNull(createdBooking,
                "Test setup should be able to find the booking that was just created.");
        assertTrue(createdBooking.checkBookedByStudent(student.getEmail()),
                "The created booking should belong to the student used in this setup step.");

        reset(mockView, mockPaymentSystem);
        return createdBooking;
    }

    /*
      Used only when a past booking is needed.
      A normal booking use case cannot create a booking for an already started performance.
     */
    private Booking injectBookingIntoSystem(Student student,
                                            Performance performance,
                                            long bookingNumber,
                                            int numTickets) {
        Booking booking = new Booking(
                student,
                performance,
                bookingNumber,
                numTickets,
                performance.getFinalTicketPrice() * numTickets,
                LocalDateTime.now().minusDays(3)
        );

        performance.addBooking(booking);
        student.addBooking(booking);
        getSystemBookings().add(booking);
        return booking;
    }

    @SuppressWarnings("unchecked")
    private Collection<Booking> getSystemBookings() {
        try {
            Field field = BookingController.class.getDeclaredField("bookings");
            field.setAccessible(true);
            return (Collection<Booking>) field.get(bookingController);
        } catch (ReflectiveOperationException e) {
            fail("Test setup failed because BookingController bookings store could not be accessed.");
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Booking> getStudentBookings(Student student) {
        try {
            Field field = Student.class.getDeclaredField("bookings");
            field.setAccessible(true);
            return (List<Booking>) field.get(student);
        } catch (ReflectiveOperationException e) {
            fail("Test setup failed because Student bookings store could not be accessed.");
            return List.of();
        }
    }

    private void assertBookingStillPresentEverywhere(Booking booking, Performance performance, Student student) {
        assertAll(
                () -> assertTrue(getSystemBookings().contains(booking),
                        "The booking should still remain in the controller store."),
                () -> assertTrue(performance.getBookings().contains(booking),
                        "The booking should still remain attached to the performance."),
                () -> assertTrue(getStudentBookings(student).contains(booking),
                        "The booking should still remain attached to the student record.")
        );
    }

    private void assertBookingRemovedFromEverywhere(Booking booking, Performance performance, Student student) {
        assertAll(
                () -> assertFalse(getSystemBookings().contains(booking),
                        "The booking should be removed from the controller store after cancellation."),
                () -> assertFalse(performance.getBookings().contains(booking),
                        "The booking should be removed from the performance after cancellation."),
                () -> assertFalse(getStudentBookings(student).contains(booking),
                        "The booking should be removed from the student after cancellation.")
        );
    }

    /*
      Guest user is not allowed to cancel bookings.
     */
    @Test
    void shouldRejectCancellationWhenCurrentUserIsGuest() {
        bookingController.setCurrentUser(null);

        bookingController.cancelBooking();

        verify(mockView).displayError("Only students can perform this action.");
        verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull());
    }

    /*
      Admin user is not allowed to cancel bookings.
     */
    @Test
    void shouldRejectCancellationWhenCurrentUserIsAdmin() {
        bookingController.setCurrentUser(createAdmin());

        bookingController.cancelBooking();

        verify(mockView).displayError("Only students can perform this action.");
        verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull());
    }

    /*
      Entertainment provider is not allowed to cancel student bookings.
     */
    @Test
    void shouldRejectCancellationWhenCurrentUserIsEntertainmentProvider() {
        bookingController.setCurrentUser(createProvider("ep2@test.com"));

        bookingController.cancelBooking();

        verify(mockView).displayError("Only students can perform this action.");
        verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull());
    }

    /*
      Bad booking number text should not stop the use case.
      The student can enter a correct booking number after that.
     */
    @Test
    void shouldRejectNonNumericBookingNumberThenAcceptValidRetry() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 2);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn("abc", String.valueOf(booking.getBookingNumber()));
        when(mockPaymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()))
                .thenReturn(true);

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Please enter a valid numeric booking number."),
                () -> verify(mockView).displaySuccess("Booking cancelled successfully."),
                () -> assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus(),
                        "The booking should be cancelled after the valid retry.")
        );
    }

    /*
      Unknown booking number should show error and let the student try again.
     */
    @Test
    void shouldRejectUnknownBookingNumberThenAcceptValidRetry() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn("999", String.valueOf(booking.getBookingNumber()));
        when(mockPaymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()))
                .thenReturn(true);

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Invalid booking number or this booking does not belong to you."),
                () -> verify(mockView).displaySuccess("Booking cancelled successfully."),
                () -> assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus(),
                        "The booking should be cancelled after the valid booking number is entered.")
        );
    }

    /*
      Student must not cancel another student's booking.
      After the error, the student can still cancel their own booking.
     */
    @Test
    void shouldRejectBookingBelongingToAnotherStudentThenAcceptOwnBookingOnRetry() {
        Student currentStudent = createStudent("student1@test.com", "name1", 111111111);
        Student otherStudent = createStudent("student2@test.com", "name2", 222222222);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusHours(2));

        Booking otherBooking = createActiveBookingThroughUseCase(otherStudent, performance, 1);
        Booking ownBooking = createActiveBookingThroughUseCase(currentStudent, performance, 2);

        bookingController.setCurrentUser(currentStudent);
        when(mockView.getInput(anyString())).thenReturn(
                String.valueOf(otherBooking.getBookingNumber()),
                String.valueOf(ownBooking.getBookingNumber())
        );
        when(mockPaymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()))
                .thenReturn(true);

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Invalid booking number or this booking does not belong to you."),
                () -> verify(mockView).displaySuccess("Booking cancelled successfully."),
                () -> assertEquals(BookingStatus.ACTIVE, otherBooking.getBookingStatus(),
                        "Another student's booking should stay active."),
                () -> assertEquals(BookingStatus.CANCELLEDBYSTUDENT, ownBooking.getBookingStatus(),
                        "The current student's own booking should be cancelled after retry.")
        );
    }

    /*
      A booking already cancelled by student is not active anymore.
     */
    @Test
    void shouldRejectCancellationWhenBookingWasAlreadyCancelledByStudent() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);
        booking.cancelByStudent();

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("This booking is not active."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus(),
                        "The old booking status should stay unchanged.")
        );
    }

    /*
      Provider-cancelled booking is also not active.
     */
    @Test
    void shouldRejectCancellationWhenBookingWasAlreadyCancelledByProvider() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);
        booking.cancelByProvider();

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("This booking is not active."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking.getBookingStatus(),
                        "The provider-cancelled status should stay unchanged.")
        );
    }

    /*
      Payment-failed booking is also not active.
     */
    @Test
    void shouldRejectCancellationWhenBookingPaymentPreviouslyFailed() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(4).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);
        booking.cancelPaymentFailed();

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("This booking is not active."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.PAYMENTFAILED, booking.getBookingStatus(),
                        "The payment-failed status should stay unchanged.")
        );
    }

    /*
      Booking can only be cancelled when the start time is more than 24 hours away.
     */
    @Test
    void shouldRejectCancellationWhenPerformanceStartsInLessThan24Hours() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusHours(23), LocalDateTime.now().plusHours(25));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Bookings can only be cancelled if the performance is at least 24 hours away."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus(),
                        "The booking should stay active when the performance is too soon.")
        );
        assertBookingStillPresentEverywhere(booking, performance, student);
    }

    /*
      Exactly 24 hours away is still not enough because the code checks isAfter(now plus 24h).
     */
    @Test
    void shouldRejectCancellationWhenPerformanceStartsExactly24HoursAway() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusHours(24), LocalDateTime.now().plusHours(26));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 1);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Bookings can only be cancelled if the performance is at least 24 hours away."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus(),
                        "The booking should stay active at the 24-hour boundary as well.")
        );
        assertBookingStillPresentEverywhere(booking, performance, student);
    }

    /*
      Old bookings for a started or past performance must also be rejected.
     */
    @Test
    void shouldRejectCancellationWhenPerformanceAlreadyStartedOrPassed() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1));
        Booking booking = injectBookingIntoSystem(student, performance, 77, 2);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Bookings can only be cancelled if the performance is at least 24 hours away."),
                () -> verify(mockPaymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()),
                () -> assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus(),
                        "The historical booking should stay active when cancellation is too late.")
        );
        assertBookingStillPresentEverywhere(booking, performance, student);
    }

    /*
      If refund fails, the booking must remain unchanged everywhere.
     */
    @Test
    void shouldKeepBookingActiveWhenRefundFails() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(6), LocalDateTime.now().plusDays(6).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 2);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));
        when(mockPaymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()))
                .thenReturn(false);

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displayError("Refund was unsuccessful, so the booking was not cancelled."),
                () -> assertEquals(BookingStatus.ACTIVE, booking.getBookingStatus(),
                        "The booking should stay active when refund fails."),
                () -> verify(mockPaymentSystem).processRefund(
                        eq(2),
                        eq("event1"),
                        eq(student.getEmail()),
                        eq(student.getPhoneNumber()),
                        eq("ep@test.com"),
                        eq(40.0),
                        isNull()
                )
        );
        assertBookingStillPresentEverywhere(booking, performance, student);
    }

    /*
      Main success path.
      Refund succeeds and the booking is removed from all places.
     */
    @Test
    void shouldCancelBookingAndRemoveItFromAllStoresWhenRefundSucceeds() {
        Student student = createStudent("student1@test.com", "name1", 111111111);
        Performance performance = createTicketedPerformance(1, 1, "event1",
                LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(7).plusHours(2));
        Booking booking = createActiveBookingThroughUseCase(student, performance, 2);

        bookingController.setCurrentUser(student);
        when(mockView.getInput(anyString())).thenReturn(String.valueOf(booking.getBookingNumber()));
        when(mockPaymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), isNull()))
                .thenReturn(true);

        bookingController.cancelBooking();

        assertAll(
                () -> verify(mockView).displaySuccess("Booking cancelled successfully."),
                () -> verify(mockPaymentSystem).processRefund(
                        eq(2),
                        eq("event1"),
                        eq(student.getEmail()),
                        eq(student.getPhoneNumber()),
                        eq("ep@test.com"),
                        eq(40.0),
                        isNull()
                ),
                () -> assertEquals(BookingStatus.CANCELLEDBYSTUDENT, booking.getBookingStatus(),
                        "The booking status should change to cancelled by student after successful refund.")
        );
        assertBookingRemovedFromEverywhere(booking, performance, student);
    }
}