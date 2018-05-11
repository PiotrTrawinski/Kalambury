package kalambury.mainWindow;

import java.awt.Dimension;
import java.awt.Toolkit;
import kalambury.mainWindow.drawingBoard.ColorWidget;
import kalambury.mainWindow.drawingBoard.DrawingBoard;
import kalambury.mainWindow.drawingBoard.DrawingTool;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.TableRow;
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
import kalambury.sendableData.ChatMessageData;
import kalambury.sendableData.FloodFillData;
import kalambury.sendableData.GamePasswordData;
import kalambury.sendableData.GameStartedData;
import kalambury.sendableData.LineDrawData;
import kalambury.sendableData.NewPlayerData;
import kalambury.sendableData.PlayerQuitData;
import kalambury.sendableData.SendableSignal;
import kalambury.sendableData.StartServerData;
import kalambury.sendableData.TurnEndedData;
import kalambury.sendableData.TurnStartedData;
import kalambury.server.Server;
import kalambury.server.SystemMessage;
import kalambury.server.SystemMessageType;


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
    
    @FXML private TurnLabel turnLabel;
    @FXML private Label passwordLabelLabel;
    @FXML private Label passwordLabel;
    
    @FXML private TableView scoreTableView;
    @FXML private TableColumn<Player, String> scoreTableNickNameColumn;
    @FXML private TableColumn<Player, Number> scoreTablePointsColumn;
    @FXML private TableColumn<Player, Boolean> scoreTableStateColumn;
    
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
    @FXML private Label numberOfTurnsLabelLabel;
    @FXML private Label numberOfTurnsLabel;
    @FXML private Slider numberOfTurnsSlider;
    @FXML private Label subTurnTimeLabelLabel;
    @FXML private Label subTurnTimeLabel;
    @FXML private Slider subTurnTimeSlider;
    
    private double scalingFactor;
    
    private ColorWidget colorWidget;
    private Chat chat;
    private static final ObservableList<Player> players = FXCollections.observableArrayList();
    
    
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
    
    
    /*
        drawing toolbox buttons clicked
    */
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
    
    
    /*
        host buttons clicked
    */
    @FXML public void onPlayButtonPressed(){
        int numberOfTurns = (int)numberOfTurnsSlider.getValue();
        int subTurnTime = (int)subTurnTimeSlider.getValue()*5;
        Server.startGame(numberOfTurns, subTurnTime);
        playButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        skipButton.setDisable(false);
    }
    @FXML public void onStopButtonPressed(){
        Server.stopGame();
        playButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        skipButton.setDisable(true);
    }
    @FXML public void onPauseButtonPressed(){
        Server.pauseGame();
        playButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(false);
        skipButton.setDisable(false);
    }
    @FXML public void onSkipButtonPressed(){
        Server.skipTurn();
    }
    
    /*
        client specific buttons clicked 
    */
    @FXML public void onSkipRequestButtonPressed(){
        chat.handleNewSystemMessage(new SystemMessage(
            "Poprosiłeś o pominięcie tury",
            Client.getTime(),
            SystemMessageType.Information
        ));
        Client.skipRequest();
    }
    
    
    /*
        buttons clicked
    */
    @FXML public void onQuitGameButtonPressed(){
        Client.quit();
    }
    
    
    /*
        chat events
    */
    @FXML private void enteredChatMessage(){
        chat.handleNewClientMessage();
    }

    
    /*
        update depending on data received from server
    */
    public void setPassword(GamePasswordData gpd){
        Platform.runLater(() -> {
            passwordLabel.setText(gpd != null ? gpd.password : "???");
        });
    }
    public void playerQuit(PlayerQuitData pqd){
        Platform.runLater(() -> {
            turnLabel.setNumberOfSubTurns(players.size());
        });
        Player player = players.get(pqd.index);
        chat.handleNewSystemMessage(new SystemMessage(
            "Gracz " + player.getNickName() + " wyszedł z gry",
            pqd.time,
            SystemMessageType.Information
        ));
        players.remove(player);
    }
    public void turnTimeOut(SendableSignal signal){
        Platform.runLater(() -> {
            drawingBoard.setDisable(true);
            skipButton.setDisable(true);
            timeLabel.setNew(0, 0);
        });
        updateDrawingPlayer(-1);
        chat.handleNewSystemMessage(new SystemMessage(
            "Koniec czasu! Nikt nie zgadł hasła", signal.time, SystemMessageType.Information
        ));
    }
    public void gamePaused(SendableSignal signal){
        chat.handleNewSystemMessage(new SystemMessage(
            "Gra zostanie wstrzymana po tej turze",
            signal.time,
            SystemMessageType.Information
        ));
    }
    public void skipRequest(SendableSignal signal){
        chat.handleNewSystemMessage(new SystemMessage(
            "Gracz poprosił o pominięcie tury", signal.time, SystemMessageType.Information
        ));
    }
    public void turnSkipped(SendableSignal signal){
        Platform.runLater(() -> {
            drawingBoard.setDisable(true);
            skipButton.setDisable(true);
            timeLabel.setNew(0, 0);
        });
        updateDrawingPlayer(-1);
        chat.handleNewSystemMessage(new SystemMessage(
            "Tura została pominięta",
            signal.time,
            SystemMessageType.Information
        ));
    }
    public void gameEnded(SendableSignal signal){
        Platform.runLater(() -> {
            drawingBoard.setDisable(true);
            timeLabel.setNew(0, 0);
            playButton.setDisable(false);
            pauseButton.setDisable(true);
            stopButton.setDisable(true);
            skipButton.setDisable(true);
        });
        updateDrawingPlayer(-1);
        
        String results = new String();
        for(int j = 0; j < players.size(); ++j){
            for(int i = 0; i < 19; ++i){
                results += " ";
            }
            results += players.get(j).getNickName() + ": " + Integer.toString(players.get(j).getScore());
            if(j != players.size() - 1){
                results += "\n";
            }
        }
        chat.handleNewSystemMessage(new SystemMessage(
            "Gra została zakończona. Wyniki:\n"+results, signal.time, SystemMessageType.Information
        ));
    }
    public void gameStarted(GameStartedData gsd){
        Platform.runLater(() -> {
            turnLabel.start(players.size(), gsd.numberOfTurns);
        });
        chat.handleNewSystemMessage(new SystemMessage(
            "Gra została rozpoczęta", gsd.time, SystemMessageType.Information
        ));
        for(int i = 0; i < players.size(); ++i){
            players.get(i).setScore(0);
        }
    }
    public void gameStopped(SendableSignal signal){
        drawingBoard.setDisable(true);
        updateDrawingPlayer(-1);
        chat.handleNewSystemMessage(new SystemMessage(
            "Gra została zakończona przez hosta", signal.time, SystemMessageType.Information
        ));
        timeLabel.setNew(0, 0);
    }
    public void turnEndedSignal(SendableSignal signal){
        Platform.runLater(() -> {
            drawingBoard.setDisable(true);
            skipButton.setDisable(true);
            timeLabel.setNew(0, 0);
        });
        updateDrawingPlayer(-1);
        chat.handleNewSystemMessage(new SystemMessage(
            "Koniec tury!", signal.time, SystemMessageType.Information
        ));
    }
    public void turnEnded(TurnEndedData ted){
        chat.handleNewSystemMessage(new SystemMessage(
            ted.winnerNickName + " wygrał!",
            ted.time,
            SystemMessageType.Information
        ));
        for(int i = 0; i < players.size(); ++i){
            players.get(i).setScore(ted.updatedScores.get(i));
        }
    }
    public void turnStarted(TurnStartedData tsd){
        Platform.runLater(() -> {
            turnLabel.nextTurn();
            skipButton.setDisable(false);
        });
        drawingBoard.clear();
        timeLabel.setNew(tsd.startTime, tsd.turnTime);
        updateDrawingPlayer(tsd.drawingPlayerId);
        if(tsd.isDrawing){
            drawingBoard.setDisable(false);
            chat.handleNewSystemMessage(new SystemMessage(
                "Rysuj hasło!",
                tsd.startTime,
                SystemMessageType.Information
            ));
        } else {
            drawingBoard.setDisable(true);
            setPassword(null);
            chat.handleNewSystemMessage(new SystemMessage(
                "Zgaduj hasło!",
                tsd.startTime,
                SystemMessageType.Information
            ));
        }
    }
    public void newPlayer(NewPlayerData npd){
        players.add(new Player(npd.nickName, 0, npd.id));
        chat.handleNewSystemMessage(new SystemMessage(
            npd.nickName + " dołączył do gry",
            npd.time,
            SystemMessageType.Information
        ));
    }
    public void floodFill(FloodFillData ffd){
        drawingBoard.floodFillRemote(ffd);
    }
    public void lineDraw(LineDrawData ldd){
        drawingBoard.drawLineRemote(ldd);
    }
    public void chatMessage(ChatMessageData cmd){
        chat.handleNewServerMessage(cmd);
    }
    public void startInfoFromServer(StartServerData ssd){
        players.addAll(ssd.players);
    }
    private void updateDrawingPlayer(int drawingId){
        for(int i = 0; i < players.size(); ++i){
            players.get(i).setIsDrawing(players.get(i).getId() == drawingId);
        }
    }
    
    
    /*
        quit - clear all resources so when user enters different game
        there is nothing left over from the previous one
    */
    public void quit(){
        chat.clear();
        drawingBoard.clear();
        players.clear();
        Platform.runLater(() -> {
            drawingBoard.setDisable(true);
            timeLabel.setNew(0, 0);
        });
        setPassword(null);
    }
    
    
    /*
        seperate thread for time-showing label to update
    */
    public void startTimeLabelThread(){
        Thread timeLabelThread = new Thread(()->timeLabel.startUpdating());
        timeLabelThread.setDaemon(true);
        timeLabelThread.start();
    }
    
    
    public ObservableList<Player> getPlayers(){
        return players;
    }
    
    
    /*
        ui scaling so all elements are of the same size (screen procentage wise)
        no matter what resolution user has.
    */
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
    
    private void scaleWidgetsToScreen(){
        scaleGridPane(gridPane, scalingFactor);
        scaleGridPane(actionsGridPane, scalingFactor);
        scaleLabel(timeLabel, "System Bold", scalingFactor);
        scaleLabel(turnLabel, "System Bold", scalingFactor);
        scaleLabel(passwordLabel, "System Regular", scalingFactor);
        scaleLabel(passwordLabelLabel, "System Bold", scalingFactor);
        scaleLabel(thicknessLabel, "System Bold", scalingFactor);
        scaleLabel(thicknessLabelLabel, "System Bold", scalingFactor);
        scaleLabel(drawToolsLabel, "System Bold", scalingFactor);
        scaleLabel(colorLabel, "System Bold", scalingFactor);
        scaleLabel(actionsLabel, "System Bold", scalingFactor);
        scaleLabel(numberOfTurnsLabelLabel, "System Bold", scalingFactor);
        scaleLabel(numberOfTurnsLabel, "System Bold", scalingFactor);
        scaleLabel(subTurnTimeLabelLabel, "System Bold", scalingFactor);
        scaleLabel(subTurnTimeLabel, "System Bold", scalingFactor);
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
        chosenColorView.setWidth(gridPane.getColumnConstraints().get(3).getMaxWidth()-4);
        chosenColorView.setHeight(gridPane.getRowConstraints().get(1).getMaxHeight()-4);
        colorChooser.setWidth(gridPane.getColumnConstraints().get(4).getMaxWidth()-4);
        colorChooser.setHeight(gridPane.getRowConstraints().get(0).getMaxHeight()+gridPane.getRowConstraints().get(1).getMaxHeight()-4);
    }
    
    
    /*
        host and client special views
    */
    public void setupHost(){
        Server.setController(this);
        
        playButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        skipButton.setDisable(true);
        
        // host has play/pause/stop/skip buttons
        actionsGridPane.getChildren().remove(playButton);
        actionsGridPane.getChildren().remove(pauseButton);
        actionsGridPane.getChildren().remove(stopButton);
        actionsGridPane.getChildren().remove(skipButton);
        actionsGridPane.getChildren().remove(skipRequestButton);
        actionsGridPane.getChildren().remove(numberOfTurnsLabelLabel);
        actionsGridPane.getChildren().remove(numberOfTurnsLabel);
        actionsGridPane.getChildren().remove(numberOfTurnsSlider);
        actionsGridPane.getChildren().remove(subTurnTimeLabelLabel);
        actionsGridPane.getChildren().remove(subTurnTimeLabel);
        actionsGridPane.getChildren().remove(subTurnTimeSlider);
        actionsGridPane.getChildren().add(playButton);
        actionsGridPane.getChildren().add(pauseButton);
        actionsGridPane.getChildren().add(stopButton);
        actionsGridPane.getChildren().add(skipButton);
        actionsGridPane.getChildren().add(numberOfTurnsLabelLabel);
        actionsGridPane.getChildren().add(numberOfTurnsLabel);
        actionsGridPane.getChildren().add(numberOfTurnsSlider);
        actionsGridPane.getChildren().add(subTurnTimeLabelLabel);
        actionsGridPane.getChildren().add(subTurnTimeLabel);
        actionsGridPane.getChildren().add(subTurnTimeSlider);
        
        RowConstraints rowCon = actionsGridPane.getRowConstraints().get(3);
        rowCon.setMinHeight((int)(33*scalingFactor));
        rowCon.setMaxHeight((int)(33*scalingFactor));
        rowCon.setPrefHeight((int)(33*scalingFactor));
        
        RowConstraints rowCon4 = actionsGridPane.getRowConstraints().get(4);
        rowCon4.setMinHeight((int)(33*scalingFactor));
        rowCon4.setMaxHeight((int)(33*scalingFactor));
        rowCon4.setPrefHeight((int)(33*scalingFactor));
        
        RowConstraints rowCon2 = gridPane.getRowConstraints().get(3);
        rowCon2.setMinHeight((int)(166*scalingFactor));
        rowCon2.setMaxHeight((int)(166*scalingFactor));
        rowCon2.setPrefHeight((int)(166*scalingFactor));
        
        RowConstraints rowCon3 = gridPane.getRowConstraints().get(2);
        rowCon3.setMinHeight((int)(213*scalingFactor));
        rowCon3.setPrefHeight((int)(213*scalingFactor));
    }
    public void setupClient(){
        // client has skip request button
        actionsGridPane.getChildren().remove(playButton);
        actionsGridPane.getChildren().remove(pauseButton);
        actionsGridPane.getChildren().remove(stopButton);
        actionsGridPane.getChildren().remove(skipButton);
        actionsGridPane.getChildren().remove(skipRequestButton);
        actionsGridPane.getChildren().remove(numberOfTurnsLabelLabel);
        actionsGridPane.getChildren().remove(numberOfTurnsLabel);
        actionsGridPane.getChildren().remove(numberOfTurnsSlider);
        actionsGridPane.getChildren().remove(subTurnTimeLabelLabel);
        actionsGridPane.getChildren().remove(subTurnTimeLabel);
        actionsGridPane.getChildren().remove(subTurnTimeSlider);
        actionsGridPane.getChildren().add(skipRequestButton);
        
        RowConstraints rowCon = actionsGridPane.getRowConstraints().get(3);
        rowCon.setMinHeight(0);
        rowCon.setMaxHeight(0);
        rowCon.setPrefHeight(0);
        
        RowConstraints rowCon4 = actionsGridPane.getRowConstraints().get(4);
        rowCon4.setMinHeight(0);
        rowCon4.setMaxHeight(0);
        rowCon4.setPrefHeight(0);
        
        RowConstraints rowCon2 = gridPane.getRowConstraints().get(3);
        rowCon2.setMinHeight((int)(100*scalingFactor));
        rowCon2.setMaxHeight((int)(100*scalingFactor));
        rowCon2.setPrefHeight((int)(100*scalingFactor));
        
        RowConstraints rowCon3 = gridPane.getRowConstraints().get(2);
        rowCon3.setMinHeight((int)(273*scalingFactor));
        rowCon3.setPrefHeight((int)(273*scalingFactor));
    }
    
    
    
    @Override public void initialize(URL url, ResourceBundle rb) { 
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        scalingFactor = width/1366.0;
        scaleWidgetsToScreen();
  
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
        
        // numberOfTurnsSlider
        numberOfTurnsSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue){
                numberOfTurnsLabel.setText(Integer.toString(newValue.intValue()));
            }
        });
        
        // subTurnTimeSlider
        subTurnTimeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue){
                subTurnTimeLabel.setText(Integer.toString(newValue.intValue()*5));
            }
        });
        
        // drawing tools
        Image penImage = new Image(getClass().getResourceAsStream("images/pencil2.png"));
        ImageView penImageView = new ImageView(penImage);
        penImageView.setFitHeight(30*scalingFactor);
        penImageView.setFitWidth(30*scalingFactor);
        pencilButton.setGraphic(penImageView);
        pencilButton.setStyle("-fx-background-color: #AAAAAA;");
        Image bucketImage = new Image(getClass().getResourceAsStream("images/bucket2.png"));
        ImageView bucketImageView = new ImageView(bucketImage);
        bucketImageView.setFitHeight(30*scalingFactor);
        bucketImageView.setFitWidth(30*scalingFactor);
        bucketButton.setGraphic(bucketImageView);
        Image colorPickerImage = new Image(getClass().getResourceAsStream("images/colorPicker2.png"));
        ImageView colorPickerImageView = new ImageView(colorPickerImage);
        colorPickerImageView.setFitHeight(30*scalingFactor);
        colorPickerImageView.setFitWidth(30*scalingFactor);
        colorPickerButton.setGraphic(colorPickerImageView);
        
        // drawingBoard
        drawingBoard.bindSize(pane.widthProperty(), pane.heightProperty());
        drawingBoard.setAspectRatio(16.0/9.0);
        drawingBoard.setColorWidget(colorWidget);
        drawingBoard.setDisable(true);
        
        // chat
        Font font = chatInput.getFont();
        font = Font.font("Monospaced", font.getSize()*scalingFactor);
        chat = new Chat(chatLog, chatLogPane, chatInput, font);
        
        
        // player tableView
        scoreTableNickNameColumn.setCellValueFactory(
            player -> player.getValue().getNickNameProperty()
        );
        scoreTableView.setFixedCellSize(35*scalingFactor);
        scoreTableNickNameColumn.setCellFactory(new Callback<TableColumn<Player, String>, TableCell<Player,String>>() {
            @Override public TableCell call(TableColumn param) {
                return new TableCell<Player, String>() {
                    @Override public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty()){
                            setText("");
                        } else{
                            Font timeLabelFont = this.getFont();
                            this.setFont(new Font("System Regular", 18*scalingFactor));
                            setText(item);
                        }
                    }
                };
            }
        });
        scoreTablePointsColumn.setCellValueFactory(
            player -> player.getValue().getScoreProperty()
        );
        scoreTablePointsColumn.setCellFactory(new Callback<TableColumn<Player, Number>, TableCell<Player,Number>>() {
            @Override public TableCell call(TableColumn param) {
                return new TableCell<Player, Number>() {
                    @Override public void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if(isEmpty()){
                            setText("");
                        } else{
                            Font timeLabelFont = this.getFont();
                            this.setFont(new Font("System Regular", 18*scalingFactor));
                            setText(item.toString());
                        }
                    }
                };
            }
        });
        scoreTableStateColumn.setCellValueFactory(
            player -> player.getValue().getIsDrawingProperty()
        );
        scoreTableStateColumn.setCellFactory(new Callback<TableColumn<Player, Boolean>, TableCell<Player,Boolean>>() {
            @Override public TableCell call(TableColumn param) {
                return new TableCell<Player, Boolean>() {
                    @Override public void updateItem(Boolean isDrawing, boolean empty) {
                        super.updateItem(isDrawing, empty);
                        setText("");
                        TableRow<Player> currentRow = getTableRow();
                        currentRow.setStyle("");
                        if(!isEmpty() && isDrawing){
                            currentRow.setStyle("-fx-background-color:lightgreen");
                        }
                    }
                };
            }
        });
        scoreTableView.setItems(players);
        
        Client.setController(this);
    }    
}
