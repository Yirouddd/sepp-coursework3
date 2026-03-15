package external;

/**
 * API for interacting with a Verification Service, which checks if an Entertainment Provider is legitimate.
 */
public interface VerificationService {
    /**
     *
     * @param businessRegistrationNumber Unique business identifier for the EP.
     * @return True if EP is legitimate and false otherwise.
     */
    boolean verifyEntertainmentProvider(String businessRegistrationNumber);

}
