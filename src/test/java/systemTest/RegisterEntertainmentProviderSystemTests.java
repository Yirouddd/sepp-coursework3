package systemTest;

import controller.UserController;
import external.VerificationService;
import interfaces.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;

import java.nio.file.Files;
import java.nio.file.Path;

public class RegisterEntertainmentProviderSystemTests {

    @Mock
    private View view;

    @Mock
    private VerificationService verificationService;

    @TempDir
    Path tempDir;

    private UserController userController;
    private Path studentsFile;
    private Path adminsFile;

    @BeforeEach
    void setUp() throws Exception {
        studentsFile = tempDir.resolve("students.txt");
        adminsFile = tempDir.resolve("admins.txt");

        Files.writeString(studentsFile, "s1@ed.ac.uk,pass123,Student One,123456\n");
        Files.writeString(adminsFile, "admin@ed.ac.uk,adminpass,Admin One\n");

        userController = new UserController(
                view,
                verificationService,
                adminsFile.toString(),
                studentsFile.toString()
        );
    }


}
