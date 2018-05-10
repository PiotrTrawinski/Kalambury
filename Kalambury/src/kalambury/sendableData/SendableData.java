package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class SendableData {
    protected DataType type = DataType.Unknown;
    
    public DataType getType(){
        return type;
    }
    
    public static SendableData receive(DataInputStream in) throws IOException{
        DataType type = DataType.fromInt(in.readInt());
        
        switch(type){
        case Unknown:          return null;
        case LineDraw:         return new LineDrawData(in);
        case FloodFill:        return new FloodFillData(in);
        case ChatMessage:      return new ChatMessageData(in);
        case StartServerData:  return new StartServerData(in);
        case NewPlayerData:    return new NewPlayerData(in);
        case Time:             return new TimeData(in);
        case GamePassword:     return new GamePasswordData(in);
        case TurnEndedData:    return new TurnEndedData(in);
        case TurnStarted:      return new TurnStartedData(in);
        case PlayerQuit:       return new PlayerQuitData(in);
        case GameStarted:      return new GameStartedData(in);
        default:               return new SendableSignal(type, in);
        }
    }

    public abstract void send(DataOutputStream out) throws IOException;
}
