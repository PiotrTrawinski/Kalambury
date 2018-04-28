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
import javafx.stage.Stage;



public class WelcomeWindowController implements Initializable {
    private Kalambury kalambury;
    @FXML Label label_create_game;
    @FXML Label label_join_game;
    public void updateTime(){

    }
    
    public void onCreateGameClicked(){
        try{kalambury.showMainWindow();}
        catch(Exception ex){System.out.println(ex.getMessage());}
        Stage stage = (Stage) label_create_game.getScene().getWindow();
        //stage.close();
    }
    
    public void setKalambury(Kalambury k){
        System.out.print("Set kalambury");
        kalambury = k;
    }
    public void onJoinGameClicked(){
        System.out.println("join");
        
    }
    @Override public void initialize(URL url, ResourceBundle rb) {
       
    }    
}
