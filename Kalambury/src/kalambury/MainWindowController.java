package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;


public class MainWindowController implements Initializable {
    
    @FXML private TimeLabel label_time;
    @FXML private Button button_test;
    
    @FXML private DrawingBoard canvas;
    @FXML private Pane pane;
    
    @FXML private Slider thicknessSlider;
    @FXML private Label thicknessLabel;
    
    @FXML private Pane chosenColorPane;
    @FXML private Pane colorChooserPane;
    
    @FXML private Label TurnLabel;
    
    @FXML private TableView scoreTableView;
    @FXML private TableColumn scoreTablePlayerColumn;
    @FXML private TableColumn scoreTablePointsColumn;
    
    @FXML private TextArea chatLog;
    @FXML private TextField chatInput;
    
    /*
        Canvas FXML functions
    */
    @FXML public void onMouseDragged(MouseEvent me){
        Point2D canvasLocation = canvas.sceneToLocal(me.getX(), me.getY());
        canvas.mouseMovedTo((int)canvasLocation.getX(), (int)canvasLocation.getY());
    }
    @FXML public void onMousePressedInDrawingBoard(MouseEvent me){
        canvas.startDrawing((int)me.getX(), (int)me.getY());
    }
    @FXML public void onMouseReleased(MouseEvent me){
        canvas.stopDrawing();
    }
    @FXML public void onKeyPressed(KeyEvent ke){
        if(ke.getCode() == KeyCode.P){
            canvas.increaseLineThickness();
        } 
        else if(ke.getCode() == KeyCode.MINUS){
            canvas.decreaseLineThickness();
        }
    }
    
    
    
    @FXML private void handleButtonAction(ActionEvent event) {
        
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
    
    @Override public void initialize(URL url, ResourceBundle rb) {
        button_test.setOnAction(this::test_button_clicked);
        
        canvas.bindSize(pane.widthProperty(), pane.heightProperty());
        canvas.setAspectRatio(16.0/9.0);
    }    
}
