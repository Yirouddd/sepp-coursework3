
public class EventPerformanceController{

    private long nextEventID;
    private long nextPerformanceID;

    /**
     * Constructs an EventPerformanceController.
     */
    public EventPerformanceController() {
        // TODO
    }

    /** Creates a new event. @return the created Event */
    public Event createEvent() {
        // TODO
        return null;
    }

    /** Searches for performances on a given date. */
    public void searchForPerformances() {
        // TODO
    }

    /** Views details of a specific performance. */
    public void viewPerformance() {
        // TODO
    }

    /** Cancels a performance. */
    public void cancelPerformance() {
        // TODO
    }

    /**
     * Checks if sponsorship is possible for a performance.
     * @param performance the performance to check
     * @param amount the sponsorship amount
     * @return true if sponsorship is possible
     */
    public boolean checkIfSponsorshipPossible(Performance performance, int amount) {
        // TODO
        return false;
    }

    /** Sponsors a performance. */
    public void sponsorPerformance() {
        // TODO
    }

    /** @param e the event to add */
    private void addEvent(Event e) {
        // TODO
    }

    /** @param p the performance to add */
    private void addPerformance(Performance p) {
        // TODO
    }

    /** @param eventID the event ID @return matching Event */
    private Event getEventByID(long eventID) {
        // TODO
        return null;
    }

    /** @param title the event title @return matching Event */
    private Event getEventByTitle(String title) {
        // TODO
        return null;
    }

    /** @param performanceID the performance ID @return matching Performance */
    private Performance getPerformanceByID(long performanceID) {
        // TODO
        return null;
    }
}