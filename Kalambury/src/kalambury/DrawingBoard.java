package kalambury;

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
    Point mouseLastPos = new Point(0, 0);
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
            mouseLastPos.x = x;
            mouseLastPos.y = y;
            drawLineOwn(mouseLastPos, mouseLastPos);
            break;
        case COLOR_PICKER:
            colorWidget.setColor(getPixelInCanvasRatio(x-drawArea.x, y-drawArea.y));
            break;
        case BUCKET:
            floodFillOwn(x, y, colorWidget.getColor());
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
                drawLineOwn(mouseLastPos, new Point(x, y));
                mouseLastPos.x = x;
                mouseLastPos.y = y;
                break;
            case COLOR_PICKER:
                colorWidget.setColor(getPixelInCanvasRatio(x-drawArea.x, y-drawArea.y));
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
        return Color.color(rAvg, gAvg, bAvg);
    }
    private Color getPixelInCanvasRatio(int x, int y){
        double xRatio = (double)drawArea.w / (double)maxWidth;
        double yRatio = (double)drawArea.h / (double)maxHeight;
        return getPixelInCanvas(x, y, xRatio, yRatio);
    }
    
    private void updatePixelInCanvas(int xToUpdate, int yToUpdate, double xRatio, double yRatio){
        Color color = getPixelInCanvas(xToUpdate, yToUpdate, xRatio, yRatio);
        pixelWriter.setColor(xToUpdate+drawArea.x, yToUpdate+drawArea.y, color);
    }
    
    private void refresh(){
        double xRatio = (double)drawArea.w / (double)maxWidth;
        double yRatio = (double)drawArea.h / (double)maxHeight;
        
        if(xRatio != 0 && yRatio != 0){
            for(int y = 0; y < drawArea.h; ++y){
                for(int x = 0; x < drawArea.w; ++x){
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
        double myXRatio = (double)drawArea.w / (double)maxWidth;
        double myYRatio = (double)drawArea.h / (double)maxHeight;
        
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
    
    private void drawPixel(ArrayList<Pixel> pixels, int x, int y, Rect drawRect, int thickness, Color color){
        thickness -= 1;
        int x1 = x-thickness-drawRect.x;
        int x2 = x+thickness-drawRect.x;
        int y1 = y-thickness-drawRect.y;
        int y2 = y+thickness-drawRect.y;
        
        for(int i = x1; i <= x2; ++i){
            for(int j = y1; j <= y2; ++j){
                if(i >= 0 && i < drawRect.w && j >= 0 && j < drawRect.h){
                    pixels.add(new Pixel(i ,j, color));
                }
            }
        }
    }
    
    private void drawPixelPartly(ArrayList<Pixel> pixels, int x, int y, Rect drawRect, int thickness, Color color){
        thickness -= 1;
        int x1 = x-thickness-drawRect.x;
        int x2 = x+thickness-drawRect.x;
        int y1 = y-thickness-drawRect.y;
        int y2 = y+thickness-drawRect.y;
        
        if(y1 >= 0 && y1 < drawRect.h){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawRect.w){
                    pixels.add(new Pixel(i, y1, color));
                }
            }
        }
        if(y2 >= 0 && y2 < drawRect.h){
            for(int i = x1; i <= x2; ++i){
                if(i >= 0 && i < drawRect.w){
                    pixels.add(new Pixel(i, y2, color));
                }
            }
        }
        if(x1 >= 0 && x1 < drawRect.w){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawRect.h){
                    pixels.add(new Pixel(x1, i, color));
                }
            }
        }
        if(x2 >= 0 && x2 < drawRect.w){
            for(int i = y-thickness; i <= y+thickness; ++i){
                if(i >= 0 && i < drawRect.h){
                    pixels.add(new Pixel(x2, i, color));
                }
            }
        }
    }
    
    private LineDrawData drawLineOwn(Point start, Point end){
        ArrayList<Pixel> drawnPixels = drawLine(start, end, drawArea, lineThickness, colorWidget.getColor());
        
        double xRatio = (double)drawArea.w / (double)maxWidth;
        double yRatio = (double)drawArea.h / (double)maxHeight; 
        
        updateVirtualTable(drawnPixels, xRatio, yRatio);
        for (Pixel pixel : drawnPixels){
            pixelWriter.setColor(pixel.x+drawArea.x, pixel.y+drawArea.y, pixel.color);
        }
        
        return new LineDrawData(start, end, drawArea, lineThickness, colorWidget.getColor());
    }
    
    public void drawLineRemote(LineDrawData data){
        ArrayList<Pixel> drawnPixels = drawLine(data.startPoint, data.endPoint, data.drawRect, data.lineThickness, data.color);
        
        double xRatio = (double)data.drawRect.w / (double)maxWidth;
        double yRatio = (double)data.drawRect.h / (double)maxHeight; 
        
        HashSet<Pixel> changed = updateVirtualTableGetCorespondingChanged(drawnPixels, xRatio, yRatio);
        for (Pixel pixel : changed){
            pixelWriter.setColor(pixel.x+drawArea.x, pixel.y+drawArea.y, pixel.color);
        }
    }
    
    private ArrayList<Pixel> drawLine(Point start, Point end, Rect drawRect, int lineThickness, Color color){
        ArrayList<Pixel> drawnPixels = new ArrayList<>();
        int d, dx, dy, ai, bi, xi, yi;
        int x1 = start.x;
        int y1 = start.y;
        int x2 = end.x;
        int y2 = end.y;
        
        int x = x1;
        int y = y1;
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
        drawPixel(drawnPixels, x, y, drawRect, lineThickness, color);
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
                drawPixelPartly(drawnPixels, x, y, drawRect, lineThickness, color);
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
                drawPixelPartly(drawnPixels, x, y, drawRect, lineThickness, color);
            }
        }
        
        return drawnPixels;
    }


    private FloodFillData floodFillOwn(int x, int y, Color replacementColor){
        if(x < drawArea.x || x >= drawArea.x+drawArea.w || y < drawArea.y || y >= drawArea.y+drawArea.h){
            return null;
        }

        double xRatio = (double)drawArea.w / (double)maxWidth;
        double yRatio = (double)drawArea.h / (double)maxHeight;

        Pixel pixel = getCorrespondingVirtualTablePixels(x-drawArea.x, y-drawArea.y, xRatio, yRatio).get(0);
        
        Color targetColor = pixel.color;
        if(targetColor.equals(replacementColor)){
            return null;
        }
        
        floodFill(pixel, replacementColor);
        refresh();
        
        return new FloodFillData(pixel, replacementColor);
    }
    public void floodFillRemote(FloodFillData data){
        floodFill(data.pixel, data.replacementColor);
        refresh();
    }
    private void floodFill(Pixel pixel, Color replacementColor){
        Color targetColor = pixel.color;
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
    }
}

