package systemTest;

import controller.UserController;
import external.VerificationService;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import user.EntertainmentProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * System tests for register entertainment provider use case.
 * In this class we try success case and several error cases too.
 */
public class RegisterEntertainmentProviderSystemTests {

    private static final String STUDENT_EMAIL = "student1@test.com";
    private static final String STUDENT_PASSWORD = "pass1";
    private static final String ADMIN_EMAIL = "admin1@test.com";
    private static final String ADMIN_PASSWORD = "pass1";

    @TempDir
    Path tempDir;

    private Path studentsFile;
    private Path adminsFile;

    private View mockView;
    private VerificationService mockVerificationService;

    private UserController userController;

    @BeforeEach
    void setUp() throws IOException {
        studentsFile = tempDir.resolve("students.txt");
        adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(
                studentsFile,
                STUDENT_EMAIL + "," + STUDENT_PASSWORD + ",name1,1234567\n"
                        + "student2@test.com,pass2,name2,7654321\n"
        );

        Files.writeString(
                adminsFile,
                ADMIN_EMAIL + "," + ADMIN_PASSWORD + ",name1\n"
        );

        mockView = mock(View.class);
        mockVerificationService = mock(VerificationService.class);

        when(mockVerificationService.verifyEntertainmentProvider(anyString())).thenReturn(true);

        userController = new UserController(
                mockView,
                mockVerificationService,
                studentsFile.toString(),
                adminsFile.toString()
        );
    }

    private void stubInputs(String... inputs) {
        when(mockView.getInput(anyString()))
                .thenReturn(inputs[0], Arrays.copyOfRange(inputs, 1, inputs.length));
    }

    private void addExistingProvider(
            String email,
            String password,
            String orgName,
            String businessNumber,
            String contactName,
            String description
    ) {
        EntertainmentProvider provider = new EntertainmentProvider(
                email,
                password,
                orgName,
                businessNumber,
                contactName,
                description
        );
        userController.getUsers().put(email, provider);
    }

