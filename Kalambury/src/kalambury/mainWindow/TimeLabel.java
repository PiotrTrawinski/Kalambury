package kalambury.mainWindow;

import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Label;
import kalambury.client.Client;


public class TimeLabel extends Label{
    
    public TimeLabel(){
        super();
    }
    
    void startUpdating(){
        while(true){
            long fullTimeInSeconds = Client.getTime() / 1000;
            long minutes = fullTimeInSeconds / 60;
            long seconds = fullTimeInSeconds % 60;
            long miliSeconds = Client.getTime() % 1000;
            
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


            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
        
        
        /*long maxDrawTime = startMinutes * 60 + startSeconds;
        
        boolean stopStatement = true;
        long startTimeMS = System.currentTimeMillis();
        while(stopStatement){
    
        // calculating the time that is left
        long estimatedTimeMS = System.currentTimeMillis() - startTimeMS;
        final long estimatedTimeMS1000 = estimatedTimeMS - estimatedTimeMS % 1000; // minus the time added by sleep mistake
        
        long timeLeftInSeconds = maxDrawTime - estimatedTimeMS / 1000;
        
        
        
            
        // ending statement - time passed
        if(timeLeftInSeconds == 0){
            
            
        }
            
        }*/
    }
}
