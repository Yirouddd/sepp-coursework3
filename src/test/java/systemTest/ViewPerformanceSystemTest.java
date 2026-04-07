package systemTest;

import controller.Controller;
import controller.EventPerformanceController;
import enums.EventType;
import external.MockPaymentSystem;
import external.PaymentSystem;
import interfaces.View;
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

public class ViewPerformanceSystemTest {

    private EventPerformanceController controller;
    private MockView mockView;
    private PaymentSystem paymentSystem;

    // Fake view used to simulate user input and capture output.
    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> performanceOutputs = new ArrayList<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input) {
            this.inputs.addAll(Arrays.asList(input));
        }

        // Return empty string when no input is left, similar to pressing enter.
        @Override
        public String getInput(String inputPrompt) {
            if (inputs.isEmpty()) {
                return "";
            }
            return inputs.remove();
        }

        @Override
        public void displaySuccess(String successMessage) {
            successMessages.add(successMessage);
        }

        @Override
        public void displayError(String errorMessage) {
            errorMessages.add(errorMessage);
        }

        @Override
        public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {
        }

        @Override
        public void displaySpecificPerformance(String performanceInfo) {
            performanceOutputs.add(performanceInfo);
        }

        @Override
        public void displayBookingRecord(String bookingRecord) {
        }
    }

    @BeforeEach
    void setup() {
        mockView = new MockView();
        paymentSystem = new MockPaymentSystem();
        controller = new EventPerformanceController(mockView, paymentSystem);
    }

    private void setCurrentUser(Object user) throws Exception {
        Field currentUserField = Controller.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(controller, user);
    }

    private Event setupEventAndPerformance() {
        EntertainmentProvider ep = new EntertainmentProvider(
                "ep@ed.ac.uk",
                "passwordEP",
                "OrgName",
                "BN001",
                "Alice EP",
                "description"
        );

        Event event = new Event(1L, "Concert", EventType.Music, true);
        event.setOrganizer(ep);

        LocalDateTime start1 = LocalDateTime.now().plusDays(5).withHour(19).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end1 = start1.plusHours(2);

        LocalDateTime start2 = LocalDateTime.now().plusDays(6).withHour(19).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end2 = start2.plusHours(3);

        Performance p1 = event.createPerformance(
                1L,
                start1,
                end1,
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0
        );

        Performance p2 = event.createPerformance(
                2L,
                start2,
                end2,
                List.of("Band B"),
                "Main Hall",
                100,
                false,
                false,
                50,
                25.0
        );

        p1.review(5, "Amazing");
        p2.review(4, "Coool");

        controller.addEvent(event);
        controller.addPerformance(p1);
        controller.addPerformance(p2);

        return event;
    }

    // When there are no performances at all, the use case should stop with an error.
    @Test
    void noPerformancesShouldShowError() {
        controller.viewPerformance();

        assertTrue(
                mockView.errorMessages.contains("No performances available."),
                "The system should show an error when no performances exist."
        );
    }

    // Non-numeric input should show an error, and then a valid retry should still work.
    @Test
    void testInvalidInputHandling() throws Exception {
        mockView = new MockView("ab", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("Invalid input. Please enter a number."),
                        "A non-numeric performance ID should show the invalid input error."
                ),
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "A later valid ID should still display a performance."
                ),
                () -> assertEquals(
                        1,
                        mockView.performanceOutputs.size(),
                        "Exactly one performance should be displayed after the valid retry."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.get(0).contains("Concert"),
                        "The displayed performance should belong to the expected event."
                )
        );
    }

    // A non-existent performance ID should show an error, then a valid ID should succeed.
    @Test
    void testInvalidPerformanceIDThenValid() throws Exception {
        mockView = new MockView("10", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("Invalid ID. Please try again."),
                        "An unknown performance ID should show the invalid ID error."
                ),
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "A later valid ID should still display a performance."
                ),
                () -> assertEquals(
                        1,
                        mockView.performanceOutputs.size(),
                        "Exactly one performance should be displayed after the valid retry."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.get(0).contains("Concert"),
                        "The displayed performance should belong to the expected event."
                )
        );
    }

    // After too many invalid attempts, the use case should stop.
    @Test
    void shouldFailAfterManyAttempts() throws Exception {
        mockView = new MockView("8", "9", "3", "4", "5", "6", "7");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("Too many unsuccessful attempts were made"),
                        "The system should stop after too many unsuccessful attempts."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.isEmpty(),
                        "No performance should be displayed when the process ends in failure."
                )
        );
    }

    // Empty input is handled the same way as other invalid input.
    @Test
    void emptyInputShouldBeAsInvalid() throws Exception {
        mockView = new MockView("", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message -> message.contains("Invalid input")),
                        "Empty input should be treated as invalid input."
                ),
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "After the valid retry, one performance should be displayed."
                ),
                () -> assertEquals(
                        1,
                        mockView.performanceOutputs.size(),
                        "Exactly one performance should be displayed after the valid retry."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.get(0).contains("Concert"),
                        "The displayed performance should belong to the expected event."
                )
        );
    }

    // The seventh attempt is still allowed by the current implementation.
    @Test
    void shouldSucceedWhenSeventhAttemptIsValid() throws Exception {
        mockView = new MockView("a", "9", "", "51", "100", "200", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "The seventh input should still be accepted if it is valid."
                ),
                () -> assertEquals(
                        1,
                        mockView.performanceOutputs.size(),
                        "Exactly one performance should be displayed after a valid seventh attempt."
                ),
                () -> assertFalse(
                        mockView.errorMessages.contains("Too many unsuccessful attempts were made"),
                        "The system should not fail when the seventh attempt is valid."
                )
        );
    }

    // After some invalid attempts, the system should still display all expected details.
    @Test
    void displaysFullDetailsSuccessfullyAfterInvalidInputs() throws Exception {
        mockView = new MockView("a", "9", "51", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("Invalid input. Please enter a number."),
                        "A non-numeric input should show the invalid input error."
                ),
                () -> assertTrue(
                        mockView.errorMessages.contains("Invalid ID. Please try again."),
                        "A non-existent ID should show the invalid ID error."
                ),
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "A valid final input should display one performance."
                ),
                () -> assertEquals(
                        1,
                        mockView.performanceOutputs.size(),
                        "Exactly one performance should be displayed."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.get(0).contains("Concert"),
                        "The displayed performance should belong to the expected event."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message -> message.contains("Event Details")),
                        "Event details header should be shown."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message -> message.contains("Event ID:")),
                        "Event ID should be shown in the event details."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message -> message.contains("4.50")),
                        "The event average rating should be shown correctly."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message -> message.contains("Amazing")),
                        "Review comments should be shown."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message -> message.contains("Coool")),
                        "All stored reviews should be shown."
                )
        );
    }

    // If the selected performance exists but its event is missing, an error should be shown.
    @Test
    void eventNotFoundShouldShowError() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        setupEventAndPerformance();

        Field eventsField = EventPerformanceController.class.getDeclaredField("events");
        eventsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Collection<Event> events = (Collection<Event>) eventsField.get(controller);
        events.clear();

        controller.viewPerformance();

        assertTrue(
                mockView.errorMessages.contains("Associated event not found"),
                "The system should show an error if the performance exists but its event cannot be found."
        );
    }

    // If there are no reviews for the selected performance, the system should say so clearly.
    @Test
    void noReviewsShowNoReviewsMessage() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));

        EntertainmentProvider ep = new EntertainmentProvider(
                "ep1@ed.ac.uk",
                "epPass1",
                "OrgName",
                "BN005",
                "Alice EP",
                "description"
        );

        Event event = new Event(1, "Concert", EventType.Music, true);
        event.setOrganizer(ep);

        LocalDateTime start = LocalDateTime.now().plusDays(5).withHour(19).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);

        Performance p = event.createPerformance(
                1L,
                start,
                end,
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0
        );

        controller.addEvent(event);
        controller.addPerformance(p);

        controller.viewPerformance();

        assertTrue(
                mockView.successMessages.stream()
                        .anyMatch(message -> message.contains("No reviews were added to this event yet")),
                "When there are no reviews, the system should clearly show that no reviews exist."
        );
    }

    // Cancelled performances should still be viewable.
    @Test
    void cancelledPerformanceShouldBeDisplayable() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1", "Student Name", 1234567));
        Event event = setupEventAndPerformance();

        Performance p = event.getPerformances().iterator().next();
        p.cancel();

        controller.viewPerformance();

        assertAll(
                () -> assertFalse(
                        mockView.performanceOutputs.isEmpty(),
                        "A cancelled performance should still be displayed."
                ),
                () -> assertTrue(
                        mockView.performanceOutputs.get(0).contains("Concert"),
                        "The cancelled performance output should still contain the event title."
                )
        );
    }
}