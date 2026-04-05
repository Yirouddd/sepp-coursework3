package controller;

import external.MockVerificationService;
import external.VerificationService;
import object.Event;
import user.*;
import interfaces.View;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Handles login, logout, EP registration, and student preference editing.
 */
public class UserController extends Controller {
    public final String PREREGISTERED_USERS_FILE_PATH;
    public final String PREREGISTERED_ADMIN_FILE_PATH;
    private Map<String, User> users;
    private final VerificationService verificationService;

    /**
     * Constructs a UserController.
     *
     * @param view UI view
     * @param preregisteredUsersFilePath students file path
     * @param preregisteredAdminFilePath admins file path
     */
    public UserController(View view,
                          VerificationService verificationService,
                          String preregisteredUsersFilePath,
                          String preregisteredAdminFilePath) {
        PREREGISTERED_USERS_FILE_PATH = preregisteredUsersFilePath;
        PREREGISTERED_ADMIN_FILE_PATH = preregisteredAdminFilePath;
        this.view = view;
        this.verificationService = verificationService;
        users = new HashMap<>();

        try {
            addPreregisteredUsers();
        } catch (FileNotFoundException e) {
            view.displayError("File not found: " + e.getMessage());
        }
    }

    /**
     * Logs a user in.
     */
    public void login() {
        while (true) {
            String email = view.getInput("Email: ").trim();
            String password = view.getInput("Password: ").trim();

            if (email.isEmpty() || password.isEmpty()) {
                view.displayError("Email/password cannot be empty.");
                return;
            }

            User user = users.get(email);

            if (user != null && user.getPassword().equals(password)) {
                currentUser = user;
                view.displaySuccess("Login successful.");
                break;
            }else {
                view.displayError("Incorrect email/password.");
            }
        }
    }

    /**
     * Logs current user out.
     */
    public void logout() {
        currentUser = null;
        view.displaySuccess("Log out successful.");
    }

    /**
     * Registers a new entertainment provider.
     */
    public void registerEntertainmentProvider() {
        String email;
        String password;
        String orgName;
        String businessNumber;
        String name;
        String description;

        while (true) {
            String head = "Please complete the following registration information.";

            email = view.getInput(head + "\nEmail: ").trim();
            if (email.isEmpty() || !email.contains("@")){
                view.displayError("Invalid email.");
                continue;
            }

            password = view.getInput("Password: ").trim();
            if (password.isEmpty()){
                view.displayError("Password cannot be empty.");
                continue;
            }

            orgName = view.getInput("Organisation name: ").trim();
            if (orgName.isEmpty()){
                view.displayError("Organisation name cannot be empty.");
                continue;
            }

            businessNumber = view.getInput("Business number: ").trim();
            if (businessNumber.isEmpty()){
                view.displayError("Business number cannot be empty.");
                continue;
            }

            if (! verificationService.verifyEntertainmentProvider(businessNumber)) {
                view.displayError("Business number verification failed.");
                continue;
            }

            if (EPAccountAlreadyExists(email, orgName, businessNumber)){
                continue;
            }

            name  = view.getInput("Name: ");
            if (name.isEmpty()){
                view.displayError("Main contact name cannot be empty.");
                continue;
            }

            description = view.getInput("Description: ");
            if (description.isEmpty()){
                view.displayError("Description cannot be empty.");
                continue;
            }

            EntertainmentProvider newEP =
                    new EntertainmentProvider(email, password, orgName, businessNumber, name, description);
            users.put(email, newEP);
            this.currentUser = newEP;

            view.displaySuccess("Register successful!");
            return;
        }
    }

