package user;
import object.Booking;

import java.util.List;
import java.util.ArrayList;

/**
 * Student class represents a student with a name.
 */
public class Student extends User {
    private String name;
    private int phoneNumber;
    private List<Booking> bookings;
    private StudentPreferences studentPreferences;

    /**
     * Constructs a new Student.
     *
     * @param email student email
     * @param password student password
     * @param name student name
     * @param phoneNumber student phone number
     */
    public Student(String email, String password, String name, int phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.bookings = new ArrayList<>();
        this.studentPreferences = new StudentPreferences(false,false, false, false, false, false);
        setEmail(email);
        setPassword(password);
    }

    /**
     * Adds a booking to the student record.
     *
     * @param booking booking to add
     */
    public void addBooking(Booking booking) {
      bookings.add(booking);
    }

    /**
     * Removes a booking from the student record.
     *
     * @param booking booking to remove
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    public StudentPreferences getStudentPreferences() {
      return studentPreferences;
    }


      /**
       * Gets student's Phone number
       *
       * @return phoneNumber student Phone number
       */
      public int getPhoneNumber () {
          return phoneNumber;
      }

    /**
     * Gets student's name
     *
     * @return name student's name
     */
    public String getName() {
        return name;
    }
}