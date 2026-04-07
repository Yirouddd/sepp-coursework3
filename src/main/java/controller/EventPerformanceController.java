package controller;

import enums.BookingStatus;
import enums.EventType;
import enums.PerformanceStatus;
import external.MockPaymentSystem;
import external.PaymentSystem;
import interfaces.View;
import object.Booking;
import user.EntertainmentProvider;
import user.Student;
import user.StudentPreferences;
import user.User;
import object.Event;
import object.Performance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Handles event and performance related use cases.
 */
public class EventPerformanceController extends Controller {
    private long nextEventID;
    private long nextPerformanceID;

    private Collection<Event> events;
    private Collection<Performance> performances;

    private PaymentSystem paymentSystem;

    /**
     * Constructs the controller.
     *
     * @param view UI view
     */
    public EventPerformanceController(View view, PaymentSystem paymentSystem) {
        this.nextEventID = 1;
        this.nextPerformanceID = 1;
        this.view = view;
        this.events = new ArrayList<>();
        this.performances = new ArrayList<>();
        this.paymentSystem = paymentSystem;
    }

    /**
     * Creates an event and one or more performances.
     *
     * @return created event or null
     */
    public Event createEvent() {
        if (!checkCurrentUserIsEntertainmentProvider()) {
            view.displayError("Only entertainment providers can create events.");
            return null;
        }

        EntertainmentProvider provider = (EntertainmentProvider) currentUser;

        String eventTitle = view.getInput("Enter event title: ").trim();
        if (eventTitle.isEmpty()) {
            view.displayError("Event title cannot be empty.");
            return null;
        }

        EventType eventType = promptEventType();
        boolean isTicketed = promptYesNo("Is the event ticketed? (yes/no): ");

        Event event = new Event(nextEventID++, eventTitle, eventType, isTicketed);
        event.setOrganizer(provider);

        int performanceCount = 0;

        while (true) {
            try {
                long performanceId = promptUniquePerformanceId();

                LocalDateTime startDateTime =
                        promptDateTime("Enter performance start date/time (yyyy-MM-dd HH:mm): ");
                LocalDateTime endDateTime =
                        promptDateTime("Enter performance end date/time (yyyy-MM-dd HH:mm): ");

                if (!endDateTime.isAfter(startDateTime)) {
                    view.displayError("End date/time must be after start date/time.");
                } else if (event.hasPerformanceAtSameTimes(startDateTime, endDateTime)) {
                    view.displayError("This event already has a performance at overlapping times.");
                } else if (providerHasSameTitleAndOverlappingTimes(provider, eventTitle, startDateTime, endDateTime)) {
                    view.displayError("An event with this title already exists for overlapping dates/times.");
                } else {
                    Collection<String> performerNames = promptPerformerNames();

                    String venueAddress = view.getInput("Enter venue address: ").trim();
                    if (venueAddress.isEmpty()) {
                        view.displayError("Venue address cannot be empty.");
                    } else {
                        int venueCapacity = promptPositiveInt("Enter venue capacity: ");
                        boolean venueIsOutdoors = promptYesNo("Is the venue outdoors? (yes/no): ");
                        boolean venueIsSmoking = promptYesNo("Is smoking allowed? (yes/no): ");

                        int numTicketsTotal = 0;
                        double ticketPrice = 0.0;

                        if (isTicketed) {
                            numTicketsTotal = promptPositiveInt("Enter number of tickets available: ");
                            ticketPrice = promptNonNegativeDouble("Enter ticket price: ");
                        }

                        Performance performance = event.createPerformance(
                                performanceId,
                                startDateTime,
                                endDateTime,
                                performerNames,
                                venueAddress,
                                venueCapacity,
                                venueIsOutdoors,
                                venueIsSmoking,
                                numTicketsTotal,
                                ticketPrice
                        );

                        addPerformance(performance);
                        performanceCount++;
                        view.displaySuccess("Performance created successfully with ID " + performance.getPerformanceId());
                    }
                }
            } catch (IllegalArgumentException e) {
                view.displayError(e.getMessage());
            }

            boolean addAnother = promptYesNo("Add another performance to this event? (yes/no): ");
            if (!addAnother) {
                break;
            }
        }

        if (performanceCount == 0) {
            view.displayError("At least one valid performance must be created.");
            return null;
        }

        addEvent(event);
        provider.addEvent(event);
        view.displaySuccess("Event created successfully with ID " + event.getEventID());
        return event;
    }

