package kalambury;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class LineDrawData extends SendableData{
    private int var1;
    private String var2;
    //...
    
    public LineDrawData(DataOutputStream out){
        //here read var1, var2 from outputStream
    }

    @Override public void send(DataInputStream in) {
        //here send var1, var2 to inputStream
    }
}
