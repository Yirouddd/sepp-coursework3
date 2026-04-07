package unitTest;

import external.MockPaymentSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class MockPaymentSystemTest {

    private MockPaymentSystem paymentSystem;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        paymentSystem = new MockPaymentSystem();
        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    private String getPrintedOutput() {
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    // Valid payment should succeed.
    @Test
    void processPayment_returnsTrue_whenAllInputsAreValid() {
        boolean result = paymentSystem.processPayment(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0);

        assertTrue(result, "Valid payment input should return true.");
    }

    // Null student email should be rejected.
    @Test
    void processPayment_returnsFalse_whenStudentEmailIsNull() {
        boolean result = paymentSystem.processPayment(2, "event1", null, 123456, "ep1@test.com", 30.0);

        assertFalse(result, "Null student email should return false.");
    }

    // Null provider email should be rejected.
    @Test
    void processPayment_returnsFalse_whenProviderEmailIsNull() {
        boolean result = paymentSystem.processPayment(2, "event1", "student1@test.com", 123456, null, 30.0);

        assertFalse(result, "Null provider email should return false.");
    }

    // Null event title should be rejected.
    @Test
    void processPayment_returnsFalse_whenEventTitleIsNull() {
        boolean result = paymentSystem.processPayment(2, null, "student1@test.com", 123456, "ep1@test.com", 30.0);

        assertFalse(result, "Null event title should return false.");
    }

    // Zero tickets should be rejected.
    @Test
    void processPayment_returnsFalse_whenTicketCountIsZero() {
        boolean result = paymentSystem.processPayment(0, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0);

        assertFalse(result, "Zero tickets should return false.");
    }

    // Negative tickets should be rejected.
    @Test
    void processPayment_returnsFalse_whenTicketCountIsNegative() {
        boolean result = paymentSystem.processPayment(-1, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0);

        assertFalse(result, "Negative ticket count should return false.");
    }

    // Zero transaction amount should be rejected.
    @Test
    void processPayment_returnsFalse_whenTransactionAmountIsZero() {
        boolean result = paymentSystem.processPayment(2, "event1", "student1@test.com", 123456, "ep1@test.com", 0.0);

        assertFalse(result, "Zero transaction amount should return false.");
    }

    // Negative transaction amount should be rejected.
    @Test
    void processPayment_returnsFalse_whenTransactionAmountIsNegative() {
        boolean result = paymentSystem.processPayment(2, "event1", "student1@test.com", 123456, "ep1@test.com", -1.0);

        assertFalse(result, "Negative transaction amount should return false.");
    }

    // Successful payment should print a purchase message.
    @Test
    void processPayment_printsPurchaseMessage_whenInputIsValid() {
        paymentSystem.processPayment(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0);

        assertTrue(getPrintedOutput().contains("has purchased 2 tickets for the event event1"),
                "Successful payment should print the purchase message.");
    }

    // Valid refund without organiser message should succeed.
    @Test
    void processRefund_returnsTrue_whenValidInputAndOrganiserMessageIsNull() {
        boolean result = paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, null);

        assertTrue(result, "Valid refund input without organiser message should return true.");
    }

    // Valid refund with organiser message should also succeed.
    @Test
    void processRefund_returnsTrue_whenValidInputAndOrganiserMessageIsProvided() {
        boolean result = paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, "msg1");

        assertTrue(result, "Valid refund input with organiser message should return true.");
    }

    // Null student email should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenStudentEmailIsNull() {
        boolean result = paymentSystem.processRefund(2, "event1", null, 123456, "ep1@test.com", 30.0, "msg1");

        assertFalse(result, "Null student email should return false for refunds.");
    }

    // Null provider email should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenProviderEmailIsNull() {
        boolean result = paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, null, 30.0, "msg1");

        assertFalse(result, "Null provider email should return false for refunds.");
    }

    // Null event title should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenEventTitleIsNull() {
        boolean result = paymentSystem.processRefund(2, null, "student1@test.com", 123456, "ep1@test.com", 30.0, "msg1");

        assertFalse(result, "Null event title should return false for refunds.");
    }

    // Zero tickets should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenTicketCountIsZero() {
        boolean result = paymentSystem.processRefund(0, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, "msg1");

        assertFalse(result, "Zero tickets should return false for refunds.");
    }

    // Negative tickets should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenTicketCountIsNegative() {
        boolean result = paymentSystem.processRefund(-2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, "msg1");

        assertFalse(result, "Negative ticket count should return false for refunds.");
    }

    // Zero transaction amount should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenTransactionAmountIsZero() {
        boolean result = paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 0.0, "msg1");

        assertFalse(result, "Zero transaction amount should return false for refunds.");
    }

    // Negative transaction amount should be rejected for refunds too.
    @Test
    void processRefund_returnsFalse_whenTransactionAmountIsNegative() {
        boolean result = paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", -5.0, "msg1");

        assertFalse(result, "Negative transaction amount should return false for refunds.");
    }

    // Successful refund should print the refund message.
    @Test
    void processRefund_printsRefundMessage_whenInputIsValid() {
        paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, null);

        assertTrue(getPrintedOutput().contains("has been refunded."),
                "Successful refund should print the refund message.");
    }

    // When organiser message is provided, it should be printed too.
    @Test
    void processRefund_printsOrganiserMessage_whenOrganiserMessageIsProvided() {
        paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, "msg1");

        assertTrue(getPrintedOutput().contains("Message from the provider who cancelled the event: msg1"),
                "Refund with organiser message should print that message.");
    }

    // When organiser message is null, the extra message line should not be printed.
    @Test
    void processRefund_doesNotPrintOrganiserMessage_whenOrganiserMessageIsNull() {
        paymentSystem.processRefund(2, "event1", "student1@test.com", 123456, "ep1@test.com", 30.0, null);

        assertFalse(getPrintedOutput().contains("Message from the provider who cancelled the event:"),
                "Refund without organiser message should not print an organiser message line.");
    }
}
