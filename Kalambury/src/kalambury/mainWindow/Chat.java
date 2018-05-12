package kalambury.mainWindow;

import java.util.concurrent.TimeUnit;
import kalambury.sendableData.SendableData;
import kalambury.sendableData.ChatMessageData;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import kalambury.client.Client;
import kalambury.server.SystemMessage;

public class Chat {
    private final TextFlow log;
    private final ScrollPane logPane;
    private final TextField userInput;
    private final Font font;
    
    public Chat(TextFlow log, ScrollPane logPane, TextField userInput, Font font){
        this.log = log;
        this.logPane = logPane;
        this.userInput = userInput;
        this.font = font;
        
        userInput.setFont(this.font);
        this.logPane.focusedProperty().addListener((observable,  oldValue,  newValue) -> {
            this.userInput.requestFocus();
        });
        userInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (userInput.getText().length() > 80) {
                    String s = userInput.getText().substring(0, 80);
                    userInput.setText(s);
                }
            }
        });
        
        this.log.prefWidthProperty().bind(this.logPane.widthProperty());
    }
    
    public void clear(){
        Platform.runLater(() -> {
            log.getChildren().clear();
            userInput.setText("");
        });
    }
    
    private void addPlayerTextNodes(int index, Text time, Text status, Text nick, Text message, Text exactTime){
        log.getChildren().add(index, time);
        log.getChildren().add(index+1, status);
        log.getChildren().add(index+2, nick);
        log.getChildren().add(index+3, message);
        log.getChildren().add(index+4, exactTime);
    }
    private void addTextNodesToCorrectTimePlace(Text time, Text status, Text nick, Text message, Text exactTime){
        Platform.runLater(()->{
            double myMessageTime = Long.parseLong(exactTime.getText());
            ObservableList<Node> nodes = log.getChildren();
            for(int i = nodes.size()-1; i >= 0; --i){
                if(i % 5 == 4){
                    double chatMessageTime = Long.parseLong(((Text)nodes.get(i)).getText());
                    if(chatMessageTime <= myMessageTime){
                        addPlayerTextNodes(i+1, time, status, nick, message, exactTime);
                        return;
                    }
                }
            }
            // if no message was earlier then this one
            addPlayerTextNodes(0, time, status, nick, message, exactTime);
        });
        
        // scroll down
        Platform.runLater(() -> {
            logPane.layout();
            logPane.setVvalue(1.0);
        });
    }
    
    private Text getTimeText(long time){
        String stringTime = String.format("%02d:%02d:%02d", 
            TimeUnit.MILLISECONDS.toHours(time),
            TimeUnit.MILLISECONDS.toMinutes(time) -  TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time))
        );   
        Text timeText = new Text("<" + stringTime + ">");
        timeText.setFill(Color.GRAY);
        timeText.setFont(font);
        
        return timeText;
    }
    
    private Text getStatusText(String status){
        Text statusText = new Text("(" + status + ")");
        statusText.setFill(Color.DARKGREEN);
        statusText.setFont(font);
        return statusText;
    }
    
    private Text getNickNameText(String nickName, boolean isLocal){
        Text nickText = new Text("[" + nickName + "] ");
        
        if(isLocal){
            nickText.setFill(Color.BLUE);
        } else {
            nickText.setFill(Color.DARKCYAN);
        }
        nickText.setStyle("-fx-font-weight: bold;");
        nickText.setFont(font);
        
        return nickText;
    }
    
    private Text getMessageText(String message, Color color){
        Text messageText = new Text(message + "\n");
        messageText.setFill(color);
        messageText.setFont(font);
        return messageText;
    }
    
    private Text getExactTimeText(long time){
        Text exactTimeText = new Text(Long.toString(time));
        exactTimeText.setVisible(false);
        exactTimeText.setManaged(false);
        return exactTimeText;
    }
    
    private Text getEmptyText(){
        Text emptyText = new Text("");
        emptyText.setVisible(false);
        emptyText.setManaged(false);
        return emptyText;
    }
    
    private void addPlayerChatMessage(String nickName, String message, long time, boolean isHost, boolean isLocal){
        Text timeText = getTimeText(time);
        Text statusText;
        if(isHost){
            statusText = getStatusText(" HOST ");
        } else {
            statusText = getStatusText("CLIENT");
        }
        Text nickText = getNickNameText(nickName, isLocal);
        Text messageText = getMessageText(message, Color.BLACK);
        Text exactTimeText = getExactTimeText(time);
        
        addTextNodesToCorrectTimePlace(timeText, statusText, nickText, messageText, exactTimeText);
    }
    
    public void handleNewClientMessage(){
        if(!userInput.getText().isEmpty()){
            String chatMessage = userInput.getText();
            String nickName = Client.getNick();
            long time = Client.getTime();
            SendableData mess = new ChatMessageData(nickName, chatMessage, time, Client.isHost());
            
            Client.appendToSend(mess);
            addPlayerChatMessage(nickName, chatMessage, time, Client.isHost(), true);
            
            Platform.runLater(()->{userInput.setText("");}); 
        }
    }

    public void handleNewServerMessage(ChatMessageData data){
        addPlayerChatMessage(data.nickName, data.message, data.time, data.isHost, false);
    }
    
    public void handleNewSystemMessage(SystemMessage systemMessage){
        Text timeText = getTimeText(systemMessage.time);
        Text statusText = getStatusText("SYSTEM");
        Text nickText = getEmptyText();
        Text messageText = getMessageText(" " + systemMessage.message, Color.DARKGREEN);
        Text exactTimeText = getExactTimeText(systemMessage.time);
        addTextNodesToCorrectTimePlace(timeText, statusText, nickText, messageText, exactTimeText);
    }
}
