package kalambury.sendableData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DrawingEndSignal extends SendableData{
    
    public DrawingEndSignal(){
        type = DataType.DrawingEndSignal;
    }
    
    public DrawingEndSignal(DataInputStream in){
        type = DataType.DrawingEndSignal;
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
