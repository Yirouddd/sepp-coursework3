import java.util.Collection;

/**
 * View interface for the performance booking system.
 * Defines the contract for all view implementations (console, GUI, etc.)
 */
public interface View {
    
    /**
     * Gets input from the user with a prompt message
     * @param inputPrompt the message to display to the user
     * @return the user's input as a String
     */
    String getInput(String inputPrompt);
    
    /**
     * Displays a success message to the user
     * @param successMessage the success message to display
     */
    void displaySuccess(String successMessage);
    
    /**
     * Displays an error message to the user
     * @param errorMessage the error message to display
     */
    void displayError(String errorMessage);
    
    /**
     * Displays a list of all performances
     * @param listOfPerformanceInfo collection of performance information strings
     */
    void displayListOfPerformances(Collection<String> listOfPerformanceInfo);
    
    /**
     * Displays details of a specific performance
     * @param performanceInfo the performance information to display
     */
    void displaySpecificPerformance(String performanceInfo);
    
    /**
     * Displays a booking record
     * @param bookingRecord the booking record to display
     */
    void displayBookingRecord(String bookingRecord);
}