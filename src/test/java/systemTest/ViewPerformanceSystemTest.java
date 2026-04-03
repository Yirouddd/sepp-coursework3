package systemTest;

import controller.EventPerformanceController;
import interfaces.View;
import object.Event;
import object.Performance;
import user.Student;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ViewPerformanceSystemTest {

    private TestView view;

    private static class TestView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> outputs = new ArrayList<>();

        public void addInput(String input) {
            inputs.add(input);
        }

        public List<String> getOutputs() {
            return outputs;
        }

        @Override
        public String getInput(String inputPrompt) {
            return inputs.remove();
        }

        @Override
        public void displaySuccess(String successMessage) {
            outputs.add(successMessage);
        }

        @Override
        public void displayError(String errorMessage) {
            outputs.add(errorMessage);
        }

        @Override
        public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {
            outputs.addAll(listOfPerformanceInfo);
        }

        @Override
        public void displaySpecificPerformance(String performanceInfo) {
            outputs.add(performanceInfo);
        }

        @Override
        public void displayBookingRecord(String bookingRecord) {
            outputs.add(bookingRecord);
        }
    }

    private Event createEvent(long id, String title) {
        return new Event(
                id,
                title, null,
                true,
                "Organiser",
                "org@email.com"
        );
    }

    private Performance createPerformance(long id, long eventId, String title) {
        return new Performance(
                id,
                eventId,
                title,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue",
                100,
                false,
                false,
                50,
                20.0
        );
    }

    @Test
    void testInvalidInputHandling() {
        view = new TestView();

        EventPerformanceController controller =
                new EventPerformanceController(new Student("stud1@uni.com",
                        "pass1", "stud1", 1234),
                        view);

        Event event = createEvent(1, "Concert");
        Performance p = createPerformance(1, 1, "Concert");

        event.addPerformance(p);
        controller.addEvent(event);
        controller.addPerformance(p);

        // invalid
        view.addInput("abc");
        // valid
        view.addInput("1");

        controller.viewPerformance();

        assertTrue(view.getOutputs().contains("Invalid input. Please enter a number."));
    }

    @Test
    void testInvalidPerformanceID() {
        view = new TestView();

        EventPerformanceController controller =
                new EventPerformanceController(new Student("stud2@uni.com",
                        "pass2", "stud2", 1234567), view);

        Event event = createEvent(1, "Concert");
        Performance p = createPerformance(1, 1, "Concert");

        event.addPerformance(p);
        controller.addEvent(event);
        controller.addPerformance(p);

        // wrong ID
        view.addInput("2");
        // correct ID
        view.addInput("1");

        controller.viewPerformance();

        assertTrue(view.getOutputs().contains("Invalid ID. Please try again."));
    }

    @Test
    void testAverageRatingAcrossEvent() {
        Event event = createEvent(1, "Concert");

        Performance p1 = createPerformance(1, 1, "Concert");
        Performance p2 = createPerformance(2, 1, "Concert");

        p1.review(5, "Great");
        p2.review(3, "Ok");

        event.addPerformance(p1);
        event.addPerformance(p2);

        assertEquals(4.0, event.getAverageRatingOfPerformances());
    }

    @Test
    void testNoReviewsAverageIsZero() {
        Event event = createEvent(1, "Concert");

        Performance p = createPerformance(1, 1, "Concert");
        event.addPerformance(p);

        assertEquals(0.0, event.getAverageRatingOfPerformances());
    }

    @Test
    void testDifferentEventsRatingAverage() {
        Event event1 = createEvent(1, "Concert");
        Event event2 = createEvent(2, "Festival");

        Performance p1 = createPerformance(1, 1, "Concert");
        Performance p2 = createPerformance(2, 2, "Festival");

        p1.review(5, "Great");
        p2.review(1, "Bad");

        event1.addPerformance(p1);
        event2.addPerformance(p2);

        assertEquals(5.0, event1.getAverageRatingOfPerformances());
    }

    @Test
    void testViewPerformanceFullFlow() {

        view = new TestView();

        Student dummyUser = new Student("testemail@uni.com", "password",
                "name", 123456789);

        EventPerformanceController controller =
                new EventPerformanceController(dummyUser, view);

        // create event
        Event event = createEvent(1, "Concert");

        // create performances
        Performance p1 = createPerformance(1, 1, "Concert");
        Performance p2 = createPerformance(2, 1, "Concert");

        // add reviews
        p1.review(5, "Coool");
        p2.review(3, "Okay");

        // link to event
        event.addPerformance(p1);
        event.addPerformance(p2);

        // add to controller
        controller.addEvent(event);
        controller.addPerformance(p1);
        controller.addPerformance(p2);

        // simulate user input
        view.addInput("abc"); // invalid input
        view.addInput("999"); // invalid ID
        view.addInput("1");   // valid

        controller.viewPerformance();

        List<String> outputs = view.getOutputs();

        assertTrue(outputs.stream().anyMatch(s -> s.contains("Invalid input")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Invalid ID")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Performance Details")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Event Details")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("4.0"))); // avg rating
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Coool")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Okay")));
    }
}