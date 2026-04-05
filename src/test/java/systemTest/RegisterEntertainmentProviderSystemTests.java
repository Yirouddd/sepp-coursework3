package systemTest;

import controller.UserController;
import external.VerificationService;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import user.EntertainmentProvider;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterEntertainmentProviderSystemTests {

    @Mock
    private View view;

    @Mock
    private VerificationService verificationService;

    @TempDir
    Path tempDir;

    private UserController userController;

    @BeforeEach
    void setUp() throws Exception {
        Path studentsFile = tempDir.resolve("students.txt");
        Path adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(studentsFile, "student1@ed.ac.uk,pass123,Student One,123456\n");
        Files.writeString(adminsFile, "admin1@ed.ac.uk,adminpass,Admin One\n");

        userController = new UserController(
                view,
                verificationService,
                adminsFile.toString(),
                studentsFile.toString()
        );

        userController.getUsers().put(
                "provider1@test.com",
                new EntertainmentProvider(
                        "provider1@test.com",
                        "providerpass",
                        "Provider One Ltd",
                        "1234567890",
                        "Alice Provider",
                        "Existing provider"
                )
        );
    }

    @Test
    void shouldRegisterEntertainmentProviderSuccessfully() {
        when(verificationService.verifyEntertainmentProvider("1111111111")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "newprovider@test.com",
                "pw123",
                "New Org",
                "1111111111",
                "Jane Contact",
                "Great events"
        );

        userController.registerEntertainmentProvider();

        assertAll(
                () -> assertNotNull(userController.getCurrentUser(),
                        "A successful registration should log the provider in."),
                () -> assertTrue(userController.getCurrentUser() instanceof EntertainmentProvider,
                        "The current user should be an entertainment provider after registration."),
                () -> assertTrue(userController.getUsers().containsKey("newprovider@test.com"),
                        "The newly registered provider should be added to the users map.")
        );

        verify(view).displaySuccess(contains("Register successful"));
    }

    @Test
    void shouldRetryAfterInvalidEmailThenRegisterSuccessfully() {
        when(verificationService.verifyEntertainmentProvider("1111111111")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "invalid-email",
                "newprovider@test.com",
                "pw123",
                "Retry Org",
                "1111111111",
                "Retry Contact",
                "Retry description"
        );

        userController.registerEntertainmentProvider();

        assertEquals("newprovider@test.com", userController.getCurrentUser().getEmail(),
                "Registration should succeed after re-entering a valid email.");

        verify(view).displayError(contains("Invalid email"));
        verify(view).displaySuccess(contains("Register successful"));
    }

    @Test
    void shouldRetryAfterVerificationFailureThenRegisterSuccessfully() {
        when(verificationService.verifyEntertainmentProvider("123")).thenReturn(false);
        when(verificationService.verifyEntertainmentProvider("1111111111")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "verify@test.com", "pw123", "Verify Org", "123",
                "verify@test.com", "pw123", "Verify Org", "1111111111", "Verifier", "Verified provider"
        );

        userController.registerEntertainmentProvider();

        assertEquals("verify@test.com", userController.getCurrentUser().getEmail(),
                "Registration should succeed after a valid business number is entered.");

        verify(view).displayError(contains("verification failed"));
        verify(view).displaySuccess(contains("Register successful"));
    }

    @Test
    void shouldRejectDuplicateEmailThenAllowFreshRegistration() {
        when(verificationService.verifyEntertainmentProvider("1111111111")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "provider1@test.com", "pw123", "Another Org", "1111111111",
                "fresh@test.com", "pw999", "Fresh Org", "1111111111", "Fresh Contact", "Fresh Description"
        );

        userController.registerEntertainmentProvider();

        assertEquals("fresh@test.com", userController.getCurrentUser().getEmail(),
                "A fresh provider should be registered after the duplicate email is rejected.");

        verify(view).displayError(contains("email already exists"));
        verify(view).displaySuccess(contains("Register successful"));
    }

    @Test
    void shouldRejectDuplicateOrganisationAndBusinessNumberThenAllowFreshRegistration() {
        when(verificationService.verifyEntertainmentProvider("1234567890")).thenReturn(true);
        when(verificationService.verifyEntertainmentProvider("2222222222")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "another@test.com", "pw123", "Provider One Ltd", "1234567890",
                "fresh2@test.com", "pw999", "Fresh Org 2", "2222222222", "Fresh Contact 2", "Fresh Description 2"
        );

        userController.registerEntertainmentProvider();

        assertEquals("fresh2@test.com", userController.getCurrentUser().getEmail(),
                "A fresh provider should be registered after the duplicate organisation is rejected.");

        verify(view).displayError(contains("already exists"));
        verify(view).displaySuccess(contains("Register successful"));
    }

    @Test
    void shouldRetryAfterEmptyDescriptionThenRegisterSuccessfully() {
        when(verificationService.verifyEntertainmentProvider("1111111111")).thenReturn(true);

        when(view.getInput(anyString())).thenReturn(
                "emptydesc@test.com", "pw123", "Desc Org", "1111111111", "Contact Name", "",
                "emptydesc@test.com", "pw123", "Desc Org", "1111111111", "Contact Name", "Valid description"
        );

        userController.registerEntertainmentProvider();

        assertEquals("emptydesc@test.com", userController.getCurrentUser().getEmail(),
                "Registration should succeed after a valid description is provided.");

        verify(view).displayError(contains("Description"));
        verify(view).displaySuccess(contains("Register successful"));
    }
}