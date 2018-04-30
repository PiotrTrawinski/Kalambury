package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatMessageData extends SendableData{
    public String nickName;
    public String message;
    public double time;
    public boolean isHost;
    
    public ChatMessageData(String nickName, String message, double time, boolean isHost){
        type = DataType.ChatMessage;
        
        this.nickName = nickName;
        this.message  = message;
        this.time     = time;
        this.isHost   = isHost;
    }
    
    public ChatMessageData(DataInputStream in){
        type = DataType.ChatMessage;
        
        try {
            nickName = in.readUTF();
            message  = in.readUTF();
            time     = in.readDouble();
            isHost   = in.readBoolean();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeUTF(nickName);
            out.writeUTF(message);
            out.writeDouble(time);
            out.writeBoolean(isHost);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
