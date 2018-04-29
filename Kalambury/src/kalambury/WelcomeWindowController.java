package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Task;
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
    @FXML Label label_info;
    @FXML TextField textfield_port;
    @FXML TextField textfield_ip;
    @FXML TextField textfield_nick;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public void updateTime(){

    }
    
    public void setKalambury(Kalambury k){
        kalambury = k;
    }
    
    public void onCreateGameClicked(){

        switchToMainStage();
        //set the port that server is working on, and start it on a new thread
        Server.setPort(Integer.parseInt(textfield_port.getText()));
        Thread serverThread = new Thread(()->Server.start());
        serverThread.setDaemon(true);   // close with application
        serverThread.start();
    }
    
    
    public void onJoinGameClicked(){
        Client.setIP(textfield_ip.getText());
        Client.setPort(Integer.parseInt(textfield_port.getText()));
        Client.setNick(textfield_nick.getText());
        label_info.setText("Connecting...");
        Task<ConnectResult> serverConnectTask = new ServerConnectTask(textfield_ip.getText(),Integer.parseInt(textfield_port.getText()));
        
        executor.submit(serverConnectTask);

        serverConnectTask.setOnSucceeded(event->{
            
            if(Client.isSocketSet()){
                System.out.print("Socket it set");  
                label_info.setText("Connection established");
                switchToMainStage();
            }
            else{
                label_info.setText("Failed to connect.");
            }
            //executor.shutdown();
        });
    }
    
    private void switchToMainStage(){
        // open main window
        try{kalambury.showMainWindow();}
        catch(Exception ex){System.out.println(ex.getMessage());}
        // close old window
        Stage stage = (Stage) label_create_game.getScene().getWindow();
        stage.close();
        
    }
    
    
    
    @Override public void initialize(URL url, ResourceBundle rb) {
       
    }    
}
