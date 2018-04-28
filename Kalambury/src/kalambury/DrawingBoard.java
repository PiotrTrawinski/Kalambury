package kalambury;

import java.util.ArrayList;
import java.util.LinkedList;
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
            createLine(x, y, x, y);
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
                createLine(mouseLastPosX, mouseLastPosY, x, y);
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
    
    private void sendToServer(ArrayList<Pixel> newPixels){
    }
    
    private ArrayList<Pixel> updateVirtualTable(ArrayList<Pixel> drawnPixels){
        ArrayList<Pixel> newPixels = new ArrayList<>();
        
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        
        for (Pixel pixel : drawnPixels){
            pixelWriter.setColor(pixel.x+drawX, pixel.y+drawY, pixel.color);
            ArrayList<Pixel> virtualPixels = getCorrespondingVirtualTablePixels(pixel.x, pixel.y, xRatio, yRatio);
            for (Pixel virtualPixel : virtualPixels){
                if(virtualPixel.color != pixel.color){
                    virtualPixel.color = pixel.color;
                    newPixels.add(virtualPixel);
                    virtualPixelTable.set(virtualPixel.x+virtualPixel.y*maxWidth, virtualPixel.color);
                }
            }
        }
        
        return newPixels;
    } 
    
    private void createLine(int x1, int y1, int x2, int y2){
        ArrayList<Pixel> drawnPixels = drawLine(x1, y1, x2, y2);
        ArrayList<Pixel> newPixelsToSend = updateVirtualTable(drawnPixels);
        sendToServer(newPixelsToSend);
    }
    
    private void drawPixel(ArrayList<Pixel> pixels, int x, int y){
        int thickness = lineThickness-1;
        int x1 = x-thickness-drawX;
        int x2 = x+thickness-drawX;
        int y1 = y-thickness-drawY;
        int y2 = y+thickness-drawY;
        
        for(int i = x1; i <= x2; ++i){
            for(int j = y1; j <= y2; ++j){
                if(i >= 0 && i < drawWidth && j >= 0 && j < drawHeight){
                    pixels.add(new Pixel(i ,j, colorWidget.getColor()));
                }
            }
        }
    }
    
    private void drawPixelPartly(ArrayList<Pixel> pixels, int x, int y){
        int thickness = lineThickness-1;
        int x1 = x-thickness-drawX;
        int x2 = x+thickness-drawX;
        int y1 = y-thickness-drawY;
        int y2 = y+thickness-drawY;
        
        if(y1 >= 0 && y1 < drawHeight){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawWidth){
                    pixels.add(new Pixel(i, y1, colorWidget.getColor()));
                }
            }
        }
        if(y2 >= 0 && y2 < drawHeight){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawWidth){
                    pixels.add(new Pixel(i, y2, colorWidget.getColor()));
                }
            }
        }
        if(x1 >= 0 && x1 < drawWidth){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawHeight){
                    pixels.add(new Pixel(x1, i, colorWidget.getColor()));
                }
            }
        }
        if(x2 >= 0 && x2 < drawWidth){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawHeight){
                    pixels.add(new Pixel(x2, i, colorWidget.getColor()));
                }
            }
        }
    }
    
    private ArrayList<Pixel> drawLine(int x1, int y1, int x2, int y2){
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
        drawPixel(drawnPixels, x, y);
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
                drawPixelPartly(drawnPixels, x, y);
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
                drawPixelPartly(drawnPixels, x, y);
            }
        }
        
        return drawnPixels;
    }

    
    private boolean isTargetColor(int x, int y, Color targetColor, double xRatio, double yRatio){
        return x < drawX+drawWidth && x >= drawX && y < drawY+drawHeight && y >= drawY
            && targetColor.equals(getPixelInCanvas(x-drawX, y-drawY, xRatio, yRatio));
    }
    private void floodFill(int x, int y, Color replacementColor){
        if(x < drawX || x >= drawX+drawWidth || y < drawY || y >= drawY+drawHeight){
            return;
        }
        
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        ArrayList<Pixel> pixels = getCorrespondingVirtualTablePixels(x-drawX, y-drawY, xRatio, yRatio);
        
        Color targetColor = pixels.get(0).color;
        if(targetColor.equals(replacementColor)){
            return;
        }
        
        LinkedList<Pixel> points = new LinkedList<>();
        points.add(pixels.get(0));
        while(!points.isEmpty()){
            int size = points.size();
            for(int i = 0; i < size; ++i){
                Pixel w = points.removeFirst();
                if(replacementColor.equals(virtualPixelTable.get(w.x + w.y*maxWidth))){
                    continue;
                }
                Pixel e = new Pixel(w.x, w.y, w.color);

                while(w.x < maxWidth-1 && targetColor.equals(virtualPixelTable.get(w.x + w.y*maxWidth))){
                    w.x += 1;
                }
                while(e.x > 0 && targetColor.equals(virtualPixelTable.get(e.x + e.y*maxWidth))){
                    e.x -= 1;
                }
                while(w.x >= e.x){
                    virtualPixelTable.set(w.x + w.y*maxWidth, replacementColor);
                    if(w.y+1 < maxHeight && targetColor.equals(virtualPixelTable.get(w.x + (w.y+1)*maxWidth))){
                        points.add(new Pixel(w.x, w.y+1, w.color));
                    }
                    if(w.y-1 >= 0 && targetColor.equals(virtualPixelTable.get(w.x + (w.y-1)*maxWidth))){
                        points.add(new Pixel(w.x, w.y-1, w.color));
                    }
                    w.x -= 1;
                }
            }
        }
        refresh();
    }
}

