package interface1;

import java.util.Collection;

public interface TextUserInterface {
    public String getInput(String inputPrompt);

    public void displaySuccess(String successMessage);

    public void displayError(String errorMessage);

    public void displayListofPerformances(Collection<String> lisftOfPerformanceInfo);

    public void displaySpecificPerformance(String performanceInfo);

    public void displayBookingRecord(String bookingRecord);
}
