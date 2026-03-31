package user;
public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportEvents;
    public boolean preferGameEvents;

    public StudentPreferences(boolean preferMusicEvents, boolean preferTheatreEvents, boolean preferDanceEvents, boolean preferMovieEvents, boolean preferSportsEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportEvents = preferSportEvents;
        this.preferGameEvents = preferGameEvents;
    }


    public void updatePreferences(String studentRawStringPreferences) {
        // Check if is null or empty
        if (studentRawStringPreferences == null || studentRawStringPreferences.trim().isEmpty()) {
            System.out.println("Invalid input: Preferences string cannot be null or empty.");
            return;
        }

        String[] separatePreferences = studentRawStringPreferences.split(",");
        for (String preference : separatePreferences) {
            // Convert the raw string to lowercase for case-insensitive maching
            String lowerCasePreferences = studentRawStringPreferences.toLowerCase().trim();

            // Process the single preference
            switch (lowerCasePreferences) {
                case "music":
                    this.preferMusicEvents = true;
                    System.out.println("Music preference updated to true.");
                    break;
                case "theatre":
                    this.preferTheatreEvents = true;
                    System.out.println("Theatre preference updated to true.");
                    break;
                case "dance":
                    this.preferDanceEvents = true;
                    System.out.println("Dance preference updated to true.");
                    break;
                case "movie":
                    this.preferMovieEvents = true;
                    System.out.println("Movie preference updated to true.");
                    break;
                case "sport":
                    this.preferSportEvents = true;
                    System.out.println("Sports preference updated to true.");
                    break;
                case "game":
                    this.preferGameEvents = true;
                    System.out.println("Game preference updated to true.");
                default:
                    System.out.println("Invalid preference: " + studentRawStringPreferences);
            }
        }

    }

}