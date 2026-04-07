package user;

import enums.EventType;

/**
 * Stores a student's preferences for different event types.
 */
public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportsEvents;
    public boolean preferGamesEvents;

    /**
     * Constructs student preferences with the given selections.
     *
     * @param preferMusicEvents if music events are preferred
     * @param preferTheatreEvents if theatre events are preferred
     * @param preferDanceEvents if dance events are preferred
     * @param preferMovieEvents if movie events are preferred
     * @param preferSportsEvents if sports events are preferred
     * @param preferGamesEvents if games events are preferred
     */
    public StudentPreferences(boolean preferMusicEvents,
                              boolean preferTheatreEvents,
                              boolean preferDanceEvents,
                              boolean preferMovieEvents,
                              boolean preferSportsEvents,
                              boolean preferGamesEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportsEvents = preferSportsEvents;
        this.preferGamesEvents = preferGamesEvents;
    }

    /**
     * Checks if the given event type matches the student's preferences.
     *
     * @param eventType the event type to be checked
     * @return true if the event type is preferred, false otherwise
     */
    public boolean matchesStudentPreference(EventType eventType) {
        if (eventType == null) {
            return false;
        }

        return switch (eventType) {
            case Music -> preferMusicEvents;
            case Theatre -> preferTheatreEvents;
            case Dance -> preferDanceEvents;
            case Movie -> preferMovieEvents;
            case Sports -> preferSportsEvents;
            case Games -> preferGamesEvents;
        };
    }

    /**
     * Updates the student's preferences with the new selection from a comma-separated preference list.
     *
     * @param studentRawStringPreferences the input preferences
     * @return true if the update was successful, false if not (input is
     * invalid)
     */
    public boolean updatePreferences(String studentRawStringPreferences) {
        if (studentRawStringPreferences == null || studentRawStringPreferences.trim().isEmpty()) {
            return false;
        }

        // Reset first so new preferences replace old ones
        preferMusicEvents = false;
        preferTheatreEvents = false;
        preferDanceEvents = false;
        preferMovieEvents = false;
        preferSportsEvents = false;
        preferGamesEvents = false;

        String[] separatePreferences = studentRawStringPreferences.split(",");

        for (String preference : separatePreferences) {
            String lowerCasePreference = preference.toLowerCase().trim();

            switch (lowerCasePreference) {
                case "music":
                    preferMusicEvents = true;
                    break;
                case "theatre":
                    preferTheatreEvents = true;
                    break;
                case "dance":
                    preferDanceEvents = true;
                    break;
                case "movie":
                    preferMovieEvents = true;
                    break;
                case "sport":
                case "sports":
                    preferSportsEvents = true;
                    break;
                case "game":
                case "games":
                    preferGamesEvents = true;
                    break;
                default:
                    return false; // invalid preference found
            }
        }
        return true;
    }
}