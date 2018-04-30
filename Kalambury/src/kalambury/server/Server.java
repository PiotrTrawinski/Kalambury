package kalambury.server;

import kalambury.sendableData.SendableData;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import kalambury.client.Client;
import kalambury.sendableData.NewPlayerData;
import kalambury.sendableData.StartServerData;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import kalambury.sendableData.TimeData;

public class Server {
    private static final int maxClients = 5;
    private static final Socket sockets[] = new Socket[maxClients];
    private static final Socket timeSockets[] = new Socket[maxClients];
    private static final DataInputStream inputStreams[] = new DataInputStream[maxClients];
    private static final DataOutputStream outputStreams[] = new DataOutputStream[maxClients];
    private static volatile int clientsCount = 0;
    private static int port;
    private static volatile ArrayDeque< Pair<SendableData,Integer> >  messagesToHandle = new ArrayDeque<Pair<SendableData,Integer>>();
    private static final Lock _mutex = new ReentrantLock(true);
    
    public static void initialize(int port){
        //set the port that server is working on, and start it on a new thread
        Server.port = port;
        Thread serverThread = new Thread(()->Server.start());
        serverThread.setDaemon(true);   // close with application
        serverThread.start();
       
    }
    
    public static void start(){        
        // incoming data thread
        Thread t = new Thread(()->Server.handleIncomingData());
        t.setDaemon(true);
        t.start();
        
        // send incoming data to clients
        Thread sendOutDataThread = new Thread( () -> Server.forwardDataToClients());
        sendOutDataThread.setDaemon(true);
        sendOutDataThread.start();
        
        // time thread
        Thread timeThreadObject = new Thread(() -> timeThread());
        timeThreadObject.setDaemon(true);
        timeThreadObject.setPriority(MAX_PRIORITY);
        timeThreadObject.start();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                if(clientsCount < maxClients){
                    Socket socket = serverSocket.accept();
                    try{
                        sockets[clientsCount] = socket;
                        inputStreams[clientsCount] = new DataInputStream(socket.getInputStream());
                        outputStreams[clientsCount] = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        
                        NewPlayerData newPlayerData = (NewPlayerData)SendableData.receive(inputStreams[clientsCount]);
                        StartServerData startServerData = new StartServerData(Client.getPlayers(), Client.getTime());
                        startServerData.send(outputStreams[clientsCount]);
                        
                        clientsCount++;
                        
                        Thread sendOutNewUserDataThread = new Thread( () -> Server.sendExcept(newPlayerData, -1));
                        sendOutNewUserDataThread.setDaemon(true);
                        sendOutNewUserDataThread.start();
                    }
                    catch(IOException ex){
                        System.err.println(ex.getMessage());
                    }
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    
    
    
    public static void forwardDataToClients(){
        //System.out.println("Forward start");
        while(true){
            
            if(messagesToHandle.size() > 0){
                _mutex.lock();
                Pair DataAndSkipIndex = messagesToHandle.removeFirst();
                _mutex.unlock();
                SendableData data = (SendableData)DataAndSkipIndex.getKey();
                int skip = (Integer)DataAndSkipIndex.getValue();
                //System.out.println("Sending except");
                sendExcept(data,skip);
            }
        }   
    }
    
    
    public static void sendExcept(SendableData data, int exceptIndex){
        for(int i = 0; i < clientsCount; i++){
            if(i != exceptIndex){
                data.send(outputStreams[i]);
                try{
                    outputStreams[i].flush();
                }
                catch(IOException ex){
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
    
    public static void handleIncomingData(){
        while(true){
            for(int i = 0; i < clientsCount; i++){ // for every client
                try{
                    if(inputStreams[i].available() > 0){
                        final SendableData input = SendableData.receive(inputStreams[i]);
                        final int skip = i;
                        //message received, send it to clients except the sender client
                        _mutex.lock();
                        messagesToHandle.addLast(new Pair(input,i));
                        _mutex.unlock();
                        //System.out.println("Adding message to queue");
                    }
                } catch(IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
    
    public static void timeThread(){
        long startTime = System.currentTimeMillis();
        long sleepTime = 1000;
        TimeData timeData = new TimeData(0);
        
        while(true){
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                System.err.printf("error sleep: \"%s\"\n", ex.getMessage());
            }
            timeData.time = System.currentTimeMillis() - startTime;
            
            _mutex.lock();
            messagesToHandle.addFirst(new Pair(timeData, -1));
            _mutex.unlock();
        }
    }
}

