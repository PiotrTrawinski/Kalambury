package kalambury;

import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ResizableCanvas extends Canvas {
        private double aspectRatio = 1;
        
        protected int drawX = 0;
        protected int drawY = 0;
        protected double drawWidth  = 0;
        protected double drawHeight = 0;
    
        
        public ResizableCanvas() {
            super();
            widthProperty().addListener(evt -> onResize());
            heightProperty().addListener(evt -> onResize());
            
            drawWidth = getWidth();
            drawHeight = getHeight();
        }

        private void setDrawingArea(){
            if(getWidth()/getHeight() > aspectRatio){
                drawWidth = aspectRatio*getHeight();
                drawHeight = getHeight();
                drawX = (int)((getWidth() - drawWidth)/2);
                drawY = 0;
            } else {
                drawWidth = getWidth();
                drawHeight = getWidth()/aspectRatio;
                drawX = 0;
                drawY = (int)((getHeight() - drawHeight)/2);
            }
        }
        
        protected void onResize() {
            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, getWidth(), getHeight());
            setDrawingArea();
            
            gc.setFill(Color.BLUEVIOLET);
            gc.fillRect(0, 0, getWidth(), getHeight());
            
            gc.setFill(Color.GRAY);
            gc.fillRect(drawX, drawY, drawWidth, drawHeight);
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