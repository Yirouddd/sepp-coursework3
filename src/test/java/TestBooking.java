package test;

import object.Booking;
import enums.BookingStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class TestBooking {

    @Test
    public void testBookingCreation() {
        Booking booking = new Booking(
                1,           // booking number
                2,           // num tickets
                50.0,        // amount paid
                LocalDateTime.now(),  // booking date/time
                BookingStatus.ACTIVE  // status
        );

        assertNotNull(booking);
        assertEquals(1, booking.getBookingNumber());  // TODO: add getBookingNumber() to Booking
    }
}