    /**
     * Searches for performances on a date.
     */
    public void searchForPerformances() {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate targetDate = null;

        // 1a: keep asking until the date format is correct
        while (targetDate == null) {
            String targetDateRow = view.getInput("Please enter a date (yyyy-MM-dd): ");

            try {
                targetDate = LocalDate.parse(targetDateRow, inputFormatter);
            } catch (DateTimeParseException e) {
                view.displayError("Invalid date format. Please use yyyy-MM-dd.");
            }
        }

        // Step 1: find all performances on the target date
        List<Performance> specificPerformances = new ArrayList<>();

        for (Performance performance : performances) {
            if (performance == null || performance.getStatus() != PerformanceStatus.ACTIVE) {
                continue;
            }
            LocalDate startDate = performance.getStartDateTime().toLocalDate();
            LocalDate endDate = performance.getEndDateTime().toLocalDate();

            // performance is on the target date if the target date falls within [startDate, endDate]
            if (!startDate.isAfter(targetDate) && !endDate.isBefore(targetDate)) {
                specificPerformances.add(performance);
            }
        }

        // 1b: no performances on the provided date
        if (specificPerformances.isEmpty()) {
            view.displayError("There are no performances on " + targetDate + ".");
            return;
        }

        // 1c: if current user is a student with preferences, matching events go first
        User user = getCurrentUser();
        if (user instanceof Student) {
            Student student = (Student) user;
            StudentPreferences preferences = student.getStudentPreferences();

            specificPerformances.sort(new Comparator<Performance>() {
                @Override
                public int compare(Performance p1, Performance p2) {
                    Event e1 = getEventByID(p1.getEventId());
                    Event e2 = getEventByID(p2.getEventId());

                    boolean match1 = e1 != null && preferences.matchesStudentPreference(e1.getEventType());
                    boolean match2 = e2 != null && preferences.matchesStudentPreference(e2.getEventType());

                    if (match1 && !match2) {
                        return -1;
                    }
                    if (!match1 && match2) {
                        return 1;
                    }
                    return p1.getStartDateTime().compareTo(p2.getStartDateTime());
                }
            });
        } else {
            specificPerformances.sort(Comparator.comparing(Performance::getStartDateTime));
        }

        // display all performances
        List<String> results = new ArrayList<>();

        for (Performance performance : specificPerformances) {
            Event event = getEventByID(performance.getEventId());
            String organiserName = event == null ? "Unknown organiser" : event.getOrganiserName();
            double eventAverageRating = event == null ? 0.0 : event.getAverageRatingOfPerformances();

            String line = "Performance ID: " + performance.getPerformanceId()
                    + " Event name: " + performance.getEventTitle()
                    + " Time: " + performance.getStartDateTime().toLocalTime().format(timeFormatter)
                    + " - " + performance.getEndDateTime().toLocalTime().format(timeFormatter)
                    + " Venue of performance: " + performance.getVenueAddress()
                    + " EP: " + organiserName
                    + " Event average rating: " + String.format("%.2f", eventAverageRating);

            results.add(line);
        }

        view.displaySuccess("Performances on " + targetDate + ":");
        view.displayListOfPerformances(results);
    }

    /**
     * Views full details of a performance and its event.
     */
    public void viewPerformance() {
        if (performances.isEmpty()) {
            view.displayError("No performances available.");
            return;
        }

        Performance performance = null;
        int attempts = 0;

        while (performance == null && attempts < 7) {
            attempts++;
            try {
                String input = view.getInput("Enter performance ID: ");
                long performanceID = Long.parseLong(input);
                performance = getPerformanceByID(performanceID);

                if (performance == null) {
                    view.displayError("Invalid ID. Please try again.");
                }
            } catch (NumberFormatException e) {
                view.displayError("Invalid input. Please enter a number.");
            }
        }

        if (performance == null) {
            view.displayError("Too many unsuccessful attempts were made");
            return;
        }

        // show performance details
        view.displaySpecificPerformance(performance.toString());

        //get the event by eventID
        Event event = getEventByID(performance.getEventId());

        if (event == null) {
            view.displayError("Associated event not found");
            return;
        }

        // show event details
        view.displaySuccess("\n---Event Details---");
        view.displaySuccess(event.toString());

        // show average rating for the event
        double averageRating = event.getAverageRatingOfPerformances();
        view.displaySuccess("Event average rating: " + String.format("%.2f", averageRating));

        // show all reviews for the event
        Collection<String> allReviews = event.getAllPerformanceReviews();
        if (allReviews.isEmpty()) {
            view.displaySuccess("No reviews were added to this event yet");
        } else {
            view.displaySuccess("All reviews for the event: ");
            for (String review : allReviews) {
                view.displaySuccess(" - " + review);
            }
        }
    }

