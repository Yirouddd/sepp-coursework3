package unitTest;

import enums.EventType;
import enums.PerformanceStatus;
import object.Booking;
import object.Performance;
import object.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import user.EntertainmentProvider;
import user.Student;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Performance class.
 * <p>
 * Includes testing for performance status, ticket reductions, increases and
 * prices, sponsorship, reviews, adding and removing booking.
 */
public class PerformanceTest {
    private Performance ticketedPerformance;
    private Performance ticketedPerformance2;
    private Performance nonTicketedPerformance;
    private Event ticketedEvent;
    private Event nonTicketedEvent;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setup() {
        EntertainmentProvider ep = new EntertainmentProvider("ep1@ed.ac.uk",
                "epPassword", "Org Name", "BN007", "Name", "description");

        ticketedEvent = new Event(1, "Concert", EventType.Music, true);
        ticketedEvent.setOrganizer(ep);

        nonTicketedEvent = new Event(2, "Sport Competition", EventType.Sports
                , false);
        nonTicketedEvent.setOrganizer(ep);

        ticketedPerformance = ticketedEvent.createPerformance(1,
                LocalDateTime.of(2026, 6, 10, 19, 0),
                LocalDateTime.of(2026, 6, 10, 21, 0),
                List.of("Band A"),
                "Venue 1",
                100,
                false,
                false,
                50,
                15.0);

        ticketedPerformance2 = ticketedEvent.createPerformance(3,
                LocalDateTime.of(2026, 4, 10, 19, 0),
                LocalDateTime.of(2026, 4, 10, 21, 0),
                List.of("Band И"),
                "Venue 2",
                100,
                false,
                false,
                50,
                20.0);

        nonTicketedPerformance = nonTicketedEvent.createPerformance(2,
                LocalDateTime.of(2026, 2, 10, 19, 0),
                LocalDateTime.of(2026, 2, 10, 22, 0),
                List.of("Football match"),
                "Pitch 1",
                200,
                false,
                false,
                50,
                0.0);

        student1 = new Student("stud1@ed.ac.uk", "studPass1", "Name 1",
                123456789);
        student2 = new Student("stud2@ed.ac.uk", "studPass2", "Name 2",
                123456780);
    }

    // Performance status

    /*
      Test that cancelling a performance sets status to cancelling.
     */
    @Test
    void cancelSetsStatusToCancelled() {
        ticketedPerformance.cancel();
        assertEquals(PerformanceStatus.CANCELLED, ticketedPerformance.getStatus());
        assertEquals(PerformanceStatus.ACTIVE, ticketedPerformance2.getStatus());
    }

    /*
      Test that cancelling twice keeps status CANCELLED.
     */
    @Test
    void cancelTwiceDoesNotChangePerformanceStatus() {
        ticketedPerformance.cancel();
        ticketedPerformance.cancel();
        assertEquals(PerformanceStatus.CANCELLED, ticketedPerformance.getStatus());
    }

    // Ticketed / Non-ticketed event

    /*
      Test that ticketed performance is identified appropriately.
     */
    @Test
    void checkIfEventIsTicketed_True() {
        assertTrue(ticketedPerformance.checkIfEventIsTicketed());
        assertTrue(ticketedPerformance2.checkIfEventIsTicketed());
    }

    /*
      Test that non-ticketed performance is identified appropriately.
     */
    @Test
    void checkIfEventIsTicketed_False() {
        assertFalse(nonTicketedPerformance.checkIfEventIsTicketed());
    }

    // Enough tickets

    /*
      Test tickets left logic for sufficient tickets quantity.
     */
    @Test
    void checkIfTicketsLeft_true() {
        assertTrue(ticketedPerformance.checkIfTicketsLeft(10));
    }

    /*
      Test tickets left logic when tickets quantity requested exceeds
      available.
     */
    @Test
    void checkIfTicketsLeft_false_ticketsExceed() {
        assertFalse(ticketedPerformance.checkIfTicketsLeft(100));
    }

    /*
      Test tickets left logic at boundaries.
     */
    @Test
    void checkIfTicketsLeft_false_boundaries() {
        assertTrue(ticketedPerformance.checkIfTicketsLeft(50));
        assertTrue(ticketedPerformance.checkIfTicketsLeft(0), "0 tickets " +
                "requested should always return true");
    }

    // Final ticket price

