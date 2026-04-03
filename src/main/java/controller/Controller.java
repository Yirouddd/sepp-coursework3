package controller;

import interfaces.TextUserInterface;
import interfaces.View;
import user.AdminStaff;
import user.Student;
import user.EntertainmentProvider;
import user.User;

import java.util.Collection;

/**
 * Abstract Controller class provides a common
 * functionality for all controllers.
 */

public abstract class Controller {

  public User currentUser;
  protected View view; //added this field

  //added constructor so that super(currentUser, view) in EventPerformanceController works
  public Controller(User currentUser, View view) {
    this.currentUser = currentUser;
    this.view = view;
  }
  /**
   * Check if the current user is a guest (not logged in)
   * 
   * @return true if the current user is a guest,
   *         false otherwise
   */

  protected boolean checkCurrentUserIsGuest() {
    return currentUser == null;
  }

  /**
   * Check if the current user is an admin staff
   * 
   * @return true if the current user is an admin staff,
   *         false otherwise
   */

  protected boolean checkCurrentUserIsAdmin() {
    return currentUser instanceof AdminStaff;
  }

  /**
   * Check if the current user is a student
   * 
   * @return true if the current user is a astudent,
   *         false otherwise
   */
  protected boolean checkCurrentUserIsStudent() {
    return currentUser instanceof Student;
  }

  /**
   * Check if the current user is an entertainment provider
   * 
   * @return true if the current user is an entertainment provider,
   *         false otherwise
   */

  protected boolean checkCurrentUserIsEntertainmentProvider() {
    return currentUser instanceof EntertainmentProvider;
  }

  /**
   * Displays the menu, allows user to select an option
   * 
   * @param options the collection of menu options
   * @return the index of the selected option in the list
   */
  protected <T> int selectFromMenu(Collection<T> options) {
    // options cannot be null, they should exist
    if (options == null || options.isEmpty()) {
      throw new IllegalArgumentException("Options cannot be null or empty, should be listed.");
    }

    System.out.println("Options were not added, sorry.");

    // display menu options
    int index = 1;
    for (T option : options) {
      System.out.println(index + ". " + option);
      index++;
    }

    // get user input via the View interface      //Todo for Sasha: here miss a input Prompt
    View view = new TextUserInterface();          // changed, since we view are not link to controller in the model diagram
    String userInput = view.getInput(""); // in view it returns string
    int choice;

    try {
      choice = Integer.parseInt(userInput);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("The input must be a number.");
    }

    if (choice < 1 || choice > options.size()) {
      throw new IllegalArgumentException("Select valid menu options.");
    }

    return choice - 1;
  }
}