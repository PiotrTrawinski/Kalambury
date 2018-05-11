package kalambury.server;

import kalambury.game.Game;
import kalambury.sendableData.SendableData;
import java.io.IOException;
import static java.lang.Thread.MAX_PRIORITY;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import kalambury.mainWindow.MainWindowController;
import kalambury.sendableData.ChatMessageData;
import kalambury.sendableData.DataType;
import kalambury.sendableData.GamePasswordData;
import kalambury.sendableData.GameStartedData;
import kalambury.sendableData.PlayerQuitData;
import kalambury.sendableData.SendableSignal;
import kalambury.sendableData.TimeData;
import kalambury.sendableData.TurnEndedData;
import kalambury.sendableData.TurnStartedData;

public class Server {
    private static MainWindowController controller;
    
    private static Thread mainThread;
    private static Thread handleIncomingDataThread;
    private static Thread sendOutDataThread;
    private static Thread timeThreadObject;
    
    private static final Lock clientsInMutex = new ReentrantLock(true);
    private static final Lock clientsOutMutex = new ReentrantLock(true);
    private static ArrayList<ClientSocket> clients;
    private static volatile int clientsCount;
    
    private static Integer port = null;
    
    private static volatile ArrayDeque<ServerMessage>  messagesToHandle;
    private static final Lock messagesToHandleMutex = new ReentrantLock(true);
    
    private static int acceptEndSignalCount = -1;
    
    private static Game game = null;
    
    private static final TreeMap<Integer, Integer> playerIndexes = new TreeMap<>();
    
    
    public static void initialize(int port){
        //set the port that server is working on, and start it on a new thread
        Server.port = port;
        acceptEndSignalCount = -1;
        clientsCount = 0;
        messagesToHandle = new ArrayDeque<>();
        clients = new ArrayList<>();
        game = null;
        mainThread = new Thread(()->Server.start());
        mainThread.setDaemon(true);   // close with application
        mainThread.start();
    }
    
