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
import org.junit.jupiter.api.Test;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class SponsorPerformanceSystemTest {

    private EventPerformanceController controller;
    private MockView mockView;

    private Performance ticketedPerformance;

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input) {
            this.inputs.addAll(Arrays.asList(input));
        }

        // If no input is left, behave like the user stopped giving input.
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
        Field viewField = Controller.class.getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(controller, mockView);
    }

    // Prepare one ticketed and one non-ticketed performance.
    private void setupPerformances() {
        EntertainmentProvider ep = new EntertainmentProvider(
                "ep1@ed.ac.uk",
                "passwordEP",
                "Org",
                "BN001",
                "EP Name",
                "description"
        );

        Event ticketedEvent = new Event(1, "Concert", EventType.Music, true);
        ticketedEvent.setOrganizer(ep);

        LocalDateTime ticketedStart = LocalDateTime.now().plusDays(5).withHour(19).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime ticketedEnd = ticketedStart.plusHours(2);

        ticketedPerformance = ticketedEvent.createPerformance(
                1,
                ticketedStart,
                ticketedEnd,
                List.of("Band A"),
                "Venue 1",
                100,
                false,
                false,
                50,
                15.0
        );

        Event nonTicketedEvent = new Event(2, "Sports", EventType.Sports, false);
        nonTicketedEvent.setOrganizer(ep);

        LocalDateTime nonTicketedStart = LocalDateTime.now().plusDays(6).withHour(19).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime nonTicketedEnd = nonTicketedStart.plusHours(3);

        Performance nonTicketedPerformance = nonTicketedEvent.createPerformance(
                2L,
                nonTicketedStart,
                nonTicketedEnd,
                List.of("Football game"),
                "Venue 2",
                100,
                false,
                false,
                50,
                0.0
        );

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

        AdminStaff admin = new AdminStaff("admin1@ed.ac.uk", "adminPass", "Admin Admin");
        setCurrentUser(admin);
    }

    // Only admin staff should be able to sponsor performances.
    @Test
    void testNotAdmin() throws Exception {
        setCurrentUser(new Student("stud@ed.ac.uk", "studentPassword", "Student Name", 123));

        controller.sponsorPerformance();

        assertTrue(
                mockView.errorMessages.contains("Only admin staff can sponsor performances."),
                "A non-admin user should not be allowed to sponsor a performance."
        );
    }

    // If there are no performances in the system, sponsorship should not continue.
    @Test
    void testNoPerformances() {
        controller.sponsorPerformance();

        assertTrue(
                mockView.errorMessages.contains("No performances available to sponsor."),
                "The system should show an error when there are no performances to sponsor."
        );
    }

    // Invalid performance ID input should be handled until a valid ID is given.
    @Test
    void testInvalidPerformanceIDThenValid() throws Exception {
        setupPerformances();

        mockView = new MockView("abc", "1000", "1", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Invalid input. Please reenter a performance ID.")),
                        "A non-numeric performance ID should show an invalid input error."
                ),
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Performance with given ID does not exist")),
                        "A non-existent performance ID should show the correct error."
                ),
                () -> assertEquals(
                        5,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After the valid retry, the sponsorship amount should be applied."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 5 from a price of 15, the final ticket price should become 10."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(m -> m.contains("Sponsorship successful")),
                        "A success message should be shown after valid sponsorship."
                )
        );
    }

    // If no input is given for the performance ID, the use case should cancel.
    @Test
    void testNoInputForPerformanceIdCancelsSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView();
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("No input provided, cancelling sponsorship."),
                        "When no performance ID input is available, sponsorship should be cancelled."
                ),
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "No sponsorship should be applied when the process is cancelled early."
                ),
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "Ticket price should remain unchanged when the process is cancelled early."
                )
        );
    }

    // Non-ticketed performances cannot be sponsored.
    @Test
    void testNonTicketedPerformance() throws Exception {
        setupPerformances();

        mockView = new MockView("2");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(
                mockView.errorMessages.stream().anyMatch(message -> message.contains("not ticketed")),
                "A non-ticketed performance should not be sponsorable."
        );
    }

    // Empty sponsorship amount is treated as invalid numeric input.
    @Test
    void testEmptySponsorshipAmount() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message -> message.contains("Invalid input")),
                        "Empty sponsorship amount should be treated as invalid input."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After retry with valid amount, sponsorship should be applied."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After retry with amount 5, the final ticket price should become 10."
                )
        );
    }

    // If no sponsorship amount is given, the process should cancel.
    @Test
    void testNoInputForAmountCancelsSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.contains("No input provided, cancelling sponsorship."),
                        "When no amount input is available, sponsorship should be cancelled."
                ),
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "No sponsorship should be applied when the amount step is cancelled."
                ),
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "Ticket price should remain unchanged when the amount step is cancelled."
                )
        );
    }

    // Negative sponsorship is not allowed.
    @Test
    void testNegativeSponsorshipAmount() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "-5", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship must be positive.")),
                        "Negative sponsorship should show the positive amount error."
                ),
                () -> assertEquals(
                        3.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After retry with amount 3, sponsorship should be applied."
                ),
                () -> assertEquals(
                        12.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 3 from a price of 15, the final ticket price should become 12."
                )
        );
    }

    // Sponsorship cannot be more than the current final ticket price.
    @Test
    void testAmountGreaterThanTicketPrice() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "100", "10");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship cannot reduce the price below zero.")),
                        "An amount larger than the current ticket price should be rejected."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After retry with amount 10, sponsorship should be applied."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 10 from 15, the final ticket price should become 5."
                )
        );
    }

    // Sponsoring exactly the current ticket price should make the performance free.
    @Test
    void testSponsorshipEqualsToTicketPriceBoundary() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "15");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "Sponsorship equal to ticket price should be fully applied."
                ),
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "The final ticket price should become zero at the exact boundary."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(msg -> msg.contains("Sponsorship successful")),
                        "A success message should be shown for exact boundary sponsorship."
                )
        );
    }

    // Zero sponsorship is not allowed.
    @Test
    void testZeroSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "0", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship must be positive.")),
                        "Zero sponsorship should show the positive amount error."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After retry with amount 5, sponsorship should be applied."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 5 from 15, the final ticket price should become 10."
                )
        );
    }

    // Non-numeric sponsorship amount should show an error and allow retry.
    @Test
    void testInvalidAmountNotNumber() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "abcd", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Invalid input. Please enter a number.")),
                        "A non-numeric sponsorship amount should show the invalid number error."
                ),
                () -> assertEquals(
                        3.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After retry with amount 3, sponsorship should be applied."
                ),
                () -> assertEquals(
                        12.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 3 from 15, the final ticket price should become 12."
                )
        );
    }

    // Several invalid amounts should not stop the process if a later valid amount is given.
    @Test
    void testInvalidAmountThenValidSponsorshipApplies() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "100", "-10", "abc", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship cannot reduce the price below zero.")),
                        "Oversponsoring should show the correct error."
                ),
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship must be positive.")),
                        "Negative sponsorship should show the positive amount error."
                ),
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Invalid input. Please enter a number.")),
                        "Non-numeric sponsorship should show the invalid number error."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After several invalid tries, the valid sponsorship amount should still be applied."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After the valid amount 5, the final ticket price should become 10."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(msg -> msg.contains("Sponsorship successful")),
                        "A success message should be shown after the valid sponsorship."
                )
        );
    }

    // Normal successful sponsorship should update the amount and final ticket price.
    @Test
    void testSuccessfulSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "3");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship successful")),
                        "A success message should be shown after valid sponsorship."
                ),
                () -> assertEquals(
                        3.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "The sponsored amount should increase by the successful sponsorship."
                ),
                () -> assertEquals(
                        12.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "The final ticket price should be reduced correctly."
                )
        );
    }

    // Full sponsorship should make the performance free.
    @Test
    void testPerformanceBecomesFreeAfterFullSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "15");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "Full sponsorship should reduce the final ticket price to zero."
                ),
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "The sponsored amount should equal the original ticket price."
                ),
                () -> assertTrue(
                        mockView.successMessages.stream().anyMatch(msg -> msg.contains("Sponsorship successful")),
                        "A success message should be shown after full sponsorship."
                )
        );
    }

    // Multiple valid sponsorships should accumulate on the same performance.
    @Test
    void testMultipleSponsorships() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "5");
        injectMockView(mockView);
        controller.sponsorPerformance();

        mockView = new MockView("1", "4");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertAll(
                () -> assertEquals(
                        9.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "Two sponsorships should be accumulated together."
                ),
                () -> assertEquals(
                        6.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "The final ticket price should reflect both sponsorships."
                )
        );
    }

    // Cancelled performances should not accept sponsorship.
    @Test
    void testCancelledPerformanceCannotBeSponsored() throws Exception {
        setupPerformances();
        ticketedPerformance.cancel();

        mockView = new MockView("1", "5");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Cancelled performances cannot be sponsored.")),
                        "A cancelled performance should not be sponsorable."
                ),
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "No sponsorship should be added to a cancelled performance."
                ),
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "The final ticket price should remain unchanged for a cancelled performance."
                )
        );
    }

    // Several sponsorships can reduce the price to zero, but not below zero.
    @Test
    void testIncrementalSponsorshipReachesZeroBoundary() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "3");
        injectMockView(mockView);
        controller.sponsorPerformance();

        mockView = new MockView("1", "5");
        injectMockView(mockView);
        controller.sponsorPerformance();

        mockView = new MockView("1", "7");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertAll(
                () -> assertEquals(
                        15.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "The accumulated sponsorship should reach the original ticket price exactly."
                ),
                () -> assertEquals(
                        0.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "The final ticket price should stop at zero."
                )
        );
    }

    // Once the current final price is 5, trying to sponsor 7 more should be rejected.
    @Test
    void testMultipleSponsorshipDoesNotGoBelowZero() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "6");
        injectMockView(mockView);
        controller.sponsorPerformance();

        mockView = new MockView("1", "4");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertAll(
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "After two valid sponsorships, the total sponsored amount should be 10."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "After sponsoring 10 from 15, the current final ticket price should be 5."
                )
        );

        mockView = new MockView("1", "7");
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertAll(
                () -> assertTrue(
                        mockView.errorMessages.stream().anyMatch(message ->
                                message.contains("Sponsorship cannot reduce the price below zero.")),
                        "Oversponsoring after previous sponsorships should still be rejected."
                ),
                () -> assertEquals(
                        10.0,
                        ticketedPerformance.getSponsoredAmount(),
                        0.0001,
                        "Rejected sponsorship should not change the accumulated sponsored amount."
                ),
                () -> assertEquals(
                        5.0,
                        ticketedPerformance.getFinalTicketPrice(),
                        0.0001,
                        "Rejected sponsorship should not change the current final ticket price."
                )
        );
    }
}