package systemTest;

import interfaces.View;
import java.util.Collection;
import java.util.Queue;
import java.util.LinkedList;

public class MockView implements View {
    private Queue<String> inputs = new LinkedList<>();
    private StringBuilder output = new StringBuilder();

    public void addInput(String input) {
        inputs.add(input);
    }

    @Override
    public String getInput(String inputPrompt) {
        if (inputs.isEmpty()) {
            throw new RuntimeException("No more inputs provided to MockView!");
        }
        return inputs.poll();  // Returns and removes from queue
    }

    @Override
    public void displaySuccess(String successMessage) {
        output.append("SUCCESS: ").append(successMessage).append("\n");
        System.out.println("SUCCESS: " + successMessage);
    }

    @Override
    public void displayError(String errorMessage) {
        output.append("ERROR: ").append(errorMessage).append("\n");
        System.out.println("ERROR: " + errorMessage);
    }

    @Override
    public void displayListOfPerformances(Collection<String> listOfPerformanceInfo) {}

    @Override
    public void displaySpecificPerformance(String performanceInfo) {}

    @Override
    public void displayBookingRecord(String bookingRecord) {}

    public String getOutput() {
        return output.toString();
    }
}