package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TurnStartedData extends SendableData{
    public long startTime;
    public long turnTime;
    public boolean isDrawing;
    
    public TurnStartedData(long startTime, long turnTime, boolean isDrawing){
        type = DataType.TurnStarted;
        this.startTime = startTime;
        this.turnTime = turnTime;
        this.isDrawing = isDrawing;
    }
    
    public TurnStartedData(DataInputStream in){
        type = DataType.TurnStarted;
        try {
            startTime = in.readLong();
            turnTime = in.readLong();
            isDrawing = in.readBoolean();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            
            out.writeLong(startTime);
            out.writeLong(turnTime);
            out.writeBoolean(isDrawing);
            
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
