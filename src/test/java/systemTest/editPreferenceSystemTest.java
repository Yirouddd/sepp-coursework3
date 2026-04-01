package systemTest;

import controller.Controller;
import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.AdminStaff;
import user.Student;
import user.User;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class editPreferenceSystemTest {
    private UserController userController;
    private MockView mockView;

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
    @BeforeEach
    void setup() throws Exception{
        userController = new UserController(
                "src/resources/student.txt",
                "src/resources/admins.txt"
        );
    }
    private void injectMockView(MockView mockView) throws Exception {
        Field viewField = UserController.class.getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, mockView);
    }

    private void setCurrentUser(Object user) throws Exception {
        Field currentUserField = Controller.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(userController, user);
    }

    @Test
    void editPreferencesSucceedsWithValidPreferences() throws Exception {
        MockView mockView = new MockView("movie");
        injectMockView(mockView);
        setCurrentUser(new Student("dyr@mail.com", "123", "yirou", 123));

        userController.editPreferences();

        assertTrue(mockView.successMessages.contains("Preferences updated."));
        assertTrue(mockView.errorMessages.isEmpty());
    }

    @Test
    void editPreferencesSucceedsWithThreeValidPreferences() throws Exception {
        mockView = new MockView("music,theatre,dance");
        injectMockView(mockView);
        setCurrentUser(new Student("dyr@mail.com", "123", "yirou", 123));

        userController.editPreferences();

        assertTrue(mockView.successMessages.contains("Preferences updated."));
        assertTrue(mockView.errorMessages.isEmpty());
    }

    @Test
    void editPreferencesRetriesWhenMoreThanThreePreferencesAreEntered() throws Exception {
        mockView = new MockView("music,theatre,dance,movie", "movie");
        injectMockView(mockView);
        setCurrentUser(new Student("dyr@mail.com", "123", "yirou", 123));

        userController.editPreferences();

        assertTrue(mockView.errorMessages.contains("You can select up to 3 preferences. Please try again."));
        assertTrue(mockView.successMessages.contains("Preferences updated."));
    }

    @Test
    void editPreferencesRetriesWhenPreferenceIsInvalid() throws Exception {
        mockView = new MockView("reading", "movie");
        injectMockView(mockView);
        setCurrentUser(new Student("dyr@mail.com", "123", "yirou", 123));

        userController.editPreferences();

        assertTrue(mockView.errorMessages.contains("Invalid preference: reading. Please try again."));
        assertTrue(mockView.successMessages.contains("Preferences updated."));
    }

    @Test
    void editPreferencesRetriesWhenPreferenceIsDuplicated() throws Exception {
        mockView = new MockView("movie,movie", "movie");
        injectMockView(mockView);
        setCurrentUser(new Student("dyr@mail.com", "123", "yirou", 123));

        userController.editPreferences();

        assertTrue(mockView.errorMessages.contains("Duplicate preference: movie. Please try again"));
        assertTrue(mockView.successMessages.contains("Preferences updated."));
    }

    @Test
    void editPreferencesFailsWhenCurrentUserIsNotStudent() throws Exception {
        mockView = new MockView("movie");
        injectMockView(mockView);
        setCurrentUser(new AdminStaff("admin@mail.com", "123", "admin"));

        userController.editPreferences();

        assertTrue(mockView.errorMessages.contains("Only students can edit preferences"));
    }

}
