package test;

import object.Event;
import object.Performance;
import enums.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class TestEventPerformance {

    @Test
    public void testEventCreation() {
        // Create an event
        Event event = new Event();
        // Check it exists
        assertNotNull(event);
    }

    @Test
    public void testEventTitle() {
        Event event = new Event();
        // TODO: Once Event has setTitle method:
        // event.setTitle("Test Event");
        // assertEquals("Test Event", event.getTitle());
    }
}
