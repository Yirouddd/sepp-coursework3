import interfaces.View;
import enums.EventType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class EventPerformanceController extends Controller {

    private long nextEventID;
    private long nextPerformanceID;

    private List<Event> events;
    private List<Performance> performances;

    public EventPerformanceController(User currentUser, View view) {
        super(currentUser, view);
        this.nextEventID = 1;
        this.nextPerformanceID = 1;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
    }

    public Event createEvent() {
        //1. get event title from EP
        String title = view.getInput("Enter event title: ");

        //checks if title is empty, keep looping until valid input is received
        while (title == null || title.trim().isEmpty()) {
            view.displayError("Title cannot be empty.");
            title = view.getInput("Enter event title: ");
        }

        //2. check if event title already exists, reject if it exists
        if (getEventByTitle(title) != null) {
            view.displayError("An event with this title already exists.");
            return null; // exit method and do not create a new event
        }


        //3. get event type from user
        String typeInput = view.getInput("Enter event type (Music, Theatre, Dance, Movie, Sports): ");
        EventType type;

        // convert string to EventType enum (parsing input)
        try {
            type = EventType.valueOf(typeInput.toUpperCase());
        } catch (IllegalArgumentException e) {
            //If input is invalid, reject it
            view.displayError("Invalid event type. Use: Music, Theatre, Dance, Movie, Sports");
            return null;
        }

        //4. get ticketed flag from EP (is this event ticketed? yes/no)
        String ticketedInput = view.getInput("Is this event ticketed (yes/no): ");
        boolean isTicketed;

        //5. Convert yes/no to boolean
        if (ticketedInput.equalsIgnoreCase("yes") || ticketedInput.equalsIgnoreCase("y")) {
            isTicketed = true;
        } else if (ticketedInput.equalsIgnoreCase("no") || ticketedInput.equalsIgnoreCase("n")) {
            isTicketed = false;
        } else {
            view.displayError("Please enter 'yes' or 'no'.");
            return null;
        }

        //step 5: Create the Event object
        //now we have all the info, create a new Event
        //give it a unique ID (eventID)
        long eventID = nextEventID++;
        Event event = new Event(); //create empty event

        //TODO: set fields (title, type, isTicketed, eventID)
        // !!Event class needs a constructor or setter methods

        //6. Get performances from user
        int performanceCount = 0;
        String addMore = view.getInput("Add a performance? (yes/no): ");

        while (addMore.equalsIgnoreCase("yes") || addMore.equalsIgnoreCase("y")) {
            // getting performance details

            String startInput = view.getInput("Enter start date/time (yyyy-MM-dd HH:mm): ");
            LocalDateTime startDateTime;
            try {
                DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                startDateTime = LocalDateTime.parse(startInput, formatter);
            } catch (Exception e) {
                view.displayError("Invalid date format. Use: yyyy-MM-dd HH:mm");
                continue; //skip this performance, ask again
            }

            //Get end date/time
            String endInput = view.getInput("Enter end date/time (yyyy-MM-dd HH:mm): ");
            LocalDateTime endDateTime;
            try {
                endDateTime = LocalDateTime.parse(endInput);
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            } catch (Exception e) {
                view.displayError("Invalid date format. Use: yyyy-MM-dd HH:mm");
                continue;  // Skip this performance, ask again
            }

            //validate: end time must be after start time
            if (!endDateTime.isAfter(startDateTime)) {
                view.displayError("End time must be after start time.");
                continue;
            }

            //get venue address
            String venueAddress = view.getInput("Enter venue address: ");
            if (venueAddress == null || venueAddress.trim().isEmpty()) {
                view.displayError("Venue address cannot be empty.");
                continue;
            }

            // get performer names (before creating performers list)
            String performerNames = view.getInput("Enter performer name(s): ");
            if (performerNames == null || performerNames.trim().isEmpty()) {
                view.displayError("Performer name cannot be empty.");
                continue;
            }

            //get venue capacity
            String capacityInput = view.getInput("Enter venue capacity: ");
            int capacity;
            try {
                capacity = Integer.parseInt(capacityInput);
                if (capacity <= 1) {
                    view.displayError("Capacity must be at least 1.");
                    continue;
                }
            } catch (NumberFormatException e) { //handles edge cases
                view.displayError("Capacity must be a number.");
                continue;
            }

            // Get if outdoors
            String outdoorsInput = view.getInput("Is venue outdoors? (yes/no): ");
            boolean isOutdoors = outdoorsInput.equalsIgnoreCase("yes") || outdoorsInput.equalsIgnoreCase("y");

            // Get if smoking allowed
            String smokingInput = view.getInput("Smoking allowed? (yes/no): ");
            boolean allowsSmoking = smokingInput.equalsIgnoreCase("yes") || smokingInput.equalsIgnoreCase("y");

            // If ticketed, get ticket info
            int numTickets = 0;
            double ticketPrice = 0.0;

            if (isTicketed) {
                // Get number of tickets
                String ticketsInput = view.getInput("Enter number of tickets: ");
                try {
                    numTickets = Integer.parseInt(ticketsInput);
                    if (numTickets < 1) {
                        view.displayError("Must have at least 1 ticket.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    view.displayError("Tickets must be a number.");
                    continue;
                }

                // Get ticket price
                String priceInput = view.getInput("Enter ticket price (£): ");
                try {
                    ticketPrice = Double.parseDouble(priceInput);
                    if (ticketPrice < 0) {
                        view.displayError("Price cannot be negative.");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    view.displayError("Price must be a number.");
                    continue;
                }
            }

            //creating Performance object
            long performanceID = nextPerformanceID++; // to give a unique ID
            Collection<String> performers = new ArrayList<>();
            performers.add(performerNames);

            Performance performance = new Performance(
                    performanceID,
                    startDateTime,
                    endDateTime,
                    performers,
                    venueAddress,
                    capacity
            );

            //TODO: Set remaining fields on performance (outdoors, smoking, tickets, price, status)
            // Add this performance to the event
            event.addPerformance(performance);
            performances.add(performance);
            performanceCount++;

            // ask if they want to add another another performance
            addMore = view.getInput("Add another performance? (yes/no): ");
        }

        // validate at least 1 performance
        if (performanceCount == 0) {
            view.displayError("Event must have at least one performance.");
            return null;
        }

        //store event
        addEvent(event);

        //display success message
        view.displaySuccess("Event '" + title + "' created successfully with " +
                            performanceCount + "performance(s).");
        return event;
        }



    public void searchforPerformances() {


    }

    public void viewPerformance() {

    }

    public void cancelPerformance() {

    }

    private boolean checkIfSponsorshipPossible(Performance performance, int amount) {

        return false;
    }

    public void sponsorPerformance() {

    }

    private void addEvent(Event e) {
        if (e != null) {
            events.add(e);
        }
    }

    private void addPerformance(Performance p) {

    }

    private Event getEventByID(long eventID) {
        for (Event e : events) {
            if (e.getEventID() == eventID) {
                return e;
            }
        }
        return null;
    }

    private Event getEventByTitle(String title) {
        if (title == null) return null;
        for (Event e : events) {
            if (e.getTitle().equalsIgnoreCase(title)) {
                return e;
            }
        }
        return null;
    }

    private Performance getPerformanceByID(long performanceID) {
        for (Performance p : performances) {
            if (p.getID() == performanceID) {
                return p;
            }
        }
        return null;
    }
}