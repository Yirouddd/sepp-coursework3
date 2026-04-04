package systemTest;

import controller.UserController;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.EntertainmentProvider;
import user.User;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class registerEntertainmentProviderSystemTest {
    private UserController userController;
    private MockView mockView;

    private static class MockView implements View {
        private final Queue<String> inputs = new ArrayDeque<>();
        private final List<String> successMessages = new ArrayList<>();
        private final List<String> errorMessages = new ArrayList<>();

        MockView(String... input) {
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
    void setup() throws Exception {
        userController = new UserController(mockView,
                "src/resources/student.txt",
                "src/resources/admins.txt"
        );
    }

    private void injectMockView(MockView mockView) throws Exception {
        Field viewField = UserController.class.getDeclaredField("view");
        viewField.setAccessible(true);
        viewField.set(userController, mockView);
    }

    @SuppressWarnings("unchecked")
    private Map<String, User> getUsersMap() throws Exception {
        Field usersField = UserController.class.getDeclaredField("users");
        usersField.setAccessible(true);
        return (Map<String, User>) usersField.get(userController);
    }

    @Test
    void registerEntertainmentProviderSucceedsWithValidInput() throws Exception {
        mockView = new MockView(
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.successMessages.contains("Register successful!"));
        assertTrue(mockView.errorMessages.isEmpty());

        Map<String, User> users = getUsersMap();
        assertTrue(users.containsKey("1234567890"));
        assertTrue(users.get("1234567890") instanceof EntertainmentProvider);
    }

    @Test
    void registerEntertainmentProviderRetriesWhenEmailIsInvalid() throws Exception {
        mockView = new MockView(
                "invalid-email",
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.errorMessages.contains("Invalid email."));
        assertTrue(mockView.successMessages.contains("Register successful!"));
    }

    @Test
    void registerEntertainmentProviderRetriesWhenPasswordIsEmpty() throws Exception {
        mockView = new MockView(
                "ep@mail.com",
                "",
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.errorMessages.contains("Password cannot be empty."));
        assertTrue(mockView.successMessages.contains("Register successful!"));
    }

    @Test
    void registerEntertainmentProviderRetriesWhenBusinessNumberIsInvalid() throws Exception {
        mockView = new MockView(
                "ep@mail.com",
                "123456",
                "ABC Org",
                "123",
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.successMessages.contains("Register successful!"));
    }

    @Test
    void registerEntertainmentProviderRetriesWhenOrganisationAlreadyExists() throws Exception {
        Map<String, User> users = getUsersMap();
        users.put(
                "1234567890",
                new EntertainmentProvider(
                        "old@mail.com",
                        "111111",
                        "ABC Org",
                        "1234567890",
                        "Old Contact",
                        "Old description"
                )
        );

        mockView = new MockView(
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "ep2@mail.com",
                "654321",
                "New Org",
                "0987654321",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.errorMessages.contains("This entertainment provider already exists! "));
        assertTrue(mockView.successMessages.contains("Register successful!"));
    }

    @Test
    void registerEntertainmentProviderRetriesWhenDescriptionIsEmpty() throws Exception {
        mockView = new MockView(
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "",
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.errorMessages.contains("empty"));
        assertTrue(mockView.successMessages.contains("Register successful!"));
    }

    @Test
    void registerEntertainmentProviderRetriesWhenMainContactNameIsEmpty() throws Exception {
        mockView = new MockView(
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "",
                "ep@mail.com",
                "123456",
                "ABC Org",
                "1234567890",
                "Alice",
                "Great entertainment provider"
        );
        injectMockView(mockView);

        userController.registerEntertainmentProvider();

        assertTrue(mockView.errorMessages.contains("Main contact name cannot be empty."));
        assertTrue(mockView.successMessages.contains("Register successful!"));
    }
}