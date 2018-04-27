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
    
    @FXML private TimeLabel label_time;
    @FXML private Button button_test;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        
    }
    
    
    public void test_button_clicked(ActionEvent event){
        // these parameters will be chosen from GUI by host
        start_time_measuring_thread(1,50);
    }
    
    public void start_time_measuring_thread(long min, long sec){
        Thread update_time_thread = new Thread(()->label_time.startUpdating(min,sec));
        update_time_thread.setDaemon(true);
        update_time_thread.start();
    }
    
    
    public void updateTime(){

    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        button_test.setOnAction(this::test_button_clicked);
        
    }    
    
}
