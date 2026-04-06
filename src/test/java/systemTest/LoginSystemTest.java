package systemTest;

import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.EntertainmentProvider;
import user.Student;
import user.User;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for the login use case.
 * Covers successful login, failure with wrong credentials, and empty input.
 */
public class LoginSystemTest {

    private UserController userController;
    private MockView mockView;

    /**
     * MockView for UI output
     */
    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        /**
         * Constructs MockView with a sequence of pre-defined inputs.
         *
         * @param input the inputs to be returned by {@link #getInput(String)} in order
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
     * Sets up new UserController with a single pre-registered student before each test.
     */
    @BeforeEach
    void setup() throws Exception {
        userController = new UserController(null, null, null, null);

        Student student = new Student("olivia@mail.com", "123", "olivia", 123);

        Map<String, User> users = new HashMap<>();
        users.put("olivia@mail.com", student);
        userController.setUsers(users);
    }

    /**
     * Injects MockView into the controller's view.
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
     * Tests that a successful login sets the current user.
     */
    @Test
    void loginSuccess_setsCurrentUser() throws Exception {
        mockView = new MockView("olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        assertNotNull(getCurrentUser(), "Current user should be set after a successful login");
    }

    /**
     * Tests that a successful login displays a success message.
     */
    @Test
    void loginSuccess_displaysSuccessMessage() throws Exception {
        mockView = new MockView("olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        assertTrue(mockView.successMessages.contains("Login successful."),
                "Login successful message");
    }

    /**
     * Tests that entering a wrong password displays an error message.
     * A valid second attempt is provided so the login loop can exit.
     */
    @Test
    void wrongPassword_displaysError() throws Exception {
        mockView = new MockView("olivia@mail.com", "wrongpassword", "olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                "An error message should be displayed when the password is wrong");
    }

    /**
     * Tests that entering an unregistered email displays an error message.
     * A valid second attempt is provided so the login loop can exit.
     */
    @Test
    void wrongEmail_displaysError() throws Exception {
        mockView = new MockView("unknown@mail.com", "123", "olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                "An error message should be displayed when the email is not registered");
    }

    /**
     * Tests that submitting empty credentials does not log in the user.
     */
    @Test
    void loginFailure_emptyCredentials_doesNotLogin() throws Exception {
        mockView = new MockView("", "");
        injectMockView(mockView);

        userController.login();

        assertNull(getCurrentUser(), "Current user remains null when empty credentials are submitted");
    }

    /**
     * Tests that an EP can log in.
     */
    @Test
    void loginSuccess_entertainmentProvider_setsCurrentUser() throws Exception {
        EntertainmentProvider ep = new EntertainmentProvider(
                "ep@mail.com", "password", "OrgName", "BN1", "Contact", "Description");
        Map<String, User> users = new HashMap<>();
        users.put("ep@mail.com", ep);
        userController.setUsers(users);

        mockView = new MockView("ep@mail.com", "password");
        injectMockView(mockView);

        userController.login();

        assertNotNull(getCurrentUser(), "EP should be set as current user after successful login");
    }
}