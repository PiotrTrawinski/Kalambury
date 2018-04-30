/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kalambury.mainWindow;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;


/**
 *
 * @author honzi
 */
public class TimeLabel extends Label{
    public TimeLabel(){
        
    }
    
    void startUpdating(long startMinutes, long startSeconds){
         
        
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
            setText(finalText); 
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
    
}
