package kalambury.mainWindow.drawingBoard;

import javafx.scene.paint.Color;

public class Pixel {
    public Color color;
    public int x;
    public int y;
    
    public Pixel(int x, int y, Color color){
        this.x = x;
        this.y = y;
        this.color = color;
    }
}

