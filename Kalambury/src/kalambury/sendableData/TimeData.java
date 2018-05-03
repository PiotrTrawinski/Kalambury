package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TimeData extends SendableData{
    public long time;
    
    public TimeData(long time){
        type = DataType.Time;
        this.time = time;
    }
    
    public TimeData(DataInputStream in) throws IOException{
        type = DataType.Time;
        time = in.readLong();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeLong(time);
        out.flush();
    }
}
