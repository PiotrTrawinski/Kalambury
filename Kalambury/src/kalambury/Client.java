
package kalambury;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author honzi
 */
public class Client {
    private static String ip;
    private static String nick;
    private static int port;
    private Socket socket;
    public static void connectToServer() {
        try{
            Socket socket = new Socket(ip, port);
        }catch(IOException ex){
            System.out.println(ex.getMessage());
        }
        
    }
    
    
    public static void setIP(String ip){
        Client.ip = ip;
    }
    
    
    public static void setPort(int port){
        Client.port = port;
    }
    public static void setNick(String nick){
        Client.nick = nick;
    }
    
}
