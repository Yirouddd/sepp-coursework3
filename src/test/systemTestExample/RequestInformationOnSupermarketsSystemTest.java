import command.*;
import controller.Controller;
import logging.Logger;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RequestSupermarketInfoSystemTest {

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println(testInfo.getDisplayName());
    }

    @AfterEach
    void clearLogs() {
        Logger.getInstance().clearLog();
        System.out.println("---");
    }

    private static void registerShieldingIndividual(Controller controller) {
        controller.runCommand(new RegisterShieldingIndividualCommand("1234567890"));
    }

    private static void loginShieldingIndividual(Controller controller) {
        controller.runCommand(new LoginCommand("shielder@example.com", "shielderPass1"));
    }

    // Extension 1a.1: Individual placed a food box order less than a week ago.
    // System should return false, indicating the individual must wait before placing another order.
    @Test
    void requestSupermarketInfoFailsWhenOrderPlacedLessThanWeekAgo() {
        Controller controller = new Controller();

        // Register and log in the shielding individual
        registerShieldingIndividual(controller);
        loginShieldingIndividual(controller);

        // Place an initial food box order
        PlaceFoodBoxOrderCommand firstOrderCmd = new PlaceFoodBoxOrderCommand(1);
        controller.runCommand(firstOrderCmd);

        // Attempt to request supermarket info again within the same week
        // (i.e., without a week having elapsed since the last order)
        RequestSupermarketInfoCommand cmd = new RequestSupermarketInfoCommand();
        controller.runCommand(cmd);

        // The result should be false because less than a week has passed since last order
        assertFalse(cmd.getResult(),
            "Expected false: shielding individual already placed an order less than a week ago");
    }
}
