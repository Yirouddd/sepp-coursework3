package systemTest;

import controller.EventPerformanceController;
import enums.EventType;
import external.PaymentSystem;
import interfaces.View;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
 * System tests for the create event use case.
 *
 * These tests call the real createEvent() use-case method and use Mockito to
 * simulate the UI input and to check the visible output messages.
 */
public class CreateEventSystemTests {

    private View mockView;
    private PaymentSystem mockPaymentSystem;
    private EventPerformanceController controller;
    private EntertainmentProvider provider;

    @BeforeEach
    void setUp() {
        mockView = mock(View.class);
        mockPaymentSystem = mock(PaymentSystem.class);
        controller = new EventPerformanceController(mockView, mockPaymentSystem);
        provider = new EntertainmentProvider(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );
        controller.setCurrentUser(provider);
    }

    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString())).thenReturn(inputs[0], java.util.Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    private List<String> capturedSuccessMessages() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockView, atLeast(0)).displaySuccess(captor.capture());
        return captor.getAllValues();
    }

    private List<String> capturedErrorMessages() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockView, atLeast(0)).displayError(captor.capture());
        return captor.getAllValues();
    }

    private void assertContains(List<String> messages, String expected) {
        assertTrue(messages.contains(expected),
                "Expected message not found: " + expected + " | Actual messages: " + messages);
    }

    /*
     * Main success case for a ticketed event.
     * It should create the event, create one performance, and attach the event to the provider.
     */
    @Test
    void shouldCreateTicketedEventWithOnePerformanceAndSaveItForProvider() {
        stubInputs(
                "event1",
                "music",
                "yes",
                "2030-05-01 19:00",
                "2030-05-01 21:00",
                "p1, p2",
                "venue1",
                "120",
                "no",
                "yes",
                "100",
                "15.5",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "A valid ticketed event should be created.");
        assertAll(
                () -> assertEquals("event1", createdEvent.getEventTitle(),
                        "The created event should keep the title entered by the provider."),
                () -> assertEquals(EventType.Music, createdEvent.getEventType(),
                        "The event type should match the provider input."),
                () -> assertTrue(createdEvent.isTicketed(),
                        "The event should be marked as ticketed when the provider answered yes."),
                () -> assertEquals(1, createdEvent.getPerformances().size(),
                        "Exactly one performance should be attached to the created event."),
                () -> assertEquals(1, provider.getEvents().size(),
                        "The provider should own the newly created event."),
                () -> assertSame(createdEvent, provider.getEvents().get(0),
                        "The same event object should be added to the provider event list.")
        );

        Performance createdPerformance = createdEvent.getPerformances().get(0);
        assertAll(
                () -> assertEquals(1L, createdPerformance.getPerformanceId(),
                        "The first created performance should have ID 1 in a fresh controller."),
                () -> assertEquals(LocalDateTime.of(2030, 5, 1, 19, 0), createdPerformance.getStartDateTime(),
                        "Start date and time should match the entered value."),
                () -> assertEquals(LocalDateTime.of(2030, 5, 1, 21, 0), createdPerformance.getEndDateTime(),
                        "End date and time should match the entered value."),
                () -> assertEquals("venue1", createdPerformance.getVenueAddress(),
                        "Venue address should be saved correctly."),
                () -> assertEquals(100, createdPerformance.getTicketsLeft(),
                        "A new ticketed performance should start with all tickets still available."),
                () -> assertEquals(15.5, createdPerformance.getTicketPrice(), 0.0001,
                        "The original ticket price should be saved correctly.")
        );

        List<String> successMessages = capturedSuccessMessages();
        assertAll(
                () -> assertContains(successMessages, "Performance created successfully with ID 1"),
                () -> assertContains(successMessages, "Event created successfully with ID 1")
        );
        verifyNoInteractions(mockPaymentSystem);
    }

    /*
     * Non-ticketed events should skip ticket count and price questions.
     * This test also checks that blank performer input is accepted.
     */
    @Test
    void shouldCreateNonTicketedEventWithoutAskingForTicketFields() {
        stubInputs(
                "event1",
                "theatre",
                "no",
                "2030-06-01 18:30",
                "2030-06-01 20:00",
                "",
                "venue1",
                "80",
                "yes",
                "no",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "A valid non-ticketed event should still be created.");
        Performance performance = createdEvent.getPerformances().get(0);

        assertAll(
                () -> assertFalse(createdEvent.isTicketed(),
                        "The created event should be non-ticketed when the provider answered no."),
                () -> assertEquals(0, performance.getTicketsLeft(),
                        "A non-ticketed performance should keep zero tickets in the current implementation."),
                () -> assertEquals(0.0, performance.getTicketPrice(), 0.0001,
                        "A non-ticketed performance should keep zero ticket price in the current implementation."),
                () -> assertEquals(1, provider.getEvents().size(),
                        "The provider should still receive the created event.")
        );

        verify(mockView, never()).getInput("Enter number of tickets available: ");
        verify(mockView, never()).getInput("Enter ticket price: ");
    }

    /**
     * Access control is important here.
     * Admin users must not be able to create events.
     */
    @Test
    void shouldRejectCreateEventWhenCurrentUserIsAdmin() {
        controller.setCurrentUser(new AdminStaff("admin1@test.com", "pass1", "name1"));

        Event createdEvent = controller.createEvent();

        assertNull(createdEvent, "No event should be created for an admin user.");
        verify(mockView).displayError("Only entertainment providers can create events.");
        verify(mockView, never()).displaySuccess(anyString());
    }

    /**
     * Student users must also be rejected.
     */
    @Test
    void shouldRejectCreateEventWhenCurrentUserIsStudent() {
        controller.setCurrentUser(new Student("student1@test.com", "pass1", "name1", 123456789));

        Event createdEvent = controller.createEvent();

        assertNull(createdEvent, "No event should be created for a student user.");
        verify(mockView).displayError("Only entertainment providers can create events.");
        verify(mockView, never()).displaySuccess(anyString());
    }

    /**
     * Event title is mandatory.
     * When it is blank, the use case should stop directly.
     */
    @Test
    void shouldRejectEmptyEventTitle() {
        stubInputs("   ");

        Event createdEvent = controller.createEvent();

        assertAll(
                () -> assertNull(createdEvent, "The event should not be created when the title is blank."),
                () -> assertTrue(provider.getEvents().isEmpty(),
                        "The provider should still have no events after a blank title."),
                () -> assertEquals(List.of("Event title cannot be empty."), capturedErrorMessages(),
                        "The system should report the exact reason for the failure.")
        );
    }

    /**
     * A provider should not create two events with the same title.
     */
    @Test
    void shouldRejectDuplicateEventTitleForSameProvider() {
        provider.addEvent(new Event(99, "event1", EventType.Movie, true));
        stubInputs("event1");

        Event createdEvent = controller.createEvent();

        assertAll(
                () -> assertNull(createdEvent, "The duplicate event should not be created."),
                () -> assertEquals(1, provider.getEvents().size(),
                        "The provider should still have only the original event."),
                () -> assertEquals("You already have an event with this title.", capturedErrorMessages().get(0),
                        "The duplicate-title message should explain the failure clearly.")
        );
    }

    /**
     * Event type entry is validated in a loop.
     * The system should continue asking until the EP gives one supported value.
     */
    @Test
    void shouldKeepAskingUntilValidEventTypeIsGiven() {
        stubInputs(
                "event1",
                "opera",
                "music",
                "yes",
                "2030-07-01 18:00",
                "2030-07-01 20:00",
                "p1",
                "venue1",
                "100",
                "no",
                "no",
                "50",
                "20",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should be created after the provider corrects the type.");
        assertAll(
                () -> assertEquals(EventType.Music, createdEvent.getEventType(),
                        "After retry, the accepted event type should be saved."),
                () -> assertContains(capturedErrorMessages(), "Invalid event type."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }

    /**
     * yes/no questions are validated too.
     * This checks the first yes/no question in the use case.
     */
    @Test
    void shouldKeepAskingUntilValidTicketedAnswerIsGiven() {
        stubInputs(
                "event1",
                "dance",
                "maybe",
                "yes",
                "2030-08-01 18:00",
                "2030-08-01 21:00",
                "p1",
                "venue1",
                "90",
                "no",
                "no",
                "70",
                "12",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should be created after a valid yes/no retry.");
        assertAll(
                () -> assertTrue(createdEvent.isTicketed(),
                        "The final accepted answer should define the ticketed flag."),
                () -> assertContains(capturedErrorMessages(), "Please answer yes or no."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }

    /**
     * Date and time format is also validated in a loop.
     */
    @Test
    void shouldKeepAskingUntilValidDateTimeFormatIsGiven() {
        stubInputs(
                "event1",
                "movie",
                "yes",
                "01/08/2030 18:00",
                "2030-08-01 18:00",
                "2030-08-01 20:00",
                "p1",
                "venue1",
                "100",
                "yes",
                "no",
                "60",
                "8.5",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should still be created after correcting the date format.");
        assertAll(
                () -> assertContains(capturedErrorMessages(), "Invalid date/time format. Use yyyy-MM-dd HH:mm."),
                () -> assertEquals(LocalDateTime.of(2030, 8, 1, 18, 0),
                        createdEvent.getPerformances().get(0).getStartDateTime(),
                        "The saved start date/time should be the corrected one."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }

    /**
     * If the first performance is invalid because the end is before the start,
     * the current implementation ends the use case with no event created.
     * This test documents that current system behaviour clearly.
     */
    @Test
    void shouldAbortEventCreationWhenFirstPerformanceEndIsNotAfterStart() {
        stubInputs(
                "event1",
                "games",
                "yes",
                "2030-09-01 20:00",
                "2030-09-01 18:00"
        );

        Event createdEvent = controller.createEvent();

        List<String> errorMessages = capturedErrorMessages();
        assertAll(
                () -> assertNull(createdEvent,
                        "No event should be created when no valid performance was created at all."),
                () -> assertTrue(provider.getEvents().isEmpty(),
                        "The provider should not receive an event after the failed first performance."),
                () -> assertContains(errorMessages, "End date/time must be after start date/time."),
                () -> assertContains(errorMessages, "At least one valid performance must be created.")
        );
    }

    /**
     * Venue capacity is validated in a loop with positive integers only.
     */
    @Test
    void shouldKeepAskingUntilVenueCapacityIsPositiveInteger() {
        stubInputs(
                "event1",
                "music",
                "yes",
                "2030-10-01 18:00",
                "2030-10-01 20:00",
                "p1",
                "venue1",
                "zero",
                "0",
                "120",
                "no",
                "no",
                "90",
                "10",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should be created after the provider enters a valid capacity.");
        assertAll(
                () -> assertContains(capturedErrorMessages(), "Please enter a valid integer."),
                () -> assertContains(capturedErrorMessages(), "Value must be positive."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }

    /**
     * Ticket count and ticket price are important for ticketed events.
     * Both inputs should be retried until valid values are given.
     */
    @Test
    void shouldKeepAskingUntilTicketFieldsAreValid() {
        stubInputs(
                "event1",
                "sports",
                "yes",
                "2030-11-01 18:00",
                "2030-11-01 20:00",
                "p1",
                "venue1",
                "150",
                "yes",
                "no",
                "-5",
                "abc",
                "200",
                "-1.0",
                "free",
                "25.0",
                "no"
        );

        Event createdEvent = controller.createEvent();
        Performance performance = createdEvent.getPerformances().get(0);

        assertNotNull(createdEvent, "The event should be created after the ticket input is corrected.");
        assertAll(
                () -> assertEquals(200, performance.getTicketsLeft(),
                        "The saved ticket count should be the final valid number."),
                () -> assertEquals(25.0, performance.getTicketPrice(), 0.0001,
                        "The saved price should be the final valid number."),
                () -> assertContains(capturedErrorMessages(), "Value must be positive."),
                () -> assertContains(capturedErrorMessages(), "Please enter a valid integer."),
                () -> assertContains(capturedErrorMessages(), "Value cannot be negative."),
                () -> assertContains(capturedErrorMessages(), "Please enter a valid number.")
        );
    }

    /**
     * This test checks the multi-performance path.
     * Two non-overlapping performances should both be attached to the same event.
     */
    @Test
    void shouldCreateTwoPerformancesForTheSameEvent() {
        stubInputs(
                "event1",
                "music",
                "yes",
                "2030-12-01 10:00",
                "2030-12-01 12:00",
                "p1",
                "venue1",
                "100",
                "no",
                "no",
                "50",
                "10",
                "yes",
                "2030-12-01 13:00",
                "2030-12-01 15:00",
                "p2",
                "venue2",
                "110",
                "yes",
                "no",
                "60",
                "12",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should be created with both performances.");
        assertAll(
                () -> assertEquals(2, createdEvent.getPerformances().size(),
                        "Two valid performances should be attached to the same event."),
                () -> assertEquals(2, capturedSuccessMessages().stream()
                                .filter(m -> m.startsWith("Performance created successfully with ID ")).count(),
                        "The system should confirm each created performance separately."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }

    /**
     * Overlap should be rejected for the second performance of the same event.
     * After that, the provider can still enter another valid second performance.
     */
    @Test
    void shouldRejectOverlappingSecondPerformanceThenAcceptNonOverlappingReplacement() {
        stubInputs(
                "event1",
                "music",
                "yes",
                "2031-01-01 10:00",
                "2031-01-01 12:00",
                "p1",
                "venue1",
                "100",
                "no",
                "no",
                "50",
                "10",
                "yes",
                "2031-01-01 11:00",
                "2031-01-01 13:00",
                "2031-01-01 13:30",
                "2031-01-01 15:00",
                "p2",
                "venue2",
                "100",
                "yes",
                "no",
                "50",
                "12",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should still be created after the EP fixes the overlap problem.");
        assertAll(
                () -> assertEquals(2, createdEvent.getPerformances().size(),
                        "The event should finally contain the first valid and the corrected second performance."),
                () -> assertContains(capturedErrorMessages(), "This event already has a performance at overlapping times."),
                () -> assertEquals(LocalDateTime.of(2031, 1, 1, 13, 30),
                        createdEvent.getPerformances().get(1).getStartDateTime(),
                        "The second saved performance should use the corrected non-overlapping time.")
        );
    }

    /**
     * The final yes/no question should also retry until valid.
     * This is a small but useful UI validation path.
     */
    @Test
    void shouldKeepAskingUntilValidAddAnotherAnswerIsGiven() {
        stubInputs(
                "event1",
                "dance",
                "no",
                "2031-02-01 19:00",
                "2031-02-01 20:00",
                "p1",
                "venue1",
                "100",
                "no",
                "no",
                "later",
                "no"
        );

        Event createdEvent = controller.createEvent();

        assertNotNull(createdEvent, "The event should still be created when the last yes/no answer is corrected.");
        assertAll(
                () -> assertEquals(1, createdEvent.getPerformances().size(),
                        "Only one performance should exist because the final accepted answer was no."),
                () -> assertContains(capturedErrorMessages(), "Please answer yes or no."),
                () -> assertContains(capturedSuccessMessages(), "Event created successfully with ID 1")
        );
    }
}