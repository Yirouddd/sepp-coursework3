package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import enums.EventType;
import external.MockPaymentSystem;
import interfaces.View;
import object.Performance;
import object.Event;
import org.junit.jupiter.api.Test;
import user.Student;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class bookPerformanceSystemTest {

    private MockView mockView;
    MockPaymentSystem paymentSystem = new MockPaymentSystem();

    EventPerformanceController eventPerformanceController =
            new EventPerformanceController(mockView, paymentSystem);

    private class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> bookingMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input) {
            inputs.addAll(Arrays.asList(input));
        }

        @Override
        public String getInput(String prompt) {
            return inputs.remove();
        }

        @Override
        public void displaySuccess(String message) {}

        @Override
        public void displayError(String message) {
            errorMessages.add(message);
        }

        @Override
        public void displayBookingRecord(String message) {
            bookingMessages.add(message);
        }

        public void displayListOfPerformances(Collection<String> list) {}
        public void displaySpecificPerformance(String p) {}
    }

    @Test
    void bookPerformanceSuccess() {
        Event event = new Event(1, "Concert", EventType.Music, true);

        mockView = new MockView("1", "2"); // performanceId = 1, 2 tickets

        Performance performance = event.createPerformance(1,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0);

        List<Performance> performances = new ArrayList<>();
        performances.add(performance);

        Student student = new Student("olivia@mail.com", "123", "Olivia", 123);

        BookingController bookingController =
                new BookingController(mockView, paymentSystem, eventPerformanceController);

        bookingController.setCurrentUser(student);

        bookingController.bookPerformance();

        assertTrue(mockView.bookingMessages.stream()
                .anyMatch(msg -> msg.contains("Booking confirmed")));
    }
}