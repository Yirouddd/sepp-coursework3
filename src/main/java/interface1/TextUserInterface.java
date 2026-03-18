package interface1;

import java.util.Collection;

public interface TextUserInterface {
    public String getInput(String inputPrompt) {
        // Implementation for getting user input
        return ""; // Placeholder return value
    }

    public void displaySuccess(String successMessage) {
        // Implementation for displaying success message
    }

    public void displayError(String errorMessage) {
        // Implementation for displaying error message
    }

    public void displayListofPerformances(Collection<String> lisftOfPerformanceInfo) {
        // Implementation for displaying a list of performances
    }

    public void displaySpecificPerformance(String performanceInfo) {
        // Implementation for displaying specific performance information
    }

    public void displayBookingRecord(String bookingRecord) {
        // Implementation for displaying booking record information
    }

}
