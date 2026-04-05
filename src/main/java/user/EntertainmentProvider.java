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
    private List<Event> events;

    /**
     * Constructs an entertainment provider.
     *
     * @param email provider email
     * @param password provider password
     * @param orgName organisation name
     * @param businessNumber business registration number
     * @param name main contact name
     * @param description organisation description
     */
    public EntertainmentProvider(String email, String password, String orgName,
                                 String businessNumber, String name, String description) {
        this.orgName = orgName;
        this.businessNumber = businessNumber;
        this.name = name;
        this.description = description;
        this.events = new ArrayList<>();
        setEmail(email);
        setPassword(password);
    }

    /**
     * Adds an event to the provider.
     *
     * @param event event to add
     */
    public void addEvent(Event event) {
        if (event != null && !events.contains(event)) {
            events.add(event);
        }
    }

    /**
     * Gets the organization name
     *
     * @return orgName organisation name
     */
    public String getOrgName() {
        return orgName;
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public List<Event> getEvents() {
        return events;
    }
}