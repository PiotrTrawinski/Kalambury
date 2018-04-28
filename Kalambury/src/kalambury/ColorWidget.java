package kalambury;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class ColorWidget {
    private final Canvas chosenView;
    private final Canvas chooser;
    private final Slider brightnessSlider;
    private final Canvas brightnessCanvas;
    private final PixelWriter chooserPixelWriter;
    private final PixelWriter brightnessPixelWriter;
    
    private final GraphicsContext chosenViewGraphics;
    
    private boolean inChoosingMode = false;
    private Color chosenColor;
    private Color fullyChosenColor;
    
    private final Color[] colorChooserTable;
    
    public ColorWidget(
            Canvas chosenView, Pane chosenPane, // what color is chosen
            Canvas chooser, Pane chooserPane, // chosing color
            Slider brightnessSlider, Canvas brightnessCanvas, Pane brightnessPane // color brightness
    ){
        chosenColor = Color.rgb(0, 0, 0);
        this.chosenView = chosenView;
        this.chooser = chooser;
        this.brightnessSlider = brightnessSlider;
        this.brightnessCanvas = brightnessCanvas;
        chooserPixelWriter = chooser.getGraphicsContext2D().getPixelWriter();
        brightnessPixelWriter = this.brightnessCanvas.getGraphicsContext2D().getPixelWriter();
        chosenViewGraphics = chosenView.getGraphicsContext2D();
        colorChooserTable = new Color[(int)chooser.getWidth()*(int)chooser.getHeight()];
        
        brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue){
                updateChosenColor();
            }
        });
        
        double sectionSize = this.chooser.getWidth() / 6;
        double interval = 255/sectionSize;
        double startX = 0;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '1', '+', '0');
        startX += sectionSize;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '-', '1', '0');
        startX += sectionSize;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '0', '1', '+');
        startX += sectionSize;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '0', '-', '1');
        startX += sectionSize;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '+', '0', '1');
        startX += sectionSize;
        paintSection(chooserPixelWriter, (int)startX, (int)(startX+sectionSize), interval, '1', '0', '-');
    
        brightnessSlider.setValue(brightnessSlider.getMax()/4);
        chooseColor((int)(3*chooser.getWidth()/5), 0);
    }
    
    
    double paintSectionGetColorChannel(char arg, double value){
        switch (arg) {
            case '1': return 255;
            case '0': return 0;
            case '+': return value;
            case '-': return 255-value;
            default:  return -1;
        }
    }
    double paintSectionGetColorChange(char arg, double value, double height){
        switch (arg) {
            case '1': return -128.0/height;
            case '0': return 128.0/height;
            case '+': return (128.0-value)/height;
            case '-': return -(128.0-value)/height;
            default:  return -1;
        }
    }
    private void paintSection(PixelWriter pixelWriter, int startX, int koniecX, double interval, char rArg, char gArg, char bArg){
        for(int x = startX; x < koniecX; ++x){
            int value = (int)((x-startX)*interval);
            double rChg = paintSectionGetColorChange(rArg, value, this.chooser.getHeight());
            double gChg = paintSectionGetColorChange(gArg, value, this.chooser.getHeight());
            double bChg = paintSectionGetColorChange(bArg, value, this.chooser.getHeight());
            double r = paintSectionGetColorChannel(rArg, value);
            double g = paintSectionGetColorChannel(gArg, value);
            double b = paintSectionGetColorChannel(bArg, value);
            for(int y = 0; y < this.chooser.getHeight(); ++y){
                colorChooserTable[x + y*(int)chooser.getWidth()] = Color.rgb((int)r, (int)g, (int)b);
                pixelWriter.setColor(x, y, Color.rgb((int)r, (int)g, (int)b));
                r += rChg;
                g += gChg;
                b += bChg;
            }
        }
    }
    
    public void startChoosing(int x, int y){
        inChoosingMode = true;
        chooseColor(x, y);
    }

    public void stopChoosing(){
        inChoosingMode = false;
    }

    public void mouseMovedTo(int x, int y){
        if(inChoosingMode){
            chooseColor(x, y);
        }
    }
    
    public Color getColor(){
        return fullyChosenColor;
    }
    
    public void setColor(Color color){
        chosenColor = color;
        brightnessSlider.setValue(brightnessSlider.getMax()/2);
        updateChosenColor();
        updateBrightnessCanvas();
    }

    private void chooseColor(int x, int y){
        if(x >= 0 && x < (int)chooser.getWidth() && y >= 0 && y < (int)chooser.getHeight()){
            chosenColor = colorChooserTable[x + y*(int)chooser.getWidth()];
            updateChosenColor();
            updateBrightnessCanvas();
        }
    }
    
    private Color applyBrightnessToChosen(double brightness){
        int r = (int)(chosenColor.getRed()*255);
        int g = (int)(chosenColor.getGreen()*255);
        int b = (int)(chosenColor.getBlue()*255);
        
        double brightnessMultiplier = Math.abs(brightness-0.5)*2;
        if(brightness < 0.5){
            r -= r * brightnessMultiplier;
            g -= g * brightnessMultiplier;
            b -= b * brightnessMultiplier;
        } else {
            r += (255-r) * brightnessMultiplier;
            g += (255-g) * brightnessMultiplier;
            b += (255-b) * brightnessMultiplier;
        }
        
        return Color.rgb(r, g, b);
    }
    
    private void updateBrightnessCanvas(){
        for(int x = 0; x < this.brightnessCanvas.getWidth(); ++x){
            double brightness = ((double)x)/this.brightnessCanvas.getWidth();
            for(int y = 0; y < this.brightnessCanvas.getHeight(); ++y){
                brightnessPixelWriter.setColor(x, y, applyBrightnessToChosen(brightness));
            }
        }
    }
    
    private void updateChosenColor(){
        double brightness = brightnessSlider.getValue()/brightnessSlider.getMax();
        fullyChosenColor = applyBrightnessToChosen(brightness);
        chosenViewGraphics.setFill(fullyChosenColor);
        chosenViewGraphics.fillRect(0, 0, chosenView.getWidth(), chosenView.getHeight());
    }
}
