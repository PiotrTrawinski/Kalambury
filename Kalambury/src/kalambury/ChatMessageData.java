package kalambury;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ChatMessageData extends SendableData{
    private int var1;
    private String var2;
    //...
    
    public ChatMessageData(DataInputStream in){
        //here read var1, var2 from outputStream
    }

    @Override public void send(DataOutputStream out) {
        //here send var1, var2 to inputStream
    }
}
