package kalambury.sendableData;

import java.io.DataOutputStream;
import java.io.IOException;

public class SendableSignal extends SendableData{
    
    public SendableSignal(DataType signal){
        this.type = signal;
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.flush();
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"\n", ex.getMessage());
        }
    }
}
