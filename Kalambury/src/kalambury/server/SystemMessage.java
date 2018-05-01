package kalambury.server;

public class SystemMessage{
    public String message;
    public long time;
    public SystemMessageType type;
    
    public SystemMessage(String message, long time, SystemMessageType type){
        this.message = message;
        this.time    = time;
        this.type    = type;
    }
}
