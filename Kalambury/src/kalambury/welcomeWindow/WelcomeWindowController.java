package kalambury.welcomeWindow;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kalambury.client.Client;
import kalambury.Kalambury;
import kalambury.server.Server;


public class WelcomeWindowController implements Initializable {
    private Kalambury kalambury;
    
    @FXML Label label_create_game;
    @FXML Label label_join_game;
    @FXML Label label_info;
    @FXML TextField textfield_port;
    @FXML TextField textfield_ip;
    @FXML TextField textfield_nick;
    
    
    public void setKalambury(Kalambury k){
        kalambury = k;
    }
    
    public void onCreateGameClicked(){
        Server.initialize(Integer.parseInt(textfield_port.getText()));
        Client.initialize(
            textfield_ip.getText(), 
            Integer.parseInt(textfield_port.getText()), 
            textfield_nick.getText(),
            label_info,
            this::switchToMainStage,
            true
        );
    }
    
    
    public void onJoinGameClicked(){
        Client.initialize(
            textfield_ip.getText(), 
            Integer.parseInt(textfield_port.getText()), 
            textfield_nick.getText(),
            label_info,
            this::switchToMainStage,
            false
        );
    }
    
    private void switchToMainStage(){
        // open main window
        try{
            kalambury.showMainWindow();
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        
        // close old window
        Stage stage = (Stage) label_create_game.getScene().getWindow();
        stage.close();
    }
    
    
    
    @Override public void initialize(URL url, ResourceBundle rb) {
       
    }    
}