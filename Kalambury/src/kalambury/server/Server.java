package kalambury.server;

import game.Game;
import kalambury.sendableData.SendableData;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;
import kalambury.client.Client;
import kalambury.sendableData.NewPlayerData;
import kalambury.sendableData.StartServerData;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import kalambury.sendableData.ChatMessageData;
import kalambury.sendableData.DataType;

import kalambury.sendableData.TimeData;
import kalambury.sendableData.TurnEndedData;
import kalambury.sendableData.TurnEndedSignal;

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
    private static int acceptEndSignalCount = -1;
    private static TimeData timeData = new TimeData(0);
    private static Game game = null;
    public static void initialize(int port){
        //set the port that server is working on, and start it on a new thread
        Server.port = port;
        Thread serverThread = new Thread(()->Server.start());
        serverThread.setDaemon(true);   // close with application
        serverThread.start();
       
    }
    
    public static void start(){      
        // will be chosen from gui

        
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
                        newPlayerData.id = clientsCount;
                        StartServerData startServerData = new StartServerData(Client.getPlayers(), Client.getTime());
                        startServerData.send(outputStreams[clientsCount]);
                        
                        clientsCount++;
                        
                        _mutex.lock();
                        messagesToHandle.addLast(new Pair(newPlayerData, -1));
                        _mutex.unlock();
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
                int skip = (Integer)DataAndSkipIndex.getValue();
                _mutex.unlock();
                SendableData data = (SendableData)DataAndSkipIndex.getKey();
                if(data.getType() == DataType.TurnEndedAcceptSignal){
                    acceptEndSignalCount++;
                    if(acceptEndSignalCount == clientsCount){
                        // everyone accepted that turn has ended
                        
                        acceptEndSignalCount = -1;
                        String winnerNick = game.endTurn();
                        ArrayList<Integer> updatedScores = new ArrayList<>();
                        for(int i = 0; i < Client.getPlayers().size(); ++i){
                            updatedScores.add(Client.getPlayers().get(i).getScore());
                        }
                        TurnEndedData ted = new TurnEndedData(updatedScores, winnerNick);
                        _mutex.lock();
                        messagesToHandle.addLast(new Pair(ted, -1));
                        _mutex.unlock();
                        game.chooseNextPlayer();
                    }
                }
                if(data.getType() == DataType.ChatMessage){
                    ChatMessageData cmd = (ChatMessageData)data;
                    if(game != null && game.verifyPassword(cmd.message,skip)){
                        game.updateCurrentTurnWinner(cmd.time, skip);
                        // tell every client that the turn has ended
                        if(acceptEndSignalCount == -1){
                            acceptEndSignalCount = 0;
                            _mutex.lock();
                            messagesToHandle.addLast(new Pair(new TurnEndedSignal(), -1));
                            _mutex.unlock();
                        }
                    }
                }
                
                
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
    public static Game getGame(){
        return game;
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

    public static void sendTo(int id,SendableData data){
        data.send(outputStreams[id]);
    }
    public static void timeThread(){
        long startTime = System.currentTimeMillis();
        long sleepTime = 1000;
        
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
    public static void startGame(){
        game = new Game(maxClients,600,90,Client.getPlayers());
        game.start();
    }
}

