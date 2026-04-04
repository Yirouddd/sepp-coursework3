package interfaces;

import java.util.Collection;
import java.util.Scanner;

/**
 * Text-based console UI implementation.
 */
public class TextUserInterface implements View {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public String getInput(String inputPrompt) {
        System.out.println(inputPrompt);
        return scanner.nextLine().trim();
    }

    @Override
    public void displaySuccess(String successMessage) {
        System.out.println(successMessage);
    }

    @Override
    public void displayError(String errorMessage) {
        System.out.println(errorMessage);
    }

    @Override
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {
        for (String performance : listOfPerformanceInfo) {
            System.out.println(performance);
        }
    }

    @Override
    public void displaySpecificPerformance(String performanceInfo) {
        System.out.println(performanceInfo);
    }

    @Override
    public void displayBookingRecord(String bookingRecord) {
        System.out.println(bookingRecord);
    }
}