    /**
     * Checks whether an EP already exists.
     *
     * @param email email
     * @param orgName organisation name
     * @param businessNumber business number
     * @return true if duplicate exists
     */
    private boolean EPAccountAlreadyExists(String email, String orgName, String businessNumber) {
        if (users.containsKey(email)) {
            view.displayError("An account with this email already exists.");
            return true;
        }

        for (User user : users.values()) {
            if (user instanceof EntertainmentProvider) {
                EntertainmentProvider ep = (EntertainmentProvider) user;
                if (ep.getOrgName().equalsIgnoreCase(orgName)
                        && ep.getBusinessNumber().equalsIgnoreCase(businessNumber)) {
                    view.displayError("This entertainment provider already exists.");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Lets a student edit preferences.
     */
    public void editPreferences() {
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can edit preferences");
            return;
        }

        Student student = (Student) currentUser;

        while (true) {
            String input = view.getInput(
                    "Select up to 3 preferences from: music, theatre, dance, movie, sports, games. \n"
                    + "Enter preferences (separated by commas): ");

            if (input == null || input.trim().isEmpty()) {
                view.displayError("Preferences cannot be empty.");
                continue;
            }

            String[] preferences = input.split(",");
            if (preferences.length > 3) {
                view.displayError("You can select up to 3 preferences. Please try again.");
                continue;
            }

            List<String> valid = new ArrayList<>();
            boolean isValid = true;

            for (String pref : preferences) {
                String p = pref.trim().toLowerCase();

                if (!(p.equals("music")
                        || p.equals("theatre")
                        || p.equals("dance")
                        || p.equals("movie")
                        || p.equals("sports")
                        || p.equals("games"))) {
                    view.displayError("Invalid preference: " + p + ". Please try again.");
                    isValid = false;
                    break;
                }

                if (valid.contains(p)) {
                    view.displayError("Duplicate preference: " + p);
                    isValid = false;
                    break;
                }

                valid.add(p);
            }

            if (!isValid) {
                continue;
            }

            try {
                student.getStudentPreferences().updatePreferences(input);
                view.displaySuccess("Preferences updated.");
                return;
            } catch (IllegalArgumentException e) {
                view.displayError(e.getMessage());
            }
        }
    }

    /**
     * Adds a user to the system.
     *
     * @param user user
     */
    private void addUser(User user) {
        if (user != null) {
            users.put(user.getEmail(), user);
        }
    }

    /**
     * Loads preregistered students and admins from files.
     *
     */
    private void addPreregisteredUsers() throws FileNotFoundException {
        if (PREREGISTERED_USERS_FILE_PATH != null){
            Scanner scanner = new Scanner(new File(PREREGISTERED_USERS_FILE_PATH));
            while (scanner.hasNextLine()){
                String userInfos = scanner.nextLine().trim();
                if (userInfos.isEmpty()) {
                    continue;
                }

                String[] userInfoArray = userInfos.split(",");
                if (userInfoArray.length != 4) {
                    view.displayError("Invalid line in preregistered users file: " + userInfos);
                    continue;
                }
                String email = userInfoArray[0].trim();
                String password = userInfoArray[1].trim();
                String name = userInfoArray[2].trim();
                int phoneNumber = Integer.parseInt(userInfoArray[3].trim());

                Student student = new Student(email, password, name, phoneNumber);
                addUser(student);
            }
            scanner.close();
        }

        if (PREREGISTERED_ADMIN_FILE_PATH != null){
            Scanner scanner = new Scanner(new File(PREREGISTERED_ADMIN_FILE_PATH));
            while (scanner.hasNextLine()){
                String userInfos = scanner.nextLine().trim();
                if (userInfos.isEmpty()) {
                    continue;
                }

                String[] userInfoArray = userInfos.split(",");
                if (userInfoArray.length != 3) {
                    view.displayError("Invalid line in preregistered admin file: " + userInfos);
                    continue;
                }
                String email = userInfoArray[0].trim();
                String password = userInfoArray[1].trim();
                String name = userInfoArray[2].trim();

                AdminStaff adminStaff = new AdminStaff(email, password, name);
                addUser(adminStaff);
            }
            scanner.close();
        }
    }

    /**
     * Finds the entertainment provider who owns a given event id.
     *
     * @param eventNumber event id
     * @return EP owner or null
     */
    private EntertainmentProvider getEntertainmentProviderOwningEvent(long eventNumber){
        for (User user : users.values()) {
            if (user instanceof EntertainmentProvider) {
                EntertainmentProvider ep = (EntertainmentProvider) user;
                for (Event event : ep.getEvents()) {
                    if (event.getEventID() == eventNumber) {
                        return ep;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }
}