package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;



public class WelcomeWindowController implements Initializable {
    
    @FXML Label label_create_game;
    @FXML Label label_join_game;
    public void updateTime(){

    }
    
    public void onCreateGameClicked(){
        System.out.println("create");
    }
    
    public void onJoinGameClicked(){
        System.out.println("join");
        
    }
    @Override public void initialize(URL url, ResourceBundle rb) {
       
    }    
}
