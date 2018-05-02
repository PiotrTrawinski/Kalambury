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
    private static final DataInputStream inputStreams[] = new DataInputStream[maxClients];
    private static final DataOutputStream outputStreams[] = new DataOutputStream[maxClients];
    private static volatile int clientsCount = 0;
    
    private static int port;
    
    private static volatile ArrayDeque< Pair<SendableData,Integer> >  messagesToHandle = new ArrayDeque<Pair<SendableData,Integer>>();
    private static final Lock messagesToHandleMutex = new ReentrantLock(true);
    
    private static int acceptEndSignalCount = -1;
    
    private static final TimeData timeData = new TimeData(0);
    
    private static Game game = null;
    
    
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
            acceptNewClients(serverSocket);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    
    private static void acceptNewClient(Socket socket) throws IOException{
        sockets[clientsCount] = socket;
        inputStreams[clientsCount] = new DataInputStream(socket.getInputStream());
        outputStreams[clientsCount] = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        NewPlayerData newPlayerData = (NewPlayerData)SendableData.receive(inputStreams[clientsCount]);
        newPlayerData.id = clientsCount;
        StartServerData startServerData = new StartServerData(Client.getPlayers(), timeData.time);
        startServerData.send(outputStreams[clientsCount]);

        clientsCount++;

        addLastMessageToHandle(new Pair(newPlayerData, -1));
    }
    
    private static void acceptNewClients(ServerSocket serverSocket) throws IOException{
        while (true) {
            if(clientsCount < maxClients){
                Socket socket = serverSocket.accept();
                acceptNewClient(socket);
            }
        }
    }
    
    public static void forwardDataToClients(){
        while(true){
            if(messagesToHandle.size() > 0){
                Pair DataAndSkipIndex = getFirstMessageToHandle();
                int skip = (Integer)DataAndSkipIndex.getValue();
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
                        addLastMessageToHandle(new Pair(ted, -1));
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
                            addLastMessageToHandle(new Pair(new TurnEndedSignal(), -1));
                        }
                    }
                }
                sendExcept(data,skip);
            }
        }   
    }
    
    
    public static Pair getFirstMessageToHandle(){
        messagesToHandleMutex.lock();
        Pair<SendableData,Integer> message = null;
        try {
            message = messagesToHandle.removeFirst();
        } finally {
            messagesToHandleMutex.unlock();
        }
        return message;
    }
    public static void addLastMessageToHandle(Pair<SendableData,Integer> messageToHandle){
        messagesToHandleMutex.lock();
        try {
            messagesToHandle.addLast(messageToHandle);
        } finally {
            messagesToHandleMutex.unlock();
        }
    }
    public static void addFirstMessageToHandle(Pair<SendableData,Integer> messageToHandle){
        messagesToHandleMutex.lock();
        try {
            messagesToHandle.addFirst(messageToHandle);
        } finally {
            messagesToHandleMutex.unlock();
        }
    }
    
    public static void sendTo(SendableData data, int clientId){
        data.send(outputStreams[clientId]);
        try{
            outputStreams[clientId].flush();
        }
        catch(IOException ex){
            System.err.println(ex.getMessage());
        }
    }
    public static void sendExcept(SendableData data, int exceptIndex){
        for(int i = 0; i < clientsCount; i++){
            if(i != exceptIndex){
                sendTo(data, i);
            }
        }
    }
    public static void sendAll(SendableData data){
        for(int i = 0; i < clientsCount; i++){
            sendTo(data, i);
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
                        //receive messsage and send it to all clients except the sender
                        final SendableData input = SendableData.receive(inputStreams[i]);
                        addLastMessageToHandle(new Pair(input,i));
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
        
        while(true){
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                System.err.printf("error sleep: \"%s\"\n", ex.getMessage());
            }
            timeData.time = System.currentTimeMillis() - startTime;
            
            addFirstMessageToHandle(new Pair(timeData, -1));
        }
    }
    
    public static void startGame(){
        game = new Game(maxClients,600,90,Client.getPlayers());
        game.start();
    }
}

