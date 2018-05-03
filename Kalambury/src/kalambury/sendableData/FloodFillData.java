package kalambury.sendableData;

import kalambury.mainWindow.drawingBoard.Pixel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.scene.paint.Color;

public class FloodFillData extends SendableData{
    public Pixel pixel;
    public Color replacementColor;
    
    public FloodFillData(Pixel pixel, Color replacementColor){
        type = DataType.FloodFill;
        
        this.pixel = pixel;
        this.replacementColor = replacementColor;
    }
    
    public FloodFillData(DataInputStream in) throws IOException{
        type = DataType.FloodFill;
        
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
    }

    @Override public void send(DataOutputStream out) throws IOException {
        out.writeInt(type.toInt());
        out.writeInt(pixel.x);
        out.writeInt(pixel.y);
        out.writeDouble(pixel.color.getRed());
        out.writeDouble(pixel.color.getGreen());
        out.writeDouble(pixel.color.getBlue());
        out.writeDouble(replacementColor.getRed());
        out.writeDouble(replacementColor.getGreen());
        out.writeDouble(replacementColor.getBlue());
        out.flush();
    }
}