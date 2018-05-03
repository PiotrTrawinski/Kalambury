package kalambury.client;

import kalambury.mainWindow.Chat;
import kalambury.sendableData.SendableData;
import kalambury.sendableData.ChatMessageData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import kalambury.mainWindow.Player;
import kalambury.mainWindow.TimeLabel;
import kalambury.mainWindow.drawingBoard.DrawingBoard;
import kalambury.sendableData.DataType;
import static kalambury.sendableData.DataType.TurnEndedSignal;
import kalambury.sendableData.FloodFillData;
import kalambury.sendableData.GamePasswordData;
import kalambury.sendableData.LineDrawData;
import kalambury.sendableData.NewPlayerData;
import kalambury.sendableData.PlayerQuitData;
import kalambury.sendableData.SendableSignal;
import kalambury.sendableData.StartServerData;
import kalambury.sendableData.TimeData;
import kalambury.sendableData.TurnEndedData;
import kalambury.sendableData.TurnStartedData;
import kalambury.server.SystemMessage;
import kalambury.server.SystemMessageType;


public class Client {
    private static String ip;
    private static String nick;
    private static int port;
    
    private static Socket socket;
    private static DataOutputStream out;
    private static DataInputStream in;
    
    private static long time;
    private static Thread timeThreadObject;
    
    private static boolean isHostFlag;
    
    private static Label wordLabel;
    private static Chat chat;
    private static DrawingBoard drawingBoard;
    private static TimeLabel timeLabel;
    private static final ObservableList<Player> players = FXCollections.observableArrayList();
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private static long syncTime;
    private static long timeAfterSync;
    private static long timeBeforeSleep;

    public static void initialize(
            String ip, int port, String nick, Label label_info, 
            Runnable switchToMainStage, boolean isHost
    ){
        Client.ip = ip;
        Client.port = port;
        Client.nick = (!nick.equals("") ? nick : "???");
        Client.isHostFlag = isHost;
        
        label_info.setText("Connecting...");
        Task<ConnectResult> serverConnectTask = new ServerConnectTask(ip, port);
        
        executor.submit(serverConnectTask);
        
        Client.timeThreadObject = new Thread(() -> Client.timeThread());
        Client.timeThreadObject.setDaemon(true);
        Client.timeThreadObject.setPriority(MAX_PRIORITY);
        Client.timeThreadObject.start();
        
        serverConnectTask.setOnSucceeded(event->{
            if(Client.isSocketSet()){
                executor.shutdown();
                
                // id will be set by server, client has no idea of it
                NewPlayerData newPlayerData = new NewPlayerData(Client.nick, -1, Client.getTime());
                sendMessage(newPlayerData);

                label_info.setText("Connection established");
                switchToMainStage.run();
                
                Thread listenThread = new Thread(() -> Client.listen());
                listenThread.setDaemon(true);
                listenThread.start();
            }
        });
    }
    
