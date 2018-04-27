package kalambury;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Kalambury extends Application {
    Stage stage;
    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("WelcomeWindowFXML.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("WelcomeWindowFXML.fxml"));
        
        Scene scene = new Scene(root);
        // for mouse/keyboard events to work properly
        scene.getRoot().requestFocus();
        stage.setScene(scene);
        stage.setTitle("Kalambury");
        stage.show();
        this.stage = stage;
       
        
        // window's smallest size is the one it starts with
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
    }
    public void closeWelcomeWindow(){
        stage.close();
    }
    
    

    public static void main(String[] args) {
        launch(args);
    }
}