    /**
     * This test checks the normal register flow.
     * After success, provider should be saved and logged in.
     */
    @Test
    void shouldRegisterProviderAndLogThemInWhenAllDetailsAreValid() {
        stubInputs(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Valid provider registration should create the account and log the provider in",
                () -> assertNotNull(provider,
                        "Current user should become the new entertainment provider after registration."),
                () -> assertEquals("ep1@test.com", provider.getEmail(),
                        "Registered provider email should match the entered email."),
                () -> assertEquals("org1", provider.getOrgName(),
                        "Registered provider organisation should match the entered organisation name."),
                () -> assertEquals("bn1", provider.getBusinessNumber(),
                        "Registered provider business number should match the verified number."),
                () -> assertTrue(userController.getUsers().containsKey("ep1@test.com"),
                        "Users map should contain the new provider account after successful registration."),
                () -> verify(mockVerificationService).verifyEntertainmentProvider("bn1"),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks invalid email first.
     * System should show error and ask again.
     */
    @Test
    void shouldRejectInvalidEmailThenRegisterSuccessfullyAfterRetry() {
        stubInputs(
                "ep1",
                "ep1@test.com",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Registration should continue after invalid email and succeed on the next full try",
                () -> assertNotNull(provider,
                        "Provider should be registered after entering a valid email on retry."),
                () -> assertEquals("ep1@test.com", provider.getEmail(),
                        "The account should use the valid email from the second attempt."),
                () -> verify(mockView, atLeastOnce()).displayError("Invalid email."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks empty password.
     * Register should not finish until password is given.
     */
    @Test
    void shouldRejectEmptyPasswordThenRegisterSuccessfullyAfterRetry() {
        stubInputs(
                "ep1@test.com",
                "",
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Registration should reject empty password and succeed after a complete retry",
                () -> assertNotNull(provider,
                        "Provider should still be registered after entering a password on retry."),
                () -> assertEquals("ep1@test.com", provider.getEmail(),
                        "Registered account should keep the details from the valid retry attempt."),
                () -> verify(mockView, atLeastOnce()).displayError("Password cannot be empty."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks business verification fail.
     * Account should not be made until number is verified.
     */
    @Test
    void shouldRetryAfterBusinessVerificationFailsThenRegisterSuccessfully() {
        when(mockVerificationService.verifyEntertainmentProvider("bnBad")).thenReturn(false);
        when(mockVerificationService.verifyEntertainmentProvider("bn1")).thenReturn(true);

        stubInputs(
                "ep1@test.com",
                "pass1",
                "org1",
                "bnBad",
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Registration should loop after failed business verification and succeed later",
                () -> assertNotNull(provider,
                        "Provider should be registered after a later verified business number is entered."),
                () -> assertEquals("bn1", provider.getBusinessNumber(),
                        "Provider should store the business number from the successful retry."),
                () -> verify(mockVerificationService).verifyEntertainmentProvider("bnBad"),
                () -> verify(mockVerificationService).verifyEntertainmentProvider("bn1"),
                () -> verify(mockView, atLeastOnce()).displayError("Business number verification failed."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks duplicate email.
     * Same email should not be used again.
     */
    @Test
    void shouldRejectDuplicateEmailThenAllowRegistrationWithAnotherEmail() {
        addExistingProvider(
                "ep1@test.com",
                "pass0",
                "org0",
                "bn0",
                "name0",
                "desc0"
        );

        stubInputs(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1",
                "ep2@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Duplicate email should be rejected before a new email is accepted",
                () -> assertNotNull(provider,
                        "Provider should be registered after changing to an unused email."),
                () -> assertEquals("ep2@test.com", provider.getEmail(),
                        "Registered provider should use the fresh email from the second attempt."),
                () -> verify(mockView, atLeastOnce()).displayError("An account with this email already exists."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks same organisation and same business number.
     * In this case system should say provider already exists.
     */
    @Test
    void shouldRejectExistingProviderWithSameOrganisationAndBusinessNumberThenAllowRetry() {
        addExistingProvider(
                "ep0@test.com",
                "pass0",
                "org1",
                "bn1",
                "name0",
                "desc0"
        );

        stubInputs(
                "ep2@test.com",
                "pass1",
                "org1",
                "bn1",
                "ep2@test.com",
                "pass1",
                "org1",
                "bn2",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Duplicate provider identity should be rejected before a non-duplicate retry succeeds",
                () -> assertNotNull(provider,
                        "Provider should be registered after changing the duplicate business details."),
                () -> assertEquals("bn2", provider.getBusinessNumber(),
                        "The registered provider should keep the non-duplicate business number from retry."),
                () -> verify(mockView, atLeastOnce()).displayError("This entertainment provider already exists."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks empty contact name.
     * Main contact person is required before finish.
     */
    @Test
    void shouldRejectEmptyContactNameThenRegisterSuccessfullyAfterRetry() {
        stubInputs(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "",
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Registration should reject empty main contact name and succeed on retry",
                () -> assertNotNull(provider,
                        "Provider should be created after entering a valid contact name."),
                () -> assertEquals("ep1@test.com", provider.getEmail(),
                        "Registered account should use the details from the successful attempt."),
                () -> verify(mockView, atLeastOnce()).displayError("Main contact name cannot be empty."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }

    /**
     * This test checks empty description.
     * Register should not finish until description is entered.
     */
    @Test
    void shouldRejectEmptyDescriptionThenRegisterSuccessfullyAfterRetry() {
        stubInputs(
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "",
                "ep1@test.com",
                "pass1",
                "org1",
                "bn1",
                "name1",
                "desc1"
        );

        userController.registerEntertainmentProvider();

        EntertainmentProvider provider = (EntertainmentProvider) userController.getCurrentUser();

        assertAll("Registration should reject empty description and succeed after the full details are re-entered",
                () -> assertNotNull(provider,
                        "Provider should be created after entering a non-empty description."),
                () -> assertEquals("ep1@test.com", provider.getEmail(),
                        "Registered account should come from the successful retry attempt."),
                () -> verify(mockView, atLeastOnce()).displayError("Description cannot be empty."),
                () -> verify(mockView).displaySuccess("Register successful!")
        );
    }
}
