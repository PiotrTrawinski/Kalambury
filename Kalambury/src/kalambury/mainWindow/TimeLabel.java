package kalambury.mainWindow;

import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Label;
import kalambury.client.Client;


public class TimeLabel extends Label{
    private volatile long startTime = 0;
    private volatile long timePeriod = 0;
    private final Thread thread;
    
    public TimeLabel(){
        super();
        thread = new Thread(()->startUpdating());
        thread.setDaemon(true);
    }
    
    public void startThread(){
        thread.start();
    }
    
    public void setNew(long startTime, long timePeriodInSeconds){
        this.startTime = startTime;
        this.timePeriod = timePeriodInSeconds;
    }
    
    private void startUpdating(){
        while(true){
            long time = timePeriod*1000 + startTime - Client.getTime();
            String stringTime;
            
            if(time > 0){
                stringTime = String.format("%02d:%02d", 
                    TimeUnit.MILLISECONDS.toMinutes(time),
                    TimeUnit.MILLISECONDS.toSeconds(time) 
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
                );   
            } else {
                stringTime = "00:00";
            }

            Platform.runLater(() -> {
                setText(stringTime); 
            });     

            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
