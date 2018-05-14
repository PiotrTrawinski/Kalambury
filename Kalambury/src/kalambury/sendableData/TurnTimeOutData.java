package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TurnTimeOutData extends SendableData{
    public long time;
    public String password;
    
    public TurnTimeOutData(long time, String password){
        type = DataType.TurnTimeOut;
        this.time = time;
        this.password = password;
    }
    
    public TurnTimeOutData(DataInputStream in) throws IOException{
        type = DataType.TurnTimeOut;
        time = in.readLong();
        password = in.readUTF();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeLong(time);
        out.writeUTF(password);
    }
}
