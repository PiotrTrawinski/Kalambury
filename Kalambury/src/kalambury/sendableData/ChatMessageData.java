package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatMessageData extends SendableData{
    public String nickName;
    public String message;
    public long time;
    public boolean isHost;
    
    public ChatMessageData(String nickName, String message, long time, boolean isHost){
        type = DataType.ChatMessage;
        
        this.nickName = nickName;
        this.message  = message;
        this.time     = time;
        this.isHost   = isHost;
    }
    
    public ChatMessageData(DataInputStream in) throws IOException{
        type = DataType.ChatMessage;
        nickName = in.readUTF();
        message  = in.readUTF();
        time     = in.readLong();
        isHost   = in.readBoolean();
    }

    @Override public void send(DataOutputStream out) throws IOException{
        out.writeInt(type.toInt());
        out.writeUTF(nickName);
        out.writeUTF(message);
        out.writeLong(time);
        out.writeBoolean(isHost);
    }
}
