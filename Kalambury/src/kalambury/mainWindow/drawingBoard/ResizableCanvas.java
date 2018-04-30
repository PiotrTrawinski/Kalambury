package kalambury.mainWindow.drawingBoard;

import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ResizableCanvas extends Canvas {
        private double aspectRatio = 1;
        protected Rect drawArea = new Rect(0, 0, 0, 0);
        
        public ResizableCanvas() {
            super();
            widthProperty().addListener(evt -> onResize());
            heightProperty().addListener(evt -> onResize());
            
            drawArea.w = (int)Math.floor(getWidth());
            drawArea.h = (int)Math.floor(getHeight());
        }

        private void setDrawingArea(){
            int width = (int)Math.floor(getWidth());
            int height = (int)Math.floor(getHeight());
            
            if((double)width/(double)height > aspectRatio){
                drawArea.w = (int)Math.floor(aspectRatio * height);
                drawArea.h = height;
                drawArea.x = (width - drawArea.w)/2;
                drawArea.y = 0;
            } else {
                drawArea.w = width;
                drawArea.h = (int)Math.floor((double)width/aspectRatio);
                drawArea.x = 0;
                drawArea.y = (height - drawArea.h)/2;
            }
        }
        
        protected void onResize() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, getWidth(), getHeight());
            setDrawingArea();
            
            gc.setFill(Color.web("#AAAAAA"));
            gc.fillRect(0, 0, getWidth(), getHeight());
        }
        
        
        public void setAspectRatio(double aspectRatio){
            this.aspectRatio = aspectRatio;
        }
        
        public void bindSize (
                ObservableValue<? extends Number> width,             
                ObservableValue<? extends Number> height)
        {
            widthProperty().bind(width);
            heightProperty().bind(height);
        }

        
        @Override public boolean isResizable() {
            return true;
        }
        @Override public double prefWidth(double height) {
            return getWidth();
        }
        @Override public double prefHeight(double width) {
            return getHeight();
        }
    }