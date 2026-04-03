package systemTest;

import controller.BookingController;
import object.Performance;
import user.Student;
import user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import external.MockPaymentSystem;
import systemTest.MockView;

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
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().minusHours(2),       // startDateTime
                LocalDateTime.now().minusHours(1),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, new MockPaymentSystem());

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
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().plusHours(2),       // startDateTime
                LocalDateTime.now().plusHours(3),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, new MockPaymentSystem());

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
                1L,                                      // performanceId
                1L,                                      // eventId
                "Test Event",                            // eventTitle
                LocalDateTime.now().minusHours(2),       // startDateTime
                LocalDateTime.now().minusHours(1),       // endDateTime
                new ArrayList<>(),                       // performersNames
                "Concert Hall",                          // venueAddress
                500,                                     // venueCapacity
                false,                                   // venueIsOutdoor
                false,                                   // venueIsAllowsSmoking
                100,                                     // numTicketsTotal
                25.0                                     // ticketPrice
        );
        performances.add(perf);

        BookingController controller = new BookingController(student, mockView, performances, new MockPaymentSystem());

        mockView.addInput("1");   // performance ID
        mockView.addInput("10");  // Invalid rating (>5)

        controller.reviewPerformance();

        String output = mockView.getOutput();
        assertTrue(output.contains("between 1 and 5") || output.contains("error"));
    }
}