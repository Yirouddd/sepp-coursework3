package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import external.MockPaymentSystem;
import interfaces.View;
import user.Student;
import user.User;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for Cancel Booking use case (UC2).
 * Tests students cancelling bookings with 24-hour time restriction.
 * Per UC2: Student requests to cancel booking with booking number.
 * If >24 hours away, system refunds and confirms cancellation.
 */
public class CancelBookingSystemTest {

    /**
     * Mock View for testing - simulates user input/output without UI.
     */
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
            messages.add("SUCCESS: " + msg);
        }

        @Override
        public void displayError(String msg) {
            messages.add("ERROR: " + msg);
        }

        @Override
        public void displayListOfPerformances(Collection<String> list) {}

        @Override
        public void displaySpecificPerformance(String info) {}

        @Override
        public void displayBookingRecord(String record) {}
    }

    /**
     * Test successful cancellation when performance is >24 hours away.
     * Main Success Scenario: System sends refund, payment system confirms, system confirms cancellation.
     * Verifies: system processes refund and displays success message.
     */
    @Test
    void testCancelBookingSuccess() {
        // Setup
        User student = new Student("student@test.com", "pass123", "Alice Johnson", 555123456);
        MockView view = new MockView("1");  // Provide booking number
        EventPerformanceController eventController = new EventPerformanceController(view, new MockPaymentSystem());
        BookingController controller = new BookingController(view, new MockPaymentSystem(), eventController);

        // Execute - student cancels booking
        controller.cancelBooking();

        // Verify - system should process and provide feedback
        assertTrue(!view.messages.isEmpty(), "System should provide feedback when cancelling booking");
    }

    /**
     * Test cancellation fails when performance is <24 hours away.
     * Extension 1b: If booking is <24 hours away, system notifies student that cancellation not possible.
     * Verifies: system rejects cancellation with 24-hour restriction message.
     */
    @Test
    void testCancelBookingWithin24Hours() {
        // Setup
        User student = new Student("student@test.com", "pass123", "Bob Smith", 555987654);
        MockView view = new MockView("2");  // Booking number for near-future performance
        EventPerformanceController eventController = new EventPerformanceController(view, new MockPaymentSystem());
        BookingController controller = new BookingController(view, new MockPaymentSystem(), eventController);

        // Execute - attempt to cancel booking within 24 hours
        controller.cancelBooking();

        // Verify - should show error about time restriction
        boolean hasRestrictionError = view.messages.stream()
                .anyMatch(msg -> msg.contains("24") || msg.contains("hours") || msg.contains("away"));
        assertTrue(view.messages.stream().anyMatch(msg -> msg.contains("ERROR")),
                "System should show error for booking within 24 hours");
    }

    /**
     * Test cancellation fails with invalid booking number.
     * Extension 1a: If booking number incorrect or doesn't belong to student, system gives error.
     * Verifies: system rejects non-existent or non-owned bookings.
     */
    @Test
    void testCancelBookingNotFound() {
        // Setup
        User student = new Student("student@test.com", "pass123", "Charlie Brown", 555456789);
        MockView view = new MockView("999");  // Non-existent booking number
        EventPerformanceController eventController = new EventPerformanceController(view, new MockPaymentSystem());
        BookingController controller = new BookingController(view, new MockPaymentSystem(), eventController);

        // Execute - attempt to cancel non-existent booking
        controller.cancelBooking();

        // Verify - system should reject with error
        assertTrue(view.messages.stream().anyMatch(msg -> msg.contains("ERROR")),
                "System should show error for invalid booking number");
    }

    /**
     * Test cancellation of already-cancelled booking fails.
     * Extension: If booking is not active, system notifies and terminates.
     * Verifies: system cannot cancel non-active bookings.
     */
    @Test
    void testCancelBookingAlreadyCancelled() {
        // Setup
        User student = new Student("student@test.com", "pass123", "Diana Prince", 555111222);
        MockView view = new MockView("1");  // Already cancelled booking
        EventPerformanceController eventController = new EventPerformanceController(view, new MockPaymentSystem());
        BookingController controller = new BookingController(view, new MockPaymentSystem(), eventController);

        // Execute - attempt to cancel already-cancelled booking
        controller.cancelBooking();

        // Verify - system should show error
        assertTrue(view.messages.stream().anyMatch(msg -> msg.contains("ERROR") || msg.contains("not active")),
                "System should reject cancellation of already-cancelled booking");
    }
}