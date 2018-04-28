package kalambury;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int maxClients = 5;
    private static Socket sockets[] = new Socket[maxClients];
    private static int clientsCount = 0;
    private static int port;

    public static void start(){
        Thread t = new Thread(()->handleIncomingData());
        t.start();
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                if(clientsCount < maxClients){
                    sockets[clientsCount++] = socket;
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
        System.out.println("handling incoming data...");
    }

}