    /**
     * Cancels a performance owned by the current provider.
     * Notifies  and refunds all the students
     */
    public void cancelPerformance() {
        if (!checkCurrentUserIsEntertainmentProvider()) {
            view.displayError("Only entertainment providers can cancel performances.");
            return;
        }

        Performance performance = null;
        String organiserMessage = null;

        while (true) {
            String performanceInput = view.getInput("Enter performance ID to cancel: ");

            // performance == null
            if (performanceInput.trim().isEmpty()) {
                view.displayError("Performance ID cannot be empty.");
                continue;
            }

            long performanceID;
            try {
                performanceID = Long.parseLong(performanceInput.trim());
            } catch (NumberFormatException e) {
                view.displayError("Invalid performance ID.");
                continue;
            }

            // sameEP == false
            performance = getPerformanceByID(performanceID);

            if (performance == null) {
                view.displayError("Performance with given number does not exist.");
                continue;
            }

            if (!performance.checkCreatedByEP(currentUser.getEmail())) {
                view.displayError("The performance with given number does not belong to you.");
                continue;
            }

            if (performance.getStatus() == PerformanceStatus.CANCELLED) {
                view.displayError("This performance has already been cancelled.");
                return;
            }

            if (!performance.checkHasNotHappenedYet()) {
                view.displayError("Performance can't be cancelled as it has already happened.");
                return;
            }

            break;
        }

        // message
        if (performance.hasActiveBooking()) {
            while (true) {
                organiserMessage = view.getInput("Provide a cancellation message for affected students: ").trim();
                if (organiserMessage.isEmpty()) {
                    view.displayError("Please provide a non-empty message for the students.");
                    continue;
                }
                break;
            }

            List<Booking> bookingsCopy = new ArrayList<>(performance.getBookings());

            for (Booking booking : bookingsCopy) {
                if (booking == null || booking.getBookingStatus() != BookingStatus.ACTIVE) {
                    continue;
                }

                boolean refundSuccess = paymentSystem.processRefund(
                        booking.getNumTickets(),
                        performance.getEventTitle(),
                        booking.getStudentEmail(),
                        booking.getStudentPhoneNumber(),
                        performance.getOrganiserEmail(),
                        booking.getTransactionAmount(),
                        organiserMessage
                );

                if (!refundSuccess) {
                    view.displayError("The performance could not be cancelled because refund processing failed.");
                    return;
                }
            }

            for (Booking booking : bookingsCopy) {
                if (booking == null || booking.getBookingStatus() != BookingStatus.ACTIVE) {
                    continue;
                }

                booking.cancelByProvider();
                booking.getStudent().removeBooking(booking);
                performance.removeBooking(booking);
            }
        }

        performance.cancel();
        performances.remove(performance);

        Event event = performance.getEvent();
        if (event != null) {
            event.removePerformance(performance);

            if (event.getPerformances().isEmpty()) {
                events.remove(event);

                if (currentUser instanceof EntertainmentProvider) {
                    ((EntertainmentProvider) currentUser).removeEvent(event);
                }
            }
        }

        view.displaySuccess("The performance has been cancelled and refunds have been processed if required.");
    }

