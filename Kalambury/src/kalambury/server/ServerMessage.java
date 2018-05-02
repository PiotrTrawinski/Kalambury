package kalambury.server;

import kalambury.sendableData.SendableData;

public class ServerMessage {
    public enum ReceiverType{
        All,
        AllExcept,
        One
    }
    
    private final SendableData data;
    private final ReceiverType receiverType;
    private final int param;
    
    public ServerMessage(SendableData data, ReceiverType receiverType, int param){
        this.data = data;
        this.receiverType = receiverType;
        this.param = param;
    }
    public ServerMessage(SendableData data, ReceiverType receiverType){
        this.data = data;
        this.receiverType = receiverType;
        this.param = -1;
    }
    
    public SendableData getData(){
        return data;
    }
    public ReceiverType getReceiverType(){
        return receiverType;
    }
    public int getParam(){
        return param;
    }
}
