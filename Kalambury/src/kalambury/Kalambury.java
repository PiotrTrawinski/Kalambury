package kalambury;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Kalambury extends Application {
    Stage welcomeStage;
    Stage mainStage;
    @Override
    public void start(Stage stage) throws Exception {
        
     
        // Create main window but don't show it
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainFXML.fxml"));
        Parent root = (Parent)loader.load();
        Scene scene =  new Scene(root);
        Stage s = new Stage();
        s.setTitle("Kalambury");
        s.setScene(scene);
        mainStage = s;
        //s.show();
        
        
        // init welcome window
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WelcomeWindowFXML.fxml"));
        Parent welcomeRoot = fxmlLoader.load();
        WelcomeWindowController controller = fxmlLoader.getController();
        controller.setKalambury(this);
        
        scene = new Scene(welcomeRoot);
        scene.getRoot().requestFocus();
        stage.setScene(scene);
        stage.setTitle("Kalambury");
        stage.show();
        this.welcomeStage = stage;
        
        
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());

    }
    
    
    public void showMainWindow() throws Exception{

        welcomeStage.close();
        mainStage.show();

        
    }
    
    

    public static void main(String[] args) {
        launch(args);
    }
}
