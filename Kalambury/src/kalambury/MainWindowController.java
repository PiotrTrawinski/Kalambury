package kalambury;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;


public class MainWindowController implements Initializable {
    
    @FXML private TimeLabel label_time;
    @FXML private Button chatSendButton;
    
    @FXML private DrawingBoard drawingBoard;
    @FXML private Pane pane;
    
    @FXML private Slider thicknessSlider;
    @FXML private Label thicknessLabel;
    
    @FXML private Button pencilButton;
    @FXML private Button colorPickerButton;
    @FXML private Button bucketButton;
    
    @FXML private Canvas chosenColorView;
    @FXML private Pane chosenColorPane;
    @FXML private Canvas colorChooser;
    @FXML private Pane colorChooserPane;
    @FXML private Slider colorBrightnessSlider;
    @FXML private Canvas colorBrightnessCanvas;
    @FXML private Pane colorBrightnessPane;
    
    @FXML private Label TurnLabel;
    
    @FXML private TableView scoreTableView;
    @FXML private TableColumn<Player,String> scoreTableNickNameColumn;
    @FXML private TableColumn<Player,Number> scoreTablePointsColumn;
    
    @FXML private TextFlow chatLog;
    @FXML private ScrollPane chatLogPane;
    @FXML private TextField chatInput;
    
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button pauseButton;
    @FXML private Button skipButton;
    @FXML private Button skipRequestButton;
    @FXML private Button quitGameButton;
    
    private ColorWidget colorWidget;
    
    private final ObservableList<Player> players = FXCollections.observableArrayList();
    
    /*
        Mouse events
    */
    @FXML public void onMousePressedInDrawingBoard(MouseEvent me){
        drawingBoard.mousePressed((int)me.getX(), (int)me.getY());
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
        drawingBoard.mouseReleased();
        colorWidget.stopChoosing();
    }
    
    
    @FXML private void onPencilButtonClicked(){
        drawingBoard.setDrawingTool(DrawingTool.PENCIL);
        pencilButton.setStyle("-fx-background-color: #AAAAAA;");
        colorPickerButton.setStyle("");
        bucketButton.setStyle("");
    }
    @FXML private void onColorPickerButtonClicked(){
        drawingBoard.setDrawingTool(DrawingTool.COLOR_PICKER);
        pencilButton.setStyle("");
        colorPickerButton.setStyle("-fx-background-color: #AAAAAA;");
        bucketButton.setStyle("");
    }
    @FXML private void onBucketButtonClicked(){
        drawingBoard.setDrawingTool(DrawingTool.BUCKET);
        pencilButton.setStyle("");
        colorPickerButton.setStyle("");
        bucketButton.setStyle("-fx-background-color: #AAAAAA;");
    }
    
    
    @FXML public void onQuitGameButtonPressed(){
        playButton.setVisible(!playButton.isVisible());
        stopButton.setVisible(!stopButton.isVisible());
        pauseButton.setVisible(!pauseButton.isVisible());
        skipButton.setVisible(!skipButton.isVisible());
        skipRequestButton.setVisible(!skipRequestButton.isVisible());
    }
    
    @FXML private void enteredChatMessage(){
        if(!chatInput.getText().isEmpty()){
            String chatMessage = chatInput.getText();
            chatInput.setText("");
            
            String nickName = players.get(0).getNickName();
            
            Text textNick = new Text("[" + nickName + "] ");
            textNick.setFill(Color.BLUE);
            textNick.setStyle("-fx-font-weight: bold;");
            
            Text textMessage = new Text(chatMessage + "\n");
            textMessage.setFill(Color.BLACK);
            
            chatLog.getChildren().add(textNick);
            chatLog.getChildren().add(textMessage);
            chatLogPane.setVvalue(1);
            
            // send to server chatMessage
            SendableData mess = new ChatMessageData(Client.getNick(),chatMessage,10000.0);
            Client.sendMessage(mess);
            
        }
    }
    
    public void test_button_clicked(ActionEvent event){
        // these parameters will be chosen from GUI by host
        //start_time_measuring_thread(1,50);

        
       
        //Client.sendMessage(message);
    }
    
    public void start_time_measuring_thread(long min, long sec){
        Thread update_time_thread = new Thread(()->label_time.startUpdating(min,sec));
        update_time_thread.setDaemon(true);
        update_time_thread.start();
    }
    
    
    public void updateTime(){

    }
    
    @Override public void initialize(URL url, ResourceBundle rb) {
        //chatSendButton.setOnAction(this::test_button_clicked);
        
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
        
        // drawing tools
        Image penImage = new Image(getClass().getResourceAsStream("images/pencil.png"));
        pencilButton.setGraphic(new ImageView(penImage));
        pencilButton.setStyle("-fx-background-color: #AAAAAA;");
        Image bucketImage = new Image(getClass().getResourceAsStream("images/bucket.png"));
        bucketButton.setGraphic(new ImageView(bucketImage));
        Image colorPickerImage = new Image(getClass().getResourceAsStream("images/colorPicker.png"));
        colorPickerButton.setGraphic(new ImageView(colorPickerImage));
        
        // drawingBoard
        drawingBoard.bindSize(pane.widthProperty(), pane.heightProperty());
        drawingBoard.setAspectRatio(16.0/9.0);
        drawingBoard.setColorWidget(colorWidget);
        
        // chat
        chatLog.prefWidthProperty().bind(chatLogPane.widthProperty());
        
        // player tableView
        scoreTableNickNameColumn.setCellValueFactory(
                player -> player.getValue().getNickNameProperty()
        );
        scoreTablePointsColumn.setCellValueFactory(
                player -> player.getValue().getScoreProperty()
        );
        scoreTableView.setItems(players);
        // example adding players to score table
        players.add(new Player("Piotr", 0));
        players.add(new Player("Oliwier", 13));
    }    
}