    public static void quit(){
        if(port != null){
            mainThread.interrupt();
            handleIncomingDataThread.interrupt();
            sendOutDataThread.interrupt();
            timeThreadObject.interrupt();
            
            try {
                mainThread.join();
                handleIncomingDataThread.join();
                sendOutDataThread.join();
                timeThreadObject.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            game = null;
            for(int i = 0; i < clients.size(); ++i){
                clients.get(i).close();
            }
        }
    }
    
    public static void start(){      
        // incoming data thread
        handleIncomingDataThread = new Thread(()->Server.handleIncomingData());
        handleIncomingDataThread.setDaemon(true);
        handleIncomingDataThread.start();
        
        // send incoming data to clients
        sendOutDataThread = new Thread( () -> Server.forwardDataToClients());
        sendOutDataThread.setDaemon(true);
        sendOutDataThread.start();
        
        // time thread
        timeThreadObject = new Thread(() -> timeThread());
        timeThreadObject.setDaemon(true);
        timeThreadObject.setPriority(MAX_PRIORITY);
        timeThreadObject.start();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            acceptNewClients(serverSocket);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public static void setController(MainWindowController controller){
        Server.controller = controller;
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
        clients.add(new ClientSocket(socket));

        NewPlayerData newPlayerData = (NewPlayerData)clients.get(clientsCount).receive();
        newPlayerData.id = createNewId();
        newPlayerData.time = Client.getTime();
        playerIndexes.put(newPlayerData.id, clientsCount);
        StartServerData startServerData = new StartServerData(controller.getPlayers(), Client.getTime());
        addLastMessageToHandle(new ServerMessage(startServerData, ServerMessage.ReceiverType.One, clientsCount));

        clientsCount++;

        addLastMessageToHandle(new ServerMessage(newPlayerData, ServerMessage.ReceiverType.All));
    }
    
    private static void acceptNewClients(ServerSocket serverSocket) throws IOException{
        serverSocket.setSoTimeout(1000);
        while (!Thread.interrupted()) {
            try{
                Socket socket = serverSocket.accept();
                acceptNewClient(socket);
            } catch(SocketTimeoutException ex){

            }
        }
        serverSocket.close();
    }
    
    private static void forwardDataToClients(){
        while(!Thread.interrupted()){
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
                    if(drawingPlayerId != -1){
                        int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
                        GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
                        TurnStartedData tsd = new TurnStartedData(Client.getTime(), game.getTurnTime(), true, drawingPlayerId);
                        sendTo(tsd, drawingPlayerIndex);
                        sendTo(passwordData, drawingPlayerIndex);
                        tsd.isDrawing = false;
                        sendExcept(tsd, drawingPlayerIndex);
                    } else {
                        game = null;
                        addLastMessageToHandle(new ServerMessage(
                            new SendableSignal(DataType.GameEndedSignal, Client.getTime()),
                            ServerMessage.ReceiverType.All
                        ));
                    }
                    break;
                }case TurnEndedAcceptSignal:{
                    acceptEndSignalCount++;
                    if(acceptEndSignalCount == clientsCount){
                        // everyone accepted that turn has ended
                        acceptEndSignalCount = -1;
                        String winnerNick = game.endTurn();
                        ArrayList<Integer> updatedScores = new ArrayList<>();
                        for(int i = 0; i < controller.getPlayers().size(); ++i){
                            updatedScores.add(controller.getPlayers().get(i).getScore());
                        }
                        TurnEndedData ted = new TurnEndedData(updatedScores, winnerNick, Client.getTime());
                        sendAll(ted);
                        
                        int drawingPlayerId = game.chooseNextPlayer();
                        if(drawingPlayerId != -1){
                            int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
                            GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
                            TurnStartedData tsd = new TurnStartedData(Client.getTime(), game.getTurnTime(), true, drawingPlayerId);
                            sendTo(tsd, drawingPlayerIndex);
                            sendTo(passwordData, drawingPlayerIndex);
                            tsd.isDrawing = false;
                            sendExcept(tsd, drawingPlayerIndex);
                        } else {
                            game = null;
                            addLastMessageToHandle(new ServerMessage(
                                new SendableSignal(DataType.GameEndedSignal, Client.getTime()),
                                ServerMessage.ReceiverType.All
                            ));
                        }
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
                            sendAll(new SendableSignal(DataType.TurnEndedSignal, Client.getTime()));
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
    
    
    private static ServerMessage getFirstMessageToHandle(){
        messagesToHandleMutex.lock();
        ServerMessage message = null;
        try {
            message = messagesToHandle.removeFirst();
        } finally {
            messagesToHandleMutex.unlock();
        }
        return message;
    }
    private static void addLastMessageToHandle(ServerMessage messageToHandle){
        messagesToHandleMutex.lock();
        try {
            messagesToHandle.addLast(messageToHandle);
        } finally {
            messagesToHandleMutex.unlock();
        }
    }
    private static void addFirstMessageToHandle(ServerMessage messageToHandle){
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
    
    private static void removeClient(int clientIndex){
        int playerId = getPlayerId(clientIndex);
        if(game != null){
            game.removePlayerFromSequence(playerId);  
        }
        clientsInMutex.lock();
        clientsOutMutex.lock();
        try{
            for(int i = clientIndex; i < clients.size()-1; ++i){
                clients.set(i, clients.get(i+1));
                playerIndexes.put(getPlayerId(i+1), i);
            }
            clients.remove(clients.size()-1);
            clientsCount--;
        } finally{
            clientsInMutex.unlock();
            clientsOutMutex.unlock();
        }
        addLastMessageToHandle(
            new ServerMessage(new PlayerQuitData(clientIndex, Client.getTime()), ServerMessage.ReceiverType.All)
        );
        if(game != null && playerId == game.getDrawingPlayerId()){
            skipTurn();
        }
    }
    
    private static void sendTo(SendableData data, int clientIndex){
        try{
            clientsOutMutex.lock();
            if(clientIndex < clientsCount){
                clients.get(clientIndex).send(data);
            }
        }
        catch(IOException ex){
            removeClient(clientIndex);
        } finally{
            clientsOutMutex.unlock();
        }
    }
    
    private static void sendExcept(SendableData data, int exceptIndex){
        for(int i = 0; i < clientsCount; i++){
            if(i != exceptIndex){
                sendTo(data, i);
            }
        }
    }
    private static void sendAll(SendableData data){
        for(int i = 0; i < clientsCount; i++){
            sendTo(data, i);
        }
    }

    public static void handleIncomingData(){
        while(!Thread.interrupted()){
            for(int i = 0; i < clientsCount; i++){ // for every client
                try{
                    clientsInMutex.lock();
                    if(i < clientsCount && clients.get(i).hasDataToReceive()){
                        //receive messsage and send it to all clients except the sender
                        final SendableData input = clients.get(i).receive();
                        addLastMessageToHandle(
                            new ServerMessage(input, ServerMessage.ReceiverType.AllExcept, i)
                        );
                    }
                } catch(IOException ex) {
                    System.err.println(ex.getMessage());
                } finally{
                    clientsInMutex.unlock();
                }
            }
        }
    }
    
    public static void timeThread(){
        long startTime = System.currentTimeMillis();
        long sleepTime = 1000;
        TimeData timeData = new TimeData(0);
        
        while(!Thread.interrupted()){
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            timeData.time = System.currentTimeMillis() - startTime;
            
            addFirstMessageToHandle(new ServerMessage(timeData, ServerMessage.ReceiverType.All));
        }
    }
    
    public static void startGame(){
        game = new Game(600, 90, 3, controller.getPlayers());
        game.start();
        addLastMessageToHandle(new ServerMessage(
            new GameStartedData(3, Client.getTime()),
            ServerMessage.ReceiverType.All
        ));
        int drawingPlayerId = game.chooseNextPlayer();
        int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
        GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
        TurnStartedData tsd = new TurnStartedData(Client.getTime(), game.getTurnTime(), true, drawingPlayerId);
        addLastMessageToHandle(new ServerMessage(tsd, ServerMessage.ReceiverType.One, drawingPlayerIndex));
        addLastMessageToHandle(new ServerMessage(passwordData, ServerMessage.ReceiverType.One, drawingPlayerIndex));
        TurnStartedData tsd2 = new TurnStartedData(tsd.startTime, tsd.turnTime, false, drawingPlayerId);
        addLastMessageToHandle(new ServerMessage(tsd2, ServerMessage.ReceiverType.AllExcept, drawingPlayerIndex));
    }
    
    public static void stopGame(){
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.GameStoppedSignal, Client.getTime()), 
            ServerMessage.ReceiverType.All
        ));
    }
    
    public static void skipTurn(){
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.TurnSkippedSignal, Client.getTime()), 
            ServerMessage.ReceiverType.All
        ));
    }
}

