package kalambury;

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
import javafx.concurrent.Task;
import javafx.scene.control.Label;


public class Client {
    private static String ip;
    private static String nick;
    private static int port;
    
    private static Socket socket;
    private static DataOutputStream out;
    private static DataInputStream in;
    
    private static long time;
    private static Thread timeThreadObject;
    
    private static Chat chat;
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    
    public static void initialize(String ip, int port, String nick, Label label_info, Runnable switchToMainStage){
        Client.ip = ip;
        Client.port = port;
        Client.nick = nick;
        
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
                
                label_info.setText("Connection established");
                switchToMainStage.run();
                
                Thread listenThread = new Thread(() -> Client.listen());
                listenThread.setDaemon(true);
                listenThread.start();
            }
        });
    }
    
    public static String getNick(){
        return nick;
    }
    public static void setChat(Chat chat){
        Client.chat = chat;
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
        data.send(out);
    }
    public static void listen(){
        while(true){
            try {
                if(in.available() > 0){
                    final SendableData input = SendableData.receive(in);
                    // tutaj obsluzyc te dane
                    ChatMessageData cmd = (ChatMessageData)input;
                    chat.handleNewServerMessage(cmd);
                    System.out.println("Data received");
                }
            } catch(IOException ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    public static long getTime(){
        return time;
    }
    public static void timeThread(){
        time = 0;
        long syncTime = 0;
        long timeAfterSync = 0;
        long sleepTime = 20;
        long timeBeforeSleep = System.currentTimeMillis();
        boolean firstTime = true;
        while(true){
            if("server has time" == "true"){
                syncTime = 0; // timeSocket.getTime()
                timeAfterSync = 0;
                timeBeforeSleep = System.currentTimeMillis();
            }
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
