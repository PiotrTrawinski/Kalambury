package kalambury.server;

import java.io.IOException;
import java.net.Socket;
import javafx.concurrent.Task;
import kalambury.client.Client;
import kalambury.client.ConnectResult;


public class ServerConnectTask extends Task<ConnectResult> {
    String ip;
    int port;
    
    public ServerConnectTask(String ip, int port){
        this.ip = ip;
        this.port = port;
    }
    
    @Override protected ConnectResult call(){
         try{
            Socket socket = new Socket(ip, port);
            Client.setSocket(socket);
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
         return null;
    }
}
