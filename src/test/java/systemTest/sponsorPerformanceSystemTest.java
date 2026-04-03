package systemTest;

import controller.EventPerformanceController;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.AdminStaff;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class sponsorPerformanceSystemTest {

    private EventPerformanceController controller;
    private TestView view;
    private AdminStaff admin;

    private Performance ticketedPerformance;
    private Performance nonTicketedPerformance;

    // Inline mock view similar to ViewPerformanceSystemTest
    private static class TestView implements interfaces.View {
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
        public void displayListOfPerformances(java.util.Collection<String> listOfPerformanceInfo) {
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

    // help
    private boolean outputContains(String k) {
        for (String s : view.getOutputs()) {
            if (s.contains(k)) {
                return true;
            }
        }
        return false;
    }
    @BeforeEach
    void setUp() {
        view = new TestView();
        admin = new AdminStaff("admin1@uni.com", "admin", "Admin");
        controller = new EventPerformanceController(admin, view);

        // Ticketed performance
        ticketedPerformance = new Performance(
                1,
                2,
                "Concert",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue 1",
                100,
                false,
                false,
                50,
                20.0
        );

        // Non-ticketed performance
        nonTicketedPerformance = new Performance(
                2,
                2,
                "Sports",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                new ArrayList<>(),
                "Venue 2",
                100,
                true,
                false,
                0,
                0.0
        );
    }

    @Test
    void testNoPerformances() {
        controller.sponsorPerformance();
        assertTrue(outputContains("No performances available to sponsor"));
    }

    @Test
    void testInvalidPerformanceID() {
        controller.addPerformance(ticketedPerformance);

        // invalid
        view.addInput("abc");
        // non-existent
        view.addInput("5");
        view.addInput("8");
        view.addInput("0");
        view.addInput("8f6");

        // valid
        view.addInput("1");

        controller.sponsorPerformance();

        assertTrue(outputContains("Invalid input"));
        assertTrue(outputContains("Performance with given ID does not exist"));
    }

    @Test
    void testNonTicketedPerformance() {
        controller.addPerformance(nonTicketedPerformance);

        view.addInput("2");

        controller.sponsorPerformance();

        assertTrue(outputContains("is not ticketed"));
    }

    @Test
    void testNegativeSponsorshipAmount() {
        controller.addPerformance(ticketedPerformance);

        view.addInput("1");   // performance ID
        view.addInput("-5");  // invalid amount
        view.addInput("5");   // valid amount

        controller.sponsorPerformance();

        assertEquals(5, ticketedPerformance.getSponsoredAmount());
        assertEquals(15, ticketedPerformance.getFinalTicketPrice());
        assertTrue(outputContains("Invalid amount"));
    }

    @Test
    void testAmountGreaterThanTicketPrice() {
        controller.addPerformance(ticketedPerformance);

        // performance ID
        view.addInput("1");
        // invalid amount
        view.addInput("100");
        // valid amount
        view.addInput("10");

        controller.sponsorPerformance();

        assertEquals(10, ticketedPerformance.getSponsoredAmount());
        assertTrue(outputContains("Invalid amount"));
    }

    @Test
    void testSuccessfulSponsorship() {
        controller.addPerformance(ticketedPerformance);

        view.addInput("1");
        view.addInput("3");

        controller.sponsorPerformance();

        assertTrue(ticketedPerformance.isSponsored());
        assertEquals(3, ticketedPerformance.getSponsoredAmount());
        assertEquals(17, ticketedPerformance.getFinalTicketPrice());
        assertTrue(outputContains("Sponsorship successful"));
    }

    @Test
    void testMultipleSponsorships() {
        controller.addPerformance(ticketedPerformance);

        // first sponsorship
        view.addInput("1");
        view.addInput("5");
        controller.sponsorPerformance();

        // second sponsorship
        view.addInput("1");
        view.addInput("4");
        controller.sponsorPerformance();

        assertEquals(9, ticketedPerformance.getSponsoredAmount());
        assertEquals(11, ticketedPerformance.getFinalTicketPrice());
    }
}