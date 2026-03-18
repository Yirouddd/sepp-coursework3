package user;

/**
 * A class representing an entertainment provider.
 */

public class EntertainmentProvider {
    private String orgName;
    private String businessNNumber;
    private String name;
    private String description;

    /**
     * Constructor for EntertainmentProvider.
     *
     * @param orgName the name of the entertainment provider
     * @param businessNNumber the business number of the entertainment provider
     * @param name the name of the entertainment provider
     * @param description the description of the entertainment provider
     */
    public EntertainmentProvider(String orgName, String businessNNumber, String name, String description) {
        this.orgName = orgName;
        this.businessNNumber = businessNNumber;
        this.name = name;
        this.description = description;
    }

    public void addEvent(Event event) {
        // Implementation for adding an event
    }
}