    /*
      Test that final ticket price is returned correctly when no sponsorship
      was applied.
     */
    @Test
    void getFinalTicketPrice_noSponsorship() {
        assertEquals(15.0, ticketedPerformance.getFinalTicketPrice());
        assertEquals(20.0, ticketedPerformance2.getFinalTicketPrice());
        assertEquals(0.0, nonTicketedPerformance.getFinalTicketPrice());
    }

    /*
      Test that final ticket price is returned correctly when a sponsorship
      was applied to one performance.
     */
    @Test
    void getFinalTicketPrice_withSponsorship() {
        ticketedPerformance.sponsor(5);
        // sponsored
        assertEquals(10.0, ticketedPerformance.getFinalTicketPrice());
        // not sponsored, should not be affected
        assertEquals(20.0, ticketedPerformance2.getFinalTicketPrice());
    }

    /*
      Test that final ticket price is returned correctly when sponsorship
      failed due to exceeding amount.
     */
    @Test
    void getFinalTicketPrice_failedSponsoredNegativeFinal() {
        ticketedPerformance.sponsor(50);
        assertEquals(0.0, ticketedPerformance.getFinalTicketPrice());
    }

    // Performance's times

    /*
      Test that performance identifies future timing correctly.
     */
    @Test
    void checkHasNotHappenedYet_true() {
        assertTrue(ticketedPerformance.checkHasNotHappenedYet());
    }

    /*
      Test that performance identifies past timing correctly.
     */
    @Test
    void checkHasNotHappenedYet_pastPerformanceReturnsFalse() {
        assertFalse(nonTicketedPerformance.checkHasNotHappenedYet());
    }

    /*
      Test that performance identifies almost current timing correctly.
     */
    @Test
    void checkHasNotHappenedYet_currentTimeReturnsFalse() {
        Performance performanceNow = ticketedEvent.createPerformance(15,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(2), List.of("Now Band Now"),
                "Venue HereNow", 100, false, false, 50, 10.0);
        assertFalse(performanceNow.checkHasNotHappenedYet());
    }

    // Created by EP

    /*
      Test that the method for checking whether the performance is created by EP.
     */
    @Test
    void checkCreatedByEP_returnTrue() {
        assertTrue(ticketedPerformance.checkCreatedByEP("ep1@ed.ac.uk"));
        assertFalse(ticketedPerformance.checkCreatedByEP("ep2@ed.ac.uk"));

    }

    // Sponsorship

