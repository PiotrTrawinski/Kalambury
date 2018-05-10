package kalambury.mainWindow;

import javafx.scene.control.Label;

public class TurnLabel extends Label{
    private int currentTurn;
    private int currentSubTurn;
    private int numberOfFullTurns;
    private int numberOfSubTurns;
    
    public TurnLabel(){
        super();
        reset();
        setText("Tura");
    }
    
    private void updateText(){
        if(currentTurn == 0){
            setText("Tura");
        } else {
            String strCurrentTurn       = Integer.toString(currentTurn);
            String strCurrentSubTurn    = Integer.toString(currentSubTurn);
            String strNumberOfFullTurns = Integer.toString(numberOfFullTurns);
            String strNumberOfSubTurns  = Integer.toString(numberOfSubTurns);
            
            setText(strCurrentTurn+"."+strCurrentSubTurn+"/"+strNumberOfFullTurns+"."+strNumberOfSubTurns);
        }
    }
    
    private void reset(){
        currentTurn = 0;
        currentSubTurn = 0;
        numberOfFullTurns = 0;
        numberOfSubTurns = 0;
    }
    
    public void start(int numberOfSubTurns, int numberOfFullTurns){
        currentTurn = 1;
        currentSubTurn = 1;
        this.numberOfSubTurns = numberOfSubTurns;
        this.numberOfFullTurns = numberOfFullTurns;
        updateText();
        currentSubTurn = 0;
    }
    
    public void nextTurn(){
        currentSubTurn++;
        if(currentSubTurn > numberOfSubTurns){
            currentTurn++;
            currentSubTurn = 1;
        }
        if(currentTurn > numberOfFullTurns){
            reset();
        }
        updateText();
    }

    public void setNumberOfSubTurns(int numberOfSubTurns){
        this.numberOfSubTurns = numberOfSubTurns;
        updateText();
    }
}
