package kalambury;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int maxClients = 5;
    private static Socket sockets[] = new Socket[maxClients];
    private static InputStream streams[] = new InputStream[maxClients];
    private static int clientsCount = 0;
    private static int port;
    private static  LinkedList<byte[]>  messagesToHandle = new LinkedList<byte[]>();
    
    public static void start(){
        Thread t = new Thread(()->handleIncomingData());
        t.setDaemon(true);
        t.start();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                if(clientsCount < maxClients){
                    Socket socket = serverSocket.accept();
                    try{InputStream in = socket.getInputStream();
                        sockets[clientsCount] = socket;
                        streams[clientsCount] = in;
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
        //System.out.println("Handle");
        while(true){
            //System.out.println("while");
        for(int i = 0; i < clientsCount; i++){ // for every client
                 //System.out.println("reading");
                byte[] buffer = new byte[1024];
                try{
                    int count;
                    if(streams[i].available() > 0){
                        System.out.println("Something to read");
                        while ((count = streams[i].read(buffer)) > 0) {
                            //Server.messagesToHandle.add(buffer);
                            System.out.println("Message Received");
                        }
                    }
                    else{
                        System.out.print("Nothing to read.");
                    }
                }
                catch(IOException ex){System.out.println(ex.getMessage());};
             }
        }
    }
    

}

