package kalambury.mainWindow;

import kalambury.sendableData.SendableData;
import kalambury.sendableData.ChatMessageData;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import kalambury.client.Client;

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
        
        this.log.prefWidthProperty().bind(this.logPane.widthProperty());
    }
    
    private void addPlayerTextNodes(int index, Text time, Text status, Text nick, Text message, Text exactTime){
        Platform.runLater(()->{
        log.getChildren().add(index, time);
        log.getChildren().add(index+1, status);
        log.getChildren().add(index+2, nick);
        log.getChildren().add(index+3, message);
        log.getChildren().add(index+4, exactTime);
        });
    }
    private void addPlayerTextNodesToCorrectTimePlace(Text time, Text status, Text nick, Text message, Text exactTime){
        double myMessageTime = Double.parseDouble(exactTime.getText());
        ObservableList<Node> nodes = log.getChildren();
        for(int i = nodes.size()-1; i >= 0; --i){
            if(i % 5 == 4){
                double chatMessageTime = Double.parseDouble(((Text)nodes.get(i)).getText());
                if(chatMessageTime <= myMessageTime){
                    addPlayerTextNodes(i+1, time, status, nick, message, exactTime);
                    return;
                }
            }
        }
        // if no message was earlier then this one
        addPlayerTextNodes(0, time, status, nick, message, exactTime);
    }
    private void addPlayerChatMessage(String nickName, String message, double time, boolean isHost, boolean isLocal){
        // prepare time Text
        Text timeText = new Text("<00:00:00>");
        timeText.setFill(Color.GRAY);
        timeText.setFont(font);
        
        // prepare status Text
        Text statusText;
        if(isHost){
            statusText = new Text("( HOST )");
        } else {
            statusText = new Text("(CLIENT)");
        }
        statusText.setFill(Color.DARKGREEN);
        statusText.setFont(font);
        
        // prepare nickname Text
        Text nickText = new Text("[" + nickName + "] ");
        if(isLocal){
            nickText.setFill(Color.BLUE);
        } else {
            nickText.setFill(Color.DARKCYAN);
        }
        nickText.setStyle("-fx-font-weight: bold;");
        nickText.setFont(font);
        
        // prepare message Text
        Text messageText = new Text(message + "\n");
        messageText.setFill(Color.BLACK);
        messageText.setFont(font);
        
        // prepare invisible exact time Text
        Text exactTimeText = new Text(Double.toString(time));
        exactTimeText.setVisible(false);
        exactTimeText.setManaged(false);
        
        // place it correctly depending on the time
        addPlayerTextNodesToCorrectTimePlace(timeText, statusText, nickText, messageText, exactTimeText);
        
        // scroll down
        logPane.setVvalue(1);
    }
    
    public void handleNewClientMessage(){
        if(!userInput.getText().isEmpty()){
            String chatMessage = userInput.getText();
            String nickName = Client.getNick();
            double time = 0; // Client.getTime()
            addPlayerChatMessage(nickName, chatMessage, time, Client.isHost(), true);
            Platform.runLater(()->{userInput.setText("");});
            
            
            SendableData mess = new ChatMessageData(nickName, chatMessage, time, Client.isHost());
            Client.sendMessage(mess);
        }
    }

    public void handleNewServerMessage(ChatMessageData data){
        addPlayerChatMessage(data.nickName, data.message, data.time, data.isHost, false);
    }
}
