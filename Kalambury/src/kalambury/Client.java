
package kalambury;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private static DataOutputStream out;
    
    public static void setIP(String ip){
        Client.ip = ip;
    }
    public static void setSocket(Socket s){
       Client.socket = s;
       try {
            Client.out = new DataOutputStream(new BufferedOutputStream(Client.socket.getOutputStream()));
      } catch(IOException e){
         System.err.println(e.getMessage());
      }
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
    public static void sendMessage(String buffer){
        try{
            out.writeUTF(buffer);
            out.flush();
            System.out.println("Success");
     }catch(IOException e){
            System.err.println(e.getMessage());
     }
        
        
    }
    
    
}
