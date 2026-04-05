import controller.MenuController;
import interfaces.TextUserInterface;
import interfaces.View;

public class Main{
    public static void main (String[] args){
        String userFilePath = "src/resources/student.txt";
        String adminFilePath = "src/resources/admins.txt";

        View appView = new TextUserInterface();
        MenuController menuController = new MenuController(appView, userFilePath, adminFilePath);
        menuController.mainMenu();
    }

}