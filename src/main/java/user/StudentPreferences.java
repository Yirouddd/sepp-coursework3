package user;

import enums.EventType;

public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportEvents;
    public boolean preferGameEvents;

    public StudentPreferences(boolean preferMusicEvents,
                              boolean preferTheatreEvents,
                              boolean preferDanceEvents,
                              boolean preferMovieEvents,
                              boolean preferSportEvents,
                              boolean preferGameEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportEvents = preferSportEvents;
        this.preferGameEvents = preferGameEvents;
    }

    public boolean matchesStudentPreference(EventType eventType) {
        if (eventType == null) {
            return false;
        }

        switch (eventType) {
            case Music:
                return preferMusicEvents;
            case Theatre:
                return preferTheatreEvents;
            case Dance:
                return preferDanceEvents;
            case Movie:
                return preferMovieEvents;
            case Sports:
                return preferSportEvents;
            default:
                return false;
        }
    }

    public void updatePreferences(String studentRawStringPreferences) {
        if (studentRawStringPreferences == null || studentRawStringPreferences.trim().isEmpty()) {
            System.out.println("Invalid input: Preferences string cannot be null or empty.");
            return;
        }

        String[] separatePreferences = studentRawStringPreferences.split(",");
        for (String preference : separatePreferences) {
            String lowerCasePreference = preference.toLowerCase().trim();

            switch (lowerCasePreference) {
                case "music":
                    this.preferMusicEvents = true;
                    break;
                case "theatre":
                    this.preferTheatreEvents = true;
                    break;
                case "dance":
                    this.preferDanceEvents = true;
                    break;
                case "movie":
                    this.preferMovieEvents = true;
                    break;
                case "sport":
                case "sports":
                    this.preferSportEvents = true;
                    break;
                case "game":
                    this.preferGameEvents = true;
                    break;
                default:
                    System.out.println("Invalid preference: " + preference);
                    break;
            }
        }
    }
}