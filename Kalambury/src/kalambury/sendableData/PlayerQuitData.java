package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerQuitData extends SendableData{
    public int index;
    public long time;
    
    public PlayerQuitData(int index, long time){
        type = DataType.PlayerQuit;
        this.index = index;
        this.time = time;
    }
    
    public PlayerQuitData(DataInputStream in) throws IOException{
        type = DataType.PlayerQuit;
        index = in.readInt();
        time = in.readLong();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeInt(index);
        out.writeLong(time);
        out.flush();
    }
}
