package systemTest;

import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.Student;
import user.User;

import java.lang.reflect.Field;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class loginLogoutSystemTest {

    private UserController userController;
    private MockView mockView;

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input) {
            inputs.addAll(Arrays.asList(input));
        }

        @Override
        public String getInput(String prompt) {
            return inputs.remove();
        }

        @Override
        public void displaySuccess(String message) {
            successMessages.add(message);
        }

        @Override
        public void displayError(String message) {
            errorMessages.add(message);
        }

        public void displayListOfPerformances(Collection<String> list) {}
        public void displaySpecificPerformance(String p) {}
        public void displayBookingRecord(String b) {}
    }

    @BeforeEach
    void setup() throws Exception {
        userController = new UserController(null, null);

        Student student = new Student("olivia@mail.com", null, "olivia", 123);

        Field passwordField = User.class.getDeclaredField("password");
        passwordField.setAccessible(true);
        passwordField.set(student, "123");

        Map<String, User> users = new HashMap<>();
        users.put("olivia@mail.com", student);
        userController.setUsers(users);
    }

    private void injectMockView(MockView mockView) throws Exception {
        Field viewField = UserController.class.getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, mockView);
    }

    @Test
    void loginAndLogoutSuccess() throws Exception {
        mockView = new MockView(
                "olivia@mail.com",
                "123"
        );
        injectMockView(mockView);

        userController.login();

        assertTrue(mockView.successMessages.contains("Login successful."));
        assertNotNull(getCurrentUser());

        userController.logout();

        assertTrue(mockView.successMessages.contains("Logout successful."));
        assertNull(getCurrentUser());
    }

    private User getCurrentUser() throws Exception {
        Field currentUserField = userController.getClass().getSuperclass().getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        return (User) currentUserField.get(userController);
    }
}