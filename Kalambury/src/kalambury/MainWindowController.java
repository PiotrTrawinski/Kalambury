package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;


public class MainWindowController implements Initializable {
    
    @FXML private TimeLabel label_time;
    @FXML private Button button_test;
    
    @FXML private DrawingBoard drawingBoard;
    @FXML private Pane pane;
    
    @FXML private Slider thicknessSlider;
    @FXML private Label thicknessLabel;
    
    @FXML private Canvas chosenColorView;
    @FXML private Pane chosenColorPane;
    @FXML private Canvas colorChooser;
    @FXML private Pane colorChooserPane;
    @FXML private Slider colorBrightnessSlider;
    @FXML private Canvas colorBrightnessCanvas;
    @FXML private Pane colorBrightnessPane;
    
    @FXML private Label TurnLabel;
    
    @FXML private TableView scoreTableView;
    @FXML private TableColumn scoreTablePlayerColumn;
    @FXML private TableColumn scoreTablePointsColumn;
    
    @FXML private TextArea chatLog;
    @FXML private TextField chatInput;
    
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button pauseButton;
    @FXML private Button skipButton;
    @FXML private Button skipRequestButton;
    @FXML private Button quitGameButton;
    
    private ColorWidget colorWidget;
    
    /*
        Mouse events
    */
    @FXML public void onMousePressedInDrawingBoard(MouseEvent me){
        drawingBoard.startDrawing((int)me.getX(), (int)me.getY());
    }
    @FXML public void onMousePressedInColorChooser(MouseEvent me){
        colorWidget.startChoosing((int)me.getX(), (int)me.getY());
    }
    
    @FXML public void onMouseDragged(MouseEvent me){
        Point2D canvasLocation = drawingBoard.sceneToLocal(me.getX(), me.getY());
        drawingBoard.mouseMovedTo((int)canvasLocation.getX(), (int)canvasLocation.getY());
        
        Point2D colorChooserLocation = colorChooser.sceneToLocal(me.getX(), me.getY());
        colorWidget.mouseMovedTo((int)colorChooserLocation.getX(), (int)colorChooserLocation.getY());
    }
    @FXML public void onMouseReleased(MouseEvent me){
        drawingBoard.stopDrawing();
        colorWidget.stopChoosing();
    }
    
    
    
    @FXML public void onQuitGameButtonPressed(){
        playButton.setVisible(!playButton.isVisible());
        stopButton.setVisible(!stopButton.isVisible());
        pauseButton.setVisible(!pauseButton.isVisible());
        skipButton.setVisible(!skipButton.isVisible());
        skipRequestButton.setVisible(!skipRequestButton.isVisible());
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
        
        // colorChooser
        colorWidget = new ColorWidget(
                chosenColorView, chosenColorPane, 
                colorChooser, colorChooserPane,
                colorBrightnessSlider, colorBrightnessCanvas, colorBrightnessPane
        );
        
        // thicknessSlider
        thicknessSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue){
                thicknessLabel.setText(Integer.toString(newValue.intValue()));
                drawingBoard.setLineThickness(newValue.intValue());
            }
        });
        
        // drawingBoard
        drawingBoard.bindSize(pane.widthProperty(), pane.heightProperty());
        drawingBoard.setAspectRatio(16.0/9.0);
        drawingBoard.setColorWidget(colorWidget);
    }    
}
