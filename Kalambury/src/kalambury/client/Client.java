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
import javafx.concurrent.Task;
import kalambury.Kalambury;
import kalambury.mainWindow.MainWindowController;
import kalambury.sendableData.DataType;
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
import kalambury.sendableData.SkipRequestData;
import kalambury.server.Server;


public class Client {
    // socket connection
    private static String ip;
    private static int port;
    private static Socket socket = null;
    private static DataOutputStream out = null;
    private static DataInputStream in = null;
    
    // this client's player
    private static String nick;
    
    // synchronized time between all clients
    private static long time;
    private static long syncTime;
    private static long timeAfterSync;
    private static long timeBeforeSleep;
    private static Thread timeThreadObject;
    
    // receiveing and sending data threads
    private static Thread listenThread;
    private static Thread sendThread;
    
    // array for the data that should be send by sendThread
    private static volatile ArrayDeque<SendableData> dataToSend;
    private static final Lock dataToSendMutex = new ReentrantLock(true);
    
    // reference to objects for communication
    private static Kalambury kalambury;
    private static MainWindowController controller;
    
    // informs if client is also the host (has initialized Server object)
    private static boolean isHostFlag;
    
    // for connection task
    private static ExecutorService executor;
    
    
    public static void initialize(
            String ip, int port, String nick, 
            Runnable switchToMainStage, boolean isHost
    ){
        // save parameters
        Client.ip = ip;
        Client.port = port;
        Client.nick = (!nick.equals("") ? nick : "???");
        Client.isHostFlag = isHost;
        
        // try to connect to the server
        Task<Void> serverConnectTask = new ServerConnectTask(ip, port);
        executor = Executors.newSingleThreadExecutor();
        executor.submit(serverConnectTask);
        
        // when succesfully connect - complete initialization
        serverConnectTask.setOnSucceeded(event->{
            if(Client.isSocketSet()){
                executor.shutdown();
                
                // initialize queue for data transfer
                dataToSend = new ArrayDeque<>();

                // initialize thread for time synchronization
                timeThreadObject = new Thread(() -> Client.timeThread());
                timeThreadObject.setDaemon(true);
                timeThreadObject.setPriority(MAX_PRIORITY);
                timeThreadObject.start();
                
                // initialize thread for listening the server
                listenThread = new Thread(() -> Client.listening());
                listenThread.setDaemon(true);
                listenThread.start();
                
                // initialize thread for sending the data to the server
                sendThread = new Thread(() -> Client.sending());
                sendThread.setDaemon(true);
                sendThread.start();
                
                // send your info to the server; id will be set by server, client has no idea of it
                NewPlayerData newPlayerData = new NewPlayerData(Client.nick, -1, Client.getTime());
                appendToSend(newPlayerData);
                
                // fully connected - switch to the main window
                switchToMainStage.run();
            }
        });
    }
    
    // socket initialization
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
    
    // object reference setters
    public static void setKalambury(Kalambury kalambury){
        Client.kalambury = kalambury;
    }
    public static void setController(MainWindowController controller){
        Client.controller = controller;
    }
    
    // utility getters
    public static boolean isHost(){
        return isHostFlag;
    }
    public static String getNick(){
        return nick;
    }
    
    
    public static void quit(){
        // stop all the threads
        timeThreadObject.interrupt();
        listenThread.interrupt();
        sendThread.interrupt();
        try {
            timeThreadObject.join();
            listenThread.join();
            sendThread.join();
        } catch (InterruptedException ex) {}
        
        // close connection
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ex) {}
        socket = null;
        
        Server.quit();
 
        // close controller before switching back to welcome window
        controller.quit();
        
        kalambury.showWelcomeWindow();
    }
    
    
    
    /*
        Sending data to the server
    */
    
    // adds data to the queue
    public static void appendToSend(SendableData data){
        dataToSendMutex.lock();
        try {
            dataToSend.addLast(data);
        } finally {
            dataToSendMutex.unlock();
        }
    }
    
    // sends data to server. if fails (disconnected) then go back to welcome window
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
    
    // thread responsible for sending all the data from the queue to the server
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
                
                // check if actually got the data
                if(data != null){
                    
                    // if data sending fails then stop the thread
                    if(!sendData(data)){
                        break;
                    }
                }
            }
            
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    
    
    /*
        listening - get data from server when avaliable
    */
    
    private static void menageReceivedData(SendableData data){
        switch(data.getType()){
        case StartServerData:
            StartServerData ssd = (StartServerData)data;
            controller.startInfoFromServer(ssd);
            time = ssd.time;
            break;
        case Time:
            TimeData td = (TimeData)data;
            syncTime = td.time;
            timeAfterSync = 0;
            timeBeforeSleep = System.currentTimeMillis();
            time = syncTime;
            break;
        case GameEndedSignal:
            controller.gameEnded((SendableSignal)data);
            appendToSend(new SendableSignal(DataType.TurnEndedAcceptSignal, Client.getTime()));
            break;
        case TurnEndedSignal:   
            controller.turnEndedSignal((SendableSignal)data); 
            appendToSend(new SendableSignal(DataType.TurnEndedAcceptSignal, Client.getTime()));
            break;
        case ChatMessage:       controller.chatMessage((ChatMessageData)data);    break;
        case LineDraw:          controller.lineDraw((LineDrawData)data);          break;
        case FloodFill:         controller.floodFill((FloodFillData)data);        break;
        case NewPlayerData:     controller.newPlayer((NewPlayerData)data);        break;
        case TurnStarted:       controller.turnStarted((TurnStartedData)data);    break;
        case GamePassword:      controller.setPassword((GamePasswordData)data);   break;
        case TurnEndedData:     controller.turnEnded((TurnEndedData)data);        break;
        case GameStoppedSignal: controller.gameStopped((SendableSignal)data);     break;
        case GameStarted:       controller.gameStarted((GameStartedData)data);    break;
        case TurnSkippedSignal: controller.turnSkipped((SendableSignal)data);     break;
        case SkipRequest:       controller.skipRequest((SkipRequestData)data);    break;
        case PlayerQuit:        controller.playerQuit((PlayerQuitData)data);      break;
        case GamePausedSignal:  controller.gamePaused((SendableSignal)data);      break;
        case TurnTimeOutSignal: controller.turnTimeOut((SendableSignal)data);     break;
        default: break;
        }
    }
    
    // thread function
    private static void listening(){
        while(!Thread.interrupted()){
            try {
                while(in.available() > 0){
                    SendableData data = SendableData.receive(in);
                    menageReceivedData(data);
                }
            } catch(IOException ex) {
                quit();
                break;
            }
            
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
 
    public static void skipRequest(String nickName){
        appendToSend(new SkipRequestData(nickName, Client.getTime()));
    }
    
    
    
    /*
        time synchronization
    */
    
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
            
            // ping the server every ~1sec to make sure the connection is working
            sleepCounter++;
            if(sleepCounter % (1000/sleepTime) == 0){
                appendToSend(new SendableSignal(DataType.TimeAcceptSignal, Client.getTime()));
            }
            
            firstTime = false;
        }
    }
}
