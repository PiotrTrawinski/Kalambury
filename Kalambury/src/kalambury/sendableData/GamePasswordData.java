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
    
    public GamePasswordData(DataInputStream in) throws IOException{
        type = DataType.GamePassword;

        password = in.readUTF();
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeUTF(password);
        out.flush();
    }
}
