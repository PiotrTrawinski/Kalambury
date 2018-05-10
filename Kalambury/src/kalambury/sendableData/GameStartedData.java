package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GameStartedData extends SendableData{
    public int numberOfTurns;
    public long time;
    
    public GameStartedData(int numberOfTurns, long time){
        type = DataType.GameStarted;
        this.numberOfTurns = numberOfTurns;
        this.time = time;
    }
    
    public GameStartedData(DataInputStream in) throws IOException{
        type = DataType.GameStarted;
        numberOfTurns = in.readInt();
        time = in.readLong();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeInt(numberOfTurns);
        out.writeLong(time);
    }
}
