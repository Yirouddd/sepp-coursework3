package systemTest;

import controller.EventPerformanceController;
import controller.UserController;
import enums.EventType;
import external.PaymentSystem;
import external.VerificationService;
import interfaces.View;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * System tests for search for performances use case.
 *
 * The comments here are kept simple on purpose.
 */
public class SearchForPerformancesSystemTests {

    private static final String STUDENT1_EMAIL = "student1@test.com";
    private static final String STUDENT1_PASSWORD = "pass1";
    private static final String STUDENT2_EMAIL = "student2@test.com";
    private static final String STUDENT2_PASSWORD = "pass2";
    private static final String ADMIN1_EMAIL = "admin1@test.com";
    private static final String ADMIN1_PASSWORD = "pass1";

    @TempDir
    Path tempDir;

    private Path studentsFile;
    private Path adminsFile;

    private View mockView;
    private VerificationService verificationService;
    private PaymentSystem paymentSystem;

    private UserController userController;
    private EventPerformanceController eventPerformanceController;

    @BeforeEach
    void setUp() throws IOException {
        studentsFile = tempDir.resolve("students.txt");
        adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(studentsFile,
                STUDENT1_EMAIL + "," + STUDENT1_PASSWORD + ",student1,712300001\n"
                        + STUDENT2_EMAIL + "," + STUDENT2_PASSWORD + ",student2,712300002\n");

        Files.writeString(adminsFile,
                ADMIN1_EMAIL + "," + ADMIN1_PASSWORD + ",admin1\n");

        mockView = mock(View.class);
        verificationService = mock(VerificationService.class);
        paymentSystem = mock(PaymentSystem.class);

        when(verificationService.verifyEntertainmentProvider(anyString())).thenReturn(true);

        userController = new UserController(
                mockView,
                verificationService,
                studentsFile.toString(),
                adminsFile.toString()
        );
        eventPerformanceController = new EventPerformanceController(mockView, paymentSystem);
    }

    /**
     * Small helper to feed many keyboard inputs.
     */
    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    /**
     * This is only for setup, so test can focus on search use case.
     */
    private EntertainmentProvider addProviderDirectly(String email, String orgName, String businessNumber) {
        EntertainmentProvider provider = new EntertainmentProvider(
                email,
                "pass1",
                orgName,
                businessNumber,
                "name1",
                "desc1"
        );
        userController.getUsers().put(email, provider);
        return provider;
    }

    /**
     * We use create event use case to make performances for search tests.
     */
    private Event createTicketedEventThroughUseCase(
            EntertainmentProvider provider,
            String eventTitle,
            EventType eventType,
            LocalDateTime start,
            LocalDateTime end
    ) {
        eventPerformanceController.setCurrentUser(provider);

        stubInputs(
                eventTitle,
                eventType.name().toLowerCase(),
                "yes",
                formatDateTime(start),
                formatDateTime(end),
                "name1, name2",
                "addr1",
                "100",
                "no",
                "no",
                "50",
                "10.0",
                "no"
        );

        Event event = eventPerformanceController.createEvent();
        assertNotNull(event, "Setup failed: expected a ticketed event to be created.");
        clearInvocations(mockView);
        return event;
    }

    /**
     * This helper is for non-ticketed event setup.
     */
    private Event createNonTicketedEventThroughUseCase(
            EntertainmentProvider provider,
            String eventTitle,
            EventType eventType,
            LocalDateTime start,
            LocalDateTime end
    ) {
        eventPerformanceController.setCurrentUser(provider);

        stubInputs(
                eventTitle,
                eventType.name().toLowerCase(),
                "no",
                formatDateTime(start),
                formatDateTime(end),
                "name1",
                "addr1",
                "80",
                "no",
                "no",
                "no"
        );

        Event event = eventPerformanceController.createEvent();
        assertNotNull(event, "Setup failed: expected a non-ticketed event to be created.");
        clearInvocations(mockView);
        return event;
    }

    private Student getStudent1() {
        return (Student) userController.getUsers().get(STUDENT1_EMAIL);
    }

    private Student getStudent2() {
        return (Student) userController.getUsers().get(STUDENT2_EMAIL);
    }

