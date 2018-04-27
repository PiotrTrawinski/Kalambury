package kalambury;

import java.util.ArrayList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class DrawingBoard extends ResizableCanvas{
    private final int maxWidth = 500;
    private final int maxHeight = 375;
    private final ArrayList<Integer> virtualPixelTable = new ArrayList<>();
    PixelWriter pixelWriter;
    //Canvas virtualTableView;
    boolean inDrawingMode = false;
    int mouseLastPosX = 0;
    int mouseLastPosY = 0;
    int lineThickness = 1;
    
    public DrawingBoard() {
        super();
        for(int i = 0; i < maxWidth*maxHeight; ++i){
            virtualPixelTable.add(255);
        }
        pixelWriter = getGraphicsContext2D().getPixelWriter();
    }
    
    public void startDrawing(int startX, int startY){
        inDrawingMode = true;
        mouseLastPosX = startX;
        mouseLastPosY = startY;
        createLine(startX, startY, startX, startY, lineThickness);
    }
    
    public void stopDrawing(){
        inDrawingMode = false;
    }
    
    public void mouseMovedTo(int x, int y){
        if(inDrawingMode){
            createLine(mouseLastPosX, mouseLastPosY, x, y, lineThickness);
            mouseLastPosX = x;
            mouseLastPosY = y;
        }
    }
    
    public void increaseLineThickness(){
        lineThickness++;
    }
    public void decreaseLineThickness(){
        lineThickness--;
        if(lineThickness < 0){
            lineThickness = 0;
        }
    }
    
    /*public void setVirtualTableCanvas(Canvas canvas){
        virtualTableView = canvas;
        GraphicsContext gc = virtualTableView.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, virtualTableView.getWidth(), virtualTableView.getHeight());
    }*/
    
    private void updatePixelInCanvas(int xToUpdate, int yToUpdate, double xRatio, double yRatio){
        ArrayList<Pixel> pixels = getCorrespondingVirtualTablePixels(xToUpdate, yToUpdate, xRatio, yRatio);
        int avarageColor = 0;
        int numberOfNonWhiteColors = 0;
        for(Pixel pixel : pixels){
            if(pixel.color != 255){
                avarageColor += pixel.color;
                numberOfNonWhiteColors++;
            }
        }
        if(numberOfNonWhiteColors > 0){
            avarageColor /= numberOfNonWhiteColors;
        } else {
            avarageColor = 255;
        }
        pixelWriter.setColor(xToUpdate+drawX, yToUpdate+drawY, Color.rgb(avarageColor,avarageColor,avarageColor));
    }
    
    @Override protected void onResize(){
        super.onResize();
        double xRatio = drawWidth / (double)maxWidth;
        double yRatio = drawHeight / (double)maxHeight;
        
        if(xRatio != 0 && yRatio != 0){
            for(int y = 0; y < Math.floor(drawHeight); ++y){
                for(int x = 0; x < Math.floor(drawWidth); ++x){
                    updatePixelInCanvas(x, y, xRatio, yRatio);
                }
            }
        }
    }
    
    private ArrayList<Pixel> getCorrespondingVirtualTablePixels(int xCanvas, int yCanvas, double xRatio, double yRatio){
        ArrayList<Pixel> pixels = new ArrayList<>();
        
        int startX = (int) Math.ceil(xCanvas/xRatio);
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
        /*PixelWriter pixelWriter = virtualTableView.getGraphicsContext2D().getPixelWriter();
        for(Pixel pixel : newPixels){
            pixelWriter.setColor(pixel.x, pixel.y, Color.rgb(pixel.color, pixel.color, pixel.color));
        }*/
    }
    
    private void createLine(int x1, int y1, int x2, int y2, int thickness){
        ArrayList<Pixel> drawnPixels = drawLine(x1, y1, x2, y2, thickness);
        
        double xRatio = (double)drawWidth / (double)maxWidth;
        double yRatio = (double)drawHeight / (double)maxHeight;
        
        ArrayList<Pixel> newPixelsToSend = new ArrayList<>();

        for (Pixel pixel : drawnPixels){
            pixelWriter.setColor(pixel.x+drawX, pixel.y+drawY, Color.rgb(pixel.color, pixel.color, pixel.color));
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
    
    private void drawPixel(ArrayList<Pixel> pixels, int x, int y, int color, int thickness){
        for(int i = x-thickness; i <= x+thickness; ++i){
            for(int j = y-thickness; j <= y+thickness; ++j){
                if(i >= drawX && i < drawX+Math.round(drawWidth) && j >= drawY && j < drawY+Math.round(drawHeight)){
                    pixels.add(new Pixel(i-drawX ,j-drawY, color));
                }
            }
        }
    }
    private ArrayList<Pixel> drawLine(int x1, int y1, int x2, int y2, int thickness){
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
        drawPixel(drawnPixels, x, y, 0, thickness);
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
                drawPixel(drawnPixels, x, y, 0, thickness);
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
                drawPixel(drawnPixels, x, y, 0, thickness);
            }
        }
        
        return drawnPixels;
    }
}

