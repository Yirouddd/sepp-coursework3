package user;
public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportsEvents;

    public StudentPreferences(boolean preferMusicEvents, boolean preferTheatreEvents, boolean preferDanceEvents, boolean preferMovieEvents, boolean preferSportsEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportsEvents = preferSportsEvents;
    }

    public void updatePreferences(String studentRawStringPreferences) {
        // Check if is null or empty
        if (studentRawStringPreferences == null || studentRawStringPreferences.trim().isEmpty()) {
            System.out.println("Invalid input: Preferences string cannot be null or empty.");
            return;
        }

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
            case "sports":
                this.preferSportsEvents = true;
                System.out.println("Sports preference updated to true.");
                break;
            default:
                System.out.println("Invalid preference: " + studentRawStringPreferences);
        }

    }

}