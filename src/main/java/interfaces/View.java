package interfaces;

import java.util.Collection;

/**
 * View interface for text-based interaction.
 */
public interface View {

    /**
     * Gets input from the user with a prompt.
     *
     * @param inputPrompt prompt to display
     * @return user input
     */
    public String getInput(String inputPrompt);

    /**
     * Displays a success/info message.
     *
     * @param successMessage message to display
     */
    public void displaySuccess(String successMessage);

    /**
     * Displays an error message.
     *
     * @param errorMessage message to display
     */
    public void displayError(String errorMessage);

    /**
     * Displays a list of performances.
     *
     * @param listOfPerformanceInfo performance info strings
     */
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo);

    /**
     * Displays one performance's details.
     *
     * @param performanceInfo performance details
     */
    public void displaySpecificPerformance(String performanceInfo);

    /**
     * Displays a booking record.
     *
     * @param bookingRecord booking record
     */
    public void displayBookingRecord(String bookingRecord);
}
