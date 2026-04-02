package interfaces;

import java.util.Collection;
import java.util.Scanner;

public class TextUserInterface implements View {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public String getInput(String inputPrompt) {
        System.out.println(inputPrompt);
        return scanner.nextLine().trim();
    }

    @Override
    public void displaySuccess(String successMessage) {
        System.out.println("SUCCESS " + successMessage);
    }

    @Override
    public void displayError(String errorMessage) {
        System.err.println("ERROR "+ errorMessage);
    }

    @Override
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {
        System.out.println("---List of Performances---");
        for (String info : listOfPerformanceInfo) {
            System.out.println(info);
        }
    }

    public void displaySpecificPerformance(String performanceInfo) {
        System.out.println("---Performance Details---");
        System.out.println(performanceInfo);
    }

    public void displayBookingRecord(String bookingRecord) {
        System.out.println("---Booking Record---");
        System.out.println(bookingRecord);
    }
}
