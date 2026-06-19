package unitTest;

import external.MockPaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MockPaymentSystemTest {
    private MockPaymentSystem paymentSystem;

    @BeforeEach
    void setUp() {
        paymentSystem = new MockPaymentSystem();
    }

    // Process Payment Tests
    @Test
    public void testProcessPaymentSuccess() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0);
        assertTrue(result, "Payment should succeed with valid inputs");
    }

    @Test
    public void testProcessPaymentNullStudentEmail() {
        boolean result = paymentSystem.processPayment(2, "Concert", null,
                123456789, "ep@test.com", 50.0);
        assertFalse(result, "Payment should fail with null student email");
    }

    @Test
    public void testProcessPaymentNullEPEmail() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, null, 50.0);
        assertFalse(result, "Payment should fail with null EP email");
    }

    @Test
    public void testProcessPaymentZeroAmount() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 0.0);
        assertFalse(result, "Payment should fail with zero amount");
    }

    @Test
    public void testProcessPaymentNegativeAmount() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", -50.0);
        assertFalse(result, "Payment should fail with negative amount");
    }

    @Test
    public void testProcessPaymentZeroTickets() {
        boolean result = paymentSystem.processPayment(0, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0);
        assertFalse(result, "Payment should fail with zero tickets");
    }

    @Test
    public void testProcessPaymentNegativeTickets() {
        boolean result = paymentSystem.processPayment(-5, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0);
        assertFalse(result, "Payment should fail with negative tickets");
    }

    @Test
    public void testProcessPaymentNullEventTitle() {
        boolean result = paymentSystem.processPayment(2, null, "student@test.com",
                123456789, "ep@test.com", 50.0);
        assertFalse(result, "Payment should fail with null event title");
    }

    @Test
    public void testProcessPaymentLargeAmount() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 999999.99);
        assertTrue(result, "Payment should succeed with large valid amount");
    }

    @Test
    public void testProcessPaymentSmallAmount() {
        boolean result = paymentSystem.processPayment(1, "Concert", "student@test.com",
                123456789, "ep@test.com", 0.01);
        assertTrue(result, "Payment should succeed with small valid amount");
    }

    // Process Refund Tests
    @Test
    public void testProcessRefundSuccess() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0, "Event cancelled");
        assertTrue(result, "Refund should succeed with valid inputs");
    }

    @Test
    public void testProcessRefundNullStudentEmail() {
        boolean result = paymentSystem.processRefund(2, "Concert", null,
                123456789, "ep@test.com", 50.0, "Event cancelled");
        assertFalse(result, "Refund should fail with null student email");
    }

    @Test
    public void testProcessRefundNullEPEmail() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, null, 50.0, "Event cancelled");
        assertFalse(result, "Refund should fail with null EP email");
    }

    @Test
    public void testProcessRefundZeroAmount() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 0.0, "Event cancelled");
        assertFalse(result, "Refund should fail with zero amount");
    }

    @Test
    public void testProcessRefundNegativeAmount() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, "ep@test.com", -50.0, "Event cancelled");
        assertFalse(result, "Refund should fail with negative amount");
    }

    @Test
    public void testProcessRefundZeroTickets() {
        boolean result = paymentSystem.processRefund(0, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0, "Event cancelled");
        assertFalse(result, "Refund should fail with zero tickets");
    }
}