package kalambury;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.scene.paint.Color;

public class BucketFillData extends SendableData{
    public Pixel pixel;
    public Color replacementColor;
    
    public BucketFillData(Pixel pixel, Color replacementColor){
        type = DataType.BucketFill;
        
        this.pixel = pixel;
        this.replacementColor = replacementColor;
    }
    
    public BucketFillData(DataInputStream in){
        type = DataType.BucketFill;
        
        try {
            int x = in.readInt();
            int y = in.readInt();
            double targetColorR = in.readDouble();
            double targetColorG = in.readDouble();
            double targetColorB = in.readDouble();
            double replacementColorR = in.readDouble();
            double replacementColorG = in.readDouble();
            double replacementColorB = in.readDouble();
            
            pixel = new Pixel(x, y, Color.color(targetColorR, targetColorG, targetColorB));
            replacementColor = Color.color(replacementColorR, replacementColorG, replacementColorB);
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            out.writeInt(pixel.x);
            out.writeInt(pixel.y);
            out.writeDouble(pixel.color.getRed());
            out.writeDouble(pixel.color.getGreen());
            out.writeDouble(pixel.color.getBlue());
            out.writeDouble(replacementColor.getRed());
            out.writeDouble(replacementColor.getGreen());
            out.writeDouble(replacementColor.getBlue());
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"", ex.getMessage());
        }
    }
}