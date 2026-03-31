package systemTest;

import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.Student;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class editPreferenceSystemTest {
    private UserController userController;
    private MockView mockView;
    private Student student;

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
    void setup(){
        student = new Student("s123", "123", "yirou", 123);

    }

    @Test
    void editPreferencesSucceedsWithValidPreferences() throws Exception {
        mockView = new MockView("movie");
        userController = new UserController(student, mockView);

        userController.editPreferences();

        assertTrue(mockView.successMessages.contains("Preferences updated."));
        assertTrue(mockView.errorMessages.isEmpty());
    }
}
