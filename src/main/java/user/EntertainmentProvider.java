package user;

import object.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing an entertainment provider.
 */

public class EntertainmentProvider extends User {
    private String orgName;
    private String businessNumber;
    private String name;
    private String description;

    private List<Event> events = new ArrayList<>();

    /**
     * Constructor for EntertainmentProvider.
     *
     * @param orgName         the name of the entertainment provider
     * @param businessNNumber the business number of the entertainment provider
     * @param name            the name of the entertainment provider
     * @param description     the description of the entertainment provider
     */
    public EntertainmentProvider(String email, String password, String orgName, String businessNumber, String name,
            String description) {
        this.orgName = orgName;
        this.businessNumber = businessNumber;
        this.name = name;
        this.description = description;
    }

    public void addEvent(Event event) {
        // Implementation for adding an event
        if (event != null) {
            events.add(event);
        }
    }

    // getter
    public String getOrgName() {
        return orgName;
    }
}