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
        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WelcomeWindowFXML.fxml"));
        Parent root = fxmlLoader.load();
        WelcomeWindowController controller = fxmlLoader.getController();
        controller.setKalambury(this);
        
        
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
    
    
    public void showMainWindow() throws Exception{
        System.out.print("new window");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindowFXML.fxml"));
        Parent root = fxmlLoader.load();
        
        Scene scene = new Scene(root);
        scene.getRoot().requestFocus();
        
        this.stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Kalambury");
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
        
    }
    
    

    public static void main(String[] args) {
        launch(args);
    }
}
