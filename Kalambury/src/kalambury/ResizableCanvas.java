package kalambury;

import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ResizableCanvas extends Canvas {
        private double aspectRatio = 1;
        
        protected int drawX = 0;
        protected int drawY = 0;
        protected int drawWidth  = 0;
        protected int drawHeight = 0;
    
        
        public ResizableCanvas() {
            super();
            widthProperty().addListener(evt -> onResize());
            heightProperty().addListener(evt -> onResize());
            
            drawWidth  = (int)Math.floor(getWidth());
            drawHeight = (int)Math.floor(getHeight());
        }

        private void setDrawingArea(){
            int width = (int)Math.floor(getWidth());
            int height = (int)Math.floor(getHeight());
            
            if((double)width/(double)height > aspectRatio){
                drawWidth  = (int)Math.floor(aspectRatio * height);
                drawHeight = height;
                drawX = (width - drawWidth)/2;
                drawY = 0;
            } else {
                drawWidth = width;
                drawHeight = (int)Math.floor((double)width/aspectRatio);
                drawX = 0;
                drawY = (height - drawHeight)/2;
            }
        }
        
        protected void onResize() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, getWidth(), getHeight());
            setDrawingArea();
            
            gc.setFill(Color.web("#C8C8C8"));
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