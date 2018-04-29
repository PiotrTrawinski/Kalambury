package kalambury;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int maxClients = 5;
    private static Socket sockets[] = new Socket[maxClients];
    private static DataInputStream inputStreams[] = new DataInputStream[maxClients];
    private static DataOutputStream outputStreams[] = new DataOutputStream[maxClients];
    private static volatile int clientsCount = 0;
    private static int port;
    private static  LinkedList<byte[]>  messagesToHandle = new LinkedList<byte[]>();
    
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
                        clientsCount++;
                    }
                    catch(IOException ex){}
                    }
                }
            }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public static void setPort(int port){
        Server.port = port;
    }
    
    
    public static void handleIncomingData(){
        while(true){
            for(int i = 0; i < clientsCount; i++){ // for every client
                try{
                    if(inputStreams[i].available() > 0){
                        String input = inputStreams[i].readUTF();
                    }
                }
                catch(IOException ex){System.out.println(ex.getMessage());};
            }
        }
    }

}

