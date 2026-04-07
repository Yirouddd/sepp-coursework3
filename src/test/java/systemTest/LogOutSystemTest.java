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

public class LogOutSystemTest {

    private UserController userController;
    private MockView mockView;

    // MockView for capturing UI output.
    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        // Constructs a MockView with a sequence of pre-defined inputs.
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

    // Sets up a fresh UserController with test users before each test.
    @BeforeEach
    void setup() throws Exception {
        userController = new UserController(null, null, null, null);

        Student student = new Student("olivia@mail.com", "123", "olivia", 123);
        AdminStaff admin = new AdminStaff("admin@mail.com", "456", "admin1");
        EntertainmentProvider ep = new EntertainmentProvider(
                "ep@mail.com", "789", "OrgName", "BN1", "Contact", "Description");

        Map<String, User> users = new HashMap<>();
        users.put("olivia@mail.com", student);
        users.put("admin@mail.com", admin);
        users.put("ep@mail.com", ep);
        userController.setUsers(users);

        mockView = new MockView();
        injectMockView(mockView);
    }

    // Injects MockView into the controller's view.
    private void injectMockView(MockView view) throws Exception {
        Field viewField = UserController.class.getSuperclass().getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, view);
    }

    // Retrieves the controller's currentUser.
    private User getCurrentUser() throws Exception {
        Field currentUserField = UserController.class.getSuperclass().getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        return (User) currentUserField.get(userController);
    }

    // Logs in a student and then resets the view so only logout messages are captured.
    private void correctLogIn() throws Exception {
        mockView = new MockView("olivia@mail.com", "123");
        injectMockView(mockView);
        userController.login();

        mockView = new MockView();
        injectMockView(mockView);
    }

    // Sets current user directly for role-specific logout checks.
    private void setCurrentUser(User user) {
        userController.setCurrentUser(user);
    }

    // Tests that a successful logout clears the current user.
    @Test
    void logoutSuccess_clearsCurrentUser() throws Exception {
        correctLogIn();

        userController.logout();

        assertNull(getCurrentUser(), "Current user should be null after logout");
    }

    // Tests that a successful logout displays the exact success message.
    @Test
    void logoutSuccess_displaysSuccessMessage() throws Exception {
        correctLogIn();

        userController.logout();

        assertTrue(mockView.successMessages.contains("Log out successful."),
                "The exact logout success message should be displayed after logout");
    }

    // Tests that logout does not display an error message after a normal logout.
    @Test
    void logoutSuccess_doesNotDisplayErrorMessage() throws Exception {
        correctLogIn();

        userController.logout();

        assertTrue(mockView.errorMessages.isEmpty(),
                "No error message should be displayed when logout is successful");
    }

    // Tests that calling logout when no user is logged in leaves the current user as null.
    @Test
    void notLoggedIn_keepsCurrentUserNull() throws Exception {
        userController.logout();

        assertNull(getCurrentUser(), "Current user should remain null when logging out without being logged in");
    }

    // Tests that calling logout when no user is logged in still shows the same success message.
    @Test
    void notLoggedIn_stillDisplaysSuccessMessage() {
        userController.logout();

        assertTrue(mockView.successMessages.contains("Log out successful."),
                "The system should still display the logout success message even if nobody was logged in");
    }

    // Tests that admin logout also clears the current user.
    @Test
    void logoutWhenCurrentUserIsAdmin_clearsCurrentUser() throws Exception {
        setCurrentUser(userController.getUsers().get("admin@mail.com"));

        userController.logout();

        assertNull(getCurrentUser(), "Current user should be null after admin logout");
    }

    // Tests that entertainment provider logout also clears the current user.
    @Test
    void logoutWhenCurrentUserIsEntertainmentProvider_clearsCurrentUser() throws Exception {
        setCurrentUser(userController.getUsers().get("ep@mail.com"));

        userController.logout();

        assertNull(getCurrentUser(), "Current user should be null after entertainment provider logout");
    }

    // Tests that logging out twice keeps the system in a valid guest state.
    @Test
    void logoutTwice_keepsCurrentUserNullAndShowsSuccessEachTime() throws Exception {
        correctLogIn();

        userController.logout();
        userController.logout();

        assertAll(
                () -> assertNull(getCurrentUser(),
                        "Current user should still be null after logging out twice"),
                () -> assertEquals(2, mockView.successMessages.size(),
                        "Two logout success messages should be displayed after two logout calls"),
                () -> assertEquals("Log out successful.", mockView.successMessages.get(0),
                        "The first logout message should be the expected one"),
                () -> assertEquals("Log out successful.", mockView.successMessages.get(1),
                        "The second logout message should also be the expected one")
        );
    }
}
