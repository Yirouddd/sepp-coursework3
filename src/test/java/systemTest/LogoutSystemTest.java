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

/**
 * System tests for logout use case.
 * Covers successful logout and attempting to logout when not logged in.
 */
public class LogoutSystemTest {

    private UserController userController;
    private MockView mockView;

    /**
     * MockView for capturing UI output.
     */
    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        /**
         * Constructs a MockView with a sequence of pre-defined inputs.
         *
         * @param input the inputs to be returned by getInput()
         */
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

        @Override
        public void displayListOfPerformances(Collection<String> list) {}

        @Override
        public void displaySpecificPerformance(String p) {}

        @Override
        public void displayBookingRecord(String b) {}
    }

    /**
     * Sets up a fresh UserController with pre-registered student
     * and injects blank MockView before each test.
     */
    @BeforeEach
    void setup() throws Exception {
        userController = new UserController(null, null, null, null);

        Student student = new Student("olivia@mail.com", "123", "olivia", 123);

        Map<String, User> users = new HashMap<>();
        users.put("olivia@mail.com", student);
        userController.setUsers(users);

        mockView = new MockView();
        injectMockView(mockView);
    }

    /**
     * Injects MockView into the controller's view
     *
     * @param view the MockView to inject
     * @throws Exception if the field cannot be accessed
     */
    private void injectMockView(MockView view) throws Exception {
        Field viewField = UserController.class.getSuperclass().getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, view);
    }

    /**
     * Retrieves the controller's currentUser
     *
     * @return the currently logged-in user, or null if nobody is logged in
     * @throws Exception if the field cannot be accessed
     */
    private User getCurrentUser() throws Exception {
        Field currentUserField = UserController.class.getSuperclass().getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        return (User) currentUserField.get(userController);
    }

    /**
     * Helper method that performs valid login and then resets the MockView
     * so logout messages are captured properly
     *
     * @throws Exception if view injection or login fails
     */
    private void correctLogIn() throws Exception {
        mockView = new MockView("olivia@mail.com", "123");
        injectMockView(mockView);
        userController.login();

        mockView = new MockView(); // reset mockView
        injectMockView(mockView);
    }

    /**
     * Tests that a successful logout clears the current user.
     */
    @Test
    void logoutSuccess_clearsCurrentUser() throws Exception {
        correctLogIn();

        userController.logout();

        assertNull(getCurrentUser(), "Current user should be null after logout");
    }

    /**
     * Tests that a successful logout displays a success message.
     */
    @Test
    void logoutSuccess_displaysSuccessMessage() throws Exception {
        correctLogIn();

        userController.logout();

        assertTrue(mockView.successMessages.stream().anyMatch(msg -> msg.toLowerCase().contains("log out successful")),
                "A logout success message should be displayed after logout");
    }

    /**
     * Tests that calling logout when no user is logged in leaves the current user as null.
     */
    @Test
    void notLoggedIn() throws Exception {
        userController.logout();

        assertNull(getCurrentUser(), "Current user should remain null when logging out without being logged in");
    }
}