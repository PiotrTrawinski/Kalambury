package kalambury.client;

import java.io.IOException;
import java.net.Socket;
import javafx.concurrent.Task;


public class ServerConnectTask extends Task<Void> {
    String ip;
    int port;
    
    public ServerConnectTask(String ip, int port){
        this.ip = ip;
        this.port = port;
    }
    
    @Override protected Void call(){
         try{
            Socket socket = new Socket(ip, port);
            Client.setSocket(socket);
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
         return null;
    }
}
