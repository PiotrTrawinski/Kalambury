package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SkipRequestData extends SendableData{
    public String nickName;
    public long time;
    
    public SkipRequestData(String nickName, long time){
        type = DataType.SkipRequest;
        this.nickName = nickName;
        this.time = time;
    }
    
    public SkipRequestData(DataInputStream in) throws IOException{
        type = DataType.SkipRequest;
        
        nickName = in.readUTF();
        time = in.readLong();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
      
        out.writeUTF(nickName);
        out.writeLong(time);
    }
}
