package systemTest;

import controller.UserController;
import enums.EventType;
import external.VerificationService;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
  System tests for the student preference editing use case.
 */
public class EditPreferencesSystemTests {

    private static final String STUDENT_EMAIL = "student1@university.ac.uk";
    private static final String STUDENT_PASSWORD = "studentPass123";
    private static final String ADMIN_EMAIL = "admin1@university.ac.uk";
    private static final String ADMIN_PASSWORD = "adminPass123";

    @TempDir
    Path tempDir;

    private Path studentsFile;
    private Path adminsFile;

    private View mockView;
    private VerificationService verificationService;
    private UserController userController;

    @BeforeEach
    void setUp() throws IOException {
        studentsFile = tempDir.resolve("students.txt");
        adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(studentsFile,
                STUDENT_EMAIL + "," + STUDENT_PASSWORD + ",Yirou Student,1234567\n"
                        + "student2@university.ac.uk,pass456,Dong Student,7654321\n");

        Files.writeString(adminsFile,
                ADMIN_EMAIL + "," + ADMIN_PASSWORD + ",Amy Admin\n");

        mockView = Mockito.mock(View.class);
        verificationService = Mockito.mock(VerificationService.class);
        when(verificationService.verifyEntertainmentProvider(anyString())).thenReturn(true);

        userController = new UserController(
                mockView,
                verificationService,
                studentsFile.toString(),
                adminsFile.toString()
        );
    }

    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    private Student preregisteredStudent() {
        return (Student) userController.getUsers().get(STUDENT_EMAIL);
    }

    private AdminStaff preregisteredAdmin() {
        return (AdminStaff) userController.getUsers().get(ADMIN_EMAIL);
    }

    private EntertainmentProvider addProviderDirectly() {
        EntertainmentProvider provider = new EntertainmentProvider(
                "provider@test.com",
                "providerPass",
                "University of Hinderburgh",
                "1234567890",
                "Hinderburgh",
                "Independent events university"
        );
        userController.getUsers().put(provider.getEmail(), provider);
        return provider;
    }

