package kalambury.server;

import kalambury.sendableData.SendableData;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int maxClients = 5;
    private static final Socket sockets[] = new Socket[maxClients];
    private static final DataInputStream inputStreams[] = new DataInputStream[maxClients];
    private static final DataOutputStream outputStreams[] = new DataOutputStream[maxClients];
    private static volatile int clientsCount = 0;
    private static int port;
    private static final  LinkedList<byte[]>  messagesToHandle = new LinkedList<byte[]>();
    
    public static void initialize(int port){
        //set the port that server is working on, and start it on a new thread
        Server.port = port;
        Thread serverThread = new Thread(()->Server.start());
        serverThread.setDaemon(true);   // close with application
        serverThread.start();
    }
    
    public static void start(){        
        Thread t = new Thread(()->Server.handleIncomingData());
        t.setDaemon(true);
        t.start();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                if(clientsCount < maxClients){
                    Socket socket = serverSocket.accept();
                    try{
                        sockets[clientsCount] = socket;
                        inputStreams[clientsCount] = new DataInputStream(socket.getInputStream());
                        outputStreams[clientsCount] = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                        clientsCount++;
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
                        Thread sendOutDataThread = new Thread( () -> Server.sendExcept(input,skip));
                        sendOutDataThread.setDaemon(true);
                        sendOutDataThread.start();
                        //message received, send it to clients except the sender client
                    }
                } catch(IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}

