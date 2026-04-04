package systemTests;

import controller.EventPerformanceController;
import enums.EventType;
import interfaces.View;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import user.EntertainmentProvider;
import user.Student;
import user.StudentPreferences;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SearchForPerformancesSystemTest {

    static EventPerformanceController controller;
    static View view;

    static Student student;

    static EntertainmentProvider musicEP;
    static EntertainmentProvider theatreEP;

    static Event musicEvent;
    static Event theatreEvent;

    static Performance musicPerformanceToday;
    static Performance musicPerformanceOtherDay;
    static Performance theatrePerformanceToday;

    @BeforeAll
    static void setUp() {
        view = mock(View.class);
        controller = new EventPerformanceController(view);

        // real student
        student = new Student("student@ed.ac.uk", "password", "Test Student", 123456789);

        // real entertainment providers
        musicEP = new EntertainmentProvider(
                "music@test.com",
                "pass",
                "Music Org",
                "BN001",
                "Alice EP",
                "Music organiser"
        );

        theatreEP = new EntertainmentProvider(
                "theatre@test.com",
                "pass",
                "Theatre Org",
                "BN002",
                "Bob EP",
                "Theatre organiser"
        );

        // real events
        musicEvent = new Event(1L, "Jazz Night", EventType.Music, true);
        theatreEvent = new Event(2L, "Hamlet", EventType.Theatre, true);

        musicEvent.setOrganizer(musicEP);
        theatreEvent.setOrganizer(theatreEP);

        controller.addEvent(musicEvent);
        controller.addEvent(theatreEvent);

        // real performances
        musicPerformanceToday = musicEvent.createPerformance(
                101L,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band A"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0
        );

        musicPerformanceOtherDay = musicEvent.createPerformance(
                102L,
                LocalDateTime.of(2026, 4, 11, 19, 0),
                LocalDateTime.of(2026, 4, 11, 21, 0),
                List.of("Band B"),
                "Main Hall",
                100,
                false,
                false,
                50,
                15.0
        );

        theatrePerformanceToday = theatreEvent.createPerformance(
                201L,
                LocalDateTime.of(2026, 4, 10, 18, 0),
                LocalDateTime.of(2026, 4, 10, 20, 0),
                List.of("Cast A"),
                "Studio Theatre",
                80,
                false,
                false,
                40,
                20.0
        );

        // add performances to controller global list
        controller.addPerformance(musicPerformanceToday);
        controller.addPerformance(musicPerformanceOtherDay);
        controller.addPerformance(theatrePerformanceToday);

        // ratings for event average
        musicPerformanceToday.review(5, "Excellent");
        musicPerformanceOtherDay.review(3, "Good");
        theatrePerformanceToday.review(2, "Average");
    }

    @BeforeEach
    void init() {
        reset(view);

        controller.setCurrentUser(student);

        StudentPreferences preferences = student.getStudentPreferences();
        preferences.preferMusicEvents = false;
        preferences.preferTheatreEvents = false;
        preferences.preferDanceEvents = false;
        preferences.preferMovieEvents = false;
        preferences.preferSportEvents = false;
        preferences.preferGameEvents = false;
    }

    @Test
    @DisplayName("Student preference matching performances are displayed first")
    void preferredPerformancesAreShownFirst() {
        student.getStudentPreferences().preferMusicEvents = true;

        when(view.getInput("Please enter a date (yyyy-MM-dd): "))
                .thenReturn("2026-04-10");

        controller.searchForPerformances();

        verify(view, never()).displayError(anyString());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(view, atLeast(3)).displaySuccess(captor.capture());

        List<String> outputs = captor.getAllValues();

        assertEquals("Performances on 2026-04-10:", outputs.get(0));

        String firstPerformanceLine = outputs.get(1);
        assertTrue(firstPerformanceLine.contains("Performance ID: 101"));
        assertTrue(firstPerformanceLine.contains("Event: Jazz Night"));
        assertTrue(firstPerformanceLine.contains("Time: 19:00 - 21:00"));
        assertTrue(firstPerformanceLine.contains("Venue: Main Hall"));
        assertTrue(firstPerformanceLine.contains("EP: " + musicEvent.getOrganiserName()));
        assertTrue(firstPerformanceLine.contains("Event average rating: 4.00"));

        String secondPerformanceLine = outputs.get(2);
        assertTrue(secondPerformanceLine.contains("Performance ID: 201"));
        assertTrue(secondPerformanceLine.contains("Event: Hamlet"));
        assertTrue(secondPerformanceLine.contains("Time: 18:00 - 20:00"));
        assertTrue(secondPerformanceLine.contains("Venue: Studio Theatre"));
        assertTrue(secondPerformanceLine.contains("EP: " + theatreEvent.getOrganiserName()));
        assertTrue(secondPerformanceLine.contains("Event average rating: 2.00"));
    }

    @Test
    @DisplayName("Incorrect date format displays error and asks again")
    void incorrectDateFormat() {
        when(view.getInput("Please enter a date (yyyy-MM-dd): "))
                .thenReturn("10/04/2026", "2026-04-10");

        controller.searchForPerformances();

        verify(view).displayError("Invalid date format. Please use yyyy-MM-dd.");
    }

    @Test
    @DisplayName("No performances on the provided date")
    void noPerformancesOnProvidedDate() {
        when(view.getInput("Please enter a date (yyyy-MM-dd): "))
                .thenReturn("2026-04-12");

        controller.searchForPerformances();

        verify(view).displayError("There are no performances on 2026-04-12.");
    }
}