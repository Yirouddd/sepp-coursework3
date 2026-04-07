package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import controller.UserController;
import enums.BookingStatus;
import enums.EventType;
import enums.PerformanceStatus;
import external.PaymentSystem;
import external.VerificationService;
import interfaces.View;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
  System tests for cancel performance use case.
 */
public class CancelPerformanceSystemTests {

    private static final String STUDENT1_EMAIL = "student1@test.com";
    private static final String STUDENT2_EMAIL = "student2@test.com";
    private static final String ADMIN1_EMAIL = "admin1@test.com";

    @TempDir
    Path tempDir;

    private Path studentsFile;
    private Path adminsFile;

    private View mockView;
    private VerificationService verificationService;
    private PaymentSystem paymentSystem;

    private UserController userController;
    private EventPerformanceController eventPerformanceController;
    private BookingController bookingController;
    private long nextSetupPerformanceId;

    @BeforeEach
    void setUp() throws IOException {
        studentsFile = tempDir.resolve("students.txt");
        adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(
                studentsFile,
                STUDENT1_EMAIL + ",pass1,name1,700000001\n"
                        + STUDENT2_EMAIL + ",pass2,name2,700000002\n"
        );

        Files.writeString(
                adminsFile,
                ADMIN1_EMAIL + ",pass1,name1\n"
        );

        mockView = mock(View.class);
        verificationService = mock(VerificationService.class);
        paymentSystem = mock(PaymentSystem.class);

        when(verificationService.verifyEntertainmentProvider(anyString())).thenReturn(true);
        when(paymentSystem.processPayment(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble()))
                .thenReturn(true);
        when(paymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), any()))
                .thenReturn(true);

        userController = new UserController(
                mockView,
                verificationService,
                studentsFile.toString(),
                adminsFile.toString()
        );
        eventPerformanceController = new EventPerformanceController(mockView, paymentSystem);
        bookingController = new BookingController(mockView, paymentSystem, eventPerformanceController);
        nextSetupPerformanceId = 1;
    }

    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    private Student student1() {
        return (Student) userController.getUsers().get(STUDENT1_EMAIL);
    }

    private Student student2() {
        return (Student) userController.getUsers().get(STUDENT2_EMAIL);
    }

    private AdminStaff admin1() {
        return (AdminStaff) userController.getUsers().get(ADMIN1_EMAIL);
    }

    private EntertainmentProvider addProviderDirectly(String email, String password, String org, String bn) {
        EntertainmentProvider provider = new EntertainmentProvider(
                email,
                password,
                org,
                bn,
                "name1",
                "desc1"
        );
        userController.getUsers().put(email, provider);
        return provider;
    }

    private String nextPerformanceIdInput() {
        return String.valueOf(nextSetupPerformanceId++);
    }

    /*
      Helper for setup. We use create event use case first,
      because cancel performance needs an existing performance.
     */
    private Event createTicketedEvent(
            EntertainmentProvider provider,
            String eventTitle,
            EventType eventType,
            LocalDateTime start,
            LocalDateTime end,
            int tickets,
            double ticketPrice
    ) {
        eventPerformanceController.setCurrentUser(provider);

        stubInputs(
                eventTitle,
                eventType.name().toLowerCase(),
                "yes",
                nextPerformanceIdInput(),
                formatDateTime(start),
                formatDateTime(end),
                "name1, name2",
                "venue1",
                "100",
                "no",
                "no",
                String.valueOf(tickets),
                String.valueOf(ticketPrice),
                "no"
        );

        Event event = eventPerformanceController.createEvent();
        assertNotNull(event, "Test setup should create an event before cancellation test runs.");
        clearInvocations(mockView, paymentSystem);
        return event;
    }

    /*
      Helper for setup. It makes one booking using booking use case.
     */
    private Booking createBooking(Student student, Performance performance, int numTickets) {
        bookingController.setCurrentUser(student);
        stubInputs(String.valueOf(performance.getPerformanceId()), String.valueOf(numTickets));
        bookingController.bookPerformance();
        Booking booking = performance.getBookings().iterator().next();
        clearInvocations(mockView, paymentSystem);
        return booking;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.withSecond(0).withNano(0).toString().replace("T", " ");
    }

    @Test
    void shouldCancelFuturePerformanceAndRefundAllActiveBookings() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Student s1 = student1();
        Student s2 = student2();

        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Music,
                LocalDateTime.now().plusDays(7).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(7).withHour(20).withMinute(0),
                60,
                10.0
        );
        Performance performance1 = event1.getPerformances().get(0);
        Booking booking1 = createBooking(s1, performance1, 2);
        Booking booking2 = createBooking(s2, performance1, 1);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()), "msg1");

        eventPerformanceController.cancelPerformance();

        assertAll("Cancelling should update performance and all active bookings.",
                () -> assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking1.getBookingStatus(),
                        "First booking should be cancelled by provider after successful cancellation."),
                () -> assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking2.getBookingStatus(),
                        "Second booking should be cancelled by provider after successful cancellation."),
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance status should become CANCELLED after successful cancellation."),
                () -> assertTrue(performance1.getBookings().isEmpty(),
                        "Cancelled performance should not keep active bookings inside it."),
                () -> assertTrue(event1.getPerformances().isEmpty(),
                        "Event should remove the cancelled performance from its list."),
                () -> verify(paymentSystem).processRefund(2, "event1", s1.getEmail(), s1.getPhoneNumber(), ep1.getEmail(), 20.0, "msg1"),
                () -> verify(paymentSystem).processRefund(1, "event1", s2.getEmail(), s2.getPhoneNumber(), ep1.getEmail(), 10.0, "msg1"),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRejectCancellationWhenCurrentUserIsStudent() {
        eventPerformanceController.setCurrentUser(student1());

        eventPerformanceController.cancelPerformance();

        verify(mockView).displayError("Only entertainment providers can cancel performances.");
    }

    @Test
    void shouldRejectCancellationWhenCurrentUserIsAdmin() {
        eventPerformanceController.setCurrentUser(admin1());

        eventPerformanceController.cancelPerformance();

        verify(mockView).displayError("Only entertainment providers can cancel performances.");
    }

    @Test
    void shouldRePromptWhenPerformanceIdIsEmptyThenCancelOwnedPerformance() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Theatre,
                LocalDateTime.now().plusDays(5).withHour(19).withMinute(0),
                LocalDateTime.now().plusDays(5).withHour(21).withMinute(0),
                30,
                8.0
        );
        Performance performance1 = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs("   ", String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Empty id should show error and then valid id should still allow cancellation.",
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "After retry with valid id, performance should still be cancelled."),
                () -> verify(mockView).displayError("Performance ID cannot be empty."),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRePromptWhenPerformanceIdIsNotANumberThenCancelOwnedPerformance() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Music,
                LocalDateTime.now().plusDays(6).withHour(18).withMinute(30),
                LocalDateTime.now().plusDays(6).withHour(20).withMinute(30),
                25,
                9.0
        );
        Performance performance1 = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs("abc", String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Wrong id format should not stop user from cancelling after a correct retry.",
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance should be cancelled after the provider enters a valid id."),
                () -> verify(mockView).displayError("Invalid performance ID."),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRePromptWhenPerformanceDoesNotExistThenCancelOwnedPerformance() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Movie,
                LocalDateTime.now().plusDays(8).withHour(17).withMinute(0),
                LocalDateTime.now().plusDays(8).withHour(19).withMinute(0),
                40,
                12.0
        );
        Performance performance1 = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs("999999", String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Non existing id should show error and valid retry should still work.",
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance should be cancelled after provider changes to a real id."),
                () -> verify(mockView).displayError("Performance with given number does not exist."),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRePromptWhenPerformanceBelongsToDifferentProviderThenCancelOwnPerformance() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        EntertainmentProvider ep2 = addProviderDirectly("ep2@test.com", "pass2", "org2", "bn2");

        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Music,
                LocalDateTime.now().plusDays(4).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(4).withHour(20).withMinute(0),
                50,
                11.0
        );
        Event event2 = createTicketedEvent(
                ep2,
                "event2",
                EventType.Theatre,
                LocalDateTime.now().plusDays(5).withHour(19).withMinute(0),
                LocalDateTime.now().plusDays(5).withHour(21).withMinute(0),
                50,
                11.0
        );

        Performance otherPerformance = event2.getPerformances().get(0);
        Performance ownPerformance = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(otherPerformance.getPerformanceId()), String.valueOf(ownPerformance.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Provider should not cancel other provider's performance, but can retry with own id.",
                () -> assertEquals(PerformanceStatus.ACTIVE, otherPerformance.getStatus(),
                        "Other provider's performance should stay active."),
                () -> assertEquals(PerformanceStatus.CANCELLED, ownPerformance.getStatus(),
                        "Provider's own performance should be cancelled after correct retry."),
                () -> verify(mockView).displayError("The performance with given number does not belong to you."),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRejectAlreadyCancelledPerformance() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Movie,
                LocalDateTime.now().plusDays(3).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(3).withHour(20).withMinute(0),
                20,
                7.0
        );
        Performance performance1 = event1.getPerformances().get(0);
        performance1.cancel();

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Already cancelled performance should not be cancelled again.",
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance should remain in cancelled state."),
                () -> verify(mockView).displayError("This performance has already been cancelled."),
                () -> verify(mockView, never()).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldRejectPerformanceThatAlreadyHappened() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Music,
                LocalDateTime.now().minusDays(2).withHour(18).withMinute(0),
                LocalDateTime.now().minusDays(2).withHour(20).withMinute(0),
                20,
                7.0
        );
        Performance performance1 = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("Past performance cannot be cancelled by provider.",
                () -> assertEquals(PerformanceStatus.ACTIVE, performance1.getStatus(),
                        "Past performance should keep old status because cancellation is rejected."),
                () -> verify(mockView).displayError("Performance can't be cancelled as it has already happened."),
                () -> verify(mockView, never()).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldAskAgainWhenCancellationMessageIsEmpty() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Student s1 = student1();

        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Music,
                LocalDateTime.now().plusDays(6).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(6).withHour(20).withMinute(0),
                30,
                10.0
        );
        Performance performance1 = event1.getPerformances().get(0);
        Booking booking1 = createBooking(s1, performance1, 1);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()), "   ", "msg1");

        eventPerformanceController.cancelPerformance();

        assertAll("If there are bookings, empty message should not be accepted.",
                () -> assertEquals(BookingStatus.CANCELLEDBYPROVIDER, booking1.getBookingStatus(),
                        "Booking should be cancelled after provider finally gives a valid message."),
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance should still be cancelled after valid message retry."),
                () -> verify(mockView).displayError("Please provide a non-empty message for the students."),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldStopCancellationWhenRefundFails() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Student s1 = student1();

        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Theatre,
                LocalDateTime.now().plusDays(9).withHour(18).withMinute(0),
                LocalDateTime.now().plusDays(9).withHour(20).withMinute(0),
                30,
                15.0
        );
        Performance performance1 = event1.getPerformances().get(0);
        Booking booking1 = createBooking(s1, performance1, 2);

        when(paymentSystem.processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), any()))
                .thenReturn(false);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()), "msg1");

        eventPerformanceController.cancelPerformance();

        assertAll("Refund failure should stop provider cancellation and keep data unchanged.",
                () -> assertEquals(BookingStatus.ACTIVE, booking1.getBookingStatus(),
                        "Booking should stay active when refund does not succeed."),
                () -> assertEquals(PerformanceStatus.ACTIVE, performance1.getStatus(),
                        "Performance should stay active when refund does not succeed."),
                () -> assertFalse(performance1.getBookings().isEmpty(),
                        "Bookings should still remain in performance when cancellation is stopped."),
                () -> verify(mockView).displayError("The performance could not be cancelled because refund processing failed."),
                () -> verify(mockView, never()).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }

    @Test
    void shouldCancelWithoutAskingForMessageWhenThereAreNoActiveBookings() {
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "pass1", "org1", "bn1");
        Event event1 = createTicketedEvent(
                ep1,
                "event1",
                EventType.Movie,
                LocalDateTime.now().plusDays(10).withHour(17).withMinute(0),
                LocalDateTime.now().plusDays(10).withHour(19).withMinute(0),
                30,
                6.0
        );
        Performance performance1 = event1.getPerformances().get(0);

        eventPerformanceController.setCurrentUser(ep1);
        stubInputs(String.valueOf(performance1.getPerformanceId()));

        eventPerformanceController.cancelPerformance();

        assertAll("No-booking cancellation should not ask for extra message.",
                () -> assertEquals(PerformanceStatus.CANCELLED, performance1.getStatus(),
                        "Performance should be cancelled even when there are no bookings."),
                () -> verify(mockView, never()).displayError("Please provide a non-empty message for the students."),
                () -> verify(paymentSystem, never()).processRefund(anyInt(), anyString(), anyString(), anyInt(), anyString(), anyDouble(), any()),
                () -> verify(mockView).displaySuccess("The performance has been cancelled and refunds have been processed if required.")
        );
    }
}