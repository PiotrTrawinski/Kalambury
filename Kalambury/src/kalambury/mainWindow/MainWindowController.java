package kalambury.mainWindow;

import java.awt.Dimension;
import java.awt.Toolkit;
import kalambury.mainWindow.drawingBoard.ColorWidget;
import kalambury.mainWindow.drawingBoard.DrawingBoard;
import kalambury.mainWindow.drawingBoard.DrawingTool;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import kalambury.client.Client;
import kalambury.sendableData.DrawingEndSignal;
import kalambury.sendableData.DrawingStartSignal;
import kalambury.server.Server;


public class MainWindowController implements Initializable {
    @FXML private GridPane gridPane;
    
    @FXML private Label thicknessLabelLabel;
    @FXML private Label drawToolsLabel;
    @FXML private Label colorLabel;
    @FXML private Label actionsLabel;
    
    @FXML private TimeLabel timeLabel;
    @FXML private Button chatSendButton;
    
    @FXML private DrawingBoard drawingBoard;
    @FXML private Pane pane;
    
    @FXML private Slider thicknessSlider;
    @FXML private Label thicknessLabel;
    
    @FXML private HBox drawToolsHBox;
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
    @FXML private Label wyrazLabel;
    
    @FXML private TableView scoreTableView;
    @FXML private TableColumn<Player,String> scoreTableNickNameColumn;
    @FXML private TableColumn<Player,Number> scoreTablePointsColumn;
    
    @FXML private TextFlow chatLog;
    @FXML private ScrollPane chatLogPane;
    @FXML private TextField chatInput;
    
    @FXML private GridPane actionsGridPane;
    @FXML private Button playButton;
    @FXML private Button stopButton;
    @FXML private Button pauseButton;
    @FXML private Button skipButton;
    @FXML private Button skipRequestButton;
    @FXML private Button quitGameButton;
    
    private ColorWidget colorWidget;
    private Chat chat;
    
    
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
        DrawingStartSignal end = new DrawingStartSignal();
        Server.sendExcept(end, -1);
        
