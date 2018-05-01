package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NewPlayerData extends SendableData{
    public String nickName;
    public int id;
    
    public NewPlayerData(String nickName, int id){
        type = DataType.NewPlayerData;
        this.nickName = nickName;
        this.id = id;
    }
    
    public NewPlayerData(DataInputStream in){
        type = DataType.NewPlayerData;
        
        try {
            nickName = in.readUTF();
            id = in.readInt();
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeUTF(nickName);
            out.writeInt(id);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
