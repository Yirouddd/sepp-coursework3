package unitTest;

import external.MockPaymentSystem;
import external.PaymentSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MockPaymentSystemTest {
    private MockPaymentSystem paymentSystem;

    @BeforeEach
    void setUp() {
        paymentSystem = new MockPaymentSystem();
    }

    @Test
    public void testProcessPaymentSuccess() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0);
        assertTrue(result, "Payment should succeed with valid inputs");
    }

    @Test
    public void testProcessPaymentNullEmail() {
        boolean result = paymentSystem.processPayment(2, "Concert", null,
                123456789, "ep@test.com", 50.0);
        assertFalse(result, "Payment should fail with null student email");
    }

    @Test
    public void testProcessPaymentZeroAmount() {
        boolean result = paymentSystem.processPayment(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 0);
        assertFalse(result, "Payment should fail with zero amount");
    }

    @Test
    public void testProcessRefundSuccess() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, "ep@test.com", 50.0, "Event cancelled");
        assertTrue(result, "Refund should succeed with valid inputs");
    }

    @Test
    public void testProcessRefundNullEPEmail() {
        boolean result = paymentSystem.processRefund(2, "Concert", "student@test.com",
                123456789, null, 50.0, "Event cancelled");
        assertFalse(result, "Refund should fail with null EP email");
    }
}