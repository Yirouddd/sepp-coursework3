package systemTest;

import controller.EventPerformanceController;
import object.Performance;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.TestView;
import user.User;

import static org.junit.jupiter.api.Assertions.*;

public class ViewPerformanceUseCaseTest {

    private Performance createPerformance(long id, String eventTitle) {
        return new Performance(
                id,
                eventTitle,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue",
                100,
                50,
                20.0
        );
    }

    @Test
    void testInvalidInputHandling() {
        TestView view = new TestView();
        EventPerformanceController controller =
                new EventPerformanceController(new User("a","b") {}, view);

        Performance p = createPerformance(1, "Concert");
        controller.addPerformance(p);

        view.addInput("abc");
        view.addInput("1");

        controller.viewPerformance();

        assertTrue(view.getOutputs().stream()
                .anyMatch(s -> s.contains("Invalid input")));
    }

    @Test
    void testInvalidPerformanceID() {
        TestView view = new TestView();
        EventPerformanceController controller =
                new EventPerformanceController(new User("a","b") {}, view);

        Performance p = createPerformance(1, "Concert");
        controller.addPerformance(p);

        view.addInput("999");
        view.addInput("1");

        controller.viewPerformance();

        assertTrue(view.getOutputs().stream()
                .anyMatch(s -> s.contains("Invalid ID")));
    }

    @Test
    void testAverageRatingAcrossEvent() {
        EventPerformanceController controller =
                new EventPerformanceController(new User("a","b") {}, new TestView());

        Performance p1 = createPerformance(1, "Concert");
        Performance p2 = createPerformance(2, "Concert");

        p1.review(5, "Great");
        p2.review(3, "Ok");

        controller.addPerformance(p1);
        controller.addPerformance(p2);

        double avg = controller.getAverageRatingForEvent("Concert");

        assertEquals(4.0, avg);
    }

    @Test
    void testNoReviewsAverageIsZero() {
        EventPerformanceController controller =
                new EventPerformanceController(new User("a","b") {}, new TestView());

        Performance p = createPerformance(1, "Concert");
        controller.addPerformance(p);

        double avg = controller.getAverageRatingForEvent("Concert");

        assertEquals(0.0, avg);
    }

    @Test
    void testDifferentEventsAverage() {
        EventPerformanceController controller =
                new EventPerformanceController(new User("a","b") {}, new TestView());

        Performance p1 = createPerformance(1, "Concert");
        Performance p2 = createPerformance(2, "Festival");

        p1.review(5, "Great");
        p2.review(1, "Bad");

        controller.addPerformance(p1);
        controller.addPerformance(p2);

        double avg = controller.getAverageRatingForEvent("Concert");

        assertEquals(5.0, avg);
    }

    @Test
    void testViewPerformanceFullFlow() {

        TestView view = new TestView();

        User dummyUser = new User("testemail@uni.com", "password") {};

        EventPerformanceController controller =
                new EventPerformanceController(dummyUser, view);

        // create performances
        Performance p1 = new Performance(
                1, "Concert",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue", 100, 50, 20.0
        );

        Performance p2 = new Performance(
                2, "Concert",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue", 100, 50, 20.0
        );

        p1.review(5, "Coool");
        p2.review(3, "Okay");

        controller.addPerformance(p1);
        controller.addPerformance(p2);

        // simulate user input for performance id
        view.addInput("abc"); //performance ID can only be a number
        view.addInput("999"); // no performance with such ID exists
        view.addInput("1"); // valid performance ID

        controller.viewPerformance();

        List<String> outputs = view.getOutputs();

        System.out.println(outputs);

        assertTrue(outputs.stream().anyMatch(s -> s.contains("Invalid input")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Invalid ID")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Performance")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("4.0"))); // avg
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Coool")));
        assertTrue(outputs.stream().anyMatch(s -> s.contains("Okay")));
    }
}