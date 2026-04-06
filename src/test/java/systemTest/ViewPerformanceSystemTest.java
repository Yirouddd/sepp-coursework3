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
import user.EntertainmentProvider;
import user.Student;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for the view performance use case.
 * <p>
 * Uses a mock view implementation to simulate user input and obtain output.
 */

public class ViewPerformanceSystemTest {

    private EventPerformanceController controller;
    private MockView mockView;
    private PaymentSystem paymentSystem;

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> performanceOutputs = new ArrayList<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input){
            this.inputs.addAll(Arrays.asList(input));
        }

        /**
         * Returns empty string when no input is available to simulate
         * pressing enter by user.
         */
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
        EntertainmentProvider ep = new EntertainmentProvider("ep@ed.ac.uk",
                "passwordEP", "OrgName", "BN001", "Alice EP", "description");
        Event event = new Event(1L, "Concert", EventType.Music, true);
        event.setOrganizer(ep);

        Performance p1 = event.createPerformance(1L,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0);

        Performance p2 = event.createPerformance(2L,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 22, 0),
                List.of("Band B"),
                "Main Hall",
                100,
                false,
                false,
                50,
                25.0);

        p1.review(5, "Amazing");
        p2.review(4, "Coool");

        controller.addEvent(event);
        controller.addPerformance(p1);
        controller.addPerformance(p2);

        return event;
    }

    /**
     * Verifies that an appropriate error message is displayed when no
     * performances exist.
     */
    @Test
    void noPerformancesShouldShowError() {
        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("No performances " +
                "available."));
    }

    /**
     * Tests invalid input handling of non-numeric (string) performance ID
     * input.
     * Verifies that a non-numeric input triggers a display of an error
     * message, and that a valid input succeeds and displays the performance.
     */
    @Test
    void testInvalidInputHandling() throws Exception{
        mockView = new MockView("ab", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("Invalid input. Please " +
                "enter a number."));
        assertFalse(mockView.performanceOutputs.isEmpty());
        assertEquals(1, mockView.performanceOutputs.size());
        assertTrue(mockView.performanceOutputs.get(0).contains("Concert"));
    }

    /**
     * Tests non-existent performance ID followed by a valid one.
     * Verifies that the valid performance is applied and an appropriate
     * error message regarding non-existent ID is shown.
     */
    @Test
    void testInvalidPerformanceIDThenValid() throws Exception {
        mockView = new MockView("10", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("Invalid ID. Please try again."));
        assertFalse(mockView.performanceOutputs.isEmpty());
        assertEquals(1, mockView.performanceOutputs.size());
        assertTrue(mockView.performanceOutputs.get(0).contains("Concert"));
    }

    /**
     * Tests handling of exceeding maximum number of invalid attempts.
     * Verifies that the process of viewing performance finishes with an
     * appropriate error.
     */
    @Test
    void shouldFailAfterManyAttempts() throws Exception {
        mockView = new MockView("8", "9", "3", "4", "5", "6", "7", "8");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("Too many unsuccessful " +
                "attempts were made"));
    }

    /**
     * Tests handling of an empty input for performance ID.
     * Verifies that the process is done as for invalid input and
     * appropriate error message is shown.
     */
    @Test
    void emptyInputShouldBeAsInvalid() throws Exception {
        mockView = new MockView("", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("Invalid input")));
        assertFalse(mockView.performanceOutputs.isEmpty());
        assertEquals(1, mockView.performanceOutputs.size());
        assertTrue(mockView.performanceOutputs.get(0).contains("Concert"));
    }

    /**
     * Tests full successful display scenario with a few invalid and
     * non-existent input attempts.
     * Verifies that the process does not stop at the invalid input and
     * performance and event details are added, as well as ratings and reviews.
     */
    @Test
    void displaysFullDetailsSuccessfullyAfterInvalidInputs() throws Exception {
        mockView = new MockView("a", "9", "51", "1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        setupEventAndPerformance();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("Invalid input. Please " +
                "enter a number."));
        assertTrue(mockView.errorMessages.contains("Invalid ID. Please try again."));

        // check presence of performance details
        assertFalse(mockView.performanceOutputs.isEmpty());
        assertEquals(1, mockView.performanceOutputs.size());
        assertTrue(mockView.performanceOutputs.get(0).contains("Concert"));

        // check presence of event details header
        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains(
                "Event Details")));
        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains(
                "Event ID:")));

        // check average rating ((5+4)/2=4.50)
        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains("4.50")));

        // check reviews
        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains("Amazing")));
        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains("Coool")));
    }

    /**
     * Tests handling when event is not found.
     * Verifies that the process terminates with an appropriate error if
     * event was not found.
     */
    @Test
    void eventNotFoundShouldShowError() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        Event event = setupEventAndPerformance();

        // remove event
        event.getPerformances().clear();
        Field eventsField = EventPerformanceController.class.getDeclaredField("events");
        eventsField.setAccessible(true);
        Collection<Event> events = (Collection<Event>) eventsField.get(controller);
        events.clear();

        controller.viewPerformance();

        assertTrue(mockView.errorMessages.contains("Associated event not found"));
    }

    /**
     * Tests handling of when no reviews were added to the performance.
     * Verifies that the appropriate message that no reviews were found is
     * displayed.
     */
    @Test
    void noReviewsShowNoReviewsMessage() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));

        EntertainmentProvider ep = new EntertainmentProvider("ep1@ed.ac.uk",
                "epPass1", "OrgName", "BN005", "Alice EP", "description");

        Event event = new Event(1, "Concert", EventType.Music, true);
        event.setOrganizer(ep);

        Performance p = event.createPerformance(1L,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0);

        controller.addEvent(event);
        controller.addPerformance(p);

        controller.viewPerformance();

        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains("No reviews were added to this event yet")));
    }

    /**
     * Tests handling of viewing the details of cancelled performance.
     * Verifies that cancelled performances are also displayable.
     */
    @Test
    void cancelledPerformanceShouldBeDisplayable() throws Exception {
        mockView = new MockView("1");
        controller = new EventPerformanceController(mockView, paymentSystem);

        setCurrentUser(new Student("student1@ed.ac.uk", "passwordS1",
                "Student Name", 1234567));
        Event event = setupEventAndPerformance();

        Performance p = event.getPerformances().iterator().next();
        p.cancel();

        controller.viewPerformance();

        assertTrue(mockView.performanceOutputs.get(0).contains("Concert"));
    }
}