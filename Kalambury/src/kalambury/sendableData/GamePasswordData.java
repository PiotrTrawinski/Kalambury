package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GamePasswordData extends SendableData{
    public String password;

    
    public GamePasswordData(String password){
        type = DataType.GamePassword;
        this.password = password;

    }
    
    public GamePasswordData(DataInputStream in){
        type = DataType.GamePassword;
        
        try {
            password = in.readUTF();

        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"\n", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeUTF(password);
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
