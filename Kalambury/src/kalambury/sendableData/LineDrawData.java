package kalambury.sendableData;

import kalambury.mainWindow.drawingBoard.Rect;
import kalambury.mainWindow.drawingBoard.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javafx.scene.paint.Color;

public class LineDrawData extends SendableData{
    public Point startPoint;
    public Point endPoint;
    public Rect drawRect;
    public int lineThickness;
    public Color color;
    
    public LineDrawData(Point startPoint, Point endPoint, Rect drawRect, int lineThickness, Color color){
        type = DataType.LineDraw;
        
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.drawRect = drawRect;
        this.lineThickness = lineThickness;
        this.color = color;
    }
    
    public LineDrawData(DataInputStream in){
        type = DataType.LineDraw;
        
        try {
            startPoint = new Point(0, 0);
            endPoint = new Point(0, 0);
            drawRect = new Rect(0, 0, 0, 0);
            
            startPoint.x = in.readInt();
            startPoint.y = in.readInt();
            endPoint.x   = in.readInt();
            endPoint.y   = in.readInt();
            
            drawRect.x = in.readInt();
            drawRect.y = in.readInt();
            drawRect.w = in.readInt();
            drawRect.h = in.readInt();
            
            lineThickness = in.readInt();
            
            double colorR = in.readDouble();
            double colorG = in.readDouble();
            double colorB = in.readDouble();
            color = Color.color(colorR, colorG, colorB);
        } catch (IOException ex) {
            System.err.printf("error reading data from stream, system error: \"%s\"", ex.getMessage());
        }
    }

    @Override public void send(DataOutputStream out) {
        try {
            out.writeInt(type.toInt());
            
            out.writeInt(startPoint.x);
            out.writeInt(startPoint.y);
            out.writeInt(endPoint.x);
            out.writeInt(endPoint.y);
            
            out.writeInt(drawRect.x);
            out.writeInt(drawRect.y);
            out.writeInt(drawRect.w);
            out.writeInt(drawRect.h);
            
            out.writeInt(lineThickness);
            
            out.writeDouble(color.getRed());
            out.writeDouble(color.getGreen());
            out.writeDouble(color.getBlue());
        } catch (IOException ex) {
            System.err.printf("error writing data to stream, system error: \"%s\"", ex.getMessage());
        }
    }
}
