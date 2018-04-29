package kalambury;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class SendableData {
    private DataType type;
    
    public DataType getType(){
        return type;
    }
    
    public static SendableData recive(int typeInt, DataOutputStream out){
        DataType type = DataType.fromInt(typeInt);
        switch(type){
        case LineDraw:    return new LineDrawData(out);
        case BucketFill:  return new BucketFillData(out);
        case ChatMessage: return new ChatMessageData(out);
        default:          return null;
        }
    }

    public abstract void send(DataInputStream in);
}
