package kalambury;

import java.util.ArrayList;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class DrawingBoard extends ResizableCanvas{
    private final int maxWidth = 3200;
    private final int maxHeight = 1800;
    private final ArrayList<Color> virtualPixelTable = new ArrayList<>();
    PixelWriter pixelWriter;
    ColorWidget colorWidget;
    boolean inDrawingMode = false;
    int mouseLastPosX = 0;
    int mouseLastPosY = 0;
    int lineThickness = 2;
    
    public DrawingBoard() {
        super();
        for(int i = 0; i < maxWidth*maxHeight; ++i){
            virtualPixelTable.add(Color.rgb(255,255,255));
        }
        pixelWriter = getGraphicsContext2D().getPixelWriter();
    }
    
    public void startDrawing(int startX, int startY){
        inDrawingMode = true;
        mouseLastPosX = startX;
        mouseLastPosY = startY;
        createLine(startX, startY, startX, startY);
    }
    
    public void stopDrawing(){
        inDrawingMode = false;
    }
    
    public void mouseMovedTo(int x, int y){
        if(inDrawingMode){
            createLine(mouseLastPosX, mouseLastPosY, x, y);
            mouseLastPosX = x;
            mouseLastPosY = y;
        }
    }

    public void setLineThickness(int newLineThickness){
        lineThickness = newLineThickness;
    }
    
    public void setColorWidget(ColorWidget colorWidget){
        this.colorWidget = colorWidget;
    }
    
    private void updatePixelInCanvas(int xToUpdate, int yToUpdate, double xRatio, double yRatio){
        ArrayList<Pixel> pixels = getCorrespondingVirtualTablePixels(xToUpdate, yToUpdate, xRatio, yRatio);
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
        pixelWriter.setColor(xToUpdate+drawX, yToUpdate+drawY, Color.rgb((int)(rAvg*255),(int)(gAvg*255),(int)(bAvg*255)));
    }
    
    @Override protected void onResize(){
        super.onResize();
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
    
    private void createLine(int x1, int y1, int x2, int y2){
        ArrayList<Pixel> drawnPixels = drawLine(x1, y1, x2, y2);
        
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        
        ArrayList<Pixel> newPixelsToSend = new ArrayList<>();

        for (Pixel pixel : drawnPixels){
            pixelWriter.setColor(pixel.x+drawX, pixel.y+drawY, pixel.color);
            ArrayList<Pixel> virtualPixels = getCorrespondingVirtualTablePixels(pixel.x, pixel.y, xRatio, yRatio);
            for (Pixel virtualPixel : virtualPixels){
                if(virtualPixel.color != pixel.color){
                    virtualPixel.color = pixel.color;
                    newPixelsToSend.add(virtualPixel);
                    virtualPixelTable.set(virtualPixel.x+virtualPixel.y*maxWidth, virtualPixel.color);
                }
            }
        }
        
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
}

