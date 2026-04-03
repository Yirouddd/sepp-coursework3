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
   * Constructs a new Student with the specified details.
   * Email and password are inherited from abstract class User.
   *
   * @param email    the student email (from User class, used as username)
   * @param password the student password (from User abstract class)
   * @param name     the name of the student
   */
  public Student(String email, String password, String name, int phoneNumber) {
    // name cannot be null or empty
    super(email, password);

    assert name != null && !name.isEmpty();
    assert phoneNumber > 0 : "Phone number should be valid.";

    this.name = name;
    this.phoneNumber = phoneNumber;
    this.bookings = new ArrayList<>();
    this.studentPreferences = new StudentPreferences(false, false, false, false, false);
  }

  /**
   * 
   */
  public List<Booking> addBooking(Booking booking) {
    assert booking != null : "Non-existent booking cannot be added.";
    bookings.add(booking);
      return List.of();
  }

  public int getPhoneNumber() {
    return phoneNumber;
  }

  public StudentPreferences getStudentPreferences() {
    return studentPreferences;
  }


  // getter

  @Override
  public String getEmail() {
    return super.getEmail();
  }

  public int getPhoneNumber () {
    return phoneNumber;
  }
}