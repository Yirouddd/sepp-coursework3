package main.user;
public class StudentPreferences {
    public boolean preferMusicEvents;
    public boolean preferTheatreEvents;
    public boolean preferDanceEvents;
    public boolean preferMovieEvents;
    public boolean preferSportsEvents;

    public StudentPreferences() {
        // Default constructor with no preferences
        this.preferMusicEvents = false;
        this.preferTheatreEvents = false;
        this.preferDanceEvents = false;
        this.preferMovieEvents = false;
        this.preferSportsEvents = false;
    }

    public StudentPreferences(boolean preferMusicEvents, boolean preferTheatreEvents, boolean preferDanceEvents, boolean preferMovieEvents, boolean preferSportsEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportsEvents = preferSportsEvents;
    }

    public void updatePreferences(boolean preferMusicEvents, boolean preferTheatreEvents, boolean preferDanceEvents, boolean preferMovieEvents, boolean preferSportsEvents) {
        this.preferMusicEvents = preferMusicEvents;
        this.preferTheatreEvents = preferTheatreEvents;
        this.preferDanceEvents = preferDanceEvents;
        this.preferMovieEvents = preferMovieEvents;
        this.preferSportsEvents = preferSportsEvents;
    }

    public List<EventType> getPreferredEventTypes() {
        List<EventType> preferredEventTypes = new ArrayList<>();

        if (preferMusicEvents) {
            preferredEventTypes.add(EventType.MUSIC);
        }
        if (preferTheatreEvents) {
            preferredEventTypes.add(EventType.THEATRE);
        }
        if (preferDanceEvents) {
            preferredEventTypes.add(EventType.DANCE);
        }
        if (preferMovieEvents) {
            preferredEventTypes.add(EventType.MOVIE);
        }
        if (preferSportsEvents) {
            preferredEventTypes.add(EventType.SPORTS);
        }

        return preferredEventTypes;
    }
}