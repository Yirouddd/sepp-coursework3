package test;

import controller.EventPerformanceController;
import interfaces.View;
import object.Event;
import user.EntertainmentProvider;
import user.User;
import test.MockView;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CreateEventSystemTest {

    @Test
    public void testCreateEventSuccess() {
        User ep = new EntertainmentProvider("ep@test.com", "password", "My Org", "12345", "John", "Concert");
        MockView mockView = new MockView();
        EventPerformanceController controller = new EventPerformanceController(ep, mockView);

        mockView.addInput("Summer Concert");           // title
        mockView.addInput("Music");                    // type
        mockView.addInput("yes");                      // ticketed
        mockView.addInput("yes");                      // add performance? YES
        mockView.addInput("2025-06-01 20:00");         // start date/time
        mockView.addInput("2025-06-01 22:00");         // end date/time
        mockView.addInput("Concert Hall");             // venue address
        mockView.addInput("500");                      // capacity
        mockView.addInput("no");                       // outdoors
        mockView.addInput("no");                       // smoking
        mockView.addInput("John Smith");               // performer names
        mockView.addInput("25.50");                    // ticket price
        mockView.addInput("100");                      // num tickets
        mockView.addInput("no");                       // add another performance? NO

        Event result = controller.createEvent();

        assertNotNull(result);
    }
}