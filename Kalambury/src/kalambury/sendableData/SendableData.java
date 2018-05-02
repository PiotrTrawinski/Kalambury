package kalambury.sendableData;

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
            System.err.printf("error reading data type from stream, system error: \"%s\"\n", ex.getMessage());
        }
        
        switch(type){
        case Unknown:               return null;
        case LineDraw:              return new LineDrawData(in);
        case FloodFill:             return new FloodFillData(in);
        case ChatMessage:           return new ChatMessageData(in);
        case StartServerData:       return new StartServerData(in);
        case NewPlayerData:         return new NewPlayerData(in);
        case Time:                  return new TimeData(in);
        case GamePassword:          return new GamePasswordData(in);
        case TurnEndedData:         return new TurnEndedData(in);
        case TurnStarted:           return new TurnStartedData(in);
        default:                    return new SendableSignal(type);
        }
    }

    public abstract void send(DataOutputStream out);
}
