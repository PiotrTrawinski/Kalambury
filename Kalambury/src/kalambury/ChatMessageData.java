package kalambury;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatMessageData extends SendableData{
    public String nickName;
    public String message;
    double time;
    
    public ChatMessageData(String nickName, String message, double time){
        this.nickName = nickName;
        this.message  = message;
        this.time     = time;
    }
    
    public ChatMessageData(DataInputStream in){
        type = DataType.ChatMessage;
        
        try {
            nickName = in.readUTF();
            message  = in.readUTF();
            time     = in.readDouble();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeUTF(nickName);
            out.writeUTF(message);
            out.writeDouble(time);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"", ex.getMessage());
        }
    }
}