    /**
     * Sponsorship validity check.
     *
     * @param performance performance
     * @param amount amount
     * @return true if sponsorship can proceed
     */
    private boolean checkIfSponsorshipPossible(Performance performance, int amount) {
        if (performance == null) {
            view.displayError("Performance cannot be null.");
            return false;
        }

        if (!performance.checkIfEventIsTicketed()) {
            view.displayError("Sponsorship cannot be applied to non-ticketed " +
                    "performances.");
            return false;
        }

        if (performance.getStatus() != PerformanceStatus.ACTIVE) {
            view.displayError("Cancelled performances cannot be sponsored.");
            return false;
        }

        if (amount <= 0) {
            view.displayError("Sponsorship must be positive.");
            return false;
        }

        if (amount > performance.getFinalTicketPrice()) {
            view.displayError("Sponsorship cannot reduce the price below zero.");
            return false;
        }

        return true;
    }

    /**
     * Sponsors a performance.
     */
    public void sponsorPerformance() {
        if (!checkCurrentUserIsAdmin()) {
            view.displayError("Only admin staff can sponsor performances.");
            return;
        }

        if (performances.isEmpty()) {
            view.displayError("No performances available to sponsor.");
            return;
        }

        Performance performance = null;

        while (performance == null) {
            try {
                String input = view.getInput("Enter performance ID to " +
                        "sponsor: ");
                long performanceID = Long.parseLong(input);
                performance = getPerformanceByID(performanceID);
                if (performance == null) {
                    view.displayError("Performance with given ID does not " +
                            "exist");
                }
            }
            catch (NumberFormatException e) {
                view.displayError("Invalid input. Please reenter a " +
                        "performance ID.");
            }
            catch (NoSuchElementException e) {
                view.displayError("No input provided, cancelling sponsorship.");
                return;
            }
        }

        // check if the event is ticketed
        if (!performance.checkIfEventIsTicketed()) {
            view.displayError("The requested performance event is not " +
                    "ticketed. It cannot be sponsored.");
            return;
        }

        // get valid sponsorship amount
        double amount;
        while (true) {
            try {
                String input = view.getInput("Enter sponsorship amount: ");
                amount = Double.parseDouble(input);

                if (!checkIfSponsorshipPossible(performance, (int) amount)) {
                    continue;
                }

                break;
            } catch (NumberFormatException e) {
                view.displayError("Invalid input. Please enter a number.");
            } catch (NoSuchElementException e) {
                view.displayError("No input provided, cancelling sponsorship.");
                return;
            }
        }

        performance.sponsor(amount);

        view.displaySuccess("Sponsorship successful! You sponsored a " +
                "performance with ID " + performance.getPerformanceId() +
                "with £" + amount);
    }

    /**
     * Adds event to the controller store.
     *
     * @param e event
     */
    public void addEvent(Event e) {
        if (e != null) {
            events.add(e);
        }
    }

    /**
     * Adds performance to the controller store.
     *
     * @param p performance
     */
    public void addPerformance(Performance p) {
        if (p != null) {
            performances.add(p);
        }
    }

    /**
     * Retrieves an event by its unique identifier.
     *
     * @param eventID the unique identifier of the event to retrieve
     * @return the Event object if found, or null if no event with the given ID exists
     */
    private Event getEventByID(long eventID) {
        for (Event e : events) {
            if (e.getEventID() == eventID) {
                return e;
            }
        }
        return null;
    }

    /**
     * Prompts the user to enter a unique performance ID.
     * Continues prompting until a valid, positive, and unique ID is provided.
     *
     * @return a unique positive performance ID that does not already exist in the system
     */
    private long promptUniquePerformanceId() {
        while (true) {
            try {
                long id = Long.parseLong(view.getInput("Enter performance ID: ").trim());

                if (id <= 0) {
                    view.displayError("Performance ID must be positive.");
                    continue;
                }

                if (getPerformanceByID(id) != null) {
                    view.displayError("A performance with this ID already exists.");
                    continue;
                }

                return id;
            } catch (NumberFormatException e) {
                view.displayError("Please enter a valid numeric performance ID.");
            }
        }
    }

