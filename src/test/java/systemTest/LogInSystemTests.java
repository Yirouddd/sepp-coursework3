package systemTest;

import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;
import user.User;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LogInSystemTests {

    private UserController userController;
    private MockView mockView;

    // Simple fake view used to simulate user input and collect output messages.
    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        // Inputs are returned one by one in the same order as given here.
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

    @BeforeEach
    void setup() throws Exception {
        userController = new UserController(null, null, null, null);

        // Prepare one account for each user type.
        Student student = new Student("olivia@mail.com", "123", "olivia", 123);
        AdminStaff admin = new AdminStaff("admin@mail.com", "admin123", "admin1");
        EntertainmentProvider ep = new EntertainmentProvider(
                "ep@mail.com", "password", "OrgName", "BN1", "Contact", "Description");

        // Put all test accounts into the controller user store.
        Map<String, User> users = new HashMap<>();
        users.put("olivia@mail.com", student);
        users.put("admin@mail.com", admin);
        users.put("ep@mail.com", ep);
        userController.setUsers(users);
    }

    // Inject the fake view because the controller was created with null view.
    private void injectMockView(MockView view) throws Exception {
        Field viewField = UserController.class.getSuperclass().getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, view);
    }

    // Read currentUser from the controller to check the login result.
    private User getCurrentUser() throws Exception {
        Field currentUserField = UserController.class.getSuperclass().getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        return (User) currentUserField.get(userController);
    }

    // Student login should succeed and set the student as current user.
    @Test
    void loginSuccess_setsStudentAsCurrentUser() throws Exception {
        mockView = new MockView("olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertNotNull(currentUser,
                        "Current user should be set after a successful login."),
                () -> assertInstanceOf(Student.class, currentUser,
                        "The logged-in user should be a Student."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "The logged-in student email should match the input email."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "A successful login should display 'Login successful.'."),
                () -> assertTrue(mockView.errorMessages.isEmpty(),
                        "A successful login should not display any error message.")
        );
    }

    // Admin login should also succeed and set the correct admin account.
    @Test
    void loginSuccess_setsAdminAsCurrentUser() throws Exception {
        mockView = new MockView("admin@mail.com", "admin123");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertNotNull(currentUser,
                        "Current user should be set after admin login."),
                () -> assertInstanceOf(AdminStaff.class, currentUser,
                        "The logged-in user should be an AdminStaff."),
                () -> assertEquals("admin@mail.com", currentUser.getEmail(),
                        "The logged-in admin email should match the input email."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "Admin login should display the success message.")
        );
    }

    // Entertainment provider login should also work.
    @Test
    void loginSuccess_setsEntertainmentProviderAsCurrentUser() throws Exception {
        mockView = new MockView("ep@mail.com", "password");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertNotNull(currentUser,
                        "Current user should be set after entertainment provider login."),
                () -> assertInstanceOf(EntertainmentProvider.class, currentUser,
                        "The logged-in user should be an EntertainmentProvider."),
                () -> assertEquals("ep@mail.com", currentUser.getEmail(),
                        "The logged-in EP email should match the input email."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "Entertainment provider login should display the success message.")
        );
    }

    // The login method trims spaces, so valid credentials with spaces should still work.
    @Test
    void loginSuccess_trimsEmailAndPassword() throws Exception {
        mockView = new MockView("  olivia@mail.com  ", " 123 ");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertNotNull(currentUser,
                        "Login should still succeed when the email and password have surrounding spaces."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "Trimmed email should match the stored student account."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "Trimmed valid credentials should still display the success message.")
        );
    }

    // Wrong password should first give an error, then a correct retry should log in successfully.
    @Test
    void wrongPassword_displaysErrorThenLogsInOnRetry() throws Exception {
        mockView = new MockView("olivia@mail.com", "wrongpassword", "olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                        "An error message should be displayed when the password is wrong."),
                () -> assertNotNull(currentUser,
                        "The user should be logged in after a valid retry."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "After retry, the logged-in user should be the student account."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "A valid retry should display the success message.")
        );
    }

    // Unknown email should also show an error first, then allow login when correct details are entered.
    @Test
    void wrongEmail_displaysErrorThenLogsInOnRetry() throws Exception {
        mockView = new MockView("unknown@mail.com", "123", "olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                        "An error message should be displayed when the email is not registered."),
                () -> assertNotNull(currentUser,
                        "The user should be logged in after entering a valid registered account."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "After retry, the current user should be the expected student account."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "A valid retry after wrong email should display the success message.")
        );
    }

    // The login loop should continue after multiple failed attempts until correct input is given.
    @Test
    void wrongCredentialsMultipleTimes_thenLoginSucceeds() throws Exception {
        mockView = new MockView(
                "wrong@mail.com", "111",
                "olivia@mail.com", "wrong",
                "olivia@mail.com", "123"
        );
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertEquals(2,
                        Collections.frequency(mockView.errorMessages, "Incorrect email/password."),
                        "Two invalid attempts should produce two incorrect credential error messages."),
                () -> assertNotNull(currentUser,
                        "A later valid attempt should still log the user in."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "The final successful login should set the expected student as current user."),
                () -> assertEquals(1,
                        Collections.frequency(mockView.successMessages, "Login successful."),
                        "Exactly one success message should be shown when login finally succeeds.")
        );
    }

    // Empty email should stop the login and should not log in any user.
    @Test
    void loginFailure_emptyEmail_doesNotLogin() throws Exception {
        mockView = new MockView("", "123");
        injectMockView(mockView);

        userController.login();

        assertAll(
                () -> assertNull(getCurrentUser(),
                        "Current user should remain null when the email is empty."),
                () -> assertTrue(mockView.errorMessages.contains("Email/password cannot be empty."),
                        "Empty email should display 'Email/password cannot be empty.'."),
                () -> assertFalse(mockView.successMessages.contains("Login successful."),
                        "Empty email should not display the success message.")
        );
    }

    // Empty password should also stop the login and keep currentUser as null.
    @Test
    void loginFailure_emptyPassword_doesNotLogin() throws Exception {
        mockView = new MockView("olivia@mail.com", "");
        injectMockView(mockView);

        userController.login();

        assertAll(
                () -> assertNull(getCurrentUser(),
                        "Current user should remain null when the password is empty."),
                () -> assertTrue(mockView.errorMessages.contains("Email/password cannot be empty."),
                        "Empty password should display 'Email/password cannot be empty.'."),
                () -> assertFalse(mockView.successMessages.contains("Login successful."),
                        "Empty password should not display the success message.")
        );
    }

    // Blank strings become empty after trimming, so they should be treated as empty input.
    @Test
    void loginFailure_blankCredentials_doesNotLogin() throws Exception {
        mockView = new MockView("   ", "   ");
        injectMockView(mockView);

        userController.login();

        assertAll(
                () -> assertNull(getCurrentUser(),
                        "Current user should remain null when both email and password are blank after trimming."),
                () -> assertTrue(mockView.errorMessages.contains("Email/password cannot be empty."),
                        "Blank credentials should display the empty input error message."),
                () -> assertFalse(mockView.errorMessages.contains("Incorrect email/password."),
                        "Blank credentials should not be treated as incorrect credentials.")
        );
    }

    // After one failed attempt, entering empty input should stop the process without logging in.
    @Test
    void wrongCredentialsThenEmptyInput_stopsWithoutLogin() throws Exception {
        mockView = new MockView("olivia@mail.com", "wrong", "", "123");
        injectMockView(mockView);

        userController.login();

        assertAll(
                () -> assertNull(getCurrentUser(),
                        "Current user should remain null when the user exits with empty input after a failed attempt."),
                () -> assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                        "The first failed attempt should display incorrect credentials."),
                () -> assertTrue(mockView.errorMessages.contains("Email/password cannot be empty."),
                        "The later empty input should display the empty input error."),
                () -> assertFalse(mockView.successMessages.contains("Login successful."),
                        "No success message should be displayed when no valid login happens.")
        );
    }

    // Email matching is case-sensitive in the current implementation.
    @Test
    void loginIsCaseSensitiveForEmail() throws Exception {
        mockView = new MockView("OLIVIA@mail.com", "123", "olivia@mail.com", "123");
        injectMockView(mockView);

        userController.login();

        User currentUser = getCurrentUser();

        assertAll(
                () -> assertTrue(mockView.errorMessages.contains("Incorrect email/password."),
                        "Using a different email case should first fail with incorrect credentials."),
                () -> assertNotNull(currentUser,
                        "The later correctly cased email should allow login."),
                () -> assertEquals("olivia@mail.com", currentUser.getEmail(),
                        "After the correct retry, the student account should be logged in."),
                () -> assertTrue(mockView.successMessages.contains("Login successful."),
                        "The successful retry should display the success message.")
        );
    }
}