    private AdminStaff getAdmin1() {
        return (AdminStaff) userController.getUsers().get(ADMIN1_EMAIL);
    }

    private String formatDateTime(LocalDateTime value) {
        return value.withSecond(0).withNano(0).toString().replace("T", " ");
    }

    @SuppressWarnings("unchecked")
    private List<String> captureDisplayedLines() {
        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mockView).displayListOfPerformances(captor.capture());
        return List.copyOf(captor.getValue());
    }

    @Test
    void shouldAskAgainWhenDateFormatIsWrongThenShowPerformancesForThatDate() {
        // One invalid date is given first, then a correct one.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        LocalDateTime start = LocalDateTime.now().plusDays(8).withHour(19).withMinute(0).withSecond(0).withNano(0);

        createTicketedEventThroughUseCase(ep1, "event1", EventType.Music, start, start.plusHours(2));

        stubInputs("05/04/2026", start.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();

        assertAll("Search should continue after one wrong date input.",
                () -> verify(mockView, atLeastOnce()).displayError("Invalid date format. Please use yyyy-MM-dd."),
                () -> verify(mockView).displaySuccess("Performances on " + start.toLocalDate() + ":"),
                () -> verify(mockView).displayListOfPerformances(anyCollection())
        );
    }

    @Test
    void shouldShowErrorWhenNoPerformanceExistsOnRequestedDate() {
        // Search date has no performance at all.
        LocalDate dateWithoutPerformance = LocalDateTime.now().plusDays(30).toLocalDate();
        stubInputs(dateWithoutPerformance.toString());

        eventPerformanceController.searchForPerformances();

        verify(mockView).displayError("There are no performances on " + dateWithoutPerformance + ".");
    }

    @Test
    void shouldShowPreferredEventTypesBeforeOtherEventTypesForStudent() {
        // Student chooses music, so music should come before sports.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        Student student1 = getStudent1();

        userController.setCurrentUser(student1);
        stubInputs("music");
        userController.editPreferences();
        clearInvocations(mockView);

        LocalDateTime base = LocalDateTime.now().plusDays(12).withHour(14).withMinute(0).withSecond(0).withNano(0);
        Event sportsEvent = createTicketedEventThroughUseCase(ep1, "event1", EventType.Sports, base.plusHours(2), base.plusHours(4));
        Event musicEvent = createTicketedEventThroughUseCase(ep1, "event2", EventType.Music, base, base.plusHours(1));

        eventPerformanceController.setCurrentUser(student1);
        stubInputs(base.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();

        assertAll("Preferred event should be listed earlier for a student.",
                () -> assertTrue(lines.get(0).contains(musicEvent.getEventTitle()),
                        "The first result should be the preferred music event."),
                () -> assertFalse(lines.get(0).contains(sportsEvent.getEventTitle()),
                        "The non-preferred sports event should not be first.")
        );
    }

    @Test
    void shouldStillSortByTimeInsidePreferredGroupForStudent() {
        // Two preferred events should still keep time order.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        Student student1 = getStudent1();

        userController.setCurrentUser(student1);
        stubInputs("music, theatre");
        userController.editPreferences();
        clearInvocations(mockView);

        LocalDateTime day = LocalDateTime.now().plusDays(13).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Event laterPreferred = createTicketedEventThroughUseCase(ep1, "event2", EventType.Theatre, day.plusHours(3), day.plusHours(4));
        Event earlierPreferred = createTicketedEventThroughUseCase(ep1, "event1", EventType.Music, day.plusHours(1), day.plusHours(2));

        eventPerformanceController.setCurrentUser(student1);
        stubInputs(day.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();

        assertAll("Preferred performances should still be ordered by time.",
                () -> assertTrue(lines.get(0).contains(earlierPreferred.getEventTitle()),
                        "Earlier preferred event should come first."),
                () -> assertTrue(lines.get(1).contains(laterPreferred.getEventTitle()),
                        "Later preferred event should come after the earlier one.")
        );
    }

    @Test
    void shouldSortByStartTimeForAdminUser() {
        // Admin does not have preferences, so order should be by start time.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        AdminStaff admin1 = getAdmin1();
        LocalDateTime day = LocalDateTime.now().plusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0);

        Event laterEvent = createTicketedEventThroughUseCase(ep1, "event2", EventType.Sports, day.plusHours(4), day.plusHours(5));
        Event earlierEvent = createTicketedEventThroughUseCase(ep1, "event1", EventType.Music, day.plusHours(1), day.plusHours(2));

        eventPerformanceController.setCurrentUser(admin1);
        stubInputs(day.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();

        assertAll("Admin search results should follow time order only.",
                () -> assertTrue(lines.get(0).contains(earlierEvent.getEventTitle()),
                        "Earlier event should be first for admin."),
                () -> assertTrue(lines.get(1).contains(laterEvent.getEventTitle()),
                        "Later event should be second for admin.")
        );
    }

    @Test
    void shouldSortByStartTimeForStudentWhenNoPreferenceMatches() {
        // Student likes games, but music and sports are searched here.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        Student student2 = getStudent2();

        userController.setCurrentUser(student2);
        stubInputs("games");
        userController.editPreferences();
        clearInvocations(mockView);

        LocalDateTime day = LocalDateTime.now().plusDays(15).withHour(11).withMinute(0).withSecond(0).withNano(0);
        Event laterEvent = createTicketedEventThroughUseCase(ep1, "event2", EventType.Sports, day.plusHours(5), day.plusHours(6));
        Event earlierEvent = createTicketedEventThroughUseCase(ep1, "event1", EventType.Music, day.plusHours(2), day.plusHours(3));

        eventPerformanceController.setCurrentUser(student2);
        stubInputs(day.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();

        assertAll("If no event matches student preferences, normal time order should be used.",
                () -> assertTrue(lines.get(0).contains(earlierEvent.getEventTitle()),
                        "Earlier event should be first when nothing matches preferences."),
                () -> assertTrue(lines.get(1).contains(laterEvent.getEventTitle()),
                        "Later event should be second when nothing matches preferences.")
        );
    }

    @Test
    void shouldNotDisplayCancelledPerformanceEvenIfItIsOnRequestedDate() {
        // Cancelled performance should be skipped by search.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        LocalDateTime day = LocalDateTime.now().plusDays(16).withHour(16).withMinute(0).withSecond(0).withNano(0);

        Event activeEvent = createTicketedEventThroughUseCase(ep1, "event1", EventType.Music, day.plusHours(1), day.plusHours(2));
        Event cancelledEvent = createTicketedEventThroughUseCase(ep1, "event2", EventType.Theatre, day.plusHours(3), day.plusHours(4));
        Performance cancelledPerformance = cancelledEvent.getPerformances().get(0);
        cancelledPerformance.cancel();

        stubInputs(day.toLocalDate().toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();

        assertAll("Cancelled performance should not be shown in results.",
                () -> assertEquals(1, lines.size(), "Only one active performance should be shown."),
                () -> assertTrue(lines.get(0).contains(activeEvent.getEventTitle()),
                        "The active event should still be shown."),
                () -> assertFalse(lines.get(0).contains(cancelledEvent.getEventTitle()),
                        "The cancelled event should not appear in search results.")
        );
    }

    @Test
    void shouldFindPerformanceThatSpansAcrossMidnightOnSecondDate() {
        // Search logic checks start date and end date, so cross-day performance should appear too.
        EntertainmentProvider ep1 = addProviderDirectly("ep1@test.com", "org1", "bn1");
        LocalDateTime start = LocalDateTime.now().plusDays(17).withHour(23).withMinute(0).withSecond(0).withNano(0);
        LocalDate secondDate = start.plusHours(2).toLocalDate();

        Event event = createNonTicketedEventThroughUseCase(ep1, "event1", EventType.Movie, start, start.plusHours(2));

        stubInputs(secondDate.toString());

        eventPerformanceController.searchForPerformances();
        List<String> lines = captureDisplayedLines();
        Iterator<String> iterator = lines.iterator();
        String firstLine = iterator.next();

        assertAll("A performance crossing midnight should be found on the next date too.",
                () -> verify(mockView).displaySuccess("Performances on " + secondDate + ":"),
                () -> assertEquals(1, lines.size(), "Only one matching performance should be listed."),
                () -> assertTrue(firstLine.contains(event.getEventTitle()),
                        "The overnight event should appear in the results.")
        );
    }
}