    public static boolean isHost(){
        return isHostFlag;
    }
    public static String getNick(){
        return nick;
    }
    public static void setChat(Chat chat){
        Client.chat = chat;
    }
    public static void setWordLabel(Label wordLabel){
        Client.wordLabel = wordLabel;
    }
    public static void setDrawingBoard(DrawingBoard drawingBoard){
        Client.drawingBoard = drawingBoard;
    }
    public static void setTimeLabel(TimeLabel timeLabel){
        Client.timeLabel = timeLabel;
    }
    public static void setSocket(Socket s){
        Client.socket = s;
        try {
            Client.out = new DataOutputStream(new BufferedOutputStream(Client.socket.getOutputStream()));
            Client.in = new DataInputStream(new BufferedInputStream(Client.socket.getInputStream()));
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }
    
    public static boolean isSocketSet(){
        return (socket != null);
    }
    public static void sendMessage(SendableData data){
        try{
            data.send(out);
            out.flush();
        } catch(IOException ex){
            
        }
    }
    public static void listen(){
        while(true){
            try {
                if(in.available() > 0){
                    final SendableData input = SendableData.receive(in);
                    switch(input.getType()){
                    case StartServerData:
                        StartServerData ssd = (StartServerData)input;
                        players.addAll(ssd.players);
                        time = ssd.time;
                        break;
                    case ChatMessage:
                        ChatMessageData cmd = (ChatMessageData)input;
                        chat.handleNewServerMessage(cmd);
                        break;
                    case LineDraw:
                        LineDrawData ldd = (LineDrawData)input;
                        drawingBoard.drawLineRemote(ldd);
                        break;
                    case FloodFill:
                        FloodFillData ffd = (FloodFillData)input;
                        drawingBoard.floodFillRemote(ffd);
                        break;
                    case NewPlayerData:
                        NewPlayerData npd = (NewPlayerData)input;
                        players.add(new Player(npd.nickName, 0, npd.id));
                        chat.handleNewSystemMessage(new SystemMessage(
                            npd.nickName + " dołączył do gry",
                            npd.time,
                            SystemMessageType.Information
                        ));
                        break;
                    case Time:
                        TimeData td = (TimeData)input;
                        syncTime = td.time;
                        timeAfterSync = 0;
                        timeBeforeSleep = System.currentTimeMillis();
                        time = syncTime;
                        break;
                    case TurnStarted:
                        TurnStartedData tsd = (TurnStartedData)input;
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
                            Platform.runLater(()->{
                                wordLabel.setText("???");
                            });
                            chat.handleNewSystemMessage(new SystemMessage(
                                "Zgaduj hasło!",
                                tsd.startTime,
                                SystemMessageType.Information
                            ));
                        }
                        break;
                    case GamePassword:
                        GamePasswordData gpd = (GamePasswordData)input;
                        Platform.runLater(()->{
                            wordLabel.setText(gpd.password);
                        });
                        break;
                    case TurnEndedSignal:
                        long tesTime = ((SendableSignal)input).time;
                        drawingBoard.setDisable(true);
                        updateDrawingPlayer(-1);
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Koniec tury!",
                            tesTime,
                            SystemMessageType.Information
                        ));
                        sendMessage(new SendableSignal(DataType.TurnEndedAcceptSignal, Client.getTime()));
                        break;
                    case TurnEndedData:
                        TurnEndedData ted = (TurnEndedData)input;
                        chat.handleNewSystemMessage(new SystemMessage(
                            ted.winnerNickName + " wygrał!",
                            ted.time,
                            SystemMessageType.Information
                        ));
                        for(int i = 0; i < players.size(); ++i){
                            players.get(i).setScore(ted.updatedScores.get(i));
                        }
                        break;
                    case GameStoppedSignal:
                        drawingBoard.setDisable(true);
                        updateDrawingPlayer(-1);
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Gra została zakończona przez hosta",
                            ((SendableSignal)input).time,
                            SystemMessageType.Information
                        ));
                        timeLabel.setNew(0, 0);
                        break;
                    case GameStartedSignal:
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Gra została rozpoczęta",
                            ((SendableSignal)input).time,
                            SystemMessageType.Information
                        ));
                        for(int i = 0; i < players.size(); ++i){
                            players.get(i).setScore(0);
                        }
                        break;
                    case TurnSkippedSignal:
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Tura została pominięta",
                            ((SendableSignal)input).time,
                            SystemMessageType.Information
                        ));
                        break;
                    case SkipRequestSignal:
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Gracz poprosił o pominięcie tury",
                            ((SendableSignal)input).time,
                            SystemMessageType.Information
                        ));
                        break;
                    case PlayerQuit:
                        PlayerQuitData pqd = (PlayerQuitData)input;
                        Player player = players.get(pqd.index);
                        chat.handleNewSystemMessage(new SystemMessage(
                            "Gracz " + player.getNickName() + " wyszedł z gry",
                            pqd.time,
                            SystemMessageType.Information
                        ));
                        players.remove(player);
                        break;
                    default:
                        break;
                    }
                }
            } catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    public static void skipRequest(){
        chat.handleNewSystemMessage(new SystemMessage(
            "Poprosiłeś o pominięcie tury",
            Client.getTime(),
            SystemMessageType.Information
        ));
        sendMessage(new SendableSignal(DataType.SkipRequestSignal, Client.getTime()));
    }
    
    public static void updateDrawingPlayer(int drawingId){
        for(int i = 0; i < players.size(); ++i){
            players.get(i).setIsDrawing(players.get(i).getId() == drawingId);
        }
    }
    
    public static ObservableList<Player> getPlayers(){
        return players;
    }
    public static void addPlayer(Player player){
        players.add(player);
    }
    
    public static long getTime(){
        return time;
    }
    public static void timeThread(){
        time = 0;
        long sleepTime = 20;
        syncTime = 0;
        timeAfterSync = 0;
        timeBeforeSleep = System.currentTimeMillis();
        boolean firstTime = true;
        while(true){
            long deltaTimeAfterSync = System.currentTimeMillis() - timeBeforeSleep;
            if(!firstTime && deltaTimeAfterSync < sleepTime/2){
                // player changed his system clock
                deltaTimeAfterSync = sleepTime;
            }
            timeAfterSync += deltaTimeAfterSync;
            time = syncTime + timeAfterSync;
            
            timeBeforeSleep = System.currentTimeMillis();
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                System.err.printf("error sleep: \"%s\"\n", ex.getMessage());
            }
            
            firstTime = false;
        }
    }
}
