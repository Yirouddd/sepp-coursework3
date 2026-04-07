package systemTest;

import controller.BookingController;
import controller.EventPerformanceController;
import enums.EventType;
import external.MockPaymentSystem;
import interfaces.View;
import object.Booking;
import object.Event;
import object.Performance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*
  System tests for review performance use case.
 */
public class ReviewPerformanceSystemTest {

    private View mockView;
    private MockPaymentSystem paymentSystem;
    private EventPerformanceController eventController;
    private BookingController bookingController;

    private Student student1;
    private Student student2;
    private AdminStaff admin1;
    private EntertainmentProvider ep1;

    @BeforeEach
    void setUp() {
        mockView = Mockito.mock(View.class);
        paymentSystem = new MockPaymentSystem();
        eventController = new EventPerformanceController(mockView, paymentSystem);
        bookingController = new BookingController(mockView, paymentSystem, eventController);

        student1 = new Student("student1@test.com", "pass1", "name1", 111111);
        student2 = new Student("student2@test.com", "pass2", "name2", 222222);
        admin1 = new AdminStaff("admin1@test.com", "pass1", "name1");
        ep1 = new EntertainmentProvider("ep1@test.com", "pass1", "org1", "bn1", "name1", "desc1");
    }

    /*
      This checks the main success path.
      Student booked the past performance, gives rating and comment, and review is stored.
     */
    @Test
    void shouldAcceptReviewForBookedPastPerformanceWithComment() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1", "5", "comment1");

        bookingController.reviewPerformance();

        assertAll("A valid review with comment should be saved correctly",
                () -> assertEquals(1, performance1.getReviewsRatings().size(),
                        "One rating should be saved after successful review."),
                () -> assertTrue(performance1.getReviewsRatings().contains(5),
                        "Stored ratings should contain the submitted score 5."),
                () -> assertEquals(1, performance1.getReviewsComments().size(),
                        "One comment should be saved together with the rating."),
                () -> assertTrue(performance1.getReviewsComments().contains("comment1"),
                        "Stored comments should contain the submitted comment text."),
                () -> assertEquals(5.0, performance1.getAverageRating(), 0.0001,
                        "Average rating should match the only submitted review."));

