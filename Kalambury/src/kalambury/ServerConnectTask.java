/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kalambury;

import java.io.IOException;
import java.net.Socket;
import javafx.concurrent.Task;

/**
 *
 * @author honzi
 */
public class ServerConnectTask extends Task<ConnectResult> {
    String ip;
    int port;
    
    public ServerConnectTask(String ip, int port){
        this.ip = ip;
        this.port = port;
    }
    
    @Override
    protected ConnectResult call(){
         try{
            Socket socket = new Socket(ip, port);
            Client.setSocket(socket);
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
         return null;
    }
    
    
    
    
}
