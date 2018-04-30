
package kalambury;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static long time;
    public static Thread timeThreadObject;
    
    public static String getNick(){
        return nick;
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
    
    
    public static long getTime(){
        return time;
    }
    public static void timeThread(){
        time = 0;
        long syncTime = 0;
        long timeAfterSync = 0;
        long sleepTime = 20;
        long timeBeforeSleep = System.currentTimeMillis();
        boolean firstTime = true;
        while(true){
            if("server has time" == "true"){
                syncTime = 0; // timeSocket.getTime()
                timeAfterSync = 0;
                timeBeforeSleep = System.currentTimeMillis();
            }
            long deltaTimeAfterSync = System.currentTimeMillis() - timeBeforeSleep;
            if(!firstTime && deltaTimeAfterSync < sleepTime/2){
                // player changed his system clock
                deltaTimeAfterSync = sleepTime;
            }
            timeAfterSync += deltaTimeAfterSync;
            time = syncTime + timeAfterSync;
            
            timeBeforeSleep = System.currentTimeMillis();
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException ex) {
                System.err.printf("error sleep: \"%s\"\n", ex.getMessage());
            }

            firstTime = false;
        }
    }
}
