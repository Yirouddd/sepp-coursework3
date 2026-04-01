package controller;

import external.MockVerificationService;
import external.VerificationService;
import interfaces.TextUserInterface;
import user.*;

import interfaces.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * UserController handles authentication (login, logout, register EP) and
 * and student preference management
 */
public class UserController extends Controller {

    public final String PREREGISTERED_USERS_FILE_PATH;
    public final String PREREGISTERED_ADMIN_FILE_PATH;
    private Map<String, User> users;
    private View view = new TextUserInterface();


    public UserController(String preregisteredUsersFilePath, String preregisteredAdminFilePath) {
        PREREGISTERED_USERS_FILE_PATH = preregisteredUsersFilePath;
        PREREGISTERED_ADMIN_FILE_PATH = preregisteredAdminFilePath;

        users = new HashMap<>();
        try {
            addPreregisteredUsers();
        } catch (FileNotFoundException e) {
            view.displayError("File not found: " + e.getMessage());
        }
    }

    public void login() {
        String email = view.getInput("Email: ").trim();
        if (email.isEmpty()) {
            view.displayError("Email cannot be empty.");
            return;
        }

        String password = view.getInput("Password: ").trim();
        if (password.isEmpty()) {
            view.displayError("Password cannot be empty.");
            return;
        }

        User user = users.get(email);

        if (user == null) {
            view.displayError("User does not exist.");
            return;
        }

        if (!user.getPassword().equals(password)) {
            view.displayError("Incorrect password.");
            return;
        }

        currentUser = user;
        view.displaySuccess("Login successful.");
    }

    public void logout() {
        // Implementation for user logout
    }

    public void registerEntertainmentProvider() {
        String email;
        String password;
        String orgName;
        String businessNumber;
        String name;
        String description;

        VerificationService verificationService = new MockVerificationService();

        while (true) {
            System.out.println("Please complete the following registration information.");

            email = view.getInput("Email: ");
            if (email.isEmpty() || !email.contains("@")){
                view.displayError("Invalid email.");
                continue;
            }

            password = view.getInput("Password: ");
            if (password.isEmpty()){
                view.displayError("Password cannot be empty.");
                continue;
            }

            orgName = view.getInput("Organisation name: ");
            if (orgName.isEmpty()){
                view.displayError("Organisation name cannot be empty.");
                continue;
            }

            businessNumber = view.getInput("Business number: ");
            if (businessNumber.isEmpty()){
                view.displayError("Business number cannot be empty.");
                continue;
            } else if (! verificationService.verifyEntertainmentProvider(businessNumber)) {
                continue;
            } else if (EPAccountAlreadyExists(email, orgName, businessNumber)){
                continue;
            }

            name  = view.getInput("Name: ");
            if (name.isEmpty()){
                view.displayError("Main contact name cannot be empty.");
                continue;
            }

            description = view.getInput("Description: ");
            if (description.isEmpty()){
                view.displayError("empty");
                continue;
            }

            EntertainmentProvider newEP = new EntertainmentProvider(email, password, orgName, businessNumber, name, description);
            users.put(businessNumber, newEP);
            this.currentUser = newEP;

            view.displaySuccess("Register successful!");
            return;
        }
    }

    private boolean EPAccountAlreadyExists(String email, String orgName, String businessNumber) {
        User existedUser = users.get(businessNumber);
        if (existedUser instanceof EntertainmentProvider existedEP){
            if (existedEP.getOrgName().equals(orgName))
            view.displayError("This entertainment provider already exists! ");
            return true;
        }
        return false; // Placeholder return value
    }

    public void editPreferences() {
        if (!checkCurrentUserIsStudent()) {
            view.displayError("Only students can edit preferences");
            return;
        }

        Student student = (Student) currentUser;

        while (true) {
            String input = view.getInput("\nSelect up to 3 preferences from: music, theatre, dance, movie, sports \n"
                    + "Enter preferences (separated by commas): ");

            String[] preferences = input.split(",");
            if (preferences.length > 3) {
                view.displayError("You can select up to 3 preferences. Please try again.");
                continue;
            }

            List<String> valid = new ArrayList<>();
            boolean isValid = true;

            for (String pref : preferences) {
                String p = pref.trim().toLowerCase();

                if (!(p.equals("music") || p.equals("theatre") || p.equals("dance")
                        || p.equals("movie") || p.equals("sport") || p.equals("game"))) {
                    view.displayError("Invalid preference: " + p + ". Please try again.");
                    isValid = false;
                    break;
                }

                if (valid.contains(p)) {
                    view.displayError("Duplicate preference: " + p + ". Please try again");
                    isValid = false;
                    break;
                }

                valid.add(p);
            }

            if (!isValid) {
                continue;
            }

            student.getStudentPreferences().updatePreferences(input);
            view.displaySuccess("Preferences updated.");
            return;
        }
    }

    private void addUser(User user) {
        // Implementation for adding a user to the system
    }

    private void addPreregisteredUsers() throws FileNotFoundException {
        if (PREREGISTERED_USERS_FILE_PATH != null){
            Scanner scanner = new Scanner(new File(PREREGISTERED_USERS_FILE_PATH));
            while (scanner.hasNextLine()){
                String userInfos = scanner.nextLine().trim();

                String[] userInfoArray = userInfos.split(",");
                if (userInfoArray.length != 4) {
                    System.out.println("Invalid line in preregistered users file: " + userInfos);
                    continue;
                }
                String email = userInfoArray[0].trim();
                String password = userInfoArray[1].trim();
                String name = userInfoArray[2].trim();
                int phoneNumber = Integer.parseInt(userInfoArray[3].trim());

                Student student = new Student(email, password, name, phoneNumber);
                users.put(email, student);
            }
        }

        if (PREREGISTERED_ADMIN_FILE_PATH != null){
            Scanner scanner = new Scanner(new File(PREREGISTERED_ADMIN_FILE_PATH));
            while (scanner.hasNextLine()){
                String userInfos = scanner.nextLine();
                String[] userInfoArray = userInfos.split(",");
                String email = userInfoArray[0].trim();
                String password = userInfoArray[1].trim();
                String name = userInfoArray[2].trim();

                AdminStaff adminStaff = new AdminStaff(email, password, name);

                users.put(email, adminStaff);
            }
        }


    }

    private EntertainmentProvider getEntertainmentProviderOwningEvent(long eventNumber){
        return null;
    }

    // Getter and Setter
    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }
}