package kalambury;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class DrawingBoard extends ResizableCanvas{
    private final int maxWidth = 1920;
    private final int maxHeight = 1080;
    private final ArrayList<Color> virtualPixelTable = new ArrayList<>();
    PixelWriter pixelWriter;
    ColorWidget colorWidget;
    boolean inDrawingMode = false;
    int mouseLastPosX = 0;
    int mouseLastPosY = 0;
    int lineThickness = 2;
    private DrawingTool drawingTool = DrawingTool.PENCIL;
    
    public DrawingBoard() {
        super();
        for(int i = 0; i < maxWidth*maxHeight; ++i){
            virtualPixelTable.add(Color.rgb(255,255,255));
        }
        pixelWriter = getGraphicsContext2D().getPixelWriter();
    }
    
    public void mousePressed(int x, int y){
        inDrawingMode = true;
        switch(drawingTool){
        case PENCIL:
            mouseLastPosX = x;
            mouseLastPosY = y;
            drawLine(x, y, x, y, drawX, drawY, drawWidth, drawHeight, lineThickness, colorWidget.getColor());
            break;
        case COLOR_PICKER:
            colorWidget.setColor(getPixelInCanvasRatio(x-drawX, y-drawY));
            break;
        case BUCKET:
            floodFill(x, y, colorWidget.getColor());
            break;
        }
        
    }
    
    public void mouseReleased(){
        inDrawingMode = false;
    }
    
    public void mouseMovedTo(int x, int y){
        if(inDrawingMode){
            switch(drawingTool){
            case PENCIL:
                drawLine(mouseLastPosX, mouseLastPosY, x, y, drawX, drawY, drawWidth, drawHeight, lineThickness, colorWidget.getColor());
                mouseLastPosX = x;
                mouseLastPosY = y;
                break;
            case COLOR_PICKER:
                colorWidget.setColor(getPixelInCanvasRatio(x-drawX, y-drawY));
                break;
            case BUCKET:
                break;
            }
        }
    }

    public void setLineThickness(int newLineThickness){
        lineThickness = newLineThickness;
    }
    
    public void setColorWidget(ColorWidget colorWidget){
        this.colorWidget = colorWidget;
    }
    
    public void setDrawingTool(DrawingTool drawingTool){
        this.drawingTool = drawingTool;
    }
    
    private Color getPixelInCanvas(int x, int y, double xRatio, double yRatio){
        ArrayList<Pixel> pixels = getCorrespondingVirtualTablePixels(x, y, xRatio, yRatio);
        double rAvg = 0;
        double gAvg = 0;
        double bAvg = 0;
        int numberOfNonWhiteColors = 0;
        for(Pixel pixel : pixels){
            if(!pixel.color.equals(Color.rgb(255, 255, 255))){
                rAvg += pixel.color.getRed();
                gAvg += pixel.color.getGreen();
                bAvg += pixel.color.getBlue();
                numberOfNonWhiteColors++;
            }
        }
        if(numberOfNonWhiteColors > 0){
            rAvg /= numberOfNonWhiteColors;
            gAvg /= numberOfNonWhiteColors;
            bAvg /= numberOfNonWhiteColors;
        } else {
            rAvg = 1;
            gAvg = 1;
            bAvg = 1;
        }
        return Color.rgb((int)(rAvg*255),(int)(gAvg*255),(int)(bAvg*255));
    }
    private Color getPixelInCanvasRatio(int x, int y){
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        return getPixelInCanvas(x, y, xRatio, yRatio);
    }
    
    private void updatePixelInCanvas(int xToUpdate, int yToUpdate, double xRatio, double yRatio){
        Color color = getPixelInCanvas(xToUpdate, yToUpdate, xRatio, yRatio);
        pixelWriter.setColor(xToUpdate+drawX, yToUpdate+drawY, color);
    }
    
    private void refresh(){
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        
        if(xRatio != 0 && yRatio != 0){
            for(int y = 0; y < drawHeight; ++y){
                for(int x = 0; x < drawWidth; ++x){
                    updatePixelInCanvas(x, y, xRatio, yRatio);
                }
            }
        }
    }
    
    @Override protected void onResize(){
        super.onResize();
        refresh();
    }
    
    private ArrayList<Pixel> getCorrespondingVirtualTablePixels(int xCanvas, int yCanvas, double xRatio, double yRatio){
        ArrayList<Pixel> pixels = new ArrayList<>();
        
        xRatio += 0.0001;
        yRatio += 0.0001;
        
        int startX = (int) Math.ceil(xCanvas/(xRatio));
        int startY = (int) Math.ceil(yCanvas/yRatio);
        
        int x = startX;
        while(x != maxWidth && Math.floor(x * xRatio) == xCanvas){
            int y = startY;
            while(y != maxHeight && Math.floor(y * yRatio) == yCanvas){
                pixels.add(new Pixel(x, y, virtualPixelTable.get(x + y*maxWidth)));
                y += 1;
            }
            x += 1;
        }

        return pixels;
    }
    
    
    private void updateVirtualTable(ArrayList<Pixel> drawnPixels, double xRatio, double yRatio){
        for (Pixel pixel : drawnPixels){
            ArrayList<Pixel> virtualPixels = getCorrespondingVirtualTablePixels(pixel.x, pixel.y, xRatio, yRatio);
            for (Pixel virtualPixel : virtualPixels){
                if(virtualPixel.color != pixel.color){
                    virtualPixelTable.set(virtualPixel.x+virtualPixel.y*maxWidth, pixel.color);
                }
            }
        }
    } 
    private HashSet<Pixel> updateVirtualTableGetCorespondingChanged(ArrayList<Pixel> drawnPixels, double xRatio, double yRatio){
        HashSet<Pixel> changedPoints = new HashSet<>();
        double myXRatio = (double)drawWidth / (double)maxWidth;
        double myYRatio = (double)drawHeight / (double)maxHeight;
        
        for (Pixel pixel : drawnPixels){
            ArrayList<Pixel> virtualPixels = getCorrespondingVirtualTablePixels(pixel.x, pixel.y, xRatio, yRatio);
            for (Pixel virtualPixel : virtualPixels){
                if(virtualPixel.color != pixel.color){
                    virtualPixelTable.set(virtualPixel.x+virtualPixel.y*maxWidth, pixel.color);
                    changedPoints.add(new Pixel((int)(virtualPixel.x*myXRatio), (int)(virtualPixel.y*myYRatio), pixel.color));
                }
            }
        }
        
        return changedPoints;
    } 
    
    private void drawPixel(
            ArrayList<Pixel> pixels, 
            int x, int y, 
            int drawX, int drawY, int drawWidth, int drawHeight, 
            int thickness, Color color
    ){
        thickness -= 1;
        int x1 = x-thickness-drawX;
        int x2 = x+thickness-drawX;
        int y1 = y-thickness-drawY;
        int y2 = y+thickness-drawY;
        
        for(int i = x1; i <= x2; ++i){
            for(int j = y1; j <= y2; ++j){
                if(i >= 0 && i < drawWidth && j >= 0 && j < drawHeight){
                    pixels.add(new Pixel(i ,j, color));
                }
            }
        }
    }
    
    private void drawPixelPartly(
            ArrayList<Pixel> pixels, 
            int x, int y, 
            int drawX, int drawY, int drawWidth, int drawHeight, 
            int thickness, Color color
    ){
        thickness -= 1;
        int x1 = x-thickness-drawX;
        int x2 = x+thickness-drawX;
        int y1 = y-thickness-drawY;
        int y2 = y+thickness-drawY;
        
        if(y1 >= 0 && y1 < drawHeight){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawWidth){
                    pixels.add(new Pixel(i, y1, color));
                }
            }
        }
        if(y2 >= 0 && y2 < drawHeight){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawWidth){
                    pixels.add(new Pixel(i, y2, color));
                }
            }
        }
        if(x1 >= 0 && x1 < drawWidth){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawHeight){
                    pixels.add(new Pixel(x1, i, color));
                }
            }
        }
        if(x2 >= 0 && x2 < drawWidth){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawHeight){
                    pixels.add(new Pixel(x2, i, color));
                }
            }
        }
    }
    
    private void drawLine(
            int x1, int y1, int x2, int y2, 
            int drawX, int drawY, int drawWidth, int drawHeight, 
            int lineThickness, Color color)
    {
        ArrayList<Pixel> drawnPixels = new ArrayList<>();
        
        int d, dx, dy, ai, bi, xi, yi;
        int x = x1, y = y1;
        if (x1 < x2){ 
            xi = 1;
            dx = x2 - x1;
        } else{ 
            xi = -1;
            dx = x1 - x2;
        }
        if (y1 < y2) { 
            yi = 1;
            dy = y2 - y1;
        } else { 
            yi = -1;
            dy = y1 - y2;
        }
        drawPixel(drawnPixels, x, y, drawX, drawY, drawWidth, drawHeight, lineThickness, color);
        if (dx > dy) {
            ai = (dy - dx) * 2;
            bi = dy * 2;
            d = bi - dx;
            while (x != x2) { 
                if (d >= 0) { 
                    x += xi;
                    y += yi;
                    d += ai;
                } else {
                    d += bi;
                    x += xi;
                }
                drawPixelPartly(drawnPixels, x, y, drawX, drawY, drawWidth, drawHeight, lineThickness, color);
            }
        } else { 
            ai = ( dx - dy ) * 2;
            bi = dx * 2;
            d = bi - dy;
            while (y != y2){ 
                if (d >= 0){ 
                    x += xi;
                    y += yi;
                    d += ai;
                }else{
                    d += bi;
                    y += yi;
                }
                drawPixelPartly(drawnPixels, x, y, drawX, drawY, drawWidth, drawHeight, lineThickness, color);
            }
        }
        
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight; 
        
    // if orginal writer
        updateVirtualTable(drawnPixels, xRatio, yRatio);
        for (Pixel pixel : drawnPixels){
            pixelWriter.setColor(pixel.x+drawX, pixel.y+drawY, pixel.color);
        }
        //sendToServer({x1, y1, x2, y2, drawX, drawY, drawWidth, drawHeight, lineThickness, color});
        
    // if got pixels from server
        /*HashSet<Pixel> changed = updateVirtualTableGetCorespondingChanged(drawnPixels, xRatio, yRatio);
        for (Pixel pixel : changed){
            pixelWriter.setColor(pixel.x+this.drawX, pixel.y+this.drawY, pixel.color);
        }*/
    }


    
    private void floodFill(int x, int y, Color replacementColor){
        if(x < drawX || x >= drawX+drawWidth || y < drawY || y >= drawY+drawHeight){
            return;
        }
        
    // if orginal writer
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        Pixel pixel = getCorrespondingVirtualTablePixels(x-drawX, y-drawY, xRatio, yRatio).get(0);
    // if got pixels from server
        //Pixel pixel = pixelFromServer;
        
        Color targetColor = pixel.color;
        if(targetColor.equals(replacementColor)){
            return;
        }
        
        ArrayDeque<Point> points = new ArrayDeque<>();
        Point point = new Point(pixel.x, pixel.y);
        points.add(point);
        while(!points.isEmpty()){
            Point w = points.pollFirst();
            if(replacementColor.equals(virtualPixelTable.get(w.x + w.y*maxWidth))){
                continue;
            }
            Point e = new Point(w.x, w.y);

            // for optimization
            int yIndex1 = w.y*maxWidth;
            int yIndex2 = (w.y+1)*maxWidth;
            int yIndex3 = (w.y-1)*maxWidth;

            while(w.x < maxWidth-1 && targetColor.equals(virtualPixelTable.get(w.x + yIndex1))){
                w.x += 1;
            }
            while(e.x > 0 && targetColor.equals(virtualPixelTable.get(e.x + yIndex1))){
                e.x -= 1;
            }
            while(w.x >= e.x){
                virtualPixelTable.set(w.x + yIndex1, replacementColor);
                if(w.y+1 < maxHeight && targetColor.equals(virtualPixelTable.get(w.x + yIndex2))){
                    points.add(new Point(w.x, w.y+1));
                }
                if(w.y-1 >= 0 && targetColor.equals(virtualPixelTable.get(w.x + yIndex3))){
                    points.add(new Point(w.x, w.y-1));
                }
                w.x -= 1;
            }
        }
        
        refresh();
        
    // if orginal writer
        //sendToServer({pixels.get(0), replacementColor});
    }
}

