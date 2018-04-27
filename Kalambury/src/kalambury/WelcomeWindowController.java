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
    
    
    // trzeba zrobic funkcje zeby zmienialo tylko 1 style
    public void onCreateGameMouseEnter(){
        label_create_game.setStyle("-fx-background-color:#666666");
        label_create_game.setCursor(Cursor.HAND);
    }
    
    public void onCreateGameMouseExit(){
        label_create_game.setStyle("-fx-background-color:#C8C8C8");
    }
    
    
    
    public void onJoinGameMouseEnter(){
        label_join_game.setStyle("-fx-background-color:#666666");
        label_join_game.setCursor(Cursor.HAND);
    }
    
    public void onJoinGameMouseExit(){
        label_join_game.setStyle("-fx-background-color:#C8C8C8");
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
