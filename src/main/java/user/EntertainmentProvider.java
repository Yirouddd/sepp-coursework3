package user;

import object.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Representing an entertainment provider with organisation details and events.
 */

public class EntertainmentProvider extends User {
    private String orgName;
    private String businessNumber;
    private String name;
    private String description;
    private List<Event> events;

    /**
     * Constructs an entertainment provider with the given details.
     *
     * @param email the provider's email
     * @param password the provider's password
     * @param orgName the organisation name
     * @param businessNumber the business registration number
     * @param name the main contact name
     * @param description the organisation description
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
     * @param event the event to add
     */
    public void addEvent(Event event) {
        if (event != null && !events.contains(event)) {
            events.add(event);
        }
    }

    /**
     * Returns the organization name
     *
     * @return the organisation name
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * Removes an event to the provider.
     *
     * @param event the event to remove
     */
    public void removeEvent(Event event) {
        events.remove(event);
    }

    /**
     * Returns the business registration number.
     *
     * @return the business number
     */
    public String getBusinessNumber() {
        return businessNumber;
    }

    /**
     * Returns the list of events.
     *
     * @return the events
     */
    public List<Event> getEvents() {
        return events;
    }
}