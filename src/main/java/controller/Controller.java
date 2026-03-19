package controller;

import interface1.View;
import user.AdminStaff;
import user.EntertainmentProvider;
import user.Student;
import user.User;

import java.util.Collection;

/**
 * Abstract Controller class provides a common
 * functionality for all controllers.
 */

public abstract class Controller {

  protected User currentUser;
  View view;

  /**
   * Constructs a Controller for the current user.
   *
   * @param currentUser - the currently logged in user
   */

  public Controller(User currentUser) {
    this.currentUser = currentUser;
  }

  /**
   * Check if the current user is a guest (not logged in)
   * 
   * @return true if the current user is a guest,
   *         false otherwise
   */

  private boolean checkCurrentUserIsGuest() {
    return currentUser == null;
  }

  /**
   * Check if the current user is an admin staff
   * 
   * @return true if the current user is an admin staff,
   *         false otherwise
   */

  private boolean checkCurrentUserIsAdmin() {
    return currentUser instanceof AdminStaff;
  }

  /**
   * Check if the current user is a student
   * 
   * @return true if the current user is a astudent,
   *         false otherwise
   */
  private boolean checkCurrentUserIsStudent() {
    return currentUser instanceof Student;
  }

  /**
   * Check if the current user is an entertainment provider
   * 
   * @return true if the current user is an entertainment provider,
   *         false otherwise
   */

  private boolean checkCurrentUserIsEntertainmentProvider() {
    return false;
  }

  /**
   * Displays the menu, allows user to select an option
   * 
   * @param options the collection of menu options
   * @return the index of the selected option in the list
   */
  public <T> int selectFromMenu(Collection<T> options) {
    // options cannot be null, they should exist
    if (options == null || options.isEmpty()) {
      throw new IllegalArgumentException("Options cannot be null or empty, should be listed.");
    }

    System.out.println("Options were not added, sorry.");

    int index = 1;
    for (T option : options) {
      System.out.println(index + ". " + option);
      index++;
    }

    String userInput = view.getInput(); // in view it returns string
    int choice;

    try {
      choice = Integer.parseInt(userInput);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The input must be a number");
    }

    if (choice < 1 || choice > options.size()) {
      throw new IllegalArgumentException("Select valid menu options.");
    }

    return choice - 1;
  }
}