package kalambury;

import kalambury.welcomeWindow.WelcomeWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kalambury.client.Client;
import kalambury.mainWindow.MainWindowController;


public class Kalambury extends Application {
    Stage welcomeStage;
    Stage mainStage;
    MainWindowController mainWindowController;
    
    @Override public void start(Stage stage) throws Exception {

        // Create main window
        FXMLLoader loaderMain = new FXMLLoader(getClass().getResource("mainWindow/MainFXML.fxml"));
        Parent rootMain = (Parent)loaderMain.load();
        mainWindowController = loaderMain.getController();
        Client.setKalambury(this);
        Scene sceneMain =  new Scene(rootMain);
        mainStage = new Stage();
        mainStage.setTitle("Kalambury");
        mainStage.setScene(sceneMain);
        
        // create welcome window
        FXMLLoader loaderWelcome = new FXMLLoader(getClass().getResource("welcomeWindow/WelcomeWindowFXML.fxml"));
        Parent welcomeRoot = loaderWelcome.load();
        WelcomeWindowController welcomeController = loaderWelcome.getController();
        welcomeController.setKalambury(this);
        
        Scene sceneWelcome = new Scene(welcomeRoot);
        sceneWelcome.getRoot().requestFocus();
        welcomeStage = stage;
        welcomeStage.setScene(sceneWelcome);
        welcomeStage.setTitle("Kalambury");
        
        // show welcome window
        welcomeStage.show();
        welcomeStage.setMinHeight(stage.getHeight());
        welcomeStage.setMinWidth(stage.getWidth());
    }
    
    
    public void showMainWindow(){
        Platform.runLater(() -> {
            welcomeStage.close();
            mainStage.show();
            mainStage.setMinHeight(mainStage.getHeight());
            mainStage.setMinWidth(mainStage.getWidth());
        });
    }
    
    public void showWelcomeWindow(){
        Platform.runLater(() -> {
            mainStage.close();
            welcomeStage.show();
            welcomeStage.setMinHeight(welcomeStage.getHeight());
            welcomeStage.setMinWidth(welcomeStage.getWidth());
        });
    }
    
    public MainWindowController getMainWindowController(){
        return mainWindowController;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