    /*
      Test sponsorship throws exception for non-ticketed performance.
     */
    @Test
    void sponsor_nonTicketedThrows() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> nonTicketedPerformance.sponsor(10));
        assertEquals("Sponsorship cannot be applied to non-ticketed " +
                "performances.", e.getMessage());
    }

    /*
      Test sponsorship throws exception for negative sponsorship amount.
     */
    @Test
    void sponsor_negativeAmountThrows() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ticketedPerformance.sponsor(-5));
        assertEquals("Sponsorship must be positive.", e.getMessage());
    }

    /*
      Test sponsorship exceeding ticket price sets the final ticket price to
      zero but does not go negative.
     */
    @Test
    void sponsor_amountExceedsTicketPrice_shouldBeZero() {
        ticketedPerformance.sponsor(100);
        assertEquals(0.0, ticketedPerformance.getFinalTicketPrice());
    }

    /*
      Test that multiple sponsorships apply correctly.
     */
    @Test
    void multipleSponsorshipsDoneCorrectly() {
        ticketedPerformance.sponsor(5);
        ticketedPerformance.sponsor(4);
        assertEquals(9.0, ticketedPerformance.getSponsoredAmount());
        assertEquals(6.0, ticketedPerformance.getFinalTicketPrice());
    }

    /*
      Test fractional sponsorship is rounded correctly.
     */
    @Test
    void sponsorshipRoundsCorrectly() {
        ticketedPerformance.sponsor(2.35);
        assertEquals(12.65, ticketedPerformance.getFinalTicketPrice(), 0.01);
        ticketedPerformance.sponsor(2.15);
        assertEquals(10.5, ticketedPerformance.getFinalTicketPrice(), 0.01);
    }

    /*
      Test sponsorship reduces ticket price accordingly.
     */
    @Test
    void sponsorshipReducesTicketPriceCorrectly() {
        ticketedPerformance.sponsor(2);
        assertEquals(2, ticketedPerformance.getSponsoredAmount());
        assertEquals(13.0, ticketedPerformance.getFinalTicketPrice());
    }

    // Reviews

    /*
      Test that invalid rating throws exception.
     */
    @Test
    void review_invalidRatingThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ticketedPerformance.review(0, "Too bad"));
        assertThrows(IllegalArgumentException.class,
                () -> ticketedPerformance.review(6, "Awesome, loved it"));
    }

    /*
      Test that null comment (empty) defaults to empty string.
     */
    @Test
    void review_nullCommentDefaultEmpty() {
        ticketedPerformance.review(5, null);
        assertEquals("",
                ticketedPerformance.getReviewsComments().iterator().next());
    }

    /*
      Test valid reviews adds rating and comment.
     */
    @Test
    void review_validInputAddsRatingAndComment() {
        ticketedPerformance.review(4, "Awesome");
        // check sizes
        assertEquals(1, ticketedPerformance.getReviewsRatings().size());
        assertEquals(1, ticketedPerformance.getReviewsComments().size());
        // check values
        assertEquals(4,
                ticketedPerformance.getReviewsRatings().iterator().next());
        assertEquals("Awesome",
                ticketedPerformance.getReviewsComments().iterator().next());
        // check average
        assertEquals(4.0, ticketedPerformance.getAverageRating());
    }

    /*
      Test that multiple reviews calculate average correctly.
     */
    @Test
    void review_multipleReviews_calculateAverageCorrectly() {
        ticketedPerformance.review(5, "Very enjoyable!!");
        ticketedPerformance.review(4, "Good");
        ticketedPerformance.review(3, "Fine");
        assertEquals(4.0, ticketedPerformance.getAverageRating());
    }

    // Bookings

    /*
      Test adding null booking throws an exception.
     */
    @Test
    void addBooking_nullThrows() {
        Exception e = assertThrows(IllegalArgumentException.class,
                () -> ticketedPerformance.addBooking(null));
        assertEquals("Booking cannot be null.", e.getMessage());
    }

    /*
      Test adding booking to non-ticketed performance throws an exception
      and displays an appropriate message.
     */
    @Test
    void addBooking_bookNonTicketedThrows() {
        Booking booking = new Booking(student1, nonTicketedPerformance, 1002, 2,
                30.0, LocalDateTime.now());
        Exception e = assertThrows(IllegalStateException.class,
                () -> nonTicketedPerformance.addBooking(booking));
        assertEquals("Cannot book a non-ticketed performance.", e.getMessage());
    }

    /*
      Test that adding booking with not enough tickets left throws an
      exception and displays an appropriate message saying that not enough
      tickets left.
     */
    @Test
    void addBooking_notEnoughTicketsThrows() {
        Booking booking = new Booking(student1, nonTicketedPerformance, 1002,
                60,
                900.0, LocalDateTime.now());
        Exception e = assertThrows(IllegalStateException.class,
                () -> ticketedPerformance.addBooking(booking));
        assertEquals("Not enough tickets left.", e.getMessage());
    }

    /*
      Test that multiple bookings reduce tickets correctly and changes state
      to has active bookings.
     */
    @Test
    void addBooking_reducesTicketsLeftAndAddsActiveBookings() {
        Booking booking = new Booking(student1, nonTicketedPerformance, 1000,
                2,
                30.0, LocalDateTime.now());
        ticketedPerformance.addBooking(booking);
        assertEquals(48, ticketedPerformance.getTicketsLeft());
        assertTrue(ticketedPerformance.hasActiveBooking());
    }

    /*
      Test that multiple bookings reduce tickets correctly.
     */
    @Test
    void addBooking_multipleBookingsReducesTickets() {
        Booking booking1 = new Booking(student1, nonTicketedPerformance, 1000,
                2,
                30.0, LocalDateTime.now());
        Booking booking2 = new Booking(student2, nonTicketedPerformance, 1005,
                4,
                60.0, LocalDateTime.now());
        ticketedPerformance.addBooking(booking1);
        ticketedPerformance.addBooking(booking2);
        assertEquals(44, ticketedPerformance.getTicketsLeft());
    }

    /*
      Test that remove booking increases number of tickets left and changes
      active status to false.
     */
    @Test
    void removeBooking_increasesTicketsLeft() {
        Booking booking = new Booking(student1, nonTicketedPerformance, 1000,
                2,
                30.0, LocalDateTime.now());
        ticketedPerformance.addBooking(booking);
        ticketedPerformance.removeBooking(booking);
        assertEquals(50, ticketedPerformance.getTicketsLeft());
        assertFalse(ticketedPerformance.hasActiveBooking());
    }
}