package systemTest;

import controller.BookingController;
import interfaces.View;
import object.Performance;
import org.junit.jupiter.api.Test;
import user.Student;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class bookPerformanceSystemTest {

    private MockView mockView;

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

        mockView = new MockView("1", "2"); // performanceId = 1, 2 tickets

        Performance performance = new Performance(
                1,                         // performanceId
                1,                      // eventId
                "Test Event",           // eventTitle
                LocalDateTime.now().plusDays(1), // future start
                LocalDateTime.now().plusDays(2), // future end
                new ArrayList<>(),      // performers
                "Test Venue",           // address
                100,                    // capacity
                false,                  // outdoor
                false,                  // smoking
                50,                     // tickets available
                10.0                    // price
        );

        List<Performance> performances = new ArrayList<>();
        performances.add(performance);

        Student student = new Student("olivia@mail.com", "123", "Olivia", 123);

        BookingController bookingController =
                new BookingController(student, mockView, performances);

        bookingController.currentUser = student;

        bookingController.bookPerformance();

        assertTrue(mockView.bookingMessages.stream()
                .anyMatch(msg -> msg.contains("Booking confirmed")));
    }
}