        /*
        playButton.setVisible(!playButton.isVisible());
        stopButton.setVisible(!stopButton.isVisible());
        pauseButton.setVisible(!pauseButton.isVisible());
        skipButton.setVisible(!skipButton.isVisible());
        skipRequestButton.setVisible(!skipRequestButton.isVisible());
        */
    }
    
    @FXML private void enteredChatMessage(){
        chat.handleNewClientMessage();
    }
    public TextField getChatInputField(){
        return chatInput;
    }
    
    public void startTimeLabelThread(){
        Thread timeLabelThread = new Thread(()->timeLabel.startUpdating());
        timeLabelThread.setDaemon(true);
        timeLabelThread.start();
    }
    
    
    public void updateTime(){

    }
    
    private void scaleGridPane(GridPane gridPane, double scalingFactor){
        for(int i = 0; i < gridPane.getColumnConstraints().size(); ++i){
            ColumnConstraints colCon = gridPane.getColumnConstraints().get(i);
            colCon.setMinWidth((int)(colCon.getMinWidth()*scalingFactor));
            colCon.setMaxWidth((int)(colCon.getMaxWidth()*scalingFactor));
            colCon.setPrefWidth((int)(colCon.getPrefWidth()*scalingFactor));
        }
        for(int i = 0; i < gridPane.getRowConstraints().size(); ++i){
            RowConstraints rowCon = gridPane.getRowConstraints().get(i);
            rowCon.setMinHeight((int)(rowCon.getMinHeight()*scalingFactor));
            rowCon.setMaxHeight((int)(rowCon.getMaxHeight()*scalingFactor));
            rowCon.setPrefHeight((int)(rowCon.getPrefHeight()*scalingFactor));
        }
    }
    
    private void scaleLabel(Label label, String fontFamily, double scalingFactor){
        Font timeLabelFont = label.getFont();
        label.setFont(new Font(fontFamily, timeLabelFont.getSize()*scalingFactor));
    }
    
    private void scaleButton(Button button, String fontFamily, double scalingFactor){
        button.setMinWidth((int)(button.getMinWidth()*scalingFactor));
        button.setMaxWidth((int)(button.getMaxWidth()*scalingFactor));
        button.setPrefWidth((int)(button.getPrefWidth()*scalingFactor));
        button.setMinHeight((int)(button.getMinHeight()*scalingFactor));
        button.setMaxHeight((int)(button.getMaxHeight()*scalingFactor));
        button.setPrefHeight((int)(button.getPrefHeight()*scalingFactor));
        Font timeLabelFont = button.getFont();
        button.setFont(new Font(fontFamily, timeLabelFont.getSize()*scalingFactor));
    }
    
    private void scalePane(Pane pane, double scalingFactor){
        pane.setMinWidth((int)(pane.getMinWidth()*scalingFactor));
        pane.setMaxWidth((int)(pane.getMaxWidth()*scalingFactor));
        pane.setPrefWidth((int)(pane.getPrefWidth()*scalingFactor));
        pane.setMinHeight((int)(pane.getMinHeight()*scalingFactor));
        pane.setMaxHeight((int)(pane.getMaxHeight()*scalingFactor));
        pane.setPrefHeight((int)(pane.getPrefHeight()*scalingFactor));
    }
    
    private void scaleCanvas(Canvas canvas, double scalingFactor){
        canvas.setWidth((int)(canvas.getWidth()*scalingFactor));
        canvas.setHeight((int)(canvas.getHeight()*scalingFactor));
    }
    
    @Override public void initialize(URL url, ResourceBundle rb) { 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        double scalingFactor = width/1366.0;
        scaleGridPane(gridPane, scalingFactor);
        scaleGridPane(actionsGridPane, scalingFactor);
        scaleLabel(timeLabel, "System Bold", scalingFactor);
        scaleLabel(TurnLabel, "System Bold", scalingFactor);
        scaleLabel(wyrazLabel, "System Regular", scalingFactor);
        scaleLabel(thicknessLabel, "System Bold", scalingFactor);
        scaleLabel(thicknessLabelLabel, "System Bold", scalingFactor);
        scaleLabel(drawToolsLabel, "System Bold", scalingFactor);
        scaleLabel(colorLabel, "System Bold", scalingFactor);
        scaleLabel(actionsLabel, "System Bold", scalingFactor);
        scaleButton(pencilButton, "System Regular", scalingFactor);
        scaleButton(colorPickerButton, "System Regular", scalingFactor);
        scaleButton(bucketButton, "System Regular", scalingFactor);
        scaleButton(playButton, "System Regular", scalingFactor);
        scaleButton(stopButton, "System Regular", scalingFactor);
        scaleButton(pauseButton, "System Regular", scalingFactor);
        scaleButton(skipButton, "System Regular", scalingFactor);
        scaleButton(skipRequestButton, "System Regular", scalingFactor);
        scaleButton(quitGameButton, "System Regular", scalingFactor);
        scaleButton(chatSendButton, "System Regular", scalingFactor);
        drawToolsHBox.setSpacing(drawToolsHBox.getSpacing()*scalingFactor);
        scaleCanvas(chosenColorView, scalingFactor);
        scaleCanvas(colorChooser, scalingFactor);
        scaleCanvas(colorBrightnessCanvas, scalingFactor);
         
        startTimeLabelThread();
        
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
        chat = new Chat(chatLog, chatLogPane, chatInput);
        Font font = chatInput.getFont();
        chatInput.setFont(new Font("System Regular", font.getSize()*scalingFactor));
        
        // player tableView
        scoreTableNickNameColumn.setCellValueFactory(
                player -> player.getValue().getNickNameProperty()
        );
        TableCell<Player, String> tableCell;
        scoreTableNickNameColumn.setCellFactory(new Callback<TableColumn<Player, String>, TableCell<Player,String>>() {
            @Override public TableCell call(TableColumn param) {
                return new TableCell<Player, String>() {
                    @Override public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty()){
                            setText("");
                        } else{
                            setText(item);
                        }
                    }
                };
            }
        });
        scoreTablePointsColumn.setCellValueFactory(
                player -> player.getValue().getScoreProperty()
        );
        scoreTableView.setItems(Client.getPlayers());
        
        Client.setChat(chat);
        Client.setDrawingBoard(drawingBoard);
        
        drawingBoard.setDisable(true);
    }    
}