    /**
     * Checks whether a provider already has an event with the same title and
     * a performance that overlaps with the given time range.
     *
     * @param provider      the entertainment provider to check
     * @param eventTitle    the title of the event to compare
     * @param startDateTime the start time of the new performance
     * @param endDateTime   the end time of the new performance
     * @return true if the provider has an event with the same title and an overlapping performance,
     *         false otherwise
     */
    private boolean providerHasSameTitleAndOverlappingTimes(
            EntertainmentProvider provider,
            String eventTitle,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        for (Event existing : provider.getEvents()) {
            if (!existing.getEventTitle().equalsIgnoreCase(eventTitle)) {
                continue;
            }

            if (existing.hasPerformanceAtSameTimes(startDateTime, endDateTime)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a performance by its unique identifier.
     *
     * @param performanceID the unique identifier of the performance to retrieve
     * @return the Performance object if found, or null if no performance with the given ID exists
     */
    Performance getPerformanceByID(long performanceID) {
        for (Performance p : performances) {
            if (p.getPerformanceId() == performanceID) {
                return p;
            }
        }
        return null;
    }

    /**
     * Prompts the user to select an event type from a predefined list.
     * Continues prompting until a valid event type is entered.
     * Accepts both singular and plural forms for sports and games.
     *
     * @return the selected EventType enum value
     */
    private EventType promptEventType() {
        while (true) {
            String raw = view.getInput("Enter event type (music, theatre, dance, movie, sports, games): ").trim().toLowerCase();

            switch (raw) {
                case "music":
                    return EventType.Music;
                case "theatre":
                    return EventType.Theatre;
                case "dance":
                    return EventType.Dance;
                case "movie":
                    return EventType.Movie;
                case "sport":
                case "sports":
                    return EventType.Sports;
                case "game":
                case "games":
                    return EventType.Games;
                default:
                    view.displayError("Invalid event type.");
            }
        }
    }

    /**
     * Prompts the user to enter a date and time in a specific format.
     * Continues prompting until a correctly formatted date/time is provided.
     *
     * @param prompt the message to display to the user when requesting input
     * @return a LocalDateTime object parsed from the user's input
     */
    private LocalDateTime promptDateTime(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        while (true) {
            String raw = view.getInput(prompt).trim();
            try {
                return LocalDateTime.parse(raw, formatter);
            } catch (DateTimeParseException e) {
                view.displayError("Invalid date/time format. Use yyyy-MM-dd HH:mm.");
            }
        }
    }

    /**
     * Prompts the user to enter a comma-separated list of performer names.
     * If the input is empty, returns an empty collection.
     *
     * @return a Collection of trimmed performer names, or an empty list if none provided
     */
    private Collection<String> promptPerformerNames() {
        String input = view.getInput("Enter performer names separated by commas (or leave blank): ").trim();
        List<String> names = new ArrayList<>();

        if (input.isEmpty()) {
            return names;
        }

        String[] split = input.split(",");
        for (String s : split) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }

        return names;
    }

    /**
     * Prompts the user to enter a positive integer value.
     * Continues prompting until a valid positive integer is provided.
     *
     * @param prompt the message to display to the user when requesting input
     * @return a positive integer value entered by the user
     */
    private int promptPositiveInt(String prompt) {
        while (true) {
            try {
                int value = Integer.parseInt(view.getInput(prompt).trim());
                if (value <= 0) {
                    view.displayError("Value must be positive.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                view.displayError("Please enter a valid integer.");
            }
        }
    }

    /**
     * Prompts the user to enter a non-negative double value.
     * Continues prompting until a valid non-negative number is provided.
     *
     * @param prompt the message to display to the user when requesting input
     * @return a non-negative double value entered by the user
     */
    private double promptNonNegativeDouble(String prompt) {
        while (true) {
            try {
                double value = Double.parseDouble(view.getInput(prompt).trim());
                if (value < 0) {
                    view.displayError("Value cannot be negative.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                view.displayError("Please enter a valid number.");
            }
        }
    }

    /**
     * Prompts the user to answer a yes/no question.
     * Accepts "yes"/"y" for true and "no"/"n" for false.
     * Continues prompting until a valid response is provided.
     *
     * @param prompt the message to display to the user when requesting input
     * @return true if the user answers "yes" or "y", false if "no" or "n"
     */
    private boolean promptYesNo(String prompt) {
        while (true) {
            String raw = view.getInput(prompt).trim().toLowerCase();
            if (raw.equals("yes") || raw.equals("y")) {
                return true;
            }
            if (raw.equals("no") || raw.equals("n")) {
                return false;
            }
            view.displayError("Please answer yes or no.");
        }
    }
}