    /*
      Check happy path and also confirm design requirement: new choices replace older stored
      preferences, not just added after them.
     */
    @Test
    void shouldReplaceExistingPreferencesWhenStudentProvidesValidChoices() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);

        student.getStudentPreferences().updatePreferences("movie, games");
        stubInputs("music, theatre, sports");

        userController.editPreferences();

        assertAll("A valid update should replace the student's old preference set",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Music),
                        "Music should become a preferred event type after the update."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Theatre),
                        "Theatre should become a preferred event type after the update."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Sports),
                        "Sports should become a preferred event type after the update."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Movie),
                        "Previously selected preferences should be cleared when they are not part of the new list."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Games),
                        "The update should replace the old set instead of appending to it."),
                () -> verify(mockView).displaySuccess("Preferences updated."),
                () -> verify(mockView, never()).displayError("Preferences cannot be empty.")
        );
    }

    /*
      Check that maximum allowed number of preferences is accepted when the list length
      is exactly three items.
     */
    @Test
    void shouldAcceptExactlyThreePreferences() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);
        stubInputs("music, dance, games");

        userController.editPreferences();

        assertAll("Exactly three preferences should be accepted",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Music),
                        "Music should be stored when it appears in a valid three-item list."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Dance),
                        "Dance should be stored when it appears in a valid three-item list."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Games),
                        "Games should be stored when it appears in a valid three-item list."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Theatre),
                        "Unselected event types should remain disabled."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Movie),
                        "Unselected event types should remain disabled."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Sports),
                        "Unselected event types should remain disabled."),
                () -> verify(mockView).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check that controller accepts a single valid preference and still cleanly clears any
      previous set of preferences.
     */
    @Test
    void shouldAcceptSinglePreferenceAndClearAnyPreviousSelections() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);

        student.getStudentPreferences().updatePreferences("music, theatre, sports");
        stubInputs("movie");

        userController.editPreferences();

        assertAll("A single preference should be valid and should clear the old selection",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Movie),
                        "Movie should be enabled after the one-item update."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Music),
                        "Music should be cleared because it is not in the new input."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Theatre),
                        "Theatre should be cleared because it is not in the new input."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Sports),
                        "Sports should be cleared because it is not in the new input."),
                () -> verify(mockView).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check that blank input is rejected, no partial change is saved, and user is asked again
      until a valid choice is provided.
     */
    @Test
    void shouldRejectEmptyInputThenAcceptValidRetry() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);

        student.getStudentPreferences().updatePreferences("sports");
        stubInputs("   ", "dance, movie");

        userController.editPreferences();

        assertAll("Empty input should be rejected and a later valid retry should succeed",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Dance),
                        "Dance should be enabled after the successful retry."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Movie),
                        "Movie should be enabled after the successful retry."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Sports),
                        "The final successful input should replace the old state."),
                () -> verify(mockView).displayError("Preferences cannot be empty."),
                () -> verify(mockView).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check that unsupported preference names are rejected and system continues asking until
      a fully valid list is given.
     */
    @Test
    void shouldRejectInvalidPreferenceThenAcceptValidRetry() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);
        stubInputs("music, chess", "theatre, games");

        userController.editPreferences();

        assertAll("Invalid preference names should trigger an error and preserve retry behaviour",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Theatre),
                        "Theatre should be enabled after the valid retry."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Games),
                        "Games should be enabled after the valid retry."),
                () -> assertFalse(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Music),
                        "The failed first attempt must not partially update the student's preferences."),
                () -> verify(mockView).displayError("Invalid preference: chess. Please try again."),
                () -> verify(mockView).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check two different invalid-input cases in one test: duplicates and lists longer than
      the allowed maximum.
     */
    @Test
    void shouldRejectDuplicateAndTooManyPreferencesThenAcceptValidRetry() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);
        stubInputs("music, music", "music, theatre, games, sports", "dance, movie");

        userController.editPreferences();

        assertAll("The system should keep prompting until the user enters a valid preference list",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Dance),
                        "Dance should be stored after the successful retry."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Movie),
                        "Movie should be stored after the successful retry."),
                () -> verify(mockView, atLeastOnce()).displayError("Duplicate preference: music"),
                () -> verify(mockView, atLeastOnce()).displayError("You can select up to 3 preferences. Please try again."),
                () -> verify(mockView).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check input normalisation behaviour: extra spaces should be ignored and valid preference
      names should work even when typed in mixed case.
     */
    @Test
    void shouldTreatPreferenceInputCaseInsensitivelyAndIgnoreExtraWhitespace() {
        Student student = preregisteredStudent();
        userController.setCurrentUser(student);
        stubInputs(" Music ,  THEATRE , sports ");

        userController.editPreferences();

        assertAll("Valid preferences should be recognised despite mixed case and surrounding whitespace",
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Music),
                        "Music should be recognised despite leading and trailing spaces."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Theatre),
                        "Theatre should be recognised despite upper-case input."),
                () -> assertTrue(
                        student.getStudentPreferences().matchesStudentPreference(EventType.Sports),
                        "Sports should be recognised despite mixed casing and spacing."),
                () -> verify(mockView).displaySuccess("Preferences updated."),
                () -> verify(mockView, never()).displayError("Preferences cannot be empty.")
        );
    }

    /*
      Check access control for unauthorised logged-in users. Administrators should receive a
      clear error and system must not ask them for preference input.
     */
    @Test
    void shouldRejectPreferenceEditingForAdminUsers() {
        userController.setCurrentUser(preregisteredAdmin());

        userController.editPreferences();

        assertAll("Admins must not be able to edit student preferences",
                () -> verify(mockView).displayError("Only students can edit preferences"),
                () -> verify(mockView, never()).displaySuccess("Preferences updated.")
        );
    }

    /*
      Check same access control rule for entertainment providers so the use case is shown
      to be protected consistently for multiple non-student roles.
     */
    @Test
    void shouldRejectPreferenceEditingForEntertainmentProviders() {
        userController.setCurrentUser(addProviderDirectly());

        userController.editPreferences();

        assertAll("Entertainment providers must not be able to edit student preferences",
                () -> verify(mockView).displayError("Only students can edit preferences"),
                () -> verify(mockView, never()).displaySuccess("Preferences updated.")
        );
    }
}