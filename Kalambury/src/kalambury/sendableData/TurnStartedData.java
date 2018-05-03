package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TurnStartedData extends SendableData{
    public long startTime;
    public long turnTime;
    public boolean isDrawing;
    public int drawingPlayerId;
    
    public TurnStartedData(long startTime, long turnTime, boolean isDrawing, int drawingPlayerId){
        type = DataType.TurnStarted;
        this.startTime = startTime;
        this.turnTime = turnTime;
        this.isDrawing = isDrawing;
        this.drawingPlayerId = drawingPlayerId;
    }
    
    public TurnStartedData(DataInputStream in) throws IOException{
        type = DataType.TurnStarted;
        
        startTime = in.readLong();
        turnTime = in.readLong();
        isDrawing = in.readBoolean();
        drawingPlayerId = in.readInt();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());

        out.writeLong(startTime);
        out.writeLong(turnTime);
        out.writeBoolean(isDrawing);
        out.writeInt(drawingPlayerId);
    }
}
