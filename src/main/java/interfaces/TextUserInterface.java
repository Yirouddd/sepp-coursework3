package interfaces;

import java.util.Collection;
import java.util.Scanner;

public class TextUserInterface implements View {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public String getInput(String inputPrompt) {
        System.out.println(inputPrompt);
        String input = scanner.nextLine().trim();
        return input;
    }

    @Override

    public void displaySuccess(String successMessage) {

    }

    public void displayError(String errorMessage) {

    }

    @Override
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {

    }

    public void displayListofPerformances(Collection<String> lisftOfPerformanceInfo) {

    }

    public void displaySpecificPerformance(String performanceInfo) {

    }

    public void displayBookingRecord(String bookingRecord) {

    }
}
