package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendableSignal extends SendableData{
    public long time;
    
    public SendableSignal(DataType signal, long time){
        this.type = signal;
        this.time = time;
    }

     public SendableSignal(DataType type, DataInputStream in) throws IOException{
        this.type = type;
        time = in.readLong();
    }
    
    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeLong(time);
        out.flush();
    }
}
