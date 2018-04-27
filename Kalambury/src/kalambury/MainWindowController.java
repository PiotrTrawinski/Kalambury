/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.concurrent.Task;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Piotr
 */
public class MainWindowController implements Initializable {
    
    @FXML private Label label_time;
    @FXML private Button button_test;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        
    }
    public void test_button_clicked(ActionEvent event){
        Thread update_time_thread = new Thread(()->updateTime());
        update_time_thread.setDaemon(true);
        update_time_thread.start();
        
        
        
      
    }
    public void updateTime(){
        
        // these parameters will be chosen from GUI by host
        long startSeconds = 45;
        long startMinutes = 1;
        
        long maxDrawTime = startMinutes * 60 + startSeconds;
        
        
        boolean stopStatement = true;
        long startTimeMS = System.currentTimeMillis();
        while(stopStatement){
    
        // calculating the time that is left
        long estimatedTimeMS = System.currentTimeMillis() - startTimeMS;
        final long estimatedTimeMS1000 = estimatedTimeMS - estimatedTimeMS % 1000; // minus the time added by sleep mistake
        
        long timeLeftInSeconds = maxDrawTime - estimatedTimeMS / 1000;
        long minutes = timeLeftInSeconds / 60;
        long seconds = timeLeftInSeconds % 60;
        
        
        // text formatting
        String formattedText = "";
        if(minutes < 10){
            formattedText += "0";
        }
        formattedText += Long.toString(minutes);
        formattedText += ":";
        if(seconds < 10){
            formattedText += "0";
        }
        formattedText += Long.toString(seconds);
        final String finalText = formattedText;
        
        
        // updating time label
        Platform.runLater(() -> {
            label_time.setText(finalText); 
        });     
            
        
        // sleep for 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        // ending statement - time passed
        if(timeLeftInSeconds == 0){
            
            
        }
            
        }
       
        
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        button_test.setOnAction(this::test_button_clicked);
        
    }    
    
}
