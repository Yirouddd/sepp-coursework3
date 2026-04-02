package controller;

import interfaces.View;

import java.util.*;

public class TestView implements View {

    private Queue<String> inputs = new LinkedList<>();
    private List<String> outputs = new ArrayList<>();

    public void addInput(String input) {
        inputs.add(input);
    }

    public List<String> getOutputs() {
        return outputs;
    }

    @Override
    public String getInput(String inputPrompt) {
        outputs.add("PROMPT: " + inputPrompt);
        return inputs.poll();
    }

    @Override
    public void displaySuccess(String successMessage) {
        outputs.add("SUCCESS: " + successMessage);
    }

    @Override
    public void displayError(String errorMessage) {
        outputs.add("ERROR: " + errorMessage);
    }

    @Override
    public void displayListOfPerformances(Collection<String> list) {
        outputs.addAll(list);
    }

    @Override
    public void displaySpecificPerformance(String performanceInfo) {
        outputs.add("PERFORMANCE: " + performanceInfo);
    }

    @Override
    public void displayBookingRecord(String bookingRecord) {
        outputs.add("BOOKING: " + bookingRecord);
    }
}