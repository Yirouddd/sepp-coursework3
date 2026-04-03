package test;

import controller.BookingController;
import interfaces.View;
import object.Booking;
import object.Performance;
import enums.BookingStatus;
import user.Student;
import user.User;
import test.MockView;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ReviewPerformanceSystemTest {

    @Test
    public void testReviewPerformanceSuccess() {
        // Setup
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();

        // Create PAST performance (already happened)
        Performance perf = new Performance(
                1L,
                LocalDateTime.now().minusHours(2),  // Started 2 hours ago
                LocalDateTime.now().minusHours(1),  // Ended 1 hour ago
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances);

        // Simulate user inputs
        mockView.addInput("1");           // performance ID
        mockView.addInput("5");           // rating (1-5)
        mockView.addInput("Great show!"); // comment

        // Execute
        controller.reviewPerformance();

        // Assert - check for success message
        String output = mockView.getOutput();
        assertTrue(output.contains("success") || output.contains("Success"));
    }

    @Test
    public void testReviewPerformanceFuturePerformance() {
        // Can't review future performance
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();

        // Create FUTURE performance
        Performance perf = new Performance(
                1L,
                LocalDateTime.now().plusHours(2),   // Starts in 2 hours
                LocalDateTime.now().plusHours(3),
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances);

        mockView.addInput("1");
        controller.reviewPerformance();

        String output = mockView.getOutput();
        assertTrue(output.contains("past performances") || output.contains("error"));
    }

    @Test
    public void testReviewPerformanceInvalidRating() {
        User student = new Student("student@test.com", "password", "John", 123456789);
        MockView mockView = new MockView();
        List<Performance> performances = new ArrayList<>();

        Performance perf = new Performance(
                1L,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1),
                new ArrayList<>(),
                "Concert Hall",
                500
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances);

        mockView.addInput("1");   // performance ID
        mockView.addInput("10");  // Invalid rating (>5)

        controller.reviewPerformance();

        String output = mockView.getOutput();
        assertTrue(output.contains("between 1 and 5") || output.contains("error"));
    }
}