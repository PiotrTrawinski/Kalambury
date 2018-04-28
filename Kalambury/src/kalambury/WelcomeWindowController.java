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
    @FXML TextField textfield_port;
    @FXML TextField textfield_ip;
    @FXML TextField textfield_nick;
    
    public void updateTime(){

    }
    
    public void onCreateGameClicked(){
        // open main window
        try{kalambury.showMainWindow();}
        catch(Exception ex){System.out.println(ex.getMessage());}
        
        // close old window
        Stage stage = (Stage) label_create_game.getScene().getWindow();
        stage.close();
        
        //set the port that server is working on, and start it on a new thread
        Server.setPort(Integer.parseInt(textfield_port.getText()));
        Thread serverThread = new Thread(()->Server.start());
        serverThread.start();

    }
    
    public void setKalambury(Kalambury k){
        System.out.print("Set kalambury");
        kalambury = k;
    }
    public void onJoinGameClicked(){
        Client.setIP(textfield_ip.getText());
        Client.setPort(Integer.parseInt(textfield_port.getText()));
        Client.setNick(textfield_nick.getText());
        Client.connectToServer();
        
    }
    @Override public void initialize(URL url, ResourceBundle rb) {
       
    }    
}
