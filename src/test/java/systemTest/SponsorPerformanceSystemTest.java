package systemTest;

import controller.Controller;
import controller.EventPerformanceController;
import enums.EventType;
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

        @Override
        public String getInput(String inputPrompt) {
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

    private void setupPerformances() {
        EntertainmentProvider ep = new user.EntertainmentProvider("ep1" +
                "@ed.ac.uk", "passwordEP", "Org", "BN001", "EP Name",
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
        Event nonTicketedEvent = new Event(2, "Sports", EventType.Sports,
                false);
        nonTicketedEvent.setOrganizer(ep);

        Performance nonTicketedPerformance = nonTicketedEvent.createPerformance(2L,
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
        controller = new EventPerformanceController(mockView);

        AdminStaff admin = new AdminStaff("admin1@ed.ac.uk", "adminPass",
                "Admin Admin");
        setCurrentUser(admin);
    }

    @Test
    void testNotAdmin() throws Exception {
        setCurrentUser(new Student("stud@ed.ac.uk", "studentPassword",
                "Student Name", 123));
        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.contains("Only admin staff can " +
                "sponsor performances."));
    }

    @Test
    void testNoPerformances() {
        controller.sponsorPerformance();
        assertTrue(mockView.errorMessages.contains("No performances available to sponsor."));
    }

    @Test
    void testInvalidPerformanceID() throws Exception {
        setupPerformances();

        mockView = new MockView("abc", "1000", "1", "5"); // invalid, invalid,
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("Invalid input. Please reenter a performance ID.")));
        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("Performance with given ID does not exist")));
    }

    @Test
    void testNonTicketedPerformance() throws Exception {
        setupPerformances();

        mockView = new MockView("2");
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("not ticketed")));
    }

    @Test
    void testNegativeSponsorshipAmount() throws Exception {
        setupPerformances();
        mockView = new MockView("1", "-5", "5"); // performance id, invalid
        // amount, valid amount
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("Sponsorship must be positive.")));
    }

    @Test
    void testAmountGreaterThanTicketPrice() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "100", "10"); // performance id, amount
        // larger than ticket price, valid amount
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.errorMessages.stream().anyMatch(message -> message.contains("Sponsorship cannot reduce the price below zero.")));
    }

    @Test
    void testSuccessfulSponsorship() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "3"); // performance id, amount
        injectMockView(mockView);

        controller.sponsorPerformance();

        assertTrue(mockView.successMessages.stream().anyMatch(message -> message.contains("Sponsorship successful")));
        assertEquals(3, ticketedPerformance.getSponsoredAmount());
        assertEquals(12, ticketedPerformance.getFinalTicketPrice());
    }

    @Test
    void testMultipleSponsorships() throws Exception {
        setupPerformances();

        mockView = new MockView("1", "5"); // performance id, amount
        injectMockView(mockView);
        controller.sponsorPerformance();

        mockView = new MockView("1", "4"); // performance id, amount
        injectMockView(mockView);
        controller.sponsorPerformance();

        assertEquals(9, ticketedPerformance.getSponsoredAmount());
        assertEquals(6, ticketedPerformance.getFinalTicketPrice());
    }
}