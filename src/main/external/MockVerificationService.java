package external;

/**
 * A mock Verification Service Provider implementation for testing.
 * This does not communicate with any real verification API, just uses local user data to simulate one.
 *
 * All this mock service does is check if the provided business number if of the appropriate length (which for our
 * purposes we will assume is 10 numbers/characters). In practice, the real verification API will look up the number
 * in a database of numbers (which we don't have access to). As such, it's fine if the mock implementation can
 * simulate both outcomes to check if our system can handle them.
 */
public class MockVerificationService implements VerificationService {
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";

    public boolean verifyEntertainmentProvider(String businessRegistrationNumber) {
        System.out.print(ANSI_CYAN);
        if (businessRegistrationNumber.length() != 10) {
            System.out.println("Business Number is not of the correct length.");
            System.out.print(ANSI_RESET);
            return false;
        }
        System.out.println("Business Number belongs to a legitimate business.");
        System.out.print(ANSI_RESET);
        return true;
    }
}
