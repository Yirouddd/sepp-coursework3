package systemTest;

import external.MockPaymentSystem;
import external.PaymentSystem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestMockPaymentSystem {

    @Test
    public void testProcessPaymentSuccess() {
        PaymentSystem paymentSystem = new MockPaymentSystem();
        boolean result = paymentSystem.processPayment(
                2,
                "Concert",
                "student@test.com",
                123456789,
                "ep@test.com",
                50.0
        );
        assertTrue(result, "Payment should succeed with valid inputs");
    }

    @Test
    public void testProcessPaymentNullEmail() {
        PaymentSystem paymentSystem = new MockPaymentSystem();
        boolean result = paymentSystem.processPayment(
                2,
                "Concert",
                null,
                123456789,
                "ep@test.com",
                50.0
        );
        assertFalse(result, "Payment should fail with null student email");
    }

    @Test
    public void testProcessPaymentZeroAmount() {
        PaymentSystem paymentSystem = new MockPaymentSystem();
        boolean result = paymentSystem.processPayment(
                2,
                "Concert",
                "student@test.com",
                123456789,
                "ep@test.com",
                0.0
        );
        assertFalse(result, "Payment should fail with zero amount");
    }

    @Test
    public void testProcessRefundSuccess() {
        PaymentSystem paymentSystem = new MockPaymentSystem();
        boolean result = paymentSystem.processRefund(
                2,
                "Concert",
                "student@test.com",
                123456789,
                "ep@test.com",
                50.0,
                "Event cancelled"
        );
        assertTrue(result, "Refund should succeed with valid inputs");
    }

    @Test
    public void testProcessRefundNullEPEmail() {
        PaymentSystem paymentSystem = new MockPaymentSystem();
        boolean result = paymentSystem.processRefund(
                2,
                "Concert",
                "student@test.com",
                123456789,
                null,
                50.0,
                "Event cancelled"
        );
        assertFalse(result, "Refund should fail with null EP email");
    }
}