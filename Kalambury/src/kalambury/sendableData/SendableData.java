package kalambury.sendableData;

import kalambury.sendableData.LineDrawData;
import kalambury.sendableData.FloodFillData;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class SendableData {
    protected DataType type = DataType.Unknown;
    
    public DataType getType(){
        return type;
    }
    
    public static SendableData receive(DataInputStream in){
        DataType type = DataType.Unknown;
        try {
            type = DataType.fromInt(in.readInt());
        } catch (IOException ex) {
            System.err.printf("error reading data type from stream, system error: \"%s\"", ex.getMessage());
        }
        
        switch(type){
        case LineDraw:    return new LineDrawData(in);
        case FloodFill:   return new FloodFillData(in);
        case ChatMessage: return new ChatMessageData(in);
        default:          return null;
        }
    }

    public abstract void send(DataOutputStream out);
}
