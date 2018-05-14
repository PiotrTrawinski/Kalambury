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
    // reference to controller for communication
    private static MainWindowController controller;
    
    // initialization and accepting clients
    private static Thread acceptClientsThread;
    
    // receiveing and sending data threads
    private static Thread handleIncomingDataThread;
    private static Thread sendOutDataThread;
    
    // synchronized time between all clients
    private static Thread timeThreadObject;
    
    // array for the data that should be send by sendOutDataThread
    private static volatile ArrayDeque<ServerMessage>  messagesToHandle;
    private static final Lock messagesToHandleMutex = new ReentrantLock(true);
    
    // array with connected clients
    private static volatile ArrayList<ClientSocket> clients;
    private static final Lock clientsInMutex = new ReentrantLock(true);
    private static final Lock clientsOutMutex = new ReentrantLock(true);
    private static Integer port = null;
    
    // every player's unique id is mapped to his index in client array
    private static final TreeMap<Integer, Integer> playerIndexes = new TreeMap<>();
    
    // game logic
    private static Game game = null;
    
    private static int acceptEndSignalCount;
    private static boolean gamePaused = false;
    private static long turnStartTime = -1;
    
    public static void initialize(int port){
        //set the port that server is working on, and start it on a new thread
        Server.port = port;
        acceptEndSignalCount = -1;
        messagesToHandle = new ArrayDeque<>();
        clients = new ArrayList<>();
        game = null;
        acceptClientsThread = new Thread(()->Server.start());
        acceptClientsThread.setDaemon(true);   // close with application
        acceptClientsThread.start();
    }
    
    public static void quit(){
        // if port == null it means player is not a host - is not a server
        if(port != null){
            
            // stop all the threads
            acceptClientsThread.interrupt();
            handleIncomingDataThread.interrupt();
            sendOutDataThread.interrupt();
            timeThreadObject.interrupt();
            try {
                acceptClientsThread.join();
                handleIncomingDataThread.join();
                sendOutDataThread.join();
                timeThreadObject.join();
            } catch (InterruptedException ex) {}
            
            // close all client connections
            for(int i = 0; i < clients.size(); ++i){
                clients.get(i).close();
            }
            
            // destroy game object
            game = null;
        }
    }
    
    public static void start(){      
        // incoming data thread
        handleIncomingDataThread = new Thread(()->Server.handleIncomingData());
        handleIncomingDataThread.setDaemon(true);
        handleIncomingDataThread.start();
        
        // send incoming data to clients
        sendOutDataThread = new Thread( () -> Server.sendDataToClients());
        sendOutDataThread.setDaemon(true);
        sendOutDataThread.start();
        
        // time synchronization thread
        timeThreadObject = new Thread(() -> timeThread());
        timeThreadObject.setDaemon(true);
        timeThreadObject.setPriority(MAX_PRIORITY);
        timeThreadObject.start();
        
        // start accepting clients
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            acceptNewClients(serverSocket);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    
    // object reference setters
    public static void setController(MainWindowController controller){
        Server.controller = controller;
    }
    
    
    // create id to assign to new clients (players)
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
    
    // getters for the players (unique_id -> array_index) map object
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
    
    
    /*
        Accepting the new clients
    */
    private static void acceptNewClient(Socket socket) throws IOException{
        ClientSocket clientSocket = new ClientSocket(socket);
        
        clientSocket.receive();
        if(game != null){
            clientSocket.send(new SendableSignal(DataType.JoinRejectSignal, Client.getTime()));
            return;
        }
        clientSocket.send(new SendableSignal(DataType.JoinAcceptSignal, Client.getTime()));
        
        NewPlayerData newPlayerData = (NewPlayerData)clientSocket.receive();
        newPlayerData.id = createNewId();
        newPlayerData.time = Client.getTime();
        playerIndexes.put(newPlayerData.id, clients.size());
        StartServerData startServerData = new StartServerData(controller.getPlayers(), Client.getTime());
        clients.add(clientSocket);
        addLastMessageToHandle(new ServerMessage(startServerData, ServerMessage.ReceiverType.One, clients.size()-1));

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
        } finally{
            clientsInMutex.unlock();
            clientsOutMutex.unlock();
        }
        addLastMessageToHandle(
            new ServerMessage(new PlayerQuitData(clientIndex, Client.getTime()), ServerMessage.ReceiverType.All)
        );
        if(game != null && game.playersSequenceSize() < 2){
            stopGame();
            game = null;
        }
        if(game != null && playerId == game.getDrawingPlayerId()){
            skipTurn();
        }
    }
    
    
    /*
        methods accesing messages queue shared by sending and receiving threads
    */
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
    
    
    /*
        Sending data to the clients
    */
    private static void sendTo(SendableData data, int clientIndex){
        try{
            clientsOutMutex.lock();
            if(clientIndex < clients.size()){
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
        for(int i = 0; i < clients.size(); i++){
            if(i != exceptIndex){
                sendTo(data, i);
            }
        }
    }
    private static void sendAll(SendableData data){
        for(int i = 0; i < clients.size(); i++){
            sendTo(data, i);
        }
    }
    private static void send(ServerMessage message){
        SendableData data = message.getData();
        
        // some messages require specific handling 
        switch(data.getType()){
        case TurnTimeOutSignal:
            sendAll(data);
            if(!gamePaused){
                gameNextTurn();
            }
            break;
        case GameStoppedSignal:
            game = null;
            sendAll(data);
            break;
        case TurnSkippedSignal:{
            acceptEndSignalCount = -1;
            sendAll(data);
            if(!gamePaused){
                gameNextTurn();
            }
            break;
        }case TurnEndedAcceptSignal:{
            acceptEndSignalCount++;
            if(acceptEndSignalCount == clients.size()){
                // everyone accepted that turn has ended
                acceptEndSignalCount = -1;
                String winnerNick = game.endTurn();
                ArrayList<Integer> updatedScores = new ArrayList<>();
                for(int i = 0; i < controller.getPlayers().size(); ++i){
                    updatedScores.add(controller.getPlayers().get(i).getScore());
                }
                TurnEndedData ted = new TurnEndedData(updatedScores, winnerNick, Client.getTime());
                sendAll(ted);
                
                if(!gamePaused){
                    gameNextTurn();
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
                    turnStartTime = -1;
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
    private static void sendDataToClients(){
        while(!Thread.interrupted()){
            while(messagesToHandle.size() > 0){
                ServerMessage message = getFirstMessageToHandle();
                send(message);
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
        get data from clients when avaliable and save in messagesToHandle array
        for the sending thread to take care of
    */
    public static void handleIncomingData(){
        while(!Thread.interrupted()){
            boolean hadData = false;
            for(int i = 0; i < clients.size(); i++){
                try{
                    clientsInMutex.lock();
                    if(i < clients.size() && clients.get(i).hasDataToReceive()){
                        hadData = true;
                        
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
            
            if(!hadData){
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    
    /*
        time synchronization
    */
    public static void timeThread(){
        long startTime = System.currentTimeMillis();
        long sleepTime = 100;
        int sleepCount = 0;
        TimeData timeData = new TimeData(0);
        
        while(!Thread.interrupted()){
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            timeData.time = System.currentTimeMillis() - startTime;
            if(game!=null && turnStartTime!=-1 && (timeData.time - turnStartTime)/1000.0 > game.getTurnTime()){
                addFirstMessageToHandle(new ServerMessage(
                    new SendableSignal(DataType.TurnTimeOutSignal, Client.getTime()), ServerMessage.ReceiverType.All)
                );
                turnStartTime = -1;
            }
            sleepCount++;
            if(sleepCount % 10 == 0){
                addFirstMessageToHandle(new ServerMessage(timeData, ServerMessage.ReceiverType.All));
            }
        }
    }
    
    
    /*
        initializing/controlling the game
    */
    public static void startGame(int numberOfTurns, int subTurnTime){
        if(gamePaused){
            // continue the previous unfinished game
            gameNextTurn();
        } else {
            // create new game
            game = new Game(numberOfTurns, subTurnTime, controller.getPlayers());
            game.start();
            gamePaused = false;
            addLastMessageToHandle(new ServerMessage(
                new GameStartedData(numberOfTurns, Client.getTime()),
                ServerMessage.ReceiverType.All
            ));
            int drawingPlayerId = game.chooseNextPlayer();
            int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
            GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
            turnStartTime = Client.getTime();
            TurnStartedData tsd = new TurnStartedData(turnStartTime, game.getTurnTime(), true, drawingPlayerId);
            addLastMessageToHandle(new ServerMessage(tsd, ServerMessage.ReceiverType.One, drawingPlayerIndex));
            addLastMessageToHandle(new ServerMessage(passwordData, ServerMessage.ReceiverType.One, drawingPlayerIndex));
            TurnStartedData tsd2 = new TurnStartedData(tsd.startTime, tsd.turnTime, false, drawingPlayerId);
            addLastMessageToHandle(new ServerMessage(tsd2, ServerMessage.ReceiverType.AllExcept, drawingPlayerIndex));
        }
    }
    
    private static void gameNextTurn(){
        if(game != null){
            int drawingPlayerId = game.chooseNextPlayer();
            if(drawingPlayerId != -1){
                int drawingPlayerIndex = getPlayerIndex(drawingPlayerId);
                GamePasswordData passwordData = new GamePasswordData(game.chooseNextPassword());
                turnStartTime = Client.getTime();
                TurnStartedData tsd = new TurnStartedData(turnStartTime, game.getTurnTime(), true, drawingPlayerId);
                sendTo(tsd, drawingPlayerIndex);
                sendTo(passwordData, drawingPlayerIndex);
                tsd.isDrawing = false;
                sendExcept(tsd, drawingPlayerIndex);
                gamePaused = false;
            } else {
                game = null;
                addLastMessageToHandle(new ServerMessage(
                    new SendableSignal(DataType.GameEndedSignal, Client.getTime()),
                    ServerMessage.ReceiverType.All
                ));
            }
        }
    }
    
    public static void stopGame(){
        gamePaused = false;
        turnStartTime = -1;
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.GameStoppedSignal, Client.getTime()), 
            ServerMessage.ReceiverType.All
        ));
    }
    
    public static void skipTurn(){
        turnStartTime = -1;
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.TurnSkippedSignal, Client.getTime()), 
            ServerMessage.ReceiverType.All
        ));
    }
    
    public static void pauseGame(){
        addLastMessageToHandle(new ServerMessage(
            new SendableSignal(DataType.GamePausedSignal, Client.getTime()), 
            ServerMessage.ReceiverType.All
        ));
        gamePaused = true;
    }
}

