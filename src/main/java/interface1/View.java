package interface1;

import java.util.Collection;

public interface View {

    /**
     * Gets input from the user with a prompt message
     * @param inputPrompt the message to display to the user
     * @return the user's input as a String
     */
    public String getInput(String inputPrompt);

    /**
     * Displays a success message to the user
     * @param successMessage the success message to display
     */
    public void displaySuccess(String successMessage);

    /**
     * Displays an error message to the user
     * @param errorMessage the error message to display
     */
    public void displayError(String errorMessage);

    /**
     * Displays a list of all performances
     * @param listOfPerformanceInfo collection of performance information strings
     */
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo);

    /**
     * Displays details of a specific performance
     * @param performanceInfo the performance information to display
     */
    public void displaySpecificPerformance(String performanceInfo);

    /**
     * Displays a booking record
     * @param bookingRecord the booking record to display
     */
    public void displayBookingRecord(String bookingRecord);
}
