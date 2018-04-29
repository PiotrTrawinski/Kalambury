package kalambury;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int maxClients = 5;
    private static Socket sockets[] = new Socket[maxClients];
    private static DataInputStream streams[] = new DataInputStream[maxClients];
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
                        streams[clientsCount] = new DataInputStream(socket.getInputStream());
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
                byte[] buffer = new byte[1024];
                try{
                    int count;
                    if(streams[i].available() > 0){
                        System.out.println("Something to read");
                        }
                    }
                catch(IOException ex){System.out.println(ex.getMessage());};
            }
        }
    }

}

