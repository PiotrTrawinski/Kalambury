package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NewPlayerData extends SendableData{
    public String nickName;
    public int id;
    public long time;
    
    public NewPlayerData(String nickName, int id, long time){
        type = DataType.NewPlayerData;
        this.nickName = nickName;
        this.id = id;
        this.time = time;
    }
    
    public NewPlayerData(DataInputStream in) throws IOException{
        type = DataType.NewPlayerData;
        
        nickName = in.readUTF();
        id = in.readInt();
        time = in.readLong();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeUTF(nickName);
        out.writeInt(id);
        out.writeLong(time);
        out.flush();
    }
}
