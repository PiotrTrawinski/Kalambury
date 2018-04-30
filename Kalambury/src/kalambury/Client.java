
package kalambury;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
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
    private static DataInputStream in;
    private static Chat chat;
    
    public static String getNick(){
        return nick;
    }
    public static void setChat(Chat chat){
        Client.chat = chat;
    }
    public static void setIP(String ip){
        Client.ip = ip;
    }
    public static void setSocket(Socket s){
        Client.socket = s;
        try {
            Client.out = new DataOutputStream(new BufferedOutputStream(Client.socket.getOutputStream()));
            Client.in = new DataInputStream(new BufferedInputStream(Client.socket.getInputStream()));
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
    public static void sendMessage(SendableData data){
            data.send(out);
    }
    public static void listen(){
        while(true){

            try{
                    if(in.available() > 0){
                        final SendableData input = SendableData.receive(in);
                        // tutaj obsluzyc te dane
                        ChatMessageData cmd = (ChatMessageData)input;
                        chat.handleNewServerMessage(cmd);
                        System.out.println("Data received");
                    }
                }
                catch(IOException ex){System.out.println(ex.getMessage());};
        }
    }
    
}
