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
    
    public TimeData(DataInputStream in){
        type = DataType.Time;
        try {
            time = in.readLong();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeLong(time);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
