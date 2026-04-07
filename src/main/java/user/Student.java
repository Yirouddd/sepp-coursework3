package user;

import object.Booking;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a student user in the system
 * Stores student's name, phone number, list of bookings, and their preferences.
 */
public class Student extends User {
    private String name;
    private int phoneNumber;
    private List<Booking> bookings;
    private StudentPreferences studentPreferences;

    /**
     * Constructs a new Student with given details.
     *
     * @param email student's email
     * @param password student's password
     * @param name student's name
     * @param phoneNumber student's phone number
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
     * Adds a booking to the student.
     *
     * @param booking booking to add
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /**
     * Removes a booking from the student.
     *
     * @param booking booking to remove
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
    }

    /**
     * Returns the student's preferences.
     *
     * @return the student preferences
     */
    public StudentPreferences getStudentPreferences() {
      return studentPreferences;
    }


      /**
       * Returns the student's phone number.
       *
       * @return the student's phone number
       */
      public int getPhoneNumber () {
          return phoneNumber;
      }

    /**
     * Gets student's name
     *
     * @return student's name
     */
    public String getName() {
        return name;
    }
}