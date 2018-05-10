package kalambury.client;

import kalambury.sendableData.SendableData;
import kalambury.sendableData.ChatMessageData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import kalambury.Kalambury;
import kalambury.mainWindow.MainWindowController;
import kalambury.mainWindow.Player;
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
import kalambury.sendableData.GameStartedData;
import kalambury.server.Server;


public class Client {
    private static Kalambury kalambury;
    private static MainWindowController controller;
    
    private static String ip;
    private static String nick;
    private static int port;
    
    private static Socket socket = null;
    private static DataOutputStream out = null;
    private static DataInputStream in = null;
    
    private static long time;
    private static Thread timeThreadObject;
    
    private static Thread listenThread;
    private static Thread sendThread;
    
    private static volatile ArrayDeque<SendableData> dataToSend;
    private static final Lock dataToSendMutex = new ReentrantLock(true);
    
    private static boolean isHostFlag;
    
    private static ExecutorService executor;
    
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
        
        dataToSend = new ArrayDeque<>();
        
        executor = Executors.newSingleThreadExecutor();
        executor.submit(serverConnectTask);
        
        timeThreadObject = new Thread(() -> Client.timeThread());
        timeThreadObject.setDaemon(true);
        timeThreadObject.setPriority(MAX_PRIORITY);
        timeThreadObject.start();
        
        serverConnectTask.setOnSucceeded(event->{
            if(Client.isSocketSet()){
                executor.shutdown();
                
                // id will be set by server, client has no idea of it
                NewPlayerData newPlayerData = new NewPlayerData(Client.nick, -1, Client.getTime());
                sendMessage(newPlayerData);
                
                listenThread = new Thread(() -> Client.listen());
                listenThread.setDaemon(true);
                listenThread.start();
                
                sendThread = new Thread(() -> Client.sending());
                sendThread.setDaemon(true);
                sendThread.start();

                label_info.setText("Connection established");
                switchToMainStage.run();
            }
        });
    }
    
    public static void setKalambury(Kalambury kalambury){
        Client.kalambury = kalambury;
    }
    public static boolean isHost(){
        return isHostFlag;
    }
    public static String getNick(){
        return nick;
    }
    public static void setController(MainWindowController controller){
        Client.controller = controller;
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
    
    public static void quit(){
        timeThreadObject.interrupt();
        listenThread.interrupt();
        sendThread.interrupt();
        try {
            timeThreadObject.join();
            listenThread.join();
            sendThread.join();
        } catch (InterruptedException ex) {
        }
        Server.quit();
        Platform.runLater(() -> {
            controller.quit();
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ex) {

            }
            socket = null;
            kalambury.showWelcomeWindow();
        });
    }
    public static void sendMessage(SendableData data){
        dataToSendMutex.lock();
        try {
            dataToSend.addLast(data);
        } finally {
            dataToSendMutex.unlock();
        }
    }
    private static boolean sendData(SendableData data){
        try{
            data.send(out);
            out.flush();
        } catch(IOException ex){
            quit();
            return false;
        }
        return true;
    }
    private static void sending(){
        while(!Thread.interrupted()){
            if(dataToSend.size() > 0){
                SendableData data = null;
                dataToSendMutex.lock();
                try {
                    data = dataToSend.removeFirst();
                } finally {
                    dataToSendMutex.unlock();
                }
                if(data != null){
                    if(!sendData(data)){
                        break;
                    }
                }
            }
        }
    }
    
    private static void listen(){
        while(!Thread.interrupted()){
            try {
                if(in.available() > 0){
                    final SendableData input = SendableData.receive(in);
                    switch(input.getType()){
                    case StartServerData:
                        StartServerData ssd = (StartServerData)input;
                        controller.startInfoFromServer((StartServerData)input);
                        time = ssd.time;
                        break;
                    case ChatMessage:
                        controller.chatMessage((ChatMessageData)input);
                        break;
                    case LineDraw:
                        controller.lineDraw((LineDrawData)input);
                        break;
                    case FloodFill:
                        controller.floodFill((FloodFillData)input);
                        break;
                    case NewPlayerData:
                        NewPlayerData npd = (NewPlayerData)input;
                        controller.newPlayer(npd);
                        break;
                    case Time:
                        TimeData td = (TimeData)input;
                        syncTime = td.time;
                        timeAfterSync = 0;
                        timeBeforeSleep = System.currentTimeMillis();
                        time = syncTime;
                        break;
                    case TurnStarted:
                        controller.turnStarted((TurnStartedData)input);
                        break;
                    case GamePassword:
                        controller.setPassword((GamePasswordData)input);
                        break;
                    case TurnEndedSignal:
                        controller.turnEndedSignal((SendableSignal)input);
                        break;
                    case TurnEndedData:
                        controller.turnEnded((TurnEndedData)input);
                        break;
                    case GameStoppedSignal:
                        controller.gameStopped((SendableSignal)input);
                        break;
                    case GameStarted:
                        controller.gameStarted((GameStartedData)input);
                        break;
                    case GameEndedSignal:
                        controller.gameEnded((SendableSignal)input);
                        sendMessage(new SendableSignal(DataType.TurnEndedAcceptSignal, Client.getTime()));
                        break;
                    case TurnSkippedSignal:
                        controller.turnSkipped((SendableSignal)input);
                        break;
                    case SkipRequestSignal:
                        controller.skipRequest((SendableSignal)input);
                        break;
                    case PlayerQuit:
                        controller.playerQuit((PlayerQuitData)input);
                        break;
                    default:
                        break;
                    }
                }
            } catch(IOException ex) {
                quit();
                break;
            }
        }
    }
 
    public static void skipRequest(){
        sendMessage(new SendableSignal(DataType.SkipRequestSignal, Client.getTime()));
    }
    
    public static long getTime(){
        return time;
    }
    public static void timeThread(){
        time = 0;
        long sleepTime = 20;
        int sleepCounter = 0;
        syncTime = 0;
        timeAfterSync = 0;
        timeBeforeSleep = System.currentTimeMillis();
        boolean firstTime = true;
        while(!Thread.interrupted()){
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
                Thread.currentThread().interrupt();
                return;
            }
            sleepCounter++;
            if(sleepCounter % (1000/sleepTime) == 0){
                sendMessage(new SendableSignal(DataType.TimeAcceptSignal, Client.getTime()));
            }
            
            firstTime = false;
        }
    }
}