        verify(mockView).displaySuccess("Review submitted successfully.");
        verify(mockView, never()).displayError("Only students can perform this action.");
    }

    /*
      Comment is optional in this use case.
      So empty comment should still work and should be stored as empty string.
     */
    @Test
    void shouldAcceptReviewWithRatingOnlyAndStoreEmptyComment() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1", "4", "");

        bookingController.reviewPerformance();

        assertAll("Rating-only review should still be accepted",
                () -> assertEquals(1, performance1.getReviewsRatings().size(),
                        "One rating should be stored when comment is left empty."),
                () -> assertTrue(performance1.getReviewsRatings().contains(4),
                        "Stored rating should match the submitted value."),
                () -> assertTrue(performance1.getReviewsComments().contains(""),
                        "Optional comment should be stored as empty string when user enters nothing."));

        verify(mockView).displaySuccess("Review submitted successfully.");
    }

    /*
      The controller keeps asking until performance id is a valid number and exists.
      This test checks one wrong text id first, then a missing numeric id, then the valid one.
     */
    @Test
    void shouldRetryWhenPerformanceIdIsTextOrUnknownThenAcceptValidId() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("abc", "99", "1", "3", "comment2");

        bookingController.reviewPerformance();

        assertAll("Controller should recover from wrong performance ids and still finish review",
                () -> assertEquals(1, performance1.getReviewsRatings().size(),
                        "A review should still be saved after the user corrects the performance id."),
                () -> assertTrue(performance1.getReviewsRatings().contains(3),
                        "The final valid rating should be stored."));

        verify(mockView).displayError("Please enter a valid numeric performance ID.");
        verify(mockView).displayError("Invalid performance ID.");
        verify(mockView).displaySuccess("Review submitted successfully.");
    }

    /*
      This checks the rating loop.
      User first gives text and then out of range values, then finally a valid rating.
     */
    @Test
    void shouldRetryWhenRatingIsNotNumericOrOutsideRange() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1", "bad", "0", "6", "2", "comment3");

        bookingController.reviewPerformance();

        assertAll("Controller should keep asking until rating is valid",
                () -> assertEquals(1, performance1.getReviewsRatings().size(),
                        "Exactly one review should be added after valid retry."),
                () -> assertTrue(performance1.getReviewsRatings().contains(2),
                        "The finally accepted rating should be stored."),
                () -> assertTrue(performance1.getReviewsComments().contains("comment3"),
                        "Comment should be stored after a valid rating is finally entered."));

        verify(mockView).displayError("Rating must be a number.");
        verify(mockView, times(2)).displayError("Rating must be between 1 and 5.");
        verify(mockView).displaySuccess("Review submitted successfully.");
    }

    /*
      A performance in future should not be reviewed yet.
      Even if the same student booked it before, the controller should stop the review.
     */
    @Test
    void shouldRejectReviewForFuturePerformanceEvenIfBookedByStudent() {
        Performance performance1 = createPerformance(1L, false, false);
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1");

        bookingController.reviewPerformance();

        assertAll("Future performance should not accept a review",
                () -> assertTrue(performance1.getReviewsRatings().isEmpty(),
                        "No rating should be stored for a performance that has not happened yet."),
                () -> assertTrue(performance1.getReviewsComments().isEmpty(),
                        "No comment should be stored when review is rejected."));

        verify(mockView).displayError("You can only review a performance after it has happened.");
        verify(mockView, never()).displaySuccess("Review submitted successfully.");
    }

    /*
      Student must really own the booking.
      If another student booked it, review must be rejected.
     */
    @Test
    void shouldRejectReviewForPastPerformanceBookedByAnotherStudent() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student2, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1");

        bookingController.reviewPerformance();

        assertAll("Student should not review another student's booking",
                () -> assertTrue(performance1.getReviewsRatings().isEmpty(),
                        "No rating should be stored when current user did not book this performance."),
                () -> assertTrue(performance1.getReviewsComments().isEmpty(),
                        "No comment should be stored when current user did not book this performance."));

        verify(mockView).displayError("You can only review a performance you booked.");
    }

    /*
      Same student but the booking is not active anymore.
      The controller only accepts an active booking for review.
     */
    @Test
    void shouldRejectReviewWhenStudentsBookingIsNoLongerActive() {
        Performance performance1 = createPerformance(1L, true, false);
        seedBooking(student1, performance1, 1L, false);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1");

        bookingController.reviewPerformance();

        assertAll("Cancelled booking should not allow review",
                () -> assertTrue(performance1.getReviewsRatings().isEmpty(),
                        "No rating should be stored when the matching booking is not active."),
                () -> assertTrue(performance1.getReviewsComments().isEmpty(),
                        "No comment should be stored when the matching booking is not active."));

        verify(mockView).displayError("You can only review a performance you booked.");
    }

    /*
      Only students can use this entry point.
      Admin user should be blocked before any review logic starts.
     */
    @Test
    void shouldRejectReviewWhenCurrentUserIsNotStudent() {
        bookingController.setCurrentUser(admin1);

        bookingController.reviewPerformance();

        verify(mockView).displayError("Only students can perform this action.");
        verify(mockView, never()).displaySuccess("Review submitted successfully.");
        verify(mockView, never()).getInput(anyString());
    }

    /*
      This test checks review state integration a bit more.
      If there is already one review, a new one should change the average correctly.
     */
    @Test
    void shouldUpdateAverageRatingWhenNewReviewIsAddedToExistingReviews() {
        Performance performance1 = createPerformance(1L, true, false);
        performance1.review(4, "old1");
        seedBooking(student1, performance1, 1L, true);
        bookingController.setCurrentUser(student1);

        when(mockView.getInput(anyString())).thenReturn("1", "2", "comment4");

        bookingController.reviewPerformance();

        assertAll("Average rating should include the newly submitted review",
                () -> assertEquals(2, performance1.getReviewsRatings().size(),
                        "There should be two ratings after the new review is added."),
                () -> assertEquals(2, performance1.getReviewsComments().size(),
                        "There should be two comments after the new review is added."),
                () -> assertEquals(3.0, performance1.getAverageRating(), 0.0001,
                        "Average rating should be recalculated from old and new reviews."),
                () -> assertTrue(performance1.getReviewsComments().contains("comment4"),
                        "New comment should appear in the performance review records."));

        verify(mockView).displaySuccess("Review submitted successfully.");
    }

    /**
     * Creates a Performance instance and configures it as past/future and cancelled/not cancelled based on parameters
     *
     * @param performanceId unique identifier for the performance
     * @param isPast whether the performance is in the past (true = past, false = future)
     * @param isCancelled whether to mark the performance as cancelled
     * @return the created and configured Performance object
     */
    private Performance createPerformance(long performanceId, boolean isPast, boolean isCancelled) {
        Event event1 = new Event(1L, "event1", EventType.Music, true);
        event1.setOrganizer(ep1);

        LocalDateTime start = isPast ? LocalDateTime.now().minusDays(2) : LocalDateTime.now().plusDays(2);
        LocalDateTime end = isPast ? LocalDateTime.now().minusDays(2).plusHours(2) : LocalDateTime.now().plusDays(2).plusHours(2);

        Performance performance1 = event1.createPerformance(
                performanceId,
                start,
                end,
                List.of("name1"),
                "venue1",
                100,
                false,
                false,
                50,
                10.0
        );

        if (isCancelled) {
            performance1.cancel();
        }

        eventController.addEvent(event1);
        eventController.addPerformance(performance1);
        return performance1;
    }

    /**
     * Seeds a booking record for a student and performance with the specified status
     *
     * @param student the student making the booking
     * @param performance the performance being booked
     * @param bookingNumber unique booking number
     * @param isActive whether the booking is active (true) or cancelled by student (false)
     */
    private void seedBooking(Student student, Performance performance, long bookingNumber, boolean isActive) {
        Booking booking1 = new Booking(
                student,
                performance,
                bookingNumber,
                1,
                performance.getFinalTicketPrice(),
                LocalDateTime.now().minusDays(3)
        );

        if (!isActive) {
            booking1.cancelByStudent();
        }

        student.addBooking(booking1);
        performance.addBooking(booking1);
        injectBookingIntoBookingController(booking1);
    }

    /**
     * Injects a Booking object directly into BookingController's internal collection using reflection.
     * This bypasses normal controller methods and is used only for test setup purposes.
     *
     * @param booking the Booking object to inject into the controller
     * @throws AssertionError (via fail()) if reflection fails, causing test setup to abort
     */
    @SuppressWarnings("unchecked")
    private void injectBookingIntoBookingController(Booking booking) {
        try {
            Field bookingsField = BookingController.class.getDeclaredField("bookings");
            bookingsField.setAccessible(true);
            ((java.util.Collection<Booking>) bookingsField.get(bookingController)).add(booking);
        } catch (ReflectiveOperationException e) {
            fail("Test setup failed because bookings could not be inserted into BookingController: " + e.getMessage());
        }
    }
}
