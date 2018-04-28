
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
    private static Socket socket;
    
    
    public static void setIP(String ip){
        Client.ip = ip;
    }
    public static void setSocket(Socket s){
        Client.socket = s;
    }
    
    public static void setPort(int port){
        Client.port = port;
    }
    public static void setNick(String nick){
        Client.nick = nick;
    }
    public static boolean isSocketSet(){
        return (socket != null);
    }
}
