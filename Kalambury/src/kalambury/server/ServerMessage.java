package kalambury.server;

import kalambury.sendableData.SendableData;

public class ServerMessage {
    private enum ReceiverType{
        All,
        AllExcept,
        One
    }
    
    SendableData data;
    ReceiverType receiverType;
    int param;
    
    
}
