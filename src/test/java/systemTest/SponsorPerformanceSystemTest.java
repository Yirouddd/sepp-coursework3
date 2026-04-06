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
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for sponsor performance use case
 * <p>
 * Uses a mock view implementation to simulate user input and obtain output.
 */

public class SponsorPerformanceSystemTest {

    private EventPerformanceController controller;
    private MockView mockView;

    private Performance ticketedPerformance;

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
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
                throw new NoSuchElementException("No more inputs given.");
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
        }

        @Override
        public void displayBookingRecord(String bookingRecord) {

        }
    }

    private void setCurrentUser(Object user) throws Exception {
        Field currentUserField = Controller.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(controller, user);
    }

    private void injectMockView(MockView mockView) throws Exception {
        Field viewField = Controller.class.getDeclaredField(
                "view");
        viewField.setAccessible(true);
        viewField.set(controller, mockView);
    }

    // helper to set up events and performances.
    private void setupPerformances() {
        EntertainmentProvider ep = new user.EntertainmentProvider("ep1" +
                "@ed.ac.uk", "passwordEP", "Org",
                "BN001", "EP Name",
                "description");

        //ticketed event
        Event ticketedEvent = new Event(1, "Concert", EventType.Music,
                true);
        ticketedEvent.setOrganizer(ep);

        ticketedPerformance = ticketedEvent.createPerformance(1,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Venue 1",
                100,
                false,
                false,
                50,
                15.0);

        // non ticketed event
        Event nonTicketedEvent = new Event(2, "Sports",
                EventType.Sports, false);
        nonTicketedEvent.setOrganizer(ep);

        Performance nonTicketedPerformance = nonTicketedEvent.
                createPerformance(2L,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 22, 0),
                List.of("Football game"),
                "Venue 2",
                100,
                false,
                false,
                50,
                0.0);

        controller.addEvent(ticketedEvent);
        controller.addEvent(nonTicketedEvent);
        controller.addPerformance(ticketedPerformance);
        controller.addPerformance(nonTicketedPerformance);
    }

    @BeforeEach
    void setup() throws Exception {
        mockView = new MockView();
        PaymentSystem paymentSystem = new MockPaymentSystem();
        controller = new EventPerformanceController(mockView, paymentSystem);

        AdminStaff admin = new AdminStaff("admin1@ed.ac.uk",
                "adminPass",
                "Admin Admin");
        setCurrentUser(admin);
    }

    /**
     * Tests that a non-admin user cannot sponsor performance.
     * Verifies that an appropriate error message is displayed.
     */
    @Test
    void testNotAdmin() throws Exception {
        setCurrentUser(new Student("stud@ed.ac.uk",
                "studentPassword",
                "Student Name",
                123));
        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.contains("Only admin staff can " +
                "sponsor performances."));
    }

    /**
     * Tests what happens when no performances exist.
     * Verifies that an error message is shown.
     */
    @Test
    void testNoPerformances() {
        controller.sponsorPerformance();
        assertTrue(mockView.errorMessages.contains("No performances available " +
                "to sponsor."));
    }

    /**
     * Tests invalid performance ID inputs.
     * Verifies proper handling of text(non numeric) and not existed IDs
     * and ensures sponsorship is applied after valid input.
     */
    @Test
    void testInvalidPerformanceIDThenValid() throws Exception {
        setupPerformances();

        mockView = new MockView("abc", "1000", "1", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.contains("Invalid input. " +
                        "Please reenter a performance ID.")));
        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.contains("Performance with "
                        + "given ID does not exist")));
        assertEquals(5, ticketedPerformance.getSponsoredAmount());
        assertEquals(10, ticketedPerformance.getFinalTicketPrice());
        assertTrue(mockView.successMessages.stream().
                anyMatch(m -> m.
                        contains("Sponsorship successful")));
    }

    /**
     * Tests that non ticketed performances cannot be sponsored.
     * Verifies that an appropriate error message is displayed.
     */
    @Test
    void testNonTicketedPerformance() throws Exception {
        setupPerformances();

        mockView = new MockView("2");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("not ticketed")));
    }

    /**
     * Tests empty sponsorship amount input.
     * Verifies that invalid input is handled, the error is
     * shown and no changes occur.
     */
    @Test
    void testEmptySponsorshipAmount() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains(("Invalid input"))));
        assertEquals(5, ticketedPerformance.getSponsoredAmount());
        assertEquals(10, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests handling of negative sponsorship amount input.
     * Verifies that invalid input is handled accordingly and the error is
     * shown.
     */
    @Test
    void testNegativeSponsorshipAmount() throws Exception {
        setupPerformances();
        mockView = new MockView("1", "-5", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship must be positive.")));
        assertEquals(3, ticketedPerformance.getSponsoredAmount());
        assertEquals(12.0, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests sponsorship amount exceeding the actual ticket price.
     * Verifies that it is rejected and appropriate error message is shown.
     */
    @Test
    void testAmountGreaterThanTicketPrice() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "100", "10");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship cannot reduce the price below zero.")));
        assertEquals(10, ticketedPerformance.getSponsoredAmount());
        assertEquals(5.0, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests boundary case when sponsorship amount equals ticket price.
     * Verifies that final ticket price is changed to zero.
     */
    @Test
    void testSponsorshipEqualsToTicketPriceBoundary() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "15");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertEquals(15, ticketedPerformance.getSponsoredAmount());
        assertEquals(0, ticketedPerformance.getFinalTicketPrice());
        assertTrue(mockView.successMessages.stream()
                .anyMatch(msg -> msg.contains("Sponsorship successful")));
    }

    /**
     * Tests handling of zero sponsorship.
     * Verifies that it is not accepted and no changes occur.
     */
    @Test
    void testZeroSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "0", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship must be positive.")));
        assertEquals(5, ticketedPerformance.getSponsoredAmount());
        assertEquals(10.0, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests non-numeric(string) sponsorship amount input.
     * Verifies that input validation is applied.
     */
    @Test
    void testInvalidAmountNotNumber() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "abcd", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();
        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.contains("Invalid input. " +
                        "Please enter a number.")));
        assertEquals(3, ticketedPerformance.getSponsoredAmount());
        assertEquals(12.0, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests multiple invalid sponsorship invalid inputs followed by a valid
     * one.
     * Verifies that the system displays error messages and processes the valid
     * input.
     */
    @Test
    void testInvalidAmountThenValidSponsorshipApplies() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "100", "-10", "abc", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship cannot reduce the price below zero.")));
        assertEquals(5, ticketedPerformance.getSponsoredAmount());
        assertEquals(10, ticketedPerformance.getFinalTicketPrice());
        assertTrue(mockView.successMessages.stream()
                .anyMatch(msg -> msg.contains("Sponsorship successful")));
    }

    /**
     * Tests a successful sponsorship scenario.
     * Verifies correct update of sponsored amount and updated ticket price.
     */
    @Test
    void testSuccessfulSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.successMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship successful")));
        assertEquals(3, ticketedPerformance.getSponsoredAmount());
        assertEquals(12, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests a case of a successful sponsorship covering entire ticket price.
     * (sponsored amount = ticket price)
     * Verifies that the performance becomes free (ticket price is set to
     * zero) and success message is shown.
     */
    @Test
    void testPerformanceBecomesFreeAfterFullSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "15");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertEquals(0, ticketedPerformance.getFinalTicketPrice());
        assertEquals(15.0, ticketedPerformance.getSponsoredAmount());
        assertTrue(mockView.successMessages.stream()
                .anyMatch(msg -> msg.contains("Sponsorship successful")));
    }

    /**
     * Tests multiple sponsorships applied to the same performance.
     * Verifies that both sponsorships are applied to the final ticket price.
     */
    @Test
    void testMultipleSponsorships() throws Exception {
        setupPerformances();

        // first sponsorship
        mockView = new MockView("1", "5");
        injectMockView(mockView);
        controller.sponsorPerformance();

        // second sponsorship
        mockView = new MockView("1", "4");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertEquals(9, ticketedPerformance.getSponsoredAmount());
        assertEquals(6, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests that cancelled performance cannot be sponsored.
     * Verifies that sponsorship cannot be applied and shows the appropriate
     * error message and no changes occur.
     */
    @Test
    void testCancelledPerformanceCannotBeSponsored() throws Exception {
        setupPerformances();
        ticketedPerformance.cancel();

        // select valid performance ID and apply sponsorship amount of 5
        mockView = new MockView("1", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Cancelled performances cannot be sponsored.")));
        assertEquals(0, ticketedPerformance.getSponsoredAmount());
    }

    /**
     * Tests that multiple sponsorships reach zero ticket price without
     * becoming negative.
     * Verifies that multiple valid sponsorships reduce the price to zero
     * without going negative accordingly.
     */
    @Test
    void testIncrementalSponsorshipReachesZeroBoundary() throws Exception {
        setupPerformances();

        // first sponsorship
        mockView = new MockView("1", "3");
        injectMockView(mockView);
        controller.sponsorPerformance();

        // second sponsorship (reaches boundary exactly)
        mockView = new MockView("1", "5");
        injectMockView(mockView);
        controller.sponsorPerformance();

        // third sponsorship
        mockView = new MockView("1", "7");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertEquals(15, ticketedPerformance.getSponsoredAmount());
        assertEquals(0, ticketedPerformance.getFinalTicketPrice());
    }

    /**
     * Tests that multiple sponsorships reach zero ticket price without
     * becoming negative.
     * Verifies that multiple valid sponsorships cannot reduce ticket
     * price below zero.
     */
    @Test
    void testMultipleSponsorshipDoesNotGoBelowZero() throws Exception {
        setupPerformances();

        // first sponsorship
        mockView = new MockView("1", "6");
        injectMockView(mockView);
        controller.sponsorPerformance();

        // second sponsorship
        mockView = new MockView("1", "4");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertEquals(10, ticketedPerformance.getSponsoredAmount());
        assertEquals(5, ticketedPerformance.getFinalTicketPrice());

        // third sponsorship, attempt to over sponsor
        mockView = new MockView("1", "7");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().
                anyMatch(message -> message.
                        contains("Sponsorship cannot reduce the price below zero.")));
        assertEquals(10, ticketedPerformance.getSponsoredAmount());
        assertEquals(5, ticketedPerformance.getFinalTicketPrice());
    }
}