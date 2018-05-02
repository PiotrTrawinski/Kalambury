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
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import kalambury.client.Client;
import kalambury.sendableData.NewPlayerData;
import kalambury.sendableData.StartServerData;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import kalambury.sendableData.ChatMessageData;
import kalambury.sendableData.DataType;
import kalambury.sendableData.GamePasswordData;
import kalambury.sendableData.SendableSignal;
import kalambury.sendableData.TimeData;
import kalambury.sendableData.TurnEndedData;
import kalambury.sendableData.TurnStartedData;

public class Server {
    private static final int maxClients = 5;
    private static final Socket sockets[] = new Socket[maxClients];
    private static final DataInputStream inputStreams[] = new DataInputStream[maxClients];
    private static final DataOutputStream outputStreams[] = new DataOutputStream[maxClients];
    private static volatile int clientsCount = 0;
    
    private static int port;
    
    private static volatile ArrayDeque<ServerMessage>  messagesToHandle = new ArrayDeque<>();
    private static final Lock messagesToHandleMutex = new ReentrantLock(true);
    
    private static int acceptEndSignalCount = -1;
    
    private static final TimeData timeData = new TimeData(0);
    
    private static Game game = null;
    
    private static final TreeMap<Integer, Integer> playerIndexes = new TreeMap<>();
    
    
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
    
    private static int createNewId(){
        Random random = new Random();
        boolean uniqueId = true;
        int newId;
        do{
            newId = random.nextInt(Integer.MAX_VALUE);
            for(Map.Entry<Integer, Integer> entry : playerIndexes.entrySet()){
                if(entry.getKey() == newId){
                    uniqueId = false;
                    break;
                }
            }
        }while(!uniqueId);
        
        return newId;
    }
    
    private static void acceptNewClient(Socket socket) throws IOException{
        sockets[clientsCount] = socket;
        inputStreams[clientsCount] = new DataInputStream(socket.getInputStream());
        outputStreams[clientsCount] = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        NewPlayerData newPlayerData = (NewPlayerData)SendableData.receive(inputStreams[clientsCount]);
        newPlayerData.id = createNewId();
        playerIndexes.put(newPlayerData.id, clientsCount);
        StartServerData startServerData = new StartServerData(Client.getPlayers(), timeData.time);
        startServerData.send(outputStreams[clientsCount]);

        clientsCount++;

        addLastMessageToHandle(new ServerMessage(newPlayerData, ServerMessage.ReceiverType.All));
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
                ServerMessage message = getFirstMessageToHandle();
                SendableData data = message.getData();
                
                switch(data.getType()){
                case GameStoppedSignal:
                    game = null;
                    sendAll(data);
                    break;
                case TurnSkippedSignal:{
                    acceptEndSignalCount = -1;
                    sendAll(data);
                    int drawingPlayerId = game.chooseNextPlayer();
                    int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
                    GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
                    TurnStartedData tsd = new TurnStartedData(timeData.time, game.getTurnTime(), true, drawingPlayerId);
                    sendTo(tsd, drawingPlayerIndex);
                    sendTo(passwordData, drawingPlayerIndex);
                    tsd.isDrawing = false;
                    sendExcept(tsd, drawingPlayerIndex);
                    break;
                }case TurnEndedAcceptSignal:{
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
                        sendAll(ted);
                        
                        int drawingPlayerId = game.chooseNextPlayer();
                        int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
                        GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
                        TurnStartedData tsd = new TurnStartedData(timeData.time, game.getTurnTime(), true, drawingPlayerId);
                        sendTo(tsd, drawingPlayerIndex);
                        sendTo(passwordData, drawingPlayerIndex);
                        tsd.isDrawing = false;
                        sendExcept(tsd, drawingPlayerIndex);
                    }
                    break;
                }case ChatMessage:
                    ChatMessageData cmd = (ChatMessageData)data;
                    int senderIndex = message.getParam();
                    int senderId = getPlayerId(senderIndex);
                    sendExcept(data, senderIndex);
                    if(game != null && game.verifyPassword(cmd.message, senderId)){
                        game.updateCurrentTurnWinner(cmd.time, senderId);
                        // tell every client that the turn has ended
                        if(acceptEndSignalCount == -1){
                            acceptEndSignalCount = 0;
                            sendAll(new SendableSignal(DataType.TurnEndedSignal));
                        }
                    }
                    break;
                default:
                    switch(message.getReceiverType()){
                    case All:
                        sendAll(data);
                        break;
                    case AllExcept:
                        sendExcept(data, message.getParam());
                        break;
                    case One:
                        sendTo(data, message.getParam());
                        break;
                    }
                }
            }
        }   
    }
    
    
    public static ServerMessage getFirstMessageToHandle(){
        messagesToHandleMutex.lock();
        ServerMessage message = null;
        try {
            message = messagesToHandle.removeFirst();
        } finally {
            messagesToHandleMutex.unlock();
        }
        return message;
    }
    public static void addLastMessageToHandle(ServerMessage messageToHandle){
        messagesToHandleMutex.lock();
        try {
            messagesToHandle.addLast(messageToHandle);
        } finally {
            messagesToHandleMutex.unlock();
        }
    }
    public static void addFirstMessageToHandle(ServerMessage messageToHandle){
        messagesToHandleMutex.lock();
        try {
            messagesToHandle.addFirst(messageToHandle);
        } finally {
            messagesToHandleMutex.unlock();
        }
    }
    
    public static Integer getPlayerIndex(int id){
        return playerIndexes.get(id);
    }
    public static Integer getPlayerId(int index){
        for(Map.Entry<Integer, Integer> entry : playerIndexes.entrySet()){
            if(entry.getValue() == index){
                return entry.getKey();
            }
        }
        return null;
    }
    
    public static void sendTo(SendableData data, int clientIndex){
        data.send(outputStreams[clientIndex]);
        try{
            outputStreams[clientIndex].flush();
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
                        addLastMessageToHandle(
                            new ServerMessage(input, ServerMessage.ReceiverType.AllExcept, i)
                        );
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
            
            addFirstMessageToHandle(new ServerMessage(timeData, ServerMessage.ReceiverType.All));
        }
    }
    
    public static void startGame(){
        game = new Game(maxClients,600,90,Client.getPlayers());
        game.start();
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.GameStartedSignal), 
            ServerMessage.ReceiverType.All
        ));
        int drawingPlayerId = game.chooseNextPlayer();
        int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
        GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
        TurnStartedData tsd = new TurnStartedData(timeData.time, game.getTurnTime(), true, drawingPlayerId);
        addLastMessageToHandle(new ServerMessage(tsd, ServerMessage.ReceiverType.One, drawingPlayerIndex));
        addLastMessageToHandle(new ServerMessage(passwordData, ServerMessage.ReceiverType.One, drawingPlayerIndex));
        TurnStartedData tsd2 = new TurnStartedData(tsd.startTime, tsd.turnTime, false, drawingPlayerId);
        addLastMessageToHandle(new ServerMessage(tsd2, ServerMessage.ReceiverType.AllExcept, drawingPlayerIndex));
    }
    
    public static void stopGame(){
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.GameStoppedSignal), 
            ServerMessage.ReceiverType.All
        ));
    }
    
    public static void skipTurn(){
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.TurnSkippedSignal), 
            ServerMessage.ReceiverType.All
        ));
    }
}

