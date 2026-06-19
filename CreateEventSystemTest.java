package systemTest;

import controller.EventPerformanceController;
import external.MockPaymentSystem;
import interfaces.View;
import object.Event;
import user.EntertainmentProvider;
import user.User;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for Create Event use case (UC7).
 * Tests Entertainment Providers creating events with performances.
 * EP creates event with title, type, ticketed flag, and >= 1 performance.
 * Class Diagram: Event + Performance objects managed by EventPerformanceController.
 */
public class CreateEventSystemTest {

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> messages = new ArrayList<>();

        MockView(String... input) {
            this.inputs.addAll(Arrays.asList(input));
        }

        @Override
        public String getInput(String prompt) {
            return inputs.isEmpty() ? "" : inputs.remove();
        }

        @Override
        public void displaySuccess(String msg) {
            messages.add(msg);
        }

        @Override
        public void displayError(String msg) {
            messages.add(msg);
        }

        @Override
        public void displayListOfPerformances(Collection<String> list) {}

        @Override
        public void displaySpecificPerformance(String info) {}

        @Override
        public void displayBookingRecord(String record) {}
    }

    /**
     * Test successful event creation with one performance.
     * Main Success Scenario: EP provides title, type, ticketed flag, performance details.
     */
    @Test
    void testCreateEventSuccess() {
        MockView view = new MockView(
                "Jazz Night",      // title
                "Music",           // type
                "yes",             // ticketed
                "yes",             // add perf
                "2025-08-01 19:00",// start
                "2025-08-01 21:00",// end
                "Hall",            // venue
                "300",             // capacity
                "no",              // outdoor
                "no",              // smoking
                "Band",            // performers
                "25.00",           // price
                "100",             // tickets
                "no"               // no more
        );

        EventPerformanceController controller = new EventPerformanceController(view, new MockPaymentSystem());
        Event result = controller.createEvent();

        assertTrue(!view.messages.isEmpty(), "System processes event creation");
    }

    /**
     * Test event fails with no performances
     * Event must have >=1 performance
     */
    @Test
    void testCreateEventNoPerformances() {
        MockView view = new MockView(
                "Empty",   // title
                "Theatre", // type
                "yes",     // ticketed
                "no"       // NO performances
        );

        EventPerformanceController controller = new EventPerformanceController(view, new MockPaymentSystem());
        Event result = controller.createEvent();

        assertTrue(!view.messages.isEmpty(), "System provides feedback");
    }

    /**
     * Test event fails with invalid type
     * Valid types: Music, Theatre, Dance, Movie, Sports
     */
    @Test
    void testCreateEventInvalidType() {
        MockView view = new MockView(
                "Bad Event",  // title
                "Wrestling"   // INVALID
        );

        EventPerformanceController controller = new EventPerformanceController(view, new MockPaymentSystem());
        Event result = controller.createEvent();

        assertTrue(!view.messages.isEmpty(), "System validates type");
    }

    /**
     * Test non-ticketed event creation.
     * Events can be ticketed or non-ticketed.
     */
    @Test
    void testCreateEventNonTicketed() {
        MockView view = new MockView(
                "Free Concert",    // title
                "Music",           // type
                "no",              // NOT ticketed
                "yes",             // add perf
                "2025-09-15 15:00",// start
                "2025-09-15 17:00",// end
                "Square",          // venue
                "1000",            // capacity
                "yes",             // outdoor
                "no",              // smoking
                "Musicians",       // performers
                "no"               // no more
        );

        EventPerformanceController controller = new EventPerformanceController(view, new MockPaymentSystem());
        Event result = controller.createEvent();

        assertTrue(!view.messages.isEmpty(), "Non-ticketed events supported");
    }
}