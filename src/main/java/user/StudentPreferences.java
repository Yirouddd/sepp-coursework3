package user;

import enums.EventType;

/**
 * Student preferences for event types.
 */
public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportsEvents;
    public boolean preferGamesEvents;

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
     * Checks if an event type matches the student's preferences.
     *
     * @param eventType event type
     * @return true if preferred, false otherwise
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
     * Replaces the student's preferences with the new selection.
     *
     * @param studentRawStringPreferences comma-separated